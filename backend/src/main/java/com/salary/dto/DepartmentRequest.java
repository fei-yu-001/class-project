package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "部门代码不能为空")
    private String deptCode;

    @NotBlank(message = "部门名称不能为空")
    private String deptName;

    private String parentDeptCode;

    private Integer deptManagerEmp;
}
