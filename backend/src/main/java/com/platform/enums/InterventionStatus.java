package com.platform.enums;

public enum InterventionStatus {
    PENDING("待受理"),
    PROCESSING("处理中"),
    SUPPLEMENT("待补充"),
    RESOLVED("已处理"),
    CLOSED("已关闭");

    private final String desc;

    InterventionStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
