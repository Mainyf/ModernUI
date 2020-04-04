/*
 * Modern UI.
 * Copyright (C) 2019 BloCamLimb. All rights reserved.
 *
 * Modern UI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Modern UI is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Modern UI. If not, see <https://www.gnu.org/licenses/>.
 */

package icyllis.modernui.gui.scroll;

import com.mojang.blaze3d.systems.RenderSystem;
import icyllis.modernui.gui.master.GlobalModuleManager;
import icyllis.modernui.gui.master.IDraggable;
import icyllis.modernui.gui.master.Module;
import icyllis.modernui.gui.widget.FlexibleWidget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.function.Function;

public class ScrollWindow<T extends ScrollGroup> extends FlexibleWidget {

    protected final Function<Integer, Float> xResizer, yResizer;

    protected final Function<Integer, Float> wResizer, hResizer;

    protected final Module module;

    protected final Minecraft minecraft;

    public final int borderThickness = 6;

    protected float centerX;

    protected float visibleHeight;

    protected float scrollAmount = 0f;

    protected final ScrollBar scrollbar;

    protected final ScrollController controller;

    protected final ScrollList<T> scrollList;

    public ScrollWindow(Module module, Function<Integer, Float> xResizer, Function<Integer, Float> yResizer, Function<Integer, Float> wResizer, Function<Integer, Float> hResizer) {
        this.minecraft = Minecraft.getInstance();
        this.module = module;
        this.xResizer = xResizer;
        this.yResizer = yResizer;
        this.wResizer = wResizer;
        this.hResizer = hResizer;
        this.scrollbar = new ScrollBar(this);
        this.controller = new ScrollController(this::callbackScrollAmount);
        this.scrollList = new ScrollList<>(this);
    }

