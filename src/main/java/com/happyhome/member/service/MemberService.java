package com.happyhome.member.service;

import com.happyhome.member.dao.MemberDao;
import com.happyhome.member.dto.MemberDto;
import java.util.Optional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;

    public MemberService(MemberDao memberDao, PasswordEncoder passwordEncoder) {
        this.memberDao = memberDao;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<MemberDto> findByUserId(String userId) {
        return memberDao.findByUserId(userId);
    }

    public Optional<MemberDto> authenticate(String userId, String rawPassword) {
        if (!StringUtils.hasText(userId) || !StringUtils.hasText(rawPassword)) {
            return Optional.empty();
        }
        return memberDao.findByUserId(userId)
                .filter(member -> matchesStoredPassword(rawPassword, member.getPassword()));
    }

    public boolean register(MemberDto member) {
        if (!StringUtils.hasText(member.getUserId())
                || !StringUtils.hasText(member.getPassword())
                || memberDao.findByUserId(member.getUserId()).isPresent()) {
            return false;
        }
        member.setPassword(passwordEncoder.encode(member.getPassword()));
        memberDao.save(member);
        return true;
    }

    public Optional<MemberDto> update(String userId, MemberDto form) {
        return memberDao.findByUserId(userId)
                .map(member -> {
                    if (StringUtils.hasText(form.getPassword())) {
                        member.setPassword(passwordEncoder.encode(form.getPassword()));
                    }
                    member.setName(form.getName());
                    member.setEmail(form.getEmail());
                    member.setPhone(form.getPhone());
                    memberDao.update(member);
                    return member;
                });
    }

    public void upgradePasswordIfLegacy(String userId, String rawPassword) {
        if (!StringUtils.hasText(rawPassword)) {
            return;
        }
        memberDao.findByUserId(userId)
                .filter(member -> !member.getPassword().startsWith("{bcrypt}"))
                .filter(member -> matchesStoredPassword(rawPassword, member.getPassword()))
                .ifPresent(member -> {
                    member.setPassword(passwordEncoder.encode(rawPassword));
                    memberDao.update(member);
                });
    }

    private boolean matchesStoredPassword(String rawPassword, String storedPassword) {
        if (!StringUtils.hasText(storedPassword)) {
            return false;
        }
        if (!storedPassword.startsWith("{")) {
            return storedPassword.equals(rawPassword);
        }
        return passwordEncoder.matches(rawPassword, storedPassword);
    }

    public void delete(String userId) {
        memberDao.deleteByUserId(userId);
    }

    public Optional<MemberDto> findByUserIdAndEmail(String userId, String email) {
        return memberDao.findByUserIdAndEmail(userId, email);
    }
}
