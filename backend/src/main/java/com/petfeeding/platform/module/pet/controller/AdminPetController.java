package com.petfeeding.platform.module.pet.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 管理后台 — 宠物审核（仅 ADMIN）
 */
@RestController
@RequestMapping("/api/admin/pets")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "管理后台-宠物审核", description = "宠物审核队列与通过/驳回（仅 ADMIN）")
public class AdminPetController {

    private final PetService petService;

    @GetMapping
    @Operation(summary = "宠物审核队列")
    public R<List<Pet>> reviewQueue(@RequestParam(value = "status", required = false) String status) {
        LambdaQueryWrapper<Pet> query = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) {
            query.eq(Pet::getStatus, status);
        }
        query.orderByDesc(Pet::getCreatedAt);
        return R.ok(petService.list(query));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "审核通过")
    public R<Pet> approve(@PathVariable Long id) {
        Pet pet = petService.getById(id);
        if (pet == null) {
            return R.fail(404, "宠物不存在");
        }
        if (!Pet.STATUS_PENDING.equals(pet.getStatus())) {
            return R.fail(400, "该宠物已审核，无需重复操作");
        }
        pet.setStatus(Pet.STATUS_APPROVED);
        pet.setRejectReason(null);
        petService.updateById(pet);
        return R.ok(pet);
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "审核驳回")
    public R<Pet> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Pet pet = petService.getById(id);
        if (pet == null) {
            return R.fail(404, "宠物不存在");
        }
        if (!Pet.STATUS_PENDING.equals(pet.getStatus())) {
            return R.fail(400, "该宠物已审核，无需重复操作");
        }
        String reason = body == null ? null : body.get("reason");
        if (reason == null || reason.trim().isEmpty()) {
            return R.fail(400, "请填写驳回原因");
        }
        if (reason.length() > 200) {
            return R.fail(400, "驳回原因不能超过200个字");
        }
        pet.setStatus(Pet.STATUS_REJECTED);
        pet.setRejectReason(reason);
        petService.updateById(pet);
        return R.ok(pet);
    }
}