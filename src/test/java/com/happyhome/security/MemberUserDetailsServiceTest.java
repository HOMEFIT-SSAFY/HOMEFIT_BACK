package com.happyhome.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.happyhome.member.dao.MemberDao;
import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

class MemberUserDetailsServiceTest {

    @Test
    void loadsAdminMemberWithAdminRole() {
        MemberUserDetailsService userDetailsService = userDetailsService(member("admin", "{bcrypt}pw"));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin");

        assertThat(userDetails.getUsername()).isEqualTo("admin");
        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_ADMIN");
    }

    @Test
    void loadsRegularMemberWithUserRole() {
        MemberUserDetailsService userDetailsService = userDetailsService(member("ssafy", "{bcrypt}pw"));

        UserDetails userDetails = userDetailsService.loadUserByUsername("ssafy");

        assertThat(userDetails.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
    }

    @Test
    void rejectsUnknownMember() {
        MemberUserDetailsService userDetailsService = userDetailsService(null);

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("missing"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private static MemberUserDetailsService userDetailsService(MemberDto member) {
        return new MemberUserDetailsService(
                new MemberService(new FakeMemberDao(member), PasswordEncoderFactories.createDelegatingPasswordEncoder()));
    }

    private static MemberDto member(String userId, String password) {
        MemberDto member = new MemberDto();
        member.setUserId(userId);
        member.setPassword(password);
        member.setName(userId);
        member.setEmail(userId + "@example.com");
        member.setPhone("010-0000-0000");
        return member;
    }

    private static class FakeMemberDao extends MemberDao {

        private final MemberDto member;

        FakeMemberDao(MemberDto member) {
            super(null);
            this.member = member;
        }

        @Override
        public Optional<MemberDto> findByUserId(String userId) {
            return Optional.ofNullable(member).filter(saved -> saved.getUserId().equals(userId));
        }
    }
}
