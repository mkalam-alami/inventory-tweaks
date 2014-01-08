package invtweaks;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 *
 * @author Jimeo Wan
 */
public class InvTweaksGuiTooltipButton extends GuiButton {

    public final static int DEFAULT_BUTTON_WIDTH = 200;
    public final static int LINE_HEIGHT = 11;

    private int hoverTime = 0;
    private long prevSystemTime = 0;

    private String tooltip = null;
    private String[] tooltipLines = null;
    private int tooltipWidth = -1;
    private boolean drawBackground = true;

    public InvTweaksGuiTooltipButton(int id, int x, int y, String displayString) {
        this(id, x, y, 150, 20, displayString, null);
    }

    /**
     * Default size is 150, the common "GuiSmallButton" button size.
     */
    public InvTweaksGuiTooltipButton(int id, int x, int y, String displayString, String tooltip) {
        this(id, x, y, 150, 20, displayString, tooltip);
    }

    public InvTweaksGuiTooltipButton(int id, int x, int y, int w, int h, String displayString) {
        this(id, x, y, w, h, displayString, null);
    }

    public InvTweaksGuiTooltipButton(int id, int x, int y, int w, int h, String displayString, String tooltip) {
        super(id, x, y, w, h, displayString);
        if(tooltip != null) {
            setTooltip(tooltip);
        }
    }

    public InvTweaksGuiTooltipButton(int id, int x, int y, int w, int h, String displayString, String tooltip,
                                     boolean drawBackground) {
        super(id, x, y, w, h, displayString);
        if(tooltip != null) {
            setTooltip(tooltip);
        }
        this.drawBackground = drawBackground;
    }

    @Override
    public void func_146112_a(Minecraft minecraft, int i, int j) {
        if(this.drawBackground) {
            super.func_146112_a(minecraft, i, j);
        } else {
            this.drawString(minecraft.fontRenderer, this.field_146126_j, this.field_146128_h,
                            this.field_146129_i + (this.field_146121_g - 8) / 2, 0x999999);
        }

        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);

        if(tooltipLines != null) {
            // Compute hover time
            if(isMouseOverButton(i, j)) {
                long systemTime = System.currentTimeMillis();
                if(prevSystemTime != 0) {
                    hoverTime += systemTime - prevSystemTime;
                }
                prevSystemTime = systemTime;
            } else {
                hoverTime = 0;
                prevSystemTime = 0;
            }

            // Draw tooltip if hover time is long enough
            if(hoverTime > InvTweaksConst.TOOLTIP_DELAY && tooltipLines != null) {

                FontRenderer fontRenderer = obf.getFontRenderer();

                // Compute tooltip params
                int x = i + 12, y = j - LINE_HEIGHT * tooltipLines.length;
                if(tooltipWidth == -1) {
                    for(String line : tooltipLines) {
                        tooltipWidth = Math.max(fontRenderer.getStringWidth(line), tooltipWidth);
                    }
                }
                if(x + tooltipWidth > obf.getCurrentScreen().field_146294_l) {
                    x = obf.getCurrentScreen().field_146294_l - tooltipWidth;
                }

                // Draw background
                drawGradientRect(x - 3, y - 3, x + tooltipWidth + 3, y + LINE_HEIGHT * tooltipLines.length, 0xc0000000,
                                 0xc0000000);

                // Draw lines
                int lineCount = 0;
                for(String line : tooltipLines) {
                    int j1 = y + (lineCount++) * LINE_HEIGHT;
                    int k = -1;
                    fontRenderer.drawStringWithShadow(line, x, j1, k);
                }
            }
        }

    }

    protected boolean isMouseOverButton(int i, int j) {
        return i >= field_146128_h && j >= field_146129_i && i < (field_146128_h + field_146120_f) && j < (field_146129_i + field_146121_g);
    }

    protected int getTextColor(int i, int j) {

        int textColor = 0xffe0e0e0;
        if(!field_146124_l) {
            textColor = 0xffa0a0a0;
        } else if(isMouseOverButton(i, j)) {
            textColor = 0xffffffa0;
        }
        return textColor;

    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
        this.tooltipLines = tooltip.split("\n");
    }

    public String getTooltip() {
        return tooltip;
    }

}
