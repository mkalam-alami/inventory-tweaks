package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.StatCollector;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.Point;

import java.util.List;

/**
 * The inventory and chest settings menu.
 *
 * @author Jimeo Wan
 */
public abstract class InvTweaksGuiSettingsAbstract extends GuiScreen {

    protected static final Logger log = InvTweaks.log;

    protected static String ON;
    protected static String OFF;
    protected static String DISABLE_CI;

    protected Minecraft mc;
    protected InvTweaksObfuscation obf;
    protected InvTweaksConfig config;
    protected GuiScreen parentScreen;

    protected static String LABEL_DONE;
    protected final static int ID_DONE = 200;

    public InvTweaksGuiSettingsAbstract(Minecraft mc, GuiScreen parentScreen, InvTweaksConfig config) {

        LABEL_DONE = StatCollector.translateToLocal("invtweaks.settings.exit");
        ON = ": " + StatCollector.translateToLocal("invtweaks.settings.on");
        OFF = ": " + StatCollector.translateToLocal("invtweaks.settings.off");
        DISABLE_CI = ": " + StatCollector.translateToLocal("invtweaks.settings.disableci");

        this.mc = mc;
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    @Override
    public void initGui() {

        List<Object> controlList = field_146292_n;
        Point p = new Point();
        moveToButtonCoords(1, p);
        controlList.add(new GuiButton(ID_DONE, p.getX() + 55, field_146295_m / 6 + 168,
                                      LABEL_DONE)); // GuiButton

        // Save control list
        field_146292_n = controlList;

    }

    @Override
    public void drawScreen(int i, int j, float f) {
        func_146276_q_();
        drawCenteredString(obf.getFontRenderer(), StatCollector.translateToLocal("invtweaks.settings.title"),
                           field_146294_l / 2, 20, 0xffffff);
        super.drawScreen(i, j, f);
    }

    @Override
    protected void func_146284_a(GuiButton guibutton) {
        // GuiButton
        if(guibutton.field_146127_k == ID_DONE) {
            obf.displayGuiScreen(parentScreen);
        }
    }

    @Override
    protected void keyTyped(char c, int keyCode) {
        if(keyCode == Keyboard.KEY_ESCAPE) {
            obf.displayGuiScreen(parentScreen);
        }
    }

    protected void moveToButtonCoords(int buttonOrder, Point p) {
        p.setX(field_146294_l / 2 - 155 + ((buttonOrder + 1) % 2) * 160);
        p.setY(field_146295_m / 6 + (buttonOrder / 2) * 24);
    }

    protected void toggleBooleanButton(GuiButton guibutton, String property, String label) {
        Boolean enabled = !Boolean.valueOf(config.getProperty(property));
        config.setProperty(property, enabled.toString());
        guibutton.field_146126_j = computeBooleanButtonLabel(property, label);
    }

    protected String computeBooleanButtonLabel(String property, String label) {
        String propertyValue = config.getProperty(property);
        if(propertyValue.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            return label + DISABLE_CI;
        } else {
            Boolean enabled = Boolean.valueOf(propertyValue);
            return label + ((enabled) ? ON : OFF);
        }
    }

}
