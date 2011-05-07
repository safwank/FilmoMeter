package safwan.filmometer.aggregator;

import safwan.filmometer.sources.IMDBSource;
import safwan.filmometer.sources.RatingSource;
import safwan.filmometer.sources.RottenTomatoesSource;
import safwan.filmometer.sources.TMDBSource;

import java.util.ArrayList;

//TODO: Figure out whether the various scores actually correspond to the same film
public class RatingAggregator {

    private ArrayList<RatingSource> ratingSources = new ArrayList<RatingSource>();

    public String getAverageRatingFor(String film)
    {
        ArrayList<RatingSource> ratingSources = loadAllSources();
        double averageScore = getAverageScoreFrom(ratingSources, film);

        return String.valueOf(averageScore);
    }

    private double getAverageScoreFrom(ArrayList<RatingSource> ratingSources, String film) {
        double totalScore = 0;
        int validSourceCount = 0;

        for(RatingSource source : ratingSources)
        {
            double currentScore = source.getRatingFor(film);

            if (currentScore > 0)
            {
                totalScore += currentScore;
                validSourceCount++;
            }
        }

        return totalScore/validSourceCount;
    }

    private ArrayList<RatingSource> loadAllSources() {
        if (ratingSources.size() > 0)
        {
            return ratingSources;
        }

        //TODO: Figure out how to make this bloody thing work with ServiceLoader (through META-INF/services) or some other method
        /*
        ServiceLoader<RatingSource> ratingSourceLoader = ServiceLoader.load(RatingSource.class);

        for(RatingSource ratingSource : ratingSourceLoader)
        {
            ratingSources.add(ratingSource);
        }*/

        ratingSources.add(new IMDBSource());
        ratingSources.add(new RottenTomatoesSource());
        ratingSources.add(new TMDBSource());

        return ratingSources;
    }
}
