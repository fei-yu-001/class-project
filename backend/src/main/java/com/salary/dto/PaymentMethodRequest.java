package com.salary.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentMethodRequest {

    @NotNull(message = "员工ID不能为空")
    private Integer empId;

    @NotBlank(message = "支付类型不能为空")
    private String payType;

    @NotBlank(message = "账号不能为空")
    private String accountNo;

    private String cardType;
}
