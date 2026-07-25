package com.platform.controller;

import com.platform.common.Result;
import com.platform.dto.RequirementCreateDTO;
import com.platform.model.Requirement;
import com.platform.service.RequirementService;
import com.platform.vo.RecommendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/requirements")
public class RequirementController {

    @Autowired
    private RequirementService requirementService;

    @GetMapping
    public Result<List<Requirement>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String professionType,
            @RequestParam(required = false) String city) {
        return Result.success(requirementService.list(keyword, status, professionType, city));
    }

    @GetMapping("/{id}")
    public Result<Requirement> getById(@PathVariable String id) {
        return Result.success(requirementService.getById(id));
    }

    @PostMapping
    public Result<Requirement> create(@RequestBody RequirementCreateDTO dto) {
        return Result.success(requirementService.create(dto));
    }

    @GetMapping("/{id}/recommend")
    public Result<List<RecommendResult>> recommend(@PathVariable String id) {
        return Result.success(requirementService.recommendProviders(id));
    }

    @GetMapping("/{id}/recommend-stats")
    public Result<Map<String, Object>> recommendStats(@PathVariable String id) {
        return Result.success(requirementService.getRecommendStats(id));
    }

    @PostMapping("/{id}/select-provider")
    public Result<Requirement> selectProvider(
            @PathVariable String id,
            @RequestParam String providerId) {
        return Result.success(requirementService.selectProvider(id, providerId));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(requirementService.getStatistics());
    }
}
