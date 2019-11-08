package com.tiviacz.travellersbackpack.fluids.effects;

import com.tiviacz.travellersbackpack.api.FluidEffect;
import com.tiviacz.travellersbackpack.fluids.FluidEffectRegistry;
import com.tiviacz.travellersbackpack.init.ModFluids;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.init.MobEffects;

public class EffectMelonJuice extends FluidEffect
{
    public EffectMelonJuice()
    {
        super(FluidRegistry.getFluid("melonjuice"));
    }
    
    
    @Override
    public void affectDrinker(FluidStack fluidStack, World world, Entity entity)
    {
        if(entity instanceof EntityPlayer)
        {
        	System.out.println("triggered");
            ((EntityPlayer)entity).clearActivePotions();
            EntityPlayer player = (EntityPlayer) entity;
            player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 600, 0));
            FluidEffectRegistry.WATER_EFFECT.affectDrinker(new FluidStack(ModFluids.MELONJUICE, 5), world, player);
        }
    }
}
