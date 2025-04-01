package com.xxl.job.executor.mvc.controller;


import com.jdragon.aggregation.core.plugin.PluginType;
import com.jdragon.aggregation.pluginloader.LoadUtil;
import com.xxl.job.executor.entity.TestEntity;
import com.xxl.job.executor.utils.Result;
import com.xxl.job.executor.utils.pagination.PageFactory;
import com.xxl.job.executor.utils.pagination.PageTable;
import com.xxl.job.executor.utils.pagination.TableRef;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("aggregation")
@RestController
@RequestMapping("/aggregation")
public class AggregationController {

    @ApiOperation("updatePlugin")
    @GetMapping("/updatePlugin")
    public String updatePlugin(@RequestParam PluginType pluginType,
                               @RequestParam String pluginName) {
        LoadUtil.updateJarLoader(pluginType, pluginName);
        return "success";
    }

    @ApiOperation("httpTest")
    @GetMapping("/httpTest")
    public Result<PageTable<TestEntity>> updatePlugin(@RequestParam Integer pageNum, @RequestParam Integer pageSize, @RequestHeader String token) {
        if (!"123456".equals(token)) {
            throw new RuntimeException("token error");
        }
        List<TestEntity> list = new ArrayList<>();
        int total = 39;
        int curStart = (pageNum - 1) * pageSize;
        if (curStart >= total) {
            return Result.success(PageFactory.buildPageTable(total, pageNum, pageSize, new TableRef<TestEntity>(list) {
            }));
        }
        for (int i = 0; i < pageSize; i++) {
            TestEntity testEntity = new TestEntity();
            int id = curStart + i + 1;
            if (id > total) {
                break;
            }
            testEntity.setId(id);
            testEntity.setTestParam("test-param" + id);
            list.add(testEntity);
        }
        return Result.success(PageFactory.buildPageTable(total, pageNum, pageSize, new TableRef<TestEntity>(list) {
        }));
    }

    @ApiOperation("login")
    @GetMapping("/login")
    public Result<String> login() {
        return Result.success("123456");
    }
}
