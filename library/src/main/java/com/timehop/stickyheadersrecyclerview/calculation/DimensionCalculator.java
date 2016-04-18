package com.timehop.stickyheadersrecyclerview.calculation;

import android.graphics.Rect;
import android.view.View;

import static android.view.ViewGroup.LayoutParams;
import static android.view.ViewGroup.MarginLayoutParams;

/**
 * Helper to calculate various view dimensions
 */
public class DimensionCalculator {

  /**
   * Returns {@link Rect} representing margins for any view.
   *
   * @param view for which to get margins
   * @return margins for the given view. All 0 if the view does not support margins
   */
  public Rect getMargins(View view) {
    LayoutParams layoutParams = view != null ? view.getLayoutParams() : null;

    if (layoutParams != null && layoutParams instanceof MarginLayoutParams) {
      return getMarginRect((MarginLayoutParams) layoutParams);
    } else {
      return new Rect();
    }
  }

  /**
   * Populates {@link Rect} with margins for any view.
   *
   *
   * @param margins rect to populate
   * @param view for which to get margins
   */
  public Rect initMargins(Rect margins, View view) {
    if (margins == null) {
      margins = new Rect();
    }

    LayoutParams layoutParams = view != null ? view.getLayoutParams() : null;

    if (layoutParams != null && layoutParams instanceof MarginLayoutParams) {
      initMarginRect(margins, (MarginLayoutParams) layoutParams);
    } else {
      margins.set(0, 0, 0, 0);
    }

    return margins;
  }

  /**
   * Converts {@link MarginLayoutParams} into a representative {@link Rect}
   *
   * @param marginLayoutParams margins to convert to a Rect
   * @return Rect representing margins, where {@link MarginLayoutParams#leftMargin} is equivalent to
   * {@link Rect#left}, etc.
   */
  private Rect getMarginRect(MarginLayoutParams marginLayoutParams) {
    if (marginLayoutParams != null) {
      return new Rect(
              marginLayoutParams.leftMargin,
              marginLayoutParams.topMargin,
              marginLayoutParams.rightMargin,
              marginLayoutParams.bottomMargin
      );
    } else {
      return new Rect();
    }
  }

  /**
   * Converts {@link MarginLayoutParams} into a representative {@link Rect}.
   *
   * @param marginRect Rect to be initialized with margins coordinates, where
   * {@link MarginLayoutParams#leftMargin} is equivalent to {@link Rect#left}, etc.
   * @param marginLayoutParams margins to populate the Rect with
   */
  private Rect initMarginRect(Rect marginRect, MarginLayoutParams marginLayoutParams) {
    if (marginRect == null) {
      marginRect = new Rect();
    }

    if (marginLayoutParams != null) {
      marginRect.set(
              marginLayoutParams.leftMargin,
              marginLayoutParams.topMargin,
              marginLayoutParams.rightMargin,
              marginLayoutParams.bottomMargin
      );
    }

    return marginRect;
  }
}
