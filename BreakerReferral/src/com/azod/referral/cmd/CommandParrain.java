package com.azod.referral.cmd;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Listener;

import com.azod.referral.main;

public class CommandParrain implements CommandExecutor, Listener {
	main plugin = main.getPlugin(main.class);
	public CommandParrain(main main) {
		this.plugin = main;
	}
	


	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		return false;
	}
	
	public boolean hasAsk(String d, String r) {
		try {
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referral`='"+plugin.getId(r)+"' AND `referred`='"+plugin.getId(d)+"' AND `accepted`=FALSE");
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
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT COUNT(*) as rowcount FROM `referral` WHERE `referral`='"+plugin.getId(s)+"' AND `accepted`=TRUE");
			ResultSet rs = statement.executeQuery();
			if(rs.next()) {
				if(rs.getInt("rowcount") == plugin.getConfig().getInt("data.maxref")) {
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
			PreparedStatement statement = plugin.getConnection()
					.prepareStatement("SELECT * FROM `referral` WHERE `referred`='"+plugin.getId(s)+"' AND `accepted`=TRUE");
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
			PreparedStatement statement = plugin.getConnection()
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
		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			try(Statement statement = plugin.getConnection().createStatement()){
				statement.executeUpdate("INSERT INTO `playerdata` (uuid, playername) VALUES ('"+p.getUniqueId().toString()+"', '"+p.getName()+"')");
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
	}
	
}
