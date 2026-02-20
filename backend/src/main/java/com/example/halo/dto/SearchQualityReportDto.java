package com.example.halo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchQualityReportDto {
    private String query;
    private int limit;
    private int vectorCount;
    private int keywordCount;
    private int overlapCount;
    private double precisionAtK;
    private List<String> vectorTopIds;
    private List<String> keywordTopIds;
}
