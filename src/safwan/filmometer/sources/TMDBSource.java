package safwan.filmometer.sources;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.data.Film;
import safwan.filmometer.tools.RestClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TMDBSource implements RatingSource {

    private DateFormat dateFormat;

    public TMDBSource() {
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public Film getInfoFor(String film) {
        String apiURL;

        try {
            apiURL = String.format("http://api.themoviedb.org/2.1/Movie.search/en/json/%s/%s", "8abd8211399f1196bdefef458fc4c5ed", URLEncoder.encode(film, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return null;
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

    private Film getFilmInfoFrom(String response) {
        if (null == response) {
            return null;
        }

        try {
            JSONArray jsonResults = new JSONArray(response);

            if (jsonResults.length() > 0) {
                JSONObject firstResult = jsonResults.getJSONObject(0);

                if (null != firstResult) {
                    Film film = new Film();
                    film.setTitle(firstResult.getString("name"));
                    film.setYear(parseYearFrom(firstResult.getString("released")));
                    film.setRating(firstResult.getDouble("rating"));

                    return film;
                }
            }
        } catch (JSONException e) {
            return null;
        } catch (ParseException e) {
            return null;
        }

        return null;
    }

    private int parseYearFrom(String date) throws ParseException {
        Date parsedDate = dateFormat.parse(date);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(parsedDate);
        return calendar.get(Calendar.YEAR);
    }
}
