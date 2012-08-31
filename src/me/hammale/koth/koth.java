package me.hammale.koth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class koth extends JavaPlugin {
	
	public Logger logger = Logger.getLogger("Minecraft");
	
	public HashSet<String> active = new HashSet<String>();
	public HashSet<kPlayer> players = new HashSet<kPlayer>();
	public HashMap<String, Double> scores = new HashMap<String, Double>();
	
	public Connection conn = null;
	
	public int totalKills, totalTime;
	
	Location blockLoc;
	
	@Override
	public void onEnable()
	{
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info(pdfFile.getName() + ", Version "
				+ pdfFile.getVersion() + ", Has Been Enabled!");
		PluginManager pm = getServer().getPluginManager();
		for(Player p : getServer().getOnlinePlayers()){
			players.add(new kPlayer(this, p));
		}
		pm.registerEvents(new listener(this), this);
		read();
		connect();
		refreshDB();
	}
	
	public void refreshDB() {
		try{
			Statement s = conn.createStatement ();
			s.executeQuery ("SELECT * FROM `board`");
			ResultSet rs = s.getResultSet();
			ArrayList<Triple> tmpplayers = new ArrayList<Triple>();
			while(rs.next()){
			    String[] results = rs.getString("time").split( ":\\s*" );
			    int time = ((Integer.parseInt(Arrays.asList(results).get(0))*60*60)) + ((Integer.parseInt(Arrays.asList(results).get(1))*60)) + ((Integer.parseInt(Arrays.asList(results).get(2))));
			    totalKills += rs.getInt("kills");
			    totalTime += time;
			    tmpplayers.add(new Triple(rs.getString("ign"),rs.getInt("kills"),time));
			    if(getPlayer(getServer().getPlayer(rs.getString("ign"))) != null){
				    getPlayer(getServer().getPlayer(rs.getString("ign"))).kills = rs.getInt("kills");		    
				    getPlayer(getServer().getPlayer(rs.getString("ign"))).time = time;
			    }
			}
			scores.clear();
			for(Triple t : tmpplayers){
				double score = (t.two/(float)totalKills) + (t.three/(float)totalTime);
				scores.put(t.one, score);
				if(getServer().getPlayer(t.one) != null){
					getPlayer(getServer().getPlayer(t.one)).score = score;
					getPlayer(getServer().getPlayer(t.one)).rank = getRank(getServer().getPlayer(t.one));
				}
			}
			syncRank();
			rs.close();
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}

	public void connect(){
        try
        {
            String userName = "root";
            String password = "r4pt0r";
            String url = "jdbc:mysql://web02/koth";
            Class.forName ("com.mysql.jdbc.Driver").newInstance();
            conn = DriverManager.getConnection(url, userName, password);
            logger.info("[KothPvp] Database connection established!");
        }
        catch (Exception e)
        {
            logger.info("[KothPvp] ERROR! Cannot connect to database server!");
        }
	}	
	
	@Override
	public void onDisable(){
		PluginDescriptionFile pdfFile = getDescription();
		this.logger.info(pdfFile.getName() + ", Version "
				+ pdfFile.getVersion() + ", Has Been Disabled!");
		if(conn != null){
            try
            {
                conn.close ();
                logger.info("[KothPvp] Database connection terminated!");
            }
            catch (Exception e) { /* ignore close errors */ }
		}
	}
	
	public String Colorize(String s) {
	    if (s == null) return null;
	    return s.replaceAll("&([0-9a-f])", "§$1");
	}
	
	public void read(){
		File tmp = new File("plugins/KothPvp/block.dat");
		if(!tmp.exists()){
			return;
		}
		String arenaloc = null;
		try{
			FileInputStream fstream = new FileInputStream("plugins/KothPvp/block.dat");
			  DataInputStream in = new DataInputStream(fstream);
			  BufferedReader br = new BufferedReader(new InputStreamReader(in));
			  String strLine;
			  while ((strLine = br.readLine()) != null)   {
				  arenaloc = strLine;
			  }
			  in.close();
		}catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
		}
		String[] arenalocarr = arenaloc.split(",");
		if (arenalocarr.length == 3)
		{
			int x = Integer.parseInt(arenalocarr[0]);
			int y = Integer.parseInt(arenalocarr[1]);
			int z = Integer.parseInt(arenalocarr[2]);
			blockLoc = new Location(getServer().getWorld("world"),x, y, z);
		}	
	}
	
	public void write(Location l){
		File folder = new File("plugins/KothPvp");
		if(!folder.exists()){
			folder.mkdir();
		}
		File tmp = new File("plugins/KothPvp/block.dat");
		if(tmp.exists()){
			tmp.delete();
		}
		 try{
			  FileWriter fstream = new FileWriter("plugins/KothPvp/block.dat");
			  BufferedWriter out = new BufferedWriter(fstream);
			  out.write(l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ());
			  out.close();
			  fstream.close();
		 }catch (Exception e){
			  System.err.println("Error: " + e.getMessage());
		 }
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage("Players only please!");
			return true;
		}
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("pvp")){
			if(args.length > 0){
				if(args[0].equalsIgnoreCase("set")
						&& p.isOp()){
					if(blockLoc != null){
						blockLoc.getBlock().setType(Material.AIR);
					}
					Location loc = p.getLocation();
					int blockX = loc.getBlockX();
					int blockY = loc.getBlockY();
					int blockZ = loc.getBlockZ();
					write(loc);
					p.sendMessage(ChatColor.GOLD + "KOTH block has been set succesfully at: " + blockX
							+ ", " + blockY + ", " + blockZ);
					loc.getBlock().setType(Material.DIAMOND_BLOCK);
					blockLoc = loc;
				}
			}else{
				p.sendMessage(ChatColor.GREEN + "[KothPVP] Giving pvp kit.");
				p.getInventory().clear();
				for (int i=0;i<p.getInventory().getSize();i++){
					p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
				}
				
				p.getInventory().setChestplate(
						new ItemStack(Material.IRON_CHESTPLATE));
				p.getInventory().setLeggings(
						new ItemStack(Material.IRON_LEGGINGS));
				p.getInventory().setHelmet(
						new ItemStack(Material.IRON_HELMET));
				p.getInventory().setBoots(
						new ItemStack(Material.IRON_BOOTS));
				ItemStack sword = new ItemStack(Material.DIAMOND_SWORD);
				sword.addEnchantment(Enchantment.getById(16), 1);
				p.getInventory().setItem(0, sword);
			}
		}else if(cmd.getName().equalsIgnoreCase("archer")){
			p.sendMessage(ChatColor.GREEN + "[KothPVP] Giving archer kit.");
			p.getInventory().clear();
			for (int i=0;i<p.getInventory().getSize();i++){
				p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
			}
			
			p.getInventory().setChestplate(
					new ItemStack(Material.IRON_CHESTPLATE));
			p.getInventory().setLeggings(
					new ItemStack(Material.IRON_LEGGINGS));
			p.getInventory().setHelmet(
					new ItemStack(Material.IRON_HELMET));
			p.getInventory().setBoots(
					new ItemStack(Material.IRON_BOOTS));
			ItemStack bow = new ItemStack(Material.BOW);
			bow.addEnchantment(Enchantment.getById(48), 1);
			p.getInventory().setItem(0, bow);
			p.getInventory().setItem(1, new ItemStack(Material.ARROW, 64));
		}else if(cmd.getName().equalsIgnoreCase("chemist")
				&& p.hasPermission("kothpvp.chemist")){
			p.sendMessage(ChatColor.GREEN + "[KothPVP] Giving chemist kit.");
			p.getInventory().clear();
			for (int i=0;i<p.getInventory().getSize();i++){
				p.getInventory().addItem(new ItemStack(Material.MUSHROOM_SOUP, 1));
			}
			
			p.getInventory().setChestplate(
					new ItemStack(Material.GOLD_CHESTPLATE));
			p.getInventory().setLeggings(
					new ItemStack(Material.GOLD_LEGGINGS));
			p.getInventory().setHelmet(
					new ItemStack(Material.GOLD_HELMET));
			p.getInventory().setBoots(
					new ItemStack(Material.GOLD_BOOTS));
			
			ItemStack sword = new ItemStack(Material.IRON_SWORD);
			sword.addEnchantment(Enchantment.getById(16), 1);
			
			ItemStack pot1 = new ItemStack(Material.POTION, 5);
			pot1.setDurability((short) 8197);
			
			ItemStack pot2 = new ItemStack(Material.POTION, 5);
			pot2.setDurability((short) 8196);
			
			ItemStack pot3 = new ItemStack(Material.POTION, 5);
			pot3.setDurability((short) 8201);
			
			ItemStack pot4 = new ItemStack(Material.POTION, 5);
			pot4.setDurability((short) 8204);
			
			p.getInventory().setItem(0, sword);
			p.getInventory().setItem(1, pot1);
			p.getInventory().setItem(2, pot2);
			p.getInventory().setItem(3, pot3);
			p.getInventory().setItem(4, pot4);
		}
		return true;
	}
	
	public kPlayer getPlayer(Player p){
		if(p == null){
			return null;
		}
		for(kPlayer kp : players){
			if(kp.getPlayer().getName().equalsIgnoreCase(p.getName())){
				return kp;
			}
		}
		return null;
	}
	
	public void syncRank(){
		System.out.print("Syncing...");
		for(String name : scores.keySet()){
			try{
				Statement s = conn.createStatement ();
				s.executeQuery ("SELECT * FROM `board` WHERE `ign` = '" + name + "'");
				ResultSet rs = s.getResultSet();			
				if(rs.next()){
					String[] results = rs.getString("time").split( ":\\s*" );
				    int time = ((Integer.parseInt(Arrays.asList(results).get(0))*60*60)) + ((Integer.parseInt(Arrays.asList(results).get(1))*60)) + ((Integer.parseInt(Arrays.asList(results).get(2))));			    
					double score = (rs.getInt("kills")/(float)totalKills) + (time/(float)totalTime);
					if(rs.getDouble("rank") != getRank(score)){
						s.executeUpdate("UPDATE `koth`.`board` SET `rank` = '" + getRank(score) + "' WHERE `board`.`ign` = '" + name +"'");
					}
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public int getRank(double ascore) {
		int rank = 1;
		for(double score : scores.values()){
			if(score > ascore){
				rank++;
			}
		}
		return rank;
	}
	
	public int getRank(Player player) {
		kPlayer kp = getPlayer(player);
		int rank = 1;
		for(double score : scores.values()){
			if(score > kp.score){
				rank++;
			}
		}
		return rank;
	}
	
}