package toidiu.com.fieldnotebook.ui.extras;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * Created by toidiu on 4/2/16.
 */
public class DelayedTextWatcher implements TextWatcher {


    private final Handler mHandler;
    private final long mDelay;
    private final OnTextChanged mOnTextChanged;
    private String mLastUpdate;
    private final Runnable mTextUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            mOnTextChanged.onTextChanged(mLastUpdate);
        }
    };

    public DelayedTextWatcher(OnTextChanged onTextChanged, final long delay) {
        mHandler = new Handler(Looper.getMainLooper());
        mOnTextChanged = onTextChanged;
        mDelay = delay;
    }

    public static void addTo(EditText et, OnTextChanged onTextChanged, final long delay) {
        et.addTextChangedListener(new DelayedTextWatcher(onTextChanged, delay));
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        mLastUpdate = s.toString();
        mHandler.removeCallbacks(mTextUpdateRunnable);
        mHandler.postDelayed(mTextUpdateRunnable, mDelay);
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnTextChanged {
        void onTextChanged(String text);
    }
}
