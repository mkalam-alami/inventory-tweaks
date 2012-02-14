import invtweaks.InvTweaksConst;
import net.minecraft.client.Minecraft;

/**
 * Icon-size button, which get drawns in a specific way to fit its small size.
 * @author Jimeo Wan
 *
 */
public class InvTweaksGuiTooltipButton extends InvTweaksObfuscationGuiButton {
    
    public final static int DEFAULT_BUTTON_WIDTH = 200;
    public final static int LINE_HEIGHT = 11;
    
    private int hoverTime = 0;
    private long prevSystemTime = 0;
    
    private String tooltip = null;
    private String[] tooltipLines = null;
    private int tooltipWidth = -1;

    public InvTweaksGuiTooltipButton(int id,
            int x, int y, String displayString) {
        this(id, x, y, 150, 20, displayString, null);
    }
    
    /**
     * Default size is 150, the common "GuiSmallButton" button size.
     */
    public InvTweaksGuiTooltipButton(int id,
            int x, int y, String displayString, String tooltip) {
        this(id, x, y, 150, 20, displayString, tooltip);
    }

    public InvTweaksGuiTooltipButton(int id, int x, int y, int w, int h,
            String displayString) {
        this(id, x, y, w, h, displayString, null);
    }
    
    public InvTweaksGuiTooltipButton(int id, int x, int y, int w, int h,
            String displayString, String tooltip) {
        super(id, x, y, w, h, displayString);
        if (tooltip != null) {
            setTooltip(tooltip);
        }
    }

    public void a(Minecraft minecraft, int i, int j) { /* drawButton */
        super.a(minecraft, i, j); 
        
        InvTweaksObfuscation obf = new InvTweaksObfuscation(minecraft);
        
        if (!isEnabled2()) {
            return;
        }
        
        if (tooltipLines != null) {
            // Compute hover time
            if (isMouseOverButton(i, j)) {
                long systemTime = System.currentTimeMillis();
                if (prevSystemTime != 0) {
                    hoverTime += systemTime - prevSystemTime;
                }
                prevSystemTime = systemTime;
            }
            else {
                hoverTime = 0;
                prevSystemTime = 0;
            }
            
            // Draw tooltip if hover time is long enough
            if (hoverTime > InvTweaksConst.TOOLTIP_DELAY && tooltipLines != null) {
                
                mq fontRenderer = obf.getFontRenderer();

                // Compute tooltip params
                int x = i + 12, y = j - LINE_HEIGHT*tooltipLines.length;
                if (tooltipWidth == -1) {
                    for (String line : tooltipLines) {
                        tooltipWidth = Math.max(
                                obf.getStringWidth(fontRenderer, line),
                                tooltipWidth);
                    }
                }
                if (x + tooltipWidth > obf.getWidth(obf.getCurrentScreen())) {
                    x = obf.getWidth(obf.getCurrentScreen()) - tooltipWidth;
                }
                
                // Draw background
                drawGradientRect(x - 3, y - 3, 
                        x + tooltipWidth + 3, y + LINE_HEIGHT*tooltipLines.length, 
                        0xc0000000, 0xc0000000);
                
                // Draw lines
                int lineCount = 0;
                for (String line : tooltipLines) {
                    obf.drawStringWithShadow(fontRenderer,
                            line, x, y + (lineCount++)*LINE_HEIGHT, -1);
                }
            }
        }

    }
    
    protected boolean isMouseOverButton(int i, int j) {
        return i >= getXPosition() && j >= getYPosition() 
            && i < getXPosition() + getWidth() 
            && j < getYPosition() + getHeight();
    }
    
    protected int getTextColor(int i, int j) {
        
        int textColor = 0xffe0e0e0;
        if (!isEnabled()) {
            textColor = 0xffa0a0a0;
        } else if (isMouseOverButton(i, j)) {
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