package safwan.filmometer.sources;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.FilmFromSource;
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

    public List<FilmFromSource> getMatchingResultsFor(String film) {
        return getMatchingResultsFor(film, 0);
    }

    public List<FilmFromSource> getMatchingResultsFor(String film, int year) {
        RestClient client = constructRestClient(film, year);

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return null;
        }

        String response = client.getResponse();
        return getFilmInfoFrom(response);
    }

    private RestClient constructRestClient(String film, int year) {
        RestClient client = new RestClient("http://www.imdbapi.com");
        client.addParam("t", film);

        if (year > 0) {
            client.addParam("y", String.valueOf(year));
        }
        return client;
    }

    private List<FilmFromSource> getFilmInfoFrom(String response) {
        if (response == null) {
            return null;
        }

        List<FilmFromSource> films = new ArrayList<FilmFromSource>();
        FilmFromSource film;

        try {
            JSONObject jsonResult = new JSONObject(response);
            film = constructFilmFrom(jsonResult);
        } catch (JSONException e) {
            return null;
        }

        films.add(film);
        return films;
    }

    private FilmFromSource constructFilmFrom(JSONObject jsonResult) throws JSONException {
        //Unfortunately the IMDB API only returns a single result
        FilmFromSource film = new FilmFromSource();
        film.setSourceDescription(SOURCE_DESCRIPTION);
        film.setTitle(jsonResult.getString("Title"));
        film.setYear(jsonResult.getInt("Year"));
        film.setCast(jsonResult.getString("Actors"));
        film.setRating(jsonResult.getDouble("Rating"));
        film.setPoster(getPosterImageFrom(jsonResult.getString("Poster")));
        film.setPrimarySource(true); //TODO: This shouldn't be assigned internally as we may end up with multiple primary sources
        return film;
    }

    private Bitmap getPosterImageFrom(String imageURL) {
        URL myImageURL;

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
