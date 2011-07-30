package net.invtweaks.gui;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import net.invtweaks.Const;
import net.invtweaks.config.InvTweaksConfig;
import net.minecraft.src.GuiButton;
import net.minecraft.src.GuiScreen;
import net.minecraft.src.GuiSmallButton;
import net.minecraft.src.InvTweaks;

/**
 * The inventory and chest settings menu.
 * 
 * @author Jimeo Wan
 *
 */
public class GuiInventorySettings extends GuiScreen {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private final static String SCREEN_TITLE = "Inventory and chests settings";

    // TODO Mod translation?
    private final static String MIDDLE_CLICK = "Middle click";
    private final static String CHEST_BUTTONS = "Chest buttons";
    private final static String SORT_ON_PICKUP = "Sort on pickup";
    private final static String ON = ": ON";
    private final static String OFF = ": OFF";

    private final static int ID_MIDDLE_CLICK = 1;
    private final static int ID_CHESTS_BUTTONS = 2;
    private final static int ID_SORT_ON_PICKUP = 3;
    // private final static int ID_CONVENIENT_SHORTCUTS = 3;
    // private final static int ID_AUTOREPLACE = 4;

    private final static int ID_EDITRULES = 100;
    private final static int ID_EDITTREE = 101;
    private final static int ID_HELP = 102;

    private final static int ID_DONE = 200;

    private GuiScreen parentScreen;
    private InvTweaksConfig config;

    public GuiInventorySettings(GuiScreen guiscreen, InvTweaksConfig config) {
        this.parentScreen = guiscreen;
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    public void initGui() {

        int x = width / 2 - 155;
        int y = height / 6;

        String ciCompatibility = config.getProperty(InvTweaksConfig.PROP_CONVENIENT_INVENTORY_COMPATIBILITY);
        if (ciCompatibility == null || !ciCompatibility.equals("true")) {
            controlList.add(new GuiSmallButton(ID_MIDDLE_CLICK, x, y, computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK)));
        }
        
        controlList.add(new GuiSmallButton(ID_CHESTS_BUTTONS, x + 160, y, computeBooleanButtonLabel(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS,
                CHEST_BUTTONS)));
        controlList.add(new GuiSmallButton(ID_SORT_ON_PICKUP, x, y + 24, computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP,
                SORT_ON_PICKUP)));

        // TODO Implement "Convenient Inventory"-like shortcuts
        // TODO Implement autoreplace options
        /*
         * controlList.add(new GuiSmallButton(ID_CONVENIENT_SHORTCUTS, x, y +
         * 24, "Convenient shortcuts: OFF")); controlList.add(new
         * GuiSmallButton(ID_AUTOREPLACE, x + 160, y + 24, "Autoreplace: ALL"));
         */

        controlList.add(new GuiButton(ID_EDITRULES, x + 55, height / 6 + 96, "Open the sorting rules file..."));
        controlList.add(new GuiButton(ID_EDITTREE, x + 55, height / 6 + 120, "Open the item tree file..."));
        controlList.add(new GuiButton(ID_HELP, x + 55, height / 6 + 144, "Open help in browser..."));

        controlList.add(new GuiButton(ID_DONE, x + 55, height / 6 + 168, "Done"));

        // Check if "external links" are supported
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
                GuiButton button = (GuiButton) o;
                if (button.id >= 100 && button.id < 200) {
                    button.enabled = false;
                }
            }
        }

    }

    public void drawScreen(int i, int j, float f) {
        drawDefaultBackground();
        drawCenteredString(fontRenderer, SCREEN_TITLE, width / 2, 20, 0xffffff);
        super.drawScreen(i, j, f);
    }

    protected void actionPerformed(GuiButton guibutton) {

        switch (guibutton.id) {

        // Toggle middle click shortcut
        case ID_MIDDLE_CLICK:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK);
            break;

        // Toggle chest buttons
        case ID_CHESTS_BUTTONS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, CHEST_BUTTONS);
            break;

        // Toggle autoreplace sound
        case ID_SORT_ON_PICKUP:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, SORT_ON_PICKUP);
            break;

        // Open rules configuration in external editor
        case ID_EDITRULES:
            try {
                Desktop.getDesktop().browse(new File(Const.CONFIG_RULES_FILE).toURI());
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open rules file", e);
            }
            break;

        // Open tree configuration in external editor
        case ID_EDITTREE:
            try {
                Desktop.getDesktop().browse(new File(Const.CONFIG_TREE_FILE).toURI());
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open tree file", e);
            }
            break;

        // Open help in external editor
        case ID_HELP:
            try {
                Desktop.getDesktop().browse(new URL(Const.HELP_URL).toURI());
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open help", e);
            }
            break;

        case ID_DONE:
            mc.displayGuiScreen(parentScreen);
        }

    }

    private void toggleBooleanButton(GuiButton guibutton, String property, String label) {
        Boolean enabled = !new Boolean(config.getProperty(property));
        config.setProperty(property, enabled.toString());
        guibutton.displayString = label + ((enabled) ? ON : OFF);
    }

    private String computeBooleanButtonLabel(String property, String label) {
        Boolean enabled = new Boolean(config.getProperty(property));
        return label + ((enabled) ? ON : OFF);
    }

}
