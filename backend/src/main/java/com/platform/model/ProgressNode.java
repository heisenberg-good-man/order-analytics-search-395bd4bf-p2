package com.platform.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ProgressNode {
    private String id;
    private String progressId;
    private String nodeName;
    private String status;
    private String description;
    private LocalDateTime estimatedTime;
    private LocalDateTime actualTime;
    private String operator;
    private String remark;
    private int sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
