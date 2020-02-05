package icyllis.modern.ui.master;

import icyllis.modern.api.global.IModuleFactory;
import icyllis.modern.system.ModernUI;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

@OnlyIn(Dist.CLIENT)
public class UniversalModernScreen extends Screen implements IMasterScreen {

    static final StringTextComponent EMPTY_TITLE = new StringTextComponent("");

    private GlobalModuleManager manager = GlobalModuleManager.INSTANCE;

    public UniversalModernScreen(Consumer<IModuleFactory> factory) {
        super(EMPTY_TITLE);
        factory.accept(manager);
    }

    @Override
    protected void init() {
        manager.build(this, width, height);
    }

    @Override
    public void resize(@Nonnull Minecraft minecraft, int width, int height) {
        manager.resize(width, height);
    }

    @Override
    public void mouseMoved(double xPos, double p_212927_3_) {

    }

    @Override
    public void addChild(IGuiEventListener eventListener) {
        children.add(eventListener);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        manager.draw();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void removed() {
        manager.clear();
    }

}
