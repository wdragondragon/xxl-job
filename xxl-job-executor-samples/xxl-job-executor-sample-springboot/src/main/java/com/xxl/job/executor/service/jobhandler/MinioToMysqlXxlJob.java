package com.xxl.job.executor.service.jobhandler;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jdragon.aggregation.commons.pagination.Table;
import com.jdragon.aggregation.commons.util.Configuration;
import com.jdragon.aggregation.datasource.AbstractDataSourcePlugin;
import com.jdragon.aggregation.datasource.BaseDataSourceDTO;
import com.jdragon.aggregation.datasource.SourcePluginType;
import com.jdragon.aggregation.datasource.file.FileHelper;
import com.jdragon.aggregation.pluginloader.PluginClassLoaderCloseable;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.executor.utils.SnowflakeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.*;


@Component
public class MinioToMysqlXxlJob extends IJobHandler {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Override
    public void execute() {
        String table = "so_pf_data_qua_eva_score";
        List<String> columns = Arrays.asList("indicate_no", "pro_id", "org_id", "score_year", "score_type", "score_type_index", "score_value");
        Configuration configuration = Configuration.newDefault();
        configuration.set("endpoint", "");
        configuration.set("accessKey", "");
        configuration.set("secretKey", "");
        configuration.set("bucket", "");
        List<Map<String, String>> dataList = readDataFromOssCsv(columns, configuration);

        BaseDataSourceDTO baseDataSourceDTO = new BaseDataSourceDTO();
        baseDataSourceDTO.setHost("");
        baseDataSourceDTO.setPort("");
        baseDataSourceDTO.setUserName("");
        baseDataSourceDTO.setPassword("");
        baseDataSourceDTO.setDatabase("data_fusion_service");

        // 填充最新的indicateId
        columns.add("indicate_id");
        String querySql = "SELECT t1.indicate_no, t1.id, t1.create_time " +
                "FROM so_pf_data_qua_eva_indicate t1 " +
                "JOIN (" +
                "    SELECT indicate_no, MAX(create_time) AS max_create_time " +
                "    FROM so_pf_data_qua_eva_indicate " +
                "    GROUP BY indicate_no) t2 " +
                "ON t1.indicate_no = t2.indicate_no AND t1.create_time = t2.max_create_time";
        Map<String, String> indicateNoToLastIdMap = getDictMapping(baseDataSourceDTO, "indicate_no", "id", querySql);
        for (Map<String, String> map : dataList) {
            String indicateNo = map.get("indicate_no");
            String indicateId = indicateNoToLastIdMap.get(indicateNo);
            if (indicateId == null) {
                throw new RuntimeException("通过indicate_no[" + indicateNo + "]无法找到最新的indicate_id");
            }
            map.put("indicate_id", indicateId);
        }

        // 转换org和pro
        Map<String, Map<String, String>> columnsTransformerMap = new HashMap<>();
        querySql = "select org.id,org.org_name,map.org_code" +
                " from so_pf_sys_organization_map map join " +
                " so_pf_sys_org org on map.dispatch_org = org.org_name and pro_id is null";
        Map<String, String> orgIdDict = getDictMapping(baseDataSourceDTO, "org_code", "id", querySql);
        querySql = "select dict_name,dict_code from so_pf_dictionaries_data where dic_type='SPECIALITY_TYPE'";
        Map<String, String> majorDict = getDictMapping(baseDataSourceDTO, "dict_name", "dict_code", querySql);
        columnsTransformerMap.put("org_id", orgIdDict);
        columnsTransformerMap.put("pro_id", majorDict);
        for (Map<String, String> data : dataList) {
            for (String column : columns) {
                String value = data.get(column);
                if (columnsTransformerMap.containsKey(column)) {
                    String newValue = columnsTransformerMap.get(column).get(value);
                    if (newValue == null) {
                        throw new RuntimeException(String.format("找不到[%s]字段的值[%s]对应的id", column, value));
                    }
                    data.put(column, newValue);
                }
            }
        }

        List<List<String>> insertDataList = new ArrayList<>();
        for (Map<String, String> data : dataList) {
            List<String> transData = new LinkedList<>();
            for (String column : columns) {
                transData.add(data.get(column));
            }
            insertDataList.add(transData);
        }

        List<String> delSqlList = new LinkedList<>();
        for (List<String> delDataList : insertDataList) {
            List<String> delWhereList = new LinkedList<>();
            String delTemplate = "%s=\"%s\"";
            for (int i = 0; i < columns.size() - 1; i++) {
                String delWhereColumn = columns.get(i);
                String delWhereValue = delDataList.get(i);
                String delWhere = String.format(delTemplate, delWhereColumn, delWhereValue);
                delWhereList.add(delWhere);
            }
            String where = String.join(" and ", delWhereList);
            String delSql = String.format("delete from %s where %s", table, where);
            delSqlList.add(delSql);
        }

        String insertColumnsStr = String.join(",", columns);
        List<String> batchInsert = new LinkedList<>();
        for (List<String> transData : insertDataList) {
            String insertValueStr = String.join(",", "\"" + transData + "\"");
            String insertSql = String.format("insert into %s(id,%s) values('%s',%s)", table, insertColumnsStr, SnowflakeUtil.getSnowflake(), insertValueStr);
            batchInsert.add(insertSql);
        }

        List<String> exeSqlList = new LinkedList<>();
        exeSqlList.addAll(delSqlList);
        exeSqlList.addAll(batchInsert);

        log.info("需要执行语句：\n{}", JSONObject.toJSONString(exeSqlList, SerializerFeature.PrettyFormat));

        PluginClassLoaderCloseable classLoaderSwapper = PluginClassLoaderCloseable.newCurrentThreadClassLoaderSwapper(SourcePluginType.SOURCE, "mysql8");
        try {
            AbstractDataSourcePlugin sourcePlugin = classLoaderSwapper.loadPlugin();
            sourcePlugin.executeBatch(baseDataSourceDTO, exeSqlList);
        } finally {
            classLoaderSwapper.close();
        }
    }

