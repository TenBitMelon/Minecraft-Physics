package me.melonboy10.blockphysics;

import static me.melonboy10.blockphysics.PhysicsWorld.*;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public abstract class PhysicsEntityOLD {
  Vector3f position;
  Vector3f velocity = new Vector3f();
  Vector3f angularVelocity = new Vector3f();
  Quaternionf rotation = new Quaternionf();

  boolean active = true;

  public record Collider(Vector3f position, float radius) {
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Collider otherCollider) return position.equals(otherCollider.position) && radius == otherCollider.radius;
      return false;
    }
  }

  private Collider[] colliders;
  public float mass = 1;
  public float momentOfInertia = 1;
  Vector3f centerOfMass = new Vector3f();

  public PhysicsEntityOLD(Location position, int scale) {
    this.position = new Vector3f((float) position.x(), (float) position.y(), (float) position.z());

    setColliders(new Collider[] {new Collider(new Vector3f(), 0.5f)});
  }

  public void setColliders(Collider[] colliders) {
    this.colliders = colliders;

    for (Collider collider : colliders) {
      mass += collider.radius * collider.radius * collider.radius;
      centerOfMass.add(collider.position);
    }
    centerOfMass.div(colliders.length);
    System.out.println("centerOfMass = " + centerOfMass);
    for (Collider collider : colliders) {
      momentOfInertia += collider.radius * collider.radius * collider.radius * collider.position.distanceSquared(centerOfMass);
    }
    System.out.println("momentOfInertia = " + momentOfInertia);
  }

  public void update() {
    if (active) {
      velocity.add(0, -GRAVITY * DELTA_TIME, 0);
//      float angularVelocityLength = angularVelocity.length();
//      if (angularVelocityLength > 0) {
//        float angle = angularVelocityLength * DELTA_TIME;
//        Vector3f axis = new Vector3f(angularVelocity).normalize();
//        rotation.rotateAxis(angle, axis);
//      }

//      position.add(velocity);
      centerOfMass.add(velocity);
      for (Collider collider : colliders) {
        collider.position.add(velocity);
//        collider.position.rotate(rotation);
      }
    } else {
      velocity.set(0, 0, 0);
      angularVelocity.set(0, 0, 0);
    }

      Particle particle = active ? Particle.CRIT : Particle.CRIT_MAGIC;
    for (Collider collider : colliders) {
      WORLD.spawnParticle(particle, position.x, position.y, position.z,  0, 0,0,0);
      WORLD.spawnParticle(particle, position.x, position.y + collider.radius, position.z, 0, 0,0,0);
      WORLD.spawnParticle(particle, position.x, position.y - collider.radius, position.z, 0, 0,0,0);
      WORLD.spawnParticle(particle, position.x + collider.radius, position.y, position.z, 0, 0,0,0);
      WORLD.spawnParticle(particle, position.x - collider.radius, position.y, position.z, 0, 0,0,0);
      WORLD.spawnParticle(particle, position.x, position.y, position.z + collider.radius, 0, 0,0,0);
      WORLD.spawnParticle(particle, position.x, position.y, position.z - collider.radius, 0, 0,0,0);
    }
  }

  abstract public void destroy();

//  Collision detection
  public boolean collidesWith(PhysicsEntityOLD otherEntity) {
//    float distance = position.distance(otherEntity.position);
//    boolean collides = distance < (radius + otherEntity.radius);
//    if (!collides) return false;
//
////    Move out of each other
//    Vector3f direction = new Vector3f(position).sub(otherEntity.position).normalize();
//    float overlap = (radius + otherEntity.radius) - distance;
//    position.add(new Vector3f(direction).mul(overlap / 2));
//    otherEntity.position.sub(new Vector3f(direction).mul(overlap / 2));
//
//    return true;

    for (Collider collider : colliders) {
      for (Collider otherCollider : otherEntity.colliders) {
        if (collider.position.distance(otherCollider.position) < collider.radius + otherCollider.radius) {
          return true;
        }
      }
    }
    return false;
  }

  public void onCollision(PhysicsEntityOLD otherEntity) {
//    if (this == otherEntity) return;
//    Vector3f collisionNormal = new Vector3f(position).sub(otherEntity.position).normalize();
//    Vector3f relativeVelocity = new Vector3f(velocity).sub(otherEntity.velocity);
//    float velocityAlongNormal = relativeVelocity.dot(collisionNormal);
//    if (velocityAlongNormal > 0) return;
//
////    Vector3f pointOfContact = new Vector3f(position).add(new Vector3f(collisionNormal).mul(radius));
//
//    // 0.8 is the "restitution" coefficient aka bounciness
//    float j = -(1 + ELASTICITY) * velocityAlongNormal / (1 / mass + 1 / otherEntity.mass);
//    Vector3f impulse = new Vector3f(collisionNormal).mul(j);
//
//    // Apply linear impulse considering mass
//    velocity.add(new Vector3f(impulse).div(mass));
//    otherEntity.velocity.sub(new Vector3f(impulse).div(otherEntity.mass));

    // Calculate the vector from the center of mass to the point of contact
//    Vector3f r1 = new Vector3f(position).sub(pointOfContact);
//    Vector3f r2 = new Vector3f(otherEntity.position).sub(pointOfContact);
//    float mu = 0.5f; // Coefficient of friction

//    Vector3f frictionCross = collisionNormal.normalize().cross(velocity.normalize());
//    Vector3f forceFriction = collisionNormal.rotate(new Quaternionf(frictionCross.x, frictionCross.y, frictionCross.z, 1)).mul(mu * j);
//
//    Vector3f torque = r1.cross(forceFriction);
//    Vector3f torque2 = r2.cross(forceFriction);
//
//    angularVelocity.add(new Vector3f(torque).div(momentOfInertia));
//    otherEntity.angularVelocity.sub(new Vector3f(torque2).div(otherEntity.momentOfInertia));

//    Vector3f frictionDirection = new Vector3f(relativeVelocity).sub(collisionNormal.mul(relativeVelocity.dot(collisionNormal))).normalize();
//    Vector3f forceFriction = new Vector3f(frictionDirection).mul(mu * j);
//
//    Vector3f torque = new Vector3f(r1).cross(forceFriction);
//    Vector3f torque2 = new Vector3f(r2).cross(forceFriction);
//
//    angularVelocity.add(new Vector3f(torque).div(momentOfInertia));
//    otherEntity.angularVelocity.sub(new Vector3f(torque2).div(otherEntity.momentOfInertia));

  }

  public void applyForce(Vector3f force) {
    velocity.add(new Vector3f(force).div(mass));
  }

  public void applyForce(Vector3f force, Vector3f pointOfContact) {
    velocity.add(new Vector3f(force).div(mass));
    angularVelocity.add(new Vector3f(pointOfContact).cross(force).div(momentOfInertia));
  }

  public static BlockDisplay createBlockDisplay(Location location, BlockData blockData) {
    Location location1 = location.clone();
    location1.setPitch(0);
    location1.setYaw(0);
    BlockDisplay blockDisplay = (BlockDisplay) location.getWorld().spawnEntity(location1, EntityType.BLOCK_DISPLAY);
    blockDisplay.setBlock(blockData);
    return blockDisplay;
  }

}
