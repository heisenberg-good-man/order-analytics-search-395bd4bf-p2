package com.platform.controller;

import com.platform.common.Result;
import com.platform.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private ProviderService providerService;

    @GetMapping
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(providerService.getStatistics());
    }
}
