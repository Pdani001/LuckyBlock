package tk.Pdani.LuckyBlock.event;

import tk.Pdani.LuckyBlock.Main;
import tk.Pdani.LuckyBlock.utils.Randomizer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.techguard.izone.Managers.ZoneManager;
import net.techguard.izone.Zones.Flags;
import net.techguard.izone.Zones.Zone;

public class BlockBreak implements Listener {
	private Main plugin;
	Randomizer random;
	String noBreakPermMsg = "";
	String inventoryFull = "";
	String regionError = "";
	boolean useWguard = false;
	boolean useIzone = false;
	ZoneManager zm = null;
	
	//<--CONFIG "DEFAULT" SETTINGS
	boolean silktouch = true;
	boolean worldsWhitelist = true;
	List<String> worlds = new ArrayList<String>();
	//-->
	public BlockBreak(tk.Pdani.LuckyBlock.Main main){
		this.plugin = main;
		this.random = new Randomizer(this.plugin);
		this.noBreakPermMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.noBreakPerm"));
		this.inventoryFull = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.inventoryFull"));
		this.regionError = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.regionError"));
		this.worlds = plugin.getConfig().getStringList("settings.worlds");
		this.worldsWhitelist = plugin.getConfig().getBoolean("settings.worldsWhitelist");
		this.silktouch = plugin.getConfig().getBoolean("settings.silktouch");
		if(plugin.getZmod()){
			this.useIzone = true;
			this.zm = new ZoneManager();
		}
		if(plugin.getWorldGuard() != null){
			this.useWguard = true;
		}
	}
	
	private final BlockFace[] surrounding = new BlockFace[]{BlockFace.NORTH, 
            BlockFace.NORTH_EAST,
            BlockFace.EAST,
            BlockFace.SOUTH_EAST,
            BlockFace.SOUTH,
            BlockFace.SOUTH_WEST,
            BlockFace.WEST,
            BlockFace.NORTH_WEST};
	public void trapPlayer(Player player, String surround, boolean above)
	{
		Location[] locs = new Location[]{player.getLocation(),
		player.getLocation().add(0,1,0),
		player.getLocation().add(0,2,0)};
		Material mat = Material.getMaterial(surround.toUpperCase());
		if(mat == null){
			mat = Material.GLASS;
		}
		for(Location loc : locs)
		{
			for(BlockFace bf : surrounding)
			{
				loc.getBlock().getRelative(bf, 1).setType(mat);
			}
		}
		// Also block above the player
		if(above)player.getLocation().add(0,2,0).getBlock().setType(mat);
	}
	
	public boolean checkZmod(Player player)
	{
		if(!plugin.getZmod()){
			return false;
		}
		Location[] locs = new Location[]{player.getLocation(),
		player.getLocation().add(0,1,0),
		player.getLocation().add(0,2,0)};
		boolean found = false;
		for(Location loc : locs)
		{
			for(BlockFace bf : surrounding)
			{
				Location rl = loc.getBlock().getRelative(bf, 1).getLocation();
				Zone  zone = ZoneManager.getZone(rl);
				if ((zone != null) && (!ZoneManager.checkPermission(zone, player, Flags.PROTECTION))) {
					found = true;
					break;
				}
			}
			if(found)break;
		}
		// Also block above the player
		Location ab = player.getLocation().add(0,2,0);
		if(!found){
			Zone  zone = ZoneManager.getZone(ab);
			if ((zone != null) && (!ZoneManager.checkPermission(zone, player, Flags.PROTECTION))) {
				found = true;
			}
		}
		
		return found;
	}
	
