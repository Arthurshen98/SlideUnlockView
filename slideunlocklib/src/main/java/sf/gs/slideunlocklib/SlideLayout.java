package sf.gs.slideunlocklib;

/*
 * Created by rom on 14.12.16.
 */

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.IdRes;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;


import java.util.LinkedHashSet;
import java.util.Set;

import sf.gs.slideunlocklib.renderers.TranslateRenderer;
import sf.gs.slideunlocklib.sliders.HorizontalSlider;
import sf.gs.slideunlocklib.util.ScreenUtils;

public class SlideLayout
        extends FrameLayout
        implements
        View.OnTouchListener,
        ISlidingData {

    public static final String TAG = SlideLayout.class.getSimpleName();

    private float mThreshold = 1.0f;

    private Set<ISlideChangeListener> mChangeListeners = new LinkedHashSet<>();
    private Set<ISlideListener> mSlideListeners = new LinkedHashSet<>();

    private boolean mStarted;

    private int mStartX;
    private int mStartY;

    private ISlider mSlider;
    private IRenderer mRenderer;

    private Dimen mParentStartDimen = new Dimen();
    private Rect mChildStartRect = new Rect();
    private Context context;

    @IdRes
    private int mChildId;
    private View mChild;

    private long mLockEventsTill = 0;

    private float mLastPercentage;

    private boolean mAllowEventsAfterFinishing;
    private boolean mFinished;

    // --- init

    public SlideLayout(Context context) {
        super(context);
        this.context = context;
        constructInit();
    }

    public SlideLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        constructInit();
    }

    public SlideLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        constructInit();
    }

    public void reset() {
        long diff = mLockEventsTill - System.currentTimeMillis();
        if (diff < 0) {
            doReset();
        } else {
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    doReset();
                }
            }, diff + 500);
        }
    }

    public void setAllowEventsAfterFinishing(boolean allow) {
        mAllowEventsAfterFinishing = allow;
    }

    private void doReset() {
        mLockEventsTill = System.currentTimeMillis() + mRenderer.onSlideReset(this, getChild());
    }

    private void constructInit() {
        setRenderer(new TranslateRenderer());
        setSlider(new HorizontalSlider());
    }
    // --- lifecycle

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        setOnTouchListener(this);
        if (mChildId == 0 && getChildCount() > 0) {
            mChild = getChildAt(0);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        setOnTouchListener(null);
        mRenderer.cancel();
    }

    // --- getters/setters

    public void setChildId(@IdRes int id) {
        mChildId = id;
        mChild = null;
    }

    public void setThreshold(float threshold) {
        mThreshold = threshold;
    }

    public View getChild() {
        if (null == mChild) {
            mChild = findViewById(mChildId);
        }
        return mChild;
    }

    public void setSlider(ISlider slider) {
        mSlider = slider;
    }

    public void setRenderer(IRenderer renderer) {
        mRenderer = renderer;
    }

    // --- listeners

    public void addSlideListener(ISlideListener listener) {
        mSlideListeners.add(listener);
    }

    public void removeSlideListener(ISlideListener listener) {
        mSlideListeners.remove(listener);
    }

    public void addSlideChangeListener(ISlideChangeListener listener) {
        mChangeListeners.add(listener);
    }

    public void removeSlideChangeListener(ISlideChangeListener listener) {
        mChangeListeners.remove(listener);
    }

    // --- ISlidingData impl.

    @Override
    public int getStartX() {
        return mStartX;
    }

    @Override
    public int getStartY() {
        return mStartY;
    }

    @Override
    public Rect getChildStartRect() {
        return mChildStartRect;
    }

    @Override
    public Dimen getParentDimen() {
        return mParentStartDimen;
    }


    // --- OnTouchListener impl.

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        int screenX = ScreenUtils.getWidth(context);
        int x = (int) motionEvent.getX();
        switch (motionEvent.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if (System.currentTimeMillis() < mLockEventsTill) {
                    return false;
                }
                if (mStarted) {
                    return false;
                }
                mStarted = canStart(motionEvent);
                if (mStarted) {
                    publishOnSlideStart();
                }
                return mStarted;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(motionEvent);
                return true;
            case MotionEvent.ACTION_UP:
                mStarted = false;
                handleFinishing(false);
                if (x > screenX * 8 / 10) {
                    new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            reset();
                        }
                    }, 1000);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                mStarted = false;
                handleFinishing(false);
                break;
        }
        return false;
    }

    private boolean canStart(MotionEvent motionEvent) {

        mStartX = (int) motionEvent.getX();
        mStartY = (int) motionEvent.getY();

        mParentStartDimen.width = getWidth();
        mParentStartDimen.height = getHeight();

        mChildStartRect.left = getChild().getLeft();
        mChildStartRect.right = getChild().getRight();
        mChildStartRect.top = getChild().getTop();
        mChildStartRect.bottom = getChild().getBottom();

        if (!mSlider.allowStart(this)) {
            return false;
        }
        mFinished = false;
        return true;
    }

    private void handleActionMove(MotionEvent motionEvent) {
        if (!mStarted) {
            return;
        }
        if (mAllowEventsAfterFinishing && mFinished && System.currentTimeMillis() > mLockEventsTill) {
            return;
        }

        int screenX = ScreenUtils.getWidth(context);

        int x = (int) motionEvent.getX();
        int y = (int) motionEvent.getY();

        float percentage = mSlider.getPercentage(this, x, y);
        if (percentage < 0) {
            percentage = 0;
        }
        if (percentage >= 1 && !mAllowEventsAfterFinishing) {
            percentage = 0.9f;
        }
        mLastPercentage = percentage;

        Point transformedXY = mSlider.getTransformedPosition(this, percentage, x, y);

        mRenderer.renderChanges(this, getChild(), percentage, transformedXY);

        publishOnSlideChanged(percentage);

        if (percentage >= mThreshold ) {
            handleFinishing(true);
        }

    }

    private void handleFinishing(final boolean done) {
        if (mFinished) {
            reset();
            return;
        }
        mFinished = true;
        if (done) {
            if (!mAllowEventsAfterFinishing) {
                mStarted = false;
            }
            mLockEventsTill = System.currentTimeMillis() + mRenderer.onSlideDone(this, getChild());
        } else {
            mStarted = false;
            mLockEventsTill = System.currentTimeMillis() + mRenderer.onSlideCancelled(this, getChild(), mLastPercentage);
        }

        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                publishOnSlideFinished(done);
            }
        }, 250);

    }

    private void publishOnSlideStart() {
        for (ISlideChangeListener listener : mChangeListeners) {
            listener.onSlideStart(this);
        }
    }

    @Override
    public void publishOnSlideChanged(float percentage) {
        for (ISlideChangeListener listener : mChangeListeners) {
            listener.onSlideChanged(this, percentage / mThreshold);
        }
    }

    private void publishOnSlideFinished(boolean done) {
        for (ISlideChangeListener listener : mChangeListeners) {
            listener.onSlideFinished(this, done);
        }
        for (ISlideListener listener : mSlideListeners) {
            listener.onSlideDone(this, done);
        }
        mRenderer.onSlideReset(this, getChild());
    }

}
