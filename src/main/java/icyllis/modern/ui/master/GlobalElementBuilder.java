package icyllis.modern.ui.master;

import icyllis.modern.api.element.INavigationBuilder;
import icyllis.modern.api.element.ITextLineBuilder;
import icyllis.modern.api.element.ITextureBuilder;
import icyllis.modern.api.internal.IElementBuilder;
import icyllis.modern.ui.button.InputBox;
import icyllis.modern.ui.button.NavigationButton;
import icyllis.modern.ui.element.UIBackground;
import icyllis.modern.ui.element.UITextLine;
import icyllis.modern.ui.element.UITexture;

public class GlobalElementBuilder implements IElementBuilder {

    public static final GlobalElementBuilder INSTANCE = new GlobalElementBuilder();

    private MasterModule receiver;
    private IMasterScreen master;

    public void setReceiver(MasterModule receiver, IMasterScreen master) {
        this.receiver = receiver;
        this.master = master;
    }

    @Override
    public void defaultBackground() {
        UIBackground u = new UIBackground();
        receiver.add(u);
    }

    @Override
    public ITextLineBuilder textLine() {
        UITextLine u = new UITextLine();
        receiver.add(u);
        return u;
    }

    @Override
    public ITextureBuilder texture() {
        UITexture u = new UITexture();
        receiver.add(u);
        return u;
    }

    @Override
    public INavigationBuilder navigation() {
        NavigationButton b = new NavigationButton();
        receiver.add(b);
        master.addChild(b);
        return b;
    }

    @Override
    public void input() {
        InputBox u = new InputBox();
        receiver.add(u);
        master.addChild(u);
    }
}