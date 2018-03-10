package me.zombie_striker.wcsr;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.zombie_striker.wcsr.DependencyDownloader.ProjectID;


public class Main extends JavaPlugin {


	@Override
	public void onEnable() {
		try {
			//if (Bukkit.getPluginManager().getPlugin("PluginConstructorAPI") == null)
				//GithubDependDownloader.autoUpdate(this, new File(getDataFolder().getParentFile(),"PluginConstructorAPI.jar"), "ZombieStriker", "PluginConstructorAPI", "PluginConstructorAPI.jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
		//	GithubUpdater.autoUpdate(this, "ZombieStriker", "WorldChangeScreenRemover","WorldChangeScreenRemover.jar");
			//No longer using the github updater. The updater below uses BukkitDev to get the update.
			new Updater(this,289776);
		}catch(Error|Exception e){
			
		}
		if(!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
			Bukkit.broadcastMessage("[WorldChangeScreenRemover] ProtocolLib has not been installed. Installing now");
			new DependencyDownloader(this, ProjectID.PROTOCOLLIB);
		}
		
		
		
		getServer().getPluginManager().registerEvents(new MagicPacketHolder(this),this);
	}
}
