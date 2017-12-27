package com.zhihu.matisse.ui;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.zhihu.matisse.R;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.model.SelectedItemCollection;
import com.zhihu.matisse.internal.ui.SelectedPreviewActivity;
import com.zhihu.matisse.internal.ui.adapter.PreviewPagerAdapter;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import java.util.ArrayList;
import java.util.List;

/**
 * 这个是底部添加了能够拖动的recyclerview的图片预览界面
 */
public class MySelectedPreviewActivity extends SelectedPreviewActivity
    implements FlexibleAdapter.OnItemClickListener, FlexibleAdapter.OnItemMoveListener,
    DragViewItem.onViewHolderReleased {
  List<DragViewItem> items = new ArrayList<>();
  private FlexibleAdapter adapter;
  private ArrayList<Item> selected;
  private RecyclerView recyclerView;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    recyclerView = new RecyclerView(this);
    RelativeLayout.LayoutParams layoutParams =
        new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT);
    layoutParams.addRule(RelativeLayout.ABOVE, R.id.bottom_toolbar);
    recyclerView.setLayoutParams(layoutParams);
    recyclerView.setBackgroundColor(getResources().getColor(R.color.zhihu_album_empty_view));
    recyclerView.setLayoutManager(
        new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    RelativeLayout parent = (RelativeLayout) mPager.getParent();
    parent.addView(recyclerView);

    Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
    selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
    items.clear();
    for (Item item : selected) {
      items.add(new DragViewItem(item, this));
    }
    adapter = new FlexibleAdapter(items);
    adapter.setMode(FlexibleAdapter.Mode.SINGLE);
    recyclerView.setAdapter(adapter);
    adapter.setLongPressDragEnabled(true).setHandleDragEnabled(true);
    adapter.addListener(this);
    adapter.addSelection(0);
  }

  @Override public void onPageSelected(int position) {
    super.onPageSelected(position);
    adapter.clearSelection();
    adapter.addSelection(position);
    adapter.notifyDataSetChanged();
    LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
    int lastVisibleItemPosition = layoutManager.findLastVisibleItemPosition();
    int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();
    if (position >= lastVisibleItemPosition || position <= firstVisibleItemPosition) {
      recyclerView.smoothScrollToPosition(position);
    }
  }

  @Override public boolean onItemClick(int position) {
    mPager.setCurrentItem(position);
    return false;
  }

  @Override public boolean shouldMoveItem(int fromPosition, int toPosition) {
    return true;
  }
  /**
   * 保存当前的ViewPage选中的条目，这里保存的不是pos而是item的HashCode,原因是{@link PreviewPagerAdapter#getItemId(int)}
   */
  long curItemHashCode;

  /**
   * 这里每次拖动超过一个条目的距离就会回调一次， 在一次拖动中会回调多次
   * @param fromPosition
   * @param toPosition
   */
  @Override public void onItemMove(int fromPosition, int toPosition) {
    Item item = selected.get(fromPosition);
    selected.remove(fromPosition);
    selected.add(toPosition, item);

  }

  /**
   * 这个方法会在拖动的开始时回调
   * @param viewHolder
   * @param actionState
   */
  @Override public void onActionStateChanged(RecyclerView.ViewHolder viewHolder, int actionState) {
    curItemHashCode = mAdapter.getItemId(mPager.getCurrentItem());
  }

  /**
   * 这个回调是通过{@link DragViewItem#DragViewItem(Item, DragViewItem.onViewHolderReleased)}传递到
   * {@link DragViewItem.DragViewViewHolder#DragViewViewHolder(View, FlexibleAdapter, DragViewItem.onViewHolderReleased)}
   *
   * 目的是为了能够在外部接收到拖动结束的事件
   * @param position
   */
  @Override public void onViewHolderReleased(int position) {
    mAdapter = new PreviewPagerAdapter(getSupportFragmentManager(), null);
    mAdapter.addAll(selected);
    mPager.setAdapter(mAdapter);
    mPager.setCurrentItem(mAdapter.getCurPositionByItemUriHashCode(curItemHashCode));


    //这里是为了保证选中的集合中与底部排序的顺序一致
    List<Item> items = mSelectedCollection.asList();
    ArrayList<Item> newSelected=new ArrayList<>(items.size());
    for(Item item:selected){
      if(mSelectedCollection.isSelected(item)){
        newSelected.add(item);
      }
    }
    mSelectedCollection.overwrite(newSelected,mSelectedCollection.getCollectionType());
  }
}
