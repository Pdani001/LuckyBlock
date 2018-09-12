package tk.Pdani.LuckyBlock.utils;

import java.util.Set;

import tk.Pdani.LuckyBlock.Main;

public class Randomizer {
    Main plugin;

    public Randomizer(tk.Pdani.LuckyBlock.Main main) {
    	this.plugin = main;
    }

    @Deprecated
    public String getRandom(String block) {
    	/*Random rand = new Random();
    	List<String> all = new ArrayList<String>();
    	all.clear();
    	int totalSum = 0;
    	Set<String> blocks = plugin.getConfig().getConfigurationSection("blocks."+block+".effects").getKeys(false);
        for(String item : blocks) {
        	int chance = plugin.getConfig().getInt("blocks."+block+".effects."+item+".chance");
        	all.add(item+";"+chance);
        	totalSum += chance;
        }
        int index = rand.nextInt(totalSum);
        int sum = 0;
        int i=0;
        while(sum < index ) {
             sum = sum + Integer.parseInt(all.get(i++).split(";")[1]);
        }*/
        return getRandomEffect(block);
    }
    
    public String getRandomEffect(String block){
    	RandomCollection<String> items = new RandomCollection<String>();
    	Set<String> blocks = plugin.getConfig().getConfigurationSection("blocks."+block+".effects").getKeys(false);
        for(String item : blocks) {
        	int chance = plugin.getConfig().getInt("blocks."+block+".effects."+item+".chance");
        	items.add(chance, item);
        }
        return items.next();
    }
}