	public boolean checkWguard(Player player)
	{
		Location[] locs = new Location[]{player.getLocation(),
		player.getLocation().add(0,1,0),
		player.getLocation().add(0,2,0)};
		boolean found = false;
		for(Location loc : locs)
		{
			for(BlockFace bf : surrounding)
			{
				Location rl = loc.getBlock().getRelative(bf, 1).getLocation();
				if(!plugin.getWorldGuard().canBuild(player,rl)){
					found = true;
					break;
				}
			}
			if(found)break;
		}
		// Also block above the player
		Location ab = player.getLocation().add(0,2,0);
		if(!found){
			if(!plugin.getWorldGuard().canBuild(player,ab)){
				found = true;
			}
		}
		
		return found;
	}
	
	public void giveBackLuckyBlock(Player player, Block block){
		if(player.hasPermission("luckyblock.nosilkdrop")){
			HashMap<Integer, ItemStack> leftOver = new HashMap<Integer, ItemStack>();
	        leftOver.putAll((player.getInventory().addItem(new ItemStack(block.getType(), 1))));
	        if (!leftOver.isEmpty()) {
	            Location loc = block.getLocation();
	            player.getWorld().dropItem(loc, new ItemStack(leftOver.get(0).getType(), leftOver.get(0).getAmount()));
	            player.sendMessage(inventoryFull);
	        }
		} else {
			Location loc = block.getLocation();
			player.getWorld().dropItem(loc, new ItemStack(block.getType(), 1));
		}
        block.setType(Material.AIR);
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onBlockBreak(BlockBreakEvent e){
		FileConfiguration c = plugin.getConfig();
		Block b = e.getBlock();
		Player player = e.getPlayer();
		boolean f = false;
		for(String key : c.getConfigurationSection("blocks").getKeys(false)){
			if(b.getType().toString().equalsIgnoreCase(c.getString("blocks."+key+".block"))){
				if(!f){
					if(e.isCancelled()){
						return;
					}
					if(worldsWhitelist){
						boolean found = false;
						for(String world : worlds){
							if(player.getWorld().getName().equals(world)){
								found = true;
							}
						}
						if(!found){
							return;
						}
					} else {
						boolean found = false;
						for(String world : worlds){
							if(player.getWorld().getName().equals(world)){
								found = true;
							}
						}
						if(found){
							return;
						}
					}
					if(!player.hasPermission("luckyblock.*.break") && !player.hasPermission("luckyblock."+key+".break")){
						e.setCancelled(true);
						player.sendMessage(noBreakPermMsg);
						return;
					}
					//e.getPlayer().sendMessage("§c"+key+" §e kiütve");
					e.setCancelled(true);
					f = true;
					if(silktouch){
						if(player.getItemInHand().getType().toString().toLowerCase().contains("pickaxe")){
							if(player.getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)){
								giveBackLuckyBlock(player, b);
								return;
							}
						}
					}
					boolean regionDisallow = false;
					String effectNew = random.getRandomEffect(key);
					//e.getPlayer().sendMessage("§c"+effectNew+". §eeffektus kijelölve");
					Location loc = b.getLocation();
					World world = b.getWorld();
					String type = c.getString("blocks."+key+".effects."+effectNew+".type");
					switch(type){
						case "lightning":
							world.strikeLightning(player.getLocation());
							break;
						case "entity":
							int amount = c.getInt("blocks."+key+".effects."+effectNew+".amount");
							String entity = c.getString("blocks."+key+".effects."+effectNew+".entity");
							EntityType ent = null;
							try {
								ent = EntityType.valueOf(entity.toUpperCase());
							} catch (IllegalArgumentException ex) {
								//plugin.log.info("Ismeretlen entitiáns! Block: "+key+"; Effect: "+effectNew+"; Entity: "+entity);
								break;
							}
							for(int i = 0; i < amount; i++){
								world.spawnEntity(loc, ent);
							}
							break;
						case "items":
							List<String> items = c.getStringList("blocks."+key+".effects."+effectNew+".items");
							for(String item : items){
								if(!item.contains(" ")){
									//plugin.log.info("Hibas targy formatum! Block: "+key+"; Effect: "+effectNew);
									boolean valid = true;
									String[] split = item.split(":");
									int id = Integer.parseInt(split[0]);
									int data = Integer.parseInt(split[1]);
									Material mat = Material.getMaterial(id);
									if(mat == null){
										valid = false;
										mat = Material.AIR;
										//plugin.log.info("Ismeretlen targy ID! Block: "+key+"; Effect: "+effectNew+"; Item: \""+(id + ":" + data)+"\"");
									}
									ItemStack is = new ItemStack(mat, 1);
									is.setDurability((short)data);
									if(valid){
										world.dropItem(loc, is);
									}
								} else {
									boolean valid = true;
									String[] split = item.split(" ");
									String i = split[0];
									int am = Integer.parseInt(split[1]);
									String enchants = "";
									for(int in = 2; in < split.length; in++){
										if(enchants.equals("")){
											enchants = split[in];
										} else {
											enchants += " " + split[in];
										}
									}
									int id = Integer.parseInt(i.split(":")[0]);
									int data = Integer.parseInt(i.split(":")[1]);
									Material mat = Material.getMaterial(id);
									if(mat == null){
										valid = false;
										mat = Material.AIR;
										//plugin.log.info("Ismeretlen targy ID! Block: "+key+"; Effect: "+effectNew+"; Item: \""+(id + ":" + data)+"\"");
									}
									ItemStack is = new ItemStack(mat, am);
									is.setDurability((short)data);
									if(!enchants.equals("")){
										ItemMeta imet = is.getItemMeta();
										for(String enc : enchants.split(" ")){
											if(!valid){
												break;
											}
											String name = enc.split(":")[0];
											int level = Integer.parseInt(enc.split(":")[1]);
											Enchantment enchant = Enchantment.getByName(name.toUpperCase());
											if(enchant == null){
												valid = false;
												//plugin.log.info("A targy hibas varazslatot tartalmaz! Block: "+key+"; Effect: "+effectNew+"; Item: \""+(id + ":" + data)+"\"");
												break;
											} else {
												imet.addEnchant(enchant, level, true);
											}
										}
										if(valid)is.setItemMeta(imet);
									}
									if(valid)world.dropItem(loc, is);
								}
							}
							break;
						case "trap":
							String trap = c.getString("blocks."+key+".effects."+effectNew+".trap");
							if(useIzone){
								regionDisallow = checkZmod(player);
								if(regionDisallow)break;
							}
							if(useWguard){
								regionDisallow = checkWguard(player);
								if(regionDisallow)break;
							}
							switch(trap){
								case "anvil":
									trapPlayer(player, "COBBLESTONE", false);
									Material anvil = Material.ANVIL;
									player.getLocation().add(0,6,0).getBlock().setType(anvil);
									break;
								case "water":
									trapPlayer(player, "OBSIDIAN", true);
									Material water = Material.WATER;
									player.getLocation().add(0,1,0).getBlock().setType(water);
									break;
								case "lava":
									trapPlayer(player, "OBSIDIAN", true);
									Material lava = Material.LAVA;
									player.getLocation().add(0,1,0).getBlock().setType(lava);
									break;
								default:
									//plugin.log.info("Ismeretlen csapda! Block: "+key+"; Effect: "+effectNew+"; Trap: "+trap);
									break;
							}
							break;
						default:
							//plugin.log.info("Ismeretlen effektus tipus! Block: "+key+"; Effect: "+effectNew);
							break;
					}
					if(!regionDisallow) {
						b.setType(Material.AIR);
					} else {
						player.sendMessage(regionError);
					}
				} else {
					plugin.getLogger().log(Level.SEVERE,"Tobb mint egy van az adott blockbol a configban");
					plugin.getLogger().log(Level.SEVERE,"A plugin kikapcsol, kerlek javitsd ki a hibat");
					plugin.getServer().getPluginManager().disablePlugin(plugin);
					break;
				}
			}
		}
	}
}
