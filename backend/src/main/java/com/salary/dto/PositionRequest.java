package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PositionRequest {

    @NotBlank(message = "职位ID不能为空")
    private String posId;

    @NotBlank(message = "职位名称不能为空")
    private String posName;

    @NotNull(message = "基本工资不能为空")
    private BigDecimal baseSalary;

    @NotBlank(message = "部门代码不能为空")
    private String deptCode;
}
