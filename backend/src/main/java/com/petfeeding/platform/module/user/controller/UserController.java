package com.petfeeding.platform.module.user.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.user.dto.LoginDTO;
import com.petfeeding.platform.module.user.dto.RegisterDTO;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 用户控制器
 */
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "用户注册、登录、管理接口")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public R<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return R.ok("注册成功");
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public R<?> login(@Valid @RequestBody LoginDTO dto) {
        return R.ok(userService.login(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "用户列表（支持按角色过滤）")
    public R<List<User>> list(@RequestParam(required = false) String role) {
        return R.ok(userService.listByRole(role));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "修改用户状态")
    public R<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String status = body.get("status");
        User user = userService.getById(id);
        if (user != null) {
            user.setStatus(status);
            userService.updateById(user);
        }
        return R.ok("操作成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户")
    public R<?> remove(@PathVariable Long id) {
        userService.removeById(id);
        return R.ok("删除成功");
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码")
    public R<?> resetPassword(@PathVariable Long id) {
        return R.ok(userService.resetPassword(id));
    }
}
