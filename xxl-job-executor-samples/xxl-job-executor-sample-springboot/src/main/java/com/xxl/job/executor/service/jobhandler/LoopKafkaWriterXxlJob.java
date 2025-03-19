package com.xxl.job.executor.service.jobhandler;

import com.jdragon.aggregation.commons.element.LongColumn;
import com.jdragon.aggregation.commons.element.Record;
import com.jdragon.aggregation.commons.element.StringColumn;
import com.jdragon.aggregation.commons.util.Configuration;
import com.jdragon.aggregation.core.job.JobContainer;
import com.jdragon.aggregation.core.plugin.PluginType;
import com.jdragon.aggregation.core.plugin.RecordSender;
import com.jdragon.aggregation.core.plugin.spi.Reader;
import com.jdragon.aggregation.core.transport.record.DefaultRecord;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class LoopKafkaWriterXxlJob {

    @XxlJob("LoopKafkaWriterXxlJob")
    public void aggregationCustom() {
        Configuration configuration = Configuration.from(new File("C:\\dev\\ideaProject\\DataAggregation\\core\\src\\main\\resources\\loopkafkawriter.json"));
        XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
        long jobId = xxlJobContext.getJobId();
        configuration.set("jobId", jobId);

        JobContainer container = new JobContainer(configuration);
        container.addConsumerPlugin(PluginType.READER, new Reader.Job() {
            private final Logger log = LoggerFactory.getLogger(LoopKafkaWriterXxlJob.class);

            private long startTime;

            private int count;

            @Override
            public void init() {
                log.info("custom init");
                startTime = System.currentTimeMillis();
                count = 0;
            }

            @Override
            public void startRead(RecordSender recordSender) {
                while (System.currentTimeMillis() - startTime < 60000) {
                    Record record = new DefaultRecord();
                    record.setColumn(0, new LongColumn(count));
                    record.setColumn(1, new StringColumn(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date())));
                    recordSender.sendToWriter(record);
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    count++;
                }
            }

            @Override
            public void post() {
                log.info("custom post");
            }

        });
        container.start();
    }
}
