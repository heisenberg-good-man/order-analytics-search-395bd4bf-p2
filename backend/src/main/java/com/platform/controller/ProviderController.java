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
import java.util.Map;

@RestController
@RequestMapping("/providers")
public class ProviderController {

    @Autowired
    private ProviderService providerService;

    @GetMapping
    public Result<List<ServiceProvider>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String verifyStatus,
            @RequestParam(required = false) String professionType) {
        return Result.success(providerService.list(keyword, verifyStatus, professionType));
    }

    @GetMapping("/{id}")
    public Result<ServiceProvider> getById(@PathVariable String id) {
        ServiceProvider p = providerService.getById(id);
        if (p == null) {
            return Result.error(404, "服务商不存在");
        }
        return Result.success(p);
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
        ServiceProvider p = providerService.update(id, dto);
        if (p == null) {
            return Result.error(404, "服务商不存在");
        }
        return Result.success(p);
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
        try {
            ServiceProvider p = providerService.submitVerify(id, dto);
            if (p == null) {
                return Result.error(404, "服务商不存在");
            }
            return Result.success(p);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/verify/approve")
    public Result<ServiceProvider> approve(@PathVariable String id, @RequestBody(required = false) VerifyAuditDTO dto) {
        try {
            if (dto == null) dto = new VerifyAuditDTO();
            ServiceProvider p = providerService.approve(id, dto);
            if (p == null) {
                return Result.error(404, "服务商不存在");
            }
            return Result.success(p);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @PostMapping("/{id}/verify/reject")
    public Result<ServiceProvider> reject(@PathVariable String id, @RequestBody VerifyAuditDTO dto) {
        try {
            if (dto.getRemark() == null || dto.getRemark().trim().isEmpty()) {
                return Result.error("驳回原因不能为空");
            }
            ServiceProvider p = providerService.reject(id, dto);
            if (p == null) {
                return Result.error(404, "服务商不存在");
            }
            return Result.success(p);
        } catch (IllegalStateException e) {
            return Result.error(e.getMessage());
        }
    }

    @GetMapping("/{id}/verify-records")
    public Result<List<VerifyRecord>> getVerifyRecords(@PathVariable String id) {
        return Result.success(providerService.getVerifyRecords(id));
    }
}
