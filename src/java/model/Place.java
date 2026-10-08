package model;

import java.math.BigDecimal;
import java.sql.Time;

public class Place {

    private int placeId;
    private int categoryId;
    private String categoryName;
    private String placeName;
    private String address;
    private String description;
    private BigDecimal estimatedCost;
    private BigDecimal rating;
    private Time openingTime;
    private Time closingTime;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String placeType;
    private boolean active;

    public Place() {
    }

    public int getPlaceId() {
        return placeId;
    }

    public void setPlaceId(int placeId) {
        this.placeId = placeId;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(
            String categoryName
    ) {
        this.categoryName = categoryName;
    }

    public String getPlaceName() {
        return placeName;
    }

    public void setPlaceName(String placeName) {
        this.placeName = placeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
    }

    public BigDecimal getEstimatedCost() {
        return estimatedCost;
    }

    public void setEstimatedCost(
            BigDecimal estimatedCost
    ) {
        this.estimatedCost = estimatedCost;
    }

    public BigDecimal getRating() {
        return rating;
    }

    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }

    public Time getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(
            Time openingTime
    ) {
        this.openingTime = openingTime;
    }

    public Time getClosingTime() {
        return closingTime;
    }

    public void setClosingTime(
            Time closingTime
    ) {
        this.closingTime = closingTime;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(
            BigDecimal latitude
    ) {
        this.latitude = latitude;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(
            BigDecimal longitude
    ) {
        this.longitude = longitude;
    }

    public String getPlaceType() {
        return placeType;
    }

    public void setPlaceType(String placeType) {
        this.placeType = placeType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /*
 * Getter alias dành cho JSP hiện tại.
     */

    public int getId() {
        return placeId;
    }

    public void setId(int id) {
        this.placeId = id;
    }

    public String getName() {
        return placeName;
    }

    public void setName(String name) {
        this.placeName = name;
    }

    public BigDecimal getEstimatedPrice() {
        return estimatedCost;
    }

    public void setEstimatedPrice(BigDecimal estimatedPrice) {
        this.estimatedCost = estimatedPrice;
    }

    public String getOpenHours() {
        if (openingTime == null && closingTime == null) {
            return "Chưa cập nhật";
        }

        if (openingTime == null) {
            return "Đến " + formatTime(closingTime);
        }

        if (closingTime == null) {
            return "Từ " + formatTime(openingTime);
        }

        return formatTime(openingTime)
                + " - "
                + formatTime(closingTime);
    }

    public String getCategoryIcon() {
        if (categoryName == null) {
            return "📍";
        }

        String value = categoryName.toUpperCase();

        if (value.contains("FOOD")
                || value.contains("ẨM THỰC")) {
            return "🍜";
        }

        if (value.contains("NATURE")
                || value.contains("THIÊN NHIÊN")) {
            return "🌳";
        }

        if (value.contains("SHOP")
                || value.contains("MUA SẮM")) {
            return "🛍️";
        }

        if (value.contains("ENTERTAINMENT")
                || value.contains("GIẢI TRÍ")) {
            return "🎡";
        }

        if (value.contains("CULTURE")
                || value.contains("VĂN HÓA")) {
            return "🏛️";
        }

        return "📍";
    }

    private String formatTime(Time time) {
        String value = time.toString();

        return value.length() >= 5
                ? value.substring(0, 5)
                : value;
    }
}
