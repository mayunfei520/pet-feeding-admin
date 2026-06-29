package com.petfeeding.platform.module.miniapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import com.petfeeding.platform.module.user.entity.User;
import com.petfeeding.platform.module.user.service.UserService;
import com.petfeeding.platform.security.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序 — 宠物管理
 */
@RestController
@RequestMapping("/api/miniapp/pets")
@RequiredArgsConstructor
@Tag(name = "小程序-宠物", description = "用户宠物 CRUD")
public class MiniAppPetController {

    private final PetService petService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "我的宠物列表")
    public R<List<Pet>> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if ("ADMIN".equals(user.getRole())) {
            return R.fail(403, "管理员请在后台查看宠物数据");
        }
        LambdaQueryWrapper<Pet> query = new LambdaQueryWrapper<>();
        query.eq(Pet::getUserId, user.getId());
        return R.ok(petService.list(query));
    }

    @PostMapping
    @Operation(summary = "添加宠物")
    public R<Pet> add(@RequestBody Pet pet, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人才能添加宠物");
        }
        pet.setUserId(user.getId());
        petService.save(pet);
        return R.ok(pet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物")
    public R<Pet> update(@PathVariable Long id, @RequestBody Pet pet, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人才能更新宠物");
        }
        Pet existing = petService.getById(id);
        if (existing == null) {
            return R.fail(404, "宠物不存在");
        }
        if (!existing.getUserId().equals(user.getId())) {
            return R.fail(403, "无权操作");
        }
        pet.setId(id);
        pet.setUserId(existing.getUserId());
        petService.updateById(pet);
        return R.ok(pet);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物")
    public R<?> delete(@PathVariable Long id, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        User user = getCurrentUser(authHeader);
        if (user == null) {
            return R.fail(401, "请先登录");
        }
        if (!"OWNER".equals(user.getRole())) {
            return R.fail(403, "只有宠物主人才能删除宠物");
        }
        Pet existing = petService.getById(id);
        if (existing == null) {
            return R.fail(404, "宠物不存在");
        }
        if (!existing.getUserId().equals(user.getId())) {
            return R.fail(403, "无权操作");
        }
        petService.removeById(id);
        return R.ok("删除成功");
    }

    private Long getUserIdOrNull(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        try {
            String token = authHeader.replace("Bearer ", "");
            return jwtUtil.getUserIdFromToken(token);
        } catch (Exception e) {
            return null;
        }
    }

    private User getCurrentUser(String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        return userId == null ? null : userService.getById(userId);
    }
}
