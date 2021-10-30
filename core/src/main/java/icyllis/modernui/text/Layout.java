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

package icyllis.modernui.text;

import icyllis.modernui.graphics.Canvas;
import icyllis.modernui.text.style.ParagraphStyle;
import icyllis.modernui.text.style.TabStopSpan;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

/**
 * A base class that manages text layout in visual elements on the screen,
 * which is designed for text pages at a high-level.
 * <p>
 * For text that will be edited, use a {@link DynamicLayout},
 * which will be updated as the text changes.
 * For text that will not change, use a {@link StaticLayout}.
 *
 * @see StaticLayout
 * @see DynamicLayout
 * @since 3.0
 */
public abstract class Layout {

    public static final int DIR_LEFT_TO_RIGHT = 1;
    public static final int DIR_RIGHT_TO_LEFT = -1;

    public static final float TAB_INCREMENT = 20;

    private static final ParagraphStyle[] NO_PARA_SPANS = {};

    /// member variables \\\

    private CharSequence mText;
    private TextPaint mPaint;
    private int mWidth;
    private Alignment mAlignment;
    private boolean mSpannedText;
    private final TextDirectionHeuristic mTextDir;

    /**
     * Subclasses of Layout use this constructor to set the display text,
     * width, and other standard properties.
     *
     * @param text  the text to render
     * @param paint the default paint for the layout.  Styles can override
     *              various attributes of the paint.
     * @param width the wrapping width for the text.
     * @param align whether to left, right, or center the text.  Styles can
     *              override the alignment.
     */
    protected Layout(CharSequence text, TextPaint paint,
                     int width, Alignment align) {
        this(text, paint, width, align, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    /**
     * Subclasses of Layout use this constructor to set the display text,
     * width, and other standard properties.
     *
     * @param text    the text to render
     * @param paint   the default paint for the layout.  Styles can override
     *                various attributes of the paint.
     * @param width   the wrapping width for the text.
     * @param align   whether to left, right, or center the text.  Styles can
     *                override the alignment.
     * @param textDir the text direction algorithm
     */
    protected Layout(CharSequence text, TextPaint paint,
                     int width, Alignment align, TextDirectionHeuristic textDir) {
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }

        // We probably should re-evaluate bgColor.
        if (paint != null) {
            paint.bgColor = 0;
        }

        mText = text;
        mPaint = paint;
        mWidth = width;
        mAlignment = align;
        mSpannedText = text instanceof Spanned;
        mTextDir = textDir;
    }


    /**
     * Replace constructor properties of this Layout with new ones.  Be careful.
     */
    void replaceWith(CharSequence text, TextPaint paint,
                     int width, Alignment align) {
        if (width < 0) {
            throw new IllegalArgumentException("Layout: " + width + " < 0");
        }

        mText = text;
        mPaint = paint;
        mWidth = width;
        mAlignment = align;
        mSpannedText = text instanceof Spanned;
    }

    /**
     * Draw this Layout on the specified Canvas.
     * <p>
     * Note that this method just calls {@link #drawBackground(Canvas, int, int)}
     * and then {@link #drawText(Canvas, int, int)}. If you need to draw something between the two,
     * such as blinking cursor and selection highlight, you may manually call them separately.
     *
     * @param canvas the canvas to draw on
     * @see #drawBackground(Canvas, int, int)
     * @see #drawText(Canvas, int, int)
     */
    public void draw(@Nonnull Canvas canvas) {
        final long range = getLineRangeForDraw(canvas);
        if (range < 0) return;
        int firstLine = (int) (range >>> 32);
        int lastLine = (int) (range & 0xFFFFFFFFL);
        drawBackground(canvas, firstLine, lastLine);
        drawText(canvas, firstLine, lastLine);
    }

    /**
     * Draw the visible background drawables of this Layout on the specified canvas.
     * <p>
     * Significantly, visible area given by <code>firstLine</code> and
     * <code>lastLine</code> is computed by {@link #getLineRangeForDraw(Canvas)}.
     * You may never just call this method without that method.
     *
     * @param canvas    the canvas to draw on
     * @param firstLine first line index (inclusive)
     * @param lastLine  last line index (inclusive)
     * @see #drawText(Canvas, int, int)
     */
    //TODO background span
    public final void drawBackground(@Nonnull Canvas canvas, int firstLine, int lastLine) {
        if (!mSpannedText) return;
        assert firstLine >= 0 && lastLine >= firstLine;
        Spanned buffer = (Spanned) mText;
    }

    /**
     * Draw all visible text lines of this Layout on the specified canvas.
     * <p>
     * Significantly, visible area given by <code>firstLine</code> and
     * <code>lastLine</code> is computed by {@link #getLineRangeForDraw(Canvas)}.
     * You may never just call this method without that method.
     *
     * @param canvas    the canvas to draw on
     * @param firstLine first line index (inclusive)
     * @param lastLine  last line index (inclusive)
     * @see #drawBackground(Canvas, int, int)
     */
    public final void drawText(@Nonnull Canvas canvas, int firstLine, int lastLine) {
        assert firstLine >= 0 && lastLine >= firstLine && lastLine < getLineCount();

        int previousLineBottom = getLineTop(firstLine);
        int previousLineEnd = getLineStart(firstLine);
        ParagraphStyle[] spans = NO_PARA_SPANS;
        int spanEnd = 0;
        final TextPaint paint = TextPaint.obtain();
        paint.set(mPaint);
        CharSequence buf = mText;

        Alignment paraAlign = mAlignment;
        TabStops tabStops = null;
        boolean tabStopsIsInitialized = false;

        final TextLine tl = TextLine.obtain();

        // Draw the lines, one at a time.
        // The baseline is the top of the following line minus the current line's descent.
        for (int lineNum = firstLine; lineNum <= lastLine; lineNum++) {
            int start = previousLineEnd;
            previousLineEnd = getLineStart(lineNum + 1);
            int end = getLineVisibleEnd(lineNum, start, previousLineEnd);

            int ltop = previousLineBottom;
            int lbottom = getLineTop(lineNum + 1);
            previousLineBottom = lbottom;
            int lbaseline = lbottom - getLineDescent(lineNum);

            int dir = getParagraphDirection(lineNum);
            int left = 0;
            int right = mWidth;

            //TODO para style
            if (mSpannedText) {
                Spanned sp = (Spanned) buf;
                spans = getParagraphSpans(sp, start, spanEnd, ParagraphStyle.class);
            }

            boolean hasTab = getLineContainsTab(lineNum);
            // Can't tell if we have tabs for sure, currently
            if (hasTab && !tabStopsIsInitialized) {
                if (tabStops == null) {
                    tabStops = new TabStops(TAB_INCREMENT, spans);
                } else {
                    tabStops.reset(TAB_INCREMENT, spans);
                }
                tabStopsIsInitialized = true;
            }

            // Determine whether the line aligns to normal, opposite, or center.
            Alignment align = paraAlign;
            if (align == Alignment.ALIGN_LEFT) {
                align = (dir == DIR_LEFT_TO_RIGHT) ?
                        Alignment.ALIGN_NORMAL : Alignment.ALIGN_OPPOSITE;
            } else if (align == Alignment.ALIGN_RIGHT) {
                align = (dir == DIR_LEFT_TO_RIGHT) ?
                        Alignment.ALIGN_OPPOSITE : Alignment.ALIGN_NORMAL;
            }

            Directions directions = getLineDirections(lineNum);
            final int ellipsisStart = getEllipsisStart(lineNum);
            tl.set(paint, buf, start, end, dir, directions, hasTab, tabStops,
                    ellipsisStart, ellipsisStart + getEllipsisCount(lineNum));

            int x;
            final int indentWidth;
            if (align == Alignment.ALIGN_NORMAL) {
                if (dir == DIR_LEFT_TO_RIGHT) {
                    indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                    x = left + indentWidth;
                } else {
                    indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                    x = right - indentWidth;
                }
            } else {
                int max = (int) tl.metrics(null);
                if (align == Alignment.ALIGN_OPPOSITE) {
                    if (dir == DIR_LEFT_TO_RIGHT) {
                        indentWidth = -getIndentAdjust(lineNum, Alignment.ALIGN_RIGHT);
                        x = right - max - indentWidth;
                    } else {
                        indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_LEFT);
                        x = left - max + indentWidth;
                    }
                } else { // Alignment.ALIGN_CENTER
                    indentWidth = getIndentAdjust(lineNum, Alignment.ALIGN_CENTER);
                    max = max & ~1;
                    x = ((right + left - max) >> 1) + indentWidth;
                }
            }

            if (directions == Directions.ALL_LEFT_TO_RIGHT && !mSpannedText && !hasTab) {
                // XXX: assumes there's nothing additional to be done
                canvas.drawText(buf, start, end, x, lbaseline, paint);
            } else {
                tl.draw(canvas, x, ltop, lbaseline, lbottom);
            }
        }

        paint.recycle();
        tl.recycle();
    }

