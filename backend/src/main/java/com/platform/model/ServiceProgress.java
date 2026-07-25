package com.platform.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceProgress {
    private String id;
    private String contractId;
    private String requirementId;
    private String requirementTitle;
    private String providerId;
    private String providerName;
    private String professionType;
    private String city;
    private BigDecimal budget;
    private String currentNode;
    private String currentStatus;
    private List<ProgressNode> nodes;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