    public List<Map<String, String>> readDataFromOssCsv(List<String> columns, Configuration ossConfig) {
        List<Map<String, String>> dataList = new LinkedList<>();
        PluginClassLoaderCloseable classLoaderSwapper = PluginClassLoaderCloseable.newCurrentThreadClassLoaderSwapper(SourcePluginType.SOURCE, "minio");
        try {
            FileHelper fileHelper = classLoaderSwapper.loadPlugin();
            fileHelper.connect(ossConfig);
            InputStream inputStream = fileHelper.getInputStream("/test", "test.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] split = line.split(",");
                    if (split.length == columns.size()) {
                        Map<String, String> values = new HashMap<>(columns.size());
                        for (int i = 0; i < dataList.size(); i++) {
                            String value = split[i].trim();
                            String key = columns.get(i);
                            values.put(key, value);
                        }
                        dataList.add(values);
                    } else {
                        log.error("读取的列数与预设字段数量不一致，数据列{}-字段列{},数据详情：{}", split.length, columns.size(), JSONObject.toJSONString(Arrays.asList(split)));
                    }
                }
            } catch (IOException e) {
                log.error("读取文件异常：{}", e.getMessage(), e);
            } finally {
                reader.close();
            }

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            classLoaderSwapper.close();
        }
        return dataList;
    }

    public Map<String, String> getDictMapping(BaseDataSourceDTO baseDataSourceDTO, String keyColumn, String valueColumn, String querySql) {
        Map<String, String> dictMapping = new HashMap<>();
        PluginClassLoaderCloseable classLoaderSwapper = PluginClassLoaderCloseable.newCurrentThreadClassLoaderSwapper(SourcePluginType.SOURCE, "mysql8");
        try {
            AbstractDataSourcePlugin sourcePlugin = classLoaderSwapper.loadPlugin();
            Table<Map<String, Object>> mapTable = sourcePlugin.executeQuerySql(baseDataSourceDTO, querySql, true);
            List<Map<String, Object>> bodies = mapTable.getBodies();
            for (Map<String, Object> body : bodies) {
                String key = String.valueOf(body.get(keyColumn));
                String value = String.valueOf(body.get(valueColumn));
                dictMapping.put(key, value);
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            classLoaderSwapper.close();
        }
        return dictMapping;
    }
}
