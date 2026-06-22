package com.happyhome.rental.email.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface RentalNoticeEmailLogMapper {

    Integer count(
            @Param("userId") String userId,
            @Param("noticeId") String noticeId,
            @Param("eventType") String eventType
    );

    void save(
            @Param("userId") String userId,
            @Param("noticeId") String noticeId,
            @Param("eventType") String eventType,
            @Param("recipientEmail") String recipientEmail,
            @Param("subject") String subject
    );
}
