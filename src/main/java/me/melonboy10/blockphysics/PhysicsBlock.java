package me.melonboy10.blockphysics;

import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4d;
import org.joml.AxisAngle4f;
import org.joml.Quaterniond;
import org.joml.Vector3d;

public class PhysicsBlock extends PhysicsEntity implements InteractiveEntity {
  BlockDisplay blockDisplay;
  TextDisplay debugDisplay;
  Interaction interaction;

  public PhysicsBlock(Location position, BlockData blockData) {
    super(position);

    blockDisplay = createBlockDisplay(position.clone().subtract(0.5, 0.5, 0.5), blockData);
    debugDisplay = createTextDisplay(position.clone().add(0, 1, 0), "");
    interaction = createInteraction(blockDisplay);
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
    //    transform.getLeftRotation().set(new AxisAngle4f().set(rotationMatrix));
    transform.getLeftRotation().set(rotation);
    transform.getTranslation().set(new Vector3d(-0.5, -0.5, -0.5).rotate(rotation).add(0.5, 0.5, 0.5));
    //    transform.getTranslation().set(0.5, 0.5, 0.5)/*.add(0, (float) Math.sin(rotationAngles.x + Math.PI / 4), (float) Math.sin(rotationAngles.x + 3 * Math.PI / 4));*/;
    //    transform.getRightRotation().set(rotation);
    blockDisplay.setTransformation(transform);

    debugDisplay.teleport(new Location(debugDisplay.getWorld(), centerOfMass.x, centerOfMass.y + 1, centerOfMass.z));
    interaction.teleport(new Location(interaction.getWorld(), centerOfMass.x, centerOfMass.y - 0.5, centerOfMass.z));

    if (PhysicsWorld.DEBUG && PhysicsWorld.DEBUG_LEVEL >= 5)
      debugDisplay.setText("""
        Position: %s
        Velocity: %s
        Angular Momentum: %s
        Rotate Momentum: %s
        Inertia Tensor: %s
        Angular Velocity: %s          
        """.formatted(centerOfMass, velocity, "angularMomentum", new Vector3d(angularMomentum).rotate(rotation.invert()), (mesh.getInverseInertiaTensor()), new Vector3d(angularMomentum).rotate(rotation.invert()).mul(mesh.getInverseInertiaTensor()).rotate(rotation)));
    else debugDisplay.setText("");
  }

  @Override
  public void destroy() {
    blockDisplay.remove();
    interaction.remove();
    debugDisplay.remove();
  }

  @Override
  public Interaction getInteraction() {
    return interaction;
  }
}
