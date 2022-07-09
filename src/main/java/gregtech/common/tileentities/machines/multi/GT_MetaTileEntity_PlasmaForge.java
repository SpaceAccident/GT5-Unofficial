package gregtech.common.tileentities.machines.multi;

import space.accident.structurelib.alignment.constructable.IConstructable;
import space.accident.structurelib.structure.IStructureDefinition;
import space.accident.structurelib.structure.StructureDefinition;
import gregtech.api.GregTech_API;
import gregtech.api.enums.HeatingCoilLevel;
import gregtech.api.enums.Materials;
import gregtech.api.gui.GT_GUIContainer_MultiMachine;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch_Energy;
import gregtech.api.objects.GT_ChunkManager;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ExoticEnergyInputHelper;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraftforge.fluids.FluidStack;

import java.util.List;
import java.util.ArrayList;

import static space.accident.structurelib.structure.StructureUtility.ofBlock;
import static gregtech.api.enums.GT_Values.V;
import static gregtech.api.enums.GT_Values.VN;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.ofCoil;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdderOptional;



public class GT_MetaTileEntity_PlasmaForge extends GT_MetaTileEntity_AbstractMultiFurnace<GT_MetaTileEntity_PlasmaForge> implements IConstructable {

    // 3600 seconds in an hour, 8 hours, 20 ticks in a second.
    double max_efficiency_time_in_ticks = 3600d * 8d * 20d;
    double discount = 1;
    final double maximum_discount = 0.5d;
    private int mHeatingCapacity = 0;
    long running_time = 0;

    private final int min_input_hatch = 0;
    private final int max_input_hatch = 6;
    private final int min_output_hatch = 0;
    private final int max_output_hatch = 2;
    private final int min_input_bus = 0;
    private final int max_input_bus = 6;
    private final int min_output_bus = 0;
    private final int max_output_bus = 1;

