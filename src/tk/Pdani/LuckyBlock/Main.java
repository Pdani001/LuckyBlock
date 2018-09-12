package tk.Pdani.LuckyBlock;

import java.util.logging.Level;
import org.bukkit.command.CommandExecutor;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;

import net.techguard.izone.iZone;
import tk.Pdani.LuckyBlock.event.BlockBreak;
import tk.Pdani.LuckyBlock.event.BlockPlace;

public class Main extends JavaPlugin implements Listener, CommandExecutor {
	String version = this.getDescription().getVersion();
	
	public void onEnable() {
		getConfig().options().copyDefaults(true);
		saveDefaultConfig();
		getServer().getPluginManager().registerEvents(new BlockBreak(this), this);
		getServer().getPluginManager().registerEvents(new BlockPlace(this), this);
		getLogger().log(Level.INFO,"Plugin enabled!");
	}
	
	public void onDisable() {
		getLogger().log(Level.INFO,"Plugin disabled!");
	}
	
	public boolean getZmod() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("iZone");
	 
	    return (plugin != null && (plugin instanceof iZone));
	}
	
	public WorldGuardPlugin getWorldGuard() {
	    Plugin plugin = getServer().getPluginManager().getPlugin("WorldGuard");
	 
	    // WorldGuard may not be loaded
	    if (plugin == null || !(plugin instanceof WorldGuardPlugin)) {
	        return null; // Maybe you want throw an exception instead
	    }
	 
	    return (WorldGuardPlugin) plugin;
	}
}
