package stream.custompopup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

public class CustomPopupWindow extends PopupWindow {

    private final int animDuration = 250;
    //Animation direction constants, automatically calculated
    private final int TOP = 0;
    private final int BOTTOM = 1;
    private int direction = 0;
    private int arrowX = 0;
    private boolean isAnimating; //Prevent popup window from being dismissed why animating.
    private boolean animateDismiss = false; //Flag to keep track of dismiss animation.

    private WindowManager.LayoutParams params;
    private boolean isShow;
    private WindowManager windowManager;
    private ViewGroup rootView;
    private ViewGroup relativeLayout;
    private ViewGroup bubbleContainer;
    private ImageView bubbleArrow;
    private Context mContext;

    public CustomPopupWindow(Context context){

        mContext = context;
        windowManager = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);

        setFocusable(true);
        setOutsideTouchable(false);
        setClippingEnabled(false);
    }

    public void initLayout(int layout){

        rootView = (ViewGroup) View.inflate(mContext, R.layout.item_popup, null);
        relativeLayout = (ViewGroup) rootView.findViewById(R.id.relativeLayout);
        bubbleContainer = (ViewGroup) rootView.findViewById(R.id.bubble_container);
        bubbleArrow = (ImageView) rootView.findViewById(R.id.bubble_arrow);
        View.inflate(mContext, layout, bubbleContainer);

        setContentView(rootView);
    }

    public void showPopupWindow(View targetView){

        animateDismiss = false;
        if(!isAnimating) {
            isAnimating = true;

            //TargetView measurements.
            int[] arr = new int[2];
            targetView.getLocationOnScreen(arr);
            int targetX = arr[0];
            int targetY = arr[1];
            int targetWidth = targetView.getWidth();
            int targetHeight = targetView.getHeight();

            //Screen dimensions.
            Rect frame = new Rect();
            ((Activity) mContext).getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
            int screenWidth = frame.width();
            int screenHeight = frame.height();

            //Popup Window measurements.
            relativeLayout.measure(0, 0);
            int popupWidth = relativeLayout.getMeasuredWidth();
            int popupHeight = relativeLayout.getMeasuredHeight();

            Log.d("Target X", String.valueOf(targetX));
            Log.d("Target Y", String.valueOf(targetY));
            Log.d("Frame Top", String.valueOf(frame.top));
            Log.d("Frame Bottom", String.valueOf(frame.bottom));
            Log.d("Popup Height", String.valueOf(popupHeight));
            Log.d("ScreenWidth", String.valueOf(screenWidth));
            Log.d("ScreenHeight", String.valueOf(screenHeight));
            Log.d("Calc", String.valueOf(screenHeight - targetY));
            Log.d("Navigation Bar", String.valueOf(getNavigationBarHeight()));

            int popupX;
            int popupY;
            int bubbleArrowSize = dpToPx(mContext, 20);

            //Calculate vertical position. Position above or below target view.
            int calcHeight = frame.height() - getNavigationBarHeight() + frame.top;
            if (calcHeight - targetY < popupHeight)
            {
                //Position arrow logic
                //Switch arrow drawable to match elevation appearance and direction.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    bubbleArrow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bubble_arrow));
                }
                else
                {
                    bubbleArrow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bubble_arrow));
                }
                //Reset rule to prevent circular dependencies.
                RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                relativeParams.addRule(RelativeLayout.BELOW, 0);
                bubbleContainer.setLayoutParams(relativeParams);
                RelativeLayout.LayoutParams relativeParams1 = new RelativeLayout.LayoutParams(bubbleArrowSize, bubbleArrowSize);
                relativeParams1.addRule(RelativeLayout.BELOW, bubbleContainer.getId());
                bubbleArrow.setLayoutParams(relativeParams1);
                relativeLayout.updateViewLayout(bubbleArrow, relativeParams1);
                relativeLayout.setClipChildren(false);
                //Set popup Y position.
                popupY = targetY - popupHeight;
                direction = BOTTOM;
            }
            else
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    bubbleArrow.setBackground(ContextCompat.getDrawable(mContext, R.drawable.bubble_arrow_top));
                }
                else
                {
                    bubbleArrow.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.bubble_arrow_top));
                }
                RelativeLayout.LayoutParams relativeParams = new RelativeLayout.LayoutParams(bubbleArrowSize, bubbleArrowSize);
                relativeParams.addRule(RelativeLayout.BELOW, 0);
                bubbleArrow.setLayoutParams(relativeParams);
                RelativeLayout.LayoutParams relativeParams1 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
                relativeParams1.addRule(RelativeLayout.BELOW, bubbleArrow.getId());
                bubbleContainer.setLayoutParams(relativeParams1);
                relativeLayout.updateViewLayout(bubbleContainer, relativeParams1);
                relativeLayout.setClipChildren(false);
                popupY = targetY + targetHeight;
                direction = TOP;
            }

            //Calculate horizontal position to position arrow in middle of target view.
            //If targetview is wider than popup window, set arrow to middle of popup window.
            int targetCalcWidth = targetWidth;
            if (targetWidth > popupWidth)
            {
                targetCalcWidth = popupWidth;
            }
            int calcX = targetX + targetWidth - popupWidth;
            if (calcX < 0)
            {
                popupX = 0;
                arrowX = targetX + targetCalcWidth/2 - bubbleArrowSize/2;
            }
            else
            {
                popupX = calcX;
                arrowX = popupWidth - targetCalcWidth/2 - bubbleArrowSize/2;
            }
            bubbleArrow.setX(arrowX);

            showAtLocation(targetView, Gravity.NO_GRAVITY, popupX, popupY);
            showAnimation(relativeLayout, 0, 1, animDuration, direction, arrowX, true);
        }
    }

    private void showAnimation(final View view, float start, final float end, int duration, final int direction, final int xposition, final boolean isWhile) {

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                int pivotY = 0;
                switch (direction)
                {
                    case TOP:
                        //Pivot Y = 0;
                        break;
                    case BOTTOM:
                        pivotY = view.getHeight();
                        break;
                }
                view.setPivotX(xposition);
                view.setPivotY(pivotY);
                view.setScaleX(value);
                view.setScaleY(value);
            }
        });
        //2nd bounce animation.
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (isWhile) {
                    showAnimation(view, end, 0.95f, animDuration / 3, direction, arrowX, false);
                }else{
                    isAnimating = false;
                }
            }
        });
        va.start();
    }

    @Override
    public void dismiss() {

        //Run hide animation first. Then hide when animation is finished.
        if(animateDismiss && !isAnimating) {
            super.dismiss();
        }
        else
        {
            hideAnimation(relativeLayout, 0.95f, 1, animDuration / 3, direction, arrowX, true);
        }
    }

    public void hideAnimation(final View view, float start, final float end, int duration, final int direction, final int xposition, final boolean isWhile){

        ValueAnimator va = ValueAnimator.ofFloat(start, end).setDuration(duration);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                int pivotY = 0;
                switch (direction)
                {
                    case TOP:
                        //Pivot Y = 0;
                        break;
                    case BOTTOM:
                        pivotY = view.getHeight();
                        break;
                }
                view.setPivotX(xposition);
                view.setPivotY(pivotY);
                view.setScaleX(value);
                view.setScaleY(value);
            }
        });
        //2nd bounce animation.
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if(isWhile){
                    hideAnimation(view, end, 0f, animDuration, direction, arrowX, false);
                }else{
                    animateDismiss = true;
                    isAnimating = false;
                    try {
                        dismiss();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
        va.start();
    }

    public WindowManager.LayoutParams getLayoutParams(){
        return params;
    }

    public ViewGroup getLayout(){
        return relativeLayout;
    }

    public boolean isShow(){
        return isShow;
    }

    //Display Utils.
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = mContext.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public int getNavigationBarHeight()
    {
        boolean hasMenuKey = ViewConfiguration.get(mContext).hasPermanentMenuKey();
        int resourceId = mContext.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0 && !hasMenuKey)
        {
            return mContext.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static int dpToPx(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
