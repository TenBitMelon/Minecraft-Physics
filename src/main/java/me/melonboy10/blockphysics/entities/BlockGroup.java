package me.melonboy10.blockphysics.entities;

import me.melonboy10.blockphysics.core.PhysicsEntity;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Vector3d;

import java.util.ArrayList;

import static me.melonboy10.blockphysics.core.PhysicsWorld.createBlockDisplay;

public class BlockGroup extends PhysicsEntity {

  BlockDisplay[][][] blocks;

  public BlockGroup(Location position, BlockData[][][] blockData) {
    super(position);

    blocks = new BlockDisplay[blockData.length][blockData[0].length][blockData[0][0].length];
    ArrayList<BoundingBox> boundingBoxes = new ArrayList<>();
    for (int x = 0; x < blockData.length; x++) {
      for (int y = 0; y < blockData[0].length; y++) {
        for (int z = 0; z < blockData[0][0].length; z++) {
          if (blockData[x][y][z] != null) {
            blocks[x][y][z] = createBlockDisplay(position.clone().add(x, y, z), blockData[x][y][z]);
            for (BoundingBox boundingBox : blockData[x][y][z].getCollisionShape(new Location(position.getWorld(), 0, 0, 0)).getBoundingBoxes()) {
              boundingBoxes.add(boundingBox.clone().shift(x, y, z));
            }
          }
        }
      }
    }
    mesh.setRegions(boundingBoxes);
  }

  @Override
  public void update() {
    super.update();

    for (int x = 0; x < blocks.length; x++) {
      int x1 = x - blocks.length / 2;
      for (int y = 0; y < blocks[0].length; y++) {
        int y1 = y - blocks[0].length / 2;
        for (int z = 0; z < blocks[0][0].length; z++) {
          int z1 = z - blocks[0][0].length / 2;
          if (blocks[x][y][z] != null) {
            blocks[x][y][z].teleport(new Location(blocks[x][y][z].getWorld(), centerOfMass.x, centerOfMass.y, centerOfMass.z));
            Vector3d translation = new Vector3d(x1 - 0.5d, y1 - 0.5d, z1 - 0.5d).rotate(rotation).add(0.5f, 0.5f, 0.5f);
            Transformation transform = blocks[x][y][z].getTransformation();
            transform.getLeftRotation().set(rotation);
            transform.getTranslation().set(translation);
            blocks[x][y][z].setTransformation(transform);
          }
        }
      }
    }
  }

  @Override
  public void destroy() {
    for (BlockDisplay[][] block : blocks) {
      for (BlockDisplay[] physicsBlocks : block) {
        for (BlockDisplay physicsBlock : physicsBlocks) {
          if (physicsBlock != null) {
            physicsBlock.remove();
          }
        }
      }
    }
  }
}
