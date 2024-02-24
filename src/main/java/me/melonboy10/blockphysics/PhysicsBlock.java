package me.melonboy10.blockphysics;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3d;
import org.joml.Vector3f;

public class PhysicsBlock extends PhysicsEntity {
  BlockDisplay blockDisplay;
  TextDisplay debugDisplay;

  public PhysicsBlock(Location position, BlockData blockData) {
    super(position);


    blockDisplay = createBlockDisplay(position.clone().subtract(0.5, 0.5, 0.5), blockData);
    debugDisplay = createTextDisplay(position.clone().add(0, 1, 0), "");


    //    Transformation transform =  blockDisplay.getTransformation();
    //    transform.getScale().set(scale);
    //    blockDisplay.setTransformation(transform);
  }

  public PhysicsBlock(Location position, BlockData blockData, boolean active) {
    this(position, blockData);
    this.active = active;
  }

  @Override
  public void update() {
    super.update();

    blockDisplay.teleport(new Location(blockDisplay.getWorld(), centerOfMass.x - 0.5, centerOfMass.y - 0.5, centerOfMass.z - 0.5));

    Transformation transform = blockDisplay.getTransformation();
    transform.getLeftRotation().set(rotation);
    //    transform.getTranslation().set(-0.5, -0.5, -0.5);
    blockDisplay.setTransformation(transform);

    debugDisplay.teleport(new Location(debugDisplay.getWorld(), centerOfMass.x, centerOfMass.y + 1, centerOfMass.z));

    if (PhysicsWorld.DEBUG && PhysicsWorld.DEBUG_LEVEL >= 5)
      debugDisplay.setText("""
        Position: %s
        Velocity: %s
        Angular Momentum: %s
        Rotate Momentum: %s
        Inertia Tensor: %s
        Angular Velocity: %s          
        """.formatted(centerOfMass, velocity, angularMomentum, new Vector3d(angularMomentum).rotate(rotation.invert()), (mesh.getInverseInertiaTensor()), new Vector3d(angularMomentum).rotate(rotation.invert()).mul(mesh.getInverseInertiaTensor()).rotate(rotation)));
    else debugDisplay.setText("");

    //    blockDisplay.teleport(new Location(blockDisplay.getWorld(), centerOfMass.x - 0.5, centerOfMass.y - 0.5, centerOfMass.z - 0.5));


    //    Vector3f centerOffset = new Vector3f(centerOfMass).rotate(rotation);
    //        System.out.println("centerOffset = " + centerOffset);
    //
    //    Transformation transform = blockDisplay.getTransformation();
    //    transform.getLeftRotation().set(rotation);
    //    blockDisplay.setTransformation(transform);

    //        Vector3f rotatedCenter = new Quaternionf(rotation).transform(centerOffset);
    //        Vector3f translation = new Vector3f(centerOfMass).sub(rotatedCenter);
    //    System.out.println("translation = " + translation);
    //
    //    System.out.println("position = " + position);
    //        if (Float.isNaN(position.x()) || Float.isNaN(velocity.x()) || Float.isNaN(angularVelocity.x())) {
    //          blockDisplay.setBlock(Material.REDSTONE_BLOCK.createBlockData());
    //          STOP_NOW = true;
    //        }
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
    debugDisplay.remove();
  }
}
