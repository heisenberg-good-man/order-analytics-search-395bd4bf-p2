package com.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class ProviderCreateDTO {
    private String name;
    private String phone;
    private String professionType;
    private String city;
    private List<String> skillTags;
}
