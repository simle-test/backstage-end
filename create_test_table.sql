-- 创建 test 表（与 questions_total 表结构相同，用于测试导入功能）
CREATE TABLE IF NOT EXISTS test (
    id SERIAL PRIMARY KEY,
    question_id VARCHAR(20),
    title TEXT,
    category_id VARCHAR(50),
    category_name VARCHAR(50),
    difficulty VARCHAR(10),
    year INTEGER,
    source VARCHAR(255),
    status VARCHAR(20),
    question_content JSONB,
    correct_answer VARCHAR(10),
    answer_analysis TEXT,
    tips TEXT,
    has_image BOOLEAN DEFAULT FALSE,
    image_url VARCHAR(255),
    has_material BOOLEAN DEFAULT FALSE,
    material_id INTEGER,
    content_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_test_question_id ON test(question_id);
CREATE INDEX IF NOT EXISTS idx_test_category_id ON test(category_id);
CREATE INDEX IF NOT EXISTS idx_test_created_at ON test(created_at);

-- 添加注释
COMMENT ON TABLE test IS '测试题目表，用于测试导入功能，结构与 questions_total 相同';
COMMENT ON COLUMN test.id IS '主键ID';
COMMENT ON COLUMN test.question_id IS '题目ID';
COMMENT ON COLUMN test.title IS '题目标题';
COMMENT ON COLUMN test.category_id IS '分类ID';
COMMENT ON COLUMN test.category_name IS '分类名称';
COMMENT ON COLUMN test.difficulty IS '难度';
COMMENT ON COLUMN test.year IS '年份';
COMMENT ON COLUMN test.source IS '来源';
COMMENT ON COLUMN test.status IS '状态';
COMMENT ON COLUMN test.question_content IS '题目内容（JSON格式）';
COMMENT ON COLUMN test.correct_answer IS '正确答案';
COMMENT ON COLUMN test.answer_analysis IS '答案解析';
COMMENT ON COLUMN test.tips IS '提示';
COMMENT ON COLUMN test.has_image IS '是否有图片';
COMMENT ON COLUMN test.image_url IS '图片URL';
COMMENT ON COLUMN test.has_material IS '是否有材料';
COMMENT ON COLUMN test.material_id IS '材料ID';
COMMENT ON COLUMN test.content_text IS '材料内容文本';
COMMENT ON COLUMN test.created_at IS '创建时间';
COMMENT ON COLUMN test.updated_at IS '更新时间';