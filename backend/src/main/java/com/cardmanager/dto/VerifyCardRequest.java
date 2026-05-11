package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class VerifyCardRequest {
    @NotBlank(message = "卡号不能为空")
    private String cardNumber;

    @NotBlank(message = "密码不能为空")
    private String cardPassword;

    private String operator;
}
