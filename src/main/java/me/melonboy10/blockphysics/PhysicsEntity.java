package me.melonboy10.blockphysics;

import lombok.Getter;
import net.minecraft.util.Tuple;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.BoundingBox;
import org.joml.*;
import org.joml.Vector3d;

import java.lang.Math;

import static me.melonboy10.blockphysics.PhysicsWorld.*;

public abstract class PhysicsEntity {
  boolean active = true;

  public record Collider(Vector3d position, float radius) {
    @Override
    public boolean equals(Object obj) {
      if (obj instanceof Collider otherCollider)
        return position.equals(otherCollider.position) && radius == otherCollider.radius;
      return false;
    }
  }


  //  All the colliders are relative to the center of mass
  //  private Collider[] colliders;
  //  private Collider[] preparedCollisionShape = {};
  //  Matrix3d inertiaTensor = new Matrix3d();
  //  Vector3d centerOfMass = new Vector3d();

  //  Stored in local space
  //  1f/ (2f / 5f * mass * 0.5f * 0.5f) is the inertia tensor for a sphere inverted
  Mesh mesh;
  Vector3d centerOfMass;
  Vector3d velocity;
  Vector3d angularMomentum;
  Quaterniond rotation;
  //  Vector3d angularVelocity = new Vector3d();


  public PhysicsEntity(Location position, Mesh mesh) {
    this.mesh = mesh;
    this.centerOfMass = position.toVector().toVector3d();
    this.velocity = new Vector3d();
    this.angularMomentum = new Vector3d();
    this.rotation = new Quaterniond();
  }

  public PhysicsEntity(Location position) {
    this(position, new Mesh(new BoundingBox[]{new BoundingBox(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5)}));
  }

  //  /**
  //   * @param colliders Colliders should be all relative to 0, 0, 0 world position
  //   */
  //  public void setColliders(Collider[] colliders) {
  //    this.colliders = colliders;
  //
  //    for (Collider collider : colliders) {
  //      mass += collider.radius * collider.radius * collider.radius;
  //      centerOfMass.add(collider.position);
  //    }
  //    centerOfMass.div(colliders.length);
  //    float momentOfInertia = 0;
  //    for (Collider collider : colliders) {
  //      momentOfInertia += collider.radius * collider.radius * collider.radius *
  // collider.position.distanceSquared(centerOfMass);
  //    }
  //    inertiaTensor.m00 = momentOfInertia;
  //    inertiaTensor.m11 = momentOfInertia;
  //    inertiaTensor.m22 = momentOfInertia;
  //
  //    for (Collider collider : colliders) {
  //      collider.position.sub(centerOfMass);
  //    }
  //  }

