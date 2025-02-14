package com.xxl.job.executor.mvc.controller;


import com.jdragon.aggregation.core.plugin.PluginType;
import com.jdragon.aggregation.pluginloader.LoadUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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

}
