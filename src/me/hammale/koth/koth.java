package me.hammale.koth;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashSet;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class koth extends JavaPlugin {
	
	public Logger logger = Logger.getLogger("Minecraft");
	
	public HashSet<kPlayer> players = new HashSet<kPlayer>();
	
	public FileConfiguration config;
	
	public Connection conn = null;
	
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
		config = getConfig();
		pm.registerEvents(new listener(this), this);
		connect();
		refreshDB();
		makeFolder();
		InvHandler.loadKits();
	}
	
	private void makeFolder() {
		File f = new File("plugins/kKitPvp/");
		if(!f.exists()){
			f.mkdir();
		}
	}
	
	public void refreshDB() {
		try{
			Statement s = conn.createStatement ();
			s.executeQuery ("SELECT * FROM `board`");
			ResultSet rs = s.getResultSet();
			while(rs.next()){
			    if(getPlayer(getServer().getPlayer(rs.getString("ign"))) != null){
				    getPlayer(getServer().getPlayer(rs.getString("ign"))).kills = rs.getInt("kills");	
			    }
			}
			rs.close();
			s.close();
		}catch(Exception e){
			e.printStackTrace();
		}		
	}

	public void connect(){
        try
        {
            String userName = "HAMMmc2179ALE";
            String password = "ALEb6f8aea723HAMM";
            String url = "jdbc:mysql://198.143.189.74/mc2179";
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
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
		if(!(sender instanceof Player)){
			sender.sendMessage("Players only please!");
			return true;
		}
		Player p = (Player) sender;
		if(cmd.getName().equalsIgnoreCase("kitset")
				&& p.isOp()){
			if(args.length == 1){
				InvHandler.saveKit(p, args[0], "NA");
			}else if(args.length == 2){
				InvHandler.saveKit(p, args[0], args[1]);
			}
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
}