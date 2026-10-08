package model;

import java.math.BigDecimal;

public class SettlementTransfer {

    private int fromMemberId;
    private String fromMemberName;
    private int toMemberId;
    private String toMemberName;
    private BigDecimal amount;

    public SettlementTransfer() {
    }

    public int getFromMemberId() {
        return fromMemberId;
    }

    public void setFromMemberId(int fromMemberId) {
        this.fromMemberId = fromMemberId;
    }

    public String getFromMemberName() {
        return fromMemberName;
    }

    public void setFromMemberName(String fromMemberName) {
        this.fromMemberName = fromMemberName;
    }

    public int getToMemberId() {
        return toMemberId;
    }

    public void setToMemberId(int toMemberId) {
        this.toMemberId = toMemberId;
    }

    public String getToMemberName() {
        return toMemberName;
    }

    public void setToMemberName(String toMemberName) {
        this.toMemberName = toMemberName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}