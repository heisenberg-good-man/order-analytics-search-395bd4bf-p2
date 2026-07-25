package com.platform.service;

import com.platform.common.BusinessException;
import com.platform.dto.InterventionCreateDTO;
import com.platform.dto.InterventionHandleDTO;
import com.platform.enums.InterventionStatus;
import com.platform.model.Contract;
import com.platform.model.Intervention;
import com.platform.model.Requirement;
import com.platform.model.ServiceProvider;
import com.platform.model.ServiceProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class InterventionService {

    @Autowired
    private RequirementService requirementService;

    @Autowired
    private ContractService contractService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private ServiceProgressService progressService;

    private final Map<String, Intervention> interventionMap = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostConstruct
    public void initMockData() {
        addMockIntervention("IV0001", "REQ0006", "CT0001", "SP0006", "SP0001",
            "张师傅（成都-司机）", "服务中", "DEMANDER", "李先生",
            "SERVICE_QUALITY", "司机迟到2小时，服务态度差", "要求退还50%费用并道歉",
            InterventionStatus.PENDING, "driver", "成都", new BigDecimal("6000"));

        addMockIntervention("IV0002", "REQ0002", "CT0002", "SP0002", null,
            "王师傅（上海-维修）", "待上门", "PROVIDER", "王师傅",
            "PAYMENT_DISPUTE", "需求方临时取消但不支付定金", "要求支付违约金300元",
            InterventionStatus.PROCESSING, "repair", "上海", new BigDecimal("1500"));

        addMockIntervention("IV0003", "REQ0001", null, "SP0001", null,
            "张师傅（北京-保姆）", "待上门", "DEMANDER", "王女士",
            "CANCEL_DISPUTE", "服务商临时爽约，找不到替代", "要求平台介入追责",
            InterventionStatus.SUPPLEMENT, "housekeeper", "北京", new BigDecimal("8000"));

        addMockIntervention("IV0004", "REQ0006", "CT0001", "SP0006", "SP0001",
            "张师傅（成都-司机）", "服务中", "DEMANDER", "李先生",
            "OTHER", "服务内容与合同描述不符", "希望平台协调解决",
            InterventionStatus.RESOLVED, "driver", "成都", new BigDecimal("6000"));

        addMockIntervention("IV0005", "REQ0005", null, "SP0005", null,
            "赵师傅（广州-家教）", "待上门", "DEMANDER", "陈家长",
            "QUALITY_COMPLAINT", "家教老师频繁更换，教学效果差", "要求全额退款",
            InterventionStatus.CLOSED, "tutor", "广州", new BigDecimal("3000"));
    }

    private void addMockIntervention(String id, String reqId, String contractId, String providerId,
                                     String progressId, String providerName, String currentNode,
                                     String initiatorRole, String initiatorName, String issueType,
                                     String complaint, String demand, InterventionStatus status,
                                     String profession, String city, BigDecimal budget) {
        Intervention iv = new Intervention();
        iv.setId(id);
        iv.setRequirementId(reqId);
        iv.setContractId(contractId);
        iv.setProviderId(providerId);
        iv.setProviderName(providerName);
        iv.setProgressId(progressId);
        iv.setCurrentNode(currentNode);
        iv.setInitiatorRole(initiatorRole);
        iv.setInitiatorName(initiatorName);
        iv.setIssueType(issueType);
        iv.setComplaintContent(complaint);
        iv.setDemandDescription(demand);
        iv.setStatus(status.name());
        iv.setProfessionType(profession);
        iv.setCity(city);
        iv.setContractBudget(budget);
        iv.setCreateTime(LocalDateTime.now().minusDays((int)(Math.random() * 10) + 1));
        iv.setUpdateTime(LocalDateTime.now().minusHours((int)(Math.random() * 5) + 1));

        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("action", "发起介入");
        r1.put("operator", initiatorName);
        r1.put("time", iv.getCreateTime());
        r1.put("note", complaint);
        records.add(r1);

        if (status == InterventionStatus.PROCESSING || status == InterventionStatus.SUPPLEMENT
            || status == InterventionStatus.RESOLVED || status == InterventionStatus.CLOSED) {
            Map<String, Object> r2 = new HashMap<>();
            r2.put("action", "受理介入");
            r2.put("operator", "平台客服-小王");
            r2.put("time", iv.getCreateTime().plusHours(2));
            r2.put("note", "已受理您的介入申请，正在核实相关情况");
            records.add(r2);
        }

        if (status == InterventionStatus.SUPPLEMENT) {
            Map<String, Object> r3 = new HashMap<>();
            r3.put("action", "要求补充材料");
            r3.put("operator", "平台客服-小王");
            r3.put("time", iv.getCreateTime().plusHours(5));
            r3.put("note", "请提供服务商爽约的聊天记录截图、合同约定截图等证明材料");
            records.add(r3);
            iv.setSupplementNote("请提供服务商爽约的聊天记录截图、合同约定截图等证明材料");
        }

        if (status == InterventionStatus.RESOLVED || status == InterventionStatus.CLOSED) {
            Map<String, Object> r3 = new HashMap<>();
            r3.put("action", "给出处理方案");
            r3.put("operator", "平台客服-小李");
            r3.put("time", iv.getCreateTime().plusDays(1));
            r3.put("note", "经核实，服务商确实存在服务内容不符情况，已协调服务商退还部分费用2000元。");
            records.add(r3);
            iv.setProcessingResult("经核实，服务商确实存在服务内容不符情况，已协调服务商退还部分费用2000元。");
            iv.setHandler("平台客服-小李");
        }

        if (status == InterventionStatus.CLOSED) {
            Map<String, Object> r4 = new HashMap<>();
            r4.put("action", "关闭介入单");
            r4.put("operator", "平台客服-小李");
            r4.put("time", iv.getCreateTime().plusDays(2));
            r4.put("note", "双方已达成一致，介入单关闭");
            records.add(r4);
        }

        iv.setProcessingRecords(records);
        interventionMap.put(id, iv);
    }

    public Intervention getById(String id) {
        Intervention iv = interventionMap.get(id);
        if (iv == null) {
            throw new BusinessException("介入单不存在");
        }
        return iv;
    }

    public List<Intervention> list(String status, String professionType, String city, String issueType) {
        return interventionMap.values().stream()
            .filter(iv -> status == null || status.isEmpty() || status.equals(iv.getStatus()))
            .filter(iv -> professionType == null || professionType.isEmpty() || professionType.equals(iv.getProfessionType()))
            .filter(iv -> city == null || city.isEmpty() || city.equals(iv.getCity()))
            .filter(iv -> issueType == null || issueType.isEmpty() || issueType.equals(iv.getIssueType()))
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        long total = interventionMap.size();
        long pending = interventionMap.values().stream().filter(i -> "PENDING".equals(i.getStatus())).count();
        long processing = interventionMap.values().stream().filter(i -> "PROCESSING".equals(i.getStatus())).count();
        long supplement = interventionMap.values().stream().filter(i -> "SUPPLEMENT".equals(i.getStatus())).count();
        long resolved = interventionMap.values().stream().filter(i -> "RESOLVED".equals(i.getStatus())).count();
        long closed = interventionMap.values().stream().filter(i -> "CLOSED".equals(i.getStatus())).count();
        stats.put("total", total);
        stats.put("pending", pending);
        stats.put("processing", processing);
        stats.put("supplement", supplement);
        stats.put("resolved", resolved);
        stats.put("closed", closed);
        return stats;
    }

    public Intervention createIntervention(InterventionCreateDTO dto) {
        if (dto.getRequirementId() == null || dto.getRequirementId().isEmpty()) {
            throw new BusinessException("请选择关联需求");
        }
        if (dto.getIssueType() == null || dto.getIssueType().isEmpty()) {
            throw new BusinessException("请选择问题类型");
        }
        if (dto.getComplaintContent() == null || dto.getComplaintContent().trim().isEmpty()) {
            throw new BusinessException("请填写问题描述");
        }

        Requirement req = requirementService.getById(dto.getRequirementId());
        if (req == null) {
            throw new BusinessException("需求不存在");
        }

        Contract signedContract = null;
        if (req.getStatus() != null && "SIGNED".equals(req.getStatus())) {
            List<Contract> contracts = contractService.getByRequirementId(dto.getRequirementId());
            signedContract = contracts.stream()
                .filter(c -> "SIGNED".equals(c.getStatus()))
                .findFirst()
                .orElse(null);
        }

        if (signedContract == null) {
            throw new BusinessException("该需求未签约或没有有效合同，无法发起平台介入");
        }

        String progressId = null;
        String currentNode = "待上门";
        try {
            ServiceProgress sp = progressService.getByContractId(signedContract.getId());
            if (sp != null) {
                progressId = sp.getId();
                currentNode = sp.getCurrentNode();
            }
        } catch (Exception e) {
            // ignore
        }

        ServiceProvider provider = null;
        try {
            provider = providerService.getById(signedContract.getProviderId());
        } catch (Exception e) {
            // ignore
        }

        String id = "IV" + String.format("%04d", idGenerator.getAndIncrement());
        Intervention iv = new Intervention();
        iv.setId(id);
        iv.setRequirementId(req.getId());
        iv.setRequirementTitle(req.getTitle());
        iv.setContractId(signedContract.getId());
        iv.setProviderId(signedContract.getProviderId());
        iv.setProviderName(provider != null ? provider.getName() + "（" + provider.getCity() + "-" + getProfessionLabel(provider.getProfessionType()) + "）" : signedContract.getProviderId());
        iv.setProgressId(progressId);
        iv.setCurrentNode(currentNode);
        iv.setInitiatorRole(dto.getInitiatorRole() != null ? dto.getInitiatorRole() : "DEMANDER");
        iv.setInitiatorName(dto.getInitiatorName() != null ? dto.getInitiatorName() : "需求方");
        iv.setIssueType(dto.getIssueType());
        iv.setComplaintContent(dto.getComplaintContent());
        iv.setDemandDescription(dto.getDemandDescription());
        iv.setMaterialUrls(dto.getMaterialUrls());
        iv.setStatus(InterventionStatus.PENDING.name());
        iv.setProfessionType(req.getProfessionType());
        iv.setCity(req.getCity());
        iv.setContractBudget(req.getBudget());
        iv.setCreateTime(LocalDateTime.now());
        iv.setUpdateTime(LocalDateTime.now());

        List<Map<String, Object>> records = new ArrayList<>();
        Map<String, Object> r1 = new HashMap<>();
        r1.put("action", "发起介入");
        r1.put("operator", iv.getInitiatorName());
        r1.put("time", LocalDateTime.now());
        r1.put("note", dto.getComplaintContent());
        records.add(r1);
        iv.setProcessingRecords(records);

        interventionMap.put(id, iv);
        return iv;
    }

    public Intervention acceptIntervention(String id, InterventionHandleDTO dto) {
        Intervention iv = getById(id);
        if (!"PENDING".equals(iv.getStatus())) {
            throw new BusinessException("只有待受理的介入单才能受理");
        }
        iv.setStatus(InterventionStatus.PROCESSING.name());
        iv.setHandler(dto.getHandler() != null ? dto.getHandler() : "平台客服");
        iv.setUpdateTime(LocalDateTime.now());

        addRecord(iv, "受理介入", iv.getHandler(),
            dto.getNote() != null ? dto.getNote() : "已受理您的介入申请，正在核实相关情况");
        return iv;
    }

    public Intervention requestSupplement(String id, InterventionHandleDTO dto) {
        Intervention iv = getById(id);
        if ("CLOSED".equals(iv.getStatus())) {
            throw new BusinessException("已关闭的介入单不能操作");
        }
        if (dto.getNote() == null || dto.getNote().trim().isEmpty()) {
            throw new BusinessException("请填写补充材料说明");
        }
        iv.setStatus(InterventionStatus.SUPPLEMENT.name());
        iv.setSupplementNote(dto.getNote());
        iv.setUpdateTime(LocalDateTime.now());

        addRecord(iv, "要求补充材料", dto.getHandler() != null ? dto.getHandler() : "平台客服", dto.getNote());
        return iv;
    }

    public Intervention resolveIntervention(String id, InterventionHandleDTO dto) {
        Intervention iv = getById(id);
        if ("CLOSED".equals(iv.getStatus())) {
            throw new BusinessException("已关闭的介入单不能操作");
        }
        if (dto.getProcessingResult() == null || dto.getProcessingResult().trim().isEmpty()) {
            throw new BusinessException("请填写处理方案");
        }
        iv.setStatus(InterventionStatus.RESOLVED.name());
        iv.setProcessingResult(dto.getProcessingResult());
        iv.setHandler(dto.getHandler() != null ? dto.getHandler() : "平台客服");
        iv.setUpdateTime(LocalDateTime.now());

        addRecord(iv, "给出处理方案", iv.getHandler(), dto.getProcessingResult());
        return iv;
    }

    public Intervention closeIntervention(String id, InterventionHandleDTO dto) {
        Intervention iv = getById(id);
        if ("CLOSED".equals(iv.getStatus())) {
            throw new BusinessException("介入单已关闭，不能重复关闭");
        }
        iv.setStatus(InterventionStatus.CLOSED.name());
        iv.setUpdateTime(LocalDateTime.now());

        addRecord(iv, "关闭介入单", dto.getHandler() != null ? dto.getHandler() : "平台客服",
            dto.getNote() != null ? dto.getNote() : "介入单已关闭");
        return iv;
    }

    public Map<String, Object> getInterventionDetail(String id) {
        Intervention iv = getById(id);
        Map<String, Object> result = new HashMap<>();
        result.put("intervention", iv);

        try {
            Requirement req = requirementService.getById(iv.getRequirementId());
            if (req != null) {
                Map<String, Object> reqInfo = new HashMap<>();
                reqInfo.put("id", req.getId());
                reqInfo.put("title", req.getTitle());
                reqInfo.put("description", req.getDescription());
                reqInfo.put("professionType", req.getProfessionType());
                reqInfo.put("city", req.getCity());
                reqInfo.put("budget", req.getBudget());
                reqInfo.put("status", req.getStatus());
                result.put("requirement", reqInfo);
            }
        } catch (Exception e) {
            // ignore
        }

        if (iv.getContractId() != null) {
            try {
                Contract contract = contractService.getById(iv.getContractId());
                if (contract != null) {
                    Map<String, Object> contractInfo = new HashMap<>();
                    contractInfo.put("id", contract.getId());
                    contractInfo.put("status", contract.getStatus());
                    contractInfo.put("budget", contract.getBudget());
                    contractInfo.put("remark", contract.getRemark());
                    result.put("contract", contractInfo);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        if (iv.getProgressId() != null) {
            try {
                ServiceProgress sp = progressService.getById(iv.getProgressId());
                if (sp != null) {
                    Map<String, Object> progressInfo = new HashMap<>();
                    progressInfo.put("id", sp.getId());
                    progressInfo.put("currentStatus", sp.getCurrentStatus());
                    progressInfo.put("currentNode", sp.getCurrentNode());
                    progressInfo.put("budget", sp.getBudget());
                    result.put("progress", progressInfo);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return result;
    }

    public List<Intervention> getByProgressId(String progressId) {
        return interventionMap.values().stream()
            .filter(iv -> progressId != null && progressId.equals(iv.getProgressId()))
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .collect(Collectors.toList());
    }

    public List<Intervention> getByRequirementId(String requirementId) {
        return interventionMap.values().stream()
            .filter(iv -> requirementId != null && requirementId.equals(iv.getRequirementId()))
            .sorted((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()))
            .collect(Collectors.toList());
    }

    private void addRecord(Intervention iv, String action, String operator, String note) {
        List<Map<String, Object>> records = iv.getProcessingRecords() != null
            ? new ArrayList<>(iv.getProcessingRecords()) : new ArrayList<>();
        Map<String, Object> record = new HashMap<>();
        record.put("action", action);
        record.put("operator", operator);
        record.put("time", LocalDateTime.now());
        record.put("note", note);
        records.add(record);
        iv.setProcessingRecords(records);
    }

    private String getProfessionLabel(String type) {
        if (type == null) return "";
        switch (type) {
            case "housekeeper": return "保姆";
            case "repair": return "维修";
            case "tutor": return "家教";
            case "driver": return "司机";
            case "cleaner": return "保洁";
            default: return type;
        }
    }
}
