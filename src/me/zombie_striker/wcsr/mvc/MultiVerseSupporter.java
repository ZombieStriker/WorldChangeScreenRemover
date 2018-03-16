package me.zombie_striker.wcsr.mvc;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.onarandombox.MultiverseCore.MultiverseCore;

public class MultiVerseSupporter {

	public static String getAlias(World world) {
		try {
			return getcore().getMVWorldManager().getMVWorld(world).getAlias();
		} catch (Error | Exception e) {
			return world.getName();
		}
	}

	public static MultiverseCore getcore() {
		return (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
	}
}
