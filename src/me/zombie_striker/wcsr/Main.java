package me.zombie_striker.wcsr;

import java.io.File;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.zombie_striker.wcsr.DependencyDownloader.ProjectID;
import me.zombie_striker.wcsr.placeholderapi.WCSR_PHE;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {

	public boolean enableTitles = false;
	public String title;
	public String subtitle;

	@Override
	public void onEnable() {
		try {
			// if (Bukkit.getPluginManager().getPlugin("PluginConstructorAPI") == null)
			// GithubDependDownloader.autoUpdate(this, new
			// File(getDataFolder().getParentFile(),"PluginConstructorAPI.jar"),
			// "ZombieStriker", "PluginConstructorAPI", "PluginConstructorAPI.jar");
		} catch (Exception e) {
			e.printStackTrace();
		}
		try {
			if (!getConfig().contains("autoUpdate")) {
				getConfig().set("autoUpdate", true);
				saveConfig();
			}
			if (getConfig().getBoolean("autoUpdate"))
				GithubUpdater.autoUpdate(this, "ZombieStriker", "WorldChangeScreenRemover",
						"WorldChangeScreenRemover.jar");
			// No longer using the github updater. The updater below uses BukkitDev to get
			// the update.
			// new Updater(this,289776);
		} catch (Error | Exception e) {

		}
		if (!getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
			Bukkit.broadcastMessage("[WorldChangeScreenRemover] ProtocolLib has not been installed. Installing now");
			new DependencyDownloader(this, ProjectID.PROTOCOLLIB);
		}

		enableTitles = (boolean) a("enableTitles", false);
		title = ChatColor.translateAlternateColorCodes('&',
				(String) a("Title", "&cYou are teleporting from world \"%WCSR_To%\""));
		subtitle = ChatColor.translateAlternateColorCodes('&',
				(String) a("SubTitle", "&aYou came from world \"%WCSR_From%\""));
		if (needsSave)
			saveConfig();

		if (enableTitles)
			try {
				new WCSR_PHE().register();
			} catch (Exception | Error e45) {
			}

		getServer().getPluginManager().registerEvents(new MagicPacketHolder(this), this);
	}

	private boolean needsSave = false;

	public Object a(String path, Object expect) {
		if (getConfig().contains(path)) {
			return getConfig().get(path);
		} else {
			needsSave = true;
			getConfig().set(path, expect);
			return expect;
		}
	}
}
