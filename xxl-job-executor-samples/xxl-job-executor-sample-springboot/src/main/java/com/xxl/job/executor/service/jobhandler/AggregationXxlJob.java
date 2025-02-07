package com.xxl.job.executor.service.jobhandler;

import com.jdragon.aggregation.commons.util.Configuration;
import com.jdragon.aggregation.core.job.JobContainer;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class AggregationXxlJob {
    @XxlJob("aggregation")
    public void aggregation() {
        Configuration configuration = Configuration.from(new File("C:\\dev\\ideaProject\\DataAggregation\\core\\src\\main\\resources\\job.json"));
        XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
        long jobId = xxlJobContext.getJobId();
        configuration.set("jobId", jobId);
        JobContainer container = new JobContainer();
        container.start(configuration);
    }
}
