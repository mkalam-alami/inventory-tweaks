package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiIconButton extends InvTweaksGuiTooltipButton {

    protected InvTweaksConfigManager cfgManager;

    private boolean useCustomTexture;

    private static ResourceLocation resourceButtonCustom = new ResourceLocation("inventorytweaks",
                                                                                "textures/gui/button10px.png");
    private static ResourceLocation resourceButtonDefault = new ResourceLocation("textures/gui/widgets.png");

    public InvTweaksGuiIconButton(InvTweaksConfigManager cfgManager, int id, int x, int y, int w, int h,
                                  String displayString, String tooltip, boolean useCustomTexture) {
        super(id, x, y, w, h, displayString, tooltip);
        this.cfgManager = cfgManager;
        this.useCustomTexture = useCustomTexture;
    }

    @Override
    public void func_146112_a(Minecraft minecraft, int i, int j) {
        super.func_146112_a(minecraft, i, j);

        // Draw background (use the 4 corners of the texture to fit best its small size)
        int k = func_146114_a(isMouseOverButton(i, j));
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if(useCustomTexture) {
            minecraft.getTextureManager().bindTexture(resourceButtonCustom);
            drawTexturedModalRect(field_146128_h, field_146129_i, (k - 1) * 10, 0, field_146120_f, field_146121_g);
        } else {
            minecraft.getTextureManager().bindTexture(resourceButtonDefault);
            drawTexturedModalRect(field_146128_h, field_146129_i, 1, 46 + k * 20 + 1, field_146120_f / 2,
                                  field_146121_g / 2);
            drawTexturedModalRect(field_146128_h, field_146129_i + field_146121_g / 2, 1,
                                  46 + k * 20 + 20 - field_146121_g / 2 - 1, field_146120_f / 2, field_146121_g / 2);
            drawTexturedModalRect(field_146128_h + field_146120_f / 2, field_146129_i, 200 - field_146120_f / 2 - 1,
                                  46 + k * 20 + 1, field_146120_f / 2, field_146121_g / 2);
            drawTexturedModalRect(field_146128_h + field_146120_f / 2, field_146129_i + field_146121_g / 2,
                                  200 - field_146120_f / 2 - 1, 46 + k * 20 + 19 - field_146121_g / 2,
                                  field_146120_f / 2, field_146121_g / 2);
        }

    }

}