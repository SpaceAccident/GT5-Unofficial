package gregtech.api.net;

import com.google.common.io.ByteArrayDataInput;
import gregtech.api.GregTech_API;
import gregtech.api.interfaces.tileentity.ICoverable;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.CoverableTileEntity;
import gregtech.api.util.ISerializableObject;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

/**
 * Client -> Server : ask for cover data
 */
public class GT_Packet_RequestCoverData extends GT_Packet_New {
    protected int mX;
    protected short mY;
    protected int mZ;

    protected byte side;
    protected int coverID;

    protected EntityPlayerMP mPlayer;

    public GT_Packet_RequestCoverData() {
        super(true);
    }

    public GT_Packet_RequestCoverData(int mX, short mY, int mZ, byte coverSide, int coverID) {
        super(false);
        this.mX = mX;
        this.mY = mY;
        this.mZ = mZ;

        this.side = coverSide;
        this.coverID = coverID;
    }
    public GT_Packet_RequestCoverData(byte coverSide, int coverID, ICoverable tile) {
        super(false);
        this.mX = tile.getXCoord();
        this.mY = tile.getYCoord();
        this.mZ = tile.getZCoord();

        this.side = coverSide;
        this.coverID = coverID;
    }

    @Override
    public byte getPacketID() {
        return 17;
    }

    @Override
    public void encode(ByteBuf aOut) {
        aOut.writeInt(mX);
        aOut.writeShort(mY);
        aOut.writeInt(mZ);

        aOut.writeByte(side);
        aOut.writeInt(coverID);
    }

    @Override
    public GT_Packet_New decode(ByteArrayDataInput aData) {
        return new GT_Packet_RequestCoverData(
                aData.readInt(),
                aData.readShort(),
                aData.readInt(),

                aData.readByte(),
                aData.readInt()
            );
    }

    @Override
    public void setINetHandler(INetHandler aHandler) {
        if (aHandler instanceof NetHandlerPlayServer) {
            mPlayer = ((NetHandlerPlayServer) aHandler).playerEntity;
        }
    }

    @Override
    public void process(IBlockAccess aWorld) {
        if (mPlayer == null) // impossible, but who knows
            return;
        World world = DimensionManager.getWorld(mPlayer.dimension);
        if (world != null) {
            final TileEntity tile = world.getTileEntity(mX, mY, mZ);
            if (tile instanceof CoverableTileEntity) {
                final CoverableTileEntity te = (CoverableTileEntity) tile;
                if (!te.isDead() && te.getCoverIDAtSide(side) == coverID) {
                    te.issueCoverUpdate(side);
                }
            }
        }
    }
}
