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

    /** 种类：CAT-猫, DOG-狗（仅支持猫和狗） */
    @NotBlank(message = "请选择宠物种类")
    @Pattern(regexp = "CAT|DOG", message = "宠物种类不合法，仅支持猫或狗")
    private String species;

    /** 具体品种（如：英短、金毛） */
    @Size(max = 20, message = "品种名称不能超过20个字")
    private String breed;

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

    /** 审核状态：PENDING-待审核, APPROVED-已通过, REJECTED-已驳回 */
    private String status;

    /** 驳回原因（驳回时填写，≤200字） */
    @Size(max = 200, message = "驳回原因不能超过200个字")
    private String rejectReason;

    /** 审核状态常量 */
    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_APPROVED = "APPROVED";
    public static final String STATUS_REJECTED = "REJECTED";

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
