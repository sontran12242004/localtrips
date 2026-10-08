package model;

import java.sql.Timestamp;

public class TripMemberPreference {

    private int tripId;
    private int userId;
    private int categoryId;
    private Timestamp createdAt;

    public TripMemberPreference() {
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(
            Timestamp createdAt
    ) {
        this.createdAt = createdAt;
    }
}