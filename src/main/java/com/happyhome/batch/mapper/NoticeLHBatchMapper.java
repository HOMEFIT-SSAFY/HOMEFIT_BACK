package com.happyhome.batch.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.happyhome.batch.dto.NoticeLHDetail;
import com.happyhome.batch.dto.NoticeLHSupply;
import com.happyhome.batch.dto.OpenApiLog;

@Mapper
public interface NoticeLHBatchMapper {
	void upsertDetail(NoticeLHDetail detail);
	void deleteSuppliesByNoticeId(String noticeId);
	void insertSupply(NoticeLHSupply supply);
	void insertLog(OpenApiLog log);
}
