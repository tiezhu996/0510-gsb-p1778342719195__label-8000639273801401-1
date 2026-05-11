package com.cardmanager.dto;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class GenerateCardRequest {
    @NotNull(message = "发卡数量不能为空")
    @Min(value = 1, message = "发卡数量至少为1")
    private Integer count;

    private String operator;
}
