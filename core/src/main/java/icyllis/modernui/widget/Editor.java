/*
 * Modern UI.
 * Copyright (C) 2019-2021 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.widget;

import icyllis.modernui.core.ArchCore;
import icyllis.modernui.text.Selection;
import icyllis.modernui.text.Spannable;
import icyllis.modernui.text.method.MovementMethod;
import icyllis.modernui.text.method.WordIterator;
import icyllis.modernui.view.MotionEvent;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.annotation.Nonnull;

/**
 * Helper class used by TextView to handle editable text views.
 */
public class Editor {

    private static final Marker MARKER = MarkerManager.getMarker("Editor");

    static final int BLINK = 500;

    private final TextView mTextView;

    boolean mSelectionMoved;
    boolean mTouchFocusSelected;

    boolean mDiscardNextActionUp;
    boolean mIgnoreActionUpEvent;

    private long mShowCursor;
    private Blink mBlink;

    boolean mCursorVisible = true;
    boolean mSelectAllOnFocus;
    boolean mTextIsSelectable;

    /*boolean mInBatchEditControllers;
    private boolean mPreserveSelection;
    private boolean mRestartActionModeOnNextRefresh;
    private boolean mRequestingLinkActionMode;

    boolean mIsBeingLongClicked;

    private float mContextMenuAnchorX, mContextMenuAnchorY;*/

    // The button state as of the last time #onTouchEvent is called.
    private int mLastButtonState;

    private WordIterator mWordIterator;

    Editor(TextView textView) {
        mTextView = textView;
    }

    void onAttachedToWindow() {
        resumeBlink();
    }

    void onDetachedFromWindow() {
        suspendBlink();
    }

    private boolean isCursorVisible() {
        // The default value is true, even when there is no associated Editor
        return mCursorVisible && mTextView.isTextEditable();
    }

    boolean shouldRenderCursor() {
        if (isCursorVisible()) {
            final long showCursorDelta = ArchCore.timeMillis() - mShowCursor;
            return showCursorDelta % (2 * BLINK) < BLINK;
        }
        return false;
    }

    private void suspendBlink() {
        if (mBlink != null) {
            mBlink.cancel();
        }
    }

    private void resumeBlink() {
        if (mBlink != null) {
            mBlink.reset();
            makeBlink();
        }
    }

    WordIterator getWordIterator() {
        if (mWordIterator == null) {
            mWordIterator = new WordIterator(mTextView.getTextLocale());
        }
        return mWordIterator;
    }

    void onFocusChanged(boolean focused, int direction) {
        mShowCursor = ArchCore.timeMillis();

        if (focused) {
            int selStart = mTextView.getSelectionStart();
            int selEnd = mTextView.getSelectionEnd();

            // SelectAllOnFocus fields are highlighted and not selected. Do not start text selection
            // mode for these, unless there was a specific selection already started.
            final boolean isFocusHighlighted = mSelectAllOnFocus && selStart == 0
                    && selEnd == mTextView.getText().length();

            // Note this may have to be moved out of the Editor class
            MovementMethod movement = mTextView.getMovementMethod();
            if (movement != null) {
                movement.onTakeFocus(mTextView, (Spannable) mTextView.getText(), direction);
            }

            // The DecorView does not have focus when the 'Done' ExtractEditText button is
            // pressed. Since it is the ViewAncestor's mView, it requests focus before
            // ExtractEditText clears focus, which gives focus to the ExtractEditText.
            // This special case ensure that we keep current selection in that case.
            // It would be better to know why the DecorView does not have focus at that time.
            if (mSelectionMoved && selStart >= 0 && selEnd >= 0) {
                /*
                 * Someone intentionally set the selection, so let them
                 * do whatever it is that they wanted to do instead of
                 * the default on-focus behavior.  We reset the selection
                 * here instead of just skipping the onTakeFocus() call
                 * because some movement methods do something other than
                 * just setting the selection in theirs and we still
                 * need to go through that path.
                 */
                Selection.setSelection((Spannable) mTextView.getText(), selStart, selEnd);
            }

            if (mSelectAllOnFocus) {
                mTextView.selectAllText();
            }

            mTouchFocusSelected = true;

            makeBlink();
        }
    }

    /**
     * Handles touch events on an editable text view, implementing cursor movement, selection, etc.
     */
    void onTouchEvent(@Nonnull MotionEvent event) {
        final int action = event.getAction();
        final boolean filterOutEvent;
        if ((action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_UP)
                && ((mLastButtonState ^ event.getButtonState()) & MotionEvent.BUTTON_PRIMARY) == 0) {
            filterOutEvent = true;
        } else {
            filterOutEvent = action == MotionEvent.ACTION_MOVE
                    && !event.isButtonPressed(MotionEvent.BUTTON_PRIMARY);
        }
        mLastButtonState = event.getButtonState();
        if (filterOutEvent) {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                mDiscardNextActionUp = true;
            }
            return;
        }

        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            // Reset this state; it will be re-set if super.onTouchEvent
            // causes focus to move to the view.
            mTouchFocusSelected = false;
            mIgnoreActionUpEvent = false;
        }
    }

    void onTouchUpEvent(@Nonnull MotionEvent event) {
        boolean selectAllGotFocus = mSelectAllOnFocus && mTextView.didTouchFocusSelect();
        CharSequence text = mTextView.getText();
        if (!selectAllGotFocus) {
            // Move cursor
            final int offset = mTextView.getOffsetForPosition(event.getX(), event.getY());
            Selection.setSelection((Spannable) text, offset);
        }
    }

    void sendOnTextChanged(int start, int before, int after) {
    }

    void addSpanWatchers(Spannable sp) {
    }

    /**
     * @return True when the TextView isFocused and has a valid zero-length selection (cursor).
     */
    private boolean shouldBlink() {
        if (!isCursorVisible() || !mTextView.isFocused()) return false;

        final int start = mTextView.getSelectionStart();
        if (start < 0) return false;

        final int end = mTextView.getSelectionEnd();
        if (end < 0) return false;

        return start == end;
    }

    void makeBlink() {
        if (shouldBlink()) {
            mShowCursor = ArchCore.timeMillis();
            if (mBlink == null) {
                mBlink = new Blink();
            }
            mTextView.removeCallbacks(mBlink);
            mTextView.postDelayed(mBlink, BLINK);
        } else {
            if (mBlink != null) {
                mTextView.removeCallbacks(mBlink);
            }
        }
    }

    private class Blink implements Runnable {

        private boolean mCancelled;

        @Override
        public void run() {
            if (mCancelled) {
                return;
            }

            if (shouldBlink()) {
                if (mTextView.getLayout() != null) {
                    mTextView.invalidateCursorPath();
                }

                mTextView.postDelayed(this, BLINK);
            }
        }

        void cancel() {
            if (!mCancelled) {
                mTextView.removeCallbacks(this);
                mCancelled = true;
            }
        }

        void reset() {
            mCancelled = false;
        }
    }
}
