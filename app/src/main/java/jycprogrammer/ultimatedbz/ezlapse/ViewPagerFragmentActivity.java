package jycprogrammer.ultimatedbz.ezlapse;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by jeffreychen on 8/31/15.
 *
 *  Used to swipe between photos in the Grid View
 *
 */
public class ViewPagerFragmentActivity extends FragmentActivity {
    public static final String EXTRA_LAPSE_ID = "The ID of the lapse clicked";
    public static final String EXTRA_LAPSE_PHOTO_POSITION = "The position of the photo clicked";
    private UUID mLapseId;
    private ArrayList<Photo> mPhotoGallery;
    /** maintains the pager adapter*/
    private ImagePagerAdapter mPagerAdapter;

    /* (non-Javadoc)
     * @see android.support.v4.app.FragmentActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getIntent().getExtras()!=null &&
                getIntent().getExtras().containsKey(EXTRA_LAPSE_ID)&&
                getIntent().getExtras().containsKey(EXTRA_LAPSE_PHOTO_POSITION)){

            mLapseId = (UUID) getIntent().getExtras().getSerializable(EXTRA_LAPSE_ID);
            mPhotoGallery = LapseGallery.get(getApplicationContext()).
                    getLapse(mLapseId).getPhotos();
        }else{
            finish();
        }

        super.setContentView(R.layout.viewpager_layout);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        ImagePagerAdapter adapter = new ImagePagerAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem((int)getIntent().getExtras().getSerializable(EXTRA_LAPSE_PHOTO_POSITION));
}

    private class ImagePagerAdapter extends PagerAdapter {


        @Override
        public int getCount() {
            return mPhotoGallery.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == ((ImageView) object);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Context context = ViewPagerFragmentActivity.this;
            ImageView imageView = new ImageView(context);
            /*int padding = context.getResources().getDimensionPixelSize(
                    R.dimen.padding_medium);
            imageView.setPadding(padding, padding, padding, padding);
            */
            imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            //BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            //Bitmap bitmap = BitmapFactory.decodeFile(mPhotoGallery.get(position).getFilePath(),bmOptions);
          //  bitmap = Bitmap.createScaledBitmap(bitmap,parent.getWidth(),parent.getHeight(),true);
            //imageView.setImageBitmap(bitmap);
            int height = getResources().getDisplayMetrics().heightPixels;
            int width = getResources().getDisplayMetrics().heightPixels;
            imageView.setImageBitmap(LapseGridActivity.
                    decodeSampledBitmapFromResource(mPhotoGallery.get(position)
                    .getFilePath(),width, height));
            //imageView.setImageResource(mPhotoGallery.get(position));
            ((ViewPager) container).addView(imageView, 0);

            imageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });

            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            ((ViewPager) container).removeView((ImageView) object);
        }
    }
}