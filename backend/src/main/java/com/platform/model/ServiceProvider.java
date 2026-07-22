package com.platform.model;

import com.platform.enums.VerifyStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ServiceProvider {
    private String id;
    private String name;
    private String phone;
    private String professionType;
    private String city;
    private List<String> skillTags;
    private VerifyStatus verifyStatus;
    private Integer completeness;
    private LocalDateTime updateTime;
    private LocalDateTime createTime;

    private String idCardNo;
    private String idCardName;
    private String idCardAddress;
    private List<String> submittedMaterials;
    private String rejectReason;
}