  public void update() {
    /** PARTICLES FOR WHERE IT IS * */
    if (DEBUG) {
      Particle particle = active ? Particle.ELECTRIC_SPARK : Particle.SCRAPE;
      //      Vector3d particlePos = new Vector3d(centerOfMass);
      //      particle(particle, particlePos);
      //      if (DEBUG_LEVEL >= 2) {
      //        particle(particle, particlePos.add(0, 0.5f, 0, new Vector3d()));
      //        particle(particle, particlePos.add(0, -0.5f, 0, new Vector3d()));
      //        particle(particle, particlePos.add(0.5f, 0, 0, new Vector3d()));
      //        particle(particle, particlePos.add(-0.5f, 0, 0, new Vector3d()));
      //        particle(particle, particlePos.add(0, 0, 0.5f, new Vector3d()));
      //        particle(particle, particlePos.add(0, 0, -0.5f, new Vector3d()));
      //      }
      //
      //      if (DEBUG_LEVEL >= 3) {
      //        Vector3d rotationX = new Vector3d(0, 1, 0).rotateX((float) (Math.PI / 4f)).mul(0.5f);
      //        Vector3d rotationY = new Vector3d(0, 0, 1).rotateY((float) (Math.PI / 4f)).mul(0.5f);
      //        Vector3d rotationZ = new Vector3d(1, 0, 0).rotateZ((float) (Math.PI / 4f)).mul(0.5f);
      //        for (int i = 0; i < 4; i++) {
      //          particle(particle, particlePos.add(rotationZ, new Vector3d()));
      //          particle(particle, particlePos.add(rotationY, new Vector3d()));
      //          particle(particle, particlePos.add(rotationX, new Vector3d()));
      //          rotationX.rotateX((float) (Math.PI / 2f));
      //          rotationY.rotateY((float) (Math.PI / 2f));
      //          rotationZ.rotateZ((float) (Math.PI / 2f));
      //        }
      //
      //        if (DEBUG_LEVEL >= 4) {
      //          rotationZ = new Vector3d(0, 0, -1).rotateX((float) (Math.PI / 8f)).rotateZ((float) (Math.PI / 8f)).mul(0.5f);
      //          for (int i = 0; i < 8; i++) {
      //            for (int j = 0; j < 8; j++) {
      //              particle(particle, particlePos.add(rotationZ, new Vector3d()));
      //              rotationZ.rotateZ((float) (Math.PI / 4f));
      //            }
      //            rotationZ.rotateX((float) (Math.PI / 4f));
      //          }
      //        }
      //      }

      for (BoundingBox region : mesh.getRegions()) {
        Vector3d particlePos = region.getCenter().toVector3d().rotate(rotation).add(centerOfMass);
        particle(particle, particlePos);
        if (DEBUG_LEVEL < 2) continue;
        particle(particle, particlePos.add(region.getMinX(), region.getMinY(), region.getMinZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMaxX(), region.getMinY(), region.getMinZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMinX(), region.getMaxY(), region.getMinZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMaxX(), region.getMaxY(), region.getMinZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMinX(), region.getMinY(), region.getMaxZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMaxX(), region.getMinY(), region.getMaxZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMinX(), region.getMaxY(), region.getMaxZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMaxX(), region.getMaxY(), region.getMaxZ(), new Vector3d()));

        if (DEBUG_LEVEL < 3) continue;
        particle(particle, particlePos.add(region.getCenterX(), region.getMinY(), region.getCenterZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getCenterX(), region.getMaxY(), region.getCenterZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMinX(), region.getCenterY(), region.getCenterZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getMaxX(), region.getCenterY(), region.getCenterZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getCenterX(), region.getCenterY(), region.getMinZ(), new Vector3d()));
        particle(particle, particlePos.add(region.getCenterX(), region.getCenterY(), region.getMaxZ(), new Vector3d()));
      }
    }

    /** ACTIVE MEANS IT MOVES | UPDATE POSITION AND ROTATION * */
    if (active) {

      centerOfMass.add(velocity.mul(DELTA_TIME, new Vector3d()));
      //      Create angular velocity from angular momentum by rotating it to match the inverse initial inertia tensor then rotating it back to the original rotation
      //      Vector3d angularVelocity = new Vector3d(angularMomentum).rotate(rotation.invert()).mul(inverseInitialInertiaTensor).rotate(rotation);
      //      Vector3d angularVelocity = new Vector3d(angularMomentum).mul(inverseInitialInertiaTensor);
      //      rotation.rotateXYZ(
      //        angularVelocity.x * DELTA_TIME,
      //        angularVelocity.y * DELTA_TIME,
      //        angularVelocity.z * DELTA_TIME);

      velocity.add(0, -GRAVITY * DELTA_TIME, 0);

      //      centerOfMass.add(velocity.mul(DELTA_TIME, new Vector3d()));
      //      rotation.rotateXYZ(angularVelocity.x * DELTA_TIME, angularVelocity.y * DELTA_TIME,
      // angularVelocity.z * DELTA_TIME);

      /** APPLY GRAVITY * */
      //      applyMasslessForce(new Vector3d(0, -GRAVITY, 0));
    } else {
      velocity.set(0);
      //      angularVelocity.set(0);
      angularMomentum.set(0);
    }

    /** UPDATE THE COLLISION SHAPE FOR COLLISION DETECTION * */
    //    preparedCollisionShape = new Collider[colliders.length];
    //    for (int i = 0; i < colliders.length; i++) {
    //      preparedCollisionShape[i] = new Collider(new
    // Vector3d(colliders[i].position).rotate(rotation).add(centerOfMass), colliders[i].radius);
    //    }
  }

  public abstract void destroy();

