package gregtech.common.blocks;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.GT_Mod;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.interfaces.ITexture;
import gregtech.api.render.TextureFactory;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

import java.util.Arrays;

import static gregtech.api.enums.Textures.BlockIcons.BASALT_STONE;
import static gregtech.api.enums.Textures.BlockIcons.GRANITE_BLACK_STONE;
import static gregtech.api.enums.Textures.BlockIcons.GRANITE_RED_STONE;
import static gregtech.api.enums.Textures.BlockIcons.MARBLE_STONE;

public class GT_Block_Ores extends GT_Block_Ores_Abstract {
    private static final String UNLOCALIZED_NAME = "gt.blockores";

    public GT_Block_Ores() {
        super(UNLOCALIZED_NAME, 7, false, Material.rock);
    }

    @Override
    public String getUnlocalizedName() {
        return UNLOCALIZED_NAME;
    }

    @Override
    public OrePrefixes[] getProcessingPrefix() { //Must have 8 entries; an entry can be null to disable automatic recipes.
        return new OrePrefixes[]{OrePrefixes.ore, OrePrefixes.oreNetherrack, OrePrefixes.oreEndstone, OrePrefixes.oreBlackgranite, OrePrefixes.oreRedgranite, OrePrefixes.oreMarble, OrePrefixes.oreBasalt, null};
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        int index = ((meta / 1000) % 16);
        switch (index) {
            case 1:
                return Blocks.netherrack.getIcon(side, 0);
            case 2:
                return Blocks.end_stone.getIcon(side, 0);
            case 3:
                return GRANITE_BLACK_STONE.getIcon();
            case 4:
                return GRANITE_RED_STONE.getIcon();
            case 5:
                return MARBLE_STONE.getIcon();
            case 6:
                return BASALT_STONE.getIcon();
            default:
                return Blocks.stone.getIcon(side, 0);
        }
    }

    @Override
    public int getBaseBlockHarvestLevel(int aMeta) {
        switch (aMeta) {
            case 3:
            case 4:
                return 3;
            case 0:
            case 1:
            case 2:
            case 5:
            case 6:
            default:
                return 0;
        }
    }

    @Override
    public Block getDroppedBlock() {
        return GregTech_API.sBlockOres1;
    }

    @Override
    public Materials[] getDroppedDusts() { //Must have 8 entries; can be null.
        return new Materials[]{Materials.Stone, Materials.Netherrack, Materials.Endstone, Materials.GraniteBlack, Materials.GraniteRed, Materials.Marble, Materials.Basalt, Materials.Stone};
    }

    @Override
    public boolean[] getEnabledMetas() {
        return new boolean[]{true, true, true, GT_Mod.gregtechproxy.enableBlackGraniteOres, GT_Mod.gregtechproxy.enableRedGraniteOres, GT_Mod.gregtechproxy.enableMarbleOres, GT_Mod.gregtechproxy.enableBasaltOres, true};
    }

    @Override
    public ITexture[] getTextureSet() {
        final ITexture[] rTextures = new ITexture[16]; //Must have 16 entries.
        Arrays.fill(rTextures, TextureFactory.of(Blocks.stone));
        rTextures[1] = TextureFactory.of(Blocks.netherrack);
        rTextures[2] = TextureFactory.of(Blocks.end_stone);
        rTextures[3] = TextureFactory.builder().addIcon(GRANITE_BLACK_STONE).stdOrient().build();
        rTextures[4] = TextureFactory.builder().addIcon(GRANITE_RED_STONE).stdOrient().build();
        rTextures[5] = TextureFactory.builder().addIcon(MARBLE_STONE).stdOrient().build();
        rTextures[6] = TextureFactory.builder().addIcon(BASALT_STONE).stdOrient().build();
        return rTextures;
    }
}
