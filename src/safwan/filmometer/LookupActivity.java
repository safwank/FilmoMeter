package safwan.filmometer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import safwan.filmometer.aggregator.RatingAggregator;
import safwan.filmometer.data.Film;

public class LookupActivity extends Activity {
    private ImageView mPoster;
    private TextView mHeader;
    private TextView mContent;
    private ProgressDialog mProgressDialog;

    private RatingAggregator mAggregator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup);

        mPoster = (ImageView) findViewById(R.id.poster);
        mHeader = (TextView) findViewById(R.id.header);
        mContent = (TextView) findViewById(R.id.content);

        mAggregator = new RatingAggregator();

        displaySearchDialog();
        enableTypeToSearchFunctionality();
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
                displaySearchDialog();
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

    private void enableTypeToSearchFunctionality() {
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    }

    private void displaySearchDialog() {
        onSearchRequested();
    }

    private void displayResult(Film summaryInfo) {
        if (summaryInfo != null) {
            setPoster(summaryInfo.getPoster());
            setHeader(summaryInfo);
            setAverageRating(String.valueOf(summaryInfo.getRating()));
        } else {
            displayNoMatchFound();
        }
    }

    private void displayNoMatchFound() {
        setPoster(null);
        setHeader(null);
        setAverageRating(null);
    }

    private void setPoster(Bitmap poster) {
        if (poster != null) {
            mPoster.setImageBitmap(poster);
        } else {
            mPoster.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.noposter));
        }
    }

    private void setHeader(Film summaryInfo) {
        if (summaryInfo != null) {
            String lineEnding = System.getProperty("line.separator");
            StringBuffer headerBuffer = new StringBuffer();

            //TODO: There should be a better way of doing this
            headerBuffer.append(summaryInfo.getTitle());
            headerBuffer.append(lineEnding);
            headerBuffer.append(summaryInfo.getYear());
            headerBuffer.append(lineEnding);
            headerBuffer.append(summaryInfo.getCast());

            mHeader.setText(headerBuffer.toString());
        } else {
            mHeader.setText(null);
        }
    }

    private void setAverageRating(String averageRating) {
        if (averageRating == null) {
            mContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            mContent.setText(getString(R.string.empty_result));
        } else {
            mContent.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 60);
            mContent.setText(averageRating);
        }
    }

    private void showProgress() {
        mProgressDialog = ProgressDialog.show(LookupActivity.this, "", "Searching and aggregating scores. Please wait...", true);
    }

    private void hideProgress() {
        mProgressDialog.hide();
    }

    /**
     * Background task to handle movie rating lookups. This correctly shows and
     * hides the {@link ProgressDialog} from the GUI thread before starting a
     * background query to the rating aggregator. When finished, it transitions
     * back to the GUI thread where it updates with the average rating and details.
     */
    private class LookupTask extends AsyncTask<String, String, Film> {
        @Override
        protected void onPreExecute() {
            showProgress();
        }

        @Override
        protected Film doInBackground(String... args) {
            String query = args[0];

            if (query == null) {
                return null;
            }

            publishProgress(query);
            return mAggregator.getSummaryInfoFor(query);
        }

        @Override
        protected void onPostExecute(Film summaryInfo) {
            hideProgress();
            displayResult(summaryInfo);
        }
    }
}
