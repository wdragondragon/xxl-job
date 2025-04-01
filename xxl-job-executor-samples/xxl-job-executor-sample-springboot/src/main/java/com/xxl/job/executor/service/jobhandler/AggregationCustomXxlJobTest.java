package com.xxl.job.executor.service.jobhandler;

import com.jdragon.aggregation.commons.element.Column;
import com.jdragon.aggregation.commons.element.Record;
import com.jdragon.aggregation.commons.element.StringColumn;
import com.jdragon.aggregation.commons.util.Configuration;
import com.jdragon.aggregation.core.job.JobContainer;
import com.jdragon.aggregation.core.plugin.PluginType;
import com.jdragon.aggregation.core.plugin.RecordReceiver;
import com.jdragon.aggregation.core.plugin.RecordSender;
import com.jdragon.aggregation.core.plugin.spi.Reader;
import com.jdragon.aggregation.core.plugin.spi.Writer;
import com.jdragon.aggregation.core.transport.record.DefaultRecord;
import com.xxl.job.core.context.XxlJobContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import com.xxl.job.core.handler.annotation.XxlJob;
import org.springframework.stereotype.Component;

@Component
public class AggregationCustomXxlJobTest {

    private final Logger log = LoggerFactory.getLogger(AggregationCustomXxlJobTest.class);

    public static void main(String[] args) {
        AggregationCustomXxlJobTest aggregationCustomXxlJobTest = new AggregationCustomXxlJobTest();
        aggregationCustomXxlJobTest.aggregationCustomTest();
    }

    @XxlJob("aggregationCustomTest")
    public void aggregationCustomTest() {
        Configuration configuration = Configuration.newDefault();
        XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
        long jobId = 1;
        if (xxlJobContext != null) {
            jobId = xxlJobContext.getJobId();
        }

        Configuration custom = Configuration.from(new HashMap<String, Object>() {{
            put("type", "custom");
            put("config", new HashMap<>());
        }});

        configuration.set("jobId", jobId);
        configuration.set("reader", custom.clone());
        configuration.set("writer", custom.clone());

        JobContainer container = new JobContainer(configuration);
        container.addConsumerPlugin(PluginType.READER, new Reader.Job() {

            @Override
            public void init() {
                log.info("reader init");
            }

            @Override
            public void startRead(RecordSender recordSender) {
                log.info("reader startRead");
                Record record = new DefaultRecord();
                record.setColumn(0, new StringColumn("123"));
                recordSender.sendToWriter(record);
            }

            @Override
            public void post() {
                log.info("reader post");
            }
        });

        container.addConsumerPlugin(PluginType.WRITER, new Writer.Job() {
            private final Logger log = LoggerFactory.getLogger(AggregationCustomXxlJobTest.class);

            @Override
            public void init() {
                log.info("writer init");
            }

            @Override
            public void startWrite(RecordReceiver recordReceiver) {
                log.info("writer startWrite");
                Record record = null;
                while ((record = recordReceiver.getFromReader()) != null) {
                    Column column = record.getColumn(0);
                    log.info("get from reader:{}", column.asString());
                }
            }

            @Override
            public void post() {
                log.info("writer post");
            }
        });
        container.start();
    }
}