  /**
   * Checks if the entity collides with the other entity by checking mesh
   *
   * @return Tuple<Vector3d, Vector3d> The location of the collision and the normal of the collision
   */
  public Tuple<Vector3d, Vector3d> collidesWith(PhysicsEntity otherEntity) {
    if (!active && !otherEntity.active) return null;

    Vector3d centerDifference = new Vector3d(otherEntity.centerOfMass).sub(centerOfMass);
    Quaterniond rotationDifference = new Quaterniond(rotation).invert().mul(otherEntity.rotation);

    //    Move mesh into world space
    Vector3d[] vertices = mesh.getVertices();
    for (Vector3d vertex : vertices) {
      Vector3d point = vertex.rotate(rotationDifference, new Vector3d()).add(centerDifference);
      BoundingBox region = otherEntity.mesh.isContained(point);
      if (region != null) {
        Vector3d normal = new Vector3d(region.getCenter().toVector3d()).sub(point);
        // Could be done with max and min
        if (normal.angle(new Vector3d(0, 1, 0)) < Math.PI / 4) normal.set(0, 1, 0);
        else if (normal.angle(new Vector3d(0, -1, 0)) < Math.PI / 4) normal.set(0, -1, 0);
        else if (normal.angle(new Vector3d(1, 0, 0)) < Math.PI / 4) normal.set(1, 0, 0);
        else if (normal.angle(new Vector3d(-1, 0, 0)) < Math.PI / 4) normal.set(-1, 0, 0);
        else if (normal.angle(new Vector3d(0, 0, 1)) < Math.PI / 4) normal.set(0, 0, 1);
        else if (normal.angle(new Vector3d(0, 0, -1)) < Math.PI / 4) normal.set(0, 0, -1);
        else normal.set(0, 1, 0);

        float distanceToNearestFace = (float) Math.min(
          Math.abs(point.x - region.getMinX()),
          Math.min(
            Math.abs(point.y - region.getMinY()),
            Math.abs(point.z - region.getMinZ())
          )
        );
        Vector3d collisionPoint = new Vector3d(normal).mul(distanceToNearestFace).add(point);
        return new Tuple<>(collisionPoint, normal);
      }
    }
    return null;
  }

  public void onCollision(PhysicsEntity otherEntity, Vector3d collisionPoint, Vector3d normal) {
    if (active && otherEntity.active) {
      Vector3d move =
        new Vector3d(collisionPoint).normalize().mul(1 - centerOfMass.distance(otherEntity.centerOfMass));
      centerOfMass.add(move);
      otherEntity.centerOfMass.sub(move);

      //      Vector3d normal = new Vector3d(collisionPoint).sub(centerOfMass).normalize();
      particleVector(collisionPoint, normal);

      //      Calculate momentum
      // m1v1 + m2v2 = m1v1' + m2v2'
      // v1' = (v1(m1 - m2) + 2m2v2) / (m1 + m2)
      float mass = mesh.getMass();
      float otherMass = otherEntity.mesh.getMass();
      Vector3d finalVelocity = new Vector3d(velocity).mul(mass - otherMass).add(new Vector3d(otherEntity.velocity).mul(2 * otherMass)).div(mass + otherMass);
      Vector3d otherFinalVelocity = new Vector3d(otherEntity.velocity).mul(otherMass - mass).add(new Vector3d(velocity).mul(2 * mass)).div(mass + otherMass);
      velocity.set(finalVelocity);
      otherEntity.velocity.set(otherFinalVelocity);
    }

    //    if (!active && !otherEntity.active) return;
    //    for (Collider collider : preparedCollisionShape) {
    //      for (Collider otherCollider : otherEntity.preparedCollisionShape) {
    //        Vector3d move = collider.position.sub(otherCollider.position, new Vector3d());
    //        if (move.length() < collider.radius + otherCollider.radius) {
    //          // Move based on masses
    //          move = move.normalize(new Vector3d()).mul(collider.radius + otherCollider.radius -
    // move.length());
    ////          particleVector(collider.position, move);
    ////          if (active && otherEntity.active) move.div(2f);
    //          if (active) centerOfMass.add(move);
    //          if (otherEntity.active) otherEntity.centerOfMass.sub(move);
    //
    //
    //          /** FORCE LOGIC **/
    //          Vector3d pointOfCollision = new
    // Vector3d(otherCollider.position).add(move.normalize().mul(otherCollider.radius));
    //          particle(Particle.SOUL_FIRE_FLAME, pointOfCollision);
    //
    //          Vector3d normal = new
    // Vector3d(otherCollider.position).sub(collider.position).normalize();
    //
    ////          if (normal.angle(new Vector3d(0, 1, 0)) < Math.PI / 4) normal.set(0, 1, 0);
    ////          else if (normal.angle(new Vector3d(0, -1, 0)) < Math.PI / 4) normal.set(0, -1, 0);
    ////          else if (normal.angle(new Vector3d(1, 0, 0)) < Math.PI / 4) normal.set(1, 0, 0);
    ////          else if (normal.angle(new Vector3d(-1, 0, 0)) < Math.PI / 4) normal.set(-1, 0, 0);
    ////          else if (normal.angle(new Vector3d(0, 0, 1)) < Math.PI / 4) normal.set(0, 0, 1);
    ////          else if (normal.angle(new Vector3d(0, 0, -1)) < Math.PI / 4) normal.set(0, 0, -1);
    ////          else normal.set(0, 1, 0);
    //
    //          particleVector(pointOfCollision, normal);
    //
    ////          Vector3d relativeVelocity = new Vector3d(velocity).sub(otherEntity.velocity);
    //
    //          Vector3d normalForce = new Vector3d(normal).mul(GRAVITY * mass * otherEntity.mass *
    // ELASTICITY);
    //          applyForce(normalForce, pointOfCollision);
    //          otherEntity.applyForce(normalForce.negate(new Vector3d()), pointOfCollision);
    //
    ////          /** ELASTICITY & VELOCITY EXCHANGE **/
    ////          float impactSpeed = relativeVelocity.dot(normal);
    ////          if (impactSpeed > 0) {
    ////            float reducedMass = (mass * otherEntity.mass) / (mass + otherEntity.mass);
    ////            float impulse = (1 + ELASTICITY) * reducedMass * impactSpeed;
    ////
    ////            Vector3d impulseVector = new Vector3d(normal).mul(impulse);
    ////            applyMasslessForce(impulseVector, pointOfCollision);
    ////            otherEntity.applyMasslessForce(impulseVector.negate(new Vector3d()),
    // pointOfCollision);
    ////          }
    //        }
    //      }
    //    }
  }

