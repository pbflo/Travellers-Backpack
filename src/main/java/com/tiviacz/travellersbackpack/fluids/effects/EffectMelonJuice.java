package com.tiviacz.travellersbackpack.fluids.effects;

import com.tiviacz.travellersbackpack.api.FluidEffect;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class EffectMelonJuice extends FluidEffect
{
    public EffectMelonJuice()
    {
        super(FluidRegistry.getFluid("melonjuice"), 30);
    }
    
    
    @Override
    public void affectDrinker(FluidStack fluidStack, World world, Entity entity)
    {
        if(entity instanceof EntityPlayer)
        {
            ((EntityPlayer)entity).clearActivePotions();
            EntityPlayer player = (EntityPlayer) entity;
            player.addPotionEffect(new PotionEffect(MobEffects.HASTE, timeInTicks, 0));
            FluidEffectRegistry.WATER_EFFECT.affectDrinker(world, player);
        }
    }
}
