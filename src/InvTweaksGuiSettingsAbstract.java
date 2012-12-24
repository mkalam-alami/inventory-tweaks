import java.util.List;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;

import org.lwjgl.util.Point;


/**
 * The inventory and chest settings menu.
 * 
 * @author Jimeo Wan
 * 
 */
public abstract class InvTweaksGuiSettingsAbstract extends aul /* GuiScreen */ {

	protected static final Logger log = Logger.getLogger("InvTweaks");
    
    protected static String ON;
    protected static String OFF;
    protected static String DISABLE_CI;
    
    protected Minecraft mc;
    protected InvTweaksObfuscation obf;
    protected InvTweaksConfig config;
    protected aul parentScreen;

    protected static String LABEL_DONE;
    protected final static int ID_DONE = 200;

    public InvTweaksGuiSettingsAbstract(Minecraft mc, aul parentScreen,
            InvTweaksConfig config) {
    	
    	LABEL_DONE = InvTweaksLocalization.get("invtweaks.settings.exit");
    	ON = ": " + InvTweaksLocalization.get("invtweaks.settings.on");
    	OFF = ": " + InvTweaksLocalization.get("invtweaks.settings.off");
    	DISABLE_CI = ": " + InvTweaksLocalization.get("invtweaks.settings.disableci");
    	
        this.mc = mc;
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void A_() { /* initGui */

        List<Object> controlList = obf.getControlList(this);
        Point p = new Point();
        moveToButtonCoords(1, p);
        controlList.add(new atb(ID_DONE, p.getX() + 55, obf.getWindowHeight(this) / 6 + 168, LABEL_DONE)); // GuiButton

        // Save control list
        obf.setControlList(this, controlList);

    }
    
    public void a(int i, int j, float f) { /* drawScreen */
        e(); // Gui.drawDefaultBackground
        a(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.title"), obf.getWindowWidth(this) / 2, 20, 0xffffff); // Gui.drawCenteredString
        super.a(i, j, f); // drawScreen
    }

    protected void a(atb guibutton) { /* actionPerformed */
        if (obf.getId(guibutton) == ID_DONE) {
            obf.displayGuiScreen(parentScreen);
        }
    }

    protected void moveToButtonCoords(int buttonOrder, Point p) {
        p.setX(obf.getWindowWidth(this) / 2 - 155 + ((buttonOrder+1) % 2) * 160);
        p.setY(obf.getWindowHeight(this) / 6 + (buttonOrder / 2) * 24);
    }

    protected void toggleBooleanButton(atb guibutton, String property, String label) {
        Boolean enabled = !new Boolean(config.getProperty(property));
        config.setProperty(property, enabled.toString());
        obf.setDisplayString(guibutton, computeBooleanButtonLabel(property, label));
    }

    protected String computeBooleanButtonLabel(String property, String label) {
        String propertyValue = config.getProperty(property);
        if (propertyValue.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            return label + DISABLE_CI;
        } else {
            Boolean enabled = new Boolean(propertyValue);
            return label + ((enabled) ? ON : OFF);
        }
    }

}
