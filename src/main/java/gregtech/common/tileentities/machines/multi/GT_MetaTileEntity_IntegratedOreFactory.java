package gregtech.common.tileentities.machines.multi;

import space.accident.structurelib.alignment.IAlignmentLimits;
import space.accident.structurelib.structure.IStructureDefinition;
import space.accident.structurelib.structure.StructureDefinition;
import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.GregTech_API;
import gregtech.api.enums.GT_Values;
import gregtech.api.enums.Materials;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.*;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_ModHandler;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import gregtech.api.util.GT_Utility;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.OreDictionary;

import java.util.*;
import java.util.stream.Collectors;

import static space.accident.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.ofFrame;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdderOptional;

public class GT_MetaTileEntity_IntegratedOreFactory extends GT_MetaTileEntity_EnhancedMultiBlockBase<GT_MetaTileEntity_IntegratedOreFactory> {

    private static final int CASING_INDEX1 = 183;
    private static final int CASING_INDEX2 = 49;
    private static final int MAX_PARA = 1024;
    private static final String CRUSH = "Macerate";
    private static final String WASH = "Ore Washer";
    private static final String THERMAL = "Thermal Centrifuge";
    private static final String CENTRIFUGE = "Centrifuge";
    private static final String SIFTER = "Sifter";
    private static final String CHEM_WASH = "Chemical Bathing";
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<GT_MetaTileEntity_IntegratedOreFactory> STRUCTURE_DEFINITION = StructureDefinition.<GT_MetaTileEntity_IntegratedOreFactory>builder()
        .addShape(STRUCTURE_PIECE_MAIN, transpose(new String[][]{
            {"           ", "           ", "       WWW ", "       WWW ", "           ", "           "},
            {"           ", "       sss ", "      sppps", "      sppps", "       sss ", "           "},
            {"           ", "       sss ", "      s   s", "      s   s", "       sss ", "           "},
            {"           ", "       sss ", "      sppps", "      sppps", "       sss ", "           "},
            {"           ", "       sss ", "      s   s", "      s   s", "       sss ", "           "},
            {"           ", "       sss ", "      sppps", "      sppps", "       sss ", "           "},
            {"iiiiii     ", "iIIIIiisssi", "iIIIIis   s", "iIIIIis   s", "iIIIIiisssi", "iiiiii     "},
            {"iggggi     ", "gt  t isssi", "g xx  sppps", "g xx  sppps", "gt  t isssi", "iggggi     "},
            {"iggggi     ", "gt  t isssi", "g xx  s   s", "g xx  s   s", "gt  t isssi", "iggggi     "},
            {"iggggi     ", "gt  t is~si", "g xx  spppO", "g xx  spppO", "gt  t isssi", "iggggi     "},
            {"iggggi     ", "gt  t isssi", "g xx  s   O", "g xx  s   O", "gt  t isssi", "iggggi     "},
            {"EEEEEE     ", "EEEEEEEEEEE", "EEEEEEEEEEE", "EEEEEEEEEEE", "EEEEEEEEEEE", "EEEEEE     "}
        }))
        .addElement('i', ofBlock(GregTech_API.sBlockCasings8, 7))
        .addElement('s', ofBlock(GregTech_API.sBlockCasings4, 1))
        .addElement('g', ofBlockAnyMeta(GameRegistry.findBlock("IC2", "blockAlloyGlass")))
        .addElement('x', ofBlock(GregTech_API.sBlockCasings2, 3))
        .addElement('p', ofBlock(GregTech_API.sBlockCasings2, 15))
        .addElement('t', ofFrame(Materials.TungstenSteel))
        .addElement('E', ofHatchAdderOptional(GT_MetaTileEntity_IntegratedOreFactory::addButtonHatchToMachineList, CASING_INDEX1, 1, GregTech_API.sBlockCasings8, 7))
        .addElement('I', ofHatchAdderOptional(GT_MetaTileEntity_IntegratedOreFactory::addSolidInputToMachineList, CASING_INDEX1, 2, GregTech_API.sBlockCasings8, 7))
        .addElement('W', ofHatchAdderOptional(GT_MetaTileEntity_IntegratedOreFactory::addFluidInputToMachineList, CASING_INDEX2, 3, GregTech_API.sBlockCasings4, 1))
        .addElement('O', ofHatchAdderOptional(GT_MetaTileEntity_IntegratedOreFactory::addOutputToMachineList, CASING_INDEX2, 4, GregTech_API.sBlockCasings4, 1))
        .build();

