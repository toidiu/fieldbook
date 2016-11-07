package toidiu.com.fieldnotebook.utils;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import toidiu.com.fieldnotebook.R;
import toidiu.com.fieldnotebook.models.FileObj;
import toidiu.com.fieldnotebook.ui.extras.FileClickInterface;

/**
 * Created by toidiu on 4/21/16.
 */
public class UtilsView {

    public static PopupMenu fileClickPopup(final View anchor, final FileObj fileObj, final FileClickInterface listener) {
        anchor.setSelected(true);
        PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (listener != null) {

                    switch (menuItem.getItemId()) {
                        case R.id.share:
                            listener.shareClicked(fileObj);
                            return true;
                        case R.id.multi_share:
                            listener.multiShareClicked(fileObj);
                            return true;
                        case R.id.print:
                            listener.printClicked(fileObj);
                            return true;
                        case R.id.duplicate:
                            listener.duplicateClicked(fileObj);
                            return true;
                    }
                }
                return false;
            }
        });
        menu.inflate(R.menu.file_click_menu);
        menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {
                anchor.setSelected(false);
            }
        });
        menu.show();
        return menu;
    }

    public static PopupMenu fileLongClickPopup(final View anchor, final FileObj fileObj, final FileClickInterface listener) {
        anchor.setSelected(true);
        PopupMenu menu = new PopupMenu(anchor.getContext(), anchor);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                if (listener != null) {

                    switch (menuItem.getItemId()) {
                        case R.id.rename:
                            listener.renameLongClicked(fileObj);
                            return true;
                        case R.id.move:
                            listener.moveLongClicked(fileObj);
                            return true;
                        case R.id.delete:
                            listener.deleteLongClicked(fileObj);
                            return true;
                    }
                }
                return false;
            }
        });
        menu.inflate(R.menu.file_long_click_menu);
        menu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu popupMenu) {
                anchor.setSelected(false);
            }
        });
        menu.show();
        return menu;
    }

    public static int dpToPx(Resources r, float dpVal){
        int px = (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dpVal, r.getDisplayMetrics()
        );
        return px;
    }

    public static void setMargins (View v, int l, int t, int r, int b) {
        if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
            ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
            p.setMargins(l, t, r, b);
            v.requestLayout();
        }
    }

}