    /**
     * Computes the range of visible lines that will be drawn on the specified canvas.
     * It will be used for {@link #drawText(Canvas, int, int)}. The higher 32 bits represent
     * the first line number, while the lower 32 bits represent the last line number.
     * Note that if the range is empty, then the method returns <code>~0L</code>.
     *
     * @param canvas the canvas used to draw this Layout
     * @return the range of lines that need to be drawn, possibly empty.
     */
    public final long getLineRangeForDraw(@Nonnull Canvas canvas) {
        final int lineCount = getLineCount();
        if (lineCount <= 0) {
            return ~0L;
        }
        final int bottom = getLineTop(lineCount);
        if (canvas.quickReject(0, 0, mWidth, bottom)) {
            return ~0L;
        }
        int lineNum = 0, lineTop = 0, lineBottom;
        int firstLine = -1, lastLine = -1;
        do {
            lineBottom = getLineTop(lineNum + 1);
            if (firstLine == -1) {
                if (!canvas.quickReject(0, lineTop, mWidth, lineBottom)) {
                    firstLine = lineNum;
                }
            } else if (canvas.quickReject(0, lineTop, mWidth, lineBottom)) {
                lastLine = lineNum - 1;
                break;
            }
            lineTop = lineBottom;
        } while (++lineNum < lineCount);

        if (firstLine == -1) {
            return ~0L;
        }
        if (lastLine == -1) {
            assert lineNum == lineCount;
            lastLine = lineCount - 1;
        }
        assert lastLine >= firstLine;
        return (long) firstLine << 32 | lastLine;
    }

