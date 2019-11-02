package com.tiviacz.travellersbackpack.blocks;

import java.util.Random;

import com.tiviacz.travellersbackpack.TravellersBackpack;
import com.tiviacz.travellersbackpack.capability.CapabilityUtils;
import com.tiviacz.travellersbackpack.handlers.ConfigHandler;
import com.tiviacz.travellersbackpack.init.ModBlocks;
import com.tiviacz.travellersbackpack.init.ModItems;
import com.tiviacz.travellersbackpack.tileentity.TileEntityTravellersBackpack;
import com.tiviacz.travellersbackpack.util.BackpackUtils;
import com.tiviacz.travellersbackpack.util.Bounds;
import com.tiviacz.travellersbackpack.util.Reference;

import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.block.Block;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import com.tiviacz.travellersbackpack.init.ModFluids;
import net.minecraft.block.Block;

public class BlockTravellersBackpack extends BlockContainer
{
	public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
	public static final AxisAlignedBB BACKPACK_NORTH_AABB = new Bounds(1, 0, 4, 15, 10, 12).toAABB();
	public static final AxisAlignedBB BACKPACK_SOUTH_AABB = new Bounds(1, 0, 4, 15, 10, 12).toAABB();
	public static final AxisAlignedBB BACKPACK_EAST_AABB = new Bounds(4, 0, 1, 12, 10, 15).toAABB();
	public static final AxisAlignedBB BACKPACK_WEST_AABB = new Bounds(4, 0, 1, 12, 10, 15).toAABB();
	
