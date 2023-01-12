package com.linkall.cdk.database.debezium;

public enum Operation {
    CREATE("c"),
    UPDATE("u"),
    DELETE("d");

    private final String code;

    Operation(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
