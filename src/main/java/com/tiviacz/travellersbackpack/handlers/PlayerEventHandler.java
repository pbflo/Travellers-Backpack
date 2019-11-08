package com.tiviacz.travellersbackpack.handlers;

import com.tiviacz.travellersbackpack.TravellersBackpack;
import com.tiviacz.travellersbackpack.blocks.BlockSleepingBag;
import com.tiviacz.travellersbackpack.capability.BackpackProvider;
import com.tiviacz.travellersbackpack.capability.CapabilityUtils;
import com.tiviacz.travellersbackpack.capability.IBackpack;
import com.tiviacz.travellersbackpack.common.ServerActions;
import com.tiviacz.travellersbackpack.gui.container.ContainerTravellersBackpack;
import com.tiviacz.travellersbackpack.gui.inventory.InventoryTravellersBackpack;
import com.tiviacz.travellersbackpack.network.client.SyncBackpackCapability;
import com.tiviacz.travellersbackpack.network.client.SyncBackpackCapabilityMP;
import com.tiviacz.travellersbackpack.util.BackpackUtils;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerSetSpawnEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import com.tiviacz.travellersbackpack.tileentity.TileEntityTravellersBackpack;
import com.tiviacz.travellersbackpack.util.Reference;
import com.tiviacz.travellersbackpack.handlers.ConfigHandler;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import java.util.Random;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.world.chunk.Chunk;
import com.tiviacz.travellersbackpack.init.ModFluids;
import net.minecraft.block.material.MaterialLiquid;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemEgg;
import net.minecraft.util.math.Vec3d;
import net.minecraft.init.Items;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.Fluid;
import com.tiviacz.travellersbackpack.handlers.ClientEventHandler;

@EventBusSubscriber(modid = TravellersBackpack.MODID)
public class PlayerEventHandler 
{
	public static final ResourceLocation BACKPACK_CAP = new ResourceLocation(TravellersBackpack.MODID, "travellers_backpack");
	
	@SubscribeEvent
    public static void attachCapability(AttachCapabilitiesEvent<Entity> event)
    {
        if(!(event.getObject() instanceof EntityPlayer)) 
        {
        	return;
        }

        event.addCapability(BACKPACK_CAP, new BackpackProvider((EntityPlayer)event.getObject()));
    }
	
	@SubscribeEvent
	public static void onPlayerStruckByLightning(EntityStruckByLightningEvent event)
	{
		if(event.getEntity() instanceof EntityPlayer)
		{
			ServerActions.electrify((EntityPlayer)event.getEntity());
		}
	}
	
	@SubscribeEvent
    public static void onPlayerSetSpawn(PlayerSetSpawnEvent event) 
	{
        EntityPlayer player = event.getEntityPlayer();
        World world = player.getEntityWorld();
        BlockPos pos = event.getNewSpawn();

        if(pos != null) 
        {
            Block block = world.getBlockState(pos).getBlock();

            if(!world.isRemote && (block instanceof BlockSleepingBag)) 
            {
                event.setCanceled(true);
            }
        }
    }
	
