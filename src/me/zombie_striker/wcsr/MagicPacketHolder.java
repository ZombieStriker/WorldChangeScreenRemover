package me.zombie_striker.wcsr;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World.Environment;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import com.comphenix.protocol.*;
import com.comphenix.protocol.events.*;

import me.clip.placeholderapi.PlaceholderAPI;
import me.zombie_striker.wcsr.mvc.MultiVerseSupporter;

public class MagicPacketHolder implements Listener {

	private List<UUID> respawnredPlayers = new ArrayList<>();
	private List<UUID> playersGoingToSametype = new ArrayList<>();

	private HashMap<UUID, Long> lasttele = new HashMap<>();
	private List<UUID> barredPlayers = new ArrayList<>();

	public static HashMap<UUID, String> from = new HashMap<>();
	public static List<UUID> quickSkippers = new ArrayList<>();

	public final Main thi;

	public HashMap<UUID, PacketContainer> tempp = new HashMap<>();
	public HashMap<UUID, List<PacketContainer>> temppLocations = new HashMap<>();

	// boolean forceNether = false;

	public final void setHolder(UUID uuid, PacketContainer p) {
		tempp.put(uuid, p);
	}

	public final PacketContainer getHolder(UUID uuid) {
		return tempp.get(uuid);
	}

	long tickCounter = 0;

	@SuppressWarnings("deprecation")
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

		try {
			for (PacketType pt : PacketType.values())
				if (pt.isServer() && pt != PacketType.Play.Server.TAB_COMPLETE && pt.isServer()
						&& pt != PacketType.Play.Server.TITLE && pt.isServer() && pt != PacketType.Play.Server.LOGIN
						&& pt != PacketType.Play.Server.CHAT && pt.isServer()
						&& pt != PacketType.Play.Server.KICK_DISCONNECT && pt != PacketType.Play.Server.TAGS
						&& pt != PacketType.Status.Server.PONG && pt != PacketType.Play.Server.BLOCK_ACTION
						&& pt != PacketType.Play.Server.PLAYER_INFO && pt != PacketType.Play.Server.WINDOW_DATA
						&& pt != PacketType.Play.Server.WINDOW_ITEMS && pt.isServer()
						&& pt != PacketType.Play.Server.RESPAWN && pt != PacketType.Play.Server.SET_COMPRESSION
						&& pt != PacketType.Play.Server.UPDATE_ENTITY_NBT
						&& pt != PacketType.Play.Server.MAP_CHUNK_BULK) {
					/*
					 * if (test == 76) { Bukkit.broadcastMessage("Stopping at " + pt.name()); break;
					 * } test++;
					 */
					protocolManager.addPacketListener(new PacketAdapter(thi, ListenerPriority.HIGHEST, pt) {

						@Override
						public void onPacketSending(final PacketEvent event) {
							if (event.isCancelled())
								return;
							try {
								event.getPlayer().getUniqueId();
							} catch (Error | Exception e4) {
								return;
							}
							if (getHolder(event.getPlayer().getUniqueId()) != null) {
								event.setCancelled(true);
								List<PacketContainer> cc = temppLocations.get(event.getPlayer().getUniqueId());
								if (cc == null)
									cc = new ArrayList<>();
								cc.add(event.getPacket());
								temppLocations.put(event.getPlayer().getUniqueId(), cc);

							}
						}
					});
				}
		} catch (Error | Exception e4) {
			e4.printStackTrace();
		}

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
								try {
									PacketContainer pc2 = protocolManager
											.createPacket(PacketType.Play.Server.UNLOAD_CHUNK);
									pc2.getIntegers().write(0, event.getPlayer().getLocation().getChunk().getX())
											.write(1, event.getPlayer().getLocation().getChunk().getZ());

									protocolManager.sendServerPacket(event.getPlayer(), pc2);
								} catch (InvocationTargetException e) {
									// e.printStackTrace();
								}

