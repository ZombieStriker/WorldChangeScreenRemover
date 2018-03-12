package me.zombie_striker.wcsr.placeholderapi;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.zombie_striker.wcsr.MagicPacketHolder;
import me.zombie_striker.wcsr.Main;


public class WCSR_PHE extends PlaceholderExpansion{

	@Override
	public String getAuthor() {
		return "Zombie_Striker";
	}

	@Override
	public String getIdentifier() {
		return "WCSR";
	}

	@Override
	public String getPlugin() {
		return null;
	}

	@Override
	public String getVersion() {
		return "1.0.0";
	}

	@Override
	public String onPlaceholderRequest(Player arg0, String arg1) {
		if(arg1.equals("From")) {
			return MagicPacketHolder.from.get(arg0.getUniqueId());
		}
		if(arg1.equals("To")){
			return arg0.getWorld().getName();
		}
		return null;
	}

}
