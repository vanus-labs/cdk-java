package com.linkall.cdk.connector;

/**
 * sink process event result
 */
public class Result {
    public static final Result SUCCESS = new Result(0, "success");
    private int code;
    private String msg;

    public Result(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

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

    @Override
    public String toString() {
        return "{\"message\":\"" + this.msg + "\",\"code\":" + this.code + "}";
    }
}
