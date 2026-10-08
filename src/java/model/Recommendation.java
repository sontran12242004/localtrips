package model;

public class Recommendation {

    private Place place;
    private double score;
    private double preferenceMatch;
    private double budgetMatch;
    private String reason;

    public Recommendation() {
    }

    public Place getPlace() {
        return place;
    }

    public void setPlace(Place place) {
        this.place = place;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public double getPreferenceMatch() {
        return preferenceMatch;
    }

    public void setPreferenceMatch(double preferenceMatch) {
        this.preferenceMatch = preferenceMatch;
    }

    public double getBudgetMatch() {
        return budgetMatch;
    }

    public void setBudgetMatch(double budgetMatch) {
        this.budgetMatch = budgetMatch;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}