package com.happyhome.member.dto;

public record MemberResponse(
        String userId,
        String name,
        String email,
        String phone,
        boolean rentalNoticeEmailEnabled
) {

    public static MemberResponse from(MemberDto member) {
        return new MemberResponse(
                member.getUserId(),
                member.getName(),
                member.getEmail(),
                member.getPhone(),
                member.isRentalNoticeEmailEnabled()
        );
    }
}
