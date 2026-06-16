package com.happyhome.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.happyhome.member.dao.MemberDao;
import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;

class MemberServiceTest {

    private final PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    @Test
    void registerEncodesPasswordWithBcrypt() {
        FakeMemberDao memberDao = new FakeMemberDao();
        MemberService memberService = new MemberService(memberDao, passwordEncoder);
        MemberDto member = member("ssafy", "plain-password");

        boolean registered = memberService.register(member);

        assertThat(registered).isTrue();
        assertThat(memberDao.saved.get("ssafy").getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("plain-password", memberDao.saved.get("ssafy").getPassword())).isTrue();
    }

    @Test
    void registerRejectsDuplicateUserId() {
        FakeMemberDao memberDao = new FakeMemberDao();
        memberDao.saved.put("ssafy", member("ssafy", "{bcrypt}stored"));
        MemberService memberService = new MemberService(memberDao, passwordEncoder);

        boolean registered = memberService.register(member("ssafy", "new-password"));

        assertThat(registered).isFalse();
    }

    @Test
    void findOrCreateOAuthMemberCreatesSocialMemberWithEncodedPassword() {
        FakeMemberDao memberDao = new FakeMemberDao();
        MemberService memberService = new MemberService(memberDao, passwordEncoder);

        MemberDto member = memberService.findOrCreateOAuthMember(
                "kakao",
                "123456789",
                "social@example.com",
                "소셜회원"
        );

        assertThat(member.getUserId()).isEqualTo("oauth_kakao_123456789");
        assertThat(member.getName()).isEqualTo("소셜회원");
        assertThat(member.getEmail()).isEqualTo("social@example.com");
        assertThat(memberDao.saved.get(member.getUserId()).getPassword()).startsWith("{bcrypt}");
    }

    @Test
    void findOrCreateOAuthMemberReturnsExistingSocialMember() {
        FakeMemberDao memberDao = new FakeMemberDao();
        memberDao.saved.put("oauth_google_abc", member("oauth_google_abc", "{bcrypt}stored"));
        MemberService memberService = new MemberService(memberDao, passwordEncoder);

        MemberDto member = memberService.findOrCreateOAuthMember("google", "abc", "new@example.com", "새 이름");

        assertThat(member.getEmail()).isEqualTo("oauth_google_abc@example.com");
        assertThat(member.getName()).isEqualTo("싸피");
    }

    @Test
    void updateEncodesNewPassword() {
        FakeMemberDao memberDao = new FakeMemberDao();
        memberDao.saved.put("ssafy", member("ssafy", "{bcrypt}old"));
        MemberService memberService = new MemberService(memberDao, passwordEncoder);
        MemberDto form = member("ssafy", "changed-password");
        form.setName("김싸피");
        form.setEmail("new@example.com");
        form.setPhone("010-2222-3333");

        Optional<MemberDto> updated = memberService.update("ssafy", form);

        assertThat(updated).isPresent();
        assertThat(updated.get().getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("changed-password", updated.get().getPassword())).isTrue();
        assertThat(updated.get().getName()).isEqualTo("김싸피");
    }

    @Test
    void upgradePasswordIfLegacyEncodesPlainPasswordAfterSuccessfulLogin() {
        FakeMemberDao memberDao = new FakeMemberDao();
        memberDao.saved.put("ssafy", member("ssafy", "plain-password"));
        MemberService memberService = new MemberService(memberDao, passwordEncoder);

        memberService.upgradePasswordIfLegacy("ssafy", "plain-password");

        assertThat(memberDao.saved.get("ssafy").getPassword()).startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("plain-password", memberDao.saved.get("ssafy").getPassword())).isTrue();
    }

    private static MemberDto member(String userId, String password) {
        MemberDto member = new MemberDto();
        member.setUserId(userId);
        member.setPassword(password);
        member.setName("싸피");
        member.setEmail(userId + "@example.com");
        member.setPhone("010-1111-2222");
        return member;
    }

    private static class FakeMemberDao extends MemberDao {

        private final Map<String, MemberDto> saved = new HashMap<>();

        FakeMemberDao() {
            super(null);
        }

        @Override
        public Optional<MemberDto> findByUserId(String userId) {
            return Optional.ofNullable(saved.get(userId));
        }

        @Override
        public void save(MemberDto member) {
            saved.put(member.getUserId(), copy(member));
        }

        @Override
        public void update(MemberDto member) {
            saved.put(member.getUserId(), copy(member));
        }

        private MemberDto copy(MemberDto member) {
            MemberDto copied = new MemberDto();
            copied.setUserId(member.getUserId());
            copied.setPassword(member.getPassword());
            copied.setName(member.getName());
            copied.setEmail(member.getEmail());
            copied.setPhone(member.getPhone());
            return copied;
        }
    }
}
