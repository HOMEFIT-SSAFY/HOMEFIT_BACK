package com.happyhome.notice.dao;

import com.happyhome.notice.dto.NoticeDto;
import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class NoticeDao {

    private final NoticeMapper mapper;

    public NoticeDao(NoticeMapper mapper) {
        this.mapper = mapper;
    }

    @PostConstruct
    void ensurePopupColumns() {
        addColumnQuietly(() -> mapper.addPopupEnabledColumnIfMissing(), () -> mapper.addPopupEnabledColumn());
        addColumnQuietly(() -> mapper.addPinnedColumnIfMissing(), () -> mapper.addPinnedColumn());
        addColumnQuietly(() -> mapper.addVisibleColumnIfMissing(), () -> mapper.addVisibleColumn());
    }

    public List<NoticeDto> findVisible(int limit) {
        return mapper.findVisible(limit);
    }

    public List<NoticeDto> findAll(int limit) {
        return mapper.findAll(limit);
    }

    public List<NoticeDto> findPopupNotices(int limit) {
        return mapper.findPopupNotices(limit);
    }

    public Optional<NoticeDto> findById(int noticeId) {
        return Optional.ofNullable(mapper.findById(noticeId));
    }

    public NoticeDto save(NoticeDto notice) {
        mapper.save(notice);
        Integer id = notice.getNoticeId();
        return id == null ? notice : findById(id).orElse(notice);
    }

    public void update(NoticeDto notice) {
        mapper.update(notice);
    }

    public void deleteById(int noticeId) {
        mapper.deleteById(noticeId);
    }

    private void addColumnQuietly(Runnable ifMissingMigration, Runnable fallbackMigration) {
        try {
            ifMissingMigration.run();
        } catch (Exception ifMissingUnsupported) {
            try {
                fallbackMigration.run();
            } catch (Exception duplicateOrUnsupported) {
                // Existing project databases may already have the column.
            }
        }
    }
}
