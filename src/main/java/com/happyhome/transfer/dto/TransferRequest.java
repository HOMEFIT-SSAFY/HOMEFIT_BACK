package com.happyhome.transfer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

public class TransferRequest {

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
    private List<String> imageUrls = new ArrayList<>();
    private List<MultipartFile> images = new ArrayList<>();

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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls == null ? new ArrayList<>() : imageUrls;
    }

    public List<MultipartFile> getImages() {
        return images;
    }

    public void setImages(List<MultipartFile> images) {
        this.images = images == null ? new ArrayList<>() : images;
    }
}
