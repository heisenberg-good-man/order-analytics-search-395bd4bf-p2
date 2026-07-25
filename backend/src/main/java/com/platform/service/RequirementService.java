package com.platform.service;

import com.platform.common.BusinessException;
import com.platform.dto.RequirementCreateDTO;
import com.platform.enums.VerifyStatus;
import com.platform.model.Requirement;
import com.platform.model.ServiceProvider;
import com.platform.vo.RecommendResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class RequirementService {

    private final Map<String, Requirement> requirementMap = new ConcurrentHashMap<>();
    private final AtomicInteger idGenerator = new AtomicInteger(1);

    @Autowired
    private ProviderService providerService;

    @PostConstruct
    public void initMockData() {
        String[] titles = {
            "急需住家保姆照顾老人",
            "家电维修 - 空调不制冷",
            "月嫂服务 - 预产期8月",
            "日常保洁每周两次",
            "家庭厨师做三餐",
            "接送孩子上下学司机"
        };
        String[] professions = {"BABYSITTER", "REPAIRMAN", "MATERNAL_NURSE", "CLEANER", "COOK", "DRIVER"};
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都"};
        String[][] skills = {
            {"做饭", "带孩子", "打扫卫生"},
            {"家电维修", "水电维修"},
            {"新生儿护理", "产妇护理", "月子餐"},
            {"日常保洁", "深度保洁"},
            {"家常菜", "川菜"},
            {"C1驾照", "长途驾驶"}
        };
        String[] contacts = {"张女士", "李先生", "王太太", "赵先生", "孙阿姨", "周先生"};
        String[] phones = {"13900000001", "13900000002", "13900000003", "13900000004", "13900000005", "13900000006"};
        String[] statuses = {"OPEN", "OPEN", "OPEN", "OPEN", "MATCHED", "CLOSED"};
        String[] budgets = {"6000", "300", "15000", "2000", "5000", "8000"};
        String[] startDates = {"2026-08-01", "2026-07-26", "2026-08-15", "2026-08-01", "2026-08-01", "2026-07-20"};
        String[] endDates = {"2026-10-31", "2026-07-26", "2026-11-15", "2026-08-31", "2026-08-31", "2026-07-31"};

        for (int i = 0; i < 6; i++) {
            Requirement r = new Requirement();
            String id = "REQ" + String.format("%04d", idGenerator.getAndIncrement());
            r.setId(id);
            r.setTitle(titles[i]);
            r.setDescription("客户需求：" + titles[i] + "。要求有经验者优先，薪资面议。");
            r.setProfessionType(professions[i]);
            r.setCity(cities[i]);
            r.setRequiredSkills(Arrays.asList(skills[i]));
            r.setContactName(contacts[i]);
            r.setContactPhone(phones[i]);
            r.setBudget(new java.math.BigDecimal(budgets[i]));
            r.setServiceStartTime(startDates[i]);
            r.setServiceEndTime(endDates[i]);
            r.setStatus(statuses[i]);
            r.setCreateTime(LocalDateTime.now().minusDays(i + 1));
            r.setUpdateTime(LocalDateTime.now().minusDays(i));
            requirementMap.put(id, r);
        }
    }

    public List<Requirement> list(String keyword, String status, String professionType, String city) {
        return requirementMap.values().stream()
            .filter(r -> {
                if (keyword != null && !keyword.trim().isEmpty()) {
                    String kw = keyword.trim().toLowerCase();
                    boolean match = r.getTitle().toLowerCase().contains(kw)
                        || r.getDescription().toLowerCase().contains(kw)
                        || r.getContactName().toLowerCase().contains(kw);
                    if (!match) return false;
                }
                if (status != null && !status.trim().isEmpty()) {
                    if (!r.getStatus().equals(status)) return false;
                }
                if (professionType != null && !professionType.trim().isEmpty()) {
                    if (!r.getProfessionType().equals(professionType)) return false;
                }
                if (city != null && !city.trim().isEmpty()) {
                    if (!r.getCity().equals(city.trim())) return false;
                }
                return true;
            })
            .sorted(Comparator.comparing(Requirement::getCreateTime).reversed())
            .collect(Collectors.toList());
    }

    public Requirement getById(String id) {
        Requirement r = requirementMap.get(id);
        if (r == null) {
            throw new BusinessException(404, "需求不存在");
        }
        return r;
    }

    public Requirement create(RequirementCreateDTO dto) {
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            throw new BusinessException("需求标题不能为空");
        }
        if (dto.getProfessionType() == null || dto.getProfessionType().trim().isEmpty()) {
            throw new BusinessException("请选择服务类型");
        }
        if (dto.getCity() == null || dto.getCity().trim().isEmpty()) {
            throw new BusinessException("请选择服务城市");
        }
        if (dto.getContactName() == null || dto.getContactName().trim().isEmpty()) {
            throw new BusinessException("联系人不能为空");
        }
        if (dto.getContactPhone() == null || dto.getContactPhone().trim().isEmpty()) {
            throw new BusinessException("联系电话不能为空");
        }

        String id = "REQ" + String.format("%04d", idGenerator.getAndIncrement());
        Requirement r = new Requirement();
        r.setId(id);
        r.setTitle(dto.getTitle());
        r.setDescription(dto.getDescription());
        r.setProfessionType(dto.getProfessionType());
        r.setCity(dto.getCity());
        r.setRequiredSkills(dto.getRequiredSkills());
        r.setBudget(dto.getBudget());
        r.setServiceStartTime(dto.getServiceStartTime());
        r.setServiceEndTime(dto.getServiceEndTime());
        r.setContactName(dto.getContactName());
        r.setContactPhone(dto.getContactPhone());
        r.setStatus("OPEN");
        r.setCreateTime(LocalDateTime.now());
        r.setUpdateTime(LocalDateTime.now());
        requirementMap.put(id, r);
        return r;
    }

    public List<RecommendResult> recommendProviders(String requirementId) {
        Requirement r = getById(requirementId);

        List<ServiceProvider> allProviders = providerService.list(null, null, null, null);

        List<RecommendResult> results = new ArrayList<>();

        for (ServiceProvider p : allProviders) {
            RecommendResult result = calcMatch(r, p);
            if (result != null) {
                results.add(result);
            }
        }

        results.sort((a, b) -> b.getMatchScore() - a.getMatchScore());

        return results;
    }

    public Map<String, Object> getRecommendStats(String requirementId) {
        Requirement r = getById(requirementId);
        List<ServiceProvider> allProviders = providerService.list(null, null, r.getProfessionType(), r.getCity());

        int total = allProviders.size();
        int approved = 0;
        int pending = 0;
        int rejected = 0;
        int notSubmitted = 0;
        int skillMatched = 0;

        for (ServiceProvider p : allProviders) {
            if (p.getVerifyStatus() == VerifyStatus.APPROVED) {
                approved++;
                if (hasAnySkillMatch(r, p)) {
                    skillMatched++;
                }
            } else if (p.getVerifyStatus() == VerifyStatus.PENDING) {
                pending++;
            } else if (p.getVerifyStatus() == VerifyStatus.REJECTED) {
                rejected++;
            } else {
                notSubmitted++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInCityProfession", total);
        stats.put("approved", approved);
        stats.put("pending", pending);
        stats.put("rejected", rejected);
        stats.put("notSubmitted", notSubmitted);
        stats.put("skillMatched", skillMatched);
        return stats;
    }

    private boolean hasAnySkillMatch(Requirement r, ServiceProvider p) {
        if (r.getRequiredSkills() == null || r.getRequiredSkills().isEmpty()) {
            return true;
        }
        if (p.getSkillTags() == null || p.getSkillTags().isEmpty()) {
            return false;
        }
        for (String skill : r.getRequiredSkills()) {
            if (p.getSkillTags().contains(skill)) {
                return true;
            }
        }
        return false;
    }

    private RecommendResult calcMatch(Requirement r, ServiceProvider p) {
        List<String> matchedSkills = new ArrayList<>();
        List<String> missingSkills = new ArrayList<>();
        int score = 0;
        StringBuilder reason = new StringBuilder();

        if (p.getVerifyStatus() != VerifyStatus.APPROVED) {
            return null;
        }
        score += 40;
        reason.append("已认证");

        if (!r.getProfessionType().equals(p.getProfessionType())) {
            return null;
        }
        score += 30;
        reason.append("、职业匹配");

        if (!r.getCity().equals(p.getCity())) {
            return null;
        }
        score += 20;
        reason.append("、同城");

        if (r.getRequiredSkills() == null || r.getRequiredSkills().isEmpty()) {
            score += 10;
            reason.append("、无技能要求");
        } else if (p.getSkillTags() != null) {
            for (String skill : r.getRequiredSkills()) {
                if (p.getSkillTags().contains(skill)) {
                    matchedSkills.add(skill);
                    score += 3;
                } else {
                    missingSkills.add(skill);
                }
            }
            if (matchedSkills.isEmpty()) {
                return null;
            }
            reason.append("、技能匹配(").append(matchedSkills.size()).append("/").append(r.getRequiredSkills().size()).append(")");
        } else {
            return null;
        }

        RecommendResult result = new RecommendResult();
        result.setProvider(p);
        result.setMatchScore(score);
        result.setMatchedSkills(matchedSkills);
        result.setMissingSkills(missingSkills);
        result.setMatchReason(reason.toString());

        return result;
    }

    public Requirement selectProvider(String requirementId, String providerId) {
        Requirement r = getById(requirementId);
        ServiceProvider p = providerService.getByIdOrThrow(providerId);

        if (p.getVerifyStatus() != VerifyStatus.APPROVED) {
            throw new BusinessException("只能选择已认证的服务商");
        }

        r.setSelectedProviderId(providerId);
        r.setStatus("MATCHED");
        r.setUpdateTime(LocalDateTime.now());
        return r;
    }

    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        Collection<Requirement> all = requirementMap.values();
        stats.put("total", all.size());
        stats.put("open", (int) all.stream().filter(r -> "OPEN".equals(r.getStatus())).count());
        stats.put("matched", (int) all.stream().filter(r -> "MATCHED".equals(r.getStatus())).count());
        stats.put("closed", (int) all.stream().filter(r -> "CLOSED".equals(r.getStatus())).count());

        Map<String, Long> professionStats = all.stream()
            .collect(Collectors.groupingBy(Requirement::getProfessionType, Collectors.counting()));
        stats.put("professionStats", professionStats);

        Map<String, Long> cityStats = all.stream()
            .collect(Collectors.groupingBy(Requirement::getCity, Collectors.counting()));
        stats.put("cityStats", cityStats);

        return stats;
    }
}
