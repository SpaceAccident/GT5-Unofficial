package gregtech.common.covers;

import com.google.common.io.ByteArrayDataInput;
import cpw.mods.fml.common.network.ByteBufUtils;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_CoverBehaviorBase;
import gregtech.api.util.GT_RenderingWorld;
import gregtech.api.util.GT_Utility;
import gregtech.api.util.ISerializableObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;

import javax.annotation.Nonnull;

public abstract class GT_Cover_FacadeBase extends GT_CoverBehaviorBase<GT_Cover_FacadeBase.FacadeData> {
    /**
     * This is the Dummy, if there is a generic Cover without behavior
     */
    public GT_Cover_FacadeBase() {
        super(FacadeData.class);
    }

    @Override
    public boolean isSimpleCover() {
        return true;
    }

    @Override
    public FacadeData createDataObject(int aLegacyData) {
        return new FacadeData();
    }

    @Override
    public FacadeData createDataObject() {
        return new FacadeData();
    }

    @Override
    protected FacadeData onCoverScrewdriverClickImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        aCoverVariable.mFlags = ((aCoverVariable.mFlags + 1) & 15);
        GT_Utility.sendChatToPlayer(aPlayer, ((aCoverVariable.mFlags & 1) != 0 ? GT_Utility.trans("128", "Redstone ") : "") + ((aCoverVariable.mFlags & 2) != 0 ? GT_Utility.trans("129", "Energy ") : "") + ((aCoverVariable.mFlags & 4) != 0 ? GT_Utility.trans("130", "Fluids ") : "") + ((aCoverVariable.mFlags & 8) != 0 ? GT_Utility.trans("131", "Items ") : ""));
        return aCoverVariable;
    }

    @Override
    protected boolean letsRedstoneGoInImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 1) != 0;
    }

    @Override
    protected boolean letsRedstoneGoOutImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 1) != 0;
    }

    @Override
    protected boolean letsEnergyInImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 2) != 0;
    }

    @Override
    protected boolean letsEnergyOutImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 2) != 0;
    }

    @Override
    protected boolean letsFluidInImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, Fluid aFluid, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 4) != 0;
    }

    @Override
    protected boolean letsFluidOutImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, Fluid aFluid, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 4) != 0;
    }

    @Override
    protected boolean letsItemsInImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, int aSlot, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 8) != 0;
    }

    @Override
    protected boolean letsItemsOutImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, int aSlot, ICoverable aTileEntity) {
        return (aCoverVariable.mFlags & 8) != 0;
    }

    @Override
    public void placeCover(byte aSide, ItemStack aCover, ICoverable aTileEntity) {
        aTileEntity.setCoverIdAndDataAtSide(aSide, GT_Utility.stackToInt(aCover), new FacadeData(GT_Utility.copyAmount(1, aCover), 0));
        if (aTileEntity.isClientSide())
            GT_RenderingWorld.getInstance().register(aTileEntity.getXCoord(), aTileEntity.getYCoord(), aTileEntity.getZCoord(), getTargetBlock(aCover), getTargetMeta(aCover));
    }

    @Override
    protected ItemStack getDropImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        return aCoverVariable.mStack;
    }

    @Override
    protected ITexture getSpecialCoverTextureImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        if (GT_Utility.isStackInvalid(aCoverVariable.mStack)) return Textures.BlockIcons.ERROR_RENDERING[0];
        Block block = getTargetBlock(aCoverVariable.mStack);
        if (block == null) return Textures.BlockIcons.ERROR_RENDERING[0];
        // TODO: change this when *someone* made the block render in both pass
        if (block.getRenderBlockPass() != 0)
            return Textures.BlockIcons.ERROR_RENDERING[0];
        return TextureFactory.builder().setFromBlock(block, getTargetMeta(aCoverVariable.mStack)).useWorldCoord().setFromSide(ForgeDirection.getOrientation(aSide)).build();
    }

    @Override
    protected Block getFacadeBlockImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        if (GT_Utility.isStackInvalid(aCoverVariable.mStack)) return null;
        return getTargetBlock(aCoverVariable.mStack);
    }

    @Override
    protected int getFacadeMetaImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        if (GT_Utility.isStackInvalid(aCoverVariable.mStack)) return 0;
        return getTargetMeta(aCoverVariable.mStack);
    }

    protected abstract Block getTargetBlock(ItemStack aFacadeStack);

    protected abstract int getTargetMeta(ItemStack aFacadeStack);

    @Override
    protected boolean isDataNeededOnClientImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        return true;
    }

    @Override
    protected void onDataChangedImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        if (aTileEntity.isClientSide())
            GT_RenderingWorld.getInstance().register(aTileEntity.getXCoord(), aTileEntity.getYCoord(), aTileEntity.getZCoord(), getTargetBlock(aCoverVariable.mStack), getTargetMeta(aCoverVariable.mStack));
    }

    @Override
    protected void onDroppedImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity) {
        if (aTileEntity.isClientSide()) {
            for (byte i = 0; i < 6; i++) {
                if (i == aSide) continue;
                // since we do not allow multiple type of facade per block, this check would be enough.
                if (aTileEntity.getCoverBehaviorAtSideNew(i) instanceof GT_Cover_FacadeBase) return;
            }
            if (aCoverVariable.mStack != null)
                // mStack == null -> cover removed before data reach client
                GT_RenderingWorld.getInstance().unregister(aTileEntity.getXCoord(), aTileEntity.getYCoord(), aTileEntity.getZCoord(), getTargetBlock(aCoverVariable.mStack), getTargetMeta(aCoverVariable.mStack));
        }
    }

    @Override
    protected boolean onCoverRightClickImpl(byte aSide, int aCoverID, FacadeData aCoverVariable, ICoverable aTileEntity, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        // in case cover data didn't hit client somehow. maybe he had a ridiculous view distance
        aTileEntity.issueCoverUpdate(aSide);
        return super.onCoverRightClickImpl(aSide, aCoverID, aCoverVariable, aTileEntity, aPlayer, aX, aY, aZ);
    }

    @Override
    public boolean isCoverPlaceable(byte aSide, ItemStack aStack, ICoverable aTileEntity) {
        // blocks that are not rendered in pass 0 are now accepted but rendered awkwardly
        // to render it correctly require changing GT_Block_Machine to render in both pass, which is not really a good idea...
        if (!super.isCoverPlaceable(aSide, aStack, aTileEntity)) return false;
        Block targetBlock = getTargetBlock(aStack);
        if (targetBlock == null) return false;
        // we allow one single type of facade on the same block for now
        // otherwise it's not clear which block this block should impersonate
        // this restriction can be lifted later by specifying a certain facade as dominate one as an extension to this class
        for (byte i = 0; i < 6; i++) {
            if (i == aSide) continue;
            GT_CoverBehaviorBase<?> behavior = aTileEntity.getCoverBehaviorAtSideNew(i);
            if (behavior == null) continue;
            Block facadeBlock = behavior.getFacadeBlock(i, aTileEntity.getCoverIDAtSide(i), aTileEntity.getComplexCoverDataAtSide(i), aTileEntity);
            if (facadeBlock == null) continue;
            if (facadeBlock != targetBlock) return false;
            if (behavior.getFacadeMeta(i, aTileEntity.getCoverIDAtSide(i), aTileEntity.getComplexCoverDataAtSide(i), aTileEntity) != getTargetMeta(aStack)) return false;
        }
        return true;
    }

    public static class FacadeData implements ISerializableObject {
        ItemStack mStack;
        int mFlags;

        public FacadeData() {
        }

        public FacadeData(ItemStack mStack, int mFlags) {
            this.mStack = mStack;
            this.mFlags = mFlags;
        }

        @Nonnull
        @Override
        public ISerializableObject copy() {
            return new FacadeData(mStack, mFlags);
        }

        @Nonnull
        @Override
        public NBTBase saveDataToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            if(mStack != null) tag.setTag("mStack", mStack.writeToNBT(new NBTTagCompound()));
            tag.setByte("mFlags", (byte) mFlags);
            return tag;
        }

        @Override
        public void writeToByteBuf(ByteBuf aBuf) {
            ByteBufUtils.writeItemStack(aBuf, mStack);
            aBuf.writeByte(mFlags);
        }

        @Override
        public void loadDataFromNBT(NBTBase aNBT) {
            NBTTagCompound tag = (NBTTagCompound) aNBT;
            mStack = ItemStack.loadItemStackFromNBT(tag.getCompoundTag("mStack"));
            mFlags = tag.getByte("mFlags");
        }

        @Nonnull
        @Override
        public ISerializableObject readFromPacket(ByteArrayDataInput aBuf, EntityPlayerMP aPlayer) {
            mStack = ISerializableObject.readItemStackFromGreggyByteBuf(aBuf);
            mFlags = aBuf.readByte();
            return this;
        }
    }
}
