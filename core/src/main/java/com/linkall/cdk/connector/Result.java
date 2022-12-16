package com.linkall.cdk.connector;

/**
 * sink process event result
 */
public class Result {
    private int code;

    private String msg;

    public static final Result SUCCESS = new Result(0, "success");

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
