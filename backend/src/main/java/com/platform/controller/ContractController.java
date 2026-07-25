package com.platform.controller;

import com.platform.common.Result;
import com.platform.dto.ContractCreateDTO;
import com.platform.model.Contract;
import com.platform.service.ContractService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    @Autowired
    private ContractService contractService;

    @GetMapping
    public Result<List<Contract>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String professionType,
            @RequestParam(required = false) String city) {
        return Result.success(contractService.list(keyword, status, professionType, city));
    }

    @GetMapping("/{id}")
    public Result<Contract> getById(@PathVariable String id) {
        return Result.success(contractService.getById(id));
    }

    @PostMapping
    public Result<Contract> createDraft(@RequestBody ContractCreateDTO dto) {
        return Result.success(contractService.createDraft(dto));
    }

    @PostMapping("/{id}/sign")
    public Result<Contract> sign(
            @PathVariable String id,
            @RequestParam String role) {
        return Result.success(contractService.signContract(id, role));
    }

    @PostMapping("/{id}/reject")
    public Result<Contract> reject(
            @PathVariable String id,
            @RequestParam String reason) {
        return Result.success(contractService.rejectContract(id, reason));
    }

    @GetMapping("/by-requirement/{requirementId}")
    public Result<List<Contract>> getByRequirementId(@PathVariable String requirementId) {
        return Result.success(contractService.getByRequirementId(requirementId));
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        return Result.success(contractService.getStatistics());
    }
}
