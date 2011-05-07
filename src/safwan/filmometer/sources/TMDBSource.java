package safwan.filmometer.sources;

import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.tools.RestClient;

public class TMDBSource implements RatingSource {

    public double getRatingFor(String film) {
        String apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s", "apikey", film);
        RestClient client = new RestClient(apiURL);

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
            JSONObject jsonResult = new JSONObject(response);
            int totalResults = jsonResult.getInt("totalResults");

            if (totalResults > 0) {
                JSONObject firstResult = jsonResult.getJSONArray("movies").getJSONObject(0);
                return firstResult != null ? firstResult.getDouble("score") : 0;
            }
        } catch (JSONException e) {
            return 0;
        }

        return 0;
    }
}
