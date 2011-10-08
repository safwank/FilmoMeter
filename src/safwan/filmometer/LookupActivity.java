package safwan.filmometer;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TextView;
import safwan.filmometer.aggregator.RatingAggregator;
import safwan.filmometer.data.Film;
import safwan.filmometer.views.ScoreMeter;

import java.util.Enumeration;
import java.util.Hashtable;

public class LookupActivity extends TabActivity {
    private TabHost mTabHost;
    private ImageView mPoster;
    private TextView mHeader;
    private TextView mDetails;
    private ProgressDialog mProgressDialog;
    private ScoreMeter mAverageMeter;
    private LinearLayout mMeterList;
    private RatingAggregator mAggregator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAggregator = new RatingAggregator();
        setContentView(R.layout.lookup);
        initializeViews();
        initializeTabs();
        displaySearchDialog();
        enableTypeToSearchFunctionality();
    }

    private void initializeViews() {
        mMeterList = (LinearLayout) findViewById(R.id.meters);
        mPoster = (ImageView) findViewById(R.id.poster);
        mHeader = (TextView) findViewById(R.id.header);
        mDetails = (TextView) findViewById(R.id.details);
        mAverageMeter = (ScoreMeter) findViewById(R.id.averageMeter);
    }

    private void initializeTabs() {
        mTabHost = getTabHost();
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator("Summary").setContent(R.id.summary));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator("Scores").setContent(R.id.meters));
        mTabHost.setCurrentTab(0);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lookup, menu);
        return true;
    }

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
            String query = intent.getStringExtra(SearchManager.QUERY);
            startSearching(query);
        }
    }

    private void startSearching(String query) {
        new LookupTask().execute(query);
    }

    private void displaySearchDialog() {
        onSearchRequested();
    }

    private void displayResult(Film summaryInfo) {
        if (summaryInfo != null) {
            displaySummary(summaryInfo);
            displayAllScores(summaryInfo);
        } else {
            displayNoMatchFound();
        }
    }

    private void enableTypeToSearchFunctionality() {
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
    }

    private void displaySummary(Film summaryInfo) {
        setPoster(summaryInfo.getPoster());
        setHeader(summaryInfo.getTitle());
        setDetails(summaryInfo);
        setAverageRating(summaryInfo.getRating() * 10);
    }

    private void displayAllScores(Film summaryInfo) {
        mMeterList.removeAllViews();

        Hashtable<String, Double> allScores = summaryInfo.getAllScores();
        Enumeration<String> scoreEnumeration = allScores.keys();

        while (scoreEnumeration.hasMoreElements()) {
            String source = scoreEnumeration.nextElement();
            double score = allScores.get(source) * 10;

            ScoreMeter meter = new ScoreMeter(this);
            meter.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            meter.setScore((float) score);
            meter.setTitle(source);
            meter.refresh();

            mMeterList.addView(meter);
        }
    }

    private void displayNoMatchFound() {
        setPoster(null);
        setHeader(getString(R.string.empty_result));
        setDetails(null);
        setAverageRating(0);

        mMeterList.removeAllViews();
    }

    private void setPoster(Bitmap poster) {
        if (poster != null) {
            //mAverageMeter.setPoster(getResizedBitmap(poster, 60, 43));
            mPoster.setImageBitmap(poster);
        } else {
            //mAverageMeter.setPoster(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
             mPoster.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
        }
    }

    private void setHeader(String header) {
        mHeader.setText(header);
    }

    private void setDetails(Film summaryInfo) {
        if (summaryInfo != null) {
            String lineEnding = System.getProperty("line.separator");
            StringBuffer headerBuffer = new StringBuffer();
            headerBuffer.append(summaryInfo.getYear());
            headerBuffer.append(lineEnding);
            headerBuffer.append(summaryInfo.getCast());

            mDetails.setText(headerBuffer.toString());
        } else {
            mDetails.setText(null);
        }
    }

    private void setAverageRating(double averageRating) {
        if (averageRating == 0) {
            mAverageMeter.setScore(0);
        } else {
            mAverageMeter.setScore((float) averageRating);
        }

        mAverageMeter.refresh();
    }

    private void showProgress() {
        mProgressDialog = ProgressDialog.show(LookupActivity.this, "", "Searching and aggregating scores. Please wait...", true);
    }

    private void hideProgress() {
        mProgressDialog.hide();
    }

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

    //TODO: This should be moved to a helper class
    public Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
    }
}
