package com.platform.dto;

import lombok.Data;

@Data
public class ProgressNodeAddDTO {
    private String nodeName;
    private String nodeType;
    private String description;
    private String estimatedTime;
    private String operator;
    private String remark;
}