    @Override
    public final void draw(float time) {
        controller.update(time);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();

        double scale = minecraft.getMainWindow().getGuiScaleFactor();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int) (x1 * scale), (int) (minecraft.getMainWindow().getFramebufferHeight() - (y2 * scale)),
                    (int) (width * scale), (int) (height * scale));

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableAlphaTest();

        RenderSystem.enableTexture();

        RenderSystem.pushMatrix();
        RenderSystem.translatef(0, -getVisibleOffset(), 0);
        scrollList.draw(time);
        RenderSystem.popMatrix();

        RenderSystem.disableTexture();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(x1, y1 + borderThickness, 0.0D).color(0, 0, 0, 0).endVertex();
        bufferBuilder.pos(x2, y1 + borderThickness, 0.0D).color(0, 0, 0, 0).endVertex();
        bufferBuilder.pos(x2, y1, 0.0D).color(0, 0, 0, 128).endVertex();
        bufferBuilder.pos(x1, y1, 0.0D).color(0, 0, 0, 128).endVertex();
        tessellator.draw();
        bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferBuilder.pos(x1, y2, 0.0D).color(0, 0, 0, 128).endVertex();
        bufferBuilder.pos(x2, y2, 0.0D).color(0, 0, 0, 128).endVertex();
        bufferBuilder.pos(x2, y2 - borderThickness, 0.0D).color(0, 0, 0, 0).endVertex();
        bufferBuilder.pos(x1, y2 - borderThickness, 0.0D).color(0, 0, 0, 0).endVertex();
        tessellator.draw();
        RenderSystem.shadeModel(GL11.GL_FLAT);

        scrollbar.draw(time);

        RenderSystem.enableTexture();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        drawEndExtra();
    }

    public void drawEndExtra() {

    }

    @Override
    public final void setPos(float x, float y) {
        throw new RuntimeException("Scroll window doesn't allow to set pos");
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);
        this.x1 = xResizer.apply(width);
        this.y1 = yResizer.apply(height);
        this.width = wResizer.apply(width);
        this.height = hResizer.apply(height);
        this.x2 = x1 + this.width;
        this.y2 = y1 + this.height;
        this.centerX = x1 + this.width / 2f;
        this.visibleHeight = this.height - borderThickness * 2;
        this.scrollbar.setPos(this.x2 - scrollbar.barThickness - 1, y1 + 1);
        this.scrollbar.setMaxLength(this.height - 2);
        this.layoutList();
        this.scrollSmooth(0);
        // Am I cute?
    }

    @Override
    public boolean updateMouseHover(double mouseX, double mouseY) {
        if (super.updateMouseHover(mouseX, mouseY)) {
            if (scrollbar.updateMouseHover(mouseX, mouseY)) {
                scrollList.setMouseHoverExit();
            } else {
                scrollList.updateMouseHover(mouseX, mouseY + getVisibleOffset());
            }
            return true;
        }
        return false;
    }

    @Override
    public void setMouseHoverExit() {
        super.setMouseHoverExit();
        scrollbar.setMouseHoverExit();
        scrollList.setMouseHoverExit();
    }

    @Override
    public boolean mouseClicked(int mouseButton) {
        if (scrollbar.mouseClicked(mouseButton)) {
            return true;
        }
        return scrollList.mouseClicked(mouseButton);
    }

    @Override
    public boolean mouseReleased(int mouseButton) {
        if (scrollbar.mouseReleased(mouseButton)) {
            return true;
        }
        return scrollList.mouseReleased(mouseButton);
    }

    @Override
    public boolean mouseScrolled(double amount) {
        if (scrollbar.mouseScrolled(amount)) {
            return true;
        }
        if (scrollList.mouseScrolled(amount)) {
            return true;
        }
        scrollSmooth(Math.round(amount * -20f));
        return true;
    }

    protected void scrollSmooth(float delta) {
        float amount = MathHelper.clamp(controller.getTargetValue() + delta, 0, getMaxScrollAmount());
        controller.setTargetValue(amount);
    }

    protected void scrollDirect(float delta) {
        float amount = Math.round(MathHelper.clamp(controller.getTargetValue() + delta, 0, getMaxScrollAmount()));
        controller.setTargetValueDirect(amount);
        callbackScrollAmount(amount);
    }

    /**
     * Controlled by scroll controller, do not call this manually
     */
    private void callbackScrollAmount(float scrollAmount) {
        this.scrollAmount = scrollAmount;
        updateScrollBarOffset();
        updateScrollList();
        GlobalModuleManager.INSTANCE.refreshMouse();
    }

    /**
     * Get total entries height
     */
    public float getTotalHeight() {
        return scrollList.getMaxHeight();
    }

    public float getVisibleHeight() {
        return visibleHeight;
    }

    public float getVisibleOffset() {
        return scrollAmount - borderThickness;
    }

    public float getMaxScrollAmount() {
        return Math.max(0, getTotalHeight() - getVisibleHeight());
    }

    public float getScrollPercentage() {
        float max = getMaxScrollAmount();
        if (max == 0) {
            return 0;
        }
        return scrollAmount / max;
    }

    public void updateScrollBarOffset() {
        scrollbar.setBarOffset(getScrollPercentage());
    }

    public void updateScrollBarLength() {
        float v = getVisibleHeight();
        float t = getTotalHeight();
        boolean renderBar = t > v;
        scrollbar.setVisible(renderBar);
        if (renderBar) {
            float p = v / t;
            scrollbar.setBarLength(p);
        }
    }

    public void updateScrollList() {
        scrollList.updateVisible(y1, getVisibleOffset(), y2);
    }

    public void layoutList() {
        scrollList.layoutGroups(getLeft(), getRight(), getTop());
        onTotalHeightChanged();
    }

    private void onTotalHeightChanged() {
        updateScrollBarLength();
        updateScrollBarOffset();
        updateScrollList();
    }

    public void addGroups(Collection<T> collection) {
        scrollList.addGroups(collection);
    }

    public void setDraggable(@Nullable IDraggable draggable) {
        module.setDraggable(draggable);
    }
}