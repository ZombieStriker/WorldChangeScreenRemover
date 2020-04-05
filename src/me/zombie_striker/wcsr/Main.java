package me.zombie_striker.wcsr;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import me.zombie_striker.wcsr.DependencyDownloader.ProjectID;
import me.zombie_striker.wcsr.placeholderapi.WCSR_PHE;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {

	public boolean enableTitles = false;
	
	public String title;
	public String subtitle;	
	public double fadeInSeconds;
	public double staySeconds;
	public double fadeOutSeconds;

	public boolean enableDelayedTitles = false;
	public String title2;
	public String subtitle2;	
	public double fadeInSeconds2;
	public double staySeconds2;
	public double fadeOutSeconds2;
	public int delayedTitlesdelay = 20;
	
	public boolean tempBlind = true;
	public boolean tempNausea = true;
	public boolean tempSlowness = true;

	public double effectDelay;

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
		updateStuff();
		getCommand("wcsr").setExecutor(new WCSRCommand(this));
		getServer().getPluginManager().registerEvents(new MagicPacketHolder(this), this);
	}

	public void updateStuff() {
		enableTitles = (boolean) a("enableTitles", false);
		
		fadeInSeconds = (double) a ("TitleFadeIn-In-Seconds",0.5);
		staySeconds = (double) a ("TitleStay-In-Seconds",5.0);
		fadeOutSeconds = (double) a ("TitleFadeOut-In-Seconds",0.5);

		tempNausea = (boolean) a ("addEffect-Nausea",true);
		tempBlind = (boolean) a ("addEffect-Blindness",true);
		tempSlowness = (boolean) a ("addEffect-Slowness",true);
		effectDelay = (double) a ("Effect-Durration",5.0);
		
		title = ChatColor.translateAlternateColorCodes('&',
				(String) a("Title", "&cYou are teleporting from world \"%WCSR_To%\""));
		subtitle = ChatColor.translateAlternateColorCodes('&',
				(String) a("SubTitle", "&aYou came from world \"%WCSR_From%\""));
		
		enableDelayedTitles = (boolean) a ("Enable-DELAYED-titles",false);
		fadeInSeconds2 = (double) a ("DELAYED-TitleFadeIn-In-Seconds",0.5);
		staySeconds2 = (double) a ("DELAYED-TitleStay-In-Seconds",5.0);
		fadeOutSeconds2 = (double) a ("DELAYED-TitleFadeOut-In-Seconds",0.5);
		title2 = ChatColor.translateAlternateColorCodes('&',
				(String) a("DELAYED-Title", "&cThis is a delayed message"));
		subtitle2 = ChatColor.translateAlternateColorCodes('&',
				(String) a("DELAYED-SubTitle", "&a In case other plugins interfere with the regular titles"));
		delayedTitlesdelay = (int) a ("DELAYED-delay-for-title-in-ticks",20);
		if (needsSave)
			saveConfig();
		
		if (enableTitles) {
			try {
				new WCSR_PHE().register();
			} catch (Exception | Error e45) {
			}
		}

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
