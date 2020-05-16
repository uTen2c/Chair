package dev.fslc.chair;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.material.Stairs;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import java.util.HashMap;
import java.util.Map;

public class ChairListener implements Listener {

    private static final String CHAIR_NAME = "dev.fslc.chair";
    private final Plugin plugin;
    private final Map<Entity, Long> lastUsed = new HashMap<>();

    public ChairListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getHand() != EquipmentSlot.HAND || event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block block = event.getClickedBlock();
        if (isStair(block)) {
            return;
        }

        if (System.currentTimeMillis() - lastUsed.getOrDefault(player, 0L) <= 500) {
            return;
        }

        if (player.isSneaking() || player.getVehicle() != null) {
            return;
        }

        Stairs data = (Stairs) block.getState().getData();
        BlockFace face = data.getFacing();
        Block topBlock = block.getLocation().clone().add(0, 1, 0).getBlock();
        if (topBlock.getType() != Material.AIR || data.isInverted()) {
            return;
        }

        Vector vec = (new Vector(face.getModX(), face.getModY(), face.getModZ()));
        player.getWorld().spawn(block.getLocation().clone().add(0.5, -0.5, 0.5), ArmorStand.class, s -> {
            s.setCustomName(CHAIR_NAME);
            s.setSmall(true);
            s.setGravity(false);
            s.setVisible(false);
            Location loc = s.getLocation().setDirection(vec);
            loc.add(loc.getDirection().multiply(0.15));
            s.teleport(loc);
            s.addPassenger(player);
        });
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        Entity entity = event.getDismounted();
        if (isChair(entity)) {
            entity.getPassengers().forEach(e -> {
                lastUsed.put(e, System.currentTimeMillis());
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location loc = entity.getLocation().clone().add(0, 1.75, 0);
                        loc.setYaw(e.getLocation().getYaw());
                        loc.setPitch(e.getLocation().getPitch());
                        e.teleport(loc);
                    }
                }.runTaskLater(plugin, 1);
            });
            entity.remove();
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                Player player = event.getPlayer();
                Entity vehicle = player.getVehicle();
                if (isChair(vehicle) && isStair(player.getLocation().getBlock())) {
                    vehicle.remove();
                }
            }
        }.runTaskLater(plugin, 1);
    }

    private boolean isChair(Entity entity) {
        return entity instanceof ArmorStand && entity.getCustomName().equals(CHAIR_NAME);
    }

    private boolean isStair(Block block) {
        return block.getType().getData() != Stairs.class;
    }
}
