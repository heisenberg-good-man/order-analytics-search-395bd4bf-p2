package com.platform.enums;

public enum VerifyStatus {
    NOT_SUBMITTED("未提交"),
    PENDING("待审核"),
    APPROVED("已认证"),
    REJECTED("已驳回");

    private final String desc;

    VerifyStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
