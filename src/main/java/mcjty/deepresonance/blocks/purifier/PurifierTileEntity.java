package mcjty.deepresonance.blocks.purifier;

import elec332.core.world.WorldHelper;
import mcjty.deepresonance.blocks.base.ElecTileBase;
import mcjty.deepresonance.blocks.tank.ITankHook;
import mcjty.deepresonance.blocks.tank.TileTank;
import mcjty.deepresonance.config.ConfigMachines;
import mcjty.deepresonance.fluid.DRFluidRegistry;
import mcjty.deepresonance.fluid.LiquidCrystalFluidTagData;
import mcjty.deepresonance.items.ModItems;
import mcjty.deepresonance.varia.InventoryLocator;
import mcjty.lib.container.InventoryHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.ITickable;
import net.minecraftforge.common.util.Constants;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import java.util.Random;

public class PurifierTileEntity extends ElecTileBase implements ITankHook, ISidedInventory, ITickable {

    private InventoryHelper inventoryHelper = new InventoryHelper(this, PurifierContainer.factory, 1);

    public PurifierTileEntity() {
    }

    private TileTank bottomTank;
    private TileTank topTank;
    private int progress = 0;

    // Cache for the inventory used to put the spent filter material in.
    private InventoryLocator inventoryLocator = new InventoryLocator();

    private LiquidCrystalFluidTagData fluidData = null;

    private static Random random = new Random();

    @Override
    public void update() {
        if (!worldObj.isRemote){
            checkStateServer();
        }
    }

    protected void checkStateServer() {
        if (progress > 0) {
            progress--;
            if (progress == 0) {
                if (fluidData != null) {
                    // Done. First check if we can actually insert the liquid. If not we postpone this.
                    progress = 1;
                    if (getOutputTank() != null) {
                        if (testFillOutputTank() && validSlot()) {
                            if (random.nextInt(doPurify()) == 0) {
                                consumeFilter();
                            }
                            progress = 0;   // Really done
                        }
                    }
                }
            }
            markDirty();
        } else {
            if (canWork() && validSlot()) {
                progress = ConfigMachines.Purifier.ticksPerPurify;
                fluidData = LiquidCrystalFluidTagData.fromStack(getInputTank().drain(null, ConfigMachines.Purifier.rclPerPurify, true));
                markDirty();
            }
        }
    }

    private static EnumFacing[] directions = new EnumFacing[] { null, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.NORTH, EnumFacing.SOUTH };

    private void consumeFilter() {
        inventoryHelper.decrStackSize(PurifierContainer.SLOT_FILTERINPUT, 1);
        ItemStack spentMaterial = new ItemStack(ModItems.spentFilterMaterialItem, 1);
        inventoryLocator.ejectStack(worldObj, pos.getX(), pos.getY(), pos.getZ(), spentMaterial, pos, directions);
    }

    private int doPurify() {
        float purity = fluidData.getPurity();
        float maxPurityToAdd = ConfigMachines.Purifier.addedPurity / 100.0f;
        float addedPurity = maxPurityToAdd;
        float maxPurity = ConfigMachines.Purifier.maxPurity / 100.0f;
        maxPurity *= fluidData.getQuality();
        if (purity + addedPurity > maxPurity) {
            addedPurity = maxPurity - purity;
            if (addedPurity < 0.0001f) {
                // We are already very pure. Do nothing.
                // Put back the fluid we extracted.
                FluidStack stack = fluidData.makeLiquidCrystalStack();
                getOutputTank().fill(null, stack, true);
                fluidData = null;
                return 1000;
            }
        }

        purity += addedPurity;
        fluidData.setPurity(purity);
        FluidStack stack = fluidData.makeLiquidCrystalStack();
        getOutputTank().fill(null, stack, true);
        fluidData = null;
        return (int) ((maxPurityToAdd - addedPurity) * 1000 / maxPurityToAdd + 1);
    }

    private boolean testFillOutputTank() {
        return getOutputTank().fill(null, new FluidStack(DRFluidRegistry.liquidCrystal, ConfigMachines.Purifier.rclPerPurify), false) == ConfigMachines.Purifier.rclPerPurify;
    }

    private TileTank getInputTank() {
        if (topTank == null) {
            return bottomTank;
        }
        return topTank;
    }

    private TileTank getOutputTank() {
        if (bottomTank == null) {
            return topTank;
        }
        return bottomTank;
    }

    private boolean canWork() {
        if (bottomTank == null && topTank == null) {
            return false;
        }
        if (getInputTank().getFluidAmount() < ConfigMachines.Purifier.rclPerPurify) {
            return false;
        }
        // Same tank so operation is possible.
        return getInputTank().getMultiBlock().equals(getOutputTank().getMultiBlock()) || testFillOutputTank();
    }

