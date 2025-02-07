package com.xxl.job.executor.core.config;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.context.XxlJobHelper;

public class XxlJobLogFilter extends Filter<ILoggingEvent> {

    @Override
    public FilterReply decide(ILoggingEvent event) {
        // 判断是否是处于xxl-job上下文中（通过xxl-job调用发起的）
        if (XxlJobContext.getXxlJobContext() != null) {
            // 调用xxl-job记录日志的方法 不同版本的xxl-job记录日志的api不一样
            StackTraceElement callerDatum = event.getCallerData()[0];
            XxlJobHelper.log(callerDatum, event.getFormattedMessage());
            // 获取ThrowableProxy对象
            ThrowableProxy throwableProxy = (ThrowableProxy) event.getThrowableProxy();
            // 判断是否有异常
            if (throwableProxy != null) {
                // 获取异常信息
                String stackTrace = ThrowableProxyUtil.asString(throwableProxy);
                // 打印异常信息
                XxlJobHelper.log("异常信息: {}", stackTrace);
            }
        }
        // 放行
        return FilterReply.NEUTRAL;
    }
}