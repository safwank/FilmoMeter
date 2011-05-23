package safwan.filmometer.aggregator;

import safwan.filmometer.data.Film;
import safwan.filmometer.data.SourceFilm;
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

    private Film getSummaryInfoFrom(List<RatingSource> ratingSources, final String film) {
        final List<List<SourceFilm>> aggregatedFilms = new ArrayList<List<SourceFilm>>();
        List<Thread> sourceThreads = new ArrayList<Thread>();

        //Retrieve the ratings asynchronously to speed things up
        for (final RatingSource source : ratingSources) {
            Thread currentThread = new Thread(new Runnable() {
                public void run() {
                    List<SourceFilm> currentFilms = source.getInfoFor(film);

                    if (currentFilms != null && !currentFilms.isEmpty()) {
                        synchronized (aggregatedFilms) {
                            aggregatedFilms.add(currentFilms);
                        }
                    }
                }
            });

            sourceThreads.add(currentThread);
            currentThread.start();
        }

        //Ensure all threads are done before moving on
        for (Thread sourceThread : sourceThreads) {
            try {
                sourceThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return CorrelateAndReturnTopResultIn(aggregatedFilms);
    }

    private Film CorrelateAndReturnTopResultIn(List<List<SourceFilm>> aggregatedFilms) {
        double totalScore = 0;
        int validSourceCount = 0;
        Film summary = GetSummaryFilmFrom(aggregatedFilms);

        if (null != summary) {
            for (List<SourceFilm> currentFilms : aggregatedFilms) {
                // Simple logic to determine whether the results from different sources correspond to each other
                for (SourceFilm currentFilm : currentFilms) {
                    if (summary.getTitle().equals(currentFilm.getTitle()) && summary.getYear() == currentFilm.getYear()) {
                        totalScore += currentFilm.getRating();
                        validSourceCount++;

                        break;
                    }
                }
            }

            summary.setRating(roundRating(totalScore / validSourceCount));
        }

        return summary;
    }

    private Film GetSummaryFilmFrom(List<List<SourceFilm>> aggregatedFilms) {
        Film summary = null;

        for (List<SourceFilm> currentFilms : aggregatedFilms) {
           if (!currentFilms.isEmpty()) {
                SourceFilm firstResult = currentFilms.get(0);

                if (firstResult.isPrimarySource()) {
                    summary = new Film();
                    summary.setTitle(firstResult.getTitle());
                    summary.setYear(firstResult.getYear());
                    summary.setCast(firstResult.getCast());
                    summary.setPoster(firstResult.getPoster());

                    break;
                }
            }
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
