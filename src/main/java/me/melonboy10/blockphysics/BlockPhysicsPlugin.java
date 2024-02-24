package me.melonboy10.blockphysics;

import com.destroystokyo.paper.brigadier.BukkitBrigadierCommandSource;
import com.destroystokyo.paper.event.brigadier.CommandRegisteredEvent;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;

import java.util.function.Consumer;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.joml.Vector3d;
import org.joml.Vector3f;

import static me.melonboy10.blockphysics.PhysicsWorld.DELTA_TIME;
import static me.melonboy10.blockphysics.PhysicsWorld.STEPS_PER_SECOND;
import static net.kyori.adventure.text.Component.text;
import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@DefaultQualifier(NonNull.class)
public final class BlockPhysicsPlugin extends JavaPlugin implements Listener {

  public static BlockPhysicsPlugin PLUGIN;
  public static final PhysicsWorld world = new PhysicsWorld();

  @Override
  public void onEnable() {
    PLUGIN = this;
    this.getServer().getPluginManager().registerEvents(this, this);
    System.out.println(Bukkit.getWorlds().get(0));
    PhysicsWorld.WORLD = Bukkit.getWorlds().get(0);
    PhysicsWorld.debugDisplay = PhysicsEntity.createTextDisplay(Bukkit.getOnlinePlayers().stream().findFirst().get().getLocation(), "");

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
                PhysicsWorld.STOP_NOW = true;
              }))
      .then(new LiteralArgument("start")
              .executesPlayer((player, args) -> {
                PhysicsWorld.STOP_NOW = false;
              }))

      .then(new LiteralArgument("step")
              .then(new IntegerArgument("steps").setOptional(true)
                      .executesPlayer((player, args) -> {
                        int steps = (int) args.getOrDefault("steps", 1);
                        for (int i = 0; i < steps; i++)
                          world.update();
                      })))
      .then(new LiteralArgument("debug")
              .executesPlayer((player, args) -> {
                PhysicsWorld.DEBUG = !PhysicsWorld.DEBUG;
              })
              .then(new IntegerArgument("level")
                      .executesPlayer((player, args) -> {
                        int value = (int) args.get("level");
                        value = Math.max(1, Math.min(5, value));
                        if (value == PhysicsWorld.DEBUG_LEVEL) PhysicsWorld.DEBUG = !PhysicsWorld.DEBUG;
                        PhysicsWorld.DEBUG_LEVEL = value;
                      })))
      .then(new LiteralArgument("time")
              .then(new FloatArgument("deltaTime")
                      .executesPlayer((player, args) -> {
                        DELTA_TIME = (float) args.get("deltaTime");
                      })))
      .then(new LiteralArgument("gravity")
              .then(new FloatArgument("gravity")
                      .executesPlayer((player, args) -> {
                        PhysicsWorld.GRAVITY = (float) args.get("gravity");
                      })))
      .then(
        new LiteralArgument("summon")
          .executesPlayer((player, args) -> {
            System.out.println("Summoning block");
            Material mat = null;
            while (mat == null || !mat.isBlock())
              mat = Material.values()[(int) (Math.random() * Material.values().length)];
            world.addEntity(new PhysicsBlock(player.getLocation(), mat.createBlockData()));
          })
          .then(new BlockStateArgument("block")
                  .executesPlayer((player, args) -> {
                    System.out.println("Summoning block");
                    world.addEntity(new PhysicsBlock(player.getLocation(), (BlockData) args.get("block")));
                  })
                  .then(new BooleanArgument("active")
                          .executesPlayer((player, args) -> {
                            PhysicsBlock block = new PhysicsBlock(player.getLocation(), (BlockData) args.get("block"));
                            block.active = (boolean) args.get("active");
                            world.addEntity(block);
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
                                        if (PhysicsWorld.STOP_NOW) return;

                                        Material mat = null;
                                        while ((mat == null || !mat.isBlock()) && block == null)
                                          mat = Material.values()[(int) (Math.random() * Material.values().length)];
                                        PhysicsBlock entity = new PhysicsBlock(location, block != null ? block : mat.createBlockData());
                                        if (facing) {
                                          entity.velocity = new Vector3d(location.getDirection().multiply(10).toVector3f());
                                        } else {
                                          entity.velocity = new Vector3d((float) (Math.random() - 0.5) * 10, 10, (float) (Math.random() - 0.5) * 10);
                                        }
                                        world.addEntity(entity);
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
                    world.addEntity(new PhysicsBlock(player.getLocation().add(i * 1.1, 0, j * 1.1), mat.createBlockData()));
                  }
                }
              })
              .then(new BlockStateArgument("block")
                      .executesPlayer((player, args) -> {
                        for (int i = 0; i < 5; i++) {
                          for (int j = 0; j < 5; j++) {
                            world.addEntity(new PhysicsBlock(player.getLocation().add(i * 1.1, 0, j * 1.1), (BlockData) args.get("block")));
                          }
                        }
                      })
                      .then(new IntegerArgument("width")
                              .executesPlayer((player, args) -> {
                                int w = (int) args.get("width");
                                for (int i = 0; i < w; i++) {
                                  for (int j = 0; j < w; j++) {
                                    world.addEntity(new PhysicsBlock(player.getLocation().add(i * 1.1, 0, j * 1.1), (BlockData) args.get("block")));
                                  }
                                }
                              }))))
      .then(new LiteralArgument("floor")
              .executesPlayer((player, args) -> {
                world.addEntity(new FloorPlane(player.getLocation(), 0.5f, 10));
              }).then(new IntegerArgument("scale")
                        .executesPlayer((player, args) -> {
                          world.addEntity(new FloorPlane(player.getLocation(), 0.5f, (int) args.get("scale")));
                        })))
      .then(new LiteralArgument("test")
              .executesPlayer((player, args) -> {
                //          world.addEntity(new BlockGroup(player.getLocation(), new BlockData[][][] {
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
                                //              world.addEntity(new BlockGroup(player.getLocation(), blockData));
                              }))))
      .register();

    new BukkitRunnable() {
      @Override
      public void run() {
        if (!PhysicsWorld.STOP_NOW)
          for (int i = 0; i < STEPS_PER_SECOND; i++) {
            world.update();
          }
      }
    }.runTaskTimer(this, 0, 1);
  }

  @Override
  public void onDisable() {
    super.onDisable();
    world.clear();
  }
}
