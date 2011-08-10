package net.invtweaks.gui;

import java.awt.Desktop;
import java.io.File;
import java.net.URL;
import java.util.logging.Logger;

import org.lwjgl.util.Point;

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

    private final static String MIDDLE_CLICK = "Middle click";
    private final static String CHEST_BUTTONS = "Chest buttons";
    private final static String SORT_ON_PICKUP = "Sort on pickup";
    private final static String SHORTCUTS = "Shortcuts";
    private final static String ON = ": ON";
    private final static String OFF = ": OFF";
    private final static String DISABLE_CI = ": Disable CI's first";

    private final static int ID_MIDDLE_CLICK = 1;
    private final static int ID_CHESTS_BUTTONS = 2;
    private final static int ID_SORT_ON_PICKUP = 3;
    private final static int ID_SHORTCUTS = 4;

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

        Point p = new Point();
        int i = 0;

        // Create large buttons
        
        moveToButtonCoords(0, p);
        controlList.add(new GuiButton(ID_EDITRULES, p.getX() + 55, height / 6 + 96, "Open the sorting rules file..."));
        controlList.add(new GuiButton(ID_EDITTREE, p.getX() + 55, height / 6 + 120, "Open the item tree file..."));
        controlList.add(new GuiButton(ID_HELP, p.getX() + 55, height / 6 + 144, "Open help in browser..."));

        controlList.add(new GuiButton(ID_DONE, p.getX() + 55, height / 6 + 168, "Done"));

        // Create settings buttons
        
        String middleClick = config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK);
        moveToButtonCoords(i++, p);
        GuiButton middleClickBtn = new GuiSmallButton(ID_MIDDLE_CLICK, p.getX(), p.getY(),
                computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK));
        controlList.add(middleClickBtn);
        if (middleClick.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            // Convenient Inventory compatibility: middle click not available
            middleClickBtn.enabled = false;
        }

        moveToButtonCoords(i++, p);
        controlList.add(new GuiSmallButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(),
                computeBooleanButtonLabel(InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS,
                CHEST_BUTTONS)));

        moveToButtonCoords(i++, p);
        controlList.add(new GuiSmallButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), 
                computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP,
                SORT_ON_PICKUP)));
        
        moveToButtonCoords(i++, p);
        controlList.add(new GuiSmallButton(ID_SHORTCUTS, p.getX(), p.getY(), 
                computeBooleanButtonLabel(InvTweaksConfig.PROP_ENABLE_SHORTCUTS,
                SHORTCUTS)));
        
        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
                GuiButton button = (GuiButton) o;
                if (button.id == ID_EDITRULES || button.id < ID_EDITTREE) {
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

        // Open rules configuration in external editor
        case ID_EDITRULES:
            try {
                Desktop.getDesktop().open(new File(Const.CONFIG_RULES_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("Failed to open rules file", e);
            }
            break;

        // Open tree configuration in external editor
        case ID_EDITTREE:
            try {
                Desktop.getDesktop().open(new File(Const.CONFIG_TREE_FILE));
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

    private void moveToButtonCoords(int buttonOrder, Point p) {
        p.setX(width / 2 - 155 + (buttonOrder % 2) * 160);
        p.setY(height / 6 + (buttonOrder / 2) * 24);
    }

    private void toggleBooleanButton(GuiButton guibutton, String property, String label) {
        Boolean enabled = !new Boolean(config.getProperty(property));
        config.setProperty(property, enabled.toString());
        guibutton.displayString = computeBooleanButtonLabel(property, label);
    }

    private String computeBooleanButtonLabel(String property, String label) {
        String propertyValue = config.getProperty(property);
        if (propertyValue.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            return label + DISABLE_CI;
        }
        else {
            Boolean enabled = new Boolean(propertyValue);
            return label + ((enabled) ? ON : OFF);
        }
    }

}
