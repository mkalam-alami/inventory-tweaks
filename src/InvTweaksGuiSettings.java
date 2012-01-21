import java.awt.Desktop;
import java.io.File;
import java.net.URL;
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
public class InvTweaksGuiSettings extends InvTweaksGuiSettingsAbstract {

    @SuppressWarnings("unused")
    private static final Logger log = Logger.getLogger("InvTweaks");

    private final static int ID_MIDDLE_CLICK = 1;
    private final static int ID_CHESTS_BUTTONS = 2;
    private final static int ID_SHORTCUTS = 3;
    private final static int ID_SHORTCUTS_HELP = 4;
    private final static int ID_AUTO_REFILL = 5;
    private final static int ID_MORE_OPTIONS = 6;
    private final static int ID_EDITRULES = 100;
    private final static int ID_EDITTREE = 101;
    private final static int ID_HELP = 102;
    
    private static String labelMiddleClick;
    private static String labelChestButtons;
    private static String labelShortcuts;
    private static String labelAutoRefill;
    private static String labelMoreOptions;
    
    public InvTweaksGuiSettings(Minecraft mc, ug parentScreen, InvTweaksConfig config) {
        super(mc, parentScreen, config);
        
        labelMiddleClick = InvTweaksLocalization.get("invtweaks.settings.middleclick");
        labelChestButtons = InvTweaksLocalization.get("invtweaks.settings.chestbuttons");
        labelShortcuts = InvTweaksLocalization.get("invtweaks.settings.shortcuts");
        labelAutoRefill = InvTweaksLocalization.get("invtweaks.settings.autorefill");
        labelMoreOptions = InvTweaksLocalization.get("invtweaks.settings.moreoptions");
        
    }

    public void c() { /* initGui */
    	super.c();

        List<Object> controlList = obf.getControlList(this);
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new zr(ID_EDITRULES, p.getX() + 55, obf.getHeight(this) / 6 + 96, "Open the sorting rules file..."));
        controlList.add(new zr(ID_EDITTREE, p.getX() + 55, obf.getHeight(this) / 6 + 120, "Open the item tree file..."));
        controlList.add(new zr(ID_HELP, p.getX() + 55, obf.getHeight(this) / 6 + 144, "Open help in browser..."));

        // Create settings buttons

        String middleClick = config.getProperty(InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK);
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton middleClickBtn = new InvTweaksGuiTooltipButton(ID_MIDDLE_CLICK, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, labelMiddleClick), "Allows to sort using\nthe middle click");
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
                InvTweaksConfig.PROP_ENABLE_SHORTCUTS, labelShortcuts), "Enables various shortcuts\nto move items around");
        controlList.add(shortcutsBtn);
        if (shortcuts.equals(InvTweaksConfig.VALUE_CI_COMPATIBILITY)) {
            // Convenient Inventory compatibility: shortcuts not available
            obf.setEnabled(shortcutsBtn, false);
            shortcutsBtn.setTooltip(shortcutsBtn.getTooltip() + "\n(In conflict with Convenient Inventory)");
        }

        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton autoRefillBtn = new InvTweaksGuiTooltipButton(ID_AUTO_REFILL, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_AUTO_REFILL, labelAutoRefill), "Replaces broken tools & emptied stacks\nwith others from your inventory");
        controlList.add(autoRefillBtn);
        
        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons), "Adds three buttons\non chests to sort them"));

        moveToButtonCoords(i++, p);
        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_MORE_OPTIONS, p.getX(), p.getY(), labelMoreOptions, "Even more cool stuff"));
    
        
        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
            	zr button = (zr) o;
                if (obf.getId(button) >= ID_EDITRULES && obf.getId(button) <= ID_HELP) {
                    obf.setEnabled(button, false);
                }
            }
        }

        // Save control list
        obf.setControlList(this, controlList);

    }

    protected void a(zr guibutton) { /* actionPerformed */
    	super.a(guibutton);
    
        switch (obf.getId(guibutton)) {

        // Toggle middle click shortcut
        case ID_MIDDLE_CLICK:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_MIDDLE_CLICK, labelMiddleClick);
            break;

        // Toggle chest buttons
        case ID_CHESTS_BUTTONS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons);
            break;

        // Toggle auto-refill
        case ID_AUTO_REFILL:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_AUTO_REFILL, labelAutoRefill);
            break;
                
        // Toggle shortcuts
        case ID_SHORTCUTS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SHORTCUTS, labelShortcuts);
            break;

        // Shortcuts help
        case ID_SHORTCUTS_HELP:
            obf.displayGuiScreen(new InvTweaksGuiShortcutsHelp(mc, this, config));
            break;
            
        // Toggle chest buttons&
        case ID_MORE_OPTIONS:
            obf.displayGuiScreen(new InvTweaksGuiSettingsAdvanced(mc, parentScreen, config));
            break;

        // Open rules configuration in external editor
        case ID_EDITRULES:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_RULES_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.rulesfile.error", e);
            }
            break;

        // Open tree configuration in external editor
        case ID_EDITTREE:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_TREE_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.treefile.error", e);
            }
            break;

        // Open help in browser
        case ID_HELP:
            try {
                Desktop.getDesktop().browse(new URL(InvTweaksConst.HELP_URL).toURI());
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.help.error", e);
            }
            break;
            
        }

    }

}
