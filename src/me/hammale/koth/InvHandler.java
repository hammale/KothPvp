package me.hammale.koth;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class InvHandler {
	
    public static HashMap<String, Kit> kits = new HashMap<String, Kit>();
    
    public static void saveKit(Player player, String name, String perm)
    {
    	kits.put(name, new Kit(copyInventory(player.getInventory()), new Armor(player), perm));
    	writeKits();
    	player.getInventory().clear();
    	player.sendMessage(ChatColor.GREEN + "Kit set!");
    }
    
    public static boolean giveKit(Player player, String kit)
    {
    	player.getInventory().clear();
	    restoreInventory(player, kits.get(kit).getItems());
	    return true;
    }
     
    private static ItemStack[] copyInventory(Inventory inv)
    {
	    ItemStack[] original = inv.getContents();
//	    ItemStack[] copy = new ItemStack[original.length];
//	    for(int i = 0; i < original.length; ++i){
//	    	if(original[i] != null){
//	    		copy[i] = original[i];
//	    	}
//    	}
	    return original;
    }
     
    private static void restoreInventory(Player p, ItemStack[] inventory)
    {
    	p.getInventory().setContents(inventory);
    }
    
	public static void writeKits() {
		File f = new File("plugins/kKitPvp/kits.dat");
		if(f.exists()){			
			f.delete();
		}
		try {
			f.createNewFile();
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));			
			oos.writeObject(kits);
			oos.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static void loadKits() {
		File f = new File("plugins/kKitPvp/kits.dat");
		if(!f.exists()){
			return;
		}
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(new FileInputStream("plugins/kKitPvp/kits.dat"));
			kits = (HashMap<String, Kit>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}