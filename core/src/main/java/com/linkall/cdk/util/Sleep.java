package com.linkall.cdk.util;

public class Sleep {
    public static long Backoff(int attempt, long max) {
        if (attempt==0) {
            return 0;
        }
        double backoff = 500 * Math.pow(2, attempt);
        if (backoff > max) {
            backoff = max;
        }
        return (long) backoff;
    }
}
