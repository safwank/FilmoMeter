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
        double totalScore = 0;
        int validSourceCount = 0;
        List<Film> films = new ArrayList<Film>();
        Film summary = null;

        //TODO: Retrieve the ratings asynchronously to speed things up
        for (RatingSource source : ratingSources) {
            Film currentFilm = source.getInfoFor(film);

            if (currentFilm != null) {
                films.add(currentFilm);
            }
        }

        for (Film currentFilm : films) {
            // Assume the first result is the most authoritative, for now :)
            if (summary == null) {
                summary = new Film();
                summary.setTitle(currentFilm.getTitle());
                summary.setYear(currentFilm.getYear());
                summary.setCast(currentFilm.getCast());
                summary.setPoster(currentFilm.getPoster());
            }

            // Simple logic to determine whether the results from different sources correspond to each other
            if (summary.getTitle().equals(currentFilm.getTitle()) && summary.getYear() == currentFilm.getYear()) {
                totalScore += currentFilm.getRating();
                validSourceCount++;
            }
        }

        if (summary != null) {
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
