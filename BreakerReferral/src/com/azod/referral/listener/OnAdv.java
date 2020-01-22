package com.azod.referral.listener;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Listener;

import com.azod.referral.main;

public class OnAdv implements Listener {

	private List<String> adv = new ArrayList<String>();
	private main main;
	
	public OnAdv(main main) {
		this.main = main;
	}

}
