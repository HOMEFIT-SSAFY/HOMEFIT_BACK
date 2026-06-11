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
