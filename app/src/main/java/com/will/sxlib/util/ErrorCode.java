package com.will.sxlib.util;

/**
 * Created by Will on 2016/5/24.
 */
public enum ErrorCode {
    /**
     * 连接失败
     */
    CONNECTION_FAILED,
    /**
     * 密码错误
     */
    PASSWORD_INVALID,
    /**
     * 续借失败，已达续借最大次数限制
     */
    RENEW_FAILED;
}
