package invtweaks;

import invtweaks.api.container.ContainerSection;
import net.minecraft.client.Minecraft;

/**
 * Chest sorting button
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiSortingButton extends InvTweaksGuiIconButton {

    private final ContainerSection section = ContainerSection.CHEST;

    private int algorithm;
    private int rowSize;

    public InvTweaksGuiSortingButton(InvTweaksConfigManager cfgManager, int id, int x, int y, int w, int h,
                                     String displayString, String tooltip, int algorithm, int rowSize,
                                     boolean useCustomTexture) {
        super(cfgManager, id, x, y, w, h, displayString, tooltip, useCustomTexture);
        this.algorithm = algorithm;
        this.rowSize = rowSize;
    }

    @Override
    public void func_146112_a(Minecraft minecraft, int i, int j) {
        super.func_146112_a(minecraft, i, j);

        // Display symbol
        int textColor = getTextColor(i, j);
        if(field_146126_j.equals("h")) {
            drawRect(field_146128_h + 3, field_146129_i + 3, field_146128_h + field_146120_f - 3, field_146129_i + 4,
                     textColor);
            drawRect(field_146128_h + 3, field_146129_i + 6, field_146128_h + field_146120_f - 3, field_146129_i + 7,
                     textColor);
        } else if(field_146126_j.equals("v")) {
            drawRect(field_146128_h + 3, field_146129_i + 3, field_146128_h + 4, field_146129_i + field_146121_g - 3,
                     textColor);
            drawRect(field_146128_h + 6, field_146129_i + 3, field_146128_h + 7, field_146129_i + field_146121_g - 3,
                     textColor);
        } else {
            drawRect(field_146128_h + 3, field_146129_i + 3, field_146128_h + field_146120_f - 3, field_146129_i + 4,
                     textColor);
            drawRect(field_146128_h + 5, field_146129_i + 4, field_146128_h + 6, field_146129_i + 5, textColor);
            drawRect(field_146128_h + 4, field_146129_i + 5, field_146128_h + 5, field_146129_i + 6, textColor);
            drawRect(field_146128_h + 3, field_146129_i + 6, field_146128_h + field_146120_f - 3, field_146129_i + 7,
                     textColor);
        }
    }

    /**
     * Sort container
     */
    @Override
    public boolean func_146116_c(Minecraft minecraft, int i, int j) {
        if(super.func_146116_c(minecraft, i, j)) {
            try {
                new InvTweaksHandlerSorting(minecraft, cfgManager.getConfig(), section, algorithm, rowSize).sort();
            } catch(Exception e) {
                InvTweaks.logInGameErrorStatic("invtweaks.sort.chest.error", e);
                e.printStackTrace();
            }
            return true;
        } else {
            return false;
        }

    }

}
