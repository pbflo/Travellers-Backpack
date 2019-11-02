package com.tiviacz.travellersbackpack.gui.container.slots;

import com.tiviacz.travellersbackpack.gui.inventory.IInventoryTanks;
import com.tiviacz.travellersbackpack.init.ModFluids;
import com.tiviacz.travellersbackpack.init.ModItems;
import com.tiviacz.travellersbackpack.items.ItemMelonJuiceBottle;
import com.tiviacz.travellersbackpack.util.FluidUtils;
import com.tiviacz.travellersbackpack.util.Reference;

import net.minecraft.init.Items;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class SlotFluid extends Slot
{
	private int index;
	
	public SlotFluid(IInventory inventoryIn, int index, int xPosition, int yPosition)
	{
		super(inventoryIn, index, xPosition, yPosition);
		
		this.index = index;
	}

	@Override
	public boolean isItemValid(ItemStack stack)
    {
		IFluidHandlerItem container = FluidUtil.getFluidHandler(stack);
		
		if(index == Reference.BUCKET_OUT_LEFT || index == Reference.BUCKET_OUT_RIGHT)
		{
			return false;
		}
		
		if(stack.getItem() == Items.POTIONITEM || stack.getItem() == Items.GLASS_BOTTLE)
		{
			return true;
		}

		if(stack.getItem() == ModItems.MELONJUICEBOTTLE || stack.getItem() == Items.GLASS_BOTTLE)
		{
			return true;
		}

		
		return container != null ? true : false;
    }
	
	public static boolean isValid(ItemStack stack)
	{
		IFluidHandlerItem container = FluidUtil.getFluidHandler(stack);
		
		if(stack.getItem() == Items.POTIONITEM || stack.getItem() == Items.GLASS_BOTTLE)
		{
			return true;
		}

		if(stack.getItem() == ModItems.MELONJUICEBOTTLE || stack.getItem() == Items.GLASS_BOTTLE)
		{
			return true;
		}

		
		return container != null ? true : false;
	}
	
	public static boolean checkFluid(ItemStack stack, FluidTank leftTank, FluidTank rightTank)
	{
		if(stack.getItem() instanceof ItemPotion || stack.getItem() instanceof ItemMelonJuiceBottle)
		{
			FluidStack fluidStack;
			if (stack.getItem() instanceof ItemPotion) {
				fluidStack = new FluidStack(ModFluids.POTION, 250);
				FluidUtils.setFluidStackNBT(stack, fluidStack);
			} else {
				fluidStack = new FluidStack(ModFluids.MELONJUICE, 250);
			}
			
			if(leftTank.getFluid() != null || leftTank.getFluidAmount() != 0)
			{
				if(leftTank.getFluid().isFluidEqual(fluidStack))
				{
					if(leftTank.getFluidAmount() == leftTank.getCapacity())
					{
						return false;
					}
					else
					{
						return true;
					}
				}
			}
			else
			{
				if(rightTank.getFluid() != null || rightTank.getFluidAmount() != 0)
				{
					if(!rightTank.getFluid().isFluidEqual(fluidStack) || rightTank.getFluidAmount() == rightTank.getCapacity())
					{
						return true;
					}
					else
					{
						return false;
					}
				}
				else
				{
					return true;
				}
			}
		}
		
		if(stack.getItem() == Items.GLASS_BOTTLE)
		{
			if(leftTank.getFluid() != null && leftTank.getFluidAmount() > 0)
			{
				return true;
			}
			
			if(leftTank.getFluid() == null && rightTank.getFluid() == null)
			{
				return true;
			}
			return false;
		}	
		else
		{
			IFluidHandlerItem container = FluidUtil.getFluidHandler(stack);
			
			if(container != null)
			{
				if(leftTank.getFluid() != null || leftTank.getFluidAmount() != 0)
				{
					if(leftTank.getFluid().isFluidEqual(container.getTankProperties()[0].getContents()))
					{
						if(leftTank.getFluidAmount() == leftTank.getCapacity())
						{
							return false;
						}
						else
						{
							return true;
						}
					}
				}
				else
				{
					if(rightTank.getFluid() != null || rightTank.getFluidAmount() != 0)
					{
						if(container.getTankProperties()[0].getContents() == null)
						{
							return false;
						}
						
						else if(!rightTank.getFluid().isFluidEqual(container.getTankProperties()[0].getContents()) || rightTank.getFluidAmount() == rightTank.getCapacity())
						{
							return true;
						}
						else
						{
							return false;
						}
					}
					else
					{
						return true;
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public void onSlotChanged()
    {
        if(inventory instanceof IInventoryTanks)
        {
        	((IInventoryTanks)inventory).updateTankSlots();
        }
        
        super.onSlotChanged();
    }
}