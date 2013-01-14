package invtweaks;

import java.awt.Desktop;
import java.io.File;
import java.util.List;
import java.util.logging.Logger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.util.Point;


/**
 * The inventory and chest advanced settings menu.
 *
 * @author Jimeo Wan
 *
 */
public class InvTweaksGuiSettingsAdvanced extends InvTweaksGuiSettingsAbstract {

    private static final Logger log = Logger.getLogger("InvTweaks");

    private final static int ID_SORT_ON_PICKUP = 1;
    private final static int ID_AUTO_EQUIP_ARMOR = 2;
    private final static int ID_ENABLE_SOUNDS = 3;
    private final static int ID_CHESTS_BUTTONS = 4;
    private final static int ID_EDITSHORTCUTS = 100;

    private static String labelChestButtons;
    private static String labelSortOnPickup;
    private static String labelEquipArmor;
    private static String labelEnableSounds;

    public InvTweaksGuiSettingsAdvanced(Minecraft mc, GuiScreen parentScreen, InvTweaksConfig config) {
        super(mc, parentScreen, config);

        labelSortOnPickup = InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup");
		labelEquipArmor = InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip");
		labelEnableSounds = InvTweaksLocalization.get("invtweaks.settings.advanced.sounds");
	    labelChestButtons = InvTweaksLocalization.get("invtweaks.settings.chestbuttons");
    }

    public void initGui() {
    	super.initGui();

        List<Object> controlList = obf.getControlList(this);
        Point p = new Point();
        int i = 0;

        // Create large buttons

        moveToButtonCoords(1, p);
        controlList.add(new GuiButton(ID_EDITSHORTCUTS, p.getX() + 55, obf.getWindowHeight(this) / 6 + 144, InvTweaksLocalization.get("invtweaks.settings.advanced.mappingsfile")));

        // Create settings buttons

        i += 2;
        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton sortOnPickupBtn = new InvTweaksGuiTooltipButton(ID_SORT_ON_PICKUP, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, labelSortOnPickup), InvTweaksLocalization.get("invtweaks.settings.advanced.sortonpickup.tooltip"));
        controlList.add(sortOnPickupBtn);

        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton enableSoundsBtn = new InvTweaksGuiTooltipButton(ID_ENABLE_SOUNDS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_SOUNDS, labelEnableSounds),
                InvTweaksLocalization.get("invtweaks.settings.advanced.sounds.tooltip"));
        controlList.add(enableSoundsBtn);

        moveToButtonCoords(i++, p);
        controlList.add(new InvTweaksGuiTooltipButton(ID_CHESTS_BUTTONS, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons), InvTweaksLocalization.get("invtweaks.settings.chestbuttons.tooltip")));

        moveToButtonCoords(i++, p);
        InvTweaksGuiTooltipButton autoEquipArmorBtn = new InvTweaksGuiTooltipButton(ID_AUTO_EQUIP_ARMOR, p.getX(), p.getY(), computeBooleanButtonLabel(
                InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, labelEquipArmor), InvTweaksLocalization.get("invtweaks.settings.advanced.autoequip.tooltip"));
        controlList.add(autoEquipArmorBtn);

        // Check if links to files are supported, if not disable the buttons
        if (!Desktop.isDesktopSupported()) {
            for (Object o : controlList) {
            	if (obf.isGuiButton(o)) {
            	    GuiButton button = obf.asGuiButton(o);
	                if (obf.getId(button) == ID_EDITSHORTCUTS) {
	                    obf.setEnabled(button, false);
	                }
            	}
            }
        }

        // Save control list
        obf.setControlList(this, controlList);

    }

	public void drawScreen(int i, int j, float f) {
        super.drawScreen(i, j, f);

        int x = obf.getWindowWidth(this) / 2;
        drawCenteredString(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt1"), x, 40, 0x999999);
        drawCenteredString(obf.getFontRenderer(), InvTweaksLocalization.get("invtweaks.settings.pvpwarning.pt2"), x, 50, 0x999999);
    }

    protected void actionPerformed(GuiButton guibutton) {

        switch (obf.getId(guibutton)) {

        // Toggle auto-refill sound
        case ID_SORT_ON_PICKUP:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SORTING_ON_PICKUP, labelSortOnPickup);
            break;

        // Toggle shortcuts
        case ID_AUTO_EQUIP_ARMOR:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_AUTO_EQUIP_ARMOR, labelEquipArmor);
            break;

        // Toggle sounds
        case ID_ENABLE_SOUNDS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_ENABLE_SOUNDS, labelEnableSounds);
            break;

        // Toggle chest buttons
        case ID_CHESTS_BUTTONS:
            toggleBooleanButton(guibutton, InvTweaksConfig.PROP_SHOW_CHEST_BUTTONS, labelChestButtons);
            break;


        // Open shortcuts mappings in external editor
        case ID_EDITSHORTCUTS:
            try {
                Desktop.getDesktop().open(new File(InvTweaksConst.CONFIG_PROPS_FILE));
            } catch (Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.settings.advanced.mappingsfile.error", e);
            }
            break;

        // Back to main settings screen
        case ID_DONE:
                obf.displayGuiScreen(new InvTweaksGuiSettings(mc, parentScreen, config));

        }

    }

}
