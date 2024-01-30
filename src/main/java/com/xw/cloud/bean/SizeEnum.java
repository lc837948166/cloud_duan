package com.xw.cloud.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 大小枚举
 *
 */
@AllArgsConstructor
public enum SizeEnum {

    /**
     * 1KB = 1024B
     */
    KB(1024),

    /**
     * 1MB = 1024KB
     */
    MB(KB.size * 1024),

    /**
     * 1GB = 1024MB
     */
    GB(MB.size * 1024);

    /**
     * 1(K/M/G)B = ? B
     */
    @Getter
    private final long size;
}
