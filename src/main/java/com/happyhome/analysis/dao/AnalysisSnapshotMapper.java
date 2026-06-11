package com.happyhome.analysis.dao;

import com.happyhome.analysis.dto.AnalysisSnapshot;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnalysisSnapshotMapper {

    void insert(AnalysisSnapshot snapshot);
}

