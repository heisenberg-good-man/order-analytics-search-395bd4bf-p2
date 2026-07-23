package com.platform.controller;

import com.platform.common.Result;
import com.platform.dto.ProviderCreateDTO;
import com.platform.dto.ProviderUpdateDTO;
import com.platform.dto.VerifyAuditDTO;
import com.platform.dto.VerifySubmitDTO;
import com.platform.model.ServiceProvider;
import com.platform.model.VerifyRecord;
import com.platform.service.ProviderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/providers")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @GetMapping
    public Result<List<ServiceProvider>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String verifyStatus,
            @RequestParam(required = false) String professionType,
            @RequestParam(required = false) String city) {
        return Result.success(providerService.list(keyword, verifyStatus, professionType, city));
    }

    @GetMapping("/{id}")
    public Result<ServiceProvider> getById(@PathVariable String id) {
        return Result.success(providerService.getByIdOrThrow(id));
    }

    @PostMapping
    public Result<ServiceProvider> create(@RequestBody ProviderCreateDTO dto) {
        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            return Result.error("姓名不能为空");
        }
        if (dto.getPhone() == null || dto.getPhone().trim().isEmpty()) {
            return Result.error("手机号不能为空");
        }
        if (dto.getProfessionType() == null || dto.getProfessionType().trim().isEmpty()) {
            return Result.error("职业类型不能为空");
        }
        if (dto.getCity() == null || dto.getCity().trim().isEmpty()) {
            return Result.error("服务城市不能为空");
        }
        return Result.success(providerService.create(dto));
    }

    @PutMapping("/{id}")
    public Result<ServiceProvider> update(@PathVariable String id, @RequestBody ProviderUpdateDTO dto) {
        return Result.success(providerService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable String id) {
        boolean ok = providerService.delete(id);
        if (!ok) {
            return Result.error(404, "服务商不存在");
        }
        return Result.success();
    }

    @PostMapping("/{id}/verify/submit")
    public Result<ServiceProvider> submitVerify(@PathVariable String id, @RequestBody VerifySubmitDTO dto) {
        return Result.success(providerService.submitVerify(id, dto));
    }

    @PostMapping("/{id}/verify/approve")
    public Result<ServiceProvider> approve(@PathVariable String id, @RequestBody(required = false) VerifyAuditDTO dto) {
        return Result.success(providerService.approve(id, dto));
    }

    @PostMapping("/{id}/verify/reject")
    public Result<ServiceProvider> reject(@PathVariable String id, @RequestBody VerifyAuditDTO dto) {
        return Result.success(providerService.reject(id, dto));
    }

    @PostMapping("/{id}/verify/send-back")
    public Result<ServiceProvider> sendBack(@PathVariable String id, @RequestBody VerifyAuditDTO dto) {
        return Result.success(providerService.sendBack(id, dto));
    }

    @GetMapping("/{id}/verify-records")
    public Result<List<VerifyRecord>> getVerifyRecords(@PathVariable String id) {
        providerService.getByIdOrThrow(id);
        return Result.success(providerService.getVerifyRecords(id));
    }

    @GetMapping("/{id}/missing-fields")
    public Result<List<String>> getMissingFields(@PathVariable String id) {
        ServiceProvider p = providerService.getByIdOrThrow(id);
        return Result.success(providerService.checkMissingFields(p));
    }
}
