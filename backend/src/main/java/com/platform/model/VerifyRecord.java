package com.platform.model;

import com.platform.enums.VerifyStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerifyRecord {
    private String id;
    private String providerId;
    private String operator;
    private VerifyStatus action;
    private String remark;
    private LocalDateTime createTime;
}
