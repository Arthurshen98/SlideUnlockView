package sf.gs.slideunlockview;

import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import sf.gs.slideunlocklib.ISlideChangeListener;
import sf.gs.slideunlocklib.SlideLayout;
import sf.gs.slideunlocklib.renderers.ScaleRenderer;
import sf.gs.slideunlocklib.sliders.HorizontalSlider;
import sf.gs.slideunlocklib.textview.ShiningFontView;

public class MainActivity extends AppCompatActivity {

    ShiningFontView tvPickupPointName;
    ImageView ivSlideUnlockImg;
    FrameLayout slideChild;
    SlideLayout slideLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvPickupPointName = findViewById(R.id.tv_pickup_point_name);
        ivSlideUnlockImg = findViewById(R.id.iv_slide_unlock_img);
        slideChild = findViewById(R.id.slide_child);
        slideLayout = findViewById(R.id.slide_layout);

        //设置字
        tvPickupPointName.setText("开始滑动吧");

        slideUnlockInit();
    }

    private void slideUnlockInit() {

        //滚动标志动画开启
        AnimationDrawable drawable = (AnimationDrawable) ivSlideUnlockImg.getDrawable();
        drawable.start();

        slideLayout.setRenderer(new ScaleRenderer());
        slideLayout.setSlider(new HorizontalSlider());

        slideLayout.setChildId(R.id.slide_child);
        slideLayout.setThreshold(0.85f);
        slideLayout.addSlideChangeListener(new ISlideChangeListener() {
            @Override
            public void onSlideStart(SlideLayout slider) {
                slider.reset();
            }

            @Override
            public void onSlideChanged(SlideLayout slider, float percentage) {
                tvPickupPointName.setAlpha(1 - percentage);
            }

            @Override
            public void onSlideFinished(SlideLayout slider, boolean done) {
                if (done) {
                    slider.reset();
                    Toast.makeText(MainActivity.this, "滑动完成", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
