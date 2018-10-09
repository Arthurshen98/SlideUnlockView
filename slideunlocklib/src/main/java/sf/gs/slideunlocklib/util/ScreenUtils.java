package sf.gs.slideunlocklib.util;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.inputmethod.InputMethodManager;

/**
 * 获取屏幕宽高
 */
public class ScreenUtils {

    public static int getWidth(Context activity){
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        return dm.widthPixels;
    }

    public static int getHeight(Context activity){
        DisplayMetrics dm = activity.getResources().getDisplayMetrics();

        return dm.heightPixels;
    }
    /**
     * 关闭软键盘
     */
    public static void closeKeybroad(Activity context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService (Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow (context.getWindow ().getDecorView ().getWindowToken (), 0);
        }
    }
}
