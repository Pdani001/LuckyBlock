package tk.Pdani.LuckyBlock.event;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

import tk.Pdani.LuckyBlock.Main;

public class BlockPlace implements Listener {
	private Main plugin;
	String noPlacePermMsg = "";
	
	//<--CONFIG "DEFAULT" SETTINGS
		boolean worldsWhitelist = true;
		List<String> worlds = new ArrayList<String>();
	//-->
	
	public BlockPlace(tk.Pdani.LuckyBlock.Main main){
		this.plugin = main;
		this.noPlacePermMsg = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.noPlacePerm"));
		this.worlds = plugin.getConfig().getStringList("settings.worlds");
		this.worldsWhitelist = plugin.getConfig().getBoolean("settings.worldsWhitelist");
	}
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent e){
		if(e.isCancelled()) return;
		FileConfiguration c = plugin.getConfig();
		Block b = e.getBlock();
		Player player = e.getPlayer();
		for(String key : c.getConfigurationSection("blocks").getKeys(false)){
			if(b.getType().toString().equalsIgnoreCase(c.getString("blocks."+key+".block"))){
				//plugin.log.info("§c"+jatekos+" §elerakott egy §c"+key+" §eLuckyBlockot");
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
				if(!player.hasPermission("luckyblock.*.place") && !player.hasPermission("luckyblock."+key+".place")){
					e.setCancelled(true);
					player.sendMessage(noPlacePermMsg);
					return;
				}
				break;
			}
		}
	}
}
