package com.azod.referral.listener;



import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import com.azod.referral.main;

import net.md_5.bungee.api.ChatColor;

public class OnAdv implements Listener {

	private main main;
	
	public OnAdv(main main) {
		this.main = main;
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void FinishAdv(PlayerAdvancementDoneEvent e) {
		Player p = e.getPlayer();
		String advdone = e.getAdvancement().getKey().toString();
		if(main.advlist.contains(advdone)&& main.isRef(p.getUniqueId().toString())) {
			main.updateCol(p.getUniqueId().toString());
			Player player = (Player) Bukkit.getOfflinePlayer(main.getPar(p.getUniqueId().toString()));
			if(Bukkit.getOnlinePlayers().contains(player)) {
				player.sendMessage(ChatColor.GOLD+p.getName()+ChatColor.GREEN+" "+main.getConfig().getString("data.advsucc_par"));
			}
			p.sendMessage(ChatColor.GREEN+main.getConfig().getString("data.advsucc_fil"));
			return;
		}
		else {
			return;
		}
	}
}
