package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.FilmFromSource;
import safwan.filmometer.tools.RestClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class TMDBSource implements RatingSource {

    private DateFormat dateFormat;
    private static final String SOURCE_DESCRIPTION = "TMDB";

    public TMDBSource() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public List<FilmFromSource> getMatchingResultsFor(String film) {
        return getMatchingResultsFor(film, 0);
    }

    public List<FilmFromSource> getMatchingResultsFor(String film, int year) {
        String encodedFilmTitle = getEncodedFilmTitle(film);
        if ("".equals(encodedFilmTitle)) {
            return null;
        }

        String apiURL = constructAPIURLBasedOn(year, encodedFilmTitle);
        RestClient client = new RestClient(apiURL);

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return null;
        }

        String response = client.getResponse();
        return getFilmInfoFrom(response);
    }

    private String getEncodedFilmTitle(String film) {
        try {
            return URLEncoder.encode(film, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    private String constructAPIURLBasedOn(int year, String encodedFilm) {
        String apiURL;
        if (year > 0) {
            apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s+%d", "8abd8211399f1196bdefef458fc4c5ed", encodedFilm, year);
        } else {
            apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s", "8abd8211399f1196bdefef458fc4c5ed", encodedFilm);
        }
        return apiURL;
    }

    private List<FilmFromSource> getFilmInfoFrom(String response) {
        if (null == response) {
            return null;
        }

        try {
            JSONArray results = new JSONArray(response);
            return getAllFilmsFrom(results);
        } catch (JSONException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }
    }

    private List<FilmFromSource> getAllFilmsFrom(JSONArray results) throws JSONException, ParseException {
        List<FilmFromSource> films = new ArrayList<FilmFromSource>();
        for (int i = 0; i < results.length(); i++) {
            JSONObject currentResult = results.getJSONObject(i);
            if (null != currentResult) {
                FilmFromSource film = createFilmFrom(currentResult);
                films.add(film);
            }
        }
        return films;
    }

    private FilmFromSource createFilmFrom(JSONObject currentResult) throws JSONException, ParseException {
        FilmFromSource film = new FilmFromSource();
        film.setSourceDescription(SOURCE_DESCRIPTION);
        film.setTitle(currentResult.getString("name"));
        film.setYear(parseYearFrom(currentResult.getString("released")));
        film.setRating(currentResult.getDouble("rating"));
        return film;
    }

    //TODO: Move this to a helper class
    private int parseYearFrom(String date) throws ParseException {
        Date parsedDate = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);
        return calendar.get(Calendar.YEAR);
    }
}
