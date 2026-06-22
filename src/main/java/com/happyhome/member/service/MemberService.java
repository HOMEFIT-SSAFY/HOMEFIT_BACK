package com.happyhome.member.service;

import com.happyhome.member.dao.MemberDao;
import com.happyhome.member.dto.MemberDto;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MemberService {

    private final MemberDao memberDao;
    private final PasswordEncoder passwordEncoder;
    private final BCryptPasswordEncoder rawBcryptPasswordEncoder = new BCryptPasswordEncoder();

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

    public MemberDto findOrCreateOAuthMember(
            String provider,
            String providerUserId,
            String email,
            String name
    ) {
        String userId = oauthUserId(provider, providerUserId, email);
        return memberDao.findByUserId(userId)
                .orElseGet(() -> {
                    MemberDto member = new MemberDto();
                    member.setUserId(userId);
                    member.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
                    member.setName(StringUtils.hasText(name) ? name : providerDisplayName(provider));
                    member.setEmail(StringUtils.hasText(email) ? email : userId + "@oauth.local");
                    member.setPhone("");
                    memberDao.save(member);
                    return member;
                });
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
        if (isRawBcryptHash(storedPassword)) {
            return rawBcryptPasswordEncoder.matches(rawPassword, storedPassword);
        }
        if (!storedPassword.startsWith("{")) {
            return storedPassword.equals(rawPassword);
        }
        return passwordEncoder.matches(rawPassword, storedPassword);
    }

    private boolean isRawBcryptHash(String storedPassword) {
        return storedPassword.startsWith("$2a$")
                || storedPassword.startsWith("$2b$")
                || storedPassword.startsWith("$2y$");
    }

    private String oauthUserId(String provider, String providerUserId, String email) {
        String normalizedProvider = sanitize(provider);
        String identity = StringUtils.hasText(providerUserId) ? providerUserId : email;
        String suffix = sanitize(identity);
        String userId = "oauth_" + normalizedProvider + "_" + suffix;
        if (userId.length() <= 50) {
            return userId;
        }
        return userId.substring(0, 50);
    }

    private String sanitize(String value) {
        if (!StringUtils.hasText(value)) {
            return "user";
        }
        String sanitized = value.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "_")
                .replaceAll("^_+|_+$", "");
        return StringUtils.hasText(sanitized) ? sanitized : "user";
    }

    private String providerDisplayName(String provider) {
        if (!StringUtils.hasText(provider)) {
            return "소셜 회원";
        }
        return provider.substring(0, 1).toUpperCase(Locale.ROOT)
                + provider.substring(1).toLowerCase(Locale.ROOT)
                + " 회원";
    }

    public void delete(String userId) {
        memberDao.deleteByUserId(userId);
    }

    public Optional<MemberDto> findByUserIdAndEmail(String userId, String email) {
        return memberDao.findByUserIdAndEmail(userId, email);
    }
}
