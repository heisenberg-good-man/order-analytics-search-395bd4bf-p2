package com.platform.controller;

import com.platform.common.Result;
import com.platform.enums.ProfessionType;
import com.platform.enums.VerifyStatus;
import com.platform.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/dict")
public class DictController {

    @Autowired
    private ProviderService providerService;

    @GetMapping("/profession-types")
    public Result<List<Map<String, String>>> getProfessionTypes() {
        List<Map<String, String>> list = new ArrayList<>();
        for (ProfessionType type : ProfessionType.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("value", type.name());
            item.put("label", type.getDesc());
            list.add(item);
        }
        return Result.success(list);
    }

    @GetMapping("/verify-statuses")
    public Result<List<Map<String, String>>> getVerifyStatuses() {
        List<Map<String, String>> list = new ArrayList<>();
        for (VerifyStatus status : VerifyStatus.values()) {
            Map<String, String> item = new HashMap<>();
            item.put("value", status.name());
            item.put("label", status.getDesc());
            list.add(item);
        }
        return Result.success(list);
    }

    @GetMapping("/cities")
    public Result<List<String>> getCities() {
        return Result.success(providerService.getAllCities());
    }
}
