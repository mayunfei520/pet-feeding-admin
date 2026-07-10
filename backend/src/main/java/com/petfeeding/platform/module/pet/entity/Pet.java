package com.petfeeding.platform.module.pet.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

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
    @NotBlank(message = "请输入宠物名字")
    @Size(max = 20, message = "名字不能超过20个字")
    private String name;

    /** 品种：CAT-猫, DOG-狗, OTHER-其他 */
    @NotBlank(message = "请选择宠物种类")
    @Pattern(regexp = "CAT|DOG|OTHER", message = "宠物种类不合法")
    private String species;

    /** 具体品种（如：英短、金毛） */
    @Size(max = 20, message = "品种名称不能超过20个字")
    private String bre;

    /** 年龄（岁） */
    @Min(value = 0, message = "年龄需在0-50岁之间")
    @Max(value = 50, message = "年龄需在0-50岁之间")
    private Integer age;

    /** 体重（kg） */
    @DecimalMin(value = "0", message = "体重需在0-200kg之间")
    @DecimalMax(value = "200", message = "体重需在0-200kg之间")
    private BigDecimal weight;

    /** 医疗备注 */
    @Size(max = 200, message = "医疗备注不能超过200个字")
    private String medicalNotes;

    /** 是否已打疫苗 */
    private Boolean vaccinated;

    /** 宠物照片 URL */
    private String image;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
