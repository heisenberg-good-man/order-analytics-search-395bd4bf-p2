package com.platform.enums;

public enum ProgressStatus {
    PENDING_VISIT("待上门"),
    IN_SERVICE("服务中"),
    PENDING_ACCEPTANCE("待验收"),
    COMPLETED("已完成"),
    ABNORMAL_PAUSED("异常暂停");

    private final String desc;

    ProgressStatus(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
