package com.example.demo.constant;

import java.time.LocalDate;

public enum CacheKeyEnum {

    USER("user"),
    CHECK_IN("checkin"),
    CONFIG("config"),
    CHECK_IN_LOCK("checkin:lock");

    private final String key;

    CacheKeyEnum(String key) {
        this.key = key;
    }

    public String getValue() {
        return key;
    }

    public String genKey(long id) {
        return key + ":" + id;
    }

    public String genKeyDate(long id, LocalDate date) {
        return key + ":" + id + ":" + date.toString();
    }
}
