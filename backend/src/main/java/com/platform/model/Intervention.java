package com.platform.model;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
public class Intervention {
    private String id;
    private String requirementId;
    private String requirementTitle;
    private String contractId;
    private String providerId;
    private String providerName;
    private String progressId;
    private String currentNode;
    private String initiatorRole;
    private String initiatorName;
    private String issueType;
    private String complaintContent;
    private String demandDescription;
    private List<String> materialUrls;
    private String status;
    private String professionType;
    private String city;
    private BigDecimal contractBudget;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String handler;
    private String processingResult;
    private String supplementNote;
    private List<Map<String, Object>> processingRecords;
}
