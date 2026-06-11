package com.happyhome.security;

import com.happyhome.member.dto.MemberDto;
import com.happyhome.member.service.MemberService;
import java.util.List;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberUserDetailsService implements UserDetailsService {

    private static final String ADMIN_USER_ID = "admin";

    private final MemberService memberService;

    public MemberUserDetailsService(MemberService memberService) {
        this.memberService = memberService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        MemberDto member = memberService.findByUserId(username)
                .orElseThrow(() -> new UsernameNotFoundException("Member not found: " + username));
        return new User(
                member.getUserId(),
                member.getPassword(),
                List.of(new SimpleGrantedAuthority(resolveAuthority(member)))
        );
    }

    private String resolveAuthority(MemberDto member) {
        if (ADMIN_USER_ID.equals(member.getUserId())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }
}
