package model;

public class GroupPreference {

    private int categoryId;
    private String categoryCode;
    private String categoryName;
    private int memberCount;
    private double percentage;

    public GroupPreference() {
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryCode() {
        return categoryCode;
    }

    public void setCategoryCode(String categoryCode) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public double getPercentage() {
        return percentage;
    }

    public void setPercentage(double percentage) {
        this.percentage = percentage;
    }

    public String getIcon() {
        if (categoryCode == null) {
            return "📍";
        }

        String code = categoryCode.toUpperCase();

        if ("FOOD".equals(code)) {
            return "🍜";
        }

        if ("NATURE".equals(code)) {
            return "🌳";
        }

        if ("SHOPPING".equals(code)) {
            return "🛍️";
        }

        if ("ENTERTAINMENT".equals(code)) {
            return "🎡";
        }

        if ("CULTURE".equals(code)) {
            return "🏛️";
        }

        return "📍";
    }
}
