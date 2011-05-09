package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.tools.RestClient;

public class RottenTomatoesSource implements RatingSource {

    public Film getInfoFor(String film) {
        RestClient client = new RestClient("http://api.rottentomatoes.com/api/public/v1.0/movies.json");
        client.addParam("apikey", "b2x78beenefg6tq3ynr56r4a");
        client.addParam("q", film);
        client.addParam("page_limit", "1");

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return null;
        }

        String response = client.getResponse();
        return getFilmInfoFrom(response);
    }

    private Film getFilmInfoFrom(String response) {
        if (response == null) {
            return null;
        }

        try {
            JSONObject rawResult = new JSONObject(response);
            int resultCount = rawResult.getInt("total");

            if (resultCount > 0) {
                JSONArray jsonResults = rawResult.getJSONArray("movies");
                JSONObject firstResult = jsonResults.getJSONObject(0);

                if (firstResult != null) {
                    Film film = new Film();
                    film.setTitle(firstResult.getString("title"));
                    film.setYear(firstResult.getInt("year"));
                    film.setCast(getFilmCastFrom(firstResult.getJSONArray("abridged_cast")));
                    film.setRating(calculateAverageRatingFor(firstResult.getJSONObject("ratings")));

                    return film;
                }
            }
        } catch (JSONException e) {
            return null;
        }

        return null;
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

        return totalScore / ratings.length() / 10;
    }
}
