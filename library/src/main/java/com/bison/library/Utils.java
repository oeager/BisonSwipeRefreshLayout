package com.bison.library;

import android.content.Context;
import android.util.TypedValue;

/**
 * Created by oeager on 2015/8/27 0027.
 * email: oeager@foxmail.com
 */
public class Utils {

    public static int dp2Px(Context context,int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }
}
