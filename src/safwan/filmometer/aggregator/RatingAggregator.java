package safwan.filmometer.aggregator;

import safwan.filmometer.data.Film;
import safwan.filmometer.data.FilmFromSource;
import safwan.filmometer.sources.IMDBSource;
import safwan.filmometer.sources.RatingSource;
import safwan.filmometer.sources.RottenTomatoesSource;
import safwan.filmometer.sources.TMDBSource;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RatingAggregator {

    private List<RatingSource> ratingSources;

    public Film getSummaryInfoFor(String keyword) {
        List<RatingSource> ratingSources = loadAllSources();
        return getSummaryInfoFrom(ratingSources, keyword);
    }

    private Film getSummaryInfoFrom(List<RatingSource> ratingSources, final String keyword) {
        final List<List<FilmFromSource>> aggregatedRatings = retrieveAggregatedRatingsAsyncFrom(ratingSources, keyword);
        return correlateRatingsAndReturnSummary(aggregatedRatings);
    }

    private List<List<FilmFromSource>> retrieveAggregatedRatingsAsyncFrom(List<RatingSource> ratingSources, final String keyword) {
        final List<List<FilmFromSource>> aggregatedFilms = new ArrayList<List<FilmFromSource>>();
        final List<Thread> sourceThreads = new ArrayList<Thread>();

        for (final RatingSource source : ratingSources) {
            final Thread sourceThread = new Thread(new Runnable() {
                public void run() {
                    List<FilmFromSource> currentFilms = getCurrentFilmsFor(source, keyword);
                    if (currentFilms != null && !currentFilms.isEmpty()) {
                        synchronized (aggregatedFilms) {
                            aggregatedFilms.add(currentFilms);
                        }
                    }
                }
            });
            sourceThreads.add(sourceThread);
            sourceThread.start();
        }

        waitForAllThreadsToComplete(sourceThreads);
        return aggregatedFilms;
    }

    private List<FilmFromSource> getCurrentFilmsFor(RatingSource ratingSource, String keyword) {
        Matcher matcher = getTitleAndYearMatcherFor(keyword);
        if (matcher.find()) {
            String title = matcher.group(1);
            Integer year = Integer.valueOf(matcher.group(3));
            return ratingSource.getMatchingResultsFor(title, year);
        }
        return ratingSource.getMatchingResultsFor(keyword);
    }

    private void waitForAllThreadsToComplete(List<Thread> sourceThreads) {
        for (Thread sourceThread : sourceThreads) {
            try {
                sourceThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private Matcher getTitleAndYearMatcherFor(String keyword) {
        String alphanumericExpression = "((?:[a-z][a-z]*[0-9]+[a-z0-9]*))";
        String commaExpression = "(,)";
        String yearExpression = "((?:(?:[1]{1}\\d{1}\\d{1}\\d{1})|(?:[2]{1}\\d{3})))(?![\\d])";

        Pattern p = Pattern.compile(alphanumericExpression + commaExpression + yearExpression, Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        return p.matcher(keyword);
    }

    private Film correlateRatingsAndReturnSummary(List<List<FilmFromSource>> aggregatedFilms) {
        Film summary = getSummaryFilmFrom(aggregatedFilms);
        if (null != summary) {
            calculateAndAssignAverageScoreBasedOnCorrelation(aggregatedFilms, summary);
        }
        return summary;
    }

    private Film getSummaryFilmFrom(List<List<FilmFromSource>> aggregatedFilms) {
        Film summary = null;
        for (List<FilmFromSource> currentFilms : aggregatedFilms) {
            if (!currentFilms.isEmpty()) {
                FilmFromSource firstResult = currentFilms.get(0);
                if (firstResult.isPrimarySource()) {
                    summary = createSummaryFrom(firstResult);
                    break;
                }
            }
        }
        return summary;
    }

    private void calculateAndAssignAverageScoreBasedOnCorrelation(List<List<FilmFromSource>> aggregatedFilms, Film summary) {
        double totalScore = 0;
        int validSourceCount = 0;

        for (List<FilmFromSource> currentFilms : aggregatedFilms) {
            for (FilmFromSource currentFilm : currentFilms) {
                if (summaryMatchesCurrentFilm(summary, currentFilm)) {
                    double currentRating = currentFilm.getRating();
                    totalScore += currentFilm.getRating();
                    summary.addScore(currentFilm.getSourceDescription(), currentRating);
                    validSourceCount++;
                    break;
                }
            }
        }

        double roundedRating = roundRating(totalScore / validSourceCount);
        summary.setRating(roundedRating);
    }

    private boolean summaryMatchesCurrentFilm(Film summary, FilmFromSource currentFilm) {
        return summary.getTitle().equals(currentFilm.getTitle()) && summary.getYear() == currentFilm.getYear();
    }

    private Film createSummaryFrom(FilmFromSource firstResult) {
        Film filmSummary = new Film();
        filmSummary.setTitle(firstResult.getTitle());
        filmSummary.setYear(firstResult.getYear());
        filmSummary.setCast(firstResult.getCast());
        filmSummary.setPoster(firstResult.getPoster());
        return filmSummary;
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

    //TODO: Move this to a helper class
    private double roundRating(double rating) {
        DecimalFormat twoDForm = new DecimalFormat("#.#");
        return Double.valueOf(twoDForm.format(rating));
    }
}
