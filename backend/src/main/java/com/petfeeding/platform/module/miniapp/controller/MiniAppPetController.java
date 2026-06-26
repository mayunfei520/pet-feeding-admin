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
    public R<List<Pet>> list(@RequestHeader("Authorization") String authHeader) {
        Long userId = getUserId(authHeader);
        LambdaQueryWrapper<Pet> query = new LambdaQueryWrapper<>();
        query.eq(Pet::getUserId, userId);
        return R.ok(petService.list(query));
    }

    @PostMapping
    @Operation(summary = "添加宠物")
    public R<Pet> add(@RequestBody Pet pet, @RequestHeader("Authorization") String authHeader) {
        pet.setUserId(getUserId(authHeader));
        petService.save(pet);
        return R.ok(pet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物")
    public R<Pet> update(@PathVariable Long id, @RequestBody Pet pet, @RequestHeader("Authorization") String authHeader) {
        Pet existing = petService.getById(id);
        if (existing == null || !existing.getUserId().equals(getUserId(authHeader))) {
            return R.fail(403, "无权操作");
        }
        pet.setId(id);
        pet.setUserId(existing.getUserId());
        petService.updateById(pet);
        return R.ok(pet);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物")
    public R<?> delete(@PathVariable Long id, @RequestHeader("Authorization") String authHeader) {
        Pet existing = petService.getById(id);
        if (existing == null || !existing.getUserId().equals(getUserId(authHeader))) {
            return R.fail(403, "无权操作");
        }
        petService.removeById(id);
        return R.ok("删除成功");
    }

    private Long getUserId(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return jwtUtil.getUserIdFromToken(token);
    }
}