								for (int x = event.getPlayer().getLocation().getChunk().getX() - 8; x < event
										.getPlayer().getLocation().getChunk().getX() + 8; x++) {
									for (int z = event.getPlayer().getLocation().getChunk().getZ() - 8; z < event
											.getPlayer().getLocation().getChunk().getZ() + 8; z++) {
										if (x == event.getPlayer().getLocation().getChunk().getX()
												&& z == event.getPlayer().getLocation().getChunk().getZ())
											continue;
										try {
											PacketContainer pc = protocolManager
													.createPacket(PacketType.Play.Server.UNLOAD_CHUNK);
											pc.getIntegers().write(0, x).write(1, z);
											protocolManager.sendServerPacket(event.getPlayer(), pc);
										} catch (InvocationTargetException e) {
											// e.printStackTrace();
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

								// if (true|| getHolder(event.getPlayer().getUniqueId()) == null) {
								// String goingTo = null;
								boolean overworld = false;
								try {
									int id = event.getPacket().getIntegers().read(0);
									overworld = (id == 0);
									// goingTo = "" + id;

								} catch (Error | Exception e45) {
									Object o = event.getPacket().getModifier().readSafely(0);
									// goingTo = o.toString();
									overworld = /* (goingTo = o.toString()) */o.toString().endsWith("overworld");
								}
								final Location previous = event.getPlayer().getLocation().clone();
								final Environment e = previous.getWorld().getEnvironment();
								// Bukkit.broadcastMessage(
								// "going to --->" + goingTo + " (" + previous.getWorld().getName() + ")");
								// if (nether) {
								/*
								 * if ((previous.getBlock().getType().name().contains("PORTAL")) ||
								 * (previous.getBlock().getType().name().contains("GATEWAY"))) {
								 * Bukkit.broadcastMessage("In portal when occurs. Assume its natural.");
								 * return; } else {
								 * 
								 * Bukkit.broadcastMessage("name = " + previous.getBlock().getType().name()); }
								 */
								if (quickSkippers.contains(event.getPlayer().getUniqueId())) {
									if (overworld) {
										// Bukkit.broadcastMessage("This is the backwards packet. Cancel it");
										event.setCancelled(true);
									} else
										// Bukkit.broadcastMessage("quick skip skip");
										return;
								}

								if (getHolder(event.getPlayer().getUniqueId()) != null) {
									// Bukkit.broadcastMessage("Both were going to be sent. Ignore it");
									setHolder(event.getPlayer().getUniqueId(), null);
									quickSkippers.remove(event.getPlayer().getUniqueId());
									final boolean isRaining = event.getPlayer().getWorld().isThundering();
									new BukkitRunnable() {

										@Override
										public void run() {
											PacketContainer pc = new PacketContainer(
													PacketType.Play.Server.UPDATE_TIME);
											pc.getLongs().write(0, event.getPlayer().getWorld().getFullTime());
											pc.getLongs().write(1, event.getPlayer().getWorld().getTime());
											try {
												protocolManager.sendServerPacket(event.getPlayer(), pc);
											} catch (InvocationTargetException e) {
												e.printStackTrace();
											}

											if (isRaining != event.getPlayer().getWorld().isThundering()) {
												PacketContainer pc2 = new PacketContainer(
														PacketType.Play.Server.GAME_STATE_CHANGE);
												pc2.getModifier().write(0,
														event.getPlayer().getWorld().isThundering() ? 2 : 1);
												try {
													protocolManager.sendServerPacket(event.getPlayer(), pc2);
												} catch (InvocationTargetException e) {
													e.printStackTrace();
												}
											}
										}
									}.runTaskLater(thi, 1);
									return;
								}

								if ((e != Environment.NORMAL)) {
									// Bukkit.broadcastMessage("Not normal. go");
									return;
								}
								setHolder(event.getPlayer().getUniqueId(), event.getPacket());
								// Bukkit.broadcastMessage("canceling");
								event.setCancelled(true);
								new BukkitRunnable() {

									@Override
									public void run() {
										if (getHolder(event.getPlayer().getUniqueId()) != null) {
											quickSkippers.add(event.getPlayer().getUniqueId());
											// Bukkit.broadcastMessage("times up. Send real");
											try {
												protocolManager.sendServerPacket(event.getPlayer(),
														getHolder(event.getPlayer().getUniqueId()));
											} catch (InvocationTargetException e1) {
												e1.printStackTrace();
											}
											// Location temp = event.getPlayer().getLocation().clone();
											// event.getPlayer().teleport(previous);
											// event.getPlayer().teleport(temp);
											quickSkippers.remove(event.getPlayer().getUniqueId());
											setHolder(event.getPlayer().getUniqueId(), null);
											if (temppLocations.get(event.getPlayer().getUniqueId()) != null) {
												// Bukkit.broadcastMessage("cc "
												// + temppLocations.get(event.getPlayer().getUniqueId()).size());
												for (int i = 0; i < temppLocations.get(event.getPlayer().getUniqueId())
														.size(); i++) {
													try {
														protocolManager.sendServerPacket(event.getPlayer(),
																temppLocations.get(event.getPlayer().getUniqueId())
																		.get(i));
													} catch (InvocationTargetException e1) {
														e1.printStackTrace();
													}
												}
											}
											temppLocations.get(event.getPlayer().getUniqueId()).clear();
											temppLocations.remove(event.getPlayer().getUniqueId());

											new BukkitRunnable() {

												@Override
												public void run() {
													// quickSkippers.remove(event.getPlayer().getUniqueId());
												}
											}.runTaskLater(thi, 0);
										}
									}
								}.runTaskLater(thi, 0);
							}

							// nether
							/*
							 * new BukkitRunnable() {
							 * 
							 * @Override public void run() { if (getHolder(event.getPlayer().getUniqueId())
							 * != null) { final Location temp = event.getPlayer().getLocation(); if
							 * (temp.getWorld().getEnvironment() == Environment.NETHER) { event.getPlayer()
							 * .teleport(Bukkit.getWorlds().get(0).getSpawnLocation()); new BukkitRunnable()
							 * {
							 * 
							 * @Override public void run() { event.getPlayer().teleport(temp); }
							 * }.runTaskLater(thi, 0); } setHolder(event.getPlayer().getUniqueId(), null); }
							 * } }.runTaskLater(thi, 0);
							 */
							// }
							/*
							 * } else { //setHolder(event.getPlayer().getUniqueId(), null);
							 * event.setCancelled(true); }
							 */

						}
					}
				});
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onWorldChange(final PlayerChangedWorldEvent e) {
		if (e.getPlayer().getWorld().getEnvironment() == e.getFrom().getEnvironment()
				&& (!e.getFrom().getName().toLowerCase().contains("nether")
						&& !e.getFrom().getName().toLowerCase().contains("the_end")
						&& (!e.getPlayer().getWorld().getName().toLowerCase().contains("nether")
								&& !e.getPlayer().getWorld().getName().toLowerCase().contains("the_end")))) {
			if (e.getPlayer().getWorld() != e.getFrom())
				if (barredPlayers.contains(e.getPlayer().getUniqueId()))
					barredPlayers.remove(e.getPlayer().getUniqueId());
				else
					playersGoingToSametype.add(e.getPlayer().getUniqueId());

			// Only do this effect if the world they are going to does not have a WCS.
			if (thi.tempNausea || thi.tempBlind || thi.tempSlowness) {
				new BukkitRunnable() {
					public void run() {
						if (thi.tempBlind) {
							e.getPlayer().addPotionEffect(
									new PotionEffect(PotionEffectType.BLINDNESS, (int) (20 * thi.effectDelay), 2));
						}
						if (thi.tempNausea) {
							e.getPlayer().addPotionEffect(
									new PotionEffect(PotionEffectType.CONFUSION, (int) (20 * thi.effectDelay), 2));
						}
						if (thi.tempSlowness) {
							e.getPlayer().addPotionEffect(
									new PotionEffect(PotionEffectType.SLOW, (int) (20 * thi.effectDelay) - 5, 2));
						}
					}
				}.runTaskLater(thi, 5);
			}

		}

		if (thi.enableTitles) {
			from.put(e.getPlayer().getUniqueId(), MultiVerseSupporter.getAlias(e.getFrom()));
			try {
				String text = PlaceholderAPI.setPlaceholders(e.getPlayer(), thi.title);
				String subtext = PlaceholderAPI.setPlaceholders(e.getPlayer(), thi.subtitle);
				e.getPlayer().sendTitle(text, subtext, ((int) thi.fadeInSeconds * 20), ((int) thi.staySeconds * 20),
						((int) thi.fadeOutSeconds * 20));
			} catch (Error | Exception e4) {
				e.getPlayer().sendTitle(thi.title, thi.subtitle, ((int) thi.fadeInSeconds * 20),
						((int) thi.staySeconds * 20), ((int) thi.fadeOutSeconds * 20));
			}
		}

		if (thi.enableDelayedTitles) {
			new BukkitRunnable() {

				public void run() {

					from.put(e.getPlayer().getUniqueId(), MultiVerseSupporter.getAlias(e.getFrom()));
					try {
						String text = PlaceholderAPI.setPlaceholders(e.getPlayer(), thi.title2);
						String subtext = PlaceholderAPI.setPlaceholders(e.getPlayer(), thi.subtitle2);
						e.getPlayer().sendTitle(text, subtext, ((int) thi.fadeInSeconds2 * 20),
								((int) thi.staySeconds2 * 20), ((int) thi.fadeOutSeconds2 * 20));
					} catch (Error | Exception e4) {
						e.getPlayer().sendTitle(thi.title2, thi.subtitle2, ((int) thi.fadeInSeconds2 * 20),
								((int) thi.staySeconds2 * 20), ((int) thi.fadeOutSeconds2 * 20));
					}
				}

			}.runTaskLater(thi, thi.delayedTitlesdelay);
		}

	}

	@EventHandler
	public void onListener(PlayerDeathEvent e) {
		respawnredPlayers.add(e.getEntity().getUniqueId());
	}
}
