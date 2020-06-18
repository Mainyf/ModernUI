/*
 * Modern UI.
 * Copyright (C) 2019 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * 3.0 any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.ui.layout;

import icyllis.modernui.ui.master.View;
import icyllis.modernui.ui.master.ViewGroup;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class FrameLayout extends ViewGroup {

    public FrameLayout() {

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int count = getChildCount();

        int maxWidth = 0;
        int maxHeight = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility().canLayout()) {
                measureChildWithMargins(child,
                        widthMeasureSpec, 0, heightMeasureSpec, 0);
                LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth,
                        child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight,
                        child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }
        }

        maxWidth = Math.max(maxWidth, getMinWidth());
        maxHeight = Math.max(maxHeight, getMinHeight());

        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec),
                resolveSize(maxHeight, heightMeasureSpec));
    }

    @Override
    protected void onLayout(boolean changed) {
        int count = getChildCount();

        int parentLeft = getLeft();
        int parentRight = getRight();

        int parentTop = getTop();
        int parentBottom = getBottom();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != Visibility.GONE) {
                LayoutParams lp = (LayoutParams) child.getLayoutParams();

                int width = child.getMeasuredWidth();
                int height = child.getMeasuredHeight();

                int childLeft;
                int childTop;

                int gravity = lp.gravity;

                int horizontalGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                switch (horizontalGravity) {
                    case Gravity.HORIZONTAL_CENTER:
                        childLeft = parentLeft + (parentRight - parentLeft - width) / 2 +
                                lp.leftMargin - lp.rightMargin;
                        break;
                    case Gravity.RIGHT:
                        childLeft = parentRight - width - lp.rightMargin;
                        break;
                    default:
                        childLeft = parentLeft + lp.leftMargin;
                }

                switch (verticalGravity) {
                    case Gravity.VERTICAL_CENTER:
                        childTop = parentTop + (parentBottom - parentTop - height) / 2 +
                                lp.topMargin - lp.bottomMargin;
                        break;
                    case Gravity.BOTTOM:
                        childTop = parentBottom - height - lp.bottomMargin;
                        break;
                    default:
                        childTop = parentTop + lp.topMargin;
                }

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    @Nonnull
    @Override
    protected ViewGroup.LayoutParams convertLayoutParams(@Nonnull ViewGroup.LayoutParams params) {
        if (params instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) params);
        } else if (params instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) params);
        }
        return new LayoutParams(params);
    }

    @Nonnull
    @Override
    protected ViewGroup.LayoutParams createDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected boolean checkLayoutParams(@Nullable ViewGroup.LayoutParams params) {
        return params instanceof LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {

        /**
         * The gravity to apply with the View to which these layout parameters
         * are associated. Default value is TOP_LEFT
         */
        public int gravity = Gravity.TOP | Gravity.LEFT;

        /**
         * Creates a new set of layout parameters with the specified width
         * and height.
         *
         * @param width  either {@link #WRAP_CONTENT},
         *               {@link #MATCH_PARENT}, or a fixed value
         * @param height either {@link #WRAP_CONTENT},
         *               {@link #MATCH_PARENT}, or a fixed value
         */
        public LayoutParams(int width, int height) {
            super(width, height);
        }

        /**
         * Creates a new set of layout parameters with the specified width
         * and height.
         *
         * @param width   either {@link #WRAP_CONTENT},
         *                {@link #MATCH_PARENT}, or a fixed value
         * @param height  either {@link #WRAP_CONTENT},
         *                {@link #MATCH_PARENT}, or a fixed value
         * @param gravity the gravity
         */
        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(@Nonnull ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(@Nonnull ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        /**
         * Copy constructor. Clones the width, height, margin values, and
         * gravity of the source.
         *
         * @param source the layout params to copy from.
         */
        public LayoutParams(@Nonnull LayoutParams source) {
            super(source);

            gravity = source.gravity;
        }

        @Nonnull
        @Override
        public LayoutParams copy() {
            return new LayoutParams(this);
        }
    }
}