package model;

import java.sql.Timestamp;

public class TripMember {

    private int tripId;
    private int userId;
    private String memberRole;
    private Timestamp joinedAt;

    private String fullName;
    private String email;

    public TripMember() {
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

    public String getMemberRole() {
        return memberRole;
    }

    public void setMemberRole(String memberRole) {
        this.memberRole = memberRole;
    }

    public Timestamp getJoinedAt() {
        return joinedAt;
    }

    public void setJoinedAt(Timestamp joinedAt) {
        this.joinedAt = joinedAt;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return memberRole;
    }

    public void setRole(String role) {
        this.memberRole = role;
    }

    public int getId() {
        return userId;
    }

    public void setId(int id) {
        this.userId = id;
    }
}
