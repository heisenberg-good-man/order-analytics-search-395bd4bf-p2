package com.platform.service;

import com.platform.dto.ProviderCreateDTO;
import com.platform.dto.ProviderUpdateDTO;
import com.platform.dto.VerifyAuditDTO;
import com.platform.dto.VerifySubmitDTO;
import com.platform.enums.VerifyStatus;
import com.platform.model.ServiceProvider;
import com.platform.model.VerifyRecord;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class ProviderService {

    private final Map<String, ServiceProvider> providerMap = new ConcurrentHashMap<>();
    private final List<VerifyRecord> verifyRecords = Collections.synchronizedList(new ArrayList<>());
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @PostConstruct
    public void initMockData() {
        String[] names = {"张三", "李四", "王五", "赵六", "钱七", "孙八", "周九", "吴十"};
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "南京"};
        String[] professions = {"BABYSITTER", "REPAIRMAN", "MATERNAL_NURSE", "CLEANER", "COOK", "DRIVER", "NURSING_WORKER", "TUTOR"};
        String[][] skillPool = {
            {"做饭", "带孩子", "打扫卫生"},
            {"水电维修", "家电维修", "管道疏通"},
            {"新生儿护理", "产妇护理", "月子餐"},
            {"日常保洁", "深度保洁", "开荒保洁"},
            {"家常菜", "川菜", "粤菜"},
            {"C1驾照", "长途驾驶", "商务接待"},
            {"老人护理", "病人护理", "康复训练"},
            {"语文辅导", "数学辅导", "英语辅导"}
        };
        VerifyStatus[] statuses = {
            VerifyStatus.NOT_SUBMITTED,
            VerifyStatus.PENDING,
            VerifyStatus.APPROVED,
            VerifyStatus.REJECTED,
            VerifyStatus.PENDING,
            VerifyStatus.APPROVED,
            VerifyStatus.NOT_SUBMITTED,
            VerifyStatus.REJECTED
        };

        for (int i = 0; i < 8; i++) {
            ServiceProvider p = new ServiceProvider();
            String id = "SP" + String.format("%04d", idGenerator.getAndIncrement());
            p.setId(id);
            p.setName(names[i]);
            p.setPhone("138" + String.format("%08d", 10000000 + i * 123));
            p.setProfessionType(professions[i]);
            p.setCity(cities[i]);
            p.setSkillTags(Arrays.asList(skillPool[i]));
            p.setVerifyStatus(statuses[i]);
            p.setCompleteness(calcCompleteness(p));
            p.setCreateTime(LocalDateTime.now().minusDays(i + 1));
            p.setUpdateTime(LocalDateTime.now().minusHours(i * 2));

            if (statuses[i] == VerifyStatus.PENDING || statuses[i] == VerifyStatus.APPROVED || statuses[i] == VerifyStatus.REJECTED) {
                p.setIdCardName(names[i]);
                p.setIdCardNo("110101199" + (i % 10) + "0101" + String.format("%04d", i * 100 + 1234));
                p.setIdCardAddress(cities[i] + "市朝阳区某某路" + (i + 1) + "号");
                p.setSubmittedMaterials(Arrays.asList("身份证正面", "身份证反面"));
                p.setCompleteness(100);
            }

            if (statuses[i] == VerifyStatus.REJECTED) {
                p.setRejectReason("身份证照片模糊，请重新上传");
            }

            providerMap.put(id, p);

            if (statuses[i] == VerifyStatus.APPROVED) {
                VerifyRecord r1 = new VerifyRecord();
                r1.setId("VR" + System.currentTimeMillis() + "_" + i + "_1");
                r1.setProviderId(id);
                r1.setAction(VerifyStatus.PENDING);
                r1.setOperator("系统");
                r1.setRemark("提交实名认证");
                r1.setCreateTime(LocalDateTime.now().minusDays(i + 1).plusHours(1));
                verifyRecords.add(r1);

                VerifyRecord r2 = new VerifyRecord();
                r2.setId("VR" + System.currentTimeMillis() + "_" + i + "_2");
                r2.setProviderId(id);
                r2.setAction(VerifyStatus.APPROVED);
                r2.setOperator("管理员");
                r2.setRemark("审核通过");
                r2.setCreateTime(LocalDateTime.now().minusDays(i).plusHours(2));
                verifyRecords.add(r2);
            } else if (statuses[i] == VerifyStatus.REJECTED) {
                VerifyRecord r1 = new VerifyRecord();
                r1.setId("VR" + System.currentTimeMillis() + "_" + i + "_1");
                r1.setProviderId(id);
                r1.setAction(VerifyStatus.PENDING);
                r1.setOperator("系统");
                r1.setRemark("提交实名认证");
                r1.setCreateTime(LocalDateTime.now().minusDays(i + 1).plusHours(1));
                verifyRecords.add(r1);

                VerifyRecord r2 = new VerifyRecord();
                r2.setId("VR" + System.currentTimeMillis() + "_" + i + "_2");
                r2.setProviderId(id);
                r2.setAction(VerifyStatus.REJECTED);
                r2.setOperator("管理员");
                r2.setRemark("身份证照片模糊，请重新上传");
                r2.setCreateTime(LocalDateTime.now().minusDays(i).plusHours(2));
                verifyRecords.add(r2);
            } else if (statuses[i] == VerifyStatus.PENDING) {
                VerifyRecord r1 = new VerifyRecord();
                r1.setId("VR" + System.currentTimeMillis() + "_" + i + "_1");
                r1.setProviderId(id);
                r1.setAction(VerifyStatus.PENDING);
                r1.setOperator("系统");
                r1.setRemark("提交实名认证");
                r1.setCreateTime(LocalDateTime.now().minusHours(i * 2 + 1));
                verifyRecords.add(r1);
            }
        }
    }

    private int calcCompleteness(ServiceProvider p) {
        int score = 0;
        int total = 6;
        if (p.getName() != null && !p.getName().isEmpty()) score++;
        if (p.getPhone() != null && !p.getPhone().isEmpty()) score++;
        if (p.getProfessionType() != null && !p.getProfessionType().isEmpty()) score++;
        if (p.getCity() != null && !p.getCity().isEmpty()) score++;
        if (p.getSkillTags() != null && !p.getSkillTags().isEmpty()) score++;
        if (p.getIdCardNo() != null && !p.getIdCardNo().isEmpty()) score++;
        return (int) Math.round(score * 100.0 / total);
    }

    public List<ServiceProvider> list(String keyword, String verifyStatus, String professionType) {
        return providerMap.values().stream()
            .filter(p -> {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    boolean match = p.getName().toLowerCase().contains(kw)
                        || p.getPhone().contains(kw)
                        || p.getCity().toLowerCase().contains(kw);
                    if (p.getSkillTags() != null) {
                        match = match || p.getSkillTags().stream().anyMatch(s -> s.toLowerCase().contains(kw));
                    }
                    if (!match) return false;
                }
                if (verifyStatus != null && !verifyStatus.trim().isEmpty()) {
                    if (!p.getVerifyStatus().name().equals(verifyStatus)) return false;
                }
                if (professionType != null && !professionType.trim().isEmpty()) {
                    if (!p.getProfessionType().equals(professionType)) return false;
                }
                return true;
            })
            .sorted(Comparator.comparing(ServiceProvider::getUpdateTime).reversed())
            .collect(Collectors.toList());
    }

    public ServiceProvider getById(String id) {
        return providerMap.get(id);
    }

    public ServiceProvider create(ProviderCreateDTO dto) {
        String id = "SP" + String.format("%04d", idGenerator.getAndIncrement());
        ServiceProvider p = new ServiceProvider();
        p.setId(id);
        p.setName(dto.getName());
        p.setPhone(dto.getPhone());
        p.setProfessionType(dto.getProfessionType());
        p.setCity(dto.getCity());
        p.setSkillTags(dto.getSkillTags());
        p.setVerifyStatus(VerifyStatus.NOT_SUBMITTED);
        p.setCreateTime(LocalDateTime.now());
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));
        providerMap.put(id, p);
        return p;
    }

    public ServiceProvider update(String id, ProviderUpdateDTO dto) {
        ServiceProvider p = providerMap.get(id);
        if (p == null) return null;
        if (dto.getName() != null) p.setName(dto.getName());
        if (dto.getPhone() != null) p.setPhone(dto.getPhone());
        if (dto.getProfessionType() != null) p.setProfessionType(dto.getProfessionType());
        if (dto.getCity() != null) p.setCity(dto.getCity());
        if (dto.getSkillTags() != null) p.setSkillTags(dto.getSkillTags());
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));
        return p;
    }

    public boolean delete(String id) {
        return providerMap.remove(id) != null;
    }

    public ServiceProvider submitVerify(String id, VerifySubmitDTO dto) {
        ServiceProvider p = providerMap.get(id);
        if (p == null) return null;
        if (p.getVerifyStatus() == VerifyStatus.PENDING) {
            throw new IllegalStateException("当前处于待审核状态，不能重复提交");
        }
        if (p.getVerifyStatus() == VerifyStatus.APPROVED) {
            throw new IllegalStateException("已认证通过，无需重复提交");
        }
        p.setIdCardName(dto.getIdCardName());
        p.setIdCardNo(dto.getIdCardNo());
        p.setIdCardAddress(dto.getIdCardAddress());
        p.setSubmittedMaterials(dto.getSubmittedMaterials());
        p.setVerifyStatus(VerifyStatus.PENDING);
        p.setRejectReason(null);
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));

        VerifyRecord r = new VerifyRecord();
        r.setId("VR" + System.currentTimeMillis());
        r.setProviderId(id);
        r.setAction(VerifyStatus.PENDING);
        r.setOperator("服务商");
        r.setRemark("提交实名认证资料");
        r.setCreateTime(LocalDateTime.now());
        verifyRecords.add(r);

        return p;
    }

    public ServiceProvider approve(String id, VerifyAuditDTO dto) {
        ServiceProvider p = providerMap.get(id);
        if (p == null) return null;
        if (p.getVerifyStatus() != VerifyStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态才能通过审核");
        }
        p.setVerifyStatus(VerifyStatus.APPROVED);
        p.setRejectReason(null);
        p.setUpdateTime(LocalDateTime.now());

        VerifyRecord r = new VerifyRecord();
        r.setId("VR" + System.currentTimeMillis());
        r.setProviderId(id);
        r.setAction(VerifyStatus.APPROVED);
        r.setOperator(dto.getOperator() != null ? dto.getOperator() : "管理员");
        r.setRemark(dto.getRemark() != null ? dto.getRemark() : "审核通过");
        r.setCreateTime(LocalDateTime.now());
        verifyRecords.add(r);

        return p;
    }

    public ServiceProvider reject(String id, VerifyAuditDTO dto) {
        ServiceProvider p = providerMap.get(id);
        if (p == null) return null;
        if (p.getVerifyStatus() != VerifyStatus.PENDING) {
            throw new IllegalStateException("只有待审核状态才能驳回");
        }
        p.setVerifyStatus(VerifyStatus.REJECTED);
        p.setRejectReason(dto.getRemark());
        p.setUpdateTime(LocalDateTime.now());

        VerifyRecord r = new VerifyRecord();
        r.setId("VR" + System.currentTimeMillis());
        r.setProviderId(id);
        r.setAction(VerifyStatus.REJECTED);
        r.setOperator(dto.getOperator() != null ? dto.getOperator() : "管理员");
        r.setRemark(dto.getRemark());
        r.setCreateTime(LocalDateTime.now());
        verifyRecords.add(r);

        return p;
    }

    public List<VerifyRecord> getVerifyRecords(String providerId) {
        return verifyRecords.stream()
            .filter(r -> r.getProviderId().equals(providerId))
            .sorted(Comparator.comparing(VerifyRecord::getCreateTime).reversed())
            .collect(Collectors.toList());
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Collection<ServiceProvider> all = providerMap.values();
        stats.put("total", all.size());
        stats.put("pending", (int) all.stream().filter(p -> p.getVerifyStatus() == VerifyStatus.PENDING).count());
        stats.put("approved", (int) all.stream().filter(p -> p.getVerifyStatus() == VerifyStatus.APPROVED).count());
        stats.put("rejected", (int) all.stream().filter(p -> p.getVerifyStatus() == VerifyStatus.REJECTED).count());
        stats.put("notSubmitted", (int) all.stream().filter(p -> p.getVerifyStatus() == VerifyStatus.NOT_SUBMITTED).count());

        Map<String, Long> professionStats = all.stream()
            .collect(Collectors.groupingBy(ServiceProvider::getProfessionType, Collectors.counting()));
        stats.put("professionStats", professionStats);

        return stats;
    }
}
