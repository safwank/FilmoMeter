package safwan.filmometer;

import android.app.Activity;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
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
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;
import safwan.filmometer.aggregator.RatingAggregator;
import safwan.filmometer.data.Film;
import safwan.filmometer.views.ScoreMeter;

public class LookupActivity extends Activity {
    private ScoreMeter mMeter;
    private TextView mHeader;
    private TextView mDetails;
    private ProgressDialog mProgressDialog;

    private RatingAggregator mAggregator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lookup);

        mMeter = (ScoreMeter) findViewById(R.id.meter);
        mHeader = (TextView) findViewById(R.id.header);
        mDetails = (TextView) findViewById(R.id.details);

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
            setHeader(summaryInfo.getTitle());
            setDetails(summaryInfo);
            setAverageRating(summaryInfo.getRating() * 10);
        } else {
            displayNoMatchFound();
        }

        refreshMeter();
    }

    private void displayNoMatchFound() {
        setPoster(null);
        setHeader(getString(R.string.empty_result));
        setDetails(null);
        setAverageRating(0);
    }

    private void setPoster(Bitmap poster) {
        if (poster != null) {
            mMeter.setPoster(getResizedBitmap(poster, 60, 43));
        } else {
            mMeter.setPoster(BitmapFactory.decodeResource(getResources(), R.drawable.icon));
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
            mMeter.setScore(0);
        } else {
            mMeter.setScore((float) averageRating);
        }
    }

    private void refreshMeter() {
        mMeter.refresh();
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

    public void setLayoutAnim_slidedown(ViewGroup panel, Context ctx) {

        AnimationSet set = new AnimationSet(true);

        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0.0f);
        animation.setDuration(800);
        animation.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub
                // MapContacts.this.mapviewgroup.setVisibility(View.VISIBLE);

            }

            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            public void onAnimationEnd(Animation animation) {

                // TODO Auto-generated method stub

            }
        });
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(
                set, 0.25f);
        panel.setLayoutAnimation(controller);
    }

    public void setLayoutAnim_slideup(ViewGroup panel, Context ctx) {

        AnimationSet set = new AnimationSet(true);

        /*
           * Animation animation = new AlphaAnimation(1.0f, 0.0f);
           * animation.setDuration(200); set.addAnimation(animation);
           */

        Animation animation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, -1.0f);
        animation.setDuration(800);
        animation.setAnimationListener(new Animation.AnimationListener() {

            public void onAnimationStart(Animation animation) {
                // TODO Auto-generated method stub

            }

            public void onAnimationRepeat(Animation animation) {
                // TODO Auto-generated method stub

            }

            public void onAnimationEnd(Animation animation) {
                // MapContacts.this.mapviewgroup.setVisibility(View.INVISIBLE);
                // TODO Auto-generated method stub

            }
        });
        set.addAnimation(animation);

        LayoutAnimationController controller = new LayoutAnimationController(
                set, 0.25f);
        panel.setLayoutAnimation(controller);
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
