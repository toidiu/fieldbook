package toidiu.com.fieldnotebook.utils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import toidiu.com.fieldnotebook.R;

/**
 * Created by toidiu on 4/12/16.
 */
public class BgImageLoader implements Target {
    private Resources resources;
    private View layout;
    private boolean animate = false;

    public BgImageLoader(Resources resources, View layout) {
        this.resources = resources;
        this.layout = layout;
    }

    public BgImageLoader(Resources resources, View layout, boolean animate) {
        this.resources = resources;
        this.layout = layout;
        this.animate = animate;
    }

    @Override
    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
        layout.setBackground(new BitmapDrawable(resources, bitmap));
        if (animate) {
            layout.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500);
        }
    }

    @Override
    public void onBitmapFailed(Drawable errorDrawable) {
        Log.d("error-------", errorDrawable.toString());
        layout.setBackgroundColor(resources.getColor(R.color.colorPrimary));
        if (animate) {
            layout.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(500);
        }
    }

    @Override
    public void onPrepareLoad(Drawable placeHolderDrawable) {

    }
}
