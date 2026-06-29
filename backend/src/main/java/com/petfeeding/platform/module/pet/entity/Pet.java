package com.petfeeding.platform.module.pet.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 宠物表
 */
@Data
@TableName("pets")
public class Pet {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 所属用户 ID */
    private Long userId;

    /** 宠物名称 */
    private String name;

    /** 品种：CAT-猫, DOG-狗, OTHER-其他 */
    private String species;

    /** 具体品种（如：英短、金毛） */
    private String breed;

    /** 年龄（岁） */
    private Integer age;

    /** 体重（kg） */
    private BigDecimal weight;

    /** 医疗备注 */
    private String medicalNotes;

    /** 是否已打疫苗 */
    private Boolean vaccinated;

    /** 宠物照片 URL */
    private String image;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
