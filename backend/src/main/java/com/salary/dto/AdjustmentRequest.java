package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjustmentRequest {

    @NotNull(message = "员工ID不能为空")
    private Integer empId;

    @NotBlank(message = "计薪周期不能为空")
    private String payPeriod;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotNull(message = "金额不能为空")
    @Positive(message = "金额必须大于0")
    private BigDecimal amount;

    private String remark;
}