    /**
     * Get the line number corresponding to the specified vertical position.
     * If you ask for a position above 0, you get 0; if you ask for a position
     * below the bottom of the text, you get the last line.
     */
    // FIXME: It may be faster to do a linear search for layouts without many lines.
    public int getLineForVertical(int vertical) {
        int high = getLineCount(), low = -1, guess;

        while (high - low > 1) {
            guess = (high + low) >> 1;

            if (getLineTop(guess) > vertical)
                high = guess;
            else
                low = guess;
        }

        return Math.max(low, 0);
    }

    /**
     * Get the line number on which the specified text offset appears.
     * If you ask for a position before 0, you get 0; if you ask for a position
     * beyond the end of the text, you get the last line.
     */
    public int getLineForOffset(int offset) {
        int high = getLineCount(), low = -1, guess;

        while (high - low > 1) {
            guess = (high + low) / 2;

            if (getLineStart(guess) > offset)
                high = guess;
            else
                low = guess;
        }

        return Math.max(low, 0);
    }

    /**
     * Return the text that is displayed by this Layout.
     */
    public final CharSequence getText() {
        return mText;
    }

    /**
     * Return the base Paint properties for this layout.
     * Do NOT change the paint, which may result in funny
     * drawing for this layout.
     */
    public final TextPaint getPaint() {
        return mPaint;
    }

    /**
     * Return the width of this layout.
     */
    public final int getWidth() {
        return mWidth;
    }

    /**
     * Return the width to which this Layout is ellipsizing, or
     * {@link #getWidth} if it is not doing anything special.
     */
    public int getEllipsizedWidth() {
        return mWidth;
    }

    /**
     * Increase the width of this layout to the specified width.
     * Be careful to use this only when you know it is appropriate&mdash;
     * it does not cause the text to reflow to use the full new width.
     */
    public final void increaseWidthTo(int wid) {
        if (wid < mWidth) {
            throw new RuntimeException("attempted to reduce Layout width");
        }

        mWidth = wid;
    }

    /**
     * Return the total height of this layout.
     */
    public int getHeight() {
        return getLineTop(getLineCount());
    }

    /**
     * Return the total height of this layout.
     *
     * @param cap if true and max lines is set, returns the height of the layout at the max lines.
     */
    public int getHeight(boolean cap) {
        return getHeight();
    }

    /**
     * Return the base alignment of this layout.
     */
    public final Alignment getAlignment() {
        return mAlignment;
    }

