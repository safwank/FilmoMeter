package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.data.SourceFilm;
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

    public TMDBSource() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public List<SourceFilm> getInfoFor(String film) {
        return getInfoFor(film, 0);
    }

    public List<SourceFilm> getInfoFor(String film, int year) {
        String apiURL;
        String encodedFilm;

        try {
            encodedFilm = URLEncoder.encode(film, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }

        if (year > 0) {
            apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s+%d", "8abd8211399f1196bdefef458fc4c5ed", encodedFilm, year);
        } else {
            apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s", "8abd8211399f1196bdefef458fc4c5ed", encodedFilm);
        }

        RestClient client = new RestClient(apiURL);

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return null;
        }

        String response = client.getResponse();
        return getFilmInfoFrom(response);
    }

    private List<SourceFilm> getFilmInfoFrom(String response) {
        if (null == response) {
            return null;
        }

        List<SourceFilm> films = new ArrayList<SourceFilm>();

        try {
            JSONArray results = new JSONArray(response);

            for (int i = 0; i < results.length(); i++) {
                JSONObject currentResult = results.getJSONObject(i);

                if (null != currentResult) {
                    SourceFilm film = new SourceFilm();
                    film.setTitle(currentResult.getString("name"));
                    film.setYear(parseYearFrom(currentResult.getString("released")));
                    film.setRating(currentResult.getDouble("rating"));

                    films.add(film);
                }
            }
        } catch (JSONException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }

        return films;
    }

    private int parseYearFrom(String date) throws ParseException {
        Date parsedDate = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);
        return calendar.get(Calendar.YEAR);
    }
}
