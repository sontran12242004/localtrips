package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class GroupFundContribution {
    private int contributionId;
    private int tripId;
    private int userId;
    private String memberName;
    private BigDecimal amount;
    private String note;
    private int createdBy;
    private Timestamp createdAt;

    public int getContributionId() { return contributionId; }
    public void setContributionId(int contributionId) { this.contributionId = contributionId; }
    public int getTripId() { return tripId; }
    public void setTripId(int tripId) { this.tripId = tripId; }
    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
    public int getCreatedBy() { return createdBy; }
    public void setCreatedBy(int createdBy) { this.createdBy = createdBy; }
    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
