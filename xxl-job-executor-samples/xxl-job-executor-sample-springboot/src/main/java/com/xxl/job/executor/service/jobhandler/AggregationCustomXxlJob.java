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
import com.jdragon.aggregation.datasource.AbstractDataSourcePlugin;
import com.jdragon.aggregation.datasource.BaseDataSourceDTO;
import com.jdragon.aggregation.datasource.DataSourceType;
import com.jdragon.aggregation.datasource.SourcePluginType;
import com.jdragon.aggregation.pluginloader.PluginClassLoaderCloseable;
import com.xxl.job.core.context.XxlJobContext;
import com.xxl.job.core.handler.annotation.XxlJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Component
public class AggregationCustomXxlJob {

    @XxlJob("aggregationCustom")
    public void aggregationCustom() {
        Configuration configuration = Configuration.from(new File("C:\\dev\\ideaProject\\DataAggregation\\core\\src\\main\\resources\\custom.json"));
        configuration.merge(Configuration.from(new File("C:\\dev\\ideaProject\\DataAggregation\\core\\src\\main\\resources\\core.json")), true);
        XxlJobContext xxlJobContext = XxlJobContext.getXxlJobContext();
        long jobId = xxlJobContext.getJobId();
        configuration.set("jobId", jobId);
        JobContainer container = new JobContainer(configuration);
        container.addConsumerPlugin(PluginType.READER, new Reader.Job() {
            private final Logger log = LoggerFactory.getLogger(AggregationCustomXxlJob.class);

            private String querySql;

            private Connection connection;

            @Override
            public void init() {
                log.info("custom init");
                querySql = "select id,test_param1 from datax_test1";

                BaseDataSourceDTO baseDataSourceDTO = new BaseDataSourceDTO();
                baseDataSourceDTO.setHost("rmHost");
                baseDataSourceDTO.setPort("3305");
                baseDataSourceDTO.setDatabase("datax_test");
                baseDataSourceDTO.setUserName("root");
                baseDataSourceDTO.setPassword("951753");
                baseDataSourceDTO.setUsePool(true);
                PluginClassLoaderCloseable loaderSwapper =
                        PluginClassLoaderCloseable.newCurrentThreadClassLoaderSwapper(SourcePluginType.SOURCE, DataSourceType.Mysql8.getTypeName());
                try {
                    AbstractDataSourcePlugin dataSourcePlugin = loaderSwapper.loadPlugin();
                    connection = dataSourcePlugin.getConnection(baseDataSourceDTO);
                } finally {
                    loaderSwapper.close();
                }
            }

            @Override
            public void startRead(RecordSender recordSender) {
                Statement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = connection.createStatement();
                    resultSet = statement.executeQuery(querySql);
                    while (resultSet.next()) {
                        int id = resultSet.getInt("id");
                        String testParam1 = resultSet.getString("test_param1");
                        Record record = new DefaultRecord();
                        record.setColumn(0, new LongColumn(id));
                        record.setColumn(1, new StringColumn(testParam1));
                        recordSender.sendToWriter(record);
                    }
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } finally {
                    closeDBResources(resultSet, statement, connection);
                }
            }

            @Override
            public void post() {
                log.info("custom post");
            }

            public void closeDBResources(ResultSet rs, Statement stmt,
                                         Connection conn) {
                if (null != rs) {
                    try {
                        rs.close();
                    } catch (SQLException unused) {
                    }
                }

                if (null != stmt) {
                    try {
                        stmt.close();
                    } catch (SQLException unused) {
                    }
                }

                if (null != conn) {
                    try {
                        conn.close();
                    } catch (SQLException unused) {
                    }
                }
            }
        });
        container.start();
    }
}
