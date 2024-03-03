package me.melonboy10.blockphysics;

import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.joml.*;
import oshi.util.tuples.Pair;
import oshi.util.tuples.Triplet;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;

import static me.melonboy10.blockphysics.PhysicsWorld.*;

public abstract class PhysicsEntity {
  public static final boolean COLLISION_DEBUG_RENDERING = false;
  public static final Vector3d COLLISION_DEBUG_OFFSET = new Vector3d(0, 90, 50);
  public static final Color[] COLORS = new Color[]{Color.GREEN, Color.BLUE, Color.YELLOW, Color.PURPLE, Color.ORANGE, Color.TEAL, Color.LIME, Color.AQUA, Color.FUCHSIA, Color.MAROON, Color.OLIVE, Color.NAVY, Color.SILVER, Color.GRAY, Color.RED, Color.WHITE, Color.BLACK};

  boolean active = true;

  Mesh mesh;
  Vector3d centerOfMass;
  Vector3d velocity;
  //  Vector3d angularVelocity;
  Vector3d angularMomentum;
  Quaterniond rotation;
  double elasticity = 0.8;
  double staticFriction = 0.4;
  double dynamicFriction = 0.5;
  //  Vector3d angularVelocity = new Vector3d();

  public record ParticleData(Particle particle, Vector3d location) {}

  ArrayList<ParticleData> particles = new ArrayList<>();

  public PhysicsEntity(Location position, Mesh mesh) {
    this.mesh = mesh;
    this.centerOfMass = position.toVector().toVector3d();
    this.velocity = new Vector3d();
    this.angularMomentum = new Vector3d(0, 0, 0);
    //    this.angularVelocity = new Vector3d();
    this.rotation = new Quaterniond();
  }

  public PhysicsEntity(Location position) {
    this(position, new Mesh(new BoundingBox[]{new BoundingBox(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5)}));
  }

