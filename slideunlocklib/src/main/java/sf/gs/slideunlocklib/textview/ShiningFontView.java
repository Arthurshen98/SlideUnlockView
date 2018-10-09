package sf.gs.slideunlocklib.textview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import sf.gs.slideunlocklib.R;


/**
 * Created by Administrator on 2017/11/24.
 * 字体滚动发亮
 */

@SuppressLint("AppCompatCustomView")
public class ShiningFontView extends TextView {

    private Paint mPaint;
    private LinearGradient mLinearGradient;
    private Matrix mGradientMatrix;
    private int mViewWidth;
    private int mTranslate;
    //可以修改字体滚动颜色
    private int startColor = Color.WHITE, centerColor = 0xff0096FF, endColor = Color.WHITE;

    public ShiningFontView(Context context) {
        super(context);
    }

    public ShiningFontView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ShiningFontView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        //初始化属性
        TypedArray mTypedArray = context.obtainStyledAttributes(attrs, R.styleable.ShiningFontView);

        startColor = mTypedArray.getColor(R.styleable.ShiningFontView_sfv_startColor, startColor);
        centerColor = mTypedArray.getColor(R.styleable.ShiningFontView_sfv_centerColor, centerColor);
        endColor = mTypedArray.getColor(R.styleable.ShiningFontView_sfv_endColor, endColor);

        mTypedArray.recycle();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
        if (mViewWidth == 0) {
            mViewWidth = getMeasuredWidth();
            if (mViewWidth > 0) {
                mPaint = getPaint();
                /**
                 * LinearGradient源码秒速：
                 * 创建一个在直线上绘制线性渐变的着色器
                 * @param x0  梯度线起始点的x坐标
                 * @param y0  梯度线起始点的y坐标
                 * @param x1  梯度线末端的x坐标
                 * @param y1  梯度线末端的y坐标
                 * @param  colors  沿梯度线分布的颜色
                 * @param  positions   可能是空的。相对位置(0 . .1)每个相应颜色的颜色数组。如果这是null，那么颜色就会沿着梯度线均匀分布。
                 * @param  tile   着色器的模式
                 *
                 * LinearGradient构造方法中的参数int[] color：
                 * 第一个元素：发光字体闪过后所显示的字体颜色，这里给定与第三个元素一样
                 * 第二个元素：字体发光的颜色
                 * 第三个元素：原字体显示的颜色
                 *
                 * mViewWidth：设置发光的宽度
                 * */
                mLinearGradient = new LinearGradient(0, 0, mViewWidth, 0, new int[]{startColor, centerColor, endColor},null, Shader.TileMode.CLAMP);
                mPaint.setShader(mLinearGradient);
                mPaint.setAlpha(1);
                mGradientMatrix = new Matrix();
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mGradientMatrix != null) {
            mTranslate += mViewWidth / 5;
            if (mTranslate > 2 * mViewWidth) {
                mTranslate = -mViewWidth;
            }
            mGradientMatrix.setTranslate(mTranslate, 0);
            mLinearGradient.setLocalMatrix(mGradientMatrix);
            //. 控制闪过的时间
            postInvalidateDelayed(150);
        }

    }

}
