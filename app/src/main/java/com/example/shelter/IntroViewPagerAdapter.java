package com.example.shelter;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewpager.widget.PagerAdapter;

import java.util.List;

public class IntroViewPagerAdapter extends PagerAdapter {

    Context mContext;
    List<screenItem> mList;

    public IntroViewPagerAdapter(Context mContext, List<screenItem> mList) {
        this.mContext = mContext;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view==object;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View)object);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater inflater=(LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layoutScreen=inflater.inflate(R.layout.layout_screen,null);
        AppCompatImageView image=layoutScreen.findViewById(R.id.intro_image);
        AppCompatTextView title=layoutScreen.findViewById(R.id.title_txt);
        AppCompatTextView description=layoutScreen.findViewById(R.id.description_txt);
        image.setImageResource(mList.get(position).getScreenImage());
        title.setText(mList.get(position).getTitle());
        description.setText(mList.get(position).getDescription());

        container.addView(layoutScreen);
        return  layoutScreen;
    }
}
