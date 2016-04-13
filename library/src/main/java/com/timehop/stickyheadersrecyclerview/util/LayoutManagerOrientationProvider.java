package com.timehop.stickyheadersrecyclerview.util;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

/**
 * OrientationProvider for ReyclerViews who use a LayoutManager
 */
public class LayoutManagerOrientationProvider implements OrientationProvider {

  public static int getRecyclerViewOrientation(RecyclerView recyclerView) {
    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
    if (layoutManager instanceof LinearLayoutManager) {
      return ((LinearLayoutManager) layoutManager).getOrientation();
    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
      return ((StaggeredGridLayoutManager) layoutManager).getOrientation();
    } else {
      throw new IllegalStateException("DividerDecoration can only be used with a valid LayoutManager: " + layoutManager.getClass().getSimpleName());
    }
  }

  public boolean isRecyclerViewReverseLayout(RecyclerView recyclerView) {
    RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
    if (layoutManager instanceof LinearLayoutManager) {
      return ((LinearLayoutManager) layoutManager).getReverseLayout();
    } else if (layoutManager instanceof StaggeredGridLayoutManager) {
      return ((StaggeredGridLayoutManager) layoutManager).getReverseLayout();
    } else {
      throw new IllegalStateException("DividerDecoration can only be used with a valid LayoutManager: " + layoutManager.getClass().getSimpleName());
    }
  }

  @Override
  public int getOrientation(RecyclerView recyclerView) {
    return getRecyclerViewOrientation(recyclerView);
  }

  @Override
  public boolean isReverseLayout(RecyclerView recyclerView) {
    return isRecyclerViewReverseLayout(recyclerView);
  }
}
