package com.petfeeding.platform.module.pet.controller;

import com.petfeeding.platform.common.result.R;
import com.petfeeding.platform.module.pet.entity.Pet;
import com.petfeeding.platform.module.pet.service.PetService;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 宠物控制器
 */
@RestController
@RequestMapping("/api/pet")
@lombok.RequiredArgsConstructor
public class PetController {

    private final PetService petService;
    private final ApplicationContext applicationContext;

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

    /** 自省诊断：排查 AdminPetController 为何未注册 (待定位后可删除) */
    @GetMapping("/_ctx")
    public R<Map<String, Object>> ctx() {
        Map<String, Object> m = new HashMap<>();
        m.put("containsAdminPetBean", applicationContext.containsBean("adminPetController"));
        try {
            Object bean = applicationContext.getBean("adminPetController");
            m.put("adminPetBeanClass", bean.getClass().getName());
            m.put("adminPetIsCglibProxy", bean.getClass().getName().contains("$$"));
            m.put("adminPetHasControllerAnno",
                    bean.getClass().isAnnotationPresent(org.springframework.stereotype.Controller.class)
                            || bean.getClass().isAnnotationPresent(RestController.class));
        } catch (Exception e) {
            m.put("adminPetBeanError", e.getClass().getSimpleName() + ": " + e.getMessage());
        }
        RequestMappingHandlerMapping rm = applicationContext.getBean(RequestMappingHandlerMapping.class);
        m.put("totalHandlerMethods", rm.getHandlerMethods().size());
        boolean hasAdmin = rm.getHandlerMethods().keySet().stream()
                .anyMatch(k -> k.toString().contains("admin/pets"));
        m.put("hasAdminPetsMapping", hasAdmin);
        return R.ok(m);
    }
}