	public BlockTravellersBackpack(String name, Material materialIn) 
	{
		super(materialIn);
		
		setRegistryName(name);
		setUnlocalizedName(name);
		setSoundType(SoundType.CLOTH);
		setHardness(1.0F);
		setResistance(100000.0F);
		setHarvestLevel("hand", 0);
		setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH)); 
		
		ModBlocks.BLOCKS.add(this);
	}
	
	@Override
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
	{
		if(worldIn.getTileEntity(pos) instanceof TileEntityTravellersBackpack)
		{
			if(!worldIn.isRemote)
			{
				if(ConfigHandler.enableBackpackBlockWearable)
				{
					if(playerIn.isSneaking())
					{
						TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)worldIn.getTileEntity(pos);
						
						if(!CapabilityUtils.isWearingBackpack(playerIn))
						{
							if(worldIn.setBlockToAir(pos))
							{
								ItemStack stack = new ItemStack(ModItems.TRAVELLERS_BACKPACK, 1, BackpackUtils.convertNameToMeta(te.getColor()));
						        te.transferToItemStack(stack);
						        CapabilityUtils.equipBackpack(playerIn, stack);
						        
						        if(te.isSleepingBagDeployed())
					        	{
					        		EnumFacing bagFacing = state.getValue(FACING);
					        		worldIn.setBlockToAir(pos.offset(bagFacing));
					        		worldIn.setBlockToAir(pos.offset(bagFacing).offset(bagFacing));
					        	}
							}
							else
							{
								playerIn.sendMessage(new TextComponentTranslation("actions.equip_backpack.fail"));
							}
						}
						else
						{
							playerIn.sendMessage(new TextComponentTranslation("actions.equip_backpack.otherbackpack"));
						}
					}
					else
					{
						playerIn.openGui(TravellersBackpack.INSTANCE, Reference.TRAVELLERS_BACKPACK_TILE_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
					}
				}
				else
				{
					playerIn.openGui(TravellersBackpack.INSTANCE, Reference.TRAVELLERS_BACKPACK_TILE_GUI_ID, worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player, boolean willHarvest)
    {
		TileEntity tile = world.getTileEntity(pos);

        if(tile instanceof TileEntityTravellersBackpack && !world.isRemote && player != null)
        {
        	TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)tile;
        	te.drop(world, player, pos.getX(), pos.getY(), pos.getZ());
        	world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
        	
        	if(te.isSleepingBagDeployed())
        	{
        		EnumFacing facing = state.getValue(FACING);
        		world.setBlockToAir(pos.offset(facing));
        		world.setBlockToAir(pos.offset(facing).offset(facing));
        	}
        } 
        else
        {
        	world.setBlockState(pos, Blocks.AIR.getDefaultState(), world.isRemote ? 11 : 3);
        }
        return false;
    }
	 
	@Override
	public boolean isFullCube(IBlockState state)
	{
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state)
	{
		return false;
	} 
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos)
    {
		switch(state.getValue(FACING))
        {
            case NORTH:
                return BACKPACK_NORTH_AABB;
            case SOUTH:
                return BACKPACK_SOUTH_AABB;
            case EAST:
                return BACKPACK_EAST_AABB;
            case WEST:
                return BACKPACK_WEST_AABB;
		default:
			return BACKPACK_NORTH_AABB;
        }
    }
	
	@Override
	@SideOnly(Side.CLIENT)
    public BlockRenderLayer getBlockLayer()
    {
        return BlockRenderLayer.CUTOUT;
    }
	
	@Override
	public EnumBlockRenderType getRenderType(IBlockState state)
    {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }
	
	@Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face)
    {
        return BlockFaceShape.UNDEFINED;
    }
	
	public EnumFacing getFacing(IBlockState state)
	{
		return state.getValue(FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) 
	{
	    EnumFacing facing = EnumFacing.getFront(meta);

	    if(facing.getAxis() == EnumFacing.Axis.Y) 
	    {
	    	facing = EnumFacing.NORTH;
	    }
	    return getDefaultState().withProperty(FACING, facing);
	}

	@Override
	public int getMetaFromState(IBlockState state) 
	{
	    return state.getValue(FACING).getIndex();
	}
	    
	@Override
	protected BlockStateContainer createBlockState() 
	{
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}
	
	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) 
	{
	    return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
	}
	
	@Override
	public int quantityDropped(Random random)
    {
        return 1;
    }
	
	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player)
    {
		ItemStack stack = new ItemStack(ModItems.TRAVELLERS_BACKPACK, 1);
		
		if(world.getTileEntity(target.getBlockPos()) instanceof TileEntityTravellersBackpack)
		{
			TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)world.getTileEntity(target.getBlockPos());
			stack = new ItemStack(ModItems.TRAVELLERS_BACKPACK, 1, BackpackUtils.convertNameToMeta(te.getColor()));
			te.transferToItemStack(stack);
		}
        return stack;
    }
	
	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune)
    {
        return ModItems.TRAVELLERS_BACKPACK;
    }

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) 
	{
		return new TileEntityTravellersBackpack();
	}
	
	//Block Abilities
	//Redstone 
	@Override
	public int getWeakPower(IBlockState state, IBlockAccess baccess, BlockPos pos, EnumFacing side) 
	{
		TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)baccess.getTileEntity(pos);
		String color = te.getColor();
		if(color.equals("Redstone")) {
			return 15;
		} else {
			return 0;
		}
	}

    @Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) 
	{
		TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)world.getTileEntity(pos);
		String color = te.getColor();
		if(color.equals("Redstone")) {
			return true;
		} else {
			return false;
		}
	}
	
	//Cactus, Melon
	@Override
	public void fillWithRain(World worldIn, BlockPos pos) 
	{
		TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)worldIn.getTileEntity(pos);
		String color = te.getColor();	
		if (color.equals("Cactus") || color.equals("Melon")) {
    		Chunk chunk = worldIn.getChunkFromBlockCoords(pos);
    		
    		if (chunk.canSeeSky(pos))
    		{ 	

				FluidTank lTank = te.getLeftTank();
				FluidTank rTank = te.getRightTank();
				Integer amount = 40;
				FluidStack fluidS;

				if (color.equals("Cactus")) {
					fluidS = new FluidStack(FluidRegistry.WATER, amount);				  	
				} else {
					fluidS = new FluidStack(ModFluids.MELONJUICE, amount);
				}
					
				lTank.fill(fluidS, true);				
				rTank.fill(fluidS, true);
        	}
		}
    }

	//Cactus
    @Override
	public void onEntityCollidedWithBlock(World worldIn, BlockPos pos, IBlockState state, Entity entityIn)
    {
        TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)worldIn.getTileEntity(pos);
		String color = te.getColor();
		if(color.equals("Cactus"))
        {
         	entityIn.attackEntityFrom(DamageSource.CACTUS, 0.75F);
        }
    }
	
	//Bookshelf
	@Override
	public float getEnchantPowerBonus(World world, BlockPos pos) 
	{
		TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)world.getTileEntity(pos);
		String color = te.getColor();
		if(color.equals("Bookshelf")) {
			return 10f;
		} else {
			return 0f;
		}
	}
	
	//Glowstone
    /*@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand)
    {
    	System.out.println("triggered glow!");
		TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)worldIn.getTileEntity(pos);
		Block b = te.getBlockType();
		String color = te.getColor();
		if(color.equals("Glowstone")) {
			b.setLightLevel(1f);
		} else {
			b.setLightLevel(0f);
		}
	}*/

	@Override
	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntityTravellersBackpack te = (TileEntityTravellersBackpack)world.getTileEntity(pos);
		String color = te.getColor();
		if(color.equals("Glowstone")) {
			System.out.println("Glow! ("+pos.getX()+"|"+pos.getY()+"|"+pos.getZ()+")");
			return 14;
		} else {
			System.out.println("No Glow! ("+pos.getX()+"|"+pos.getY()+"|"+pos.getZ()+")");
			return 0;
		}
	}

}
