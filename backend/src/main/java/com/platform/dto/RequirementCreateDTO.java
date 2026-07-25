package com.platform.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RequirementCreateDTO {
    private String title;
    private String description;
    private String professionType;
    private String city;
    private List<String> requiredSkills;
    private BigDecimal budget;
    private String serviceStartTime;
    private String serviceEndTime;
    private String contactName;
    private String contactPhone;
}
