package safwan.filmometer.sources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.SourceFilm;
import safwan.filmometer.tools.RestClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class IMDBSource implements RatingSource {

    private static final String SOURCE_DESCRIPTION = "IMDB";

    public List<SourceFilm> getInfoFor(String film) {
        return getInfoFor(film, 0);
    }

    public List<SourceFilm> getInfoFor(String film, int year) {
        RestClient client = new RestClient("http://www.imdbapi.com");
        client.addParam("t", film);

        if (year > 0) {
            client.addParam("y", String.valueOf(year));
        }

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
        SourceFilm film = new SourceFilm();

        try {
            //Unfortunately the IMDB API only returns a single result
            JSONObject jsonResult = new JSONObject(response);

            film.setSourceDescription(SOURCE_DESCRIPTION);
            film.setTitle(jsonResult.getString("Title"));
            film.setYear(jsonResult.getInt("Year"));
            film.setCast(jsonResult.getString("Actors"));
            film.setRating(jsonResult.getDouble("Rating"));
            film.setPoster(getPosterImageFrom(jsonResult.getString("Poster")));
            film.setPrimarySource(true);

        } catch (JSONException e) {
            return null;
        }

        films.add(film);
        return films;
    }

    private Bitmap getPosterImageFrom(String imageURL) {
        URL myImageURL = null;

        try {
            myImageURL = new URL(imageURL);
        } catch (MalformedURLException e) {
            return null;
        }

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
