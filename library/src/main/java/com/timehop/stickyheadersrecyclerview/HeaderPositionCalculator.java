package com.timehop.stickyheadersrecyclerview;

import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.timehop.stickyheadersrecyclerview.caching.HeaderProvider;
import com.timehop.stickyheadersrecyclerview.calculation.DimensionCalculator;
import com.timehop.stickyheadersrecyclerview.util.OrientationProvider;

/**
 * Calculates the position and location of header views
 */
public class HeaderPositionCalculator {

  private final StickyRecyclerHeadersAdapter mAdapter;
  private final OrientationProvider mOrientationProvider;
  private final HeaderProvider mHeaderProvider;
  private final DimensionCalculator mDimensionCalculator;

  public HeaderPositionCalculator(StickyRecyclerHeadersAdapter adapter, HeaderProvider headerProvider, OrientationProvider orientationProvider, DimensionCalculator dimensionCalculator) {
    mAdapter = adapter;
    mHeaderProvider = headerProvider;
    mOrientationProvider = orientationProvider;
    mDimensionCalculator = dimensionCalculator;
  }

  /**
   * Determines if a view should have a sticky header.
   * The view has a sticky header if:
   * 1. It is the first element in the recycler view
   * 2. It has a valid ID associated to its position
   *
   * @param itemView given by the RecyclerView
   * @param orientation of the Recyclerview
   * @param position of the list item in question
   * @return True if the view should have a sticky header
   */
  public boolean hasStickyHeader(View itemView, int orientation, int position) {
    int offset, margin;

    if (itemView != null) {
      Rect rect = mDimensionCalculator.getMargins(itemView);

      if (orientation == LinearLayout.VERTICAL) {
        offset = itemView.getTop();
        margin = rect.top;
      } else {
        offset = itemView.getLeft();
        margin = rect.left;
      }
    } else {
      offset = -1;
      margin = 0;
    }

    return offset <= margin && mAdapter.getHeaderId(position) >= 0;
  }

  /**
   * Determines if an item in the list should have a header that is different than the item in the
   * list that immediately precedes it. Items with no headers will always return false.
   *
   * @param position of the list item in questions
   * @param isReverseLayout TRUE if layout manager has flag isReverseLayout
   * @return true if this item has a different header than the previous item in the list
   */
  public boolean hasNewHeader(int position, boolean isReverseLayout) {
    if (indexOutOfBounds(position)) {
      return false;
    }

    int numColumns = mAdapter.getNumColumns() - mAdapter.getSpanSize(position) + 1;
    int columnOfItem = position % numColumns;
    if (columnOfItem > 0) {
      int firstItemOnRowPosition = position - columnOfItem;

      return hasNewHeader(firstItemOnRowPosition, isReverseLayout);
    }

    long headerId = mAdapter.getHeaderId(position);

    if (headerId < 0) {
      return false;
    }

    long nextItemHeaderId = -1;
    int nextItemPosition = position + (isReverseLayout? 1: -1);
    if (!indexOutOfBounds(nextItemPosition)){
      nextItemHeaderId = mAdapter.getHeaderId(nextItemPosition);
    }

    int firstItemPosition = isReverseLayout? mAdapter.getItemCount()-1 : 0;

    return position == firstItemPosition || headerId != nextItemHeaderId;
  }

  private boolean indexOutOfBounds(int position) {
    return position < 0 || position >= mAdapter.getItemCount();
  }

  /**
   * Verify if header obscure some item on RecyclerView
   *
   * @param parent RecyclerView containing all the list items
   * @return first item that is fully beneath a header
   */
  public boolean headerObscuringSomeItem(RecyclerView parent, View firstHeader) {
    for (int i = 0; i < parent.getChildCount(); i++) {
      View child = parent.getChildAt(i);

      if (itemIsObscuredByHeader(parent, child, firstHeader, mOrientationProvider.getOrientation(parent))) {
          return true;
      }
    }

    return false;
  }

