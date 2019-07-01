package com.example.android.keepingcurrent.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.android.keepingcurrent.model.ArticleType;
import com.example.android.keepingcurrent.ui.CategoryFragment;

import static com.example.android.keepingcurrent.model.ArticleType.Type.types;

public class CategoryPagerAdapter extends FragmentPagerAdapter {

    public String[] fragmentTags = new String[getCount()];

    public CategoryPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        fragmentTags[position] = fragment.getTag();
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return ArticleType.Type.getName(types[position]);
    }

    @Override
    public Fragment getItem(int position) {

        return CategoryFragment.newInstance(types[position]);
    }

    @Override
    public int getCount() {
        return types.length;
    }
}
