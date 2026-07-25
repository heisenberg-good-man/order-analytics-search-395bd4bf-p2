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
            "接送孩子上下学司机",
            "家具安装服务"
        };
        String[] professions = {"BABYSITTER", "REPAIRMAN", "MATERNAL_NURSE", "CLEANER", "COOK", "DRIVER", "REPAIRMAN"};
        String[] cities = {"北京", "上海", "广州", "深圳", "杭州", "成都", "北京"};
        String[][] skills = {
            {"做饭", "带孩子", "打扫卫生"},
            {"家电维修", "水电维修"},
            {"新生儿护理", "产妇护理", "月子餐"},
            {"日常保洁", "深度保洁"},
            {"家常菜", "川菜"},
            {"C1驾照", "长途驾驶"},
            {"家具安装", "水电维修"}
        };
        String[] contacts = {"张女士", "李先生", "王太太", "赵先生", "孙阿姨", "周先生", "吴女士"};
        String[] phones = {"13900000001", "13900000002", "13900000003", "13900000004", "13900000005", "13900000006", "13900000007"};
        String[] statuses = {"OPEN", "OPEN", "OPEN", "OPEN", "MATCHED", "SIGNED", "CLOSED"};
        String[] budgets = {"6000", "300", "15000", "2000", "5000", "8000", "500"};
        String[] startDates = {"2026-08-01", "2026-07-26", "2026-08-15", "2026-08-01", "2026-08-01", "2026-07-20", "2026-07-10"};
        String[] endDates = {"2026-10-31", "2026-07-26", "2026-11-15", "2026-08-31", "2026-08-31", "2026-07-31", "2026-07-12"};
        String[] selectedProviderIds = {null, null, null, null, null, "SP0006", null};

        for (int i = 0; i < 7; i++) {
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
            r.setSelectedProviderId(selectedProviderIds[i]);
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
            results.add(result);
        }

        results.sort((a, b) -> {
            if (a.isSignable() != b.isSignable()) {
                return a.isSignable() ? -1 : 1;
            }
            return b.getMatchScore() - a.getMatchScore();
        });

        return results;
    }

    public Map<String, Object> getRecommendStats(String requirementId) {
        Requirement r = getById(requirementId);
        List<ServiceProvider> allProviders = providerService.list(null, null, null, null);

        int total = allProviders.size();
        int signable = 0;
        int unSignable = 0;
        int approved = 0;
        int pending = 0;
        int rejected = 0;
        int notSubmitted = 0;
        int cityMatched = 0;
        int professionMatched = 0;
        int skillMatched = 0;

        for (ServiceProvider p : allProviders) {
            boolean isApproved = p.getVerifyStatus() == VerifyStatus.APPROVED;
            boolean isProfessionMatch = r.getProfessionType().equals(p.getProfessionType());
            boolean isCityMatch = r.getCity().equals(p.getCity());
            boolean hasSkillMatch = hasAnySkillMatch(r, p);

            if (isApproved) approved++;
            else if (p.getVerifyStatus() == VerifyStatus.PENDING) pending++;
            else if (p.getVerifyStatus() == VerifyStatus.REJECTED) rejected++;
            else notSubmitted++;

            if (isProfessionMatch) professionMatched++;
            if (isCityMatch) cityMatched++;
            if (isApproved && hasSkillMatch) skillMatched++;

            if (isApproved && isProfessionMatch && isCityMatch && hasSkillMatch) {
                signable++;
            } else {
                unSignable++;
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("total", total);
        stats.put("signable", signable);
        stats.put("unSignable", unSignable);
        stats.put("approved", approved);
        stats.put("pending", pending);
        stats.put("rejected", rejected);
        stats.put("notSubmitted", notSubmitted);
        stats.put("cityMatched", cityMatched);
        stats.put("professionMatched", professionMatched);
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
        List<String> unSignableReasons = new ArrayList<>();
        List<String> matchPoints = new ArrayList<>();

        boolean approved = p.getVerifyStatus() == VerifyStatus.APPROVED;
        boolean professionMatched = r.getProfessionType().equals(p.getProfessionType());
        boolean cityMatched = r.getCity().equals(p.getCity());
        boolean hasSkillMatch = false;

        if (r.getRequiredSkills() == null || r.getRequiredSkills().isEmpty()) {
            hasSkillMatch = true;
        } else if (p.getSkillTags() != null) {
            for (String skill : r.getRequiredSkills()) {
                if (p.getSkillTags().contains(skill)) {
                    matchedSkills.add(skill);
                    hasSkillMatch = true;
                } else {
                    missingSkills.add(skill);
                }
            }
        }

        if (!approved) {
            unSignableReasons.add("未通过实名认证");
        } else {
            score += 40;
            matchPoints.add("已认证");
        }

        if (!professionMatched) {
            unSignableReasons.add("职业类型不匹配");
        } else {
            score += 30;
            matchPoints.add("职业匹配");
        }

        if (!cityMatched) {
            unSignableReasons.add("服务城市不同");
        } else {
            score += 20;
            matchPoints.add("同城");
        }

        if (!hasSkillMatch) {
            unSignableReasons.add("技能不匹配");
        } else {
                if (r.getRequiredSkills() == null || r.getRequiredSkills().isEmpty()) {
                    score += 10;
                    matchPoints.add("无技能要求");
                } else {
                    score += matchedSkills.size() * 3;
                    matchPoints.add("技能匹配(" + matchedSkills.size() + "/" + r.getRequiredSkills().size() + ")");
                }
        }

        boolean signable = approved && professionMatched && cityMatched && hasSkillMatch;

        RecommendResult result = new RecommendResult();
        result.setProvider(p);
        result.setMatchScore(signable ? score : 0);
        result.setMatchedSkills(matchedSkills);
        result.setMissingSkills(missingSkills);
        result.setMatchReason(String.join("、", matchPoints));
        result.setSignable(signable);
        result.setUnSignableReasons(unSignableReasons);

        return result;
    }

    public Requirement selectProvider(String requirementId, String providerId) {
        Requirement r = getById(requirementId);
        ServiceProvider p = providerService.getByIdOrThrow(providerId);

        RecommendResult matchResult = calcMatch(r, p);
        if (!matchResult.isSignable()) {
            throw new BusinessException("该服务商不可签约：" + String.join("、", matchResult.getUnSignableReasons()));
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
        stats.put("signed", (int) all.stream().filter(r -> "SIGNED".equals(r.getStatus())).count());
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
