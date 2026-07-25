package com.platform.service;

import com.platform.common.BusinessException;
import com.platform.dto.ProviderCreateDTO;
import com.platform.dto.ProviderUpdateDTO;
import com.platform.dto.VerifyAuditDTO;
import com.platform.dto.VerifySubmitDTO;
import com.platform.enums.ProfessionType;
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

    private static final List<String> REQUIRED_MATERIALS = Arrays.asList("身份证正面", "身份证反面");

    @PostConstruct
    public void initMockData() {
        String[] names = {"李桂芳", "王师傅", "张月嫂", "赵阿姨", "钱大爷", "孙女士", "周师傅", "吴老师"};
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "南京"};
        String[] professions = {"BABYSITTER", "REPAIRMAN", "MATERNAL_NURSE", "CLEANER", "COOK", "DRIVER", "NURSING_WORKER", "TUTOR"};
        String[][] skillPool = {
            {"做饭", "带孩子", "打扫卫生", "辅食制作"},
            {"水电维修", "家电维修", "管道疏通"},
            {"新生儿护理", "产妇护理", "月子餐", "催乳"},
            {"日常保洁", "深度保洁", "开荒保洁"},
            {"家常菜", "川菜", "粤菜"},
            {"C1驾照", "长途驾驶", "商务接待"},
            {"老人护理", "病人护理", "康复训练"},
            {"语文辅导", "数学辅导", "英语辅导"}
        };
        VerifyStatus[] statuses = {
            VerifyStatus.PENDING,
            VerifyStatus.APPROVED,
            VerifyStatus.PENDING,
            VerifyStatus.REJECTED,
            VerifyStatus.NOT_SUBMITTED,
            VerifyStatus.APPROVED,
            VerifyStatus.NOT_SUBMITTED,
            VerifyStatus.PENDING
        };
        String[][] materialsByStatus = {
            {"身份证正面", "身份证反面", "健康证"},
            {"身份证正面", "身份证反面", "职业资格证", "健康证"},
            {"身份证正面", "身份证反面"},
            {"身份证正面", "身份证反面"},
            {},
            {"身份证正面", "身份证反面", "驾驶证", "从业资格证"},
            {},
            {"身份证正面", "身份证反面", "教师资格证"}
        };

        for (int i = 0; i < 8; i++) {
            ServiceProvider p = new ServiceProvider();
            String id = "SP" + String.format("%04d", idGenerator.getAndIncrement());
            p.setId(id);
            p.setName(names[i]);
            p.setPhone("138" + String.format("%08d", 10000000 + i * 137));
            p.setProfessionType(professions[i]);
            p.setCity(cities[i]);
            p.setSkillTags(Arrays.asList(skillPool[i]));
            p.setVerifyStatus(statuses[i]);
            p.setCreateTime(LocalDateTime.now().minusDays(i + 1));
            p.setUpdateTime(LocalDateTime.now().minusHours(i * 2 + 1));

            if (statuses[i] == VerifyStatus.PENDING || statuses[i] == VerifyStatus.APPROVED || statuses[i] == VerifyStatus.REJECTED) {
                p.setIdCardName(names[i]);
                p.setIdCardNo("110101199" + (i % 10) + "0" + String.format("%02d", i + 1) + "01" + String.format("%04d", i * 123 + 1234));
                p.setIdCardAddress(cities[i] + "市朝阳区幸福路" + (i + 1) + "号院" + (i + 2) + "号楼");
                p.setSubmittedMaterials(Arrays.asList(materialsByStatus[i]));
            }

            if (statuses[i] == VerifyStatus.REJECTED) {
                p.setRejectReason("材料不完整，请补充手持身份证照片和职业资格证书");
            }

            p.setCompleteness(calcCompleteness(p));
            providerMap.put(id, p);

            if (statuses[i] == VerifyStatus.APPROVED) {
                VerifyRecord r1 = new VerifyRecord();
                r1.setId("VR" + (1000 + i * 10 + 1));
                r1.setProviderId(id);
                r1.setAction(VerifyStatus.PENDING);
                r1.setOperator(names[i]);
                r1.setRemark("提交实名认证资料");
                r1.setCreateTime(LocalDateTime.now().minusDays(i + 1).plusHours(2));
                verifyRecords.add(r1);

                VerifyRecord r2 = new VerifyRecord();
                r2.setId("VR" + (1000 + i * 10 + 2));
                r2.setProviderId(id);
                r2.setAction(VerifyStatus.APPROVED);
                r2.setOperator("平台审核员-小王");
                r2.setRemark("资料齐全，审核通过");
                r2.setCreateTime(LocalDateTime.now().minusDays(i).plusHours(4));
                verifyRecords.add(r2);
            } else if (statuses[i] == VerifyStatus.REJECTED) {
                VerifyRecord r1 = new VerifyRecord();
                r1.setId("VR" + (1000 + i * 10 + 1));
                r1.setProviderId(id);
                r1.setAction(VerifyStatus.PENDING);
                r1.setOperator(names[i]);
                r1.setRemark("提交实名认证资料");
                r1.setCreateTime(LocalDateTime.now().minusDays(i + 1).plusHours(3));
                verifyRecords.add(r1);

                VerifyRecord r2 = new VerifyRecord();
                r2.setId("VR" + (1000 + i * 10 + 2));
                r2.setProviderId(id);
                r2.setAction(VerifyStatus.REJECTED);
                r2.setOperator("平台审核员-小李");
                r2.setRemark("材料不完整，请补充手持身份证照片和职业资格证书");
                r2.setCreateTime(LocalDateTime.now().minusDays(i).plusHours(5));
                verifyRecords.add(r2);
            } else if (statuses[i] == VerifyStatus.PENDING) {
                VerifyRecord r1 = new VerifyRecord();
                r1.setId("VR" + (1000 + i * 10 + 1));
                r1.setProviderId(id);
                r1.setAction(VerifyStatus.PENDING);
                r1.setOperator(names[i]);
                r1.setRemark("提交实名认证资料");
                r1.setCreateTime(LocalDateTime.now().minusHours(i * 3 + 2));
                verifyRecords.add(r1);
            }
        }

        ServiceProvider p9 = new ServiceProvider();
        p9.setId("SP0009");
        p9.setName("郑阿姨");
        p9.setPhone("13800000009");
        p9.setProfessionType("CLEANER");
        p9.setCity("天津");
        p9.setSkillTags(Arrays.asList("日常保洁", "深度保洁"));
        p9.setVerifyStatus(VerifyStatus.NOT_SUBMITTED);
        p9.setIdCardName("郑阿姨");
        p9.setIdCardNo("120101199001010009");
        p9.setIdCardAddress("天津市和平区南京路9号");
        p9.setSubmittedMaterials(Arrays.asList("身份证正面"));
        p9.setRejectReason(null);
        p9.setCreateTime(LocalDateTime.now().minusDays(9));
        p9.setUpdateTime(LocalDateTime.now().minusDays(8));
        p9.setCompleteness(calcCompleteness(p9));
        providerMap.put("SP0009", p9);

        ServiceProvider p10 = new ServiceProvider();
        p10.setId("SP0010");
        p10.setName("冯师傅");
        p10.setPhone("13800000010");
        p10.setProfessionType("REPAIRMAN");
        p10.setCity("重庆");
        p10.setSkillTags(Arrays.asList("水电维修", "家电维修"));
        p10.setVerifyStatus(VerifyStatus.REJECTED);
        p10.setIdCardName("冯师傅");
        p10.setIdCardNo("500101198501010010");
        p10.setIdCardAddress("重庆市渝中区解放碑10号");
        p10.setSubmittedMaterials(Arrays.asList("身份证正面", "身份证反面"));
        p10.setRejectReason("[退回补充] 请补充职业资格证书和健康证明，材料不齐全无法通过审核");
        p10.setCreateTime(LocalDateTime.now().minusDays(10));
        p10.setUpdateTime(LocalDateTime.now().minusDays(1).plusHours(3));
        p10.setCompleteness(calcCompleteness(p10));
        providerMap.put("SP0010", p10);

        VerifyRecord vr10_1 = new VerifyRecord();
        vr10_1.setId("VR1010_1");
        vr10_1.setProviderId("SP0010");
        vr10_1.setAction(VerifyStatus.PENDING);
        vr10_1.setOperator("冯师傅");
        vr10_1.setRemark("提交实名认证资料");
        vr10_1.setCreateTime(LocalDateTime.now().minusDays(2));
        verifyRecords.add(vr10_1);

        VerifyRecord vr10_2 = new VerifyRecord();
        vr10_2.setId("VR1010_2");
        vr10_2.setProviderId("SP0010");
        vr10_2.setAction(VerifyStatus.REJECTED);
        vr10_2.setOperator("平台审核员-小张");
        vr10_2.setRemark("[退回补充] 请补充职业资格证书和健康证明，材料不齐全无法通过审核");
        vr10_2.setCreateTime(LocalDateTime.now().minusDays(1).plusHours(3));
        verifyRecords.add(vr10_2);
    }

    public List<String> checkMissingFields(ServiceProvider p) {
        List<String> missing = new ArrayList<>();
        if (p.getName() == null || p.getName().trim().isEmpty()) missing.add("姓名");
        if (p.getPhone() == null || p.getPhone().trim().isEmpty()) missing.add("手机号");
        if (p.getProfessionType() == null || p.getProfessionType().trim().isEmpty()) missing.add("期望职业");
        if (p.getCity() == null || p.getCity().trim().isEmpty()) missing.add("服务城市");
        if (p.getIdCardName() == null || p.getIdCardName().trim().isEmpty()) missing.add("身份证姓名");
        if (p.getIdCardNo() == null || p.getIdCardNo().trim().isEmpty()) missing.add("身份证号");
        if (p.getIdCardAddress() == null || p.getIdCardAddress().trim().isEmpty()) missing.add("身份证地址");
        if (p.getSubmittedMaterials() == null || p.getSubmittedMaterials().isEmpty()) {
            missing.add("认证材料");
        } else {
            for (String req : REQUIRED_MATERIALS) {
                if (!p.getSubmittedMaterials().contains(req)) {
                    missing.add("材料-" + req);
                }
            }
        }
        return missing;
    }

    private int calcCompleteness(ServiceProvider p) {
        int score = 0;
        int total = 8;
        if (p.getName() != null && !p.getName().isEmpty()) score++;
        if (p.getPhone() != null && !p.getPhone().isEmpty()) score++;
        if (p.getProfessionType() != null && !p.getProfessionType().isEmpty()) score++;
        if (p.getCity() != null && !p.getCity().isEmpty()) score++;
        if (p.getSkillTags() != null && !p.getSkillTags().isEmpty()) score++;
        if (p.getIdCardNo() != null && !p.getIdCardNo().isEmpty()) score++;
        if (p.getIdCardAddress() != null && !p.getIdCardAddress().isEmpty()) score++;
        if (p.getSubmittedMaterials() != null && !p.getSubmittedMaterials().isEmpty()) score++;
        return (int) Math.round(score * 100.0 / total);
    }

    public List<ServiceProvider> list(String keyword, String verifyStatus, String professionType, String city) {
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
                    if (p.getProfessionType() != null) {
                        try {
                            ProfessionType pt = ProfessionType.valueOf(p.getProfessionType());
                            match = match || pt.getDesc().toLowerCase().contains(kw);
                        } catch (Exception ignored) {}
                    }
                    if (!match) return false;
                }
                if (verifyStatus != null && !verifyStatus.trim().isEmpty()) {
                    if (!p.getVerifyStatus().name().equals(verifyStatus)) return false;
                }
                if (professionType != null && !professionType.trim().isEmpty()) {
                    if (!p.getProfessionType().equals(professionType)) return false;
                }
                if (city != null && !city.trim().isEmpty()) {
                    if (!p.getCity().equals(city.trim())) return false;
                }
                return true;
            })
            .sorted(Comparator.comparing(ServiceProvider::getUpdateTime).reversed())
            .collect(Collectors.toList());
    }

    public ServiceProvider getById(String id) {
        return providerMap.get(id);
    }

    public ServiceProvider getByIdOrThrow(String id) {
        return getProviderOrThrow(id);
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
        ServiceProvider p = getProviderOrThrow(id);

        if (p.getVerifyStatus() == VerifyStatus.PENDING) {
            throw new BusinessException("待审核状态下不能修改资料，请等待审核结果或联系平台");
        }
        if (p.getVerifyStatus() == VerifyStatus.APPROVED) {
            if (dto.getProfessionType() != null || dto.getCity() != null) {
                throw new BusinessException("已认证通过，职业和城市不能随意修改，如需变更请联系平台");
            }
        }

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

    private ServiceProvider getProviderOrThrow(String id) {
        ServiceProvider p = providerMap.get(id);
        if (p == null) {
            throw new BusinessException(404, "服务商不存在");
        }
        return p;
    }

    private void assertPendingStatus(ServiceProvider p, String action) {
        if (p.getVerifyStatus() != VerifyStatus.PENDING) {
            throw new BusinessException("只有待审核状态才能" + action);
        }
    }

    private void addVerifyRecord(String providerId, VerifyStatus action, String operator, String remark) {
        VerifyRecord r = new VerifyRecord();
        r.setId("VR" + System.currentTimeMillis() + "_" + (int)(Math.random() * 1000));
        r.setProviderId(providerId);
        r.setAction(action);
        r.setOperator(operator != null && !operator.isEmpty() ? operator : "系统");
        r.setRemark(remark);
        r.setCreateTime(LocalDateTime.now());
        verifyRecords.add(r);
    }

    public ServiceProvider submitVerify(String id, VerifySubmitDTO dto) {
        ServiceProvider p = getProviderOrThrow(id);

        if (p.getVerifyStatus() == VerifyStatus.PENDING) {
            throw new BusinessException("当前处于待审核状态，不能重复提交");
        }
        if (p.getVerifyStatus() == VerifyStatus.APPROVED) {
            throw new BusinessException("已认证通过，无需重复提交");
        }

        if (dto.getIdCardName() == null || dto.getIdCardName().trim().isEmpty()) {
            throw new BusinessException("身份证姓名不能为空");
        }
        if (dto.getIdCardNo() == null || dto.getIdCardNo().trim().isEmpty()) {
            throw new BusinessException("身份证号不能为空");
        }
        if (dto.getIdCardAddress() == null || dto.getIdCardAddress().trim().isEmpty()) {
            throw new BusinessException("身份证地址不能为空");
        }
        if (dto.getSubmittedMaterials() == null || dto.getSubmittedMaterials().isEmpty()) {
            throw new BusinessException("请至少提交一项认证材料");
        }
        List<String> missingMaterials = new ArrayList<>();
        for (String req : REQUIRED_MATERIALS) {
            if (!dto.getSubmittedMaterials().contains(req)) {
                missingMaterials.add(req);
            }
        }
        if (!missingMaterials.isEmpty()) {
            throw new BusinessException("缺少必填材料：" + String.join("、", missingMaterials));
        }
        if (p.getProfessionType() == null || p.getProfessionType().trim().isEmpty()) {
            throw new BusinessException("请先完善期望职业信息");
        }
        if (p.getCity() == null || p.getCity().trim().isEmpty()) {
            throw new BusinessException("请先完善服务城市信息");
        }

        p.setIdCardName(dto.getIdCardName());
        p.setIdCardNo(dto.getIdCardNo());
        p.setIdCardAddress(dto.getIdCardAddress());
        p.setSubmittedMaterials(dto.getSubmittedMaterials());
        p.setVerifyStatus(VerifyStatus.PENDING);
        p.setRejectReason(null);
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));

        addVerifyRecord(id, VerifyStatus.PENDING, p.getName(), "提交实名认证资料");

        return p;
    }

    public ServiceProvider approve(String id, VerifyAuditDTO dto) {
        ServiceProvider p = getProviderOrThrow(id);
        assertPendingStatus(p, "通过审核");

        p.setVerifyStatus(VerifyStatus.APPROVED);
        p.setRejectReason(null);
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));

        String remark = (dto != null && dto.getRemark() != null && !dto.getRemark().isEmpty())
            ? dto.getRemark() : "资料齐全，审核通过";
        String operator = (dto != null && dto.getOperator() != null && !dto.getOperator().isEmpty())
            ? dto.getOperator() : "平台审核员";
        addVerifyRecord(id, VerifyStatus.APPROVED, operator, remark);

        return p;
    }

    public ServiceProvider reject(String id, VerifyAuditDTO dto) {
        ServiceProvider p = getProviderOrThrow(id);
        assertPendingStatus(p, "驳回");

        if (dto == null || dto.getRemark() == null || dto.getRemark().trim().isEmpty()) {
            throw new BusinessException("驳回原因不能为空");
        }

        p.setVerifyStatus(VerifyStatus.REJECTED);
        p.setRejectReason(dto.getRemark());
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));

        String operator = (dto.getOperator() != null && !dto.getOperator().isEmpty())
            ? dto.getOperator() : "平台审核员";
        addVerifyRecord(id, VerifyStatus.REJECTED, operator, dto.getRemark());

        return p;
    }

    public ServiceProvider sendBack(String id, VerifyAuditDTO dto) {
        ServiceProvider p = getProviderOrThrow(id);
        assertPendingStatus(p, "退回补充资料");

        if (dto == null || dto.getRemark() == null || dto.getRemark().trim().isEmpty()) {
            throw new BusinessException("退回原因不能为空");
        }

        p.setVerifyStatus(VerifyStatus.REJECTED);
        p.setRejectReason(dto.getRemark());
        p.setUpdateTime(LocalDateTime.now());
        p.setCompleteness(calcCompleteness(p));

        String operator = (dto.getOperator() != null && !dto.getOperator().isEmpty())
            ? dto.getOperator() : "平台审核员";
        addVerifyRecord(id, VerifyStatus.REJECTED, operator, "[退回补充] " + dto.getRemark());

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

        Map<String, Long> cityStats = all.stream()
            .collect(Collectors.groupingBy(ServiceProvider::getCity, Collectors.counting()));
        stats.put("cityStats", cityStats);

        return stats;
    }

    public List<String> getAllCities() {
        return providerMap.values().stream()
            .map(ServiceProvider::getCity)
            .filter(Objects::nonNull)
            .distinct()
            .sorted()
            .collect(Collectors.toList());
    }
}
