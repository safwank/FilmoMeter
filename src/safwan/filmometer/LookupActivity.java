package safwan.filmometer;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;
import android.widget.TextView;
import safwan.filmometer.aggregator.RatingAggregator;

public class LookupActivity extends Activity implements Animation.AnimationListener
{
    private String mEntryTitle;

    private View mTitleBar;
    private TextView mTitle;
    private ProgressBar mProgress;
    private TextView mContent;

    private Animation mSlideIn;
    private Animation mSlideOut;

    private RatingAggregator mAggregator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup);

        // Load animations used to show/hide progress bar
        mSlideIn = AnimationUtils.loadAnimation(this, R.anim.slide_in);
        mSlideOut = AnimationUtils.loadAnimation(this, R.anim.slide_out);

        // Listen for the "in" animation so we make the progress bar visible
        // only after the sliding has finished.
        mSlideIn.setAnimationListener(this);

        mTitleBar = findViewById(R.id.title_bar);
        mTitle = (TextView) findViewById(R.id.title);
        mProgress = (ProgressBar) findViewById(R.id.progress);
        mContent = (TextView) findViewById(R.id.content);

        mAggregator = new RatingAggregator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lookup, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.lookup_search: {
                onSearchRequested();
                return true;
            }
        }
        return false;
    }

    public void onNewIntent(Intent intent) {
        final String action = intent.getAction();
        if (Intent.ACTION_SEARCH.equals(action)) {
            // Start query for incoming search request
            String query = intent.getStringExtra(SearchManager.QUERY);
            startSearching(query);
        }
    }

    private void startSearching(String query) {
        // Start lookup for new movie in background
        new LookupTask().execute(query);
    }

    /**
     * Set the title for the current entry.
     */
    protected void setEntryTitle(String entryText) {
        mEntryTitle = entryText;
        mTitle.setText(mEntryTitle);
    }

    /**
     * Set the content for the current entry. This will update our
     * content view to show the requested content.
     */
    protected void setEntryContent(String entryContent) {
        mContent.setText(entryContent);
    }

    public void onAnimationStart(Animation animation) {
    }

    /**
     * Make the {@link ProgressBar} visible when our in-animation finishes.
     */
    public void onAnimationEnd(Animation animation) {
        mProgress.setVisibility(View.VISIBLE);
    }

    public void onAnimationRepeat(Animation animation) {
    }

    /**
     * Background task to handle movie rating lookups. This correctly shows and
     * hides the loading animation from the GUI thread before starting a
     * background query to the rating aggregator. When finished, it transitions
     * back to the GUI thread where it updates with the newly-found entry.
     */
    private class LookupTask extends AsyncTask<String, String, String> {
        /**
         * Before jumping into background thread, start sliding in the
         * {@link ProgressBar}. We'll only show it once the animation finishes.
         */
        @Override
        protected void onPreExecute() {
            mTitleBar.startAnimation(mSlideIn);
        }

        @Override
        protected String doInBackground(String... args) {
            String query = args[0];
            String averageRating = null;

            try {
                if (query != null) {
                    // Push our requested word to the title bar
                    publishProgress(query);

                    averageRating = mAggregator.getAverageRatingFor(query);
                }
            } catch (Exception e) {
            }

            if (averageRating == null) {
                averageRating = getString(R.string.empty_result);
            }

            return averageRating;
        }

        /**
         * Our progress update pushes a title bar update.
         */
        @Override
        protected void onProgressUpdate(String... args) {
            String searchWord = args[0];
            setEntryTitle(searchWord);
        }

        /**
         * When finished, push the newly-found entry content into our
         * content view and hide the {@link ProgressBar}.
         */
        @Override
        protected void onPostExecute(String parsedText) {
            mTitleBar.startAnimation(mSlideOut);
            mProgress.setVisibility(View.INVISIBLE);

            setEntryContent(parsedText);
        }
    }
}
