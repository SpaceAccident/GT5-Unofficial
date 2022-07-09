package gregtech.api.interfaces.tileentity;

/**
 * This File has just internal Information about the Redstone State of a TileEntity
 */
public interface IRedstoneEmitter extends IHasWorldObjectAndCoords {
    /**
     * gets the Redstone Level the TileEntity should emit to the given Output Side
     */
    byte getOutputRedstoneSignal(byte aSide);

    /**
     * sets the Redstone Level the TileEntity should emit to the given Output Side
     * <p/>
     * Do not use this if ICoverable is implemented. ICoverable has @getInternalOutputRedstoneSignal for Machine internal Output Redstone, so that it doesnt conflict with Cover Redstone.
     * This sets the true Redstone Output Signal. Only Cover Behaviors should use it, not MetaTileEntities.
     */
    void setOutputRedstoneSignal(byte aSide, byte aStrength);

    /**
     * gets the Redstone Level the TileEntity should emit to the given Output Side
     */
    byte getStrongOutputRedstoneSignal(byte aSide);

    /**
     * sets the Redstone Level the TileEntity should emit to the given Output Side
     * <p/>
     * Do not use this if ICoverable is implemented. ICoverable has @getInternalOutputRedstoneSignal for Machine internal Output Redstone, so that it doesnt conflict with Cover Redstone.
     * This sets the true Redstone Output Signal. Only Cover Behaviors should use it, not MetaTileEntities.
     */
    void setStrongOutputRedstoneSignal(byte aSide, byte aStrength);

    /**
     * Gets the Output for the comparator on the given Side
     */
    byte getComparatorValue(byte aSide);

    /**
     * Get the redstone output signal strength for a given side
     */
    default byte getGeneralRS(byte aSide) {
        return 0;
    }
}
