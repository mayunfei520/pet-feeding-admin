package com.petfeeding.platform.module.user;

import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.common.util.PasswordPolicyUtil;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.module.user.service.MiniAppUserService;
import com.petfeeding.platform.security.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MiniAppUserServiceTest {

    @Mock private UserMapper userMapper;
    @Mock private JwtUtil jwtUtil;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ObjectMapper objectMapper;

    private MiniAppUserService miniAppUserService;

    @BeforeEach
    void setUp() {
        miniAppUserService = new MiniAppUserService(userMapper, jwtUtil, passwordEncoder, objectMapper);
    }

    @Test
    void register_validInput_success() {
        try (MockedStatic<PasswordPolicyUtil> mocked = mockStatic(PasswordPolicyUtil.class)) {
            mocked.when(() -> PasswordPolicyUtil.validate(anyString())).then(invocation -> null);
            when(userMapper.selectCount(any())).thenReturn(0L);
            when(userMapper.insert(any())).thenReturn(1);
            when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("token");

            var result = miniAppUserService.register("13800000001", "Abc123456", "nickname", "1234", "男", "张三");

            assertNotNull(result);
            assertEquals("token", result.getToken());
        }
    }

    @Test
    void register_emptyPhone_throws() {
        assertThrows(BusinessException.class,
                () -> miniAppUserService.register("", "Abc123456", "nick", "1234", null, "张三"));
    }

    @Test
    void register_wrongCode_throws() {
        try (MockedStatic<PasswordPolicyUtil> mocked = mockStatic(PasswordPolicyUtil.class)) {
            mocked.when(() -> PasswordPolicyUtil.validate(anyString())).then(invocation -> null);
            when(userMapper.selectCount(any())).thenReturn(0L);

            assertThrows(BusinessException.class,
                    () -> miniAppUserService.register("13800000001", "Abc123456", "nick", "9999", "男", "张三"));
        }
    }

    @Test
    void register_invalidRealName_throws() {
        try (MockedStatic<PasswordPolicyUtil> mocked = mockStatic(PasswordPolicyUtil.class)) {
            mocked.when(() -> PasswordPolicyUtil.validate(anyString())).then(invocation -> null);
            when(userMapper.selectCount(any())).thenReturn(0L);

            assertThrows(BusinessException.class,
                    () -> miniAppUserService.register("13800000001", "Abc123456", "nick", "1234", "男", "A"));
            assertThrows(BusinessException.class,
                    () -> miniAppUserService.register("13800000001", "Abc123456", "nick", "1234", "男", ""));
            assertThrows(BusinessException.class,
                    () -> miniAppUserService.register("13800000001", "Abc123456", "nick", "1234", "男", null));
        }
    }

    @Test
    void register_duplicatePhone_throws() {
        try (MockedStatic<PasswordPolicyUtil> mocked = mockStatic(PasswordPolicyUtil.class)) {
            mocked.when(() -> PasswordPolicyUtil.validate(anyString())).then(invocation -> null);
            when(userMapper.selectCount(any())).thenReturn(1L);

            assertThrows(BusinessException.class,
                    () -> miniAppUserService.register("13800000001", "Abc123456", "nick", "1234", "男", "张三"));
        }
    }

    @Test
    void loginByWechat_newUser_createsAndReturns() {
        when(userMapper.selectOne(any())).thenReturn(null);
        when(userMapper.insert(any())).thenReturn(1);
        when(jwtUtil.generateToken(anyLong(), anyString(), anyString())).thenReturn("wx-token");

        var result = miniAppUserService.loginByWechat("wx-code");

        assertNotNull(result);
        assertEquals("wx-token", result.getToken());
        verify(userMapper).insert(any());
    }

    @Test
    void loginByWechat_existingUser_returnsWithoutCreate() {
        User existingUser = new User();
        existingUser.setId(10L);
        existingUser.setUsername("wx_user");
        existingUser.setRole("OWNER");

        when(userMapper.selectOne(any())).thenReturn(existingUser);
        when(jwtUtil.generateToken(10L, "wx_user", "OWNER")).thenReturn("existing-token");

        var result = miniAppUserService.loginByWechat("wx-code");

        assertNotNull(result);
        assertEquals(10L, result.getUserId());
        verify(userMapper, never()).insert(any());
    }

    @Test
    void sendCode_returnsSuccess() {
        // The sendCode method should always succeed in mock mode
        assertDoesNotThrow(() -> miniAppUserService.sendCode("13800000001"));
    }
}