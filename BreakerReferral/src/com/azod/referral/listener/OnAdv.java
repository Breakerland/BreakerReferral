package com.azod.referral.listener;



import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

import com.azod.referral.main;

public class OnAdv implements Listener {

	private main main;
	
	public OnAdv(main main) {
		this.main = main;
	}
	
	@EventHandler (priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void FinishAdv(PlayerAdvancementDoneEvent e) {
		Player p = e.getPlayer();
		String advdone = e.getAdvancement().getKey().toString();
		if(main.advlist.contains(advdone)) {
			main.updateCol(p.getUniqueId().toString());
			return;
		}
		else {
			return;
		}
	}
}
