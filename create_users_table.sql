-- 用户表创建脚本
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(20),
    role VARCHAR(20) NOT NULL DEFAULT 'user',
    avatar VARCHAR(255),
    avatar_color VARCHAR(50),
    status VARCHAR(20) NOT NULL DEFAULT 'active',
    practice_count INT NOT NULL DEFAULT 0,
    pass_rate NUMERIC(5,2) NOT NULL DEFAULT 0,
    join_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_role ON users(role);
CREATE INDEX IF NOT EXISTS idx_status ON users(status);

-- 插入默认管理员用户（密码为 admin123 的 BCrypt 加密值）
INSERT INTO users (username, password, email, role, status) 
SELECT 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye.IjzqAKL9xL5jvMFVdNJHvGCgTq/VEq', 'admin@example.com', 'admin', 'active'
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');
