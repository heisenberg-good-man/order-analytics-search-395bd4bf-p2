package com.platform.service;

import com.platform.common.BusinessException;
import com.platform.dto.ProgressAdvanceDTO;
import com.platform.enums.ProgressStatus;
import com.platform.model.Contract;
import com.platform.model.ProgressNode;
import com.platform.model.ServiceProgress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ServiceProgressService {

    @Autowired
    private ContractService contractService;

    private final Map<String, ServiceProgress> progressMap = new ConcurrentHashMap<>();
    private final Map<String, ProgressNode> nodeMap = new ConcurrentHashMap<>();
    private final AtomicInteger progressIdGenerator = new AtomicInteger(1);
    private final AtomicInteger nodeIdGenerator = new AtomicInteger(1);

    private static final List<String> NODE_ORDER = Arrays.asList(
        ProgressStatus.PENDING_VISIT.name(),
        ProgressStatus.IN_SERVICE.name(),
        ProgressStatus.PENDING_ACCEPTANCE.name(),
        ProgressStatus.COMPLETED.name()
    );

    @PostConstruct
    public void initMockData() {
        createProgressForSignedContract("CT0001", ProgressStatus.IN_SERVICE.name());
    }

    private void createProgressForSignedContract(String contractId, String currentStatus) {
        Contract c = contractService.getById(contractId);
        if (c == null || !"SIGNED".equals(c.getStatus())) {
            return;
        }

        String progressId = "SP" + String.format("%04d", progressIdGenerator.getAndIncrement());
        ServiceProgress sp = new ServiceProgress();
        sp.setId(progressId);
        sp.setContractId(contractId);
        sp.setRequirementId(c.getRequirementId());
        sp.setRequirementTitle(c.getRequirementTitle());
        sp.setProviderId(c.getProviderId());
        sp.setProviderName(c.getProviderName());
        sp.setProfessionType(c.getProfessionType());
        sp.setCity(c.getCity());
        sp.setBudget(c.getBudget());
        sp.setCurrentStatus(currentStatus);
        sp.setCurrentNode(getNodeDesc(currentStatus));
        sp.setCreateTime(LocalDateTime.now().minusDays(1));
        sp.setUpdateTime(LocalDateTime.now());

        List<ProgressNode> nodes = new ArrayList<>();
        int currentIdx = NODE_ORDER.indexOf(currentStatus);

        for (int i = 0; i < NODE_ORDER.size(); i++) {
            String nodeStatus = NODE_ORDER.get(i);
            ProgressNode node = new ProgressNode();
            node.setId("PN" + String.format("%04d", nodeIdGenerator.getAndIncrement()));
            node.setProgressId(progressId);
            node.setNodeName(getNodeDesc(nodeStatus));
            node.setStatus(nodeStatus);
            node.setSortOrder(i + 1);

            if (i < currentIdx) {
                node.setActualTime(LocalDateTime.now().minusDays(1).plusHours(i * 2));
                node.setDescription(generateNodeDesc(nodeStatus));
                node.setRemark("正常推进");
                node.setOperator("系统");
            } else if (i == currentIdx) {
                node.setEstimatedTime(LocalDateTime.now().plusDays(1));
                node.setDescription(generateNodeDesc(nodeStatus));
                node.setRemark("进行中");
                node.setOperator("系统");
            }

            node.setCreateTime(LocalDateTime.now().minusDays(1));
            node.setUpdateTime(LocalDateTime.now());
            nodes.add(node);
            nodeMap.put(node.getId(), node);
        }

        sp.setNodes(nodes);
        progressMap.put(progressId, sp);
    }

    private String generateNodeDesc(String status) {
        switch (status) {
            case "PENDING_VISIT": return "服务商已确认，按约定时间上门服务";
            case "IN_SERVICE": return "服务进行中，按质按量完成约定内容";
            case "PENDING_ACCEPTANCE": return "服务完成，等待需求方验收确认";
            case "COMPLETED": return "需求方验收通过，服务圆满完成";
            default: return "";
        }
    }

    private String getNodeDesc(String status) {
        try {
            return ProgressStatus.valueOf(status).getDesc();
        } catch (Exception e) {
            return status;
        }
    }

    public ServiceProgress getByContractId(String contractId) {
        return progressMap.values().stream()
            .filter(p -> p.getContractId().equals(contractId))
            .findFirst()
            .orElse(null);
    }

    public ServiceProgress getByRequirementId(String requirementId) {
        return progressMap.values().stream()
            .filter(p -> p.getRequirementId().equals(requirementId))
            .findFirst()
            .orElse(null);
    }

    public ServiceProgress getById(String id) {
        ServiceProgress sp = progressMap.get(id);
        if (sp == null) {
            throw new BusinessException("服务进度不存在");
        }
        return sp;
    }

    public List<ServiceProgress> list(String status, String professionType, String city) {
        return progressMap.values().stream()
            .filter(p -> status == null || status.isEmpty() || p.getCurrentStatus().equals(status))
            .filter(p -> professionType == null || professionType.isEmpty() || p.getProfessionType().equals(professionType))
            .filter(p -> city == null || city.isEmpty() || p.getCity().equals(city))
            .sorted(Comparator.comparing(ServiceProgress::getUpdateTime).reversed())
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Collection<ServiceProgress> all = progressMap.values();
        stats.put("total", all.size());
        stats.put("inService", (int) all.stream().filter(p -> "IN_SERVICE".equals(p.getCurrentStatus())).count());
        stats.put("pendingAcceptance", (int) all.stream().filter(p -> "PENDING_ACCEPTANCE".equals(p.getCurrentStatus())).count());
        stats.put("completed", (int) all.stream().filter(p -> "COMPLETED".equals(p.getCurrentStatus())).count());
        stats.put("abnormalPaused", (int) all.stream().filter(p -> "ABNORMAL_PAUSED".equals(p.getCurrentStatus())).count());
        stats.put("pendingVisit", (int) all.stream().filter(p -> "PENDING_VISIT".equals(p.getCurrentStatus())).count());

        Map<String, Long> professionStats = all.stream()
            .collect(Collectors.groupingBy(ServiceProgress::getProfessionType, Collectors.counting()));
        stats.put("professionStats", professionStats);

        Map<String, Long> cityStats = all.stream()
            .collect(Collectors.groupingBy(ServiceProgress::getCity, Collectors.counting()));
        stats.put("cityStats", cityStats);

        return stats;
    }

    public ServiceProgress createProgress(String contractId) {
        Contract c = contractService.getById(contractId);
        if (c == null) {
            throw new BusinessException("合同不存在");
        }
        if (!"SIGNED".equals(c.getStatus())) {
            throw new BusinessException("只有已签约的合同才能开启服务进度");
        }

        ServiceProgress existing = getByContractId(contractId);
        if (existing != null) {
            throw new BusinessException("该合同已有服务进度，不能重复创建");
        }

        String progressId = "SP" + String.format("%04d", progressIdGenerator.getAndIncrement());
        ServiceProgress sp = new ServiceProgress();
        sp.setId(progressId);
        sp.setContractId(contractId);
        sp.setRequirementId(c.getRequirementId());
        sp.setRequirementTitle(c.getRequirementTitle());
        sp.setProviderId(c.getProviderId());
        sp.setProviderName(c.getProviderName());
        sp.setProfessionType(c.getProfessionType());
        sp.setCity(c.getCity());
        sp.setBudget(c.getBudget());
        sp.setCurrentStatus(ProgressStatus.PENDING_VISIT.name());
        sp.setCurrentNode(ProgressStatus.PENDING_VISIT.getDesc());
        sp.setCreateTime(LocalDateTime.now());
        sp.setUpdateTime(LocalDateTime.now());

        List<ProgressNode> nodes = new ArrayList<>();
        for (int i = 0; i < NODE_ORDER.size(); i++) {
            String nodeStatus = NODE_ORDER.get(i);
            ProgressNode node = new ProgressNode();
            node.setId("PN" + String.format("%04d", nodeIdGenerator.getAndIncrement()));
            node.setProgressId(progressId);
            node.setNodeName(getNodeDesc(nodeStatus));
            node.setStatus(nodeStatus);
            node.setSortOrder(i + 1);
            node.setCreateTime(LocalDateTime.now());
            node.setUpdateTime(LocalDateTime.now());

            if (i == 0) {
                node.setDescription("合同已签约，等待服务商上门");
                node.setEstimatedTime(LocalDateTime.now().plusDays(1));
                node.setRemark("待上门");
                node.setOperator("系统");
            }

            nodes.add(node);
            nodeMap.put(node.getId(), node);
        }

        sp.setNodes(nodes);
        progressMap.put(progressId, sp);
        return sp;
    }

    public ServiceProgress advanceNode(String progressId, ProgressAdvanceDTO dto) {
        ServiceProgress sp = getById(progressId);
        String currentStatus = sp.getCurrentStatus();

        if ("ABNORMAL_PAUSED".equals(currentStatus)) {
            throw new BusinessException("当前为异常暂停状态，请先恢复服务后再推进");
        }
        if ("COMPLETED".equals(currentStatus)) {
            throw new BusinessException("服务已完成，不能继续推进");
        }

        int currentIdx = NODE_ORDER.indexOf(currentStatus);
        if (currentIdx < 0) {
            throw new BusinessException("当前节点状态异常");
        }

        ProgressNode currentNode = sp.getNodes().stream()
            .filter(n -> n.getStatus().equals(currentStatus))
            .findFirst().orElse(null);
        if (currentNode != null) {
            currentNode.setActualTime(LocalDateTime.now());
            if (dto.getRemark() != null && !dto.getRemark().isEmpty()) {
                currentNode.setRemark(dto.getRemark());
            }
            currentNode.setOperator(dto.getOperator() != null ? dto.getOperator() : "操作人");
            currentNode.setUpdateTime(LocalDateTime.now());
        }

        if (currentIdx < NODE_ORDER.size() - 1) {
            String nextStatus = NODE_ORDER.get(currentIdx + 1);
            sp.setCurrentStatus(nextStatus);
            sp.setCurrentNode(getNodeDesc(nextStatus));

            ProgressNode nextNode = sp.getNodes().stream()
                .filter(n -> n.getStatus().equals(nextStatus))
                .findFirst().orElse(null);
            if (nextNode != null) {
                nextNode.setDescription(generateNodeDesc(nextStatus));
                nextNode.setOperator(dto.getOperator() != null ? dto.getOperator() : "操作人");
                if (dto.getEstimatedTime() != null && !dto.getEstimatedTime().isEmpty()) {
                    try {
                        nextNode.setEstimatedTime(LocalDateTime.parse(dto.getEstimatedTime(), DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                    } catch (Exception e) {
                        nextNode.setEstimatedTime(LocalDateTime.now().plusDays(1));
                    }
                } else {
                    nextNode.setEstimatedTime(LocalDateTime.now().plusDays(1));
                }
                nextNode.setRemark("进行中");
                nextNode.setUpdateTime(LocalDateTime.now());
            }
        } else {
            sp.setCurrentStatus("COMPLETED");
            sp.setCurrentNode(ProgressStatus.COMPLETED.getDesc());
        }

        sp.setUpdateTime(LocalDateTime.now());
        return sp;
    }

    public ServiceProgress pauseProgress(String progressId, String reason, String operator) {
        ServiceProgress sp = getById(progressId);

        if ("COMPLETED".equals(sp.getCurrentStatus())) {
            throw new BusinessException("已完成的服务不能暂停");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BusinessException("请填写异常暂停原因");
        }

        sp.setCurrentStatus("ABNORMAL_PAUSED");
        sp.setCurrentNode("异常暂停");
        sp.setUpdateTime(LocalDateTime.now());

        ProgressNode pauseNode = new ProgressNode();
        pauseNode.setId("PN" + String.format("%04d", nodeIdGenerator.getAndIncrement()));
        pauseNode.setProgressId(progressId);
        pauseNode.setNodeName("异常暂停");
        pauseNode.setStatus("ABNORMAL_PAUSED");
        pauseNode.setDescription(reason);
        pauseNode.setActualTime(LocalDateTime.now());
        pauseNode.setOperator(operator != null ? operator : "操作人");
        pauseNode.setRemark(reason);
        pauseNode.setSortOrder(99);
        pauseNode.setCreateTime(LocalDateTime.now());
        pauseNode.setUpdateTime(LocalDateTime.now());
        nodeMap.put(pauseNode.getId(), pauseNode);

        List<ProgressNode> nodes = new ArrayList<>(sp.getNodes());
        nodes.add(pauseNode);
        sp.setNodes(nodes);

        return sp;
    }

    public ServiceProgress resumeProgress(String progressId, String operator) {
        ServiceProgress sp = getById(progressId);

        if (!"ABNORMAL_PAUSED".equals(sp.getCurrentStatus())) {
            throw new BusinessException("只有异常暂停状态才能恢复服务");
        }

        String resumeStatus = ProgressStatus.PENDING_VISIT.name();
        for (int i = NODE_ORDER.size() - 1; i >= 0; i--) {
            String st = NODE_ORDER.get(i);
            ProgressNode node = sp.getNodes().stream()
                .filter(n -> n.getStatus().equals(st) && n.getActualTime() != null)
                .findFirst().orElse(null);
            if (node != null) {
                if (i < NODE_ORDER.size() - 1) {
                    resumeStatus = NODE_ORDER.get(i + 1);
                } else {
                    resumeStatus = "COMPLETED";
                }
                break;
            }
        }

        sp.setCurrentStatus(resumeStatus);
        sp.setCurrentNode(getNodeDesc(resumeStatus));
        sp.setUpdateTime(LocalDateTime.now());

        return sp;
    }
}
