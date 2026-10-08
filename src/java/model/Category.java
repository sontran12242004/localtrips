package model;

public class Category {

    private int categoryId;
    private String categoryCode;
    private String categoryName;
    private int placeCount;

    public Category() {
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

    public void setCategoryCode(
            String categoryCode
    ) {
        this.categoryCode = categoryCode;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public int getPlaceCount() {
        return placeCount;
    }

    public void setPlaceCount(int placeCount) {
        this.placeCount = placeCount;
    }

    public int getId() {
        return categoryId;
    }

    public void setId(int id) {
        this.categoryId = id;
    }

    public String getName() {
        return categoryName;
    }

    public void setName(String name) {
        this.categoryName = name;
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
