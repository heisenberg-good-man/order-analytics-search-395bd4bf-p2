package com.platform.dto;

import lombok.Data;

import java.util.List;

@Data
public class VerifySubmitDTO {
    private String idCardName;
    private String idCardNo;
    private String idCardAddress;
    private List<String> submittedMaterials;
}
