import net.minecraft.client.Minecraft;

/**
 * Obfuscation layer for gui buttons.
 * @author Jimeo Wan
 *
 */
public class InvTweaksObfuscationGuiButton extends atb /* GuiButton */ {

    public InvTweaksObfuscationGuiButton(int arg0, int arg1, int arg2, int arg3, int arg4, String arg5) {
        super(arg0, arg1, arg2, arg3, arg4, arg5);
    }

    protected void drawGradientRect(
            int i, int j, int k, int l, int m, int n) {
        a(i, j, k, l, m, n);
    }
    protected void drawTexturedModalRect(
            int xPosition, int i, int j, int k, int l, int m) {
        b(xPosition, i, j, k, l, m);
    }
    protected void drawRect(int i, int j, int k, int l, int textColor) {
        a(i, j, k, l, textColor);
    }

    protected String getDisplayString() {
        return e;
    }
    protected int getTexture(Minecraft mc, String texture) {
        return mc.o.b(texture); // renderengine.getTexture
    }
    protected int getHoverState(boolean mouseOverButton) {
        return this.a(mouseOverButton);
    }
    
    protected boolean isEnabled2() {
        return this.h;
    }
    protected boolean isEnabled() {
        return this.g;
    }
    
    protected int getXPosition() {
        return c;
    }
    protected int getYPosition() {
        return d;
    }
    protected int getWidth() {
        return a;
    }
    protected int getHeight() {
        return b;
    }
}