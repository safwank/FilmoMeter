package safwan.filmometer.sources;

import org.json.JSONException;
import org.json.JSONObject;
import safwan.filmometer.tools.RestClient;

public class IMDBSource implements IRatingSource {

    public double getRatingFor(String film) {
        RestClient client = new RestClient("http://www.imdbapi.com");
        client.addParam("t", film);

        try {
            client.execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return 0;
        }

        String response = client.getResponse();
        return getRatingFrom(response);
    }

    private double getRatingFrom(String response) {
        try {
            JSONObject jsonResult = new JSONObject(response);
            return jsonResult != null ? jsonResult.getDouble("Rating") : 0;
        } catch (JSONException e) {
            return 0;
        }
    }
}
