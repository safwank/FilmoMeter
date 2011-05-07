package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.tools.RestClient;

public class RottenTomatoesSource implements RatingSource {

    public double getRatingFor(String film) {
        RestClient client = new RestClient("http://api.rottentomatoes.com/api/public/v1.0/movies.json");
        client.addParam("apikey", "b2x78beenefg6tq3ynr56r4a");
        client.addParam("q", film);
        client.addParam("page_limit", "1");

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return 0;
        }

        String response = client.getResponse();
        return getRatingFrom(response);
    }

    private double getRatingFrom(String response) {
        if (response == null)
        {
            return 0;
        }

        try {
            JSONObject rawResult = new JSONObject(response);
            int resultCount = rawResult.getInt("total");

            if (resultCount > 0)
            {
                JSONArray jsonResults = rawResult.getJSONArray("movies");
                JSONObject firstResult = jsonResults.getJSONObject(0);
                return calculateAverageRatingFor(firstResult.getJSONObject("ratings"));
            }

            return 0;
        } catch (JSONException e) {
            return 0;
        }
    }

    private double calculateAverageRatingFor(JSONObject ratings) {
        double totalScore = 0;

        try {
            totalScore = ratings.getDouble("critics_score") + ratings.getDouble("audience_score");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return totalScore/ratings.length()/10;
    }

}
