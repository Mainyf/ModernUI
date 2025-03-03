/*
 * Modern UI.
 * Copyright (C) 2019-2022 BloCamLimb. All rights reserved.
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

package icyllis.modernui.textmc;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import icyllis.modernui.ModernUI;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.VanillaPackResources;
import net.minecraft.server.packs.resources.ResourceProvider;
import net.minecraft.server.packs.resources.SimpleResource;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import static icyllis.modernui.ModernUI.*;

/**
 * Fast and modern text render type.
 */
public class TextRenderType extends RenderType {

    private static final ResourceLocation
            SHADER_RL = new ResourceLocation(ModernUI.ID, "rendertype_modern_text"),
            SHADER_SEE_THROUGH_RL = new ResourceLocation(ModernUI.ID, "rendertype_modern_text_see_through");

    static final ShaderStateShard
            RENDERTYPE_MODERN_TEXT = new ShaderStateShard(TextRenderType::getShader),
            RENDERTYPE_MODERN_TEXT_SEE_THROUGH = new ShaderStateShard(TextRenderType::getShaderSeeThrough);

    private static volatile ShaderInstance sShader;
    private static volatile ShaderInstance sShaderSeeThrough;

    /**
     * Texture id to render type map
     */
    private static final Int2ObjectMap<TextRenderType> TYPES = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<TextRenderType> SEE_THROUGH_TYPES = new Int2ObjectOpenHashMap<>();

    /**
     * Only the texture id is different, the rest state are same
     */
    private static final ImmutableList<RenderStateShard> GENERAL_STATES;
    private static final ImmutableList<RenderStateShard> SEE_THROUGH_STATES;

    static {
        GENERAL_STATES = ImmutableList.of(
                RENDERTYPE_MODERN_TEXT,
                TRANSLUCENT_TRANSPARENCY,
                LEQUAL_DEPTH_TEST,
                CULL,
                LIGHTMAP,
                NO_OVERLAY,
                NO_LAYERING,
                MAIN_TARGET,
                DEFAULT_TEXTURING,
                COLOR_DEPTH_WRITE,
                DEFAULT_LINE
        );
        SEE_THROUGH_STATES = ImmutableList.of(
                RENDERTYPE_MODERN_TEXT_SEE_THROUGH,
                TRANSLUCENT_TRANSPARENCY,
                NO_DEPTH_TEST,
                CULL,
                LIGHTMAP,
                NO_OVERLAY,
                NO_LAYERING,
                MAIN_TARGET,
                DEFAULT_TEXTURING,
                COLOR_WRITE,
                DEFAULT_LINE
        );
    }

    private final int hashCode;

    private TextRenderType(int texture) {
        super("modern_text",
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, false, true,
                () -> {
                    GENERAL_STATES.forEach(RenderStateShard::setupRenderState);
                    RenderSystem.enableTexture();
                    RenderSystem.setShaderTexture(0, texture);
                },
                () -> GENERAL_STATES.forEach(RenderStateShard::clearRenderState));
        this.hashCode = Objects.hash(super.hashCode(), GENERAL_STATES, texture);
    }

    private TextRenderType(int texture, String t) {
        super(t,
                DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
                VertexFormat.Mode.QUADS, 256, false, true,
                () -> {
                    SEE_THROUGH_STATES.forEach(RenderStateShard::setupRenderState);
                    RenderSystem.enableTexture();
                    RenderSystem.setShaderTexture(0, texture);
                },
                () -> SEE_THROUGH_STATES.forEach(RenderStateShard::clearRenderState));
        this.hashCode = Objects.hash(super.hashCode(), SEE_THROUGH_STATES, texture);
    }

    @Nonnull
    static TextRenderType getOrCreate(int texture, boolean seeThrough) {
        TextRenderType type;
        if (seeThrough) {
            // do not use lambdas for deferred construction
            type = SEE_THROUGH_TYPES.get(texture);
            if (type == null) {
                type = new TextRenderType(texture, "modern_text_see_through");
                SEE_THROUGH_TYPES.put(texture, type);
            }
        } else {
            type = TYPES.get(texture);
            if (type == null) {
                type = new TextRenderType(texture);
                TYPES.put(texture, type);
            }
        }
        return type;
    }

    static void clear() {
        TYPES.clear();
        SEE_THROUGH_TYPES.clear();
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * Singleton, the constructor is private
     */
    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    public static ShaderInstance getShader() {
        return sShader;
    }

    public static ShaderInstance getShaderSeeThrough() {
        return sShaderSeeThrough;
    }

    /**
     * Load Modern UI text shaders for early text rendering. These shaders are loaded only once,
     * and cannot be overridden by other resource packs or reloaded. They will not be closed
     * unless Minecraft is quited.
     */
    public static synchronized void preloadShaders() {
        if (sShader != null) {
            return;
        }
        final VanillaPackResources resources = Minecraft.getInstance().getClientPackSource().getVanillaPack();
        final ResourceProvider provider = location -> {
            // don't worry
            InputStream stream = ModernUITextMC.class
                    .getResourceAsStream("/assets/" + location.getNamespace() + "/" + location.getPath());
            if (stream == null) {
                return resources.getResource(location);
            }
            return new SimpleResource(ModernUI.ID, location, stream, null);
        };
        try {
            sShader = new ShaderInstance(provider, SHADER_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
            sShaderSeeThrough = new ShaderInstance(provider, SHADER_SEE_THROUGH_RL,
                    DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP);
        } catch (IOException e) {
            throw new IllegalStateException("Bad text shaders", e);
        }
        LOGGER.info(MARKER, "Preloaded modern text shaders");
    }
}
