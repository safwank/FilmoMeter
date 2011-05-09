package safwan.filmometer.sources;

import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.tools.RestClient;

public class TMDBSource implements RatingSource {

    public Film getInfoFor(String film) {
        String apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s", "apikey", film);
        RestClient client = new RestClient(apiURL);

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
            JSONObject jsonResult = new JSONObject(response);
            int totalResults = jsonResult.getInt("totalResults");

            if (totalResults > 0) {
                JSONObject firstResult = jsonResult.getJSONArray("movies").getJSONObject(0);

                if (firstResult != null) {
                    Film film = new Film();
                    film.setTitle(firstResult.getString("name"));
                    film.setYear(firstResult.getInt("year"));
                    film.setRating(firstResult.getDouble("score"));

                    return film;
                }
            }
        } catch (JSONException e) {
            return null;
        }

        return null;
    }
}
