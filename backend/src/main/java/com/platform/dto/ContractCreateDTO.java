package com.platform.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ContractCreateDTO {
    private String requirementId;
    private String providerId;
    private BigDecimal budget;
    private String serviceStartTime;
    private String serviceEndTime;
    private String remark;
}
