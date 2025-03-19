package com.xxl.job.executor.utils;

import cn.hutool.core.net.NetUtil;
import cn.hutool.core.util.IdUtil;

public class SnowflakeUtil {

    public static Long getSnowflake() {
        String ip = NetUtil.getLocalhostStr();
        int workId = ip.hashCode() & 0x1F;

        String hostname = NetUtil.getLocalHostName();
        int datacenterId = hostname.hashCode() & 0x1F;

        return IdUtil.getSnowflake(workId, datacenterId).nextId();
    }
}