    /**
     * Return the heuristic used to determine paragraph text direction.
     */
    public final TextDirectionHeuristic getTextDirectionHeuristic() {
        return mTextDir;
    }

    /**
     * Return the number of lines of text in this layout.
     */
    public abstract int getLineCount();

    /**
     * Return the vertical position of the top of the specified line
     * (0&hellip;getLineCount()).
     * If the specified line is equal to the line count, returns the
     * bottom of the last line.
     */
    public abstract int getLineTop(int line);

    /**
     * Return the descent of the specified line(0&hellip;getLineCount() - 1).
     */
    public abstract int getLineDescent(int line);

    /**
     * Return the text offset of the beginning of the specified line (
     * 0&hellip;getLineCount()). If the specified line is equal to the line
     * count, returns the length of the text.
     */
    public abstract int getLineStart(int line);

    /**
     * Returns the primary directionality of the paragraph containing the
     * specified line, either 1 for left-to-right lines, or -1 for right-to-left
     * lines (see {@link #DIR_LEFT_TO_RIGHT}, {@link #DIR_RIGHT_TO_LEFT}).
     */
    public abstract int getParagraphDirection(int line);

    /**
     * Returns whether the specified line contains one or more
     * characters that need to be handled specially, like tabs.
     */
    public abstract boolean getLineContainsTab(int line);

    /**
     * Returns the directional run information for the specified line.
     * The array alternates counts of characters in left-to-right
     * and right-to-left segments of the line.
     *
     * <p>NOTE: this is inadequate to support bidirectional text, and will change.
     */
    public abstract Directions getLineDirections(int line);

    /**
     * Returns the (negative) number of extra pixels of ascent padding in the
     * top line of the Layout.
     */
    public abstract int getTopPadding();

    /**
     * Returns the number of extra pixels of descent padding in the
     * bottom line of the Layout.
     */
    public abstract int getBottomPadding();

    /**
     * Returns the left indent for a line.
     */
    public int getIndentAdjust(int line, Alignment alignment) {
        return 0;
    }

    /**
     * Return the offset of the first character to be ellipsized away,
     * relative to the start of the line.  (So 0 if the beginning of the
     * line is ellipsized, not getLineStart().)
     */
    public abstract int getEllipsisStart(int line);

    /**
     * Returns the number of characters to be ellipsized away, or 0 if
     * no ellipsis is to take place.
     */
    public abstract int getEllipsisCount(int line);

