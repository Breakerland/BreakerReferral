package com.azod.referral.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.azod.referral.main;

public class padv implements CommandExecutor {
	public main plugin;
	public padv(main main) {
		this.plugin = main;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if( !(sender instanceof Player)) {
			
			return false;
			
		}
			Player p = (Player) sender;
			p.performCommand("parrain advancement");
			return false;
	}

}
