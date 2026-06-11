package com.happyhome.member.dao;

import com.happyhome.member.dto.MemberDto;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class MemberDao {

    private final MemberMapper mapper;

    public MemberDao(MemberMapper mapper) {
        this.mapper = mapper;
    }

    public Optional<MemberDto> findByUserId(String userId) {
        return Optional.ofNullable(mapper.findByUserId(userId));
    }

    public Optional<MemberDto> findByUserIdAndEmail(String userId, String email) {
        return Optional.ofNullable(mapper.findByUserIdAndEmail(userId, email));
    }

    public void save(MemberDto member) {
        mapper.save(member);
    }

    public void update(MemberDto member) {
        mapper.update(member);
    }

    public void deleteByUserId(String userId) {
        mapper.deleteByUserId(userId);
    }
}
