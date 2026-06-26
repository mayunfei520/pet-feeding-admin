package com.petfeeding.platform.module.pet.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物控制器
 */
@RestController
@RequestMapping("/api/pet")
@RequiredArgsConstructor
@Tag(name = "宠物管理", description = "宠物的增删改查")
public class PetController {

    private final PetService petService;

    @GetMapping("/all")
    @Operation(summary = "获取所有宠物列表")
    public R<List<Pet>> listAll() {
        return R.ok(petService.list());
    }

    @PostMapping
    @Operation(summary = "添加宠物")
    public R<Pet> add(@RequestBody Pet pet) {
        petService.save(pet);
        return R.ok(pet);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新宠物信息")
    public R<Pet> update(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        petService.updateById(pet);
        return R.ok(pet);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除宠物")
    public R<?> delete(@PathVariable Long id) {
        petService.removeById(id);
        return R.ok("删除成功");
    }
}
