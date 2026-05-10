package com.cardmanager.dto;

import lombok.Data;

@Data
public class CardQueryRequest {
    private String cardNumber;
    private String batchNumber;
    private Integer status;
    private String useTimeStart;
    private String useTimeEnd;
    private Integer page = 1;
    private Integer size = 10;
    private String sortBy; // e.g., "useTime" or "createTime"
}
