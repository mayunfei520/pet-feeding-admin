package com.petfeeding.platform.module.user;

import com.petfeeding.platform.common.exception.BusinessException;
import com.petfeeding.platform.common.util.PasswordPolicyUtil;
import com.petfeeding.platform.module.order.mapper.OrderMapper;
import com.petfeeding.platform.module.pet.mapper.PetMapper;
import com.petfeeding.platform.module.user.dto.LoginDTO;
import com.petfeeding.platform.module.user.dto.LoginResultDTO;
import com.petfeeding.platform.module.user.dto.RegisterDTO;
import com.petfeeding.platform.module.user.dto.UpdateProfileDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.mapper.UserMapper;
import com.petfeeding.platform.module.user.service.impl.UserServiceImpl;
import com.petfeeding.platform.security.util.JwtUtil;
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
class UserServiceImplTest {

    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtUtil jwtUtil;
    @Mock private PetMapper petMapper;
    @Mock private OrderMapper orderMapper;
    @Mock private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encoded_pwd");
        testUser.setRole("ADMIN");
        testUser.setStatus("ACTIVE");
        testUser.setRealName("Test User");
    }

    @Test
    void register_newUser_success() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("newuser");
        dto.setPassword("Abc123456");
        dto.setRole("OWNER");

        try (MockedStatic<PasswordPolicyUtil> mocked = mockStatic(PasswordPolicyUtil.class)) {
            mocked.when(() -> PasswordPolicyUtil.validate(anyString())).then(invocation -> null);
            when(userMapper.exists(any())).thenReturn(false);
            when(passwordEncoder.encode("Abc123456")).thenReturn("encoded");
            when(userMapper.insert(any())).thenReturn(1);

            User result = userService.register(dto);

            assertNotNull(result);
            assertEquals("newuser", result.getUsername());
            assertEquals("encoded", result.getPassword());
            assertEquals("OWNER", result.getRole());
            assertEquals("ACTIVE", result.getStatus());
        }
    }

    @Test
    void register_duplicateUsername_throws() {
        RegisterDTO dto = new RegisterDTO();
        dto.setUsername("existing");
        dto.setPassword("Abc123456");

        try (MockedStatic<PasswordPolicyUtil> mocked = mockStatic(PasswordPolicyUtil.class)) {
            mocked.when(() -> PasswordPolicyUtil.validate(anyString())).then(invocation -> null);
            when(userMapper.exists(any())).thenReturn(true);

            assertThrows(BusinessException.class, () -> userService.register(dto));
            verify(userMapper, never()).insert(any());
        }
    }

    @Test
    void login_validCredentials_success() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Abc123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("Abc123456", "encoded_pwd")).thenReturn(true);
        when(jwtUtil.generateToken(1L, "testuser", "ADMIN")).thenReturn("mock-token");

        LoginResultDTO result = userService.login(dto);

        assertNotNull(result);
        assertEquals("mock-token", result.getToken());
        assertEquals(1L, result.getUserId());
        assertEquals("testuser", result.getUsername());
        assertEquals("ADMIN", result.getRole());
    }

    @Test
    void login_wrongPassword_throws() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("wrong");

        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("wrong", "encoded_pwd")).thenReturn(false);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    void login_disabledUser_throws() {
        testUser.setStatus("DISABLED");
        LoginDTO dto = new LoginDTO();
        dto.setUsername("testuser");
        dto.setPassword("Abc123456");

        when(userMapper.selectOne(any())).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> userService.login(dto));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_userNotFound_throws() {
        LoginDTO dto = new LoginDTO();
        dto.setUsername("nonexistent");
        dto.setPassword("Abc123456");

        when(userMapper.selectOne(any())).thenReturn(null);

        assertThrows(BusinessException.class, () -> userService.login(dto));
    }

    @Test
    void resetPassword_adminUser_success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.encode(anyString())).thenReturn("new_encoded");
        when(userMapper.updateById(any())).thenReturn(1);

        String newPassword = userService.resetPassword(1L);

        assertNotNull(newPassword);
        assertEquals(8, newPassword.length());
        verify(userMapper).updateById(any());
    }

    @Test
    void resetPassword_nonAdminUser_throws() {
        testUser.setRole("OWNER");
        when(userMapper.selectById(1L)).thenReturn(testUser);

        assertThrows(BusinessException.class, () -> userService.resetPassword(1L));
    }

    @Test
    void updateProfile_validFields_success() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(userMapper.updateById(any())).thenReturn(1);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setPhone("13900000000");
        dto.setEmail("new@email.com");
        dto.setGender("男");

        userService.updateProfile(1L, dto);

        assertEquals("13900000000", testUser.getPhone());
        assertEquals("new@email.com", testUser.getEmail());
        assertEquals("男", testUser.getGender());
    }

    @Test
    void updateProfile_userNotFound_throws() {
        when(userMapper.selectById(999L)).thenReturn(null);

        UpdateProfileDTO dto = new UpdateProfileDTO();
        dto.setPhone("13900000000");

        assertThrows(BusinessException.class, () -> userService.updateProfile(999L, dto));
    }
}