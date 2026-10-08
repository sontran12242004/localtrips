package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Trip {

    private int tripId;
    private int ownerId;
    private String tripName;
    private String destination;
    private Date startDate;
    private Date endDate;
    private BigDecimal budget;
    private String description;
    private String status;
    private Timestamp createdAt;

    /*
     * Vai trò của user hiện tại trong Trip:
     * OWNER hoặc MEMBER.
     */
    private String role;

    public Trip() {
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public String getTripName() {
        return tripName;
    }

    public void setTripName(String tripName) {
        this.tripName = tripName;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getBudget() {
        return budget;
    }

    public void setBudget(BigDecimal budget) {
        this.budget = budget;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    /*
     * Getter alias dành cho các JSP hiện tại.
     */

    public int getId() {
        return tripId;
    }

    public void setId(int id) {
        this.tripId = id;
    }

    public String getName() {
        return tripName;
    }

    public void setName(String name) {
        this.tripName = name;
    }

    public String getArea() {
        return destination;
    }

    public void setArea(String area) {
        this.destination = area;
    }
}