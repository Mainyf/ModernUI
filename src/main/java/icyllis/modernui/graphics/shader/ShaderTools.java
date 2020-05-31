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

package icyllis.modernui.graphics.shader;

import icyllis.modernui.graphics.shader.program.*;
import icyllis.modernui.system.ModernUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;
import net.minecraftforge.resource.VanillaResourceType;

import javax.annotation.Nonnull;
import java.util.function.Predicate;

@OnlyIn(Dist.CLIENT)
public class ShaderTools {

    public static void init() {
        IResourceManager manager = Minecraft.getInstance().getResourceManager();
        if (manager instanceof IReloadableResourceManager) {
            ((IReloadableResourceManager) manager).addReloadListener(
                    (ISelectiveResourceReloadListener) ShaderTools::compileShaders);
        }
    }

    private static void compileShaders(IResourceManager manager, @Nonnull Predicate<IResourceType> typePredicate) {
        if (typePredicate.test(VanillaResourceType.SHADERS)) {
            RingShader.INSTANCE.compile(manager);
            RoundedRectShader.INSTANCE.compile(manager);
            RoundedRectFrameShader.INSTANCE.compile(manager);
            CircleShader.INSTANCE.compile(manager);
            FeatheredRectShader.INSTANCE.compile(manager);
            ModernUI.LOGGER.debug(ShaderProgram.MARKER, "Shaders have been compiled, total : {}", ShaderProgram.compileCount);
            ShaderProgram.compileCount = 0;
        }
    }

    public static <T extends ShaderProgram> void useShader(@Nonnull T shader) {
        int program = shader.getProgram();
        ShaderLinkHelper.func_227804_a_(program);
    }

    public static void releaseShader() {
        ShaderLinkHelper.func_227804_a_(0);
    }

    /*@Nonnull
    public static UniformFloat getUniformFloat(@Nonnull ShaderProgram shader, String name) {
        return new UniformFloat(GL20.glGetUniformLocation(shader.getProgram(), name));
    }

    @Nonnull
    public static UniformMatrix4f getUniformMatrix4f(@Nonnull ShaderProgram shader, String name) {
        int loc = GL20.glGetUniformLocation(shader.getProgram(), name);
        if (loc == -1) {
            throw new RuntimeException();
        }
        return new UniformMatrix4f(loc);
    }*/
}
