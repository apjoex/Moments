package com.x.memories.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.x.memories.R;
import com.x.memories.models.Post;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;

/**
 * Created by AKINDE-PETERS on 9/11/2016.
 */
public class ScreenSlideAdapter  extends PagerAdapter{

    private Context context;
    private ArrayList<Post> posts;
    private LayoutInflater inflater;

    // constructor
    public ScreenSlideAdapter(Context activity, ArrayList<Post> posts) {
        this.context = activity;
        this.posts = posts;
    }

//    public ScreenSlideAdapter(FragmentManager supportFragmentManager) {
//    }

    @Override
    public int getCount() {
        return posts.size();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        final ImageView image;
        final Button request_btn;
        final TextView caption_text;

        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View viewLayout = inflater.inflate(R.layout.fragment_photo_view, container, false);

        image = (ImageView) viewLayout.findViewById(R.id.image);
        request_btn = (Button) viewLayout.findViewById(R.id.request_btn);
        caption_text = (TextView)viewLayout.findViewById(R.id.caption_text);

        Post post = posts.get(position);

                if(post.getPrivacy()){

            request_btn.setVisibility(View.VISIBLE);
            caption_text.setVisibility(View.INVISIBLE);

            Glide.with(context)
                    .load(post.getUrl())
                    .placeholder(R.drawable.memories_grey_big)
                    .bitmapTransform(new BlurTransformation(context,50))
                    .into(image);
        }else{

            request_btn.setVisibility(View.INVISIBLE);
            caption_text.setVisibility(View.VISIBLE);
            caption_text.setText(post.getCaption());
            caption_text.setTypeface(null, Typeface.ITALIC);

            Glide.with(context)
                    .load(post.getUrl())
                    .placeholder(R.drawable.memories_grey_big)
                    .fitCenter()
                    .into(image);
        }


//        Glide.with(context)
//                .load(listingsPhoto.getUrl())
//                .placeholder(R.drawable.place_holder) // optional
//                .error(R.drawable.place_holder)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .into(imgDisplay);

//        Glide.with(_activity)
//                .load(listingsPhoto.getUrl())
//                .asBitmap()
//                .placeholder(R.drawable.place_holder)
//                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .asIs()
//                .into(new BitmapImageViewTarget(imgDisplay) {
//                    @Override
//                    protected void setResource(Bitmap resource) {
//                        RoundedBitmapDrawable circularBitmapDrawable = RoundedBitmapDrawableFactory.create(_activity.getResources(), resource);
//                        circularBitmapDrawable.setCircular(true);
//                        imgDisplay.setImageDrawable(circularBitmapDrawable);
//                    }
//                });
//
//        ImageLoader imageLoader = AppController.getInstance().getImageLoader();
//        imgDisplay.setImageUrl(listingsPhoto.getUrl(),imageLoader);

        // close button click event
//        btnClose.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ((Activity) _activity).finish();
//            }
//        });

        container.addView(viewLayout);

        return viewLayout;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return false;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((RelativeLayout) object);

    }
}
