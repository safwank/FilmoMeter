package safwan.filmometer.sources;

import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.tools.RestClient;

public class IMDBSource implements RatingSource {

    public Film getInfoFor(String film) {
        RestClient client = new RestClient("http://www.imdbapi.com");
        client.addParam("t", film);

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

            if (jsonResult != null) {
                Film film = new Film();
                film.setTitle(jsonResult.getString("Title"));
                film.setYear(jsonResult.getInt("Year"));
                film.setCast(jsonResult.getString("Actors"));
                film.setRating(jsonResult.getDouble("Rating"));

                return film;
            }
        } catch (JSONException e) {
            return null;
        }

        return null;
    }
}
