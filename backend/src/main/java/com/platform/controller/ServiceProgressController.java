package com.platform.controller;

import com.platform.common.Result;
import com.platform.dto.ProgressAdvanceDTO;
import com.platform.dto.ProgressNodeAddDTO;
import com.platform.model.ProgressNode;
import com.platform.model.ServiceProgress;
import com.platform.service.ServiceProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin
public class ServiceProgressController {

    @Autowired
    private ServiceProgressService progressService;

    @GetMapping
    public Result<List<ServiceProgress>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String professionType,
            @RequestParam(required = false) String city) {
        List<ServiceProgress> list = progressService.list(status, professionType, city);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<ServiceProgress> getById(@PathVariable String id) {
        ServiceProgress sp = progressService.getById(id);
        return Result.success(sp);
    }

    @GetMapping("/by-contract/{contractId}")
    public Result<ServiceProgress> getByContractId(@PathVariable String contractId) {
        ServiceProgress sp = progressService.getByContractId(contractId);
        return Result.success(sp);
    }

    @GetMapping("/by-requirement/{requirementId}")
    public Result<ServiceProgress> getByRequirementId(@PathVariable String requirementId) {
        ServiceProgress sp = progressService.getByRequirementId(requirementId);
        return Result.success(sp);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = progressService.getStatistics();
        return Result.success(stats);
    }

    @PostMapping("/create/{contractId}")
    public Result<ServiceProgress> createProgress(@PathVariable String contractId) {
        ServiceProgress sp = progressService.createProgress(contractId);
        return Result.success(sp);
    }

    @PostMapping("/{id}/advance")
    public Result<ServiceProgress> advanceNode(
            @PathVariable String id,
            @RequestBody ProgressAdvanceDTO dto) {
        ServiceProgress sp = progressService.advanceNode(id, dto);
        return Result.success(sp);
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        Map<String, Object> detail = progressService.getProgressDetail(id);
        return Result.success(detail);
    }

    @PostMapping("/{id}/nodes")
    public Result<ProgressNode> addCustomNode(
            @PathVariable String id,
            @RequestBody ProgressNodeAddDTO dto) {
        ProgressNode node = progressService.addCustomNode(id, dto);
        return Result.success(node);
    }

    @PostMapping("/{id}/pause")
    public Result<ServiceProgress> pauseProgress(
            @PathVariable String id,
            @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        String operator = body.get("operator");
        ServiceProgress sp = progressService.pauseProgress(id, reason, operator);
        return Result.success(sp);
    }

    @PostMapping("/{id}/resume")
    public Result<ServiceProgress> resumeProgress(
            @PathVariable String id,
            @RequestBody(required = false) Map<String, String> body) {
        String operator = body != null ? body.get("operator") : null;
        ServiceProgress sp = progressService.resumeProgress(id, operator);
        return Result.success(sp);
    }
}
