package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.FilmFromSource;
import safwan.filmometer.tools.RestClient;

import java.util.ArrayList;
import java.util.List;

public class RottenTomatoesSource implements RatingSource {

    private static final String SOURCE_DESCRIPTION = "Rotten Tomatoes";

    public List<FilmFromSource> getMatchingResultsFor(String film) {
        return getMatchingResultsFor(film, 0);
    }

    public List<FilmFromSource> getMatchingResultsFor(String film, int year) {
        RestClient client = constructRestClient(film);

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return null;
        }

        String response = client.getResponse();
        return getFilmInfoFrom(response);
    }

    private RestClient constructRestClient(String film) {
        RestClient client = new RestClient("http://api.rottentomatoes.com/api/public/v1.0/movies.json");
        client.addParam("apikey", "b2x78beenefg6tq3ynr56r4a");
        client.addParam("q", film);
        client.addParam("page_limit", "5");
        return client;
    }

    private List<FilmFromSource> getFilmInfoFrom(String response) {
        if (response == null) {
            return null;
        }

        List<FilmFromSource> films = new ArrayList<FilmFromSource>();

        try {
            JSONObject rawResult = new JSONObject(response);
            if (resultIsNotEmpty(rawResult)) {
                films = getAllFilmsFrom(rawResult);
            }
        } catch (JSONException e) {
            return null;
        }

        return films;
    }

    private boolean resultIsNotEmpty(JSONObject rawResult) throws JSONException {
        int resultCount = rawResult.getInt("total");
        return resultCount > 0;
    }

    private List<FilmFromSource> getAllFilmsFrom(JSONObject rawResult) throws JSONException {
        List<FilmFromSource> films = new ArrayList<FilmFromSource>();
        JSONArray results = rawResult.getJSONArray("movies");

        for (int i = 0; i < results.length(); i++) {
            JSONObject currentResult = results.getJSONObject(i);
            if (null != currentResult) {
                FilmFromSource film = constructFilmFrom(currentResult);
                films.add(film);
            }
        }

        return films;
    }

    private FilmFromSource constructFilmFrom(JSONObject currentResult) throws JSONException {
        FilmFromSource film = new FilmFromSource();
        film.setSourceDescription(SOURCE_DESCRIPTION);
        film.setTitle(currentResult.getString("title"));
        film.setYear(currentResult.getInt("year"));
        film.setCast(getFilmCastFrom(currentResult.getJSONArray("abridged_cast")));
        film.setRating(calculateAverageRatingFor(currentResult.getJSONObject("ratings")));
        return film;
    }

    private String getFilmCastFrom(JSONArray cast) {
        StringBuffer castBuffer = new StringBuffer();

        //TODO: There should be a better way of concatenating the strings. Check the Java API.
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

        int typesOfRating = 2;
        int fullRating = 10;
        return totalScore / typesOfRating / fullRating;
    }
}
