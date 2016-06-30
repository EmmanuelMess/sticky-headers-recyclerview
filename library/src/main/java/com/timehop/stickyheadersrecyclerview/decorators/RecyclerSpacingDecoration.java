package com.timehop.stickyheadersrecyclerview.decorators;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.DimenRes;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

/**
 * Created by Rafael Baboni Dominiquini on 30/06/16.
 */
public class RecyclerSpacingDecoration extends RecyclerView.ItemDecoration {

  private int orientation = -1;
  private int spanCount = -1;

  private int fullSpacing;
  private int halfSpacing;

  public RecyclerSpacingDecoration(Context context, @DimenRes int spacingDimen) {

    this(context.getResources().getDimensionPixelSize(spacingDimen));
  }

  public RecyclerSpacingDecoration(int spacingPx) {

    fullSpacing = spacingPx;
    halfSpacing = spacingPx / 2;
  }

  @Override
  public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {

    super.getItemOffsets(outRect, view, parent, state);

    if (orientation == -1) {
      orientation = getOrientation(parent);
    }

    if (spanCount == -1) {
      spanCount = getTotalSpan(parent);
    }

    int childCount = parent.getLayoutManager().getItemCount();
    int childIndex = parent.getChildAdapterPosition(view);

    int itemSpanSize = getItemSpanSize(parent, childIndex);
    int spanIndex = getItemSpanIndex(parent, childIndex);

    if (spanCount > 0) {
      setSpacings(outRect, parent, childCount, childIndex, itemSpanSize, spanIndex);
    }
  }

  protected void setSpacings(Rect outRect, RecyclerView parent, int childCount, int childIndex, int itemSpanSize, int spanIndex) {

    outRect.top = 0;
    outRect.bottom = fullSpacing;
    outRect.left = halfSpacing;
    outRect.right = halfSpacing;

    if (isTopEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.top = fullSpacing;
    }

    if (isLeftEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.left = fullSpacing;
    }

    if (isRightEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.right = fullSpacing;
    }

    if (isBottomEdge(parent, childCount, childIndex, itemSpanSize, spanIndex)) {
      outRect.bottom = fullSpacing;
    }
  }

  protected int getTotalSpan(RecyclerView parent) {

    RecyclerView.LayoutManager mgr = parent.getLayoutManager();
    if (mgr instanceof GridLayoutManager) {
      return ((GridLayoutManager) mgr).getSpanCount();
    } else if (mgr instanceof StaggeredGridLayoutManager) {
      return ((StaggeredGridLayoutManager) mgr).getSpanCount();
    } else if (mgr instanceof LinearLayoutManager) {
      return 1;
    }

    return -1;
  }

  protected int getItemSpanSize(RecyclerView parent, int childIndex) {

    RecyclerView.LayoutManager mgr = parent.getLayoutManager();
    if (mgr instanceof GridLayoutManager) {
      return ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanSize(childIndex);
    } else if (mgr instanceof StaggeredGridLayoutManager) {
      return 1;
    } else if (mgr instanceof LinearLayoutManager) {
      return 1;
    }

    return -1;
  }

  protected int getItemSpanIndex(RecyclerView parent, int childIndex) {

    RecyclerView.LayoutManager mgr = parent.getLayoutManager();
    if (mgr instanceof GridLayoutManager) {
      return ((GridLayoutManager) mgr).getSpanSizeLookup().getSpanIndex(childIndex, spanCount);
    } else if (mgr instanceof StaggeredGridLayoutManager) {
      return childIndex % spanCount;
    } else if (mgr instanceof LinearLayoutManager) {
      return 0;
    }

    return -1;
  }

  protected int getOrientation(RecyclerView parent) {

    RecyclerView.LayoutManager mgr = parent.getLayoutManager();
    if (mgr instanceof LinearLayoutManager) {
      return ((LinearLayoutManager) mgr).getOrientation();
    } else if (mgr instanceof GridLayoutManager) {
      return ((GridLayoutManager) mgr).getOrientation();
    } else if (mgr instanceof StaggeredGridLayoutManager) {
      return ((StaggeredGridLayoutManager) mgr).getOrientation();
    }

    return OrientationHelper.VERTICAL;
  }

  protected boolean isLeftEdge(RecyclerView parent, int childCount, int childIndex, int itemSpanSize, int spanIndex) {

    if (orientation == OrientationHelper.VERTICAL) {
      return spanIndex == 0;
    } else {
      return (childIndex == 0) || isFirstItemEdgeValid((childIndex < spanCount), parent, childIndex);
    }
  }

  protected boolean isRightEdge(RecyclerView parent, int childCount, int childIndex, int itemSpanSize, int spanIndex) {

    if (orientation == OrientationHelper.VERTICAL) {
      return (spanIndex + itemSpanSize) == spanCount;
    } else {
      return isLastItemEdgeValid((childIndex >= childCount - spanCount), parent, childCount, childIndex, spanIndex);
    }
  }

  protected boolean isTopEdge(RecyclerView parent, int childCount, int childIndex, int itemSpanSize, int spanIndex) {

    if (orientation == OrientationHelper.VERTICAL) {
      return (childIndex == 0) || isFirstItemEdgeValid((childIndex < spanCount), parent, childIndex);
    } else {
      return spanIndex == 0;
    }
  }

  protected boolean isBottomEdge(RecyclerView parent, int childCount, int childIndex, int itemSpanSize, int spanIndex) {

    if (orientation == OrientationHelper.VERTICAL) {
      return isLastItemEdgeValid((childIndex >= childCount - spanCount), parent, childCount, childIndex, spanIndex);
    } else {
      return (spanIndex + itemSpanSize) == spanCount;
    }
  }

  protected boolean isFirstItemEdgeValid(boolean isOneOfFirstItems, RecyclerView parent, int childIndex) {

    int totalSpanArea = 0;

    if (isOneOfFirstItems) {
      for (int i = childIndex; i >= 0; i--) {
        totalSpanArea = totalSpanArea + getItemSpanSize(parent, i);
      }
    }

    return isOneOfFirstItems && totalSpanArea <= spanCount;
  }

  protected boolean isLastItemEdgeValid(boolean isOneOfLastItems, RecyclerView parent, int childCount, int childIndex, int spanIndex) {

    int totalSpanRemaining = 0;

    if (isOneOfLastItems) {
      for (int i = childIndex; i < childCount; i++) {
        totalSpanRemaining = totalSpanRemaining + getItemSpanSize(parent, i);
      }
    }

    return isOneOfLastItems && (totalSpanRemaining <= spanCount - spanIndex);
  }
}