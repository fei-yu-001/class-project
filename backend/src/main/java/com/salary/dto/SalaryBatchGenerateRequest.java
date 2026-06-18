package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class SalaryBatchGenerateRequest {

    @NotBlank(message = "计薪周期不能为空")
    private String payPeriod;

    private List<Integer> empIds;

    private boolean overwriteUnpaid;
}
