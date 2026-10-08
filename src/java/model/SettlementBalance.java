package model;

import java.math.BigDecimal;

public class SettlementBalance {

    private int memberId;
    private String memberName;
    private BigDecimal paid = BigDecimal.ZERO;
    private BigDecimal owed = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;

    public SettlementBalance() {
    }

    public SettlementBalance(
            int memberId,
            String memberName
    ) {
        this.memberId = memberId;
        this.memberName = memberName;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public BigDecimal getPaid() {
        return paid;
    }

    public void setPaid(BigDecimal paid) {
        this.paid = paid;
    }

    public BigDecimal getOwed() {
        return owed;
    }

    public void setOwed(BigDecimal owed) {
        this.owed = owed;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public void calculateBalance() {
        this.balance = paid.subtract(owed);
    }
}