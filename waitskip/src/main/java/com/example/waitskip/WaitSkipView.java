package com.example.waitskip;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.StringRes;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TextSwitcher;
import android.widget.TextView;

import java.util.Locale;

/**
 * Braindigit
 * Created on 1/4/17.
 */

public class WaitSkipView extends RelativeLayout {
    private static final String COLOR_GREEN = "#FF3FB54F";
    private static final String SECOND_FORMATER = "%d s";
    private static final long DEFAULT_SKIP_AFTER_MILLIS = 5000;
    private static final long DEFAULT_TIMER_INTERVAL = 500;

    private NumberAnimatorView timerTextView;
    private TextSwitcher action;
    private CountDownTimer timer;
    private OnSkipActionListener skipActionListener;
    private TimerFinishListener timerFinishListener;
    private String initialText;
    private String completionText;
    private boolean isComplete;

    public WaitSkipView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) return;
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_wait_skip, this);
        setClipChildren(true);
        setLayoutTransition(new LayoutTransition());
        timerTextView = (NumberAnimatorView) findViewById(R.id.numberAnimator);
        action = (TextSwitcher) findViewById(R.id.completeAction);

//        Text fade out
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setDuration(500);
        action.setOutAnimation(fadeOut);
//        Fade in
        Animation fadeIn = new AlphaAnimation(0, 1);
        fadeIn.setDuration(1000);
        action.setInAnimation(fadeIn);

        action.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (skipActionListener != null)
                    skipActionListener.onSkip();
            }
        });

        ShapeDrawable timerCircle = drawOval(true, timerTextView.getWidth(), timerTextView.getHeight(),
                Color.parseColor(COLOR_GREEN));
        GradientDrawable actionOval = drawRoundedRectangle(Color.parseColor(COLOR_GREEN));


        if (Build.VERSION.SDK_INT >= 16) {
            findViewById(R.id.timerFrame).setBackground(timerCircle);
            action.setBackground(actionOval);
        } else {
            findViewById(R.id.timerFrame).setBackgroundDrawable(timerCircle);
            action.setBackgroundDrawable(actionOval);
        }
    }

    public void setCompletionText(@StringRes int initial, @StringRes int onCompletion) {
        this.initialText = getContext().getString(initial);
        this.completionText = getContext().getString(onCompletion);
        setActionText();
    }

    public void setCompletionText(String initialText, String completionText) {
        this.initialText = initialText;
        this.completionText = completionText;
        setActionText();
    }

    private void setActionText() {
        String text = isComplete ? completionText : initialText;
        action.setText(text == null ? "" : text);
    }

    public void start() {
        start(DEFAULT_SKIP_AFTER_MILLIS, DEFAULT_TIMER_INTERVAL);
    }

    public void start(long duration) {
        start(duration, DEFAULT_TIMER_INTERVAL);
    }

    public void start(long duration, long interval) {
        if (timer != null) {
            timer.cancel();
            timer = null;
            start(duration, interval);
        }
        this.isComplete = false;
        setActionText();
        animateAction(-100, 0);
        timerTextView.setVisibility(VISIBLE);
        action.setVisibility(VISIBLE);
        if (timer == null) {
            timer = new CountDownTimer(duration, interval) {
                int currentSecs;

                @Override
                public void onTick(long millisUntilFinished) {
                    int remainingSecs = (int) (millisUntilFinished / 1000L);
                    if (currentSecs == remainingSecs) return;
                    timerTextView.animate(currentSecs, remainingSecs).setDuration(300).start();
                    currentSecs = remainingSecs;
                }

                @Override
                public void onFinish() {
                    isComplete = true;
                    timerTextView.setVisibility(INVISIBLE);
//                    ObjectAnimator.ofFloat(timerTextView, "alpha", 1, 0).setDuration(1000).start();
                    setActionText();
                    if (timerFinishListener != null)
                        timerFinishListener.onFinish();
                    timer = null;
                }

            }.start();
        }
    }

    public void cancel() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    public void setSkipActionListener(OnSkipActionListener listener) {
        this.skipActionListener = listener;
    }

    public void setTimerFinishListener(TimerFinishListener listener) {
        this.timerFinishListener = listener;
    }

    public void setSimpleWaitlSkipListener(SimpleWaitSkipListener listener) {
        this.timerFinishListener = listener;
        this.skipActionListener = listener;
    }

    public interface OnSkipActionListener {
        void onSkip();
    }

    public interface TimerFinishListener {
        void onFinish();
    }

    public static abstract class SimpleWaitSkipListener implements OnSkipActionListener, TimerFinishListener {
        @Override
        public void onFinish() {
//            Ignored
        }

        @Override
        public void onSkip() {
//            Ignored
        }
    }

    private void animateAction(float fromXDelta, float toXDelta) {
        TranslateAnimation animation = new TranslateAnimation(fromXDelta, toXDelta, 0, 0);
        animation.setFillAfter(true);
        animation.setDuration(500);
        action.startAnimation(animation);
    }

    private Animation animateAlpha(float fromAlpha, float toAlpha, long duration, Interpolator interpolator) {
        Animation alpha = new AlphaAnimation(fromAlpha, toAlpha);
        alpha.setInterpolator(interpolator); //add this
        alpha.setDuration(duration);
        return alpha;
    }

    private void startSet(View view, Animation... animations) {
        AnimationSet animation = new AnimationSet(false); //change to false
        for (Animation animation1 : animations) {
            animation.addAnimation(animation1);
        }
        animation.setFillAfter(true);
        animation.setFillBefore(true);
        view.startAnimation(animation);
    }

    private static ShapeDrawable drawOval(boolean fill, int width, int height, int color) {
        ShapeDrawable oval = new ShapeDrawable(new OvalShape());
        oval.setIntrinsicHeight(height);
        oval.setIntrinsicWidth(width);
        oval.getPaint().setStyle(fill ? Paint.Style.FILL : Paint.Style.STROKE);
        oval.getPaint().setColor(color);
        return oval;
    }

    private static GradientDrawable drawRoundedRectangle(int color) {
        GradientDrawable gdDefault = new GradientDrawable();
        gdDefault.setColor(color);
        gdDefault.setCornerRadius(16);
        return gdDefault;
    }
}