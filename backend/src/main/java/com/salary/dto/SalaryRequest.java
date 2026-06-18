package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalaryRequest {

    @NotNull(message = "员工ID不能为空")
    private Integer empId;

    @NotBlank(message = "计薪周期不能为空")
    private String payPeriod;

    private BigDecimal baseSnap;

    private BigDecimal grossTotal;

    private BigDecimal deductTotal;

    private BigDecimal netPay;

    private String status;
}
