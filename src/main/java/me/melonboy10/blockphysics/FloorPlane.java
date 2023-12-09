package me.melonboy10.blockphysics;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import static java.lang.Math.PI;
import static me.melonboy10.blockphysics.PhysicsWorld.ELASTICITY;
import static me.melonboy10.blockphysics.PhysicsWorld.GRAVITY;

public class FloorPlane extends PhysicsEntity {
  BlockDisplay blockDisplay;

  public FloorPlane(Location position, float frictionCoefficient, int size) {
    super(position);
    this.active = false;

    Collider[] colliders = new Collider[size * size];
    for (int i = 0; i < size * size; i++) {
      colliders[i] = new Collider(new Vector3f(i % size, 0, (float) (i / size)).add((float) position.x(), (float) position.y(), (float) position.z()), 0.5f);
    }
    setColliders(colliders);

    blockDisplay = createBlockDisplay(position, Material.WHITE_STAINED_GLASS.createBlockData());
    Transformation transform = blockDisplay.getTransformation();
//    transform.getScale().set(size * 2, 0, size * 2);
//    transform.getTranslation().set(-size, 0, -size);
    blockDisplay.setTransformation(transform);
  }

  @Override
  public void destroy() {
    blockDisplay.remove();
  }

//  @Override
//  public boolean collidesWith(PhysicsEntity otherEntity) {
////    boolean collides =
////      otherEntity.position.y - otherEntity.radius < position.y &&
////      otherEntity.position.x + otherEntity.radius > position.x - floorSize &&
////      otherEntity.position.x - otherEntity.radius < position.x + floorSize &&
////      otherEntity.position.z + otherEntity.radius > position.z - floorSize &&
////      otherEntity.position.z - otherEntity.radius < position.z + floorSize;
////    if (!collides) return false;
////
////    // Move out of the floor
////    otherEntity.position.y = position.y + otherEntity.radius;
////    return true;
//    return super.collidesWith(otherEntity);
////    Vector3f move = collisionShape.collidesWith(otherEntity.collisionShape);
////    if (move == null) return false;
////    otherEntity.position.add(move);
////    return true;
//  }

//  @Override
//  public void onCollision(PhysicsEntity otherEntity) {
////    super.onCollision(otherEntity);
////    Vector3f normalForce = new Vector3f(0, 1, 0).mul(otherEntity.mass * GRAVITY).mul(ELASTICITY);
////
////    Vector3f frictionForce = new Vector3f(otherEntity.velocity).mul(-frictionCoefficient);
////
////    Vector3f totalForce = new Vector3f(normalForce).add(frictionForce);
////    Vector3f acceleration = new Vector3f(totalForce).div(otherEntity.mass);
////    otherEntity.velocity.add(acceleration);
//    otherEntity.velocity.mul(frictionCoefficient, -ELASTICITY, frictionCoefficient);
////    otherEntity.position.y = position.y + otherEntity.radius * 2;
//  }
}
