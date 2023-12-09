package me.melonboy10.blockphysics;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Display;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.NotNull;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static me.melonboy10.blockphysics.PhysicsWorld.STOP_NOW;

public class PhysicsBlock extends PhysicsEntity {
  BlockDisplay blockDisplay;

  public PhysicsBlock(Location position, BlockData blockData) {
    super(position);


    blockDisplay = createBlockDisplay(position, blockData);
    blockDisplay.setInterpolationDuration(1);
//    Transformation transform =  blockDisplay.getTransformation();
//    transform.getScale().set(scale);
//    blockDisplay.setTransformation(transform);
  }

  public PhysicsBlock(Location position, BlockData blockData, boolean active) {
    super(position);
    this.active = active;

    blockDisplay = createBlockDisplay(position, blockData);
  }

  @Override
  public void update() {
    super.update();

    blockDisplay.teleport(new Location(blockDisplay.getWorld(), centerOfMass.x - 0.5, centerOfMass.y - 0.5, centerOfMass.z - 0.5));



//    Vector3f centerOffset = new Vector3f(centerOfMass).add(position).rotate(rotation);
//    System.out.println("centerOffset = " + centerOffset);
//
//    Transformation transform = blockDisplay.getTransformation();
//    transform.getLeftRotation().set(rotation);
//    blockDisplay.setTransformation(transform);

//    Vector3f rotatedCenter = new Quaternionf(rotation).transform(centerOffset);
//    Vector3f translation = new Vector3f(position).sub(rotatedCenter);
//    System.out.println("translation = " + translation);
//
//    System.out.println("position = " + position);
//    if (Float.isNaN(position.x()) || Float.isNaN(velocity.x()) || Float.isNaN(angularVelocity.x())) {
//      blockDisplay.setBlock(Material.REDSTONE_BLOCK.createBlockData());
//      STOP_NOW = true;
//    }
//    else blockDisplay.teleport(new Location(blockDisplay.getWorld(), translation.x, translation.y, translation.z));
//    blockDisplay.teleport(new Location(blockDisplay.getWorld(), position.x, position.y, position.z));

//    Location lookTo = Bukkit.getPlayer("melonboy10").getLocation();
//    Vec3 lookToVec = new Vec3(lookTo.getX(), lookTo.getY(), lookTo.getZ());
//    Vec3 lookFromVec = new Vec3(position.x, position.y, position.z);
//    Vec3 lookVec = lookToVec.subtract(lookFromVec);
//    Vec3 lookVecNormalized = lookVec.normalize();
//    rotation = new Quaternionf(lookVecNormalized.x, lookVecNormalized.y, lookVecNormalized.z, 0);
  }

  @Override
  public void destroy() {
    blockDisplay.remove();
  }
}
