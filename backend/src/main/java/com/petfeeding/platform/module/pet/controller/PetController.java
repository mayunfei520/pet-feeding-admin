package com.petfeeding.platform.module.pet.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 宠物控制器
 */
@RestController
@RequestMapping("/api/pet")
@lombok.RequiredArgsConstructor
public class PetController {

    private final PetService petService;

    @GetMapping("/all")
    public R<List<Pet>> listAll() {
        return R.ok(petService.list());
    }

    @PostMapping
    public R<Pet> add(@RequestBody Pet pet) {
        petService.save(pet);
        return R.ok(pet);
    }

    @PutMapping("/{id}")
    public R<Pet> update(@PathVariable Long id, @RequestBody Pet pet) {
        pet.setId(id);
        petService.updateById(pet);
        return R.ok(pet);
    }

    @DeleteMapping("/{id}")
    public R<?> delete(@PathVariable Long id) {
        petService.removeById(id);
        return R.ok("删除成功");
    }
}
