package me.zombie_striker.wcsr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;
import com.comphenix.protocol.wrappers.BlockPosition;

import me.clip.placeholderapi.PlaceholderAPI;

public class MagicPacketHolder implements Listener {

	private List<UUID> respawnredPlayers = new ArrayList<>();
	private List<UUID> playersGoingToSametype = new ArrayList<>();

	private HashMap<UUID, Long> lasttele = new HashMap<>();
	private List<UUID> barredPlayers = new ArrayList<>();

	public static HashMap<UUID, String> from = new HashMap<>();

	public final Main thi;

	public PacketContainer tempp = null;

	boolean forceNether = false;

	public final void setHolder(PacketContainer p) {
		tempp = p;
	}

	public final PacketContainer getHolder() {
		return tempp;
	}

	long tickCounter = 0;

	public MagicPacketHolder(Main p) {
		thi = p;

		final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();
		new BukkitRunnable() {

			@Override
			public void run() {
				tickCounter++;
				tickCounter %= 100000;

			}
		}.runTaskLater(thi, 1);
		/*
		 * for (final PacketType p2 : PacketType.values()) if (p2.isServer() &&
		 * !p2.name().toLowerCase().contains("entity") &&
		 * !p2.name().toLowerCase().contains("custom") &&
		 * !p2.name().toLowerCase().contains("chunk") &&
		 * !p2.name().toLowerCase().contains("slot"))
		 * 
		 * protocolManager.addPacketListener(new PacketAdapter(thi,
		 * ListenerPriority.NORMAL, p2) {
		 * 
		 * @Override public void onPacketSending(final PacketEvent event) {
		 * System.out.print(p2.name()); } });
		 */
		protocolManager
				.addPacketListener(new PacketAdapter(thi, ListenerPriority.NORMAL, PacketType.Play.Server.RESPAWN) {
					@Override
					public void onPacketSending(final PacketEvent event) {

						if (respawnredPlayers.contains(event.getPlayer().getUniqueId())) {
							new BukkitRunnable() {
								public void run() {
									respawnredPlayers.remove(event.getPlayer().getUniqueId());
								}
							}.runTaskLater(thi, 2);
						} else {
							if (playersGoingToSametype.contains(event.getPlayer().getUniqueId())) {
								playersGoingToSametype.remove(event.getPlayer().getUniqueId());
								event.setCancelled(true);
								PacketContainer pc2 = protocolManager.createPacket(PacketType.Play.Server.UNLOAD_CHUNK);
								pc2.getIntegers().write(0, event.getPlayer().getLocation().getChunk().getX()).write(1,
										event.getPlayer().getLocation().getChunk().getZ());

								try {
									protocolManager.sendServerPacket(event.getPlayer(), pc2);
								} catch (InvocationTargetException e) {
									e.printStackTrace();
								}

								for (int x = event.getPlayer().getLocation().getChunk().getX() - 8; x < event
										.getPlayer().getLocation().getChunk().getX() + 8; x++) {
									for (int z = event.getPlayer().getLocation().getChunk().getZ() - 8; z < event
											.getPlayer().getLocation().getChunk().getZ() + 8; z++) {
										if (x == event.getPlayer().getLocation().getChunk().getX()
												&& z == event.getPlayer().getLocation().getChunk().getZ())
											continue;
										PacketContainer pc = protocolManager
												.createPacket(PacketType.Play.Server.UNLOAD_CHUNK);
										pc.getIntegers().write(0, x).write(1, z);
										try {
											protocolManager.sendServerPacket(event.getPlayer(), pc);
										} catch (InvocationTargetException e) {
											e.printStackTrace();
										}
									}
								}
							} else {
								// The hack level is over 9000!
								if (lasttele.containsKey(event.getPlayer().getUniqueId())) {
									long tick = lasttele.get(event.getPlayer().getUniqueId());
									if (tick == tickCounter)
										barredPlayers.add(event.getPlayer().getUniqueId());
								}
								lasttele.put(event.getPlayer().getUniqueId(), tickCounter);

								if (getHolder() == null) {
									if (event.getPacket().getIntegers().read(0) == -1) {
										if (forceNether == false) {
											event.setCancelled(true);
											setHolder(event.getPacket());
										}

										// nether
										new BukkitRunnable() {
											@Override
											public void run() {
												if (getHolder() != null) {
													final Location temp = event.getPlayer().getLocation();
													if (temp.getWorld().getEnvironment() == Environment.NETHER) {
														event.getPlayer()
																.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
														new BukkitRunnable() {
															@Override
															public void run() {
																forceNether = true;
																event.getPlayer().teleport(temp);
																forceNether = false;
															}
														}.runTaskLater(thi, 0);
													}
													setHolder(null);
												}
											}
										}.runTaskLater(thi, 0);
									}
								} else {
									setHolder(null);
									event.setCancelled(true);
								}
							}
						}
					}
				});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		if (e.getPlayer().getWorld().getEnvironment() == e.getFrom().getEnvironment()
				&& (!e.getFrom().getName().toLowerCase().contains("nether")
						&& !e.getFrom().getName().toLowerCase().contains("the_end")
						&& (!e.getPlayer().getWorld().getName().toLowerCase().contains("nether")
								&& !e.getPlayer().getWorld().getName().toLowerCase().contains("the_end"))))
			if (e.getPlayer().getWorld() != e.getFrom())
				if (barredPlayers.contains(e.getPlayer().getUniqueId()))
					barredPlayers.remove(e.getPlayer().getUniqueId());
				else
					playersGoingToSametype.add(e.getPlayer().getUniqueId());

		if (thi.enableTitles) {
			from.put(e.getPlayer().getUniqueId(), e.getFrom().getName());
			try {
				String text = PlaceholderAPI.setPlaceholders(e.getPlayer(), thi.title);
				String subtext = PlaceholderAPI.setPlaceholders(e.getPlayer(), thi.subtitle);
				e.getPlayer().sendTitle(text, subtext);

			} catch (Error | Exception e4) {
				e4.printStackTrace();
			}
		}

	}

	@EventHandler
	public void onListener(PlayerDeathEvent e) {
		respawnredPlayers.add(e.getEntity().getUniqueId());
	}
}