  public Rect getHeaderBounds(RecyclerView recyclerView, View header, View firstView, boolean firstHeader, boolean enableStickyHeader) {
    int orientation = mOrientationProvider.getOrientation(recyclerView);

    Rect bounds = getDefaultHeaderOffset(recyclerView, header, firstView, orientation, enableStickyHeader);

    if (enableStickyHeader && firstHeader && isStickyHeaderBeingPushedOffscreen(recyclerView, header)) {
      View viewAfterNextHeader = getFirstViewUnobscuredByHeader(recyclerView, header);
      int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterNextHeader);
      View secondHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
      translateHeaderWithNextHeader(recyclerView, mOrientationProvider.getOrientation(recyclerView), bounds, header, viewAfterNextHeader, secondHeader);
    }

    return bounds;
  }

  private Rect getDefaultHeaderOffset(RecyclerView recyclerView, View header, View firstView, int orientation, boolean enableStickyHeader) {
    int translationX, translationY;

    Rect headerMargins = mDimensionCalculator.getMargins(header);

    if (header != null && firstView != null) {
      ViewGroup.LayoutParams layoutParams = firstView.getLayoutParams();
      int leftMargin = 0;
      int topMargin = 0;
      if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
        ViewGroup.MarginLayoutParams marginLayoutParams = (ViewGroup.MarginLayoutParams) layoutParams;
        leftMargin = marginLayoutParams.leftMargin;
        topMargin = marginLayoutParams.topMargin;
      }

      if (orientation == LinearLayoutManager.VERTICAL) {
        translationX = firstView.getLeft() - leftMargin + headerMargins.left;
        translationY = enableStickyHeader ?
                Math.max(firstView.getTop() - topMargin - header.getHeight() - headerMargins.bottom, getListTop(recyclerView) + headerMargins.top) :
                firstView.getTop() - topMargin - header.getHeight() - headerMargins.bottom;
      } else {
        translationY = firstView.getTop() - topMargin + headerMargins.top;
        translationX = enableStickyHeader ?
                Math.max(firstView.getLeft() - leftMargin - header.getWidth() - headerMargins.right, getListLeft(recyclerView) + headerMargins.left) :
                firstView.getLeft() - leftMargin - header.getWidth() - headerMargins.right;
      }

      return new Rect(translationX, translationY, translationX + header.getWidth(), translationY + header.getHeight());
    } else {
      return headerMargins;
    }
  }

  private boolean isStickyHeaderBeingPushedOffscreen(RecyclerView recyclerView, View stickyHeader) {
    if (stickyHeader != null) {
      View viewAfterHeader = getFirstViewUnobscuredByHeader(recyclerView, stickyHeader);
      int firstViewUnderHeaderPosition = recyclerView.getChildAdapterPosition(viewAfterHeader);
      if (firstViewUnderHeaderPosition == RecyclerView.NO_POSITION) {
        return false;
      }

      boolean isReverseLayout = mOrientationProvider.isReverseLayout(recyclerView);
      if (firstViewUnderHeaderPosition > 0 && hasNewHeader(firstViewUnderHeaderPosition, isReverseLayout)) {
        View nextHeader = mHeaderProvider.getHeader(recyclerView, firstViewUnderHeaderPosition);
        Rect nextHeaderMargins = mDimensionCalculator.getMargins(nextHeader);
        Rect headerMargins = mDimensionCalculator.getMargins(stickyHeader);

        if (mOrientationProvider.getOrientation(recyclerView) == LinearLayoutManager.VERTICAL) {
          int topOfNextHeader = viewAfterHeader.getTop() - nextHeaderMargins.bottom - nextHeader.getHeight() - nextHeaderMargins.top;
          int bottomOfThisHeader = recyclerView.getPaddingTop() + stickyHeader.getBottom() + headerMargins.top + headerMargins.bottom;
          if (topOfNextHeader < bottomOfThisHeader) {
            return true;
          }
        } else {
          int leftOfNextHeader = viewAfterHeader.getLeft() - nextHeaderMargins.right - nextHeader.getWidth() - nextHeaderMargins.left;
          int rightOfThisHeader = recyclerView.getPaddingLeft() + stickyHeader.getRight() + headerMargins.left + headerMargins.right;
          if (leftOfNextHeader < rightOfThisHeader) {
            return true;
          }
        }
      }
    }

    return false;
  }

  private void translateHeaderWithNextHeader(RecyclerView recyclerView, int orientation, Rect translation,
    View currentHeader, View viewAfterNextHeader, View nextHeader) {

    Rect nextHeaderMargins = mDimensionCalculator.getMargins(nextHeader);
    Rect stickyHeaderMargins = mDimensionCalculator.getMargins(currentHeader);

    if (orientation == LinearLayoutManager.VERTICAL) {
      int topOfStickyHeader = getListTop(recyclerView) + stickyHeaderMargins.top + stickyHeaderMargins.bottom;
      int shiftFromNextHeader = viewAfterNextHeader.getTop() - nextHeader.getHeight() - nextHeaderMargins.bottom - nextHeaderMargins.top - currentHeader.getHeight() - topOfStickyHeader;
      if (shiftFromNextHeader < topOfStickyHeader) {
        translation.top += shiftFromNextHeader;
      }
    } else {
      int leftOfStickyHeader = getListLeft(recyclerView) + stickyHeaderMargins.left + stickyHeaderMargins.right;
      int shiftFromNextHeader = viewAfterNextHeader.getLeft() - nextHeader.getWidth() - nextHeaderMargins.right - nextHeaderMargins.left - currentHeader.getWidth() - leftOfStickyHeader;
      if (shiftFromNextHeader < leftOfStickyHeader) {
        translation.left += shiftFromNextHeader;
      }
    }
  }

  /**
   * Returns the first item currently in the RecyclerView that is not obscured by a header.
   *
   * @param parent Recyclerview containing all the list items
   * @return first item that is fully beneath a header
   */
  private View getFirstViewUnobscuredByHeader(RecyclerView parent, View firstHeader) {
    boolean isReverseLayout = mOrientationProvider.isReverseLayout(parent);
    int step = isReverseLayout? -1 : 1;
    int from = isReverseLayout? parent.getChildCount()-1 : 0;
    for (int i = from; i >= 0 && i <= parent.getChildCount() - 1; i += step) {
      View child = parent.getChildAt(i);
      if (!itemIsObscuredByHeader(parent, child, firstHeader, mOrientationProvider.getOrientation(parent))) {
        return child;
      }
    }
    return null;
  }

  /**
   * Determines if an item is obscured by a header
   *
   *
   * @param parent
   * @param item        to determine if obscured by header
   * @param header      that might be obscuring the item
   * @param orientation of the {@link RecyclerView}
   * @return true if the item view is obscured by the header view
   */
  private boolean itemIsObscuredByHeader(RecyclerView parent, View item, View header, int orientation) {
    RecyclerView.LayoutParams layoutParams = (RecyclerView.LayoutParams) item.getLayoutParams();
    Rect rect = mDimensionCalculator.getMargins(header);

    int adapterPosition = parent.getChildAdapterPosition(item);
    if (adapterPosition == RecyclerView.NO_POSITION || mHeaderProvider.getHeader(parent, adapterPosition) != header) {
      // Resolves https://github.com/timehop/sticky-headers-recyclerview/issues/36
      // Handles an edge case where a trailing header is smaller than the current sticky header.
      return false;
    }

    if (orientation == LinearLayoutManager.VERTICAL) {
      int itemTop = item.getTop() - layoutParams.topMargin;
      int headerBottom = getListTop(parent) + header.getBottom() + rect.bottom + rect.top;
      if (itemTop >= headerBottom) {
        return false;
      }
    } else {
      int itemLeft = item.getLeft() - layoutParams.leftMargin;
      int headerRight = getListLeft(parent) + header.getRight() + rect.right + rect.left;
      if (itemLeft >= headerRight) {
        return false;
      }
    }

    return true;
  }

  private int getListTop(RecyclerView view) {
    if (view.getLayoutManager().getClipToPadding()) {
      return view.getPaddingTop();
    } else {
      return 0;
    }
  }

  private int getListLeft(RecyclerView view) {
    if (view.getLayoutManager().getClipToPadding()) {
      return view.getPaddingLeft();
    } else {
      return 0;
    }
  }
}
