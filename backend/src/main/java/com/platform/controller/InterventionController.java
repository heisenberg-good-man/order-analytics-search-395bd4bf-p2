package com.platform.controller;

import com.platform.common.Result;
import com.platform.dto.InterventionCreateDTO;
import com.platform.dto.InterventionHandleDTO;
import com.platform.model.Intervention;
import com.platform.service.InterventionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/intervention")
@CrossOrigin
public class InterventionController {

    @Autowired
    private InterventionService interventionService;

    @GetMapping
    public Result<List<Intervention>> list(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String professionType,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String issueType) {
        List<Intervention> list = interventionService.list(status, professionType, city, issueType);
        return Result.success(list);
    }

    @GetMapping("/{id}")
    public Result<Intervention> getById(@PathVariable String id) {
        Intervention iv = interventionService.getById(id);
        return Result.success(iv);
    }

    @GetMapping("/{id}/detail")
    public Result<Map<String, Object>> getDetail(@PathVariable String id) {
        Map<String, Object> detail = interventionService.getInterventionDetail(id);
        return Result.success(detail);
    }

    @GetMapping("/statistics")
    public Result<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = interventionService.getStatistics();
        return Result.success(stats);
    }

    @GetMapping("/by-progress/{progressId}")
    public Result<List<Intervention>> getByProgressId(@PathVariable String progressId) {
        List<Intervention> list = interventionService.getByProgressId(progressId);
        return Result.success(list);
    }

    @GetMapping("/by-requirement/{requirementId}")
    public Result<List<Intervention>> getByRequirementId(@PathVariable String requirementId) {
        List<Intervention> list = interventionService.getByRequirementId(requirementId);
        return Result.success(list);
    }

    @PostMapping
    public Result<Intervention> create(@RequestBody InterventionCreateDTO dto) {
        Intervention iv = interventionService.createIntervention(dto);
        return Result.success(iv);
    }

    @PostMapping("/{id}/accept")
    public Result<Intervention> accept(@PathVariable String id, @RequestBody InterventionHandleDTO dto) {
        Intervention iv = interventionService.acceptIntervention(id, dto);
        return Result.success(iv);
    }

    @PostMapping("/{id}/supplement")
    public Result<Intervention> requestSupplement(@PathVariable String id, @RequestBody InterventionHandleDTO dto) {
        Intervention iv = interventionService.requestSupplement(id, dto);
        return Result.success(iv);
    }

    @PostMapping("/{id}/resolve")
    public Result<Intervention> resolve(@PathVariable String id, @RequestBody InterventionHandleDTO dto) {
        Intervention iv = interventionService.resolveIntervention(id, dto);
        return Result.success(iv);
    }

    @PostMapping("/{id}/close")
    public Result<Intervention> close(@PathVariable String id, @RequestBody InterventionHandleDTO dto) {
        Intervention iv = interventionService.closeIntervention(id, dto);
        return Result.success(iv);
    }
}
