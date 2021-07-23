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

import icyllis.modernui.annotation.RenderThread;
import icyllis.modernui.graphics.texture.Texture2D;
import icyllis.modernui.platform.Bitmap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.function.IntFunction;

import static icyllis.modernui.graphics.GLWrapper.GL_ALPHA;
import static icyllis.modernui.graphics.GLWrapper.GL_UNSIGNED_BYTE;

/**
 * Maintains a font texture atlas, which is specified with a font family, size and style.
 * The glyphs in the texture are tightly packed, dynamically generated with mipmaps. Each
 * glyph is represented as a {@link TexturedGlyph}.
 * <p>
 * The initial texture size is 256*256, and each enlargement double the height and width
 * alternately. The max texture size would be 16384*16384 and the image is 8-bit grayscale.
 * The OpenGL texture id may change due to enlarging the texture size.
 *
 * @see GlyphManager
 */
@RenderThread
public class FontAtlas {

    /**
     * The width in pixels of a transparent border between individual glyphs in the atlas.
     * This border keeps neighboring glyphs from "bleeding through" when mipmap used.
     */
    private static final int GLYPH_BORDER = 1;
    private static final int INITIAL_SIZE = 256;
    private static final int MIPMAP_LEVEL = 4;

    private static final IntFunction<TexturedGlyph> sFactory = i -> new TexturedGlyph();

    // texture object is immutable, but texture ID (the int) can change by resizing
    public final Texture2D mTexture = new Texture2D();

    // OpenHashMap uses less memory than RBTree/AVLTree, but higher than ArrayMap
    private final Int2ObjectMap<TexturedGlyph> mGlyphs = new Int2ObjectOpenHashMap<>();

    // position for next glyph sprite
    private int mPosX = GLYPH_BORDER;
    private int mPosY = GLYPH_BORDER;

    // max height of current line
    private int mLineHeight;

    // current texture size
    private int mWidth;
    private int mHeight;

    // create from any thread
    public FontAtlas() {
    }

    @Nonnull
    public TexturedGlyph getGlyph(int glyphCode) {
        return mGlyphs.computeIfAbsent(glyphCode, sFactory);
    }

    void export() {
        try {
            Bitmap.download(Bitmap.Format.RGBA, mTexture, false)
                    .saveDialog(Bitmap.SaveFormat.PNG, 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stitch(TexturedGlyph glyph, long data) {
        if (mWidth == 0) {
            resize();
        }
        if (mPosX + glyph.width + GLYPH_BORDER >= mWidth) {
            mPosX = GLYPH_BORDER;
            // we are on the right half
            if (mWidth == mHeight && mWidth != INITIAL_SIZE) {
                mPosX += mWidth >> 1;
            }
            mPosY += mLineHeight + GLYPH_BORDER * 2;
            mLineHeight = 0;
        }
        if (mPosY + glyph.height + GLYPH_BORDER >= mHeight) {
            // move to the right half
            if (mWidth != mHeight) {
                mPosX = GLYPH_BORDER + mWidth;
                mPosY = GLYPH_BORDER;
            }
            resize();
        }

        mTexture.upload(0, mPosX, mPosY, glyph.width, glyph.height, glyph.width,
                0, 0, 1, GL_ALPHA, GL_UNSIGNED_BYTE, data);
        mTexture.generateMipmap();

        glyph.u1 = (float) mPosX / mWidth;
        glyph.v1 = (float) mPosY / mHeight;
        glyph.u2 = (float) (mPosX + glyph.width) / mWidth;
        glyph.v2 = (float) (mPosY + glyph.height) / mHeight;

        mPosX += glyph.width + GLYPH_BORDER * 2;
        mLineHeight = Math.max(mLineHeight, glyph.height);
    }

    private void resize() {
        // never initialized
        if (mWidth == 0) {
            mWidth = mHeight = INITIAL_SIZE;
            mTexture.initCompat(GL_ALPHA, INITIAL_SIZE, INITIAL_SIZE, MIPMAP_LEVEL);
            // we have border that not upload data, so generate mipmap may leave undefined data
            mTexture.clear(0);
        } else {
            final boolean vertical;
            if (mHeight != mWidth) {
                mWidth <<= 1;
                for (TexturedGlyph glyph : mGlyphs.values()) {
                    glyph.u1 *= 0.5;
                    glyph.u2 *= 0.5;
                }
                vertical = false;
            } else {
                mHeight <<= 1;
                for (TexturedGlyph glyph : mGlyphs.values()) {
                    glyph.v1 *= 0.5;
                    glyph.v2 *= 0.5;
                }
                vertical = true;
            }

            mTexture.resize(mWidth, mHeight, true);
            if (vertical) {
                mTexture.clear(0, 0, mHeight >> 1, mWidth, mHeight >> 1);
            } else {
                mTexture.clear(0, mWidth >> 1, 0, mWidth >> 1, mHeight);
            }
            // we later generate mipmap
        }
    }
}