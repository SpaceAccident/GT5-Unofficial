package gregtech.common.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.gui.GT_ContainerMetaTile_Machine;
import gregtech.api.gui.GT_Slot_Output;
import gregtech.api.gui.GT_Slot_Render;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.common.tileentities.storage.GT_MetaTileEntity_QuantumChest;
import gregtech.common.tileentities.storage.GT_MetaTileEntity_SuperChest;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;

public class GT_Container_QuantumChest extends GT_ContainerMetaTile_Machine {

    public int mContent = 0;

    public GT_Container_QuantumChest(InventoryPlayer aInventoryPlayer, IGregTechTileEntity aTileEntity) {
        super(aInventoryPlayer, aTileEntity);
    }

    @Override
    public void addSlots(InventoryPlayer aInventoryPlayer) {
        addSlotToContainer(new Slot(mTileEntity, 0, 80, 17));
        addSlotToContainer(new GT_Slot_Output(mTileEntity, 1, 80, 53));
        addSlotToContainer(new GT_Slot_Render(mTileEntity, 2, 59, 42));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        if (mTileEntity.isClientSide() || mTileEntity.getMetaTileEntity() == null) return;
        if (mTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_QuantumChest) {
            mContent = ((GT_MetaTileEntity_QuantumChest) mTileEntity.getMetaTileEntity()).mItemCount;
        } else if (mTileEntity.getMetaTileEntity() instanceof GT_MetaTileEntity_SuperChest) {
            mContent = ((GT_MetaTileEntity_SuperChest) mTileEntity.getMetaTileEntity()).mItemCount;
        } else {
            mContent = 0;
        }

        for (Object crafter : this.crafters) {
            ICrafting aPlayer = (ICrafting) crafter;
            aPlayer.sendProgressBarUpdate(this, 100, mContent & 65535);
            aPlayer.sendProgressBarUpdate(this, 101, mContent >>> 16);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        super.updateProgressBar(id, value);
        switch (id) {
            case 100:
                mContent = mContent & 0xffff0000 | value & 0x0000ffff;
                break;
            case 101:
                mContent = mContent & 0x0000ffff | value << 16;
                break;
        }
    }

    @Override
    public int getSlotCount() {
        return 2;
    }

    @Override
    public int getShiftClickSlotCount() {
        return 1;
    }
}