  public void update() {
    /* PARTICLES FOR WHERE IT IS */
    //    System.out.println(this.hashCode());
    if (DEBUG) {
      particles.clear();
      Particle particle = active ? Particle.ELECTRIC_SPARK : Particle.SCRAPE;
      for (BoundingBox region : mesh.getRegions()) {
        Vector3d particlePos = region.getCenter().toVector3d();
        particle(particle, particlePos);
        if (DEBUG_LEVEL < 2) continue;
        particle(particle, particlePos.add(region.getMinX(), region.getMinY(), region.getMinZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMaxX(), region.getMinY(), region.getMinZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMinX(), region.getMaxY(), region.getMinZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMaxX(), region.getMaxY(), region.getMinZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMinX(), region.getMinY(), region.getMaxZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMaxX(), region.getMinY(), region.getMaxZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMinX(), region.getMaxY(), region.getMaxZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMaxX(), region.getMaxY(), region.getMaxZ(), new Vector3d()).rotate(rotation).add(centerOfMass));

        if (DEBUG_LEVEL < 3) continue;
        particle(particle, particlePos.add(region.getCenterX(), region.getMinY(), region.getCenterZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getCenterX(), region.getMaxY(), region.getCenterZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMinX(), region.getCenterY(), region.getCenterZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getMaxX(), region.getCenterY(), region.getCenterZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getCenterX(), region.getCenterY(), region.getMinZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
        particle(particle, particlePos.add(region.getCenterX(), region.getCenterY(), region.getMaxZ(), new Vector3d()).rotate(rotation).add(centerOfMass));
      }

      if (DEBUG_LEVEL == -1) {
        particleVector(centerOfMass, velocity, Particle.ELECTRIC_SPARK);
        //        particleVector(centerOfMass, angularVelocity, Particle.WATER_BUBBLE);
        particleVector(centerOfMass, angularMomentum, Particle.WATER_BUBBLE);
        particle(Particle.WAX_ON, centerOfMass);
      }
      ;
    }
    if (particles.size() > 100) particles.clear();

    /* ACTIVE MEANS IT MOVES | UPDATE POSITION AND ROTATION */
    if (active) {

      centerOfMass.add(velocity.mul(DELTA_TIME, new Vector3d()));


      //            Create angular velocity from angular momentum by rotating it to match the inverse initial inertia tensor then rotating it back to the original rotation
      //            Vector3d angularVelocity = new Vector3d(angularMomentum).mul(mesh.getInverseInertiaTensor()).rotate(
      //      rotation.rotateAxis(angularVelocity.length(), angularVelocity.normalize());

      //      rotation.rotateXYZ(angularMomentum.x * DELTA_TIME, angularMomentum.y * DELTA_TIME, angularMomentum.z * DELTA_TIME);

      //      Vector3d angularVelocity = new Matrix3d(rotationMatrix).mul(mesh.getInverseInertiaTensor()).mul(rotationMatrix.transpose(new Matrix3d())).transform(angularMomentum);
      //      rotationMatrix.add(new Matrix3d(0, -angularVelocity.z, angularVelocity.y, angularVelocity.z, 0, -angularVelocity.x, -angularVelocity.y, angularVelocity.x, 0).scale(DELTA_TIME));
      //
      //      rotationMatrix.getNormalizedRotation(rotation);

      //      rotation.rotateXYZ(angularVelocity.x * DELTA_TIME, angularVelocity.y * DELTA_TIME, angularVelocity.z * DELTA_TIME);
      rotation.rotateXYZ(angularMomentum.x * DELTA_TIME, angularMomentum.y * DELTA_TIME, angularMomentum.z * DELTA_TIME);


      velocity.add(0, -GRAVITY * DELTA_TIME, 0);

    } else {
      velocity.set(0);
      //      angularVelocity.set(0);
      angularMomentum.set(0);
    }
  }

  public abstract void destroy();

  public record CollisionData(Vector3d collisionPoint, Vector3d normal, double distanceInset) {}

  public CollisionData collidesWith(PhysicsEntity otherEntity) {
    Vector3d support = mesh.getMinkowskiDifferencePoint(otherEntity.mesh, new Vector3d(1, 0, 0)).add(centerOfMass).sub(otherEntity.centerOfMass);

    Simplex simplex = new Simplex();
    simplex.pushFront(support);

    Vector3d direction = support.negate(new Vector3d());

    if (COLLISION_DEBUG_RENDERING) {
      for (Vector3d vertex : mesh.getVertices()) {
        for (Vector3d meshVertex : otherEntity.mesh.getVertices()) {
          particle(Particle.BUBBLE_POP, new Vector3d(vertex).add(centerOfMass).sub(meshVertex).sub(otherEntity.centerOfMass), COLLISION_DEBUG_OFFSET);
        }
      }
      particle(Particle.COMPOSTER, COLLISION_DEBUG_OFFSET);
    }

    for (int i = 0; i < 100; i++) {
      support = mesh.getMinkowskiDifferencePoint(otherEntity.mesh, direction).add(centerOfMass).sub(otherEntity.centerOfMass);

      if (support.dot(direction) <= 0) return null; // Is not in the direction of the origin
      simplex.pushFront(support);

      //      PhysicsWorld.addDebugLine(Component.text("Direction: %s".formatted(direction)));

      if (simplex.containsOrigin(direction)) {
        if (COLLISION_DEBUG_RENDERING)
          for (Vector3d vertex : simplex.vertices) {
            particle(Particle.GLOW, vertex, COLLISION_DEBUG_OFFSET);
          }
        return EPACollidesWith(simplex, otherEntity);
        //        return null;
      }
    }

    return null;
  }

  public record IntPair(int a, int b) {
    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      IntPair intPair = (IntPair) o;

      if (a != intPair.a) return false;
      return b == intPair.b;
    }
  }

  public CollisionData EPACollidesWith(Simplex simplex, PhysicsEntity otherEntity) {
    //    System.out.println("==================== CHECKING COLLISION ====================");
    ArrayList<Vector3d> polytope = new ArrayList<>(Arrays.asList(simplex.vertices).stream().map(Vector3d::new).toList());
    ArrayList<Integer> faces = new ArrayList<>(Arrays.asList(
      0, 1, 2,
      0, 3, 1,
      0, 2, 3,
      1, 3, 2
    ));
    /*
     * list: vector4d[] - the face normals composed of (normal.x, normal.y, normal.z, distance from origin)
     * integer - the index of the face that is closest to the origin
     */
    Pair<ArrayList<Vector4d>, Integer> faceData = getFaceNormals(polytope, faces);
    ArrayList<Vector4d> normals = faceData.getA();
    int closestFace = faceData.getB();

    Vector3d minNormal = new Vector3d();
    double minDistance = Double.MAX_VALUE;
    int c = 0;

    while (minDistance == Double.MAX_VALUE && c < 50) {
      //    while (minDistance == Double.MAX_VALUE && c < DEBUG_LEVEL) {
      c += 1;
      if (COLLISION_DEBUG_RENDERING) {
        for (Vector3d vector3d : polytope) {
          particle(Particle.FLAME, vector3d, new Vector3d(COLLISION_DEBUG_OFFSET));
        }

        if (c == DEBUG_LEVEL)
          for (int j = 0; j < faces.size(); j += 3) {
            Vector3d a1 = polytope.get(faces.get(j));
            Vector3d b1 = polytope.get(faces.get(j + 1));
            Vector3d c1 = polytope.get(faces.get(j + 2));

            for (int i = 0; i < 10; i++) {
              Vector3d particleLocation = new Vector3d(a1).sub(b1).mul(i / 10f, new Vector3d()).add(COLLISION_DEBUG_OFFSET).add(b1);
              WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
            }
            for (int i = 0; i < 10; i++) {
              Vector3d particleLocation = new Vector3d(b1).sub(c1).mul(i / 10f, new Vector3d()).add(COLLISION_DEBUG_OFFSET).add(c1);
              WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
            }
            for (int i = 0; i < 10; i++) {
              Vector3d particleLocation = new Vector3d(c1).sub(a1).mul(i / 10f, new Vector3d()).add(COLLISION_DEBUG_OFFSET).add(a1);
              WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
            }

            Vector3d mid = new Vector3d(a1).add(b1).add(c1).div(3).add(COLLISION_DEBUG_OFFSET);
            WORLD.spawnParticle(Particle.REDSTONE, mid.x, mid.y, mid.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
          }
      }

      //      PhysicsWorld.addDebugLine(Component.text("Faces: %s".formatted(faces)));
      //      System.out.println("\nc = " + c);

      minNormal = new Vector3d(normals.get(closestFace).x, normals.get(closestFace).y, normals.get(closestFace).z);
      minDistance = normals.get(closestFace).w;


      //      System.out.println("minDistance = " + minDistance);

      Vector3d support = mesh.getMinkowskiDifferencePoint(otherEntity.mesh, minNormal).add(centerOfMass).sub(otherEntity.centerOfMass);
      double supportDistance = support.dot(minNormal);
      //      System.out.println("supportDistance = " + supportDistance);

      if (COLLISION_DEBUG_RENDERING) {
        for (int i = 0; i < 10; i++) {
          Vector3d particleLocation = new Vector3d(minNormal).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
          //          WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(COLORS[c % COLORS.length], 0.25f));
        }
        particle(Particle.SOUL_FIRE_FLAME, support, new Vector3d(COLLISION_DEBUG_OFFSET));
      }

      if (Math.abs(supportDistance - minDistance) > 0.001d) {
        minDistance = Double.MAX_VALUE;
        //        System.out.println("is not close");

        ArrayList<IntPair> uniqueEdges = new ArrayList<>();

        for (int i = 0; i < normals.size(); i++) {
          if (Simplex.sameDirection(new Vector3d(normals.get(i).x, normals.get(i).y, normals.get(i).z), support)) {
            //            System.out.println("is same direction");
            //            System.out.println(uniqueEdges);
            int face = i * 3;

            addUniqueEdge(uniqueEdges, faces, face + 0, face + 1);
            addUniqueEdge(uniqueEdges, faces, face + 1, face + 2);
            addUniqueEdge(uniqueEdges, faces, face + 2, face + 0);

            //            System.out.println("faces = " + faces);

            faces.set(face + 2, faces.get(faces.size() - 1));
            faces.remove(faces.size() - 1);
            faces.set(face + 1, faces.get(faces.size() - 1));
            faces.remove(faces.size() - 1);
            faces.set(face + 0, faces.get(faces.size() - 1));
            faces.remove(faces.size() - 1);

            //            System.out.println("faces = " + faces);

            normals.set(i, normals.get(normals.size() - 1));
            normals.remove(normals.size() - 1);

            i--;
          }
        }

        ArrayList<Integer> newFaces = new ArrayList<>();
        for (IntPair edge : uniqueEdges) {
          newFaces.add(edge.a());
          newFaces.add(edge.b());
          newFaces.add(polytope.size());
        }
        polytope.add(support);
        //        System.out.println("newFaces = " + newFaces.size() / 3);
        //        System.out.println("polytope = " + polytope.size());

        Pair<ArrayList<Vector4d>, Integer> newFaceData = getFaceNormals(polytope, newFaces);
        ArrayList<Vector4d> newNormals = newFaceData.getA();
        int newClosestFace = newFaceData.getB();

        //        System.out.println("newNormals = " + newNormals.size());

        double oldMinDistance = Double.MAX_VALUE;
        for (int i = 0; i < normals.size(); i++) {
          if (normals.get(i).w < oldMinDistance) {
            oldMinDistance = normals.get(i).w;
            closestFace = i;
          }
        }

        //        System.out.println("oldMinDistance = " + oldMinDistance);

        if (newNormals.get(newClosestFace).w < oldMinDistance) {
          closestFace = newClosestFace + normals.size();
        }

        //        System.out.println("we got closest");

        faces.addAll(newFaces);
        normals.addAll(newNormals);
      }
    }

    Vector3d collisionNormal = new Vector3d(minNormal);
    double distanceInset = minDistance + 0.001;

    // This is dumb
    // There has to be a better way
    //    System.out.println("closestFace = " + closestFace);
    //    System.out.println("faces = " + faces);
    //    System.out.println("polytope = " + polytope);
    Vector3d vertexA = polytope.get(faces.get(closestFace * 3));
    Vector3d vertexB = polytope.get(faces.get(closestFace * 3 + 1));
    Vector3d vertexC = polytope.get(faces.get(closestFace * 3 + 2));
    Vector3d barycentricCoefficients = barycentric(collisionNormal.mul(distanceInset, new Vector3d()), vertexA, vertexB, vertexC);

    Vector3d vertexThisA = mesh.getFurthestPoint(vertexA);
    Vector3d vertexThisB = mesh.getFurthestPoint(vertexB);
    Vector3d vertexThisC = mesh.getFurthestPoint(vertexC);
    Vector3d collisionPoint = new Vector3d(vertexThisA).mul(barycentricCoefficients.x).add(new Vector3d(vertexThisB).mul(barycentricCoefficients.y)).add(new Vector3d(vertexThisC).mul(barycentricCoefficients.z));

    collisionPoint.add(centerOfMass);

    if (COLLISION_DEBUG_RENDERING) {
      particleVector(collisionPoint, collisionNormal);

      for (int i = 0; i < 10; i++) {
        Vector3d particleLocation = new Vector3d(collisionNormal.mul(distanceInset, new Vector3d())).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));
      }

      for (int i = 0; i < 10; i++) {
        Vector3d particleLocation = new Vector3d(vertexA).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));
      }
      for (int i = 0; i < 10; i++) {
        Vector3d particleLocation = new Vector3d(vertexB).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));
      }
      for (int i = 0; i < 10; i++) {
        Vector3d particleLocation = new Vector3d(vertexC).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));
      }
      WORLD.spawnParticle(Particle.REDSTONE, collisionPoint.x, collisionPoint.y, collisionPoint.z, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 0.5f));
    }

    //    return null;
    return new CollisionData(collisionPoint, collisionNormal, distanceInset);
  }

  Vector3d barycentric(Vector3d p, Vector3d a, Vector3d b, Vector3d c) {
    Vector3d v0 = b.sub(a, new Vector3d());
    Vector3d v1 = c.sub(a, new Vector3d());
    Vector3d v2 = p.sub(a, new Vector3d());
    double d00 = v0.dot(v0);
    double d01 = v0.dot(v1);
    double d11 = v1.dot(v1);
    double d20 = v2.dot(v0);
    double d21 = v2.dot(v1);
    double denom = d00 * d11 - d01 * d01;
    double v = (d11 * d20 - d01 * d21) / denom;
    double w = (d00 * d21 - d01 * d20) / denom;
    return new Vector3d(1.0 - v - w, v, w);
  }

  public void addUniqueEdge(ArrayList<IntPair> uniqueEdges, ArrayList<Integer> faces, int a, int b) {
    //      0--<--3
    //     / \ B /   A: 2-0
    //    / A \ /    B: 0-2
    //   1-->--2

    IntPair reverse = new IntPair(faces.get(b), faces.get(a));
    //          System.out.println("Contains: " + uniqueEdges.contains(reverse) + " | " + reverse.a() + " | " + reverse.b());

    for (IntPair uniqueEdge : uniqueEdges) {
      if (uniqueEdge.equals(reverse)) {
        uniqueEdges.remove(uniqueEdge);
        return;
      }
    }
    uniqueEdges.add(new IntPair(faces.get(a), faces.get(b)));
  }

  ;

  Pair<ArrayList<Vector4d>, Integer> getFaceNormals(ArrayList<Vector3d> polytope, ArrayList<Integer> faces) {
    ArrayList<Vector4d> normals = new ArrayList<>();
    int minTriangle = 0;
    double minDistance = Double.MAX_VALUE;

    //    System.out.println("Faces: " + faces.size() / 3);
    for (int i = 0; i < faces.size(); i += 3) {
      Vector3d a = polytope.get(faces.get(i));
      Vector3d b = polytope.get(faces.get(i + 1));
      Vector3d c = polytope.get(faces.get(i + 2));

      Vector3d normal = b.sub(a, new Vector3d()).cross(c.sub(a, new Vector3d())).normalize();
      double distance = a.dot(normal);

      if (distance < 0) {
        normal.negate();
        distance *= -1;
      }

      normals.add(new Vector4d(normal, distance));

      if (distance < minDistance) {
        minTriangle = i / 3;
        minDistance = distance;
      }
    }

    //    System.out.println("Normals: " + normals.size());
    return new Pair<ArrayList<Vector4d>, Integer>(normals, minTriangle);
  }

  public void onCollision(PhysicsEntity otherEntity, CollisionData collisionData) {
    particle(Particle.SOUL_FIRE_FLAME, collisionData.collisionPoint);
    //    System.out.println(this.hashCode() + " | " + otherEntity.hashCode());
    //    Position Resolution
    Vector3d move = new Vector3d(collisionData.normal).mul(collisionData.distanceInset).div(mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
    if (active) centerOfMass.sub(move.mul(mesh.getInverseMass(), new Vector3d()));
    if (otherEntity.active) otherEntity.centerOfMass.add(move.mul(otherEntity.mesh.getInverseMass()));

    //    //      Calculate momentum
    //    // m1v1 + m2v2 = m1v1' + m2v2'
    //    // v1' = (v1(m1 - m2) + 2m2v2) / (m1 + m2)
    //
    //    float mass = mesh.getMass();
    //    float otherMass = otherEntity.mesh.getMass();
    //    Vector3d finalVelocity = new Vector3d(velocity).mul(mass - otherMass).add(new Vector3d(otherEntity.velocity).mul(2 * otherMass)).div(mass + otherMass);
    //    Vector3d otherFinalVelocity = new Vector3d(otherEntity.velocity).mul(otherMass - mass).add(new Vector3d(velocity).mul(2 * mass)).div(mass + otherMass);
    //    if (active && otherEntity.active) {
    //      velocity.set(finalVelocity);
    //      otherEntity.velocity.set(otherFinalVelocity);
    //    } else if (active) {
    //      velocity.set(finalVelocity.add(otherEntity.velocity.negate()));
    //    } else if (otherEntity.active) {
    //      otherEntity.velocity.set(otherFinalVelocity.add(velocity.negate()));
    //    }

    Vector3d relativeVelocity = otherEntity.velocity.sub(velocity, new Vector3d());
    double velocityAlongNormal = relativeVelocity.dot(collisionData.normal);

    if (velocityAlongNormal > 0) return;

    //    The bounciness / how much energy is conserved
    //    The impulse made of the velocity along the normal and the mass
    double impulse = -(1 + (elasticity + otherEntity.elasticity) / 2) * velocityAlongNormal / (mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
    //    System.out.println("impulse = " + impulse);
    Vector3d impulseVector = collisionData.normal.mul(impulse, new Vector3d());
    //    System.out.println("impulseVector = " + impulseVector);
    if (active) velocity.sub(impulseVector.mul(mesh.getInverseMass(), new Vector3d()));
    //    System.out.println("velocity = " + velocity);
    if (otherEntity.active) otherEntity.velocity.add(impulseVector.mul(otherEntity.mesh.getInverseMass()));
    //    System.out.println("otherEntity.velocity = " + otherEntity.velocity);

    //    Rotational Resolution
    Vector3d rA = collisionData.collisionPoint.sub(centerOfMass, new Vector3d());
    //    System.out.println("rA = " + rA);
    Vector3d rB = collisionData.collisionPoint.sub(otherEntity.centerOfMass, new Vector3d());
    //    System.out.println("rB = " + rB);
    Vector3d torqueA = rA.cross(impulseVector, new Vector3d());
    //    System.out.println("torqueA = " + torqueA);
    Vector3d torqueB = rB.cross(impulseVector, new Vector3d());
    particleVector(collisionData.collisionPoint, torqueA, Particle.FLAME);
    particleVector(collisionData.collisionPoint, torqueB, Particle.FLAME);
    //    System.out.println("torqueB = " + torqueB);
    if (active) angularMomentum.sub(torqueA.mul(mesh.getInverseInertiaTensor()).mul(DELTA_TIME));
    //    if (active) angularVelocity.add(new Vector3d(torqueA).mul(mesh.getInverseInertiaTensor()).mul(DELTA_TIME));
    //    System.out.println("angularMomentum = " + angularMomentum);
    if (otherEntity.active) otherEntity.angularMomentum.add(torqueB.mul(otherEntity.mesh.getInverseInertiaTensor()).mul(DELTA_TIME));
    //      otherEntity.angularVelocity.sub(new Vector3d(torqueB).mul(otherEntity.mesh.getInverseInertiaTensor()).mul(DELTA_TIME));
    //    System.out.println("otherEntity.angularMomentum = " + otherEntity.angularMomentum);

    // Friction stuff
    relativeVelocity = otherEntity.velocity.sub(velocity, new Vector3d());
    //    System.out.println("relativeVelocity = " + relativeVelocity);
    velocityAlongNormal = relativeVelocity.dot(collisionData.normal);
    //    System.out.println("velocityAlongNormal = " + velocityAlongNormal);

    //    System.out.println("collisionData.normal = " + collisionData.normal);
    Vector3d tangent = relativeVelocity.sub(collisionData.normal.mul(velocityAlongNormal, new Vector3d()), new Vector3d());
    if (tangent.lengthSquared() > 0.0001)
      tangent.normalize();
    //    System.out.println("tangent = " + tangent);
    double velocityAlongTangent = relativeVelocity.dot(tangent);
    //    System.out.println("velocityAlongTangent = " + velocityAlongTangent);

    double mu = new Vector2d(staticFriction, otherEntity.staticFriction).length();
    //    System.out.println("dynamicFriction = " + dynamicFriction);
    double frictionImpulse = -velocityAlongTangent / (mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
    //    System.out.println("frictionImpulse = " + frictionImpulse);

    Vector3d frictionImpulseVector = new Vector3d();
    if (Math.abs(frictionImpulse) < impulse * mu) {
      frictionImpulseVector = new Vector3d(tangent).mul(frictionImpulse);
      //      System.out.println("1 frictionImpulseVector = " + frictionImpulseVector);
    } else {
      mu = new Vector2d(dynamicFriction, otherEntity.dynamicFriction).length();
      frictionImpulseVector = new Vector3d(tangent).mul(-impulse * mu);
      //      System.out.println("2 frictionImpulseVector = " + frictionImpulseVector);
    }

    if (active)
      velocity.sub(frictionImpulseVector.mul(mesh.getInverseMass(), new Vector3d()).mul(DELTA_TIME));
    //    System.out.println("velocity = " + velocity);
    if (otherEntity.active)
      otherEntity.velocity.add(frictionImpulseVector.mul(otherEntity.mesh.getInverseMass()).mul(DELTA_TIME));
    //    System.out.println("otherEntity.velocity = " + otherEntity.velocity);
  }

  public void applyForce(Vector3d force) {
    velocity.add(new Vector3d(force).div(mesh.getMass()));
  }

  public void applyMasslessForce(Vector3d force) {
    velocity.add(new Vector3d(force));
  }

  public void applyForce(Vector3d force, Vector3d position) {
    velocity.add(new Vector3d(force).div(mesh.getMass()));
    //    angularMomentum.add(new Vector3d(position).sub(centerOfMass).cross(force));
  }

  public void applyMasslessForce(Vector3d force, Vector3d position) {
    velocity.add(new Vector3d(force));
  }

  public void particleVector(Vector3d location, Vector3d vector, Particle particle) {
    for (int i = 0; i < 10; i++) {
      Vector3d particleLocation = new Vector3d(location).add(new Vector3d(vector).mul(i / 10f));
      particle(particle, particleLocation);
    }
    //    particle(Particle.SOUL_FIRE_FLAME, location);
  }

  public void particleVector(Vector3d location, Vector3d vector) {
    particleVector(location, vector, Particle.FLAME);
  }

  public void particle(Particle particle, Vector3d location) {
    particle(particle, location, new Vector3d());
  }

  public void particle(Particle particle, Vector3d location, Vector3d offset) {
    if (DEBUG)
      particles.add(new ParticleData(particle, location.add(offset, new Vector3d())));
  }

  public void showParticles() {
    for (ParticleData particleData : particles) {
      WORLD.spawnParticle(particleData.particle, particleData.location.x, particleData.location.y, particleData.location.z, 0, 0, 0, 0);
    }
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
    textDisplay.setAlignment(TextDisplay.TextAlignment.LEFT);
    textDisplay.setTeleportDuration(1);
    return textDisplay;
  }

  public static Interaction createInteraction(BlockDisplay blockDisplay) {
    Interaction interaction = (Interaction) blockDisplay.getWorld().spawnEntity(blockDisplay.getLocation(), EntityType.INTERACTION);
    interaction.setInteractionHeight(1.1f);
    interaction.setInteractionWidth(1.1f);
    return interaction;
  }
}
