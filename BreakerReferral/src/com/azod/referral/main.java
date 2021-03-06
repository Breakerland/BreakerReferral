package com.azod.referral;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;


import com.azod.referral.cmd.CommandParrain;
import com.azod.referral.cmd.Padv;
import com.azod.referral.listener.OnAdv;
import com.azod.referral.listener.OnInte;
import com.azod.referral.listener.OnJoin;

import net.milkbowl.vault.economy.Economy;


public class main extends JavaPlugin {
	public String host, database, username, password, playerdata, referrals, achievements, rewards;
	public int port;
    private Connection connection;
    public List<String> advlist = new ArrayList<String>();
    public List<String> uuidlist = new ArrayList<String>();
    public HashMap<String, Integer> listrew = new HashMap<>();
    public final Set<UUID> players = new HashSet<>();
    public final Set<UUID> rewplayer = new HashSet<>();
    public static Economy economy = null;
	public void onEnable() {
		saveDefaultConfig();
		if(!getConfig().getString("firstlaunch").equalsIgnoreCase("false")) {
			Logger.getLogger("[BreakerReferral] Edit the config.yml and reload/restart (don't forget to set firstlaunch: false)");
			Bukkit.getPluginManager().disablePlugin(this);
			return;
		}
		mysqlSetup();
		initTable();
		adduuid();
		initAdv();
		setupEconomy();
		CommandParrain instance = new CommandParrain(this);
		getServer().getPluginManager().registerEvents(instance, this);
		this.getCommand("parrain").setExecutor(instance);	
		getCommand("padv").setExecutor(new Padv());
		getServer().getPluginManager().registerEvents(new OnJoin(), this);
	    getServer().getPluginManager().registerEvents(new OnAdv(this), this);
	    getServer().getPluginManager().registerEvents(new OnInte(this), this);
	    if(getServer().getPluginManager().getPlugin("Vault").equals(null)){
	    	Logger.getLogger("[PluginName] Vault required!");
	    	getServer().getPluginManager().disablePlugin(this);
	    	}
	}	
	public void mysqlSetup() {
		host = this.getConfig().getString("db.host");
		port = this.getConfig().getInt("db.port");
		database = this.getConfig().getString("db.database");
		username = this.getConfig().getString("db.username");
		password = this.getConfig().getString("db.password");
		playerdata = "playerdata";
		referrals = "referral";
		achievements = "achievements";
		rewards = "rewards";
		 try {    
			 synchronized(this) {
				 if(getConnection() != null && !getConnection().isClosed()) {
					 return;
				 }
				 Class.forName("com.mysql.jdbc.Driver");
				 setConnection(DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database, this.username, this.password));
				 Bukkit.getConsoleSender().sendMessage("žaSuccessfully connected to db");
				 }
		 	} catch (ClassNotFoundException e) {
	            e.printStackTrace();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }		
	}
	public Connection getConnection() {
		return connection;
	}	
	public void setConnection(Connection connection) {
		this.connection = connection;
	}
	public void initTable() {
		try (Statement statement = getConnection().createStatement()){
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `playerdata` ( `id` INT(11) NOT NULL AUTO_INCREMENT, `username` VARCHAR(16) NOT NULL, `uuid` VARCHAR(36) NOT NULL, PRIMARY KEY (`id`))");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `referral` ( `referral` int(11) NOT NULL, `referred` int(11) NOT NULL,`accepted` BOOLEAN NOT NULL DEFAULT FALSE,`date` timestamp NOT NULL DEFAULT current_timestamp())");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `rewards` ( `referral` int(11) NOT NULL, `referred` int(11) NOT NULL)");
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `achievements` ( `id` int(11) NOT NULL)");
		}catch (SQLException e) {
			e.printStackTrace();
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+"Tables initialized");
	}
	public void initAdv() {
		addadv();
		for(String s : advlist) {
			s = s.replace("minecraft:", "");
			if(!colExist(achievements, s)) {
				createCol(achievements, s);
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD+s+" has been added to achievements");
			}
			if(!colExist(rewards, s)) {
				createCol(rewards, s);
				Bukkit.getConsoleSender().sendMessage(ChatColor.GOLD+s+" has been added to rewards");
			}
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+"Colums updated");
	}
	public void updateCol(String s) {
		for(String st : advlist) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(s));
			String stt =st;
			
			 st = st.replace("minecraft:", "");
			try {
				PreparedStatement statement = getConnection()
						.prepareStatement("SELECT * FROM `achievements` WHERE `id`='"+getId(s)+"' AND `"+st+"`='0'");
				ResultSet rs = statement.executeQuery();
				if(rs.next()) {
					if(hasAdvancement(p, stt)) {
						try {
							PreparedStatement statementt = getConnection()
									.prepareStatement("UPDATE `achievements` SET `"+st+"`='1' WHERE `id`='"+getId(s)+"'");
							statementt.executeUpdate();
						}catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
				
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean colExist(String t, String s) {
		try {
			DatabaseMetaData md = connection.getMetaData();
			ResultSet rs = md.getColumns(null, null, t, s);
			 if (rs.next()) {
			      return true;
			    }
			 else {
				 return false;
			 }
			}catch (SQLException e) {
			e.printStackTrace();
		}
		return true;	
	}
	public void createCol(String t, String s) {
		try (Statement statement = getConnection().createStatement()){
			statement.executeUpdate("ALTER TABLE `" + t + "` ADD `" + s + "` BOOLEAN NOT NULL DEFAULT FALSE;");
			Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN +"Collum "+s+" added");

		}catch (SQLException e) {
			e.printStackTrace();
		}
	}
    public String getUnique(Integer i) {
    	try(Statement statement = getConnection().createStatement()){
    		ResultSet rs = statement.executeQuery("SELECT * FROM `playerdata` WHERE id='"+i+"'");
    		if(rs.next()) {
    			String uuid = rs.getString("uuid");
    			return uuid;
    		}
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    public Integer getId(String s) {
    	try(Statement statement = getConnection().createStatement()){
    		ResultSet rs = statement.executeQuery("SELECT * FROM `playerdata` WHERE uuid='"+s+"'");
    		if(rs.next()) {
    			Integer uuid = rs.getInt("id");
    			return uuid;
    		}
    		
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
	public static boolean hasAdvancement(OfflinePlayer player, String name) {
        // name should be something like minecraft:husbandry/break_diamond_hoe
        Advancement a = getAdvancement(name);
        if(a == null){
            // advancement does not exists.
            return false;
        }
        Player p = (Player) player;
        AdvancementProgress progress = p.getAdvancementProgress(a);
        // getting the progress of this advancement.
        return progress.isDone();
        //returns true or false.
    }
    public static Advancement getAdvancement(String name) {
        Iterator<Advancement> it = Bukkit.getServer().advancementIterator();
        // gets all 'registered' advancements on the server.
        while (it.hasNext()) {
            // loops through these.
            Advancement a = it.next();
            if (a.getKey().toString().equalsIgnoreCase(name)) {
                //checks if one of these has the same name as the one you asked for. If so, this is the one it will return.
                return a;
            }
        }
        return null;
    }
	@SuppressWarnings("unchecked")
	public void addadv() {
		advlist.addAll((Collection<? extends String>) getConfig().getList("advancement"));
		for(String s : advlist) {
			listrew.put(s, getConfig().getInt("rewards."+s));
		}
	}
	public void adduuid() {
	try {
		PreparedStatement statement = getConnection()
				.prepareStatement("SELECT `referred` FROM `referral`");
		ResultSet rs = statement.executeQuery();
		if(rs.next()) {
			while(rs.next()) {
				uuidlist.add(getUnique(rs.getInt("referred")));
			}
		}
	}catch (SQLException e) {
		e.printStackTrace();
	}
	}
	public boolean hasAsk(String d, String r) {
		try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`='"+getId(r)+"' AND `referred`='"+getId(d)+"' AND `accepted`=FALSE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean haveMax(String s) {
		try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT COUNT(*) as rowcount FROM `referral` WHERE `referral`='"+getId(s)+"' AND `accepted`=TRUE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				if(rs.getInt("rowcount") == getConfig().getInt("data.maxref")) {
					return true;
				}				
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean isRef(String s) {
		try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referred`='"+getId(s)+"' AND `accepted`=TRUE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public boolean DataExist(String s) {
		try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT * FROM `playerdata` WHERE `uuid`='"+s+"'");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	public void addData(OfflinePlayer p) {
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			try(Statement statement = getConnection().createStatement()){
				statement.executeUpdate("INSERT INTO `playerdata` (uuid, username) VALUES ('"+p.getUniqueId().toString()+"', '"+p.getName()+"')");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	public void addRef(String d, String r) {
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			try(Statement statement = getConnection().createStatement()){
				statement.executeUpdate("INSERT INTO `referral` (referral,referred,accepted) VALUES ('"+getId(r)+"','"+getId(d)+"', '0')");
				
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	public void delRef(String d, String r) {
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			try(Statement statement = getConnection().createStatement()){
				statement.executeUpdate("DELETE FROM `referral` WHERE `referral`='"+getId(r)+"' AND `referred`='"+getId(d)+"' AND `accepted`=FALSE");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	public void accRef(String d, String r) {
		Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
			try(Statement statement = getConnection().createStatement()){
				statement.executeUpdate("UPDATE `referral` SET `accepted`=TRUE WHERE `referral`='"+getId(r)+"' AND `referred`='"+getId(d)+"' AND `accepted`=FALSE");
				statement.executeUpdate("INSERT INTO `rewards` (referral,referred) VALUES ('"+getId(r)+"','"+getId(d)+"')");
				statement.executeUpdate("INSERT INTO `achievements` (id) VALUES ('"+getId(d)+"')");
			}catch (SQLException e) {
				e.printStackTrace();
			}
			updateCol(Bukkit.getOfflinePlayer(UUID.fromString(d)).getUniqueId().toString());
		});
	}
	public boolean haveChild(String uuid) {
		try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`='"+getId(uuid)+"' AND `accepted`='1'");
			
			ResultSet results = statement.executeQuery();			
			if(results.next()) {
				return true;
			}
		}catch (SQLException e) {
			e.printStackTrace();
		}
		return false;		
	}
	public boolean setupEconomy(){
        RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
        }
        return (economy != null);
    }
	public UUID getPar(String d) {
	try {
			PreparedStatement statement = getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referred`='"+getId(d)+"' AND `accepted`='1'");
			
			
			ResultSet results = statement.executeQuery();
			if(results.next()) {
				UUID player = UUID.fromString(getUnique(results.getInt("referral")));
				return player;
			}
			
		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return null;
	
	}


}
