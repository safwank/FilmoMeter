package safwan.filmometer.data;

import android.graphics.Bitmap;

import java.util.Hashtable;

public class Film {
    private String title;
    private int year;
    private String cast;
    private double rating;
    private Bitmap poster;
    private Hashtable<String, Double> allScores;

    public Film()
    {
        allScores = new Hashtable<String, Double>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCast() {
        return cast;
    }

    public void setCast(String cast) {
        this.cast = cast;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public Bitmap getPoster() {
        return poster;
    }

    public void setPoster(Bitmap poster) {
        this.poster = poster;
    }

    public Hashtable<String, Double> getAllScores() {
        return allScores;
    }

    public void addScore(String sourceDescription, double score)
    {
        allScores.put(sourceDescription, score);
    }
}