    @SuppressWarnings("SpellCheckingInspection")
    private static final String[][] structure_string = new String[][] {
        {"                                 ", "         N   N     N   N         ", "         N   N     N   N         ", "         N   N     N   N         ", "                                 ", "                                 ", "                                 ", "         N   N     N   N         ", "         N   N     N   N         ", " NNN   NNN   N     N   NNN   NNN ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN "},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "         N   N     N   N         ", "                                 ", "         N   N     N   N         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", " CCC   CCC   N     N   CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN           NbbbN NbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbN NbbbN           NbbbN NbbbN", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN    N N    NbbbN NbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "      NNNbbbbbNNsNNbbbbbNNN      ", "    ss   bCCCb     bCCCb   ss    ", "   s     N   N     N   N     s   ", "   s                         s   ", "  N      N   N     N   N      N  ", "  N      bCCCb     bCCCb      N  ", "  N     sbbbbbNNsNNbbbbbs     N  ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", " CbC   CbC   N     N   CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbN           NbbbN NbbbN", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "  s     s               s     s  ", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "NbbbN NbbbN           NbbbN NbbbN", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbNNNNNsNsNNNNNbbbN NbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "    ss   bCCCb     bCCCb   ss    ", "         bCCCb     bCCCb         ", "  s      NCCCN     NCCCN      s  ", "  s      NCCCN     NCCCN      s  ", "         NCCCN     NCCCN         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", " CCCCCCCCC   N     N   CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN           NbbbNNNbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbNNNbbbN           NbbbNNNbbbN", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN    NbN    NbbbNNNbbbN",},
        {"                                 ", "         N   N     N   N         ", "   s     N   N     N   N     s   ", "  s      NCCCN     NCCCN      s  ", "                                 ", "                                 ", "                                 ", "         NCCCN     NCCCN         ", "         N   N     N   N         ", " NNN   NN    N     N    NN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN     NbN     NNN   NNN ",},
        {"                                 ", "                                 ", "   s                         s   ", "  s      NCCCN     NCCCN      s  ", "                                 ", "                                 ", "                                 ", "         NCCCN     NCCCN         ", "                                 ", "   N   N                 N   N   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   N   N                 N   N   ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "   N   N                 N   N   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   N   N       NbN       N   N   ",},
        {"                                 ", "         N   N     N   N         ", "  N      N   N     N   N      N  ", "         NCCCN     NCCCN         ", "                                 ", "                                 ", "                                 ", "         NCCCN     NCCCN         ", "         N   N     N   N         ", " NNN   NN    N     N    NN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN     NbN     NNN   NNN ",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "  N      bCCCb     bCCCb      N  ", "         bCCCb     bCCCb         ", "         NCCCN     NCCCN         ", "         NCCCN     NCCCN         ", "         NCCCN     NCCCN         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", " CCCCCCCCC   N     N   CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN           NbbbNNNbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbNNNbbbN           NbbbNNNbbbN", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN    NbN    NbbbNNNbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "  N     sbbbbbNNsNNbbbbbs     N  ", "         bCCCb     bCCCb         ", "         N   N     N   N         ", "                                 ", "         N   N     N   N         ", "         bCCCb     bCCCb         ", "  s     sbbbbbNNsNNbbbbbs     s  ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", " CbC   CbC   N     N   CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbN           NbbbN NbbbN", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "  s     s               s     s  ", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "NbbbN NbbbN           NbbbN NbbbN", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbNNNNNsNsNNNNNbbbN NbbbN",},
        {" NNN   NNN   N     N   NNN   NNN ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", " NNN   NNN   N     N   NNN   NNN ", "   N   N                 N   N   ", " NNN   NNN   N     N   NNN   NNN ", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", "NNNN   NNNCCCb     bCCCNNN   NNNN", " CCC   CCC   N     N   CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN           NbbbN NbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbN NbbbN           NbbbN NbbbN", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN    NbN    NbbbN NbbbN",},
        {"                                 ", " CCC   CCC   N     N   CCC   CCC ", " CbC   CbC   N     N   CbC   CbC ", " CCCCCCCCC   N     N   CCCCCCCCC ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " CCCCCCCCC   N     N   CCCCCCCCC ", " CbC   CbC   N     N   CbC   CbC ", " CCC   CCC   N     N   CCC   CCC ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", " NNN   NNN     NbN     NNN   NNN ",},
        {"                                 ", " CCC   CCC             CCC   CCC ", " CbC   CbC             CbC   CbC ", " CCCCCCCCC             CCCCCCCCC ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " CCCCCCCCC             CCCCCCCCC ", " CbC   CbC             CbC   CbC ", " CCC   CCC             CCC   CCC ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N      NbN      N     N  ",},
        {"                                 ", " CCC   CCC             CCC   CCC ", " CbC   CbC             CbC   CbC ", " CCCCCCCCC             CCCCCCCCC ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " CCCCCCCCC             CCCCCCCCC ", " CbC   CbC             CbC   CbC ", " CCC   CCC             CCC   CCC ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N      NbN      N     N  ",},
        {" NNN   NNN             NNN   NNN ", "NbbbN NbbbN           NbbbN NbbbN", "NbbbN NbbbN           NbbbN NbbbN", "NbbbNNNbbbN           NbbbNNNbbbN", " NNN   NNN             NNN   NNN ", "   N   N                 N   N   ", " NNN   NNN             NNN   NNN ", "NbbbNNNbbbN           NbbbNNNbbbN", "NbbbN NbbbN           NbbbN NbbbN", "NbbbN NbbbN           NbbbN NbbbN", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N     NsNsN     N     N  ",},
        {"                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N    NbbbbbN    N     N  ",},
        {"                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                N                ", " NsNNNNNsNNNNsbbbbbsNNNNsNNNNNsN ",},
        {"                                 ", "                                 ", "  s     s               s     s  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  s     s               s     s  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                ~                ", "               NNN               ", "  NbbbbbNbbbbNbbbbbNbbbbNbbbbbN  ",},
        {"                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                N                ", " NsNNNNNsNNNNsbbbbbsNNNNsNNNNNsN ",},
        {"                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N               N     N  ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N    NbbbbbN    N     N  ",},
        {" NNN   NNN             NNN   NNN ", "NbbbN NbbbN           NbbbN NbbbN", "NbbbN NbbbN           NbbbN NbbbN", "NbbbNNNbbbN           NbbbNNNbbbN", " NNN   NNN             NNN   NNN ", "   N   N                 N   N   ", " NNN   NNN             NNN   NNN ", "NbbbNNNbbbN           NbbbNNNbbbN", "NbbbN NbbbN           NbbbN NbbbN", "NbbbN NbbbN           NbbbN NbbbN", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N     NsNsN     N     N  ",},
        {"                                 ", " CCC   CCC             CCC   CCC ", " CbC   CbC             CbC   CbC ", " CCCCCCCCC             CCCCCCCCC ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " CCCCCCCCC             CCCCCCCCC ", " CbC   CbC             CbC   CbC ", " CCC   CCC             CCC   CCC ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N      NbN      N     N  ",},
        {"                                 ", " CCC   CCC             CCC   CCC ", " CbC   CbC             CbC   CbC ", " CCCCCCCCC             CCCCCCCCC ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " CCCCCCCCC             CCCCCCCCC ", " CbC   CbC             CbC   CbC ", " CCC   CCC             CCC   CCC ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "  N     N      NbN      N     N  ",},
        {"                                 ", " CCC   CCC   N     N   CCC   CCC ", " CbC   CbC   N     N   CbC   CbC ", " CCCCCCCCC   N     N   CCCCCCCCC ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " CCCCCCCCC   N     N   CCCCCCCCC ", " CbC   CbC   N     N   CbC   CbC ", " CCC   CCC   N     N   CCC   CCC ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", " NNN   NNN     NbN     NNN   NNN ",},
        {" NNN   NNN   N     N   NNN   NNN ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", " NNN   NNN   N     N   NNN   NNN ", "   N   N                 N   N   ", " NNN   NNN   N     N   NNN   NNN ", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", "NNNN   NNNCCCb     bCCCNNN   NNNN", " CCC   CCC   N     N   CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN           NbbbN NbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbN NbbbN           NbbbN NbbbN", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN    NbN    NbbbN NbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "  N     sbbbbbNNsNNbbbbbs     N  ", "         bCCCb     bCCCb         ", "         N   N     N   N         ", "                                 ", "         N   N     N   N         ", "         bCCCb     bCCCb         ", "  s     sbbbbbNNsNNbbbbbs     s  ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", " CbC   CbC   N     N   CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbN           NbbbN NbbbN", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "  s     s               s     s  ", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "NbbbN NbbbN           NbbbN NbbbN", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbNNNNNsNsNNNNNbbbN NbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "  N      bCCCb     bCCCb      N  ", "         bCCCb     bCCCb         ", "         NCCCN     NCCCN         ", "         NCCCN     NCCCN         ", "         NCCCN     NCCCN         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", " CCCCCCCCC   N     N   CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN           NbbbNNNbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbNNNbbbN           NbbbNNNbbbN", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN    NbN    NbbbNNNbbbN",},
        {"                                 ", "         N   N     N   N         ", "  N      N   N     N   N      N  ", "         NCCCN     NCCCN         ", "                                 ", "                                 ", "                                 ", "         NCCCN     NCCCN         ", "         N   N     N   N         ", " NNN   NN    N     N    NN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN     NbN     NNN   NNN ",},
        {"                                 ", "                                 ", "   s                         s   ", "  s      NCCCN     NCCCN      s  ", "                                 ", "                                 ", "                                 ", "         NCCCN     NCCCN         ", "                                 ", "   N   N                 N   N   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   N   N                 N   N   ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", "   N   N                 N   N   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   N   N       NbN       N   N   ",},
        {"                                 ", "         N   N     N   N         ", "   s     N   N     N   N     s   ", "  s      NCCCN     NCCCN      s  ", "                                 ", "                                 ", "                                 ", "         NCCCN     NCCCN         ", "         N   N     N   N         ", " NNN   NN    N     N    NN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "   C   C                 C   C   ", "   C   C                 C   C   ", "   C   C                 C   C   ", " NNN   NNN     NbN     NNN   NNN ",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "    ss   bCCCb     bCCCb   ss    ", "         bCCCb     bCCCb         ", "  s      NCCCN     NCCCN      s  ", "  s      NCCCN     NCCCN      s  ", "         NCCCN     NCCCN         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "NbbbNNNbbNCCCb     bCCCNbbNNNbbbN", " CCCCCCCCC   N     N   CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN           NbbbNNNbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbNNNbbbN           NbbbNNNbbbN", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", " CCCCCCCCC             CCCCCCCCC ", "NbbbNNNbbbN    NbN    NbbbNNNbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "      NNNbbbbbNNsNNbbbbbNNN      ", "    ss   bCCCb     bCCCb   ss    ", "   s     N   N     N   N     s   ", "   s                         s   ", "  N      N   N     N   N      N  ", "  N      bCCCb     bCCCb      N  ", "  N     sbbbbbNNsNNbbbbbs     N  ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", " CbC   CbC   N     N   CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbN           NbbbN NbbbN", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "  s     s               s     s  ", " NNN   NNN             NNN   NNN ", " NNN   NNN             NNN   NNN ", "NbbbN NbbbN           NbbbN NbbbN", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", " CbC   CbC             CbC   CbC ", "NbbbN NbbbNNNNNsNsNNNNNbbbN NbbbN",},
        {"         N   N     N   N         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "         N   N     N   N         ", "                                 ", "         N   N     N   N         ", "         bCCCb     bCCCb         ", "         bCCCb     bCCCb         ", "NbbbN NbbNCCCb     bCCCNbbN NbbbN", " CCC   CCC   N     N   CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN           NbbbN NbbbN", "  N     N               N     N  ", "  N     N               N     N  ", "                                 ", "  N     N               N     N  ", "  N     N               N     N  ", "NbbbN NbbbN           NbbbN NbbbN", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", " CCC   CCC             CCC   CCC ", "NbbbN NbbbN    N N    NbbbN NbbbN",},
        {"                                 ", "         N   N     N   N         ", "         N   N     N   N         ", "         N   N     N   N         ", "                                 ", "                                 ", "                                 ", "         N   N     N   N         ", "         N   N     N   N         ", " NNN   NNN   N     N   NNN   NNN ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ", "                                 ", "                                 ", "                                 ", " NNN   NNN             NNN   NNN ",}
    };


    protected static final int DIM_TRANS_CASING = 12;
    protected static final int DIM_INJECTION_CASING = 13;
    protected static final int DIM_BRIDGE_CASING = 14;

    private boolean isMultiChunkloaded = true;

    protected static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<GT_MetaTileEntity_PlasmaForge> STRUCTURE_DEFINITION = StructureDefinition.<GT_MetaTileEntity_PlasmaForge>builder()
        .addShape(STRUCTURE_PIECE_MAIN, structure_string)
        .addElement('C', ofCoil(GT_MetaTileEntity_PlasmaForge::setCoilLevel, GT_MetaTileEntity_PlasmaForge::getCoilLevel))
        .addElement('b', ofHatchAdderOptional(GT_MetaTileEntity_PlasmaForge::addBottomHatch, DIM_INJECTION_CASING, 3, GregTech_API.sBlockCasings1, DIM_INJECTION_CASING))
        .addElement('N', ofBlock(GregTech_API.sBlockCasings1, DIM_TRANS_CASING))
        .addElement('s', ofBlock(GregTech_API.sBlockCasings1, DIM_BRIDGE_CASING))
        .build();

    @Override
    protected boolean addBottomHatch(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        boolean exotic = addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex);
        return super.addBottomHatch(aTileEntity, aBaseCasingIndex) || exotic;
    }

    public GT_MetaTileEntity_PlasmaForge(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_PlasmaForge(String aName) {
        super(aName);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_PlasmaForge(this.mName);
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addInfo("Transcending Dimensional Boundaries.")
            .addInfo("Takes " + EnumChatFormatting.RED + GT_Utility.formatNumbers(max_efficiency_time_in_ticks/(3600*20)) + EnumChatFormatting.GRAY + " hours of continuous run time to fully breach dimensional")
            .addInfo("boundaries and achieve maximum efficiency. This reduces fuel")
            .addInfo("consumption by up to " + EnumChatFormatting.RED + GT_Utility.formatNumbers(100*maximum_discount) + "%" + EnumChatFormatting.GRAY + ". Does not overclock.")
            .addInfo("Author: " +
                EnumChatFormatting.DARK_RED + EnumChatFormatting.BOLD + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "C" +
                EnumChatFormatting.GOLD + EnumChatFormatting.BOLD + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "o" +
                EnumChatFormatting.GREEN + EnumChatFormatting.BOLD + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "l" +
                EnumChatFormatting.DARK_AQUA + EnumChatFormatting.BOLD + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "e" +
                EnumChatFormatting.DARK_PURPLE + EnumChatFormatting.BOLD + EnumChatFormatting.ITALIC + EnumChatFormatting.UNDERLINE + "n")
            .addSeparator()
            .beginStructureBlock(33, 24, 33, false)
            .addStructureInfo("DTPF Structure is too complex! See schematic for details.")
            .addStructureInfo("2112 Heating coils required.")
            .addStructureInfo("120 Dimensional bridge blocks required.")
            .addStructureInfo("1270 Dimensional injection casings required.")
            .addStructureInfo("2121 Dimensionally transcendent casings required.")
            .addStructureInfo("--------------------------------------------")
            .addStructureInfo("Requires 1-2 energy hatches or 1 TT energy hatch.")
            .addStructureInfo("Requires 1 maintenance hatch.")
            .addStructureInfo("Requires 0-6 input hatches.")
            .addStructureInfo("Requires 0-2 output hatches.")
            .addStructureInfo("Requires 0-2 input busses.")
            .addStructureInfo("Requires 0-2 output busses.")
            .addStructureInfo("--------------------------------------------")
            .addStructureInfo("If you are having difficulties with the blueprint")
            .addStructureInfo("you can rotate the controller. This multi is symmetrical.")
            .toolTipFinisher("Gregtech");
        return tt;
    }

    @Override
    public boolean addToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        boolean exotic = addExoticEnergyInputToMachineList(aTileEntity, aBaseCasingIndex);
        return super.addToMachineList(aTileEntity, aBaseCasingIndex) || exotic;
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            if (aActive)
                return new ITexture[]{
                    casingTexturePages[0][DIM_BRIDGE_CASING],
                    TextureFactory.builder().addIcon(OVERLAY_DTPF_ON).extFacing().build(),
                    TextureFactory.builder().addIcon(OVERLAY_FUSION1_GLOW).extFacing().glow().build()};
            return new ITexture[]{
                casingTexturePages[0][DIM_BRIDGE_CASING],
                TextureFactory.builder().addIcon(OVERLAY_DTPF_OFF).extFacing().build()};
        }
        return new ITexture[]{casingTexturePages[0][DIM_BRIDGE_CASING]};
    }

    @Override
    public Object getClientGUI(int aID, InventoryPlayer aPlayerInventory, IGregTechTileEntity aBaseMetaTileEntity) {
        return new GT_GUIContainer_MultiMachine(aPlayerInventory, aBaseMetaTileEntity, getLocalName(), "PlasmaForge.png");
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack){
        return 0;
    }

    @Override
    public GT_Recipe.GT_Recipe_Map getRecipeMap() {
        return GT_Recipe.GT_Recipe_Map.sPlasmaForgeRecipes;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_PlasmaForge> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        boolean recipe_process = processRecipe(getCompactedInputs(), getCompactedFluids());

        // If recipe cannot be found then continuity is broken and reset running time to 0.
        if (!recipe_process) {
            running_time = 0;
            discount = 1;
        }

        return recipe_process;
    }

    protected boolean processRecipe(ItemStack[] tItems, FluidStack[] tFluids) {

        // Get information about multi configuration.
        long tVoltage = GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList());
        byte tTier = (byte) Math.max(1, GT_Utility.getTier(tVoltage));

        // Look up recipe. If not found it will return null.
        GT_Recipe tRecipe_0 = GT_Recipe.GT_Recipe_Map.sPlasmaForgeRecipes.findRecipe(
            getBaseMetaTileEntity(),
            false,
            V[tTier],
            tFluids,
            tItems
        );

        // Sanity check.
        if (tRecipe_0 == null)
            return false;

        // If coil heat capacity is too low, refuse to start recipe.
        if (mHeatingCapacity <= tRecipe_0.mSpecialValue)
            return false;

        // Reduce fuel quantity if machine has been running for long enough.
        GT_Recipe tRecipe_1 = tRecipe_0.copy();
        for(int i = 0; i < tRecipe_0.mFluidInputs.length; i++) {
            if (tRecipe_1.mFluidInputs[i].isFluidEqual(Materials.ExcitedDTCC.getFluid(1L))) {
                // If running for max_efficiency_time_in_ticks then discount is at maximum
                double time_percentage = running_time / max_efficiency_time_in_ticks;
                time_percentage = Math.min(time_percentage, 1.0d);
                discount = (1 - time_percentage);
                discount = Math.max(maximum_discount, discount);
                tRecipe_1.mFluidInputs[i].amount = (int) Math.round(tRecipe_1.mFluidInputs[i].amount * discount);
            }
        }

        // Takes items/fluids from hatches/busses.
        if (!tRecipe_1.isRecipeInputEqual(true, tFluids, tItems)) {
            return false;
        }

        // Vital recipe info.
        mEUt = -tRecipe_0.mEUt;
        mMaxProgresstime = tRecipe_0.mDuration;
        mMaxProgresstime = Math.max(1, mMaxProgresstime);

        // Output items/fluids.
        mOutputItems = tRecipe_0.mOutputs.clone();
        mOutputFluids = tRecipe_0.mFluidOutputs.clone();
        updateSlots();

        // All conditions met so increment running_time.
        running_time += mMaxProgresstime;
        return true;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {

        this.mHeatingCapacity = 0;

        setCoilLevel(HeatingCoilLevel.None);

        if (!checkPiece(STRUCTURE_PIECE_MAIN, 16, 21, 16))
            return false;

        if (getCoilLevel() == HeatingCoilLevel.None)
            return false;

        // Item input bus check.
        if ((mInputBusses.size() < min_input_bus) || (mInputBusses.size() > max_input_bus))
            return false;

        // Item output bus check.
        if ((mOutputBusses.size() < min_output_bus) || (mOutputBusses.size() > max_output_bus))
            return false;

        // Fluid input hatch check.
        if ((mInputHatches.size() < min_input_hatch) || (mInputHatches.size() > max_input_hatch))
            return false;

        // Fluid output hatch check.
        if ((mOutputHatches.size() < min_output_hatch) || (mOutputHatches.size() > max_output_hatch))
            return false;

        // If there is more than 1 TT energy hatch, the structure check will fail.
        // If there is a TT hatch and a normal hatch, the structure check will fail.
        if (mExoticEnergyHatches.size() > 0){
            if (mEnergyHatches.size() > 0) return false;
            if (mExoticEnergyHatches.size() > 1) return false;
        }

        // If there is 0 or more than 2 energy hatches structure check will fail.
        if (mEnergyHatches.size() > 0) {
            if (mEnergyHatches.size() > 2) return false;

            // Check will also fail if energy hatches are not of the same tier.
            byte tier_of_hatch = mEnergyHatches.get(0).mTier;
            for(GT_MetaTileEntity_Hatch_Energy energyHatch : mEnergyHatches) {
                if (energyHatch.mTier != tier_of_hatch) { return false; }
            }
        }

        // One maintenance hatch only. Mandatory.
        if (mMaintenanceHatches.size() != 1)
            return false;

        // Heat capacity of coils used on multi. No free heat from extra EU!
        this.mHeatingCapacity = (int) getCoilLevel().getHeat();

        // All structure checks passed, return true.
        return true;
    }

    public void clearHatches() {
        super.clearHatches();
        mExoticEnergyHatches.clear();
    }

    @Override
    public boolean addOutput(FluidStack aLiquid) {
        if (aLiquid == null)
            return false;
        FluidStack tLiquid = aLiquid.copy();

        return dumpFluid(this.mOutputHatches, tLiquid, true) ||
            dumpFluid(this.mOutputHatches, tLiquid, false);
    }

    @Override
    public boolean drainEnergyInput(long aEU) {
        return GT_ExoticEnergyInputHelper.drainEnergy(aEU, getExoticAndNormalEnergyHatchList());
    }

    @Override
    public boolean onRunningTick(ItemStack aStack) {
        if (mEUt < 0) {
            if (!drainEnergyInput(-mEUt)) {
                running_time = 0;
                discount = 1;
                criticalStopMachine();
                return false;
            }
        }

        return true;
    }

    @Override
    public String[] getInfoData() {

        long storedEnergy=0;
        long maxEnergy=0;

        for(GT_MetaTileEntity_Hatch tHatch : mExoticEnergyHatches) {
            if (isValidMetaTileEntity(tHatch)) {
                storedEnergy+=tHatch.getBaseMetaTileEntity().getStoredEU();
                maxEnergy+=tHatch.getBaseMetaTileEntity().getEUCapacity();
            }
        }

        return new String[]{
            "------------ Critical Information ------------",
            StatCollector.translateToLocal("GT5U.multiblock.Progress") + ": " +
                EnumChatFormatting.GREEN + GT_Utility.formatNumbers(mProgresstime) + EnumChatFormatting.RESET + "t / " +
                EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(mMaxProgresstime) + EnumChatFormatting.RESET + "t",
            StatCollector.translateToLocal("GT5U.multiblock.energy") + ": " +
                EnumChatFormatting.GREEN + GT_Utility.formatNumbers(storedEnergy) + EnumChatFormatting.RESET + " EU / " +
                EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(maxEnergy) + EnumChatFormatting.RESET + " EU",
            StatCollector.translateToLocal("GT5U.multiblock.usage") + ": " +
                EnumChatFormatting.RED + GT_Utility.formatNumbers(-mEUt) + EnumChatFormatting.RESET + " EU/t",
            StatCollector.translateToLocal("GT5U.multiblock.mei") + ": " +
                EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList())) + EnumChatFormatting.RESET + " EU/t(*" + EnumChatFormatting.YELLOW + GT_Utility.formatNumbers(GT_ExoticEnergyInputHelper.getMaxInputAmpsMulti(getExoticAndNormalEnergyHatchList())) + EnumChatFormatting.RESET + "A) " +
                StatCollector.translateToLocal("GT5U.machines.tier") + ": " +
                EnumChatFormatting.YELLOW + VN[GT_Utility.getTier(GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList()))] + EnumChatFormatting.RESET,
            StatCollector.translateToLocal("GT5U.EBF.heat") + ": " +
                EnumChatFormatting.GREEN + GT_Utility.formatNumbers(mHeatingCapacity) + EnumChatFormatting.RESET + " K",
            "Ticks run: " + EnumChatFormatting.GREEN + GT_Utility.formatNumbers(running_time) + EnumChatFormatting.RESET + ", Fuel Discount: " + EnumChatFormatting.RED + GT_Utility.formatNumbers(100 * (1-discount)) + EnumChatFormatting.RESET + "%",
            "-----------------------------------------"
        };
    }

    public List<GT_MetaTileEntity_Hatch> getExoticAndNormalEnergyHatchList() {
        List<GT_MetaTileEntity_Hatch> tHatches = new ArrayList<>();
        tHatches.addAll(mExoticEnergyHatches);
        tHatches.addAll(mEnergyHatches);
        return tHatches;
    }

    public void onPostTick(IGregTechTileEntity aBaseMetaTileEntity, long aTick) {
        if (aBaseMetaTileEntity.isServerSide() && !aBaseMetaTileEntity.isAllowedToWork()) {
            // Reset running time and discount.
            running_time = 0;
            discount = 1;
            // If machine has stopped, stop chunkloading.
            GT_ChunkManager.releaseTicket((TileEntity) aBaseMetaTileEntity);
            isMultiChunkloaded = false;
        } else if (aBaseMetaTileEntity.isServerSide() && aBaseMetaTileEntity.isAllowedToWork() && !isMultiChunkloaded) {
            // Load a 3x3 area centered on controller when machine is running.
            GT_ChunkManager.releaseTicket((TileEntity) aBaseMetaTileEntity);

            int ControllerXCoordinate = ((TileEntity) aBaseMetaTileEntity).xCoord;
            int ControllerZCoordinate = ((TileEntity) aBaseMetaTileEntity).zCoord;

            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate, ControllerZCoordinate));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate + 16, ControllerZCoordinate));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate - 16, ControllerZCoordinate));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate, ControllerZCoordinate + 16));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate, ControllerZCoordinate - 16));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate + 16, ControllerZCoordinate + 16));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate + 16, ControllerZCoordinate - 16));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate - 16, ControllerZCoordinate + 16));
            GT_ChunkManager.requestChunkLoad((TileEntity) aBaseMetaTileEntity, new ChunkCoordIntPair(ControllerXCoordinate - 16, ControllerZCoordinate - 16));

            isMultiChunkloaded = true;
        }

        super.onPostTick(aBaseMetaTileEntity, aTick);
    }

    @Override
    public void construct(ItemStack stackSize, boolean hintsOnly) {
        buildPiece(STRUCTURE_PIECE_MAIN, stackSize, hintsOnly, 16,21,16);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setLong("eRunningTime", running_time);
        super.saveNBTData(aNBT);
    }

    @Override
    public void loadNBTData(final NBTTagCompound aNBT) {
        running_time = aNBT.getLong("eRunningTime");
        super.loadNBTData(aNBT);
    }
}