	@SubscribeEvent
    public static void onPlayerJoin(EntityJoinWorldEvent event)
    {
		if(!event.getWorld().isRemote)
		{
			if(event.getEntity() instanceof EntityPlayerMP)
			{
				EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
				
				NBTTagCompound playerData = ServerActions.extractPlayerProps(player.getUniqueID());

				if(playerData != null)
				{
					IBackpack cap = CapabilityUtils.getCapability(player);
					cap.setWearable(new ItemStack(playerData));
					
					//Sync
					TravellersBackpack.NETWORK.sendTo(new SyncBackpackCapability(new ItemStack(playerData).writeToNBT(new NBTTagCompound())), player);
				}
			}
		}
    }
	
	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event)
    {
		if(event.getEntity() instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.getEntity();
			
			if(!player.world.isRemote)
			{
				if(CapabilityUtils.isWearingBackpack(player))
				{
					if(!player.getEntityWorld().getGameRules().getBoolean("keepInventory"))
					{
						BackpackUtils.onPlayerDeath(player.world, player, CapabilityUtils.getWearingBackpack(player));
					}
					else
					{
						ServerActions.storePlayerProps(player);
					}
				}
			}
		}
    }
	
	@SubscribeEvent
	public static void onPlayerLogsIn(PlayerEvent.PlayerLoggedInEvent event)
	{
		if(event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;
			
			if(CapabilityUtils.isWearingBackpack(player))
			{
				//Sync
				TravellersBackpack.NETWORK.sendTo(new SyncBackpackCapability(CapabilityUtils.getWearingBackpack(player).writeToNBT(new NBTTagCompound())), player);
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event)
	{
		if(event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;
			
			if(event.isEndConquered())
			{
				NBTTagCompound playerData = ServerActions.getBackpackMap().remove(player.getUniqueID());
				
				if(playerData != null)
				{
					IBackpack cap = CapabilityUtils.getCapability(player);
					cap.setWearable(new ItemStack(playerData));
					
					//Sync
					TravellersBackpack.NETWORK.sendTo(new SyncBackpackCapability(new ItemStack(playerData).writeToNBT(new NBTTagCompound())), player);
				}
			}
		}
	}
	
	@SubscribeEvent
	public static void onPlayerInEnd(LivingEvent event)
	{
		if(event.getEntityLiving() instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.getEntityLiving();
			
			if(player.dimension == 1)
			{
				if(CapabilityUtils.isWearingBackpack(player))
				{
					if(ServerActions.getBackpackMap().isEmpty())
					{
						ServerActions.storeBackpackProps(player);
					}
					
					InventoryTravellersBackpack inv = CapabilityUtils.getBackpackInv(player);
					
					if(!inv.hasTileEntity())
					{
						if(player.openContainer instanceof ContainerTravellersBackpack)
						{
							ServerActions.storeBackpackProps(player);
						}
					}
				}
				else
				{
					if(!ServerActions.getBackpackMap().isEmpty())
					{
						ServerActions.getBackpackMap().clear();
					}
				}
			}
		}
	}
	
	@SubscribeEvent
    public static void onPlayerTravelsAcrossDimensions(PlayerEvent.PlayerChangedDimensionEvent event)
    {
		if(event.player instanceof EntityPlayerMP)
		{
			EntityPlayerMP player = (EntityPlayerMP)event.player;

			if(CapabilityUtils.isWearingBackpack(player))
			{
				//Sync
				TravellersBackpack.NETWORK.sendTo(new SyncBackpackCapability(CapabilityUtils.getWearingBackpack(player).writeToNBT(new NBTTagCompound())), (EntityPlayerMP)player);
			}
		}
    }
	
	@SubscribeEvent
	public static void onPlayerTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event)
	{
		if(event.getTarget() instanceof EntityPlayer)
		{
			EntityPlayer target = (EntityPlayer)event.getTarget();
			
			if(CapabilityUtils.isWearingBackpack(target))
			{
				TravellersBackpack.NETWORK.sendToAllTracking(new SyncBackpackCapabilityMP(CapabilityUtils.getWearingBackpack(target).writeToNBT(new NBTTagCompound()), target.getEntityId()), target);
			}
			else
			{
				TravellersBackpack.NETWORK.sendToAllTracking(new SyncBackpackCapabilityMP(ItemStack.EMPTY.writeToNBT(new NBTTagCompound()), target.getEntityId()), target);
			}
		}
	}

	//Wearable Abilities
	
	@SubscribeEvent
	public static void playerTick(TickEvent.PlayerTickEvent event)
	{	
		if(ConfigHandler.enableWornAbilities)
        {
			EntityPlayer player = (EntityPlayer)event.player;
			Random ran;
			int rand;

			if (CapabilityUtils.isWearingBackpack(player))
			{
				ItemStack tb = CapabilityUtils.getWearingBackpack(player);
				int colnr = tb.getMetadata();
				String color = Reference.BACKPACK_NAMES[colnr];
				World world = player.world;
				
				if (ClientEventHandler.ACTIVATED_ABILITIES || ExcludedDA(color)) {
					if (color.equals("Bat")  && !ExcludedAB("Bat")) { //Bat
						player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 5, 0, false, false));
					} else if (color.equals("Squid") && !ExcludedAB("Squid")) { //Squid
						player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 5, 0, false, false));
					} else if (color.equals("Pigman") && !ExcludedAB("Pigman")) { //Pigman
						player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 5, 0, false, false));
						if (player.isBurning()) {
							player.extinguish();
						}
					} else if (color.equals("Pig") && !ExcludedAB("Pig")) { //Pig
						ran = new Random();
						rand = ran.nextInt(2000);
						if (rand == 5) {
							player.playSound(SoundEvents.ENTITY_PIG_AMBIENT, 1f, 1f);
						}
					} else if (color.equals("Cactus") || color.equals("Melon")) { //Cactus / Melon
						if (!ExcludedAB("Cactus") || !ExcludedAB("Melon")) {
				
							Chunk chunk = world.getChunkFromBlockCoords(player.getPosition());
							FluidStack fluidS;
					
							FluidTank rightT = new FluidTank(Reference.BASIC_TANK_CAPACITY);
							FluidTank leftT = new FluidTank(Reference.BASIC_TANK_CAPACITY);
							NBTTagCompound complete = tb.getTagCompound();
							NBTTagCompound rTank = complete.getCompoundTag("RightTank");
							NBTTagCompound lTank = complete.getCompoundTag("LeftTank");
							Fluid rFluid =  FluidRegistry.getFluid(rTank.getString("FluidName"));
							Fluid lFluid =  FluidRegistry.getFluid(lTank.getString("FluidName"));
							int rAmount =  rTank.getInteger("Amount");
							int lAmount =  lTank.getInteger("Amount");

							if (rFluid!=null && lFluid!=null) {
								fluidS = new FluidStack(rFluid,rAmount);
					 		   	rightT.fill(fluidS, true);
					 		   	fluidS = new FluidStack(lFluid,lAmount);
								leftT.fill(fluidS, true);					
							}
	
							if (color.equals("Cactus")) {
								fluidS = new FluidStack(FluidRegistry.WATER, 7);				  	
							} else {
								fluidS = new FluidStack(ModFluids.MELONJUICE, 7);
							}
					
					
    						
							if (world.isRaining() && chunk.canSeeSky(player.getPosition()) && !world.isRemote) { //Rain
								ran = new Random();
								rand = ran.nextInt(20);
								if (rand == 5) {
									if (rFluid==null) rightT.setFluid(fluidS);
									if (lFluid==null) leftT.setFluid(fluidS);
									rightT.fill(fluidS, true);
									leftT.fill(fluidS, true);

									complete.getCompoundTag("RightTank").setInteger("Amount",rightT.getFluidAmount());
									complete.getCompoundTag("LeftTank").setInteger("Amount",leftT.getFluidAmount());
							
									tb.setTagCompound(complete);
									System.out.println("rain: "+rightT.getFluidAmount()+";"+rightT.getFluid().getFluid().getName()+ " | "+leftT.getFluidAmount()+";"+leftT.getFluid().getFluid().getName());
									//Update Interface -> currently only when taken off (sometimes)
								}
							} else if (world.getBlockState(player.getPosition()).getBlock().getMaterial(world.getBlockState(player.getPosition())) == MaterialLiquid.WATER) { //In Water
								ran = new Random();
								rand = ran.nextInt(20);
								if (rand == 5) {
									if (rFluid==null) rightT.setFluid(fluidS);
									if (lFluid==null) leftT.setFluid(fluidS);					
									rightT.fill(fluidS, true);
									leftT.fill(fluidS, true);

									complete.getCompoundTag("RightTank").setInteger("Amount",rightT.getFluidAmount());
									complete.getCompoundTag("LeftTank").setInteger("Amount",leftT.getFluidAmount());
	
									tb.setTagCompound(complete);
									System.out.println("water: "+rightT.getFluidAmount()+";"+rightT.getFluid().getFluid().getName()+ " | "+leftT.getFluidAmount()+";"+leftT.getFluid().getFluid().getName());
									//Update Interface -> currently only when taken off
								}
							}
						}
					} else if (color.equals("Chicken") && !ExcludedAB("Chicken")) { //Chicken
						//if (this.timeUntilNextEgg<=0) {
							ran = new Random();
							rand = ran.nextInt(6000);
							if (rand == 6) {
								Vec3d look = player.getLookVec();
								EntityItem item = new EntityItem(world, player.posX-(look.x * 0.8), player.posY+0.65, player.posZ-(look.z * 0.8));
								ItemStack itemstack = new ItemStack(Items.EGG, 1);
								item.setOwner(player.getName());
								item.setItem(itemstack);
								item.setPickupDelay(8);
								item.motionY=0.001;
								item.motionX=look.x*0.3;
								item.motionZ=look.z*0.3;

								world.spawnEntity(item);
								player.playSound(SoundEvents.ENTITY_CHICKEN_EGG, 1.0F, 1f);
						  	  //timeUntilNextEgg = ran.nextInt(6000) + 6000;
							}
						/*} else {
							this.timeUntilNextEgg--;
						}*/
					} 
				}
			}
        }
	}

	public static boolean ExcludedAB(String color) {
		for (String inside : ConfigHandler.bannedAbilities) {
			if (inside.equals(color))
			return true;
		}
		return false;
	}

	public static boolean ExcludedDA(String color) {
		for (String inside : ConfigHandler.undeactivateableAbilities) {
			if (inside.equals(color))
			return true;
		}
		return false;
	}

}