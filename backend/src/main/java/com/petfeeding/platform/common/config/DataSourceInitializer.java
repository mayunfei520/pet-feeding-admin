package com.petfeeding.platform.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据库初始化（自动建表，仅开发环境用）
 * 生产环境由 MySQL 自行维护表结构，不再每次启动都执行建表语句
 */
@Slf4j
@Component
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!prod")
public class DataSourceInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("初始化数据库表结构...");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "username VARCHAR(50) NOT NULL UNIQUE," +
            "password VARCHAR(255) NOT NULL," +
            "phone VARCHAR(20)," +
            "email VARCHAR(100)," +
            "gender VARCHAR(10)," +
            "real_name VARCHAR(20)," +
            "role VARCHAR(20) NOT NULL DEFAULT 'OWNER'," +
            "avatar VARCHAR(500)," +
            "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS pets (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "user_id BIGINT NOT NULL," +
            "name VARCHAR(50) NOT NULL," +
            "species VARCHAR(20) NOT NULL," +
            "breed VARCHAR(50)," +
            "age INT," +
            "weight DECIMAL(5,2)," +
            "medical_notes VARCHAR(500)," +
            "vaccinated TINYINT," +
            "image VARCHAR(500)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS feeders (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "user_id BIGINT NOT NULL UNIQUE," +
            "real_name VARCHAR(20) NOT NULL," +
            "id_card VARCHAR(256) NOT NULL," +
            "service_area VARCHAR(100) NOT NULL," +
            "experience VARCHAR(500)," +
            "description VARCHAR(500)," +
            "certification VARCHAR(1000)," +
            "status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
            "rating DECIMAL(3,2) DEFAULT 5.00," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS orders (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "order_no VARCHAR(30) NOT NULL UNIQUE," +
            "owner_id BIGINT NOT NULL," +
            "feeder_id BIGINT," +
            "pet_id BIGINT NOT NULL," +
            "service_date DATE NOT NULL," +
            "service_period VARCHAR(10) NOT NULL," +
            "status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
            "address VARCHAR(200) NOT NULL," +
            "notes VARCHAR(500)," +
            "price DECIMAL(10,2) NOT NULL," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS reviews (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "order_id BIGINT NOT NULL UNIQUE," +
            "owner_id BIGINT NOT NULL," +
            "feeder_id BIGINT NOT NULL," +
            "rating TINYINT NOT NULL," +
            "content VARCHAR(1000)," +
            "images VARCHAR(2000)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS payments (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "order_id BIGINT NOT NULL," +
            "user_id BIGINT NOT NULL," +
            "amount DECIMAL(10,2) NOT NULL," +
            "pay_method VARCHAR(20) NOT NULL," +
            "pay_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID'," +
            "transaction_id VARCHAR(100)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sms_logs (" +
            "id BIGINT AUTO_INCREMENT PRIMARY KEY," +
            "order_id BIGINT," +
            "feeder_id BIGINT," +
            "phone VARCHAR(20)," +
            "content VARCHAR(1000)," +
            "status VARCHAR(20)," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")");

        // 插入种子数据（仅当 users 表为空时）
        int userCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users", Integer.class);
        if (userCount == 0) {
            log.info("插入种子数据...");
            String encodedPwd = passwordEncoder.encode("123456");
            jdbcTemplate.update("INSERT INTO users (id, username, password, phone, email, role, status) VALUES " +
                "(1, 'admin', '" + encodedPwd + "', '13800000001', 'admin@pet.com', 'ADMIN', 'ACTIVE')," +
                "(2, 'zhangsan', '" + encodedPwd + "', '13800000002', 'zhangsan@pet.com', 'OWNER', 'ACTIVE')," +
                "(3, 'lisi', '" + encodedPwd + "', '13800000003', 'lisi@pet.com', 'OWNER', 'ACTIVE')," +
                "(4, 'feeder01', '" + encodedPwd + "', '13800000004', 'feeder01@pet.com', 'FEEDER', 'ACTIVE')");

            jdbcTemplate.update("INSERT INTO pets (id, user_id, name, species, breed, age, weight) VALUES " +
                "(1, 2, '花花', '猫', '英短', 2, 4.5)," +
                "(2, 2, '旺财', '狗', '金毛', 3, 25.0)," +
                "(3, 3, '豆豆', '猫', '布偶', 1, 3.8)");

            jdbcTemplate.update("INSERT INTO feeders (id, user_id, real_name, id_card, service_area, experience, description, status, rating) VALUES " +
                "(1, 4, '李明', '110101199001011234', '朝阳区', '3年宠物喂养经验', '本人热爱小动物，有多年猫咪和狗狗喂养经验。', 'APPROVED', 4.80)");

            jdbcTemplate.update("INSERT INTO orders (id, order_no, owner_id, feeder_id, pet_id, service_date, service_period, status, address, price) VALUES " +
                "(1, 'SF20240101001', 2, 1, 1, '2026-07-01', 'MORNING', 'CONFIRMED', '北京市朝阳区安贞路10号', 80.00)," +
                "(2, 'SF20240101002', 2, 1, 2, '2026-07-02', 'AFTERNOON', 'PENDING', '北京市朝阳区安贞路10号', 120.00)," +
                "(3, 'SF20240101003', 3, NULL, 3, '2026-07-05', 'EVENING', 'PENDING', '北京市海淀区中关村大街1号', 80.00)");

            jdbcTemplate.update("INSERT INTO reviews (id, order_id, owner_id, feeder_id, rating, content) VALUES " +
                "(1, 1, 2, 1, 5, '非常专业，花花被照顾得很好！')");

            jdbcTemplate.update("INSERT INTO payments (id, order_id, user_id, amount, pay_method, pay_status, transaction_id) VALUES " +
                "(1, 1, 2, 80.00, 'WECHAT', 'PAID', 'WX2024010112345678')");

            log.info("种子数据插入完成");
        }

        log.info("数据库表初始化完成");
    }
}
