package com.tiviacz.travellersbackpack.handlers;

import com.tiviacz.travellersbackpack.TravellersBackpack;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.Config.Comment;

@Config(modid = TravellersBackpack.MODID)
public class ConfigHandler 
{
	@Comment("Places backpack at place where player died")
	public static boolean backpackDeathPlace = true;
	
	@Comment("Enables tool cycling via shift + scroll combination, while backpack is worn")
	public static boolean enableToolCycling = true;
	
	public static boolean toolSlotsAcceptSwords = false;
	
	@Comment("Render tools in tool slots on the backpack, while worn")
	public static boolean renderTools = true;
	
	@Comment("Enables tanks and tool slots overlay, while backpack is worn")
	public static boolean enableOverlay = true;
	
	public static boolean oldGuiTankRender = false;
	
	@Comment("Enables wearing backpack directly from ground")
	public static boolean enableBackpackBlockWearable = true;
	
	@Comment("Enables backpacks spawning in loot chests")
	public static boolean enableLoot = true;
	
	@Comment("Disabling this option may improve performance")
	public static boolean enableBackpackItemFluidRenderer = true;
	
	@Comment("Enables tip, how to obtain a backpack, if there's no crafting recipe for it")
	public static boolean obtainTips = true;
	
//	@Comment("Backpack will not drop after player death to avoid place conflicts")
//	public static boolean keepBackpack = true;
	
	@Comment("Enables button in backpack gui, which allows to empty tank")
	public static boolean enableEmptyTankButton = true;

	@Comment("Enables the abilities of the backpack when placed on the ground")
	public static boolean enableBlockAbilities = true;

	@Comment("Enables the abilities of the backpack when worn")
	public static boolean enableWornAbilities = true;

	@Comment("Enables the keybinding for turning of the abilities when worn")
	public static boolean enableAbilityActivation = true;

	@Comment({"A list of all banned Abilities. No ability in this list will be used.","(Name of the Backpack with the effect; Block and Worn Abilities possible)"})
	public static String[] bannedAbilities = {"Redstone","Cactus"};

	@Comment({"A list of all undeactivateable Abilities. No ability in this list can be deactivated with the Key.","(Name of the Backpack with the effect; Only Worn Abilities possible)"})
	public static String[] undeactivateableAbilities = {"Slime"};
}