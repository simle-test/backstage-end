package com.example.backstage.service.impl;

import com.example.backstage.dto.DeduplicationRequest;
import com.example.backstage.dto.DeduplicationResult;
import com.example.backstage.dto.DuplicateDetail;
import com.example.backstage.entity.Question;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.service.DeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class DeduplicationServiceImpl implements DeduplicationService {

    private static final Logger log = LoggerFactory.getLogger(DeduplicationServiceImpl.class);
    
    private static final int BATCH_SIZE = 1000;
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.9;
    
    private final QuestionRepository questionRepository;

    public DeduplicationServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }

    @Override
    public DeduplicationResult detectDuplicates(DeduplicationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始检测重复题目，模式: {}, 字段: {}", request.getMode(), request.getFields());
        
        DeduplicationResult result = new DeduplicationResult();
        result.setMode(request.getMode());
        result.setFieldsUsed(request.getFields());
        
        List<Question> questions = fetchQuestions(request);
        result.setTotalCount(questions.size());
        
        if (questions.isEmpty()) {
            result.setMessage("没有找到匹配的题目");
            result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }
        
        List<DuplicateDetail> duplicates;
        List<DeduplicationResult.DuplicateGroup> groups;
        
        if ("exact".equalsIgnoreCase(request.getMode())) {
            Map<List<String>, List<Question>> groupsMap = findExactDuplicates(questions, request.getFields(), request);
            duplicates = extractDuplicates(groupsMap, null);
            groups = buildDuplicateGroups(groupsMap, null);
        } else {
            double threshold = request.getSimilarityThreshold() != null 
                ? request.getSimilarityThreshold() : DEFAULT_SIMILARITY_THRESHOLD;
            List<Pair<Question, Question>> similarPairs = findSimilarDuplicates(questions, request.getFields(), threshold, request);
            
            Map<Integer, List<Question>> groupsMap = buildSimilarGroups(similarPairs, questions);
            duplicates = extractSimilarDuplicates(groupsMap, threshold);
            groups = buildSimilarDuplicateGroups(groupsMap, threshold);
        }
        
        result.setDuplicateCount(duplicates.size());
        result.setUniqueCount(questions.size() - duplicates.size());
        result.setDuplicates(duplicates);
        result.setDuplicateGroups(groups);
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        
        log.info("检测完成，总数: {}, 重复数: {}, 耗时: {}ms", 
            questions.size(), duplicates.size(), result.getProcessingTimeMs());
        
        return result;
    }

    @Override
    @Transactional
    public DeduplicationResult removeDuplicates(DeduplicationRequest request) {
        long startTime = System.currentTimeMillis();
        log.info("开始移除重复题目，模式: {}", request.getMode());
        
        DeduplicationResult result = detectDuplicates(request);
        
        if (result.getDuplicateCount() == 0) {
            result.setMessage("没有找到重复题目，无需移除");
            return result;
        }
        
        Set<Integer> duplicateIds = result.getDuplicates().stream()
            .map(DuplicateDetail::getId)
            .collect(Collectors.toSet());
        
        int removedCount = 0;
        List<String> errors = new ArrayList<>();
        
        List<List<Integer>> batches = batchList(new ArrayList<>(duplicateIds), BATCH_SIZE);
        for (List<Integer> batch : batches) {
            try {
                questionRepository.deleteAllById(batch);
                removedCount += batch.size();
                log.info("已删除 {} 条重复记录", batch.size());
            } catch (Exception e) {
                String error = "删除批次失败: " + e.getMessage();
                errors.add(error);
                log.error(error, e);
            }
        }
        
        result.setRemovedCount(removedCount);
        result.setRemovalRate(result.getTotalCount() > 0 ? 
            (double) removedCount / result.getTotalCount() * 100 : 0);
        result.setProcessingTimeMs(System.currentTimeMillis() - startTime);
        result.setMessage(String.format("成功移除 %d 条重复记录", removedCount));
        
        log.info("移除完成，删除: {}, 耗时: {}ms", removedCount, result.getProcessingTimeMs());
        
        return result;
    }

    @Override
    public DeduplicationResult previewDeduplication(DeduplicationRequest request) {
        log.info("预览去重结果");
        return detectDuplicates(request);
    }

    @Override
    public List<DuplicateDetail> findDuplicatesByField(String fieldName, String fieldValue) {
        List<Question> questions = fetchAllQuestions();
        
        return questions.stream()
            .filter(q -> {
                String value = getFieldValue(q, fieldName);
                return value != null && value.equals(fieldValue);
            })
            .map(this::convertToDetail)
            .collect(Collectors.toList());
    }

    @Override
    public long countDuplicates() {
        DeduplicationRequest request = new DeduplicationRequest();
        request.setMode("exact");
        request.setFields(Arrays.asList("title"));
        
        DeduplicationResult result = detectDuplicates(request);
        return result.getDuplicateCount();
    }

    private List<Question> fetchQuestions(DeduplicationRequest request) {
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE, Sort.by(Sort.Direction.ASC, "id"));
        
        if (request.getCategoryFilter() != null && !request.getCategoryFilter().isEmpty()) {
            return questionRepository.findAll().stream()
                .filter(q -> request.getCategoryFilter().equals(q.getCategoryId()))
                .collect(Collectors.toList());
        }
        
        return questionRepository.findAll();
    }

    private List<Question> fetchAllQuestions() {
        return questionRepository.findAll();
    }

    private Map<List<String>, List<Question>> findExactDuplicates(List<Question> questions, 
            List<String> fields, DeduplicationRequest request) {
        
        Map<List<String>, List<Question>> groups = new ConcurrentHashMap<>();
        
        questions.parallelStream().forEach(question -> {
            List<String> keyValues = fields.stream()
                .map(field -> normalizeValue(getFieldValue(question, field), request))
                .collect(Collectors.toList());
            
            groups.computeIfAbsent(keyValues, k -> new ArrayList<>()).add(question);
        });
        
        return groups.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 1)
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private List<Pair<Question, Question>> findSimilarDuplicates(List<Question> questions, 
            List<String> fields, double threshold, DeduplicationRequest request) {
        
        List<Pair<Question, Question>> similarPairs = Collections.synchronizedList(new ArrayList<>());
        int size = questions.size();
        
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                Question q1 = questions.get(i);
                Question q2 = questions.get(j);
                
                double similarity = calculateSimilarity(q1, q2, fields, request);
                if (similarity >= threshold) {
                    similarPairs.add(new Pair<>(q1, q2, similarity));
                }
            }
        }
        
        return similarPairs;
    }

    private Map<Integer, List<Question>> buildSimilarGroups(List<Pair<Question, Question>> pairs, 
            List<Question> allQuestions) {
        
        Map<Integer, Integer> groupIdMap = new HashMap<>();
        Map<Integer, List<Question>> groups = new HashMap<>();
        AtomicInteger nextGroupId = new AtomicInteger(1);
        
        for (Pair<Question, Question> pair : pairs) {
            Integer g1 = groupIdMap.get(pair.first.getId());
            Integer g2 = groupIdMap.get(pair.second.getId());
            
            if (g1 == null && g2 == null) {
                int newId = nextGroupId.getAndIncrement();
                groupIdMap.put(pair.first.getId(), newId);
                groupIdMap.put(pair.second.getId(), newId);
                groups.put(newId, new ArrayList<>(Arrays.asList(pair.first, pair.second)));
            } else if (g1 != null && g2 == null) {
                groupIdMap.put(pair.second.getId(), g1);
                groups.get(g1).add(pair.second);
            } else if (g1 == null && g2 != null) {
                groupIdMap.put(pair.first.getId(), g2);
                groups.get(g2).add(pair.first);
            } else if (!g1.equals(g2)) {
                List<Question> merged = new ArrayList<>(groups.get(g1));
                merged.addAll(groups.get(g2));
                groups.put(g1, merged);
                groups.remove(g2);
                groupIdMap.forEach((k, v) -> {
                    if (v.equals(g2)) groupIdMap.put(k, g1);
                });
            }
        }
        
        return groups;
    }

    private double calculateSimilarity(Question q1, Question q2, List<String> fields, 
            DeduplicationRequest request) {
        
        double totalSimilarity = 0.0;
        int fieldCount = 0;
        
        for (String field : fields) {
            String v1 = normalizeValue(getFieldValue(q1, field), request);
            String v2 = normalizeValue(getFieldValue(q2, field), request);
            
            if (v1 != null && v2 != null) {
                totalSimilarity += levenshteinSimilarity(v1, v2);
                fieldCount++;
            }
        }
        
        return fieldCount > 0 ? totalSimilarity / fieldCount : 0.0;
    }

    private double levenshteinSimilarity(String s1, String s2) {
        if (s1.equals(s2)) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;
        
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) dp[i][0] = i;
        for (int j = 0; j <= s2.length(); j++) dp[0][j] = j;
        
        for (int i = 1; i <= s1.length(); i++) {
            for (int j = 1; j <= s2.length(); j++) {
                int cost = s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1;
                dp[i][j] = Math.min(Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1), dp[i - 1][j - 1] + cost);
            }
        }
        
        int maxLen = Math.max(s1.length(), s2.length());
        return 1.0 - (double) dp[s1.length()][s2.length()] / maxLen;
    }

    private String normalizeValue(String value, DeduplicationRequest request) {
        if (value == null) return null;
        
        String result = value;
        
        if (request.getTrimWhitespace() == null || request.getTrimWhitespace()) {
            result = result.trim().replaceAll("\\s+", "");
        }
        
        if (request.getIgnoreCase() == null || request.getIgnoreCase()) {
            result = result.toLowerCase();
        }
        
        return result;
    }

    private String getFieldValue(Question question, String fieldName) {
        switch (fieldName.toLowerCase()) {
            case "title":
                return question.getTitle();
            case "questioncontent":
            case "question_content":
                return question.getQuestionContent();
            case "correctanswer":
            case "correct_answer":
                return question.getCorrectAnswer();
            case "answeranalysis":
            case "answer_analysis":
                return question.getAnswerAnalysis();
            case "categoryid":
            case "category_id":
                return question.getCategoryId();
            case "categoryname":
            case "category_name":
                return question.getCategoryName();
            case "source":
                return question.getSource();
            case "questionid":
            case "question_id":
                return question.getQuestionId();
            case "contenttext":
            case "content_text":
                return question.getContentText();
            default:
                log.warn("未知字段: {}", fieldName);
                return null;
        }
    }

    private List<DuplicateDetail> extractDuplicates(Map<List<String>, List<Question>> groups, 
            Double similarityThreshold) {
        
        List<DuplicateDetail> duplicates = new ArrayList<>();
        
        for (List<Question> group : groups.values()) {
            if (group.size() <= 1) continue;
            
            Question representative = group.get(0);
            
            for (int i = 1; i < group.size(); i++) {
                DuplicateDetail detail = convertToDetail(group.get(i));
                detail.setDuplicateOfId(representative.getId());
                if (similarityThreshold != null) {
                    detail.setSimilarityScore(similarityThreshold);
                }
                duplicates.add(detail);
            }
        }
        
        return duplicates;
    }

    private List<DuplicateDetail> extractSimilarDuplicates(Map<Integer, List<Question>> groups, 
            Double similarityThreshold) {
        
        List<DuplicateDetail> duplicates = new ArrayList<>();
        
        for (List<Question> group : groups.values()) {
            if (group.size() <= 1) continue;
            
            Question representative = group.get(0);
            
            for (int i = 1; i < group.size(); i++) {
                DuplicateDetail detail = convertToDetail(group.get(i));
                detail.setDuplicateOfId(representative.getId());
                detail.setSimilarityScore(similarityThreshold);
                duplicates.add(detail);
            }
        }
        
        return duplicates;
    }

    private List<DeduplicationResult.DuplicateGroup> buildDuplicateGroups(
            Map<List<String>, List<Question>> groups, Double similarityThreshold) {
        
        List<DeduplicationResult.DuplicateGroup> resultGroups = new ArrayList<>();
        
        for (List<Question> group : groups.values()) {
            if (group.size() <= 1) continue;
            
            Question representative = group.get(0);
            DeduplicationResult.DuplicateGroup groupDTO = new DeduplicationResult.DuplicateGroup();
            groupDTO.setRepresentativeId(representative.getId());
            groupDTO.setRepresentativeTitle(truncate(representative.getTitle(), 50));
            groupDTO.setGroupSize(group.size());
            
            List<DuplicateDetail> duplicates = new ArrayList<>();
            for (int i = 1; i < group.size(); i++) {
                DuplicateDetail detail = convertToDetail(group.get(i));
                detail.setDuplicateOfId(representative.getId());
                if (similarityThreshold != null) {
                    detail.setSimilarityScore(similarityThreshold);
                }
                duplicates.add(detail);
            }
            groupDTO.setDuplicates(duplicates);
            
            resultGroups.add(groupDTO);
        }
        
        return resultGroups;
    }

    private List<DeduplicationResult.DuplicateGroup> buildSimilarDuplicateGroups(
            Map<Integer, List<Question>> groups, Double similarityThreshold) {
        
        List<DeduplicationResult.DuplicateGroup> resultGroups = new ArrayList<>();
        
        for (List<Question> group : groups.values()) {
            if (group.size() <= 1) continue;
            
            Question representative = group.get(0);
            DeduplicationResult.DuplicateGroup groupDTO = new DeduplicationResult.DuplicateGroup();
            groupDTO.setRepresentativeId(representative.getId());
            groupDTO.setRepresentativeTitle(truncate(representative.getTitle(), 50));
            groupDTO.setGroupSize(group.size());
            
            List<DuplicateDetail> duplicates = new ArrayList<>();
            for (int i = 1; i < group.size(); i++) {
                DuplicateDetail detail = convertToDetail(group.get(i));
                detail.setDuplicateOfId(representative.getId());
                detail.setSimilarityScore(similarityThreshold);
                duplicates.add(detail);
            }
            groupDTO.setDuplicates(duplicates);
            
            resultGroups.add(groupDTO);
        }
        
        return resultGroups;
    }

    private DuplicateDetail convertToDetail(Question question) {
        DuplicateDetail detail = new DuplicateDetail();
        detail.setId(question.getId());
        detail.setQuestionId(question.getQuestionId());
        detail.setTitle(truncate(question.getTitle(), 100));
        detail.setCorrectAnswer(question.getCorrectAnswer());
        detail.setCategoryName(question.getCategoryName());
        detail.setSource(question.getSource());
        detail.setCreatedAt(question.getCreatedAt());
        return detail;
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }

    private <T> List<List<T>> batchList(List<T> list, int batchSize) {
        List<List<T>> batches = new ArrayList<>();
        for (int i = 0; i < list.size(); i += batchSize) {
            batches.add(list.subList(i, Math.min(i + batchSize, list.size())));
        }
        return batches;
    }

    private static class Pair<T, U> {
        final T first;
        final U second;
        final Double similarity;
        
        Pair(T first, U second) {
            this.first = first;
            this.second = second;
            this.similarity = null;
        }
        
        Pair(T first, U second, Double similarity) {
            this.first = first;
            this.second = second;
            this.similarity = similarity;
        }
    }
}