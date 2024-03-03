package me.melonboy10.blockphysics;

import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Interaction;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.joml.Vector3d;

import static me.melonboy10.blockphysics.PhysicsWorld.*;

@DefaultQualifier(NonNull.class)
public final class BlockPhysicsPlugin extends JavaPlugin implements Listener {

  public static BlockPhysicsPlugin PLUGIN;

  @Override
  public void onEnable() {
    PLUGIN = this;
    new PhysicsWorld();
    this.getServer().getPluginManager().registerEvents(this, this);

    System.out.println(Bukkit.getWorlds().get(0));

    //    if (Bukkit.getOnlinePlayers().size() > 0) {
    //      //      world.addEntity(new FloorPlane(Bukkit.getPlayer("melonboy10").getLocation(), 0.5f,
    //      // 25));
    //      //      Bukkit.getOnlinePlayers().forEach(player -> world.addEntity(new
    //      // PhysicsPlayer(player)));
    //      Bukkit.getOnlinePlayers().forEach(player -> world.addEntity(new PhysicsBlock(player.getLocation(), Material.GLASS.createBlockData(), false)));
    //    }

    new CommandTree("physics")
      .executesPlayer((player, args) -> {
        player.teleport(player.getLocation().clone().add(0.1, 0.1, 0.1));
      })
      .then(new LiteralArgument("stop")
              .executesPlayer((player, args) -> {
                STOP_NOW = true;
              }))
      .then(new LiteralArgument("start")
              .executesPlayer((player, args) -> {
                STOP_NOW = false;
              }))

      .then(new LiteralArgument("step")
              .then(new IntegerArgument("steps").setOptional(true)
                      .executesPlayer((player, args) -> {
                        int steps = (int) args.getOrDefault("steps", 1);
                        STOP_NOW = false;
                        for (int i = 0; i < steps; i++)
                          update();
                        STOP_NOW = true;
                      })))
      .then(new LiteralArgument("debug")
              .executesPlayer((player, args) -> {
                DEBUG = !DEBUG;
              })
              .then(new IntegerArgument("level")
                      .executesPlayer((player, args) -> {
                        int value = (int) args.get("level");
                        if (value == DEBUG_LEVEL) DEBUG = !DEBUG;
                        DEBUG_LEVEL = value;
                      })))
      .then(new LiteralArgument("time")
              .then(new FloatArgument("deltaTime")
                      .executesPlayer((player, args) -> {
                        DELTA_TIME = (float) args.get("deltaTime");
                      })))
      .then(new LiteralArgument("gravity")
              .then(new FloatArgument("gravity")
                      .executesPlayer((player, args) -> {
                        GRAVITY = (float) args.get("gravity");
                      })))
      .then(
        new LiteralArgument("summon")
          .executesPlayer((player, args) -> {
            System.out.println("Summoning block");
            Material mat = null;
            while (mat == null || !mat.isBlock())
              mat = Material.values()[(int) (Math.random() * Material.values().length)];
            addEntity(new PhysicsBlock(player.getLocation(), mat.createBlockData()));
          })
          .then(new BlockStateArgument("block")
                  .executesPlayer((player, args) -> {
                    System.out.println("Summoning block");
                    addEntity(new PhysicsBlock(player.getLocation(), (BlockData) args.get("block")));
                  })
                  .then(new BooleanArgument("active")
                          .executesPlayer((player, args) -> {
                            PhysicsBlock block = new PhysicsBlock(player.getLocation(), (BlockData) args.get("block"));
                            block.active = (boolean) args.get("active");
                            addEntity(block);
                          }))
          ))
      .then(
        new LiteralArgument("emitter")
          .then(new IntegerArgument("rate").setOptional(true)
                  .then(new BooleanArgument("facing").setOptional(true)
                          .then(new BlockStateArgument("block").setOptional(true)
                                  .executesPlayer((player, args) -> {
                                    new BukkitRunnable() {
                                      final Location location = player.getLocation();
                                      final boolean facing = (boolean) args.getOrDefault("facing", false);
                                      final BlockData block = args.get("block") != null ? (BlockData) args.get("block") : null;

                                      @Override
                                      public void run() {
                                        if (STOP_NOW) return;

                                        Material mat = null;
                                        while ((mat == null || !mat.isBlock()) && block == null)
                                          mat = Material.values()[(int) (Math.random() * Material.values().length)];
                                        PhysicsBlock entity = new PhysicsBlock(location, block != null ? block : mat.createBlockData());
                                        if (facing) {
                                          entity.velocity = new Vector3d(location.getDirection().multiply(10).toVector3f());
                                        } else {
                                          entity.velocity = new Vector3d((float) (Math.random() - 0.5) * 10, 10, (float) (Math.random() - 0.5) * 10);
                                        }
                                        addEntity(entity);
                                      }
                                    }.runTaskTimer(this, 0, (int) args.getOrDefault("rate", 20));
                                  }))
                  )))
      .then(new LiteralArgument("grid")
              .executesPlayer((player, args) -> {
                for (int i = 0; i < 5; i++) {
                  for (int j = 0; j < 5; j++) {
                    Material mat = null;
                    while (mat == null || !mat.isBlock())
                      mat = Material.values()[(int) (Math.random() * Material.values().length)];
                    addEntity(new PhysicsBlock(player.getLocation().add(i * 1.1, 0, j * 1.1), mat.createBlockData()));
                  }
                }
              })
              .then(new BlockStateArgument("block")
                      .executesPlayer((player, args) -> {
                        for (int i = 0; i < 5; i++) {
                          for (int j = 0; j < 5; j++) {
                            addEntity(new PhysicsBlock(player.getLocation().add(i * 1.1, 0, j * 1.1), (BlockData) args.get("block")));
                          }
                        }
                      })
                      .then(new IntegerArgument("width")
                              .executesPlayer((player, args) -> {
                                int w = (int) args.get("width");
                                for (int i = 0; i < w; i++) {
                                  for (int j = 0; j < w; j++) {
                                    addEntity(new PhysicsBlock(player.getLocation().add(i * 1.1, 0, j * 1.1), (BlockData) args.get("block")));
                                  }
                                }
                              }))))
      .then(new LiteralArgument("floor")
              .executesPlayer((player, args) -> {
                addEntity(new FloorPlane(player.getLocation(), 10));
              }).then(new IntegerArgument("scale")
                        .executesPlayer((player, args) -> {
                          addEntity(new FloorPlane(player.getLocation(), (int) args.get("scale")));
                        })))
      .then(new LiteralArgument("test")
              .executesPlayer((player, args) -> {
                DEBUG_LEVEL = 2;
                DEBUG = true;
                GRAVITY = 0;

                addEntity(new PhysicsBlock(player.getLocation().add(1, 1, 0), Material.COBWEB.createBlockData()) {{
                  angularMomentum = new Vector3d(1, 0, 0);
                }});
                addEntity(new PhysicsBlock(player.getLocation().add(-1, 1, 0), Material.COBWEB.createBlockData()) {{
                  angularMomentum = new Vector3d(-1, 0, 0);
                }});
                addEntity(new PhysicsBlock(player.getLocation().add(0, 1, 1), Material.COBWEB.createBlockData()) {{
                  angularMomentum = new Vector3d(0, 0, 1);
                }});
                addEntity(new PhysicsBlock(player.getLocation().add(0, 1, -1), Material.COBWEB.createBlockData()) {{
                  angularMomentum = new Vector3d(0, 0, -1);
                }});


                //          PhysicsWorld.addEntity(new BlockGroup(player.getLocation(), new BlockData[][][] {
                //            {
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //            },
                //            {
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //            },
                //            {
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() },
                //              { Material.STONE.createBlockData(), Material.STONE.createBlockData(), Material.STONE.createBlockData() }
                //            }
                //          }));
              }))
      .then(new LiteralArgument("area")
              .then(new LocationArgument("location1")
                      .then(new LocationArgument("location2")
                              .executesPlayer((player, args) -> {
                                Location loc1 = (Location) args.get("location1");
                                Location loc2 = (Location) args.get("location2");
                                int minX = Math.min(loc1.getBlockX(), loc2.getBlockX());
                                int minY = Math.min(loc1.getBlockY(), loc2.getBlockY());
                                int minZ = Math.min(loc1.getBlockZ(), loc2.getBlockZ());
                                int maxX = Math.max(loc1.getBlockX(), loc2.getBlockX());
                                int maxY = Math.max(loc1.getBlockY(), loc2.getBlockY());
                                int maxZ = Math.max(loc1.getBlockZ(), loc2.getBlockZ());
                                BlockData[][][] blockData = new BlockData[maxX - minX + 1][maxY - minY + 1][maxZ - minZ + 1];
                                for (int x = minX; x <= maxX; x++) {
                                  for (int y = minY; y <= maxY; y++) {
                                    for (int z = minZ; z <= maxZ; z++) {
                                      Block block = player.getWorld().getBlockAt(x, y, z);
                                      blockData[x - minX][y - minY][z - minZ] = block.getBlockData();
                                    }
                                  }
                                }
                                //              PhysicsWorld.addEntity(new BlockGroup(player.getLocation(), blockData));
                              }))))
      .then(new LiteralArgument("particles")
              .executesPlayer((player, args) -> {
                new BukkitRunnable() {
                  Location location = player.getLocation();

                  @Override
                  public void run() {
                    Particle[] values = Particle.values();
                    for (int i = 0; i < values.length; i++) {
                      Particle particle = values[i];
                      location.getWorld().spawnParticle(particle, location.clone().add(i % 10 * 5, 0, i / 10 * 5), 1);
                    }
                  }
                }.runTaskTimer(this, 0, 1);
              }))
      .register();

    new BukkitRunnable() {
      @Override
      public void run() {
        PhysicsWorld.update();
      }
    }.runTaskTimer(this, 0, 1);
  }

  @Override
  public void onDisable() {
    super.onDisable();
    PhysicsWorld.clear();
  }

  @Nullable
  public PhysicsEntity playerMovingBlock = null;

  @EventHandler
  public void onEntityInteract(PlayerInteractEntityEvent event) {
    if (event.getHand() == EquipmentSlot.OFF_HAND) return;
    if (event.getRightClicked() instanceof Interaction) {
      if (playerMovingBlock != null) {
        playerMovingBlock = null;
        return;
      }

      for (PhysicsEntity entity : entities) {
        if (entity instanceof InteractiveEntity && ((InteractiveEntity) entity).getInteraction().equals(event.getRightClicked())) {
          playerMovingBlock = entity;
          return;
        }
      }
    }
  }

  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {
    if (playerMovingBlock != null) {
      playerMovingBlock.centerOfMass = event.getPlayer().getLocation().getDirection().toVector3d().mul(2).add(event.getPlayer().getEyeLocation().toVector().toVector3d());
      playerMovingBlock.velocity.set(0);
    }
  }
}
