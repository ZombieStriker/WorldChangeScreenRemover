package me.zombie_striker.wcsr;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;

public class MagicPacketHolder implements Listener {

	private List<UUID> respawnredPlayers = new ArrayList<>();
	private List<UUID> playersGoingToSametype = new ArrayList<>();

	public final JavaPlugin thi;

	public PacketContainer tempp = null;

	public final void setHolder(PacketContainer p) {
		tempp = p;
	}

	public final PacketContainer getHolder() {
		return tempp;
	}

	public MagicPacketHolder(JavaPlugin p) {
		thi = p;

		final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

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
								new BukkitRunnable() {
									public void run() {
										playersGoingToSametype.remove(event.getPlayer().getUniqueId());
									}
								}.runTaskLater(thi, 2);

								event.setCancelled(true);
								PacketContainer pc2 = protocolManager.createPacket(PacketType.Play.Server.UNLOAD_CHUNK);
								pc2.getIntegers().write(0, event.getPlayer().getLocation().getChunk().getX()).write(1,
										event.getPlayer().getLocation().getChunk().getZ());
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
								if (getHolder() == null) {
									if (event.getPacket().getIntegers().read(0) == -1) {
										setHolder(event.getPacket());
										event.setCancelled(true);
										// Don't fault me
										// If in one tick the packet is still there, this was really meant to go to the
										// nether
										new BukkitRunnable() {

											@Override
											public void run() {
												if (getHolder() != null) {
													try {
														protocolManager.sendServerPacket(event.getPlayer(),
																getHolder());
													} catch (InvocationTargetException e) {
														e.printStackTrace();
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

						for (int i = 0; i < 25; i++) {
							new BukkitRunnable() {
								public void run() {
									event.getPlayer().teleport(event.getPlayer().getLocation());
								}
							}.runTaskLater(thi, i);
						}
					}
				});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldChange(PlayerChangedWorldEvent e) {
		if (e.getPlayer().getWorld().getEnvironment() == e.getFrom().getEnvironment()
				|| (!e.getFrom().getName().toLowerCase().contains("nether")
						|| !e.getFrom().getName().toLowerCase().contains("the_end")))
			if (e.getPlayer().getWorld() != e.getFrom())
				playersGoingToSametype.add(e.getPlayer().getUniqueId());
	}

	@EventHandler
	public void onListener(PlayerDeathEvent e) {
		respawnredPlayers.add(e.getEntity().getUniqueId());
	}
}
