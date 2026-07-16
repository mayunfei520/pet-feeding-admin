package com.petfeeding.platform.common.util;

import com.petfeeding.platform.common.exception.BusinessException;
import java.util.regex.Pattern;

/**
 * 密码复杂度校验工具（后端兜底）。
 * 前端已有体验层校验，但接口可被直接调用绕过，故后端必须强制校验。
 * 规则：长度 8-20 位，且必须同时包含大写字母、小写字母、数字。
 */
public final class PasswordPolicyUtil {

    private static final Pattern PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,20}$");

    public static final String MESSAGE = "密码需为8-20位，且同时包含大小写字母与数字";

    private PasswordPolicyUtil() {
    }

    /**
     * 校验密码复杂度，不达标直接抛出 BusinessException。
     */
    public static void validate(String password) {
        if (password == null || !PATTERN.matcher(password).matches()) {
            throw new BusinessException(MESSAGE);
        }
    }
}
