//package me.melonboy10.blockphysics;
//
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.block.data.BlockData;
//import org.joml.Quaternionf;
//import org.joml.Vector3f;
//
//import java.util.ArrayList;
//
//public class BlockGroup extends PhysicsEntity {
//
//  PhysicsBlock[][][] blocks;
//
//  public BlockGroup(Location position, BlockData[][][] blockData) {
//    super(position, blockData.length);
//    blocks = new PhysicsBlock[blockData.length][][];
//
//    ArrayList<Collider> colliders = new ArrayList<>();
//    for (int x = 0; x < blockData.length; x++) {
//      blocks[x] = new PhysicsBlock[blockData[x].length][];
//      for (int y = 0; y < blockData[x].length; y++) {
//        blocks[x][y] = new PhysicsBlock[blockData[x][y].length];
//        for (int z = 0; z < blockData[x][y].length; z++) {
//          if (blockData[x][y][z] != null) {
//            blocks[x][y][z] = new PhysicsBlock(position.clone().add(x, y, z), blockData[x][y][z]);
//            blocks[x][y][z].active = false;
//
//            colliders.add(new Collider(new Vector3f(x, y, z), 1));
//          }
//        }
//      }
//    }
//    setColliders(colliders.toArray(new Collider[0]), null);
//  }
//
//  @Override
//  public void update() {
//    super.update();
////    Vector3f groupCenter = new Vector3f();
////
////    for (int i = 0; i < blocks.length; i++) {
////      PhysicsBlock[][] block = blocks[i];
////      for (int j = 0; j < block.length; j++) {
////        PhysicsBlock[] physicsBlocks = block[j];
////        for (int k = 0; k < physicsBlocks.length; k++) {
////          PhysicsBlock physicsBlock = physicsBlocks[k];
////          if (physicsBlock != null) {
////            groupCenter.add(i, j, k);
////          }
////        }
////      }
////    }
////
////    groupCenter.div(blocks.length * blocks.length * blocks.length); // Calculate the average center
////
////    for (int i = 0; i < blocks.length; i++) {
////      PhysicsBlock[][] block = blocks[i];
////      for (int j = 0; j < block.length; j++) {
////        PhysicsBlock[] physicsBlocks = block[j];
////        for (int k = 0; k < physicsBlocks.length; k++) {
////          PhysicsBlock physicsBlock = physicsBlocks[k];
////          if (physicsBlock != null) {
////            Vector3f relativePosition = new Vector3f(i, j, k).sub(groupCenter);
////            relativePosition.rotate(rotation); // Rotate the relative position
////            physicsBlock.position = new Vector3f(position).add(relativePosition);
////            physicsBlock.rotation = new Quaternionf(rotation);
////            physicsBlock.update();
////          }
////        }
////      }
////    }
//  }
//
//  @Override
//  public void destroy() {
//    for (PhysicsBlock[][] block : blocks) {
//      for (PhysicsBlock[] physicsBlocks : block) {
//        for (PhysicsBlock physicsBlock : physicsBlocks) {
//          if (physicsBlock != null) {
//            physicsBlock.destroy();
//          }
//        }
//      }
//    }
//  }
//}
