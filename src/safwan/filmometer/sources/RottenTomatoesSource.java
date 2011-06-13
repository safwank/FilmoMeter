package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.data.SourceFilm;
import safwan.filmometer.tools.RestClient;

import java.util.ArrayList;
import java.util.List;

public class RottenTomatoesSource implements RatingSource {

    private static final String SOURCE_DESCRIPTION = "Rotten Tomatoes";

    public List<SourceFilm> getInfoFor(String film) {
        return getInfoFor(film, 0);
    }

    public List<SourceFilm> getInfoFor(String film, int year) {
        RestClient client = new RestClient("http://api.rottentomatoes.com/api/public/v1.0/movies.json");
        client.addParam("apikey", "b2x78beenefg6tq3ynr56r4a");
        client.addParam("q", film);
        client.addParam("page_limit", "5");

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return null;
        }

        String response = client.getResponse();
        return getFilmInfoFrom(response);
    }

    private List<SourceFilm> getFilmInfoFrom(String response) {
        if (response == null) {
            return null;
        }

        List<SourceFilm> films = new ArrayList<SourceFilm>();

        try {
            JSONObject rawResult = new JSONObject(response);
            int resultCount = rawResult.getInt("total");

            if (resultCount > 0) {
                JSONArray results = rawResult.getJSONArray("movies");

                for (int i = 0; i < results.length(); i++) {
                    JSONObject currentResult = results.getJSONObject(i);

                    if (null != currentResult) {
                        SourceFilm film = new SourceFilm();
                        film.setSourceDescription(SOURCE_DESCRIPTION);
                        film.setTitle(currentResult.getString("title"));
                        film.setYear(currentResult.getInt("year"));
                        film.setCast(getFilmCastFrom(currentResult.getJSONArray("abridged_cast")));
                        film.setRating(calculateAverageRatingFor(currentResult.getJSONObject("ratings")));

                        films.add(film);
                    }
                }
            }
        } catch (JSONException e) {
            return null;
        }

        return films;
    }

    private String getFilmCastFrom(JSONArray cast) {
        StringBuffer castBuffer = new StringBuffer();

        for (int i = 0; i < cast.length(); i++) {
            if (i > 0) {
                castBuffer.append(", ");
            }

            try {
                castBuffer.append(cast.getJSONObject(i).getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return castBuffer.toString();
    }

    private double calculateAverageRatingFor(JSONObject ratings) {
        if (ratings == null || ratings.length() == 0) {
            return 0;
        }

        double totalScore = 0;

        try {
            totalScore = ratings.getDouble("critics_score") + ratings.getDouble("audience_score");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return totalScore / 2 / 10;
    }
}
