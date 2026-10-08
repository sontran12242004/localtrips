package model;

import java.math.BigDecimal;

public class GroupFundSummary {
    private BigDecimal totalContributed = BigDecimal.ZERO;
    private BigDecimal totalSpent = BigDecimal.ZERO;
    private BigDecimal balance = BigDecimal.ZERO;

    public BigDecimal getTotalContributed() { return totalContributed; }
    public void setTotalContributed(BigDecimal value) { this.totalContributed = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public void setTotalSpent(BigDecimal value) { this.totalSpent = value == null ? BigDecimal.ZERO : value; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal value) { this.balance = value == null ? BigDecimal.ZERO : value; }
}
