package com.happyhome.member.dto;

public class MemberUpdateRequest {

    private String password;
    private String name;
    private String email;
    private String phone;
    private boolean rentalNoticeEmailEnabled;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isRentalNoticeEmailEnabled() {
        return rentalNoticeEmailEnabled;
    }

    public void setRentalNoticeEmailEnabled(boolean rentalNoticeEmailEnabled) {
        this.rentalNoticeEmailEnabled = rentalNoticeEmailEnabled;
    }

    public MemberDto toMemberDto() {
        MemberDto member = new MemberDto();
        member.setPassword(password);
        member.setName(name);
        member.setEmail(email);
        member.setPhone(phone);
        member.setRentalNoticeEmailEnabled(rentalNoticeEmailEnabled);
        return member;
    }
}
