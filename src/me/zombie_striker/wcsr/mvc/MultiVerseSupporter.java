package me.zombie_striker.wcsr.mvc;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.utils.WorldManager;

public class MultiVerseSupporter {

	public static String getAlias(World world) {
		try {
		return new WorldManager(getcore()).getMVWorld(world).getAlias();
		}catch(Error|Exception e) {
			return world.getName();
		}
	}
	public static MultiverseCore getcore() {
		return (MultiverseCore) Bukkit.getPluginManager().getPlugin("Multiverse-Core");
	}
}
