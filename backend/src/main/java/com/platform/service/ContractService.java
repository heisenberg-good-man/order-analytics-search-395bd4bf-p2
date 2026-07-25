package com.platform.service;

import com.platform.common.BusinessException;
import com.platform.dto.ContractCreateDTO;
import com.platform.enums.VerifyStatus;
import com.platform.model.Contract;
import com.platform.model.Requirement;
import com.platform.model.ServiceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ContractService {

    private final Map<String, Contract> contractMap = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Autowired
    private RequirementService requirementService;

    @Autowired
    private ProviderService providerService;

    @PostConstruct
    public void initMockData() {
        Contract c1 = new Contract();
        c1.setId("CT" + String.format("%04d", idGenerator.getAndIncrement()));
        c1.setRequirementId("REQ0006");
        c1.setRequirementTitle("接送孩子上下学司机");
        c1.setProviderId("SP0006");
        c1.setProviderName("孙女士");
        c1.setProfessionType("DRIVER");
        c1.setCity("成都");
        c1.setSkillTags(Arrays.asList("C1驾照", "长途驾驶", "商务接待"));
        c1.setBudget(new BigDecimal("8000"));
        c1.setServiceStartTime("2026-07-20");
        c1.setServiceEndTime("2026-07-31");
        c1.setRemark("每月8000元，接送孩子上下学");
        c1.setProviderVerifyStatus("APPROVED");
        c1.setProviderVerifySummary("已认证，资料齐全");
        c1.setStatus("SIGNED");
        c1.setProviderConfirmed(true);
        c1.setDemanderConfirmed(true);
        c1.setCreateTime(LocalDateTime.now().minusDays(5));
        c1.setUpdateTime(LocalDateTime.now().minusDays(4));
        contractMap.put(c1.getId(), c1);

        Contract c2 = new Contract();
        c2.setId("CT" + String.format("%04d", idGenerator.getAndIncrement()));
        c2.setRequirementId("REQ0002");
        c2.setRequirementTitle("家电维修 - 空调不制冷");
        c2.setProviderId("SP0002");
        c2.setProviderName("王师傅");
        c2.setProfessionType("REPAIRMAN");
        c2.setCity("上海");
        c2.setSkillTags(Arrays.asList("水电维修", "家电维修"));
        c2.setBudget(new BigDecimal("300"));
        c2.setServiceStartTime("2026-07-26");
        c2.setServiceEndTime("2026-07-26");
        c2.setRemark("上门维修空调");
        c2.setProviderVerifyStatus("APPROVED");
        c2.setProviderVerifySummary("已认证，资料齐全");
        c2.setStatus("DRAFT");
        c2.setProviderConfirmed(false);
        c2.setDemanderConfirmed(false);
        c2.setCreateTime(LocalDateTime.now().minusHours(3));
        c2.setUpdateTime(LocalDateTime.now().minusHours(3));
        contractMap.put(c2.getId(), c2);
    }

    public List<Contract> list(String keyword, String status, String professionType, String city) {
        return contractMap.values().stream()
            .filter(c -> {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    boolean match = c.getRequirementTitle().toLowerCase().contains(kw)
                        || c.getProviderName().toLowerCase().contains(kw)
                        || c.getId().toLowerCase().contains(kw);
                    if (!match) return false;
                }
                if (status != null && !status.trim().isEmpty()) {
                    if (!c.getStatus().equals(status)) return false;
                }
                if (professionType != null && !professionType.trim().isEmpty()) {
                    if (!c.getProfessionType().equals(professionType)) return false;
                }
                if (city != null && !city.trim().isEmpty()) {
                    if (!c.getCity().equals(city.trim())) return false;
                }
                return true;
            })
            .sorted(Comparator.comparing(Contract::getCreateTime).reversed())
            .collect(Collectors.toList());
    }

    public Contract getById(String id) {
        Contract c = contractMap.get(id);
        if (c == null) {
            throw new BusinessException(404, "合同不存在");
        }
        return c;
    }

    public Contract createDraft(ContractCreateDTO dto) {
        if (dto.getRequirementId() == null || dto.getRequirementId().trim().isEmpty()) {
            throw new BusinessException("需求ID不能为空");
        }
        if (dto.getProviderId() == null || dto.getProviderId().trim().isEmpty()) {
            throw new BusinessException("请先选择服务商");
        }
        if (dto.getBudget() == null || dto.getBudget().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("请填写有效的预算金额");
        }
        if (dto.getServiceStartTime() == null || dto.getServiceStartTime().trim().isEmpty()) {
            throw new BusinessException("请选择服务开始时间");
        }
        if (dto.getServiceEndTime() == null || dto.getServiceEndTime().trim().isEmpty()) {
            throw new BusinessException("请选择服务结束时间");
        }

        Requirement req = requirementService.getById(dto.getRequirementId());
        ServiceProvider provider = providerService.getByIdOrThrow(dto.getProviderId());

        if (provider.getVerifyStatus() != VerifyStatus.APPROVED) {
            String statusDesc = provider.getVerifyStatus().getDesc();
            throw new BusinessException("服务商当前为" + statusDesc + "状态，不能生成合同，请先完成实名认证");
        }

        if ("CLOSED".equals(req.getStatus())) {
            throw new BusinessException("需求已关闭，不能生成合同");
        }
        if ("SIGNED".equals(req.getStatus())) {
            throw new BusinessException("需求已签约，不能重复生成合同");
        }
        if ("MATCHED".equals(req.getStatus())) {
            long signedCount = contractMap.values().stream()
                .filter(c -> c.getRequirementId().equals(req.getId()) && "SIGNED".equals(c.getStatus()))
                .count();
            if (signedCount > 0) {
                throw new BusinessException("该需求已有已签约合同，不能重复生成");
            }
        }
        if (!req.getProfessionType().equals(provider.getProfessionType())) {
            throw new BusinessException("服务商职业类型与需求不匹配");
        }
        if (!req.getCity().equals(provider.getCity())) {
            throw new BusinessException("服务商所在城市与需求不匹配");
        }

        long existingDraftCount = contractMap.values().stream()
            .filter(c -> c.getRequirementId().equals(req.getId()) && "DRAFT".equals(c.getStatus()))
            .count();
        if (existingDraftCount > 0) {
            throw new BusinessException("该需求已有待确认的合同草案，请先处理现有合同");
        }

        long existingSignedCount = contractMap.values().stream()
            .filter(c -> c.getRequirementId().equals(req.getId()) && "SIGNED".equals(c.getStatus()))
            .count();
        if (existingSignedCount > 0) {
            throw new BusinessException("该需求已有已签约合同，不能重复生成");
        }

        String id = "CT" + String.format("%04d", idGenerator.getAndIncrement());
        Contract c = new Contract();
        c.setId(id);
        c.setRequirementId(req.getId());
        c.setRequirementTitle(req.getTitle());
        c.setProviderId(provider.getId());
        c.setProviderName(provider.getName());
        c.setProfessionType(provider.getProfessionType());
        c.setCity(provider.getCity());
        c.setSkillTags(provider.getSkillTags());
        c.setBudget(dto.getBudget());
        c.setServiceStartTime(dto.getServiceStartTime());
        c.setServiceEndTime(dto.getServiceEndTime());
        c.setRemark(dto.getRemark());
        c.setProviderVerifyStatus(provider.getVerifyStatus().name());
        c.setProviderVerifySummary("已认证，资料完整度 " + provider.getCompleteness() + "%");
        c.setStatus("DRAFT");
        c.setProviderConfirmed(false);
        c.setDemanderConfirmed(false);
        c.setCreateTime(LocalDateTime.now());
        c.setUpdateTime(LocalDateTime.now());
        contractMap.put(id, c);

        return c;
    }

    public Contract signContract(String id, String role) {
        Contract c = getById(id);

        if (!"DRAFT".equals(c.getStatus())) {
            throw new BusinessException("只有草案状态的合同才能签约");
        }

        if ("provider".equals(role)) {
            c.setProviderConfirmed(true);
        } else if ("demander".equals(role)) {
            c.setDemanderConfirmed(true);
        } else {
            throw new BusinessException("无效的签约角色");
        }

        if (Boolean.TRUE.equals(c.getProviderConfirmed()) && Boolean.TRUE.equals(c.getDemanderConfirmed())) {
            c.setStatus("SIGNED");
            Requirement req = requirementService.getById(c.getRequirementId());
            if (!"SIGNED".equals(req.getStatus())) {
                req.setStatus("SIGNED");
                req.setSelectedProviderId(c.getProviderId());
                req.setUpdateTime(LocalDateTime.now());
            }
        }

        c.setUpdateTime(LocalDateTime.now());
        return c;
    }

    public Contract rejectContract(String id, String reason) {
        Contract c = getById(id);

        if (!"DRAFT".equals(c.getStatus())) {
            throw new BusinessException("只有草案状态的合同才能驳回");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写驳回原因");
        }

        c.setStatus("REJECTED");
        c.setRejectReason(reason);
        c.setUpdateTime(LocalDateTime.now());
        return c;
    }

    public List<Contract> getByRequirementId(String requirementId) {
        return contractMap.values().stream()
            .filter(c -> c.getRequirementId().equals(requirementId))
            .sorted(Comparator.comparing(Contract::getCreateTime).reversed())
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Collection<Contract> all = contractMap.values();
        stats.put("total", all.size());
        stats.put("draft", (int) all.stream().filter(c -> "DRAFT".equals(c.getStatus())).count());
        stats.put("signed", (int) all.stream().filter(c -> "SIGNED".equals(c.getStatus())).count());
        stats.put("rejected", (int) all.stream().filter(c -> "REJECTED".equals(c.getStatus())).count());

        Map<String, Long> professionStats = all.stream()
            .collect(Collectors.groupingBy(Contract::getProfessionType, Collectors.counting()));
        stats.put("professionStats", professionStats);

        Map<String, Long> cityStats = all.stream()
            .collect(Collectors.groupingBy(Contract::getCity, Collectors.counting()));
        stats.put("cityStats", cityStats);

        return stats;
    }
}
