package me.melonboy10.blockphysics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;

public class FloorPlane extends PhysicsEntity {
  BlockDisplay blockDisplay;

  public FloorPlane(Location position, int size) {
    super(position, new Mesh(new BoundingBox[]{new BoundingBox(-size, -0.5f, -size, size, 0.5f, size)}));
    this.active = false;
    this.elasticity = 0;

    blockDisplay = createBlockDisplay(position, Material.WHITE_STAINED_GLASS.createBlockData());
    Transformation transform = blockDisplay.getTransformation();
    transform.getScale().set(size * 2, 1, size * 2);
    transform.getTranslation().set(-size, -0.5, -size);
    blockDisplay.setTransformation(transform);
  }

  @Override
  public void destroy() {
    blockDisplay.remove();
  }
}
