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

package icyllis.modernui.ui.animation;

@FunctionalInterface
public interface ITimeInterpolator {

    /**
     * Default interpolator, you don't need to call this if you want to keep default
     */
    ITimeInterpolator LINEAR = in -> in;

    /**
     * From slow to fast, seldom used
     */
    ITimeInterpolator ACCELERATE = in -> in * in;

    /**
     * From fast to slow, commonly used
     */
    ITimeInterpolator DECELERATE = in -> 1.0f - (1.0f - in) * (1.0f - in);

    /**
     * Slow to fast to slow
     */
    ITimeInterpolator ACC_DEC = in -> (float) (Math.cos((in + 1) * Math.PI) / 2.0d) + 0.5f;

    /**
     * From fast to slow, it's better to use {@link #DECELERATE}, they are similar
     */
    ITimeInterpolator SINE = in -> (float) Math.sin(Math.PI / 2.0d * in);

    /**
     * Get interpolation value
     *
     * @param progress [0.0, 1.0] determined by timeline, 0.0 represents
     *                 the start and 1.0 represents the end
     * @return interpolated value
     */
    float getInterpolation(float progress);
}