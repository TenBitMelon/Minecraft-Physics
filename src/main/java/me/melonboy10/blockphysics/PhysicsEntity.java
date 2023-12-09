package me.melonboy10.blockphysics;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.EntityType;
import org.joml.Matrix3d;
import org.joml.Quaternionf;
import org.joml.Vector3f;


import static me.melonboy10.blockphysics.PhysicsWorld.*;

public abstract class PhysicsEntity {
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

  //  All the colliders are relative to the center of mass
  private Collider[] colliders;
  private Collider[] preparedCollisionShape = {};
  public float mass = 1;
  Matrix3d inertiaTensor = new Matrix3d();
  Vector3f centerOfMass = new Vector3f();

  public PhysicsEntity(Location position) {
    setColliders(new Collider[] {new Collider(position.toVector().toVector3f(), 0.5f)});
  }

  /**
   * @param colliders Colliders should be all relative to 0, 0, 0 world position
   */
  public void setColliders(Collider[] colliders) {
    this.colliders = colliders;

    for (Collider collider : colliders) {
      mass += collider.radius * collider.radius * collider.radius;
      centerOfMass.add(collider.position);
    }
    centerOfMass.div(colliders.length);
    float momentOfInertia = 0;
    for (Collider collider : colliders) {
      momentOfInertia += collider.radius * collider.radius * collider.radius * collider.position.distanceSquared(centerOfMass);
    }
    inertiaTensor.m00 = momentOfInertia;
    inertiaTensor.m11 = momentOfInertia;
    inertiaTensor.m22 = momentOfInertia;

    for (Collider collider : colliders) {
      collider.position.sub(centerOfMass);
    }
  }

  public void update() {
    /** PARTICLES FOR WHERE IT IS **/
    if (DEBUG) {
      Particle particle = active ? Particle.ELECTRIC_SPARK : Particle.SCRAPE;
      for (Collider collider : colliders) {
        Vector3f particlePos = new Vector3f(collider.position).rotate(rotation).add(centerOfMass);
        particle(particle, particlePos);
        if (DEBUG_LEVEL < 2) continue;
        particle(particle, particlePos.add(0, collider.radius, 0, new Vector3f()));
        particle(particle, particlePos.add(0, -collider.radius, 0, new Vector3f()));
        particle(particle, particlePos.add(collider.radius, 0, 0, new Vector3f()));
        particle(particle, particlePos.add(-collider.radius, 0, 0, new Vector3f()));
        particle(particle, particlePos.add(0, 0, collider.radius, new Vector3f()));
        particle(particle, particlePos.add(0, 0, -collider.radius, new Vector3f()));

        if (DEBUG_LEVEL < 3) continue;
        Vector3f rotationX = new Vector3f(0, 1, 0).rotateX((float) (Math.PI / 4f)).mul(collider.radius);
        Vector3f rotationY = new Vector3f(0, 0, 1).rotateY((float) (Math.PI / 4f)).mul(collider.radius);
        Vector3f rotationZ = new Vector3f(1, 0, 0).rotateZ((float) (Math.PI / 4f)).mul(collider.radius);
        for (int i = 0; i < 4; i++) {
          particle(particle, particlePos.add(rotationZ, new Vector3f()));
          particle(particle, particlePos.add(rotationY, new Vector3f()));
          particle(particle, particlePos.add(rotationX, new Vector3f()));
          rotationX.rotateX((float) (Math.PI / 2f));
          rotationY.rotateY((float) (Math.PI / 2f));
          rotationZ.rotateZ((float) (Math.PI / 2f));
        }

        if (DEBUG_LEVEL < 4) continue;

        rotationZ = new Vector3f(0, 0, -1).rotateX((float) (Math.PI / 8f)).rotateZ((float) (Math.PI / 8f)).mul(collider.radius);
        for (int i = 0; i < 8; i++) {
          for(int j = 0; j < 8; j++) {
            particle(particle, particlePos.add(rotationZ, new Vector3f()));
            rotationZ.rotateZ((float) (Math.PI / 4f));
          }
          rotationZ.rotateX((float) (Math.PI / 4f));
        }
      }
    }

    /** ACTIVE MEANS IT MOVES | UPDATE POSITION AND ROTATION **/
    if (active) {
      centerOfMass.add(velocity.mul(DELTA_TIME, new Vector3f()));
      rotation.rotateXYZ(angularVelocity.x * DELTA_TIME, angularVelocity.y * DELTA_TIME, angularVelocity.z * DELTA_TIME);

      /** APPLY GRAVITY **/
      applyMasslessForce(new Vector3f(0, -GRAVITY, 0));
    } else {
      velocity.set(0);
      angularVelocity.set(0);
    }

    /** UPDATE THE COLLISION SHAPE FOR COLLISION DETECTION **/
    preparedCollisionShape = new Collider[colliders.length];
    for (int i = 0; i < colliders.length; i++) {
      preparedCollisionShape[i] = new Collider(new Vector3f(colliders[i].position).rotate(rotation).add(centerOfMass), colliders[i].radius);
    }
  }

