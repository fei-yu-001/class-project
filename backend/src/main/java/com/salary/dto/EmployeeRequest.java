package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeRequest {

    @NotBlank(message = "姓名不能为空")
    @Size(max = 30)
    private String empName;

    private String gender;

    private String birthDate;

    @NotBlank(message = "入职日期不能为空")
    private String hireDate;

    @NotBlank(message = "职位ID不能为空")
    private String posId;

    @NotBlank(message = "部门代码不能为空")
    private String deptCode;

    private String empStatus;
}
