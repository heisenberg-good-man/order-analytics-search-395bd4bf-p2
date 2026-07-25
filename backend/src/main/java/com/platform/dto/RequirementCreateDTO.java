package com.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class RequirementCreateDTO {
    private String title;
    private String description;
    private String professionType;
    private String city;
    private List<String> requiredSkills;
    private String contactName;
    private String contactPhone;
}
