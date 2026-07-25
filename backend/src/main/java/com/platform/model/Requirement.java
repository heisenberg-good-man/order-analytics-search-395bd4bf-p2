package com.platform.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class Requirement {
    private String id;
    private String title;
    private String description;
    private String professionType;
    private String city;
    private List<String> requiredSkills;
    private String contactName;
    private String contactPhone;
    private String status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private String selectedProviderId;
}
