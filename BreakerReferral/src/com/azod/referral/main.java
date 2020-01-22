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
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.azod.referral.cmd.CommandParrain;
import com.azod.referral.listener.OnAdv;
import com.azod.referral.listener.OnJoin;


public class main extends JavaPlugin {
	public String host, database, username, password, playerdata, referrals, achievements, rewards;
	public int port;
    private Connection connection;
    public List<String> advlist = new ArrayList<String>();
    public List<String> uuidlist = new ArrayList<String>();
	public void onEnable() {
		saveDefaultConfig();
		mysqlSetup();
		initTable();
		initAdv();
		CommandParrain instance = new CommandParrain(this);
		getServer().getPluginManager().registerEvents(instance, this);
		this.getCommand("parrain").setExecutor(instance);	
		getServer().getPluginManager().registerEvents(new OnJoin(), this);
	    getServer().getPluginManager().registerEvents(new OnAdv(this), this);
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
				 Bukkit.getConsoleSender().sendMessage("§aSuccessfully connected to db");
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
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS `rewards` ( `id` int(11) NOT NULL)");
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
		for(String u : uuidlist) {
			updateCol(u);
		}
		Bukkit.getConsoleSender().sendMessage(ChatColor.GREEN+"Players advancements updated");
	}
	public void updateCol(String s) {
		for(String st : advlist) {
			OfflinePlayer p = Bukkit.getOfflinePlayer(UUID.fromString(s));
			 st = st.replace("minecraft:", "");
			try {
				PreparedStatement statement = getConnection()
						.prepareStatement("SELECT * FROM `achievements` WHERE `id`='"+getId(s)+"' AND `"+st+"`=FALSE");
				ResultSet rs = statement.executeQuery();
				if(rs.next()) {
					if(hasAdvancement(p, st)) {
						try {
							PreparedStatement statementt = getConnection()
									.prepareStatement("UPDATE `achievements` SET `"+st+"`=TRUE WHERE `id`='"+getId(s)+"'");
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
    		String uuid = statement.executeQuery("SELECT * FROM `playerdata` WHERE id='"+i+"`").getString("uuid");
    		return uuid;
    	}catch (SQLException e) {
    		e.printStackTrace();
    	}
    	return null;
    }
    public Integer getId(String s) {
    	try(Statement statement = getConnection().createStatement()){
    		Integer uuid = statement.executeQuery("SELECT * FROM `playerdata` WHERE uuid='"+s+"`").getInt("id");
    		return uuid;
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
}
