package com.happyhome.member.dao;

import com.happyhome.member.dto.MemberDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberMapper {

    MemberDto findByUserId(String userId);

    MemberDto findByUserIdAndEmail(@Param("userId") String userId, @Param("email") String email);

    void save(MemberDto member);

    void update(MemberDto member);

    void deleteByUserId(String userId);
}