  abstract public void destroy();

  /**
   * Checks if the entity collides with the other entity by checking preparedCollisionShape
   * If a collision is found the whole entity is moved out of the other entity
   *
   * Then do all the force calculations
   */
  public void collidesWith(PhysicsEntity otherEntity) {
    if (!active && !otherEntity.active) return;
    for (Collider collider : preparedCollisionShape) {
      for (Collider otherCollider : otherEntity.preparedCollisionShape) {
        Vector3f move = collider.position.sub(otherCollider.position, new Vector3f());
        if (move.length() < collider.radius + otherCollider.radius) {
          // Move based on masses
          move = move.normalize(new Vector3f()).mul(collider.radius + otherCollider.radius - move.length());
//          particleVector(collider.position, move);
//          if (active && otherEntity.active) move.div(2f);
          if (active) centerOfMass.add(move);
          if (otherEntity.active) otherEntity.centerOfMass.sub(move);


          /** FORCE LOGIC **/
          Vector3f pointOfCollision = new Vector3f(otherCollider.position).add(move.normalize().mul(otherCollider.radius));
          particle(Particle.SOUL_FIRE_FLAME, pointOfCollision);

          Vector3f normal = new Vector3f(otherCollider.position).sub(collider.position).normalize();

//          if (normal.angle(new Vector3f(0, 1, 0)) < Math.PI / 4) normal.set(0, 1, 0);
//          else if (normal.angle(new Vector3f(0, -1, 0)) < Math.PI / 4) normal.set(0, -1, 0);
//          else if (normal.angle(new Vector3f(1, 0, 0)) < Math.PI / 4) normal.set(1, 0, 0);
//          else if (normal.angle(new Vector3f(-1, 0, 0)) < Math.PI / 4) normal.set(-1, 0, 0);
//          else if (normal.angle(new Vector3f(0, 0, 1)) < Math.PI / 4) normal.set(0, 0, 1);
//          else if (normal.angle(new Vector3f(0, 0, -1)) < Math.PI / 4) normal.set(0, 0, -1);
//          else normal.set(0, 1, 0);

//          particleVector(pointOfCollision, normal);

          Vector3f relativeVelocity = new Vector3f(velocity).sub(otherEntity.velocity);

          Vector3f normalForce = new Vector3f(normal).mul(GRAVITY * mass * otherEntity.mass * ELASTICITY);
          applyForce(normalForce, pointOfCollision);
          otherEntity.applyForce(normalForce.negate(new Vector3f()), pointOfCollision);

//          /** ELASTICITY & VELOCITY EXCHANGE **/
//          float impactSpeed = relativeVelocity.dot(normal);
//          if (impactSpeed > 0) {
//            float reducedMass = (mass * otherEntity.mass) / (mass + otherEntity.mass);
//            float impulse = (1 + ELASTICITY) * reducedMass * impactSpeed;
//
//            Vector3f impulseVector = new Vector3f(normal).mul(impulse);
//            applyMasslessForce(impulseVector, pointOfCollision);
//            otherEntity.applyMasslessForce(impulseVector.negate(new Vector3f()), pointOfCollision);
//          }
        }
      }
    }
  }

  public static void particleVector(Vector3f location, Vector3f vector) {
    System.out.println("location = " + location);

    for (int i = 0; i < 10; i++) {
      Vector3f particleLocation = new Vector3f(location).add(new Vector3f(vector).mul(i / 10f));
      WORLD.spawnParticle(Particle.FLAME, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0);
    }
    WORLD.spawnParticle(Particle.SOUL_FIRE_FLAME, location.x, location.y, location.z, 0, 0, 0, 0);
  }

  public static void particle(Particle particle, Vector3f location) {
    WORLD.spawnParticle(particle, location.x, location.y, location.z, 0, 0, 0, 0);
  }

  public void applyForce(Vector3f force) {
    velocity.add(new Vector3f(force).div(mass));
  }

  public void applyMasslessForce(Vector3f force) {
    velocity.add(new Vector3f(force));
  }

  public void applyForce(Vector3f force, Vector3f position) {
    velocity.add(new Vector3f(force).div(mass));
//    angularVelocity.add(new Vector3f(force).cross(new Vector3f(position).sub(centerOfMass)).div(momentOfInertia));
  }

  public void applyMasslessForce(Vector3f force, Vector3f position) {
    velocity.add(new Vector3f(force));
//    angularVelocity.add(new Vector3f(force).cross(new Vector3f(position).sub(centerOfMass)).div(momentOfInertia));
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
