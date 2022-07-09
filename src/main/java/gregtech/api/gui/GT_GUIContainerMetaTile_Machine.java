package gregtech.api.gui;

import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Dyes;
import gregtech.api.gui.widgets.GT_GuiCoverTabLine;
import gregtech.api.gui.widgets.GT_GuiIcon;
import gregtech.api.gui.widgets.GT_GuiTooltip;
import gregtech.api.gui.widgets.GT_GuiTooltipManager;
import gregtech.api.gui.widgets.GT_GuiTabLine.DisplayStyle;
import gregtech.api.gui.widgets.GT_GuiTabLine.GT_GuiTabIconSet;
import gregtech.api.gui.widgets.GT_GuiTabLine.GT_ITabRenderer;
import gregtech.api.gui.widgets.GT_GuiTooltipManager.GT_IToolTipRenderer;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.entity.RenderItem;
import gregtech.api.util.GT_TooltipDataCache;
import net.minecraft.entity.player.InventoryPlayer;

import java.util.List;

import org.lwjgl.opengl.GL11;

/**
 * NEVER INCLUDE THIS FILE IN YOUR MOD!!!
 * <p/>
 * The GUI-Container I use for all my MetaTileEntities
 */
public class GT_GUIContainerMetaTile_Machine extends GT_GUIContainer implements GT_IToolTipRenderer, GT_ITabRenderer {

    public final GT_ContainerMetaTile_Machine mContainer;

    protected GT_GuiTooltipManager mTooltipManager = new GT_GuiTooltipManager();
    protected GT_TooltipDataCache mTooltipCache = new GT_TooltipDataCache();

    // Cover Tabs support. Subclasses can override display position, style and visuals by overriding setupCoverTabs
    public GT_GuiCoverTabLine coverTabs;
    private static final int
        COVER_TAB_LEFT = -16,
        COVER_TAB_TOP = 1,
        COVER_TAB_HEIGHT = 20,
        COVER_TAB_WIDTH = 18,
        COVER_TAB_SPACING = 2;
    private static final DisplayStyle
        COVER_TAB_X_DIR = DisplayStyle.NONE,
        COVER_TAB_Y_DIR = DisplayStyle.NORMAL;
    private static final GT_GuiTabIconSet TAB_ICONSET =  new GT_GuiTabIconSet(
        GT_GuiIcon.TAB_NORMAL,
        GT_GuiIcon.TAB_HIGHLIGHT,
        GT_GuiIcon.TAB_DISABLED);

    public GT_GUIContainerMetaTile_Machine(GT_ContainerMetaTile_Machine aContainer, String aGUIbackground) {
        super(aContainer, aGUIbackground);
        mContainer = aContainer;

        DisplayStyle preferredDisplayStyle = GT_Mod.gregtechproxy.mCoverTabsVisible
        ? (GT_Mod.gregtechproxy.mCoverTabsFlipped ? DisplayStyle.INVERSE : DisplayStyle.NORMAL)
        : DisplayStyle.NONE;
        setupCoverTabs(preferredDisplayStyle);

        // Only setup tooltips if they're currently enabled.
        if (GT_Mod.gregtechproxy.mTooltipVerbosity > 0 || GT_Mod.gregtechproxy.mTooltipShiftVerbosity > 0) {
            setupTooltips();
        }
    }

    public GT_GUIContainerMetaTile_Machine(InventoryPlayer aInventoryPlayer, IGregTechTileEntity aTileEntity,
            String aGUIbackground) {
        this(new GT_ContainerMetaTile_Machine(aInventoryPlayer, aTileEntity), aGUIbackground);
    }

    /**
     * Initialize the coverTabs object according to client preferences
     */
    protected void setupCoverTabs(DisplayStyle preferredDisplayStyle) {
        coverTabs = new GT_GuiCoverTabLine(
            this,
            COVER_TAB_LEFT,
            COVER_TAB_TOP,
            COVER_TAB_HEIGHT,
            COVER_TAB_WIDTH,
            COVER_TAB_SPACING,
            COVER_TAB_X_DIR,
            COVER_TAB_Y_DIR,
            preferredDisplayStyle,
            getTabBackground(),
            getMachine().getBaseMetaTileEntity(),
            getColorization());
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float parTicks) {
        super.drawScreen(mouseX, mouseY, parTicks);
        if (mc.thePlayer.inventory.getItemStack() == null) {
            GL11.glPushMatrix();
            GL11.glTranslatef(guiLeft, guiTop, 0.0F);
            mTooltipManager.onTick(this, mouseX, mouseY);
            GL11.glPopMatrix();
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float parTicks, int mouseX, int mouseY) {
        // Drawing tabs
        coverTabs.drawTabs(parTicks, mouseX, mouseY);

        // Applying machine coloration, which subclasses rely on
        Dyes color = getColorization();
        GL11.glColor3ub((byte) color.mRGBa[0], (byte) color.mRGBa[1], (byte) color.mRGBa[2]);

        // Binding machine's own texture, which subclasses rely on being set
        super.drawGuiContainerBackgroundLayer(parTicks, mouseX, mouseY);
    }

    /**
     * @return The color used to render this machine's GUI
     */
    private Dyes getColorization() {
        if (GregTech_API.sMachineMetalGUI) {
            return Dyes.MACHINE_METAL;
        } else if (GregTech_API.sColoredGUI && mContainer != null && mContainer.mTileEntity != null) {
            byte colorByte = mContainer.mTileEntity.getColorization();
            Dyes color;
            if (colorByte != -1)
                color = Dyes.get(colorByte);
            else
                color = Dyes.MACHINE_METAL;
            return color;
        } else {
            return Dyes.dyeWhite;
        }
    }

    /**
     * @return This machine's MetaTileEntity
     */
    private MetaTileEntity  getMachine() {
        return (MetaTileEntity ) mContainer.mTileEntity.getMetaTileEntity();
    }

    // Tabs support

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton)
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        // Check for clicked tabs
        coverTabs.onMouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void initGui() {
        super.initGui();
        // Perform layout of tabs
        coverTabs.onInit();
    }

    /**
     * @return the background textures used by this machine GUI's tabs
     */
    protected GT_GuiTabIconSet getTabBackground() {
        return TAB_ICONSET;
    }

    // Tooltips support

    /**
     * Load data for and create appropriate tooltips for this machine.
     * Only called when one of regular or shift tooltips are enabled.
     */
    protected void setupTooltips() {    }

    // GT_IToolTipRenderer and GT_ITabRenderer implementations
    @Override
    public void drawHoveringText(List text, int mouseX, int mouseY, FontRenderer font) {
        super.drawHoveringText(text, mouseX, mouseY, font);
    }
    @Override
    public int getGuiTop() {
        return guiTop;
    }
    @Override
    public int getGuiLeft() {
        return guiLeft;
    }
    @Override
    public int getXSize() {
        return xSize;
    }
    @Override
    public FontRenderer getFontRenderer() {
        return fontRendererObj;
    }
    @Override
    public RenderItem getItemRenderer() {
        return itemRender;
    }
    @Override
    public void addToolTip(GT_GuiTooltip toolTip) {
        mTooltipManager.addToolTip(toolTip);
    }
    @Override
    public boolean removeToolTip(GT_GuiTooltip toolTip) {
        return mTooltipManager.removeToolTip(toolTip);
    }
}
