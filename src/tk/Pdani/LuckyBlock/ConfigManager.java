package tk.Pdani.LuckyBlock;

import java.util.List;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class ConfigManager extends JavaPlugin {
	public FileConfiguration config = getConfig();
	public String getString(String path) {
		return getConfig().getString(path);
	}
	public List<String> getStringList(String path) {
		return getConfig().getStringList(path);
	}
	public int getInt(String path){
		return getConfig().getInt(path);
	}
	public boolean getBoolean(String path){
		return getConfig().getBoolean(path);
	}
	public void save(){
		saveConfig();
	}
	public void load(){
		reloadConfig();
	}
}
