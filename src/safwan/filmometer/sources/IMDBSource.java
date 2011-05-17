package safwan.filmometer.sources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.tools.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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

        Film film = new Film();

        try {
            JSONObject jsonResult = new JSONObject(response);

            film.setTitle(jsonResult.getString("Title"));
            film.setYear(jsonResult.getInt("Year"));
            film.setCast(jsonResult.getString("Actors"));
            film.setRating(jsonResult.getDouble("Rating"));
            film.setPoster(getPosterImageFrom(jsonResult.getString("Poster")));
        } catch (JSONException e) {
            return null;
        }

        return film;
    }

    private Bitmap getPosterImageFrom(String imageURL) {
        URL myImageURL = null;

        try {
            myImageURL = new URL(imageURL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        assert myImageURL != null;

        try {
            HttpURLConnection conn = (HttpURLConnection) myImageURL.openConnection();
            conn.setDoInput(true);
            conn.connect();
            InputStream is = conn.getInputStream();

            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
