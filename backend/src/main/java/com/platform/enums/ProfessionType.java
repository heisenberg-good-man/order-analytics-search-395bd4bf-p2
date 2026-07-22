package com.platform.enums;

public enum ProfessionType {
    BABYSITTER("保姆"),
    REPAIRMAN("维修工"),
    MATERNAL_NURSE("月嫂"),
    CLEANER("保洁"),
    COOK("厨师"),
    DRIVER("司机"),
    NURSING_WORKER("护工"),
    TUTOR("家教");

    private final String desc;

    ProfessionType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