    private static final HashSet<Integer> isCrushedOre = new HashSet<>();
    private static final HashSet<Integer> isCrushedPureOre = new HashSet<>();
    private static final HashSet<Integer> isPureDust = new HashSet<>();
    private static final HashSet<Integer> isImpureDust = new HashSet<>();
    private static final HashSet<Integer> isThermal = new HashSet<>();
    private static final HashSet<Integer> isOre = new HashSet<>();
    private static boolean isInit = false;
    private ItemStack[] sMidProduct;
    private int sMode = 0;
    private boolean sVoidStone = false;

    private static void initHash() {
        for (String name : OreDictionary.getOreNames()) {
            if (name == null || name.isEmpty()) continue;
            if (name.startsWith("crushedPurified")) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    isCrushedPureOre.add(GT_Utility.stackToInt(stack));
                }
            } else if (name.startsWith("crushedCentrifuged")) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    isThermal.add(GT_Utility.stackToInt(stack));
                }
            } else if (name.startsWith("crushed")) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    isCrushedOre.add(GT_Utility.stackToInt(stack));
                }
            } else if (name.startsWith("dustImpure")) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    isImpureDust.add(GT_Utility.stackToInt(stack));
                }
            } else if (name.startsWith("dustPure")) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    isPureDust.add(GT_Utility.stackToInt(stack));
                }
            } else if (name.startsWith("ore")) {
                for (ItemStack stack : OreDictionary.getOres(name)) {
                    isOre.add(GT_Utility.stackToInt(stack));
                }
            }
        }
    }

    public GT_MetaTileEntity_IntegratedOreFactory(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_IntegratedOreFactory(String aName) {
        super(aName);
    }

    public boolean addButtonHatchToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) {
            return false;
        }
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) return false;
        if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Energy) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            return mEnergyHatches.add((GT_MetaTileEntity_Hatch_Energy) aMetaTileEntity);
        } else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Maintenance) {
            ((GT_MetaTileEntity_Hatch_Maintenance) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            return mMaintenanceHatches.add((GT_MetaTileEntity_Hatch_Maintenance) aMetaTileEntity);
        }
        return false;
    }

    public boolean addSolidInputToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) return false;
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) return false;
        if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_InputBus) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            ((GT_MetaTileEntity_Hatch_InputBus) aMetaTileEntity).mRecipeMap = getRecipeMap();
            return mInputBusses.add((GT_MetaTileEntity_Hatch_InputBus) aMetaTileEntity);
        }
        return false;
    }

    public boolean addFluidInputToMachineList(IGregTechTileEntity aTileEntity, int aBaseCasingIndex) {
        if (aTileEntity == null) return false;
        IMetaTileEntity aMetaTileEntity = aTileEntity.getMetaTileEntity();
        if (aMetaTileEntity == null) return false;
        if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Input) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            ((GT_MetaTileEntity_Hatch_Input) aMetaTileEntity).mRecipeMap = getRecipeMap();
            return mInputHatches.add((GT_MetaTileEntity_Hatch_Input) aMetaTileEntity);
        } else if (aMetaTileEntity instanceof GT_MetaTileEntity_Hatch_Muffler) {
            ((GT_MetaTileEntity_Hatch) aMetaTileEntity).updateTexture(aBaseCasingIndex);
            return mMufflerHatches.add((GT_MetaTileEntity_Hatch_Muffler) aMetaTileEntity);
        }
        return false;
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_IntegratedOreFactory> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        final GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Ore Processor")
            .addInfo("Controller Block for the Integrated Ore Factory")
            .addInfo("It is OP. I mean ore processor.")
            .addInfo("Do all ore procession in one step.")
            .addInfo("Can process up to 1024 ores per time.")
            .addInfo("Every ore costs 30EU/t, 2L lubricant, 200L distilled water.")
            .addInfo("Process time is depend on mode.")
            .addInfo("Use a screwdriver to switch mode.")
            .addInfo("Sneak click with screwdriver to void the stone dusts.")
            .addSeparator()
            .beginStructureBlock(6, 12, 11, false)
            .addController("The third layer")
            .addEnergyHatch("Button Casing", 1)
            .addMaintenanceHatch("Button Casing", 1)
            .addInputBus("Input ore/crushed ore", 2)
            .addInputHatch("Input lubricant/distilled water/washing chemicals", 3)
            .addMufflerHatch("Output Pollution", 3)
            .addOutputBus("Output products", 4)
            .toolTipFinisher("Gregtech");
        return tt;
    }

    @Override
    protected IAlignmentLimits getInitialAlignmentLimits() {
        return (d, r, f) -> !r.isUpsideDown() && !f.isVerticallyFliped();
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 8, 9, 1);
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    private int getTime() {
        switch (sMode) {
            case 0:
                return 30 * 20;
            case 1:
                return 15 * 20;
            case 2:
                return 10 * 20;
            case 3:
                return 20 * 20;
            case 4:
                return 17 * 20;
        }
        //go to hell
        return 1000000000;
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        if (!isInit) {
            initHash();
            isInit = true;
        }

        int tCharged = MAX_PARA;
        List<ItemStack> tInput = getStoredInputs();
        List<FluidStack> tInputFluid = getStoredFluids();

        int tLube = 0;
        int tWater = 0;

        for (FluidStack fluid : tInputFluid) {
            if (fluid != null && fluid.equals(GT_ModHandler.getDistilledWater(1L))) {
                tWater += fluid.amount;
            } else if (fluid != null && fluid.equals(Materials.Lubricant.getFluid(1L))) {
                tLube += fluid.amount;
            }
        }

        tCharged = Math.min(tCharged, tLube / 2);
        tCharged = Math.min(tCharged, tWater / 200);

        List<ItemStack> tOres = new ArrayList<>();
        int tRealUsed = 0;

        for (ItemStack ore : tInput) {
            if (tCharged <= 0) break;
            int tID = GT_Utility.stackToInt(ore);
            if (tID == 0) continue;
            if (isPureDust.contains(tID) || isImpureDust.contains(tID) || isCrushedPureOre.contains(tID) ||
                isThermal.contains(tID) || isCrushedOre.contains(tID) || isOre.contains(tID)) {
                if (ore.stackSize <= tCharged) {
                    tRealUsed += ore.stackSize;
                    tOres.add(GT_Utility.copy(ore));
                    tCharged -= ore.stackSize;
                    ore.stackSize = 0;
                } else {
                    tRealUsed = tCharged;
                    tOres.add(GT_Utility.copyAmount(tCharged, ore));
                    ore.stackSize -= tCharged;
                    tCharged = 0;
                    break;
                }
            }
        }

        if (tRealUsed == 0) {
            return false;
        }

        depleteInput(GT_ModHandler.getDistilledWater(tRealUsed * 200L));
        depleteInput(Materials.Lubricant.getFluid(tRealUsed * 2L));

        sMidProduct = tOres.toArray(new ItemStack[0]);

        switch (sMode) {
            case 0:
                doMac(isOre);
                doWash(isCrushedOre);
                doThermal(isCrushedPureOre, isCrushedOre);
                doMac(isThermal, isOre, isCrushedOre, isCrushedPureOre);
                break;
            case 1:
                doMac(isOre);
                doWash(isCrushedOre);
                doMac(isOre, isCrushedOre, isCrushedPureOre);
                doCentrifuge(isImpureDust, isPureDust);
                break;
            case 2:
                doMac(isOre);
                doMac(isThermal, isOre, isCrushedOre, isCrushedPureOre);
                doCentrifuge(isImpureDust, isPureDust);
                break;
            case 3:
                doMac(isOre);
                doWash(isCrushedOre);
                doSift(isCrushedPureOre);
                break;
            case 4:
                doMac(isOre);
                doChemWash(isCrushedOre, isCrushedPureOre);
                doMac(isCrushedOre, isCrushedPureOre);
                doCentrifuge(isImpureDust, isPureDust);
                break;
            default:
                return false;
        }

        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;
        this.mOutputItems = sMidProduct;
        calculateOverclockedNessMulti(30 * tRealUsed, getTime(), 1, getMaxInputVoltage());
        if (this.mEUt > 0) {
            this.mEUt = -this.mEUt;
        }
        this.updateSlots();

        return true;
    }

    @SafeVarargs
    private final boolean checkTypes(int aID, HashSet<Integer>... aTables) {
        for (HashSet<Integer> set : aTables) {
            if (set.contains(aID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public final void onScrewdriverRightClick(byte aSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (aPlayer.isSneaking()) {
            sVoidStone = !sVoidStone;
            GT_Utility.sendChatToPlayer(aPlayer, StatCollector.translateToLocalFormatted("GT5U.machines.oreprocessor.void", sVoidStone));
            return;
        }
        sMode = (sMode + 1) % 5;
        String des;
        switch (sMode) {
            case 0: des = EnumChatFormatting.AQUA + CRUSH + "->" + WASH + "->" + THERMAL + "->" + CRUSH; break;
            case 1: des = EnumChatFormatting.AQUA + CRUSH + "->" + WASH + "->" + CRUSH + "->" + CENTRIFUGE; break;
            case 2: des = EnumChatFormatting.AQUA + CRUSH + "->" + CRUSH + "->" + CENTRIFUGE; break;
            case 3: des = EnumChatFormatting.AQUA + CRUSH + "->" + WASH + "->" + SIFTER; break;
            case 4: des = EnumChatFormatting.AQUA + CRUSH + "->" + CHEM_WASH + "->" + CRUSH + "->" + CENTRIFUGE; break;
            default: des = "";
        }
        GT_Utility.sendChatToPlayer(aPlayer, StatCollector.translateToLocalFormatted("GT5U.machines.oreprocessor", des, getTime() / 20));
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        sMode = aNBT.getInteger("ssMode");
        sVoidStone = aNBT.getBoolean("ssStone");
        super.loadNBTData(aNBT);
    }

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        aNBT.setInteger("ssMode", sMode);
        aNBT.setBoolean("ssStone", sVoidStone);
        super.saveNBTData(aNBT);
    }

    @SafeVarargs
    private final void doMac(HashSet<Integer>... aTables) {
        List<ItemStack> tProduct = new ArrayList<>();
        if (sMidProduct != null) {
            for (ItemStack aStack : sMidProduct) {
                int tID = GT_Utility.stackToInt(aStack);
                if (checkTypes(tID, aTables)) {
                    GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sMaceratorRecipes.findRecipe(getBaseMetaTileEntity(), false, GT_Values.V[15], null, aStack);
                    if (tRecipe != null) {
                        tProduct.addAll(getOutputStack(tRecipe, aStack.stackSize));
                    } else {
                        tProduct.add(aStack);
                    }
                } else {
                    tProduct.add(aStack);
                }
            }
        }
        doCompress(tProduct);
    }

    @SafeVarargs
    private final void doWash(HashSet<Integer>... aTables) {
        List<ItemStack> tProduct = new ArrayList<>();
        if (sMidProduct != null) {
            for (ItemStack aStack : sMidProduct) {
                int tID = GT_Utility.stackToInt(aStack);
                if (checkTypes(tID, aTables)) {
                    GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sOreWasherRecipes.findRecipe(getBaseMetaTileEntity(), false, GT_Values.V[15], new FluidStack[]{GT_ModHandler.getDistilledWater(Integer.MAX_VALUE)}, aStack);
                    if (tRecipe != null) {
                        tProduct.addAll(getOutputStack(tRecipe, aStack.stackSize));
                    } else {
                        tProduct.add(aStack);
                    }
                } else {
                    tProduct.add(aStack);
                }
            }
        }
        doCompress(tProduct);
    }

    @SafeVarargs
    private final void doThermal(HashSet<Integer>... aTables) {
        List<ItemStack> tProduct = new ArrayList<>();
        if (sMidProduct != null) {
            for (ItemStack aStack : sMidProduct) {
                int tID = GT_Utility.stackToInt(aStack);
                if (checkTypes(tID, aTables)) {
                    GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sThermalCentrifugeRecipes.findRecipe(getBaseMetaTileEntity(), false, GT_Values.V[15], null, aStack);
                    if (tRecipe != null) {
                        tProduct.addAll(getOutputStack(tRecipe, aStack.stackSize));
                    } else {
                        tProduct.add(aStack);
                    }
                } else {
                    tProduct.add(aStack);
                }
            }
        }
        doCompress(tProduct);
    }

    @SafeVarargs
    private final void doCentrifuge(HashSet<Integer>... aTables) {
        List<ItemStack> tProduct = new ArrayList<>();
        if (sMidProduct != null) {
            for (ItemStack aStack : sMidProduct) {
                int tID = GT_Utility.stackToInt(aStack);
                if (checkTypes(tID, aTables)) {
                    GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sCentrifugeRecipes.findRecipe(getBaseMetaTileEntity(), false, GT_Values.V[15], null, aStack);
                    if (tRecipe != null) {
                        tProduct.addAll(getOutputStack(tRecipe, aStack.stackSize));
                    } else {
                        tProduct.add(aStack);
                    }
                } else {
                    tProduct.add(aStack);
                }
            }
        }
        doCompress(tProduct);
    }

    @SafeVarargs
    private final void doSift(HashSet<Integer>... aTables) {
        List<ItemStack> tProduct = new ArrayList<>();
        if (sMidProduct != null) {
            for (ItemStack aStack : sMidProduct) {
                int tID = GT_Utility.stackToInt(aStack);
                if (checkTypes(tID, aTables)) {
                    GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sSifterRecipes.findRecipe(getBaseMetaTileEntity(), false, GT_Values.V[15], null, aStack);
                    if (tRecipe != null) {
                        tProduct.addAll(getOutputStack(tRecipe, aStack.stackSize));
                    } else {
                        tProduct.add(aStack);
                    }
                } else {
                    tProduct.add(aStack);
                }
            }
        }
        doCompress(tProduct);
    }

    @SafeVarargs
    private final void doChemWash(HashSet<Integer>... aTables) {
        List<ItemStack> tProduct = new ArrayList<>();
        if (sMidProduct != null) {
            for (ItemStack aStack : sMidProduct) {
                int tID = GT_Utility.stackToInt(aStack);
                if (checkTypes(tID, aTables)) {
                    GT_Recipe tRecipe = GT_Recipe.GT_Recipe_Map.sChemicalBathRecipes.findRecipe(getBaseMetaTileEntity(), false, GT_Values.V[15], getStoredFluids().toArray(new FluidStack[0]), aStack);
                    if (tRecipe != null && tRecipe.getRepresentativeFluidInput(0) != null) {
                        FluidStack tInputFluid = tRecipe.getRepresentativeFluidInput(0).copy();
                        int tStored = getFluidAmount(tInputFluid);
                        int tWashed = Math.min(tStored / tInputFluid.amount, aStack.stackSize);
                        depleteInput(new FluidStack(tInputFluid.getFluid(), tWashed * tInputFluid.amount));
                        tProduct.addAll(getOutputStack(tRecipe, tWashed));
                        if (tWashed < aStack.stackSize) {
                            tProduct.add(GT_Utility.copyAmountUnsafe(aStack.stackSize - tWashed, aStack));
                        }
                    } else {
                        tProduct.add(aStack);
                    }
                } else {
                    tProduct.add(aStack);
                }
            }
        }
        doCompress(tProduct);
    }

    private int getFluidAmount(FluidStack aFluid) {
        int tAmt = 0;
        if (aFluid == null) return 0;
        for (FluidStack fluid : getStoredFluids()) {
            if (aFluid.isFluidEqual(fluid)) {
                tAmt += fluid.amount;
            }
        }
        return tAmt;
    }

    private List<ItemStack> getOutputStack(GT_Recipe aRecipe, int aTime) {
        List<ItemStack> tOutput = new ArrayList<>();
        for (int i = 0; i < aRecipe.mOutputs.length; i ++) {
            int tChance = aRecipe.getOutputChance(i);
            if (tChance == 10000) {
                tOutput.add(GT_Utility.copyAmountUnsafe(aTime * aRecipe.getOutput(i).stackSize, aRecipe.getOutput(i)));
            } else {
                //Use Normal Distribution
                double u = aTime * (tChance / 10000D);
                double e = aTime * (tChance / 10000D) * (1 - (tChance / 10000D));
                Random random = new Random();
                int tAmount = (int) Math.ceil(Math.sqrt(e) * random.nextGaussian() + u);
                tOutput.add(GT_Utility.copyAmountUnsafe(tAmount * aRecipe.getOutput(i).stackSize, aRecipe.getOutput(i)));
            }
        }
        return tOutput.stream().filter(i -> (i != null && i.stackSize > 0)).collect(Collectors.toList());
    }

    private void doCompress(List<ItemStack> aList) {
        HashMap<Integer, Integer> rProduct = new HashMap<>();
        for (ItemStack stack : aList) {
            int tID = GT_Utility.stackToInt(stack);
            if (sVoidStone) {
                if (GT_Utility.areStacksEqual(Materials.Stone.getDust(1), stack)) {
                    continue;
                }
            }
            if (tID != 0) {
                if (rProduct.containsKey(tID)) {
                    rProduct.put(tID, rProduct.get(tID) + stack.stackSize);
                } else {
                    rProduct.put(tID, stack.stackSize);
                }
            }
        }
        sMidProduct = new ItemStack[rProduct.size()];
        int cnt = 0;
        for (Integer id : rProduct.keySet()) {
            ItemStack stack = GT_Utility.intToStack(id);
            sMidProduct[cnt] = GT_Utility.copyAmountUnsafe(rProduct.get(id), stack);
            cnt ++;
        }
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        return checkPiece(STRUCTURE_PIECE_MAIN, 8, 9, 1) && mMaintenanceHatches.size() <= 1 && !mMufflerHatches.isEmpty();
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public int getPollutionPerSecond(ItemStack aStack) {
        return 200;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_IntegratedOreFactory(mName);
    }

    @Override
    public ITexture[] getTexture(IGregTechTileEntity aBaseMetaTileEntity, byte aSide, byte aFacing, byte aColorIndex, boolean aActive, boolean aRedstone) {
        if (aSide == aFacing) {
            if (aActive)
                return new ITexture[]{
                    Textures.BlockIcons.getCasingTextureForId(CASING_INDEX2),
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE).extFacing().build(),
                    TextureFactory.builder().addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_ACTIVE_GLOW).extFacing().glow().build()};
            return new ITexture[]{
                Textures.BlockIcons.getCasingTextureForId(CASING_INDEX2),
                TextureFactory.builder().addIcon(OVERLAY_FRONT_PROCESSING_ARRAY).extFacing().build(),
                TextureFactory.builder().addIcon(OVERLAY_FRONT_PROCESSING_ARRAY_GLOW).extFacing().glow().build()};
        }
        return new ITexture[]{Textures.BlockIcons.getCasingTextureForId(CASING_INDEX2)};
    }
}
