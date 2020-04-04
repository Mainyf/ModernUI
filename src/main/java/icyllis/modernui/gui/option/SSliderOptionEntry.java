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

package icyllis.modernui.gui.option;

import icyllis.modernui.font.TextAlign;
import icyllis.modernui.gui.widget.SliderSmooth;
import icyllis.modernui.gui.scroll.SettingScrollWindow;
import net.minecraft.util.math.MathHelper;

import java.util.function.Consumer;
import java.util.function.Function;

public class SSliderOptionEntry extends OptionEntry {

    private SliderSmooth slider;

    private double minValue;

    private double maxValue;

    private float stepSize;

    private double currentValue;

    private Consumer<Double> saveOptionFunc;

    private Function<Double, String> displayStringFunc;

    private String displayString;

    public SSliderOptionEntry(SettingScrollWindow window, String optionTitle, double minValue, double maxValue, float stepSize, double currentValue, Consumer<Double> saveOption, Function<Double, String> displayStringFunc) {
        super(window, optionTitle);
        currentValue = MathHelper.clamp(currentValue, minValue, maxValue);
        slider = new SliderSmooth(84, (currentValue - minValue) / (maxValue - minValue), this::onPercentageChange);
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.stepSize = stepSize == 0 ? 0.01f / (float) (maxValue - minValue) : stepSize;
        this.currentValue = stepSize * (Math.round(currentValue / stepSize));
        this.saveOptionFunc = saveOption;
        this.displayStringFunc = displayStringFunc;
        this.displayString = displayStringFunc.apply(currentValue);
        
    }

    public SSliderOptionEntry(SettingScrollWindow window, String optionTitle, double minValue, double maxValue, float stepSize, double currentValue, Consumer<Double> saveOption) {
        this(window, optionTitle, minValue, maxValue, stepSize, currentValue, saveOption, String::valueOf);
    }

    @Override
    public void drawExtra(float centerX, float y, float currentTime) {
        slider.setPos(centerX + 40, y + 9);
        slider.draw(currentTime);
        fontRenderer.drawString(displayString, centerX + 154, y + 6, titleBrightness, 1.0f, TextAlign.RIGHT);
    }

    @Override
    public void mouseMoved(double deltaCenterX, double deltaY, double mouseX, double mouseY) {
        slider.mouseMoved(mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(double deltaCenterX, double deltaY, double mouseX, double mouseY, int mouseButton) {
        return slider.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseReleased(double deltaCenterX, double deltaY, double mouseX, double mouseY, int mouseButton) {
        return slider.mouseReleased(mouseX, mouseY, mouseButton);
    }

    @Override
    public boolean mouseDragged(double deltaCenterX, double deltaY, double mouseX, double mouseY, int mouseButton, double deltaMouseX, double deltaMouseY) {
        return slider.mouseDragged(mouseX, mouseY, mouseButton, deltaMouseX, deltaMouseY);
    }

    protected void onPercentageChange(double percentage) {
        currentValue = minValue + (maxValue - minValue) * percentage;
        currentValue = stepSize * (Math.round(currentValue / stepSize));
        displayString = displayStringFunc.apply(currentValue);
        saveOption();
    }

    public void saveOption() {
        saveOptionFunc.accept(currentValue);
    }
}