package de.rheinfabrik.mvvm_example.activities;

import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import butterknife.InjectView;
import butterknife.Views;
import de.rheinfabrik.mvvm_example.R;
import de.rheinfabrik.mvvm_example.activities.extras.DetailsActivityExtras;
import de.rheinfabrik.mvvm_example.utils.BlurTransformation;
import de.rheinfabrik.mvvm_example.viewmodels.DetailsViewModel;
import icepick.Icepick;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Activity responsible for displaying the details.
 */
public class DetailsActivity extends RxAppCompatActivity {

    // Members

    @InjectView(R.id.details_toolbar)
    protected Toolbar mToolbar;

    @InjectView(R.id.details_background)
    protected ImageView mImageView;

    @InjectView(R.id.details_plot)
    protected TextView mTextView;

    @InjectView(R.id.details_plot_card_view)
    protected CardView mCardView;

    private DetailsViewModel mViewModel;

    // Activity lifecycle

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // View
        setContentView(R.layout.activity_details);
        Views.inject(this);
        setSupportActionBar(mToolbar);

        // Back navigation
        mToolbar.setNavigationOnClickListener(x -> onBackPressed());

        // Style
        mToolbar.setTitleTextColor(getResources().getColor(R.color.white));
        mToolbar.setNavigationIcon(R.mipmap.ic_action_up);

        // View Model
        mViewModel = new DetailsViewModel();

        // Send item to view model
        DetailsActivityExtras extras = new DetailsActivityExtras();
        Icepick.restoreInstanceState(extras, getIntent().getExtras());
        mViewModel.setItemCommand.call(extras.searchResult);

        // Start loading the details
        mViewModel.loadDetailsCommand.call(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Bind title
        mViewModel.title()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mToolbar::setTitle);

        // Bind plot visibility
        mViewModel.plot()
                .map(text -> text == null || text.isEmpty() ? View.GONE : View.VISIBLE)
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mCardView::setVisibility);

        // Bind plot
        mViewModel.plot()
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(mTextView::setText);

        // Bind poster
        mViewModel.posterUrl()
                .map(url -> Picasso.with(getApplicationContext()).load(url).transform(new BlurTransformation(this)))
                .compose(bindToLifecycle())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(picasso -> {
                    picasso.into(mImageView);
                });
    }
}
