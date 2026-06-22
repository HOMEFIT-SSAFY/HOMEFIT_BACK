package com.happyhome.rental.email.dao;

import org.springframework.stereotype.Repository;

@Repository
public class RentalNoticeEmailLogDao {

    private final RentalNoticeEmailLogMapper mapper;

    public RentalNoticeEmailLogDao(RentalNoticeEmailLogMapper mapper) {
        this.mapper = mapper;
    }

    public boolean exists(String userId, String noticeId, String eventType) {
        Integer count = mapper.count(userId, noticeId, eventType);
        return count != null && count > 0;
    }

    public void save(String userId, String noticeId, String eventType, String recipientEmail, String subject) {
        mapper.save(userId, noticeId, eventType, recipientEmail, subject);
    }
}
