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

package icyllis.modernui.system;

import icyllis.modernui.gui.master.GlobalModuleManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.ModList;
import net.optifine.shaders.gui.GuiShaders;

import java.lang.reflect.InvocationTargetException;

public class ModIntegration {

    // client only
    public static final boolean optifineLoaded;

    static {
        boolean of = false;
        try {
            Class<?> clazz = Class.forName("optifine.Installer");
            of = true;
            String ver = (String) clazz.getMethod("getOptiFineVersion").invoke(null);
            ModernUI.LOGGER.info(ModernUI.MARKER, "OptiFine loaded, version : {}", ver);
        } catch (ClassNotFoundException | NoSuchMethodException ignored) {

        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        optifineLoaded = of;
    }

    public static void init() {
        ModList modList = ModList.get();

    }

    public static class OptiFine {

        public static void openShadersGui() {
            Minecraft.getInstance().displayGuiScreen(
                    new GuiShaders(GlobalModuleManager.INSTANCE.getModernScreen(), Minecraft.getInstance().gameSettings));
        }
    }
}
