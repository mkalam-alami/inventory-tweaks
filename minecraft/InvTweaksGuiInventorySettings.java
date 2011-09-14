import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.LinkedList;
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
public class InvTweaksGuiInventorySettings extends qr /* GuiScreen */ {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private final static String SCREEN_TITLE = "Inventory and chests settings";

    private final static String MIDDLE_CLICK = "Middle click";
    private final static String CHEST_BUTTONS = "Chest buttons";
    private final static String SORT_ON_PICKUP = "Sort on pickup";
    private final static String SHORTCUTS = "Shortcuts";
    private final static String ON = ": ON";
    private final static String OFF = ": OFF";
    private final static String DISABLE_CI = ": Disable CI";
    private final static String SP_ONLY = ": Only in SP";

    private final static int ID_MIDDLE_CLICK = 1;
    private final static int ID_CHESTS_BUTTONS = 2;
    private final static int ID_SORT_ON_PICKUP = 3;
    private final static int ID_SHORTCUTS = 4;
    private final static int ID_SHORTCUTS_HELP = 5;

    private final static int ID_EDITRULES = 100;
    private final static int ID_EDITTREE = 101;
    private final static int ID_HELP = 102;
    private final static int ID_DONE = 200;

    private Minecraft mc;
    private InvTweaksObfuscation obf;
    private qr parentScreen;
    private InvTweaksConfig config;

    public InvTweaksGuiInventorySettings(Minecraft mc, qr parentScreen,
            InvTweaksConfig config) {
        this.mc = mc;
        this.obf = new InvTweaksObfuscation(mc);
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void initGui() {

        List<vj> controlList = new LinkedList<vj>();
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new vj(ID_EDITRULES, p.getX() + 55, obf.getHeight(this) / 6 + 96, "Open the sorting rules file..."));
        controlList.add(new vj(ID_EDITTREE, p.getX() + 55, obf.getHeight(this) / 6 + 120, "Open the item tree file..."));
        controlList.add(new vj(ID_HELP, p.getX() + 55, obf.getHeight(this) / 6 + 144, "Open help in browser..."));
        controlList.add(new vj(ID_DONE, p.getX() + 55, obf.getHeight(this) / 6 + 168, "Done"));

        // Create settings buttons

        String middleClick = config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK);
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton middleClickBtn = new InvTweaksGuiTooltipButton(ID_MIDDLE_CLICK, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK), "To sort using the middle click");
        controlList.add(middleClickBtn);
        if (middleClick.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            // Convenient Inventory compatibility: middle click not available
            obf.setEnabled(middleClickBtn, false);
            middleClickBtn.setTooltip(middleClickBtn.getTooltip() + "\n(In conflict with Convenient Inventory)");
        }

        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_SHORTCUTS_HELP, 
                p.getX() + 130, p.getY(), 20, 20, "?", "Shortcuts help"));
        String shortcuts = config.getProperty(InvTweaksConfig.PROP_ENABLE_SHORTCUTS);
        InvTweaksGuiTooltipButton shortcutsBtn = new InvTweaksGuiTooltipButton(ID_SHORTCUTS, p.getX(), p.getY(), 130, 20, computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SHORTCUTS, SHORTCUTS), "Enables various shortcuts\nto move items around");
        controlList.add(shortcutsBtn);
        if (shortcuts.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            // Convenient Inventory compatibility: shortcuts not available
            obf.setEnabled(shortcutsBtn, false);
            shortcutsBtn.setTooltip(shortcutsBtn.getTooltip() + "\n(In conflict with Convenient Inventory)");
        }
        
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, SORT_ON_PICKUP), "Moves picked up items\nto the right slots");
        controlList.add(sortOnPickupBtn);
        if (obf.isMultiplayerWorld()) {
            // Sorting on pickup unavailable in SMP
            obf.setEnabled(sortOnPickupBtn, false);
            obf.setDisplayString(sortOnPickupBtn, SORT_ON_PICKUP + SP_ONLY);
            sortOnPickupBtn.setTooltip(sortOnPickupBtn.getTooltip() + "\n(Single player only)");
        }

        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, CHEST_BUTTONS), "Adds three buttons\non chests to sort them"));

        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
                vj button = (vj) o;
                if (obf.getId(button) == ID_EDITRULES || obf.getId(button) < ID_EDITTREE) {
                    obf.setEnabled(button, false);
                }
            }
        }

        // Save control list
        obf.setControlList(this, controlList);

    }
    
    public void drawScreen(int i, int j, float f) {
        k(); // Gui.drawDefaultBackground
        a(obf.getFontRenderer(), SCREEN_TITLE, obf.getWidth(this) / 2, 20, 0xffffff); // Gui.drawCenteredString
        super.a(i, j, f); // drawScreen
    }

    protected void actionPerformed(vj guibutton) {

        switch (obf.getId(guibutton)) {

        // Toggle middle click shortcut
        case ID_MIDDLE_CLICK:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK);
            break;

        // Toggle chest buttons&
        case ID_CHESTS_BUTTONS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, CHEST_BUTTONS);
            break;

        // Toggle auto-refill sound
        case ID_SORT_ON_PICKUP:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, SORT_ON_PICKUP);
            break;

        // Toggle shortcuts
        case ID_SHORTCUTS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SHORTCUTS, SHORTCUTS);
            break;

        // Shortcuts help
        case ID_SHORTCUTS_HELP:
            obf.displayGuiScreen(new InvTweaksGuiShortcutsHelp(mc, this, config));
            break;

        // Open rules configuration in external editor
        case ID_EDITRULES:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_RULES_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open rules file", e);
            }
            break;

        // Open tree configuration in external editor
        case ID_EDITTREE:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_TREE_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open tree file", e);
            }
            break;

        // Open help in external editor
        case ID_HELP:
            try {
                Desktop.getDesktop().browse(new URL(InvTweaksConst.HELP_URL).toURI());
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open help", e);
            }
            break;

        case ID_DONE:
            obf.displayGuiScreen(parentScreen);
        }

    }

    private void moveToButtonCoords(int buttonOrder, Point p) {
        p.setX(obf.getWidth(this) / 2 - 155 + ((buttonOrder+1) % 2) * 160);
        p.setY(obf.getHeight(this) / 6 + (buttonOrder / 2) * 24);
    }

    private void toggleBooleanButton(vj guibutton, String property, String label) {
        Boolean enabled = !new Boolean(config.getProperty(property));
        config.setProperty(property, enabled.toString());
        obf.setDisplayString(guibutton, computeBooleanButtonLabel(property, label));
    }

    private String computeBooleanButtonLabel(String property, String label) {
        String propertyValue = config.getProperty(property);
        if (propertyValue.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            return label + DISABLE_CI;
        } else {
            Boolean enabled = new Boolean(propertyValue);
            return label + ((enabled) ? ON : OFF);
        }
    }

}
