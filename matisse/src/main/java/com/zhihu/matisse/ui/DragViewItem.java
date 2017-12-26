package com.zhihu.matisse.ui;

import android.content.Context;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;
import com.zhihu.matisse.R;
import com.zhihu.matisse.engine.impl.PicassoEngine;
import com.zhihu.matisse.internal.entity.Item;
import com.zhihu.matisse.internal.entity.SelectionSpec;
import com.zhihu.matisse.internal.utils.PhotoMetadataUtils;
import com.zhihu.matisse.internal.utils.UIUtils;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractFlexibleItem;
import eu.davidea.viewholders.FlexibleViewHolder;
import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;
import java.util.List;

/**
 * Created by huangbaole on 2017/12/25.
 */

public class DragViewItem extends AbstractFlexibleItem<DragViewItem.DragViewViewHolder> {
  Item item;
  onViewHolderReleased released;

  public DragViewItem(Item item, onViewHolderReleased listener) {
    this.item = item;
    this.released = listener;
  }

  @Override public boolean equals(Object o) {
    return false;
  }

  @Override public int getLayoutRes() {
    return R.layout.item_imageview;
  }

  @Override public DragViewViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
    return new DragViewViewHolder(view, adapter, released);
  }

  @Override
  public void bindViewHolder(FlexibleAdapter adapter, final DragViewViewHolder holder, int position,
      List payloads) {
    Context context = holder.imageView.getContext();
    if (item.isGif()) {
      SelectionSpec.getInstance().imageEngine.loadGifImage(context, 55, 50, holder.imageView,
          item.getContentUri());
    } else {
      holder.imageView.getViewTreeObserver()
          .addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override public void onGlobalLayout() {
              holder.imageView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
              SelectionSpec.getInstance().imageEngine.loadImage(holder.imageView.getContext(),
                  holder.imageView.getWidth(), holder.imageView.getHeight(), holder.imageView,
                  item.getContentUri());
            }
          });
    }
    if (adapter.isSelected(position)) {
      holder.imageView.setBackgroundResource(R.drawable.square);
      int i = UIUtils.dip2px(context, 1.5f);
      holder.imageView.setPadding(i, i, i, i);
    } else {
      holder.imageView.setBackgroundResource(0);
      holder.imageView.setPadding(0, 0, 0, 0);
    }
  }

  public interface onViewHolderReleased {
    void onViewHolderReleased(int position);
  }

  @Override public boolean isDraggable() {
    return true;
  }

  public static class DragViewViewHolder extends FlexibleViewHolder {
    ImageView imageView;
    onViewHolderReleased viewHolderReleased;

    public DragViewViewHolder(View view, FlexibleAdapter adapter, onViewHolderReleased listener) {
      super(view, adapter);
      imageView = (ImageView) view;
      this.viewHolderReleased = listener;
    }

    /**
     * 拖动结束的回调，修改当前的UI
     * @param position
     */
    @Override public void onItemReleased(int position) {
      super.onItemReleased(position);
      imageView.animate().scaleY(1).scaleX(1).setDuration(200).start();
      viewHolderReleased.onViewHolderReleased(position);
    }

    /**
     * 拖动开始的回调，修改当前UI
     * @param position
     * @param actionState
     */
    @Override public void onActionStateChanged(int position, int actionState) {
      super.onActionStateChanged(position, actionState);
      imageView.animate().scaleX(1.3f).scaleY(1.3f).setDuration(200).start();
    }
  }
}
