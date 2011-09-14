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
public class InvTweaksGuiInventorySettings extends qr {

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
    private GuiScreen parentScreen;
    private InvTweaksConfig config;

    public InvTweaksGuiInventorySettings(Minecraft mc, GuiScreen parentScreen,
            InvTweaksConfig config) {
        this.mc = mc;
        this.parentScreen = parentScreen;
        this.config = config;
    }

    public void initGui() {

        List<GuiButton> controlList = new LinkedList<GuiButton>();
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new GuiButton(ID_EDITRULES, p.getX() + 55, height / 6 + 96, "Open the sorting rules file..."));
        controlList.add(new GuiButton(ID_EDITTREE, p.getX() + 55, height / 6 + 120, "Open the item tree file..."));
        controlList.add(new GuiButton(ID_HELP, p.getX() + 55, height / 6 + 144, "Open help in browser..."));
        controlList.add(new GuiButton(ID_DONE, p.getX() + 55, height / 6 + 168, "Done"));

        // Create settings buttons

        String middleClick = config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK);
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton middleClickBtn = new InvTweaksGuiTooltipButton(ID_MIDDLE_CLICK, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, MIDDLE_CLICK), "To sort using the middle click");
        controlList.add(middleClickBtn);
        if (middleClick.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            // Convenient Inventory compatibility: middle click not available
            middleClickBtn.enabled = false;
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
            shortcutsBtn.enabled = false;
            shortcutsBtn.setTooltip(shortcutsBtn.getTooltip() + "\n(In conflict with Convenient Inventory)");
        }
        
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, SORT_ON_PICKUP), "Moves picked up items\nto the right slots");
        controlList.add(sortOnPickupBtn);
        if (mc.isMultiplayerWorld()) {
            // Sorting on pickup unavailable in SMP
            sortOnPickupBtn.enabled = false;
            sortOnPickupBtn.displayString = SORT_ON_PICKUP + SP_ONLY;
            sortOnPickupBtn.setTooltip(sortOnPickupBtn.getTooltip() + "\n(Single player only)");
        }

        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, CHEST_BUTTONS), "Adds three buttons\non chests to sort them"));

        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
                GuiButton button = (GuiButton) o;
                if (button.id == ID_EDITRULES || button.id < ID_EDITTREE) {
                    button.enabled = false;
                }
            }
        }

        // Save control list
        this.controlList = controlList;

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

        // Shortcuts help
        case ID_SHORTCUTS_HELP:
            mc.displayGuiScreen(new InvTweaksGuiShortcutsHelp(mc, this, config));
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
            mc.displayGuiScreen(parentScreen);
        }

    }

    private void moveToButtonCoords(int buttonOrder, Point p) {
        p.setX(width / 2 - 155 + ((buttonOrder+1) % 2) * 160);
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
        } else {
            Boolean enabled = new Boolean(propertyValue);
            return label + ((enabled) ? ON : OFF);
        }
    }

}
