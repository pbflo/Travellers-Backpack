package com.tiviacz.travellersbackpack.items;

import java.util.List;

import javax.annotation.Nullable;

import com.tiviacz.travellersbackpack.TravellersBackpack;
import com.tiviacz.travellersbackpack.init.ModItems;
import com.tiviacz.travellersbackpack.init.ModFluids;
import com.tiviacz.travellersbackpack.util.IHasModel;
import com.tiviacz.travellersbackpack.fluids.effects.EffectMelonJuice;
import com.tiviacz.travellersbackpack.fluids.FluidEffectRegistry;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemFood;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.item.EnumAction;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemMelonJuiceBottle extends ItemFood implements IHasModel
{
	public ItemMelonJuiceBottle(String name, int amount, float saturation, boolean isWolfFood) {
		super(amount, saturation, isWolfFood);
		setUnlocalizedName(name);
		setRegistryName(name);
		setMaxStackSize(16);

		ModItems.ITEMS.add(this);
		setAlwaysEdible();
	}

	@Override
	public void registerModels() 
	{
		TravellersBackpack.proxy.registerItemRenderer(this, 0, "inventory");
	}

	@Override
	protected void onFoodEaten(ItemStack stack, World worldIn, EntityPlayer player) {
		if(!worldIn.isRemote) {
			FluidEffectRegistry.MELON_EFFECT.affectDrinker(new FluidStack(ModFluids.MELONJUICE, 5), worldIn, player);
		}
	}

	@Override
	public EnumAction getItemUseAction(ItemStack stack)
    {
        return EnumAction.DRINK;
    }

    @Override
	@SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn)
    {
		tooltip.add(TextFormatting.BLUE + I18n.format("melonjuice.bottle.obtain"));
		tooltip.add(TextFormatting.GOLD + I18n.format("melonjuice.bottle.effect"));
    }

}