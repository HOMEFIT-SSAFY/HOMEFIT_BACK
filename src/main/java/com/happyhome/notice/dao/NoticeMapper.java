package com.happyhome.notice.dao;

import com.happyhome.notice.dto.NoticeDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface NoticeMapper {

    void addPopupEnabledColumnIfMissing();

    void addPopupEnabledColumn();

    void addPinnedColumnIfMissing();

    void addPinnedColumn();

    void addVisibleColumnIfMissing();

    void addVisibleColumn();

    List<NoticeDto> findVisible(int limit);

    List<NoticeDto> findAll(int limit);

    List<NoticeDto> findPopupNotices(int limit);

    NoticeDto findById(int noticeId);

    void save(NoticeDto notice);

    void update(NoticeDto notice);

    void deleteById(int noticeId);
}
