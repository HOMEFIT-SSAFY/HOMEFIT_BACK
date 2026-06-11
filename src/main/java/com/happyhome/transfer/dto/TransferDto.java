package com.happyhome.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TransferDto {

    private Integer transferId;
    private String writerId;
    private String title;
    private String content;
    private String status;
    private String address;
    private String detailAddress;
    private String floor;
    private BigDecimal exclusiveArea;
    private Integer depositAmount;
    private Integer monthlyRentAmount;
    private Integer maintenanceFee;
    private Integer transferFee;
    private LocalDate contractEndDate;
    private LocalDate moveInDate;
    private String contactPhone;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> imageUrls = new ArrayList<>();

    public Integer getTransferId() {
        return transferId;
    }

    public void setTransferId(Integer transferId) {
        this.transferId = transferId;
    }

    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDetailAddress() {
        return detailAddress;
    }

    public void setDetailAddress(String detailAddress) {
        this.detailAddress = detailAddress;
    }

    public String getFloor() {
        return floor;
    }

    public void setFloor(String floor) {
        this.floor = floor;
    }

    public BigDecimal getExclusiveArea() {
        return exclusiveArea;
    }

    public void setExclusiveArea(BigDecimal exclusiveArea) {
        this.exclusiveArea = exclusiveArea;
    }

    public Integer getDepositAmount() {
        return depositAmount;
    }

    public void setDepositAmount(Integer depositAmount) {
        this.depositAmount = depositAmount;
    }

    public Integer getMonthlyRentAmount() {
        return monthlyRentAmount;
    }

    public void setMonthlyRentAmount(Integer monthlyRentAmount) {
        this.monthlyRentAmount = monthlyRentAmount;
    }

    public Integer getMaintenanceFee() {
        return maintenanceFee;
    }

    public void setMaintenanceFee(Integer maintenanceFee) {
        this.maintenanceFee = maintenanceFee;
    }

    public Integer getTransferFee() {
        return transferFee;
    }

    public void setTransferFee(Integer transferFee) {
        this.transferFee = transferFee;
    }

    public LocalDate getContractEndDate() {
        return contractEndDate;
    }

    public void setContractEndDate(LocalDate contractEndDate) {
        this.contractEndDate = contractEndDate;
    }

    public LocalDate getMoveInDate() {
        return moveInDate;
    }

    public void setMoveInDate(LocalDate moveInDate) {
        this.moveInDate = moveInDate;
    }

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getViewCount() {
        return viewCount;
    }

    public void setViewCount(Integer viewCount) {
        this.viewCount = viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
    }
}
