package com.petfeeding.platform.module.miniapp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
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
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "我的宠物列表")
    public R<List<Pet>> list(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            // 未登录或演示模式，返回所有宠物
            return R.ok(petService.list());
        }
        LambdaQueryWrapper<Pet> query = new LambdaQueryWrapper<>();
        query.eq(Pet::getUserId, userId);
        return R.ok(petService.list(query));
    }

    @PostMapping
    @Operation(summary = "添加宠物")
    public R<Pet> add(@RequestBody Pet pet, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        if (userId == null) {
            // 演示模式：使用默认用户ID
            userId = 1L;
        }
        pet.setUserId(userId);
        petService.save(pet);
        return R.ok(pet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物")
    public R<Pet> update(@PathVariable Long id, @RequestBody Pet pet, @RequestHeader(value = "Authorization", required = false) String authHeader) {
        Long userId = getUserIdOrNull(authHeader);
        Pet existing = petService.getById(id);
        if (existing == null) {
            return R.fail(404, "宠物不存在");
        }
        if (userId != null && !existing.getUserId().equals(userId)) {
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
        Long userId = getUserIdOrNull(authHeader);
        Pet existing = petService.getById(id);
        if (existing == null) {
            return R.fail(404, "宠物不存在");
        }
        if (userId != null && !existing.getUserId().equals(userId)) {
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
}
