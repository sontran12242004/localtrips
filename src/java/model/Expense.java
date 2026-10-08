package model;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Expense {

    private int expenseId;
    private int tripId;
    private int payerId;
    private int createdBy;
    private String description;
    private BigDecimal amount;
    private boolean fromGroupFund;
    private Timestamp createdAt;

    private String payerName;
    private List<String> participantNames = new ArrayList<>();

    public Expense() {
    }

    public int getExpenseId() {
        return expenseId;
    }

    public void setExpenseId(int expenseId) {
        this.expenseId = expenseId;
    }

    public int getId() {
        return expenseId;
    }

    public void setId(int id) {
        this.expenseId = id;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getPayerId() {
        return payerId;
    }

    public void setPayerId(int payerId) {
        this.payerId = payerId;
    }

    public int getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(int createdBy) {
        this.createdBy = createdBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public boolean isFromGroupFund() {
        return fromGroupFund;
    }

    public boolean getFromGroupFund() {
        return fromGroupFund;
    }

    public void setFromGroupFund(boolean fromGroupFund) {
        this.fromGroupFund = fromGroupFund;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getPayerName() {
        return payerName;
    }

    public void setPayerName(String payerName) {
        this.payerName = payerName;
    }

    public List<String> getParticipantNames() {
        return participantNames;
    }

    public void setParticipantNames(
            List<String> participantNames
    ) {
        this.participantNames = participantNames;
    }

    public void addParticipantName(String participantName) {
        this.participantNames.add(participantName);
    }
}