package com.example.demo.constant;

public enum ScoreTypeEnum {

    ADD("add"),
    SUBTRACT("subtract");

    private final String value;

    ScoreTypeEnum(String value) {
        this.value = value;
    }

    public String getValue(){

        return value;
    }
}
