package org.ngelmakproject.web.rest.dto;


public class DonationStats {

    private Integer totalAmount;
    private int count;
    private Integer averageAmount;
    private Integer lastDonationAmount;

    public Integer getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public Integer getAverageAmount() {
        return averageAmount;
    }

    public void setAverageAmount(Integer averageAmount) {
        this.averageAmount = averageAmount;
    }

    public Integer getLastDonationAmount() {
        return lastDonationAmount;
    }

    public void setLastDonationAmount(Integer lastDonationAmount) {
        this.lastDonationAmount = lastDonationAmount;
    }
}
