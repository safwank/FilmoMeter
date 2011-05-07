package safwan.filmometer.aggregator;

import safwan.filmometer.sources.IMDBSource;
import safwan.filmometer.sources.IRatingSource;
import safwan.filmometer.sources.RottenTomatoesSource;

import java.util.ArrayList;

//TODO: Figure out whether the various scores actually correspond to the same film
public class RatingAggregator {

    public String getAverageRatingFor(String film)
    {
        ArrayList<IRatingSource> ratingSources = loadAllSources();
        double averageScore = getAverageScoreFrom(ratingSources, film);

        return String.valueOf(averageScore);
    }

    private double getAverageScoreFrom(ArrayList<IRatingSource> ratingSources, String film) {
        double totalScore = 0;
        int validSourceCount = 0;

        for(IRatingSource source : ratingSources)
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

    private ArrayList<IRatingSource> loadAllSources() {
        ArrayList<IRatingSource> ratingSources = new ArrayList<IRatingSource>();

        //TODO: figure out how to make this bloody thing work with ServiceLoader (through META-INF/services) or some other method
        /*
        ServiceLoader<IRatingSource> ratingSourceLoader = ServiceLoader.load(IRatingSource.class);

        for(IRatingSource ratingSource : ratingSourceLoader)
        {
            ratingSources.add(ratingSource);
        }*/

        ratingSources.add(new IMDBSource());
        ratingSources.add(new RottenTomatoesSource());

        return ratingSources;
    }
}
