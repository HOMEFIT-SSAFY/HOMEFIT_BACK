package com.happyhome.rental.dao;

import com.happyhome.rental.dto.RentalDetail;
import com.happyhome.rental.dto.RentalNotice;
import com.happyhome.rental.dto.RentalSearchCondition;
import com.happyhome.rental.dto.RentalSupply;

import java.util.List;
import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RentalNoticeMapper {

	void upsert(RentalNotice notice);

	List<RentalNotice> findRecent();

	List<RentalNotice> findByCondition(RentalSearchCondition condition);

    Optional<RentalNotice> findById(String noticeId);

    Optional<RentalDetail> findDetailByNoticeId(String noticeId);

    List<RentalSupply> findSuppliesByNoticeId(String noticeId);

    void deleteApiNoticesBefore(String noticeDate);
}
