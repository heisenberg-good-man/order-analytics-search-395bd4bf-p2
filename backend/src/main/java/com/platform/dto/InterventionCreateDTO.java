package com.platform.dto;

import lombok.Data;
import java.util.List;

@Data
public class InterventionCreateDTO {
    private String requirementId;
    private String progressId;
    private String initiatorRole;
    private String initiatorName;
    private String issueType;
    private String complaintContent;
    private String demandDescription;
    private List<String> materialUrls;
}
