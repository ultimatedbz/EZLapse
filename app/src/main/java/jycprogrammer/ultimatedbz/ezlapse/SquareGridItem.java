package jycprogrammer.ultimatedbz.ezlapse;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by jeffreychen on 9/4/15.
 */
public class SquareGridItem extends ImageView {

    public SquareGridItem(Context context) {
        super(context);
    }

    public SquareGridItem(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareGridItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec); // This is the key that will make the height equivalent to its width
    }
}