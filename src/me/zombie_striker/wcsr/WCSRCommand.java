package me.zombie_striker.wcsr;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class WCSRCommand implements CommandExecutor {

	private Main main;
	public WCSRCommand(Main m) {
		this.main = m;
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command arg1, String arg2, String[] args) {
		if(args.length != 1) {
			sender.sendMessage(ChatColor.GOLD+"[WCSR]"+ChatColor.WHITE+" Commands");
			sender.sendMessage("/WCSR reload: Reload config");
			return true;
		}
		if(args[0].equalsIgnoreCase("reload")) {
			main.reloadConfig();
			main.updateStuff();
			sender.sendMessage(ChatColor.GOLD+"[WCSR]"+ChatColor.WHITE+" Reloaded config");
		}
		return false;
	}

}
