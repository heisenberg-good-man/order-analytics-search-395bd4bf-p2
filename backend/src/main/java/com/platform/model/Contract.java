package com.platform.model;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class Contract {
    private String id;
    private String requirementId;
    private String requirementTitle;
    private String providerId;
    private String providerName;
    private String professionType;
    private String city;
    private List<String> skillTags;
    private BigDecimal budget;
    private String serviceStartTime;
    private String serviceEndTime;
    private String remark;
    private String providerVerifyStatus;
    private String providerVerifySummary;
    private String status;
    private Boolean providerConfirmed;
    private Boolean demanderConfirmed;
    private String rejectReason;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
