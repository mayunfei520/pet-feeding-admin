package com.petfeeding.platform.common.util;

import cn.hutool.core.util.HexUtil;
import cn.hutool.crypto.Mode;
import cn.hutool.crypto.Padding;
import cn.hutool.crypto.symmetric.SM4;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Security;

/**
 * 国密 SM4 工具（身份证号等敏感字段加密存储）
 * 算法：SM4 / CBC / PKCS5Padding(对 SM4 等价于 PKCS7Padding)，密钥 16 字节（由 sm4.key 配置，生产环境经环境变量注入）
 */
@Component
public class Sm4Util {

    private static Sm4Util INSTANCE;

    static {
        // SM4 是国密算法，标准 JDK 的 JCE 不内置，必须注册 Bouncy Castle provider 才能使用
        if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
            Security.addProvider(new BouncyCastleProvider());
        }
    }

    private final SM4 sm4;

    public Sm4Util(@Value("${sm4.key}") String keyHex) {
        byte[] keyBytes = HexUtil.decodeHex(keyHex);
        byte[] iv = "sm4DefaultIv1234".getBytes(StandardCharsets.UTF_8);
        this.sm4 = new SM4(Mode.CBC, Padding.PKCS5Padding, keyBytes);
        this.sm4.setIv(iv);
        INSTANCE = this;
    }

    /** 供 IdCardTypeHandler（非 Spring 托管、仅局部生效）取用已初始化的实例 */
    public static Sm4Util getInstance() {
        return INSTANCE;
    }

    /** 明文 -> Base64 密文 */
    public String encrypt(String plainText) {
        if (plainText == null) return null;
        return sm4.encryptBase64(plainText);
    }

    /** 密文 -> 明文；若输入为旧明文（解密失败）则原样返回，兼容历史数据 */
    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) return cipherText;
        try {
            return sm4.decryptStr(cipherText);
        } catch (Exception e) {
            return cipherText;
        }
    }

    /** 判断字符串是否已是密文（用于历史数据迁移） */
    public boolean isEncrypted(String s) {
        if (s == null) return false;
        try {
            String d = sm4.decryptStr(s);
            return d != null && !d.equals(s);
        } catch (Exception e) {
            return false;
        }
    }
}
