package safwan.filmometer.aggregator;

import safwan.filmometer.data.Film;
import safwan.filmometer.sources.IMDBSource;
import safwan.filmometer.sources.RatingSource;
import safwan.filmometer.sources.RottenTomatoesSource;
import safwan.filmometer.sources.TMDBSource;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class RatingAggregator {

    private List<RatingSource> ratingSources;

    public Film getSummaryInfoFor(String film) {
        List<RatingSource> ratingSources = loadAllSources();

        return getSummaryInfoFrom(ratingSources, film);
    }

    private Film getSummaryInfoFrom(List<RatingSource> ratingSources, String film) {

        ArrayList<List<Film>> aggregatedFilms = new ArrayList<List<Film>>();

        //TODO: Retrieve the ratings asynchronously to speed things up
        for (RatingSource source : ratingSources) {
            List<Film> currentFilms = source.getInfoFor(film);

            if (currentFilms != null && currentFilms.size() > 0) {
                aggregatedFilms.add(currentFilms);
            }
        }

        return CorrelateAndReturnTopResultIn(aggregatedFilms);
    }

    private Film CorrelateAndReturnTopResultIn(ArrayList<List<Film>> aggregatedFilms) {
        double totalScore = 0;
        int validSourceCount = 0;
        Film summary = null;

        for (List<Film> currentFilms : aggregatedFilms) {
            if (null == summary) {
                // Assume the first result is the most authoritative, for now :)
                Film firstResult = currentFilms.get(0);

                summary = new Film();
                summary.setTitle(firstResult.getTitle());
                summary.setYear(firstResult.getYear());
                summary.setCast(firstResult.getCast());
                summary.setPoster(firstResult.getPoster());

                totalScore += firstResult.getRating();
                validSourceCount++;
            } else {
                // Simple logic to determine whether the results from different sources correspond to each other
                for (Film currentFilm : currentFilms) {
                    if (summary.getTitle().equals(currentFilm.getTitle()) && summary.getYear() == currentFilm.getYear()) {
                        totalScore += currentFilm.getRating();
                        validSourceCount++;

                        break;
                    }
                }
            }
        }

        if (null != summary) {
            summary.setRating(roundRating(totalScore / validSourceCount));
        }

        return summary;
    }

    private List<RatingSource> loadAllSources() {
        if (ratingSources != null) {
            return ratingSources;
        }

        //TODO: Figure out how to make this bloody thing work with ServiceLoader (through META-INF/services) or some other method
        /*
        ServiceLoader<RatingSource> ratingSourceLoader = ServiceLoader.load(RatingSource.class);

        for(RatingSource ratingSource : ratingSourceLoader)
        {
            ratingSources.add(ratingSource);
        }*/

        ratingSources = new ArrayList<RatingSource>();
        ratingSources.add(new IMDBSource());
        ratingSources.add(new RottenTomatoesSource());
        ratingSources.add(new TMDBSource());

        return ratingSources;
    }

    private double roundRating(double rating) {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Double.valueOf(twoDForm.format(rating));
    }
}
