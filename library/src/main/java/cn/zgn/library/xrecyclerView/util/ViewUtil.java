package cn.zgn.library.xrecyclerView.util;

import android.content.Context;
import android.util.TypedValue;
import android.view.View;

import java.lang.reflect.Field;

public class ViewUtil {

    public static int[] getUnDisplayViewSize(View view) {
        int size[] = new int[2];
        int width = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int height = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        view.measure(width, height);
        int measuredHeight = view.getMeasuredHeight();
        int measuredWidth = view.getMeasuredWidth();
        if (measuredHeight == 0) {//equal 0 ? fuck mMeasuredHeight !
            try {
                Class<? extends View> aClass = view.getClass();
                Field mMeasuredHeight = aClass.getDeclaredField("mMeasuredHeight");
                mMeasuredHeight.setAccessible(true);
                measuredHeight = (int) mMeasuredHeight.get(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (measuredWidth == 0) {
            try {
                Class<? extends View> aClass = view.getClass();
                Field mMeasuredWidth = aClass.getDeclaredField("mMeasuredWidth");
                mMeasuredWidth.setAccessible(true);
                measuredWidth = (int) mMeasuredWidth.get(view);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        size[0] = measuredWidth;
        size[1] = measuredHeight;
        return size;
    }
    public static int dp2px(Context context ,float dpVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public static int sp2px(Context context ,float spVal){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getApplicationContext().getResources().getDisplayMetrics());
    }

    public static float px2dp(Context context ,float pxVal){
        final float scale = context.getApplicationContext().getResources().getDisplayMetrics().density;
        return (pxVal / scale);
    }

    public static float px2sp(Context context ,float pxVal){
        return (pxVal / context.getApplicationContext().getResources().getDisplayMetrics().scaledDensity);
    }

}