  public static void particleVector(Vector3d location, Vector3d vector) {
    //    System.out.println("location = " + location);

    for (int i = 0; i < 10; i++) {
      Vector3d particleLocation = new Vector3d(location).add(new Vector3d(vector).mul(i / 10f));
      WORLD.spawnParticle(Particle.FLAME, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0);
    }
    WORLD.spawnParticle(Particle.SOUL_FIRE_FLAME, location.x, location.y, location.z, 0, 0, 0, 0);
  }

  public static void particle(Particle particle, Vector3d location) {
    WORLD.spawnParticle(particle, location.x, location.y, location.z, 0, 0, 0, 0);
  }

  public void applyForce(Vector3d force) {
    velocity.add(new Vector3d(force).div(mesh.getMass()));
  }

  public void applyMasslessForce(Vector3d force) {
    velocity.add(new Vector3d(force));
  }

  public void applyForce(Vector3d force, Vector3d position) {
    velocity.add(new Vector3d(force).div(mesh.getMass()));
    //    Torque = r x F
    angularMomentum.add(new Vector3d(position).sub(centerOfMass).cross(force));

    //    angularVelocity.add(new Vector3d(force).cross(new
    // Vector3d(position).sub(centerOfMass)).div(momentOfInertia));
  }

  public void applyMasslessForce(Vector3d force, Vector3d position) {
    velocity.add(new Vector3d(force));
    //    angularVelocity.add(new Vector3d(force).cross(new
    // Vector3d(position).sub(centerOfMass)).div(momentOfInertia));
  }

  public static BlockDisplay createBlockDisplay(Location location, BlockData blockData) {
    Location location1 = location.clone();
    location1.setPitch(0);
    location1.setYaw(0);
    BlockDisplay blockDisplay =
      (BlockDisplay) location.getWorld().spawnEntity(location1, EntityType.BLOCK_DISPLAY);
    blockDisplay.setBlock(blockData);
    blockDisplay.setTeleportDuration(1);
    return blockDisplay;
  }

  public static TextDisplay createTextDisplay(Location location, String text) {
    Location location1 = location.clone();
    location1.setPitch(0);
    location1.setYaw(0);
    TextDisplay textDisplay =
      (TextDisplay) location.getWorld().spawnEntity(location1, EntityType.TEXT_DISPLAY);
    textDisplay.setText(text);
    textDisplay.setBillboard(Display.Billboard.VERTICAL);
    textDisplay.setLineWidth(400);
    textDisplay.setTeleportDuration(1);
    return textDisplay;
  }
}
