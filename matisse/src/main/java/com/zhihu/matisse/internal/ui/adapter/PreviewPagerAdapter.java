/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.internal.ui.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.ui.PreviewItemFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * 修改原有的的PageAdapter,添加两个额外的方法。
 */
public class PreviewPagerAdapter extends FragmentPagerAdapter {

    private ArrayList<Item> mItems = new ArrayList<>();
    private OnPrimaryItemSetListener mListener;

    public PreviewPagerAdapter(FragmentManager manager, OnPrimaryItemSetListener listener) {
        super(manager);
        mListener = listener;
    }

    @Override
    public Fragment getItem(int position) {
        return PreviewItemFragment.newInstance(mItems.get(position));
    }

    /**
     * 这个方法的作用是保证在底部拖动位置修改之后的ViewPager能够找到正确的Fragment.
     * 由于{@link FragmentPagerAdapter#instantiateItem(ViewGroup, int)}中的FragmentManager会查找之前创建过的Fragment,
     * 并且Fragment不能够重新加载,所以这里修改了查找Fragment的凭据，也就是fragmentName.
     *
     * 值得注意的是，这样处理了之后，当底部的view拖动，修改顺序之后，ViewPager是依然可以找到左右的Fragment
     * 但是！！！！！！！此时的ViewPager中的排列和当前的mItems的排列可能是不对应的，应该注意。
     * @param position
     * @return
     */
    @Override public long getItemId(int position) {
        return mItems.get(position).getContentUri().hashCode();
    }
    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (mListener != null) {
            mListener.onPrimaryItemSet(position);
        }
    }

    public Item getMediaItem(int position) {
        return mItems.get(position);
    }

    public void addAll(List<Item> items) {
        mItems.addAll(items);
    }

    interface OnPrimaryItemSetListener {

        void onPrimaryItemSet(int position);
    }

    /**
     * 由于位置的不对应，所以需要一个方法，根据需要显示的Item去获取应该设置的位置。
     * @param itemcode
     * @return
     */
    public int getCurPositionByItemUriHashCode(long itemcode){
        if(itemcode==0)return 0;
        for(int i=0;i<mItems.size();i++){
            if(itemcode==mItems.get(i).getContentUri().hashCode()){
                return i;
            }
        }
        return 0;
    }
}