    private boolean validSlot(){
        return inventoryHelper.getStackInSlot(PurifierContainer.SLOT_FILTERINPUT) != null && inventoryHelper.getStackInSlot(PurifierContainer.SLOT_FILTERINPUT).getItem() == ModItems.filterMaterialItem;
    }


    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("progress", progress);
        if (fluidData != null) {
            NBTTagCompound dataCompound = new NBTTagCompound();
            fluidData.writeDataToNBT(dataCompound);
            tagCompound.setTag("data", dataCompound);
            tagCompound.setInteger("amount", fluidData.getInternalTankAmount());
        }
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);

        writeBufferToNBT(tagCompound);
    }

    private void writeBufferToNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = new NBTTagList();
        for (int i = 0 ; i < inventoryHelper.getCount() ; i++) {
            ItemStack stack = inventoryHelper.getStackInSlot(i);
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            if (stack != null) {
                stack.writeToNBT(nbtTagCompound);
            }
            bufferTagList.appendTag(nbtTagCompound);
        }
        tagCompound.setTag("Items", bufferTagList);
    }


    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        progress = tagCompound.getInteger("progress");
        if (tagCompound.hasKey("data")) {
            NBTTagCompound dataCompound = (NBTTagCompound) tagCompound.getTag("data");
            int amount = dataCompound.getInteger("amount");
            fluidData = LiquidCrystalFluidTagData.fromNBT(dataCompound, amount);
        } else {
            fluidData = null;
        }
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        readBufferFromNBT(tagCompound);
    }

    private void readBufferFromNBT(NBTTagCompound tagCompound) {
        NBTTagList bufferTagList = tagCompound.getTagList("Items", Constants.NBT.TAG_COMPOUND);
        for (int i = 0 ; i < bufferTagList.tagCount() ; i++) {
            NBTTagCompound nbtTagCompound = bufferTagList.getCompoundTagAt(i);
            inventoryHelper.setStackInSlot(i, ItemStack.loadItemStackFromNBT(nbtTagCompound));
        }
    }


    @Override
    public void hook(TileTank tank, EnumFacing direction) {
        if (direction == EnumFacing.DOWN){
            if (validRCLTank(tank)) {
                bottomTank = tank;
            }
        } else if (topTank == null){
            if (validRCLTank(tank)){
                topTank = tank;
            }
        }
    }

    @Override
    public void unHook(TileTank tank, EnumFacing direction) {
        if (tilesEqual(bottomTank, tank)){
            bottomTank = null;
            notifyNeighboursOfDataChange();
        } else if (tilesEqual(topTank, tank)){
            topTank = null;
            notifyNeighboursOfDataChange();
        }
    }

    @Override
    public void onContentChanged(TileTank tank, EnumFacing direction) {
        if (tilesEqual(topTank, tank)){
            if (!validRCLTank(tank)) {
                topTank = null;
            }
        }
        if (tilesEqual(bottomTank, tank)){
            if (!validRCLTank(tank)) {
                bottomTank = null;
            }
        }
    }

    private boolean validRCLTank(TileTank tank){
        Fluid fluid = DRFluidRegistry.getFluidFromStack(tank.getFluid());
        return fluid == null || fluid == DRFluidRegistry.liquidCrystal;
    }

    private boolean tilesEqual(TileTank first, TileTank second){
        return first != null && second != null && first.getPos().equals(second.getPos()) && WorldHelper.getDimID(first.getWorld()) == WorldHelper.getDimID(second.getWorld());
    }

    @Override
    public int[] getSlotsForFace(EnumFacing side) {
        return new int[] { PurifierContainer.SLOT_FILTERINPUT };
    }

    @Override
    public boolean canInsertItem(int index, ItemStack item, EnumFacing side) {
        return PurifierContainer.factory.isInputSlot(index) || PurifierContainer.factory.isSpecificItemSlot(index);
    }

    @Override
    public boolean canExtractItem(int index, ItemStack item, EnumFacing side) {
        return PurifierContainer.factory.isOutputSlot(index);
    }

    @Override
    public int getSizeInventory() {
        return inventoryHelper.getCount();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventoryHelper.getStackInSlot(index);
    }

    @Override
    public ItemStack decrStackSize(int index, int amount) {
        return inventoryHelper.decrStackSize(index, amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int index) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int index, ItemStack stack) {
        inventoryHelper.setInventorySlotContents(getInventoryStackLimit(), index, stack);
    }

    @Override
    public String getName() {
        return "Purifier Inventory";
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    /**
     * Get the formatted ChatComponent that will be used for the sender's username in chat
     */
    @Override
    public IChatComponent getDisplayName() {
        return null;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return canPlayerAccess(player);
    }

    @Override
    public void openInventory(EntityPlayer player) {

    }

    @Override
    public void closeInventory(EntityPlayer player) {

    }

    @Override
    public boolean isItemValidForSlot(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {

    }

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {

    }
}
