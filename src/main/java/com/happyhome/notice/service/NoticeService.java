package com.happyhome.notice.service;

import com.happyhome.notice.dao.NoticeDao;
import com.happyhome.notice.dto.NoticeDto;
import com.happyhome.notice.dto.NoticeRequest;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class NoticeService {

    private final NoticeDao noticeDao;

    public NoticeService(NoticeDao noticeDao) {
        this.noticeDao = noticeDao;
    }

    public List<NoticeDto> findVisible(int limit) {
        return noticeDao.findVisible(normalizeLimit(limit));
    }

    public List<NoticeDto> findAll(int limit) {
        return noticeDao.findAll(normalizeLimit(limit));
    }

    public List<NoticeDto> findPopupNotices(int limit) {
        return noticeDao.findPopupNotices(normalizeLimit(limit));
    }

    public Optional<NoticeDto> findById(int noticeId) {
        return noticeDao.findById(noticeId);
    }

    public NoticeDto create(NoticeRequest request, String fallbackWriter) {
        NoticeDto notice = toNotice(new NoticeDto(), request, fallbackWriter);
        return noticeDao.save(notice);
    }

    public Optional<NoticeDto> update(int noticeId, NoticeRequest request, String fallbackWriter) {
        return noticeDao.findById(noticeId).map(existing -> {
            NoticeDto notice = toNotice(existing, request, fallbackWriter);
            noticeDao.update(notice);
            return noticeDao.findById(noticeId).orElse(notice);
        });
    }

    public void delete(int noticeId) {
        noticeDao.deleteById(noticeId);
    }

    private NoticeDto toNotice(NoticeDto notice, NoticeRequest request, String fallbackWriter) {
        String title = request == null ? "" : request.title();
        String content = request == null ? "" : request.content();
        String writer = request == null ? "" : request.writer();
        if (!StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new IllegalArgumentException("제목과 내용을 입력해 주세요.");
        }
        notice.setTitle(title.trim());
        notice.setContent(content.trim());
        notice.setWriter(StringUtils.hasText(writer) ? writer.trim() : fallbackWriter);
        notice.setPopupEnabled(request != null && Boolean.TRUE.equals(request.popupEnabled()));
        notice.setPinned(request != null && Boolean.TRUE.equals(request.pinned()));
        notice.setVisible(request == null || request.visible() == null || Boolean.TRUE.equals(request.visible()));
        return notice;
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return 30;
        }
        return Math.min(limit, 100);
    }
}
