package com.platform.vo;

import com.platform.model.ServiceProvider;
import lombok.Data;

import java.util.List;

@Data
public class RecommendResult {
    private ServiceProvider provider;
    private int matchScore;
    private List<String> matchedSkills;
    private List<String> missingSkills;
    private String matchReason;
}
