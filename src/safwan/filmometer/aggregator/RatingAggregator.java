package safwan.filmometer.aggregator;

import safwan.filmometer.tools.RestClient;

public class RatingAggregator {
    public String getAverageRatingFor(String film)
    {
        //TODO: Load all sources, get ratings from each and average them out

        RestClient client = new RestClient("http://www.imdbapi.com");
        client.AddParam("t", film);

        try {
            client.Execute(RestClient.RequestMethod.GET);
        } catch (Exception e) {
            return e.toString();
        }

        String response = client.getResponse();
        return response;
    }
}
