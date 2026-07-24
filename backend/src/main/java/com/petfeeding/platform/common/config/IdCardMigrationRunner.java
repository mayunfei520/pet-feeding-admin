package com.petfeeding.platform.common.config;

import com.petfeeding.platform.common.util.Sm4Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * 启动时将 feeders 表中明文身份证号迁移为 SM4 密文（幂等，已加密的跳过）。
 * 使用 JdbcTemplate 直读原始列值，绕开 TypeHandler，避免对已是密文的数据二次加密。
 */
@Component
@Order(1000)
public class IdCardMigrationRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private Sm4Util sm4Util;

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.query("SELECT id, id_card FROM feeders", (rs, rowNum) -> {
                long id = rs.getLong("id");
                String idCard = rs.getString("id_card");
                if (idCard != null && !sm4Util.isEncrypted(idCard)) {
                    jdbcTemplate.update("UPDATE feeders SET id_card = ? WHERE id = ?",
                            sm4Util.encrypt(idCard), id);
                }
                return null;
            });
        } catch (Exception e) {
            // 表尚未初始化（如 dev 启动顺序）时跳过；生产环境表已存在不会触发
        }
    }
}
