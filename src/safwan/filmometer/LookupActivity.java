package safwan.filmometer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
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

    protected void setPoster(Bitmap poster)
    {
        mPoster.setImageBitmap(poster);
    }

    protected void setHeader(Film summaryInfo) {
        String lineEnding = System.getProperty("line.separator");
        StringBuffer headerBuffer = new StringBuffer();

        headerBuffer.append(summaryInfo.getTitle());
        headerBuffer.append(lineEnding);
        headerBuffer.append(summaryInfo.getYear());
        headerBuffer.append(lineEnding);
        headerBuffer.append(summaryInfo.getCast());

        mHeader.setText(headerBuffer.toString());
    }

    protected void setAverageRating(String averageRating) {
        mContent.setText(averageRating);
    }

    /**
     * Background task to handle movie rating lookups. This correctly shows and
     * hides the {@link ProgressDialog} from the GUI thread before starting a
     * background query to the rating aggregator. When finished, it transitions
     * back to the GUI thread where it updates with the average rating and details.
     */
    private class LookupTask extends AsyncTask<String, String, String> {
        private Film summaryInfo = null;

        @Override
        protected void onPreExecute() {
            mProgressDialog = ProgressDialog.show(LookupActivity.this, "", "Searching. Please wait...", true);
        }

        @Override
        protected String doInBackground(String... args) {
            String query = args[0];
            String averageRating;

            try {
                if (query != null) {
                    publishProgress(query);

                    summaryInfo = mAggregator.getSummaryInfoFor(query);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            averageRating = summaryInfo == null ? getString(R.string.empty_result) : String.valueOf(summaryInfo.getRating());

            return averageRating;
        }

        @Override
        protected void onProgressUpdate(String... args) {
        }

        @Override
        protected void onPostExecute(String averageRating) {
            mProgressDialog.hide();

            setPoster(summaryInfo.getPoster());
            setHeader(summaryInfo);
            setAverageRating(averageRating);
        }
    }
}