    /**
     * Gets the unsigned horizontal extent of the specified line, including
     * leading margin indent, but excluding trailing whitespace.
     */
    public float getLineMax(int line) {
        float margin = getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, false);
        return margin + (signedExtent >= 0 ? signedExtent : -signedExtent);
    }

    /**
     * Gets the unsigned horizontal extent of the specified line, including
     * leading margin indent and trailing whitespace.
     */
    public float getLineWidth(int line) {
        float margin = getParagraphLeadingMargin(line);
        float signedExtent = getLineExtent(line, true);
        return margin + (signedExtent >= 0 ? signedExtent : -signedExtent);
    }

    /**
     * Returns the signed horizontal extent of the specified line, excluding
     * leading margin.  If full is false, excludes trailing whitespace.
     *
     * @param line the index of the line
     * @param full whether to include trailing whitespace
     * @return the extent of the line
     */
    private float getLineExtent(int line, boolean full) {
        final int start = getLineStart(line);
        final int end = full ? getLineEnd(line) : getLineVisibleEnd(line);

        final boolean hasTabs = getLineContainsTab(line);
        TabStops tabStops = null;
        if (hasTabs && mText instanceof Spanned) {
            // Just checking this line should be good enough, tabs should be
            // consistent across all lines in a paragraph.
            TabStopSpan[] tabs = getParagraphSpans((Spanned) mText, start, end, TabStopSpan.class);
            if (tabs != null && tabs.length > 0) {
                tabStops = new TabStops(TAB_INCREMENT, tabs); // XXX should reuse
            }
        }
        final Directions directions = getLineDirections(line);
        // Returned directions can actually be null
        if (directions == null) {
            return 0f;
        }
        final int dir = getParagraphDirection(line);

        final TextLine tl = TextLine.obtain();
        final TextPaint paint = TextPaint.obtain();
        paint.set(mPaint);
        tl.set(paint, mText, start, end, dir, directions, hasTabs, tabStops,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        final float width = tl.metrics(null);
        tl.recycle();
        paint.recycle();
        return width;
    }

    /**
     * Return the text offset after the last character on the specified line.
     */
    public final int getLineEnd(int line) {
        return getLineStart(line + 1);
    }

    /**
     * Return the text offset after the last visible character (so whitespace
     * is not counted) on the specified line.
     */
    public int getLineVisibleEnd(int line) {
        return getLineVisibleEnd(line, getLineStart(line), getLineStart(line + 1));
    }

    private int getLineVisibleEnd(int line, int start, int end) {
        CharSequence text = mText;
        char ch;
        if (line == getLineCount() - 1) {
            return end;
        }

        for (; end > start; end--) {
            ch = text.charAt(end - 1);

            if (ch == '\n') {
                return end - 1;
            }

            if (!LineBreaker.isLineEndSpace(ch)) {
                break;
            }

        }

        return end;
    }

    /**
     * Return the vertical position of the bottom of the specified line.
     */
    public final int getLineBottom(int line) {
        return getLineTop(line + 1);
    }

    /**
     * Return the vertical position of the baseline of the specified line.
     */
    public final int getLineBaseline(int line) {
        // getLineTop(line+1) == getLineBottom(line)
        return getLineTop(line + 1) - getLineDescent(line);
    }

    /**
     * Get the ascent of the text on the specified line.
     * The return value is negative to match the Paint.ascent() convention.
     */
    public final int getLineAscent(int line) {
        // getLineTop(line+1) - getLineDescent(line) == getLineBaseLine(line)
        return getLineTop(line) - (getLineTop(line + 1) - getLineDescent(line));
    }

    public int getOffsetToLeftOf(int offset) {
        return getOffsetToLeftRightOf(offset, true);
    }

    public int getOffsetToRightOf(int offset) {
        return getOffsetToLeftRightOf(offset, false);
    }

    private int getOffsetToLeftRightOf(int caret, boolean toLeft) {
        int line = getLineForOffset(caret);
        int lineStart = getLineStart(line);
        int lineEnd = getLineEnd(line);
        int lineDir = getParagraphDirection(line);

        boolean lineChanged = false;
        boolean advance = toLeft == (lineDir == DIR_RIGHT_TO_LEFT);
        // if walking off line, look at the line we're headed to
        if (advance) {
            if (caret == lineEnd) {
                if (line < getLineCount() - 1) {
                    lineChanged = true;
                    ++line;
                } else {
                    return caret; // at very end, don't move
                }
            }
        } else {
            if (caret == lineStart) {
                if (line > 0) {
                    lineChanged = true;
                    --line;
                } else {
                    return caret; // at very start, don't move
                }
            }
        }

        if (lineChanged) {
            lineStart = getLineStart(line);
            lineEnd = getLineEnd(line);
            int newDir = getParagraphDirection(line);
            if (newDir != lineDir) {
                // unusual case.  we want to walk onto the line, but it runs
                // in a different direction than this one, so we fake movement
                // in the opposite direction.
                toLeft = !toLeft;
                lineDir = newDir;
            }
        }

        Directions directions = getLineDirections(line);

        TextLine tl = TextLine.obtain();
        // XXX: we don't care about tabs
        tl.set(mPaint, mText, lineStart, lineEnd, lineDir, directions, false, null,
                getEllipsisStart(line), getEllipsisStart(line) + getEllipsisCount(line));
        caret = lineStart + tl.getOffsetToLeftRightOf(caret - lineStart, toLeft);
        tl.recycle();
        return caret;
    }

    /**
     * Returns the effective leading margin (unsigned) for this line,
     * taking into account LeadingMarginSpan and LeadingMarginSpan2.
     *
     * @param line the line index
     * @return the leading margin of this line
     */
    private int getParagraphLeadingMargin(int line) {
        //TODO
        return 0;
    }

    /**
     * Return how wide a layout must be in order to display the specified text with one line per
     * paragraph.
     *
     * <p>As of O, Uses
     * {@link TextDirectionHeuristics#FIRSTSTRONG_LTR} as the default text direction heuristics. In
     * the earlier versions uses {@link TextDirectionHeuristics#LTR} as the default.</p>
     */
    public static float getDesiredWidth(CharSequence source,
                                        TextPaint paint) {
        return getDesiredWidth(source, 0, source.length(), paint);
    }

    /**
     * Return how wide a layout must be in order to display the specified text slice with one
     * line per paragraph.
     *
     * <p>As of O, Uses
     * {@link TextDirectionHeuristics#FIRSTSTRONG_LTR} as the default text direction heuristics. In
     * the earlier versions uses {@link TextDirectionHeuristics#LTR} as the default.</p>
     */
    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint) {
        return getDesiredWidth(source, start, end, paint, TextDirectionHeuristics.FIRSTSTRONG_LTR);
    }

    /**
     * Return how wide a layout must be in order to display the
     * specified text slice with one line per paragraph.
     *
     * @hide
     */
    public static float getDesiredWidth(CharSequence source, int start, int end, TextPaint paint,
                                        TextDirectionHeuristic textDir) {
        return getDesiredWidthWithLimit(source, start, end, paint, textDir, Float.MAX_VALUE);
    }

    /**
     * Return how wide a layout must be in order to display the
     * specified text slice with one line per paragraph.
     * <p>
     * If the measured width exceeds given limit, returns limit value instead.
     *
     * @hide
     */
    public static float getDesiredWidthWithLimit(CharSequence source, int start, int end,
                                                 TextPaint paint, TextDirectionHeuristic textDir, float upperLimit) {
        float need = 0;

        int next;
        for (int i = start; i <= end; i = next) {
            next = TextUtils.indexOf(source, '\n', i, end);

            if (next < 0)
                next = end;

            // note, omits trailing paragraph char
            float w = measurePara(paint, source, i, next, textDir);
            if (w > upperLimit) {
                return upperLimit;
            }

            if (w > need)
                need = w;

            next++;
        }

        return need;
    }

    private static float measurePara(TextPaint paint, CharSequence text, int start, int end,
                                     TextDirectionHeuristic textDir) {
        MeasuredParagraph mt = null;
        TextLine tl = TextLine.obtain();
        try {
            mt = MeasuredParagraph.buildForBidi(text, start, end, textDir, mt);
            final char[] chars = mt.getChars();
            final int len = chars.length;
            final Directions directions = mt.getDirections(0, len);
            final int dir = mt.getParagraphDir();
            boolean hasTabs = false;
            TabStops tabStops = null;
            // leading margins should be taken into account when measuring a paragraph
            int margin = 0;
            /*if (text instanceof Spanned) {
                Spanned spanned = (Spanned) text;
                LeadingMarginSpan[] spans = getParagraphSpans(spanned, start, end,
                        LeadingMarginSpan.class);
                for (LeadingMarginSpan lms : spans) {
                    margin += lms.getLeadingMargin(true);
                }
            }*/
            for (char c : chars) {
                if (c == '\t') {
                    hasTabs = true;
                    if (text instanceof Spanned spanned) {
                        int spanEnd = spanned.nextSpanTransition(start, end,
                                TabStopSpan.class);
                        TabStopSpan[] spans = getParagraphSpans(spanned, start, spanEnd,
                                TabStopSpan.class);
                        if (spans != null && spans.length > 0) {
                            tabStops = new TabStops(TAB_INCREMENT, spans);
                        }
                    }
                    break;
                }
            }
            tl.set(paint, text, start, end, dir, directions, hasTabs, tabStops,
                    0 /* ellipsisStart */, 0 /* ellipsisEnd */);
            return margin + Math.abs(tl.metrics(null));
        } finally {
            tl.recycle();
            if (mt != null) {
                mt.recycle();
            }
        }
    }

    /**
     * Returns the same as <code>text.getSpans()</code>, except where
     * <code>start</code> and <code>end</code> are the same and are not
     * at the very beginning of the text, in which case an empty array
     * is returned instead.
     * <p>
     * This is needed because of the special case that <code>getSpans()</code>
     * on an empty range returns the spans adjacent to that range, which is
     * primarily for the sake of <code>TextWatchers</code> so they will get
     * notifications when text goes from empty to non-empty.  But it also
     * has the unfortunate side effect that if the text ends with an empty
     * paragraph, that paragraph accidentally picks up the styles of the
     * preceding paragraph (even though those styles will not be picked up
     * by new text that is inserted into the empty paragraph).
     * <p>
     * The reason it just checks whether <code>start</code> and <code>end</code>
     * is the same is that the only time a line can contain 0 characters
     * is if it is the final paragraph of the Layout; otherwise any line will
     * contain at least one printing or newline character.  The reason for the
     * additional check if <code>start</code> is greater than 0 is that
     * if the empty paragraph is the entire content of the buffer, paragraph
     * styles that are already applied to the buffer will apply to text that
     * is inserted into it.
     */
    @Nullable
    static <T> T[] getParagraphSpans(@Nonnull Spanned text, int start, int end, Class<T> type) {
        if (start == end && start > 0) {
            return null;
        }

        if (text instanceof SpannableStringBuilder) {
            return ((SpannableStringBuilder) text).getSpans(start, end, type, false, null);
        } else {
            return text.getSpans(start, end, type, null);
        }
    }

    private void ellipsize(int start, int end, int line,
                           char[] dest, int destoff, TextUtils.TruncateAt method) {
        final int ellipsisCount = getEllipsisCount(line);
        if (ellipsisCount == 0) {
            return;
        }
        final int ellipsisStart = getEllipsisStart(line);
        final int lineStart = getLineStart(line);

        final String ellipsisString = TextUtils.getEllipsisString(method);
        final int ellipsisStringLen = ellipsisString.length();
        // Use the ellipsis string only if there are that at least as many characters to replace.
        final boolean useEllipsisString = ellipsisCount >= ellipsisStringLen;
        final int min = Math.max(0, start - ellipsisStart - lineStart);
        final int max = Math.min(ellipsisCount, end - ellipsisStart - lineStart);

        for (int i = min; i < max; i++) {
            final char c;
            if (useEllipsisString && i < ellipsisStringLen) {
                c = ellipsisString.charAt(i);
            } else {
                c = TextUtils.ELLIPSIS_FILLER;
            }

            final int a = i + ellipsisStart + lineStart;
            dest[destoff + a - start] = c;
        }
    }

    public enum Alignment {
        ALIGN_NORMAL,
        ALIGN_OPPOSITE,
        ALIGN_CENTER,
        // internal use
        ALIGN_LEFT,
        // internal use
        ALIGN_RIGHT
    }

    static class Ellipsizer implements CharSequence, GetChars {

        CharSequence mText;
        Layout mLayout;
        int mWidth;
        TextUtils.TruncateAt mMethod;

        public Ellipsizer(CharSequence s) {
            mText = s;
        }

        @Override
        public char charAt(int off) {
            char[] buf = TextUtils.obtain(1);
            getChars(off, off + 1, buf, 0);
            char ret = buf[0];

            TextUtils.recycle(buf);
            return ret;
        }

        @Override
        public void getChars(int start, int end, char[] dest, int destoff) {
            int line1 = mLayout.getLineForOffset(start);
            int line2 = mLayout.getLineForOffset(end);

            TextUtils.getChars(mText, start, end, dest, destoff);

            for (int i = line1; i <= line2; i++) {
                mLayout.ellipsize(start, end, i, dest, destoff, mMethod);
            }
        }

        @Override
        public int length() {
            return mText.length();
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[end - start];
            getChars(start, end, s, 0);
            return new String(s);
        }

        @Override
        public String toString() {
            char[] s = new char[length()];
            getChars(0, length(), s, 0);
            return new String(s);
        }
    }

    static class SpannedEllipsizer extends Ellipsizer implements Spanned {

        private final Spanned mSpanned;

        public SpannedEllipsizer(CharSequence display) {
            super(display);
            mSpanned = (Spanned) display;
        }

        @Nullable
        @Override
        public <T> T[] getSpans(int start, int end, Class<? extends T> type, @Nullable List<T> out) {
            return mSpanned.getSpans(start, end, type, out);
        }

        @Override
        public int getSpanStart(@Nonnull Object tag) {
            return mSpanned.getSpanStart(tag);
        }

        @Override
        public int getSpanEnd(@Nonnull Object tag) {
            return mSpanned.getSpanEnd(tag);
        }

        @Override
        public int getSpanFlags(@Nonnull Object tag) {
            return mSpanned.getSpanFlags(tag);
        }

        @Override
        public int nextSpanTransition(int start, int limit, Class<?> type) {
            return mSpanned.nextSpanTransition(start, limit, type);
        }

        @Override
        public CharSequence subSequence(int start, int end) {
            char[] s = new char[end - start];
            getChars(start, end, s, 0);

            SpannableString ss = new SpannableString(new String(s));
            TextUtils.copySpansFrom(mSpanned, start, end, Object.class, ss, 0);
            return ss;
        }
    }
}