package me.melonboy10.blockphysics;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;
import org.bukkit.util.BoundingBox;
import org.joml.*;
import oshi.util.tuples.Pair;

import java.lang.Math;
import java.util.ArrayList;
import java.util.Arrays;

import static me.melonboy10.blockphysics.PhysicsWorld.*;

public abstract class PhysicsEntity {
  public static final boolean COLLISION_DEBUG_RENDERING = false;
  public static final Vector3d COLLISION_DEBUG_OFFSET = new Vector3d(0, 90, 50);
  public static final Color[] COLORS = new Color[]{Color.GREEN, Color.BLUE, Color.YELLOW, Color.PURPLE, Color.ORANGE, Color.TEAL, Color.LIME, Color.AQUA, Color.FUCHSIA, Color.MAROON, Color.OLIVE, Color.NAVY, Color.SILVER, Color.GRAY, Color.RED, Color.WHITE, Color.BLACK};

  boolean active = true;

  Mesh mesh;
  Vector3d centerOfMass;
  Vector3d velocity;
  Vector3d angularVelocity;
  //  Vector3d angularMomentum;
  Quaterniond rotation;
  double elasticity = 0.4;
  double staticFriction = 0.4;
  double dynamicFriction = 0.5;

  public static class ParticleData {
    protected final Particle particle;
    protected final Vector3d location;

    public ParticleData(Particle particle, Vector3d location) {
      this.particle = particle;
      this.location = location;
    }

    public void spawn(World world) {
      WORLD.spawnParticle(particle, location.x, location.y, location.z, 0, 0, 0, 0);
    }
  }

  public static class ColorParticleData extends ParticleData {
    private final Color color;

    public ColorParticleData(Color color, Vector3d location) {
      super(Particle.REDSTONE, location);
      this.color = color;
    }

    @Override
    public void spawn(World world) {
      WORLD.spawnParticle(particle, location.x, location.y, location.z, 0, 0, 0, 0, new Particle.DustOptions(color, 0.1f));
    }
  }

  ArrayList<ParticleData> particles = new ArrayList<>();

  public PhysicsEntity(Location position, Mesh mesh) {
    this.mesh = mesh;
    this.centerOfMass = position.toVector().toVector3d();
    this.velocity = new Vector3d();
//    this.angularMomentum = new Vector3d(0, 0, 0);
    this.angularVelocity = new Vector3d();
    this.rotation = new Quaterniond();
  }

  public PhysicsEntity(Location position) {
    this(position, new Mesh(new BoundingBox[]{new BoundingBox(-0.5, -0.5, -0.5, 0.5, 0.5, 0.5)}));
  }

  public void update() {
    /* PARTICLES FOR WHERE IT IS */
    //    System.out.println(this.hashCode());
    if (DEBUG) {
      Particle particle = active ? Particle.ELECTRIC_SPARK : Particle.SCRAPE;
      for (BoundingBox region : mesh.getRegions()) {
        Vector3d min = new Vector3d(region.getMinX(), region.getMinY(), region.getMinZ());
        Vector3d max = new Vector3d(region.getMaxX(), region.getMaxY(), region.getMaxZ());
        particle(particle, new Vector3d(min.x, min.y, max.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(min.x, max.y, min.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(max.x, min.y, min.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(min.x, max.y, max.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(max.x, min.y, max.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(max.x, max.y, min.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(min.x, min.y, min.z).rotate(rotation).add(centerOfMass));
        particle(particle, new Vector3d(max.x, max.y, max.z).rotate(rotation).add(centerOfMass));
      }

      if (DEBUG_LEVEL == -1) {
        particleVector(centerOfMass, velocity, Particle.ELECTRIC_SPARK);
        particleVector(centerOfMass, angularVelocity, Particle.WATER_BUBBLE);
//        particleVector(centerOfMass, angularMomentum, Particle.WATER_BUBBLE);
        particle(Particle.WAX_ON, centerOfMass);
      }
    }

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
//      Vector3d angularVelocity = new Vector3d(this.angularMomentum).mul(mesh.getInverseInertiaTensor().rotate(rotation, new Matrix3d()));

      Quaterniond deltaRotation = new Quaterniond(angularVelocity.x * DELTA_TIME, angularVelocity.y * DELTA_TIME, angularVelocity.z * DELTA_TIME, 0).mul(rotation).mul(0.5);
      rotation.add(deltaRotation);


      velocity.add(0, -GRAVITY * DELTA_TIME, 0);

    } else {
      velocity.set(0);
      angularVelocity.set(0);
//      angularMomentum.set(0);
    }
  }

  public abstract void destroy();

  public record CollisionData(Vector3d collisionPoint, Vector3d normal, double distanceInset) {}

  public CollisionData collidesWith(PhysicsEntity otherEntity) {
//    Quaterniond relativeRotation = new Quaterniond();
    Quaterniond relativeRotation = new Quaterniond(rotation).invert().mul(otherEntity.rotation);
    Vector3d support = mesh.getMinkowskiDifferencePoint(otherEntity.mesh, new Vector3d(1, 0, 0), relativeRotation).add(centerOfMass).sub(otherEntity.centerOfMass);

    Simplex simplex = new Simplex();
    simplex.pushFront(support);

    Vector3d direction = support.negate(new Vector3d());

    if (COLLISION_DEBUG_RENDERING) {
      for (Vector3d vertex : mesh.getVertices()) {
        for (Vector3d meshVertex : otherEntity.mesh.getVertices()) {
          particle(Color.fromRGB(114, 214, 214), new Vector3d(vertex).add(centerOfMass).sub(meshVertex.rotate(relativeRotation, new Vector3d())).sub(otherEntity.centerOfMass), COLLISION_DEBUG_OFFSET);
        }
      }
      particle(Particle.COMPOSTER, COLLISION_DEBUG_OFFSET);
    }

    for (int i = 0; i < 100; i++) {
      if (COLLISION_DEBUG_RENDERING)
        for (int a = 0; a < 10; a++) {
          particle(Color.OLIVE, new Vector3d(direction).mul(a / 10f).add(COLLISION_DEBUG_OFFSET));
        }
      support = mesh.getMinkowskiDifferencePoint(otherEntity.mesh, direction, relativeRotation).add(centerOfMass).sub(otherEntity.centerOfMass);
//      System.out.println(support);

      if (COLLISION_DEBUG_RENDERING) {
        particle(Color.fromRGB(255, 207, 97), support, COLLISION_DEBUG_OFFSET);
      }

      if (support.dot(direction) <= 0) return null; // Is not in the direction of the origin
      simplex.pushFront(support);

      if (COLLISION_DEBUG_RENDERING) {
        System.out.println(Arrays.toString(simplex.vertices));
        for (Vector3d vertex : simplex.vertices) {
          if (vertex != null)
            particle(Particle.FLAME, vertex, COLLISION_DEBUG_OFFSET);
        }
      }

      //      PhysicsWorld.addDebugLine(Component.text("Direction: %s".formatted(direction)));

      if (simplex.containsOrigin(direction)) {
        if (COLLISION_DEBUG_RENDERING)
          for (Vector3d vertex : simplex.vertices) {
            particle(Particle.GLOW, vertex, COLLISION_DEBUG_OFFSET);
          }
        return EPACollidesWith(simplex, otherEntity);
//        return new CollisionData(new Vector3d(), new Vector3d(), 0);
      }
    }

    return null;
  }


  public CollisionData EPACollidesWith(Simplex simplex, PhysicsEntity otherEntity) {
    //    System.out.println("==================== CHECKING COLLISION ====================");
    Quaterniond relativeRotation = new Quaterniond(rotation).invert().mul(otherEntity.rotation);
    ArrayList<Vector3d> polytope = new ArrayList<>(Arrays.stream(simplex.vertices).map(Vector3d::new).toList());
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
    int minFace = faceData.getB();

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

        if (c == DEBUG_LEVEL) {
          for (int j = 0; j < faces.size(); j += 3) {
            Vector3d a1 = polytope.get(faces.get(j));
            Vector3d b1 = polytope.get(faces.get(j + 1));
            Vector3d c1 = polytope.get(faces.get(j + 2));

            for (int i = 0; i < 10; i++) {
              Vector3d particleLocation = new Vector3d(a1).sub(b1).mul(i / 10f).add(COLLISION_DEBUG_OFFSET).add(b1);
              WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));

              particleLocation = new Vector3d(b1).sub(c1).mul(i / 10f).add(COLLISION_DEBUG_OFFSET).add(c1);
              WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));

              particleLocation = new Vector3d(c1).sub(a1).mul(i / 10f).add(COLLISION_DEBUG_OFFSET).add(a1);
              WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
            }

            Vector3d mid = new Vector3d(a1).add(b1).add(c1).div(3).add(COLLISION_DEBUG_OFFSET);
            WORLD.spawnParticle(Particle.REDSTONE, mid.x, mid.y, mid.z, 0, 0, 0, 0, new Particle.DustOptions(Color.RED, 0.5f));
          }
          return null;
        }
      }

      //      PhysicsWorld.addDebugLine(Component.text("Faces: %s".formatted(faces)));
      //      System.out.println("\nc = " + c);

      minNormal = new Vector3d(normals.get(minFace).x, normals.get(minFace).y, normals.get(minFace).z);
      minDistance = normals.get(minFace).w;


//      System.out.println("minDistance = " + minDistance);

      Vector3d support = mesh.getMinkowskiDifferencePoint(otherEntity.mesh, minNormal, relativeRotation).add(centerOfMass).sub(otherEntity.centerOfMass);
      double supportDistance = minNormal.dot(support);
//      System.out.println("supportDistance = " + supportDistance);

      if (COLLISION_DEBUG_RENDERING) {
        for (int i = 0; i < 10; i++) {
          Vector3d particleLocation = new Vector3d(minNormal).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
          WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(COLORS[c % COLORS.length], 0.25f));
        }
        particle(Particle.SOUL_FIRE_FLAME, support, new Vector3d(COLLISION_DEBUG_OFFSET));
      }

      if (Math.abs(supportDistance - minDistance) > 0.001d) {
        minDistance = Double.MAX_VALUE;
        //        System.out.println("is not close");

        ArrayList<IntPair> uniqueEdges = new ArrayList<>();

        for (int i = 0; i < normals.size(); i++) {
          if (new Vector3d(normals.get(i).x, normals.get(i).y, normals.get(i).z).dot(support) > new Vector3d(normals.get(i).x, normals.get(i).y, normals.get(i).z).dot(polytope.get(faces.get(i * 3)))) {
            //            System.out.println("is same direction");
            //            System.out.println(uniqueEdges);

//            addUniqueEdge(uniqueEdges, faces, i * 3, i * 3 + 1);
//            addUniqueEdge(uniqueEdges, faces, i * 3 + 1, i * 3 + 2);
//            addUniqueEdge(uniqueEdges, faces, i * 3 + 2, i * 3);
//
//            //            System.out.println(uniqueEdges);
//
//            faces.remove(i * 3 + 2);
//            faces.remove(i * 3 + 1);
//            faces.remove(i * 3);
//            normals.remove(i);
//
//            i -= 1;


            int face = i * 3;

            addUniqueEdge(uniqueEdges, faces, face + 0, face + 1);
            addUniqueEdge(uniqueEdges, faces, face + 1, face + 2);
            addUniqueEdge(uniqueEdges, faces, face + 2, face + 0);

//            faces.remove(i * 3 + 2);
//            faces.remove(i * 3 + 1);
//            faces.remove(i * 3);
//            normals.remove(i);

            //            System.out.println("faces = " + faces);
            // This could probably be relaced with whats above, but this works and I'm not going to touch it
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
        int newMinFace = newFaceData.getB();

//        System.out.println("newNormals = " + newNormals.size());

        double oldMinDistance = Double.MAX_VALUE;
        for (int i = 0; i < normals.size(); i++) {
          if (normals.get(i).w < oldMinDistance) {
            oldMinDistance = normals.get(i).w;
            minFace = i;
          }
        }

//        System.out.println("oldMinDistance = " + oldMinDistance);

        if (newNormals.get(newMinFace).w < oldMinDistance) {
          minFace = newMinFace + normals.size();
        }

//        System.out.println("we got closest");

        faces.addAll(newFaces);
        normals.addAll(newNormals);
      }
    }

    Vector3d collisionNormal = new Vector3d(minNormal);
//    System.out.println("minDistance2 = " + minDistance);
//    System.out.println("c = " + c);
    double distanceInset = minDistance + 0.001;

    // This is dumb
    // There has to be a better way
//    System.out.println("closestFace = " + minFace);
//    System.out.println("faces = " + faces);
//    System.out.println("polytope = " + polytope);
    Vector3d vertexA = polytope.get(faces.get(minFace * 3));
    Vector3d vertexB = polytope.get(faces.get(minFace * 3 + 1));
    Vector3d vertexC = polytope.get(faces.get(minFace * 3 + 2));
//    System.out.println("vertexA = " + vertexA);
//    System.out.println("vertexB = " + vertexB);
//    System.out.println("vertexC = " + vertexC);
//    System.out.println("collisionNormal = " + collisionNormal);
//    System.out.println("distanceInset = " + distanceInset);
    Vector3d barycentricCoefficients = barycentric(collisionNormal.mul(distanceInset, new Vector3d()), vertexA, vertexB, vertexC);
//    System.out.println("barycentricCoefficients = " + barycentricCoefficients);

    Vector3d vertexThisA = mesh.getFurthestPoint(vertexA, null);
    Vector3d vertexThisB = mesh.getFurthestPoint(vertexB, null);
    Vector3d vertexThisC = mesh.getFurthestPoint(vertexC, null);
//    System.out.println("vertexThisA = " + vertexThisA);
//    System.out.println("vertexThisB = " + vertexThisB);
//    System.out.println("vertexThisC = " + vertexThisC);
    Vector3d collisionPoint = new Vector3d(vertexThisA).mul(barycentricCoefficients.x).add(new Vector3d(vertexThisB).mul(barycentricCoefficients.y)).add(new Vector3d(vertexThisC).mul(barycentricCoefficients.z));
//    System.out.println("collisionPoint = " + collisionPoint);

    collisionPoint.add(centerOfMass);

    if (COLLISION_DEBUG_RENDERING) {
      particleVector(collisionPoint, collisionNormal);

      for (int i = 0; i < 10; i++) {
        Vector3d particleLocation = new Vector3d(collisionNormal.mul(distanceInset, new Vector3d())).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));

        particleLocation = new Vector3d(vertexA).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));

        particleLocation = new Vector3d(vertexB).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));

        particleLocation = new Vector3d(vertexC).mul(i / 10f).add(COLLISION_DEBUG_OFFSET);
        WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(Color.SILVER, 0.25f));
      }
      WORLD.spawnParticle(Particle.REDSTONE, collisionPoint.x, collisionPoint.y, collisionPoint.z, 0, 0, 0, 0, new Particle.DustOptions(Color.AQUA, 0.5f));
    }

    //    return null;
    return new CollisionData(collisionPoint, collisionNormal.normalize(), distanceInset);
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

    if (uniqueEdges.contains(reverse)) {
      uniqueEdges.remove(reverse);
    } else {
      uniqueEdges.add(new IntPair(faces.get(a), faces.get(b)));
    }
  }

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
    return new Pair<>(normals, minTriangle);
  }

  public void onCollision(PhysicsEntity otherEntity, CollisionData collisionData) {
    WORLD.spawnParticle(Particle.SOUL_FIRE_FLAME, collisionData.collisionPoint.x, collisionData.collisionPoint.y, collisionData.collisionPoint.z, 0, 0, 0, 0);

    Vector3d rA = collisionData.collisionPoint.sub(centerOfMass, new Vector3d());
    Vector3d rB = collisionData.collisionPoint.sub(otherEntity.centerOfMass, new Vector3d());

//    Vector3d angularVelocityA = new Vector3d(angularMomentum).mul(mesh.getInverseInertiaTensor());
//    Vector3d angularVelocityB = new Vector3d(otherEntity.angularMomentum).mul(otherEntity.mesh.getInverseInertiaTensor());
    Vector3d angularVelocityA = new Vector3d(angularVelocity);
    Vector3d angularVelocityB = new Vector3d(otherEntity.angularVelocity);
    Vector3d velocityAtPointA = angularVelocityA.cross(rA, new Vector3d()).add(velocity);
    Vector3d velocityAtPointB = angularVelocityB.cross(rB, new Vector3d()).add(otherEntity.velocity);
    Vector3d relativeVelocity = velocityAtPointB.sub(velocityAtPointA, new Vector3d());

    double velocityAlongNormal = relativeVelocity.dot(collisionData.normal);
    if (velocityAlongNormal > 0) return;

    Vector3d move = new Vector3d(collisionData.normal).mul(collisionData.distanceInset).div(mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
    if (active) centerOfMass.sub(move.mul(mesh.getInverseMass(), new Vector3d()));
    if (otherEntity.active) otherEntity.centerOfMass.add(move.mul(otherEntity.mesh.getInverseMass(), new Vector3d()));

    double impulse = -(1 + (elasticity + otherEntity.elasticity) / 2) * velocityAlongNormal / (mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
    Vector3d impulseVector = collisionData.normal.mul(impulse, new Vector3d());
    Vector3d impulseVectorA = new Vector3d(impulseVector).mul(mesh.getInverseMass());
    Vector3d impulseVectorB = new Vector3d(impulseVector).mul(otherEntity.mesh.getInverseMass());
    if (active) velocity.sub(impulseVectorA);
    if (otherEntity.active) otherEntity.velocity.add(impulseVectorB);

    Vector3d torqueA = rA.cross(impulseVectorA, new Vector3d()).mul(mesh.getInverseInertiaTensor());
    Vector3d torqueB = rB.cross(impulseVectorB, new Vector3d()).mul(otherEntity.mesh.getInverseInertiaTensor());
    if (active) angularVelocity.sub(torqueA);
    if (otherEntity.active) otherEntity.angularVelocity.add(torqueB);
//    Vector3d torqueA = rA.cross(impulseVectorA, new Vector3d());
//    Vector3d torqueB = rB.cross(impulseVectorB, new Vector3d());
//    if (active) angularMomentum.sub(torqueA);
//    if (otherEntity.active) otherEntity.angularMomentum.add(torqueB);

    Vector3d tangent = relativeVelocity.sub(collisionData.normal.mul(velocityAlongNormal, new Vector3d()), new Vector3d());
    if (tangent.lengthSquared() > 0) {
      tangent.normalize();
      double impulseMagnitude = -relativeVelocity.dot(tangent) / (mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
      Vector3d impulseVectorTangent = new Vector3d(tangent).mul(impulseMagnitude);
      Vector3d impulseVectorATangent = new Vector3d(impulseVectorTangent).mul(mesh.getInverseMass());
      Vector3d impulseVectorBTangent = new Vector3d(impulseVectorTangent).mul(otherEntity.mesh.getInverseMass());
      if (active) velocity.sub(impulseVectorATangent);
      if (otherEntity.active) otherEntity.velocity.add(impulseVectorBTangent);

      Vector3d frictionTorqueA = rA.cross(impulseVectorATangent, new Vector3d()).mul(mesh.getInverseInertiaTensor());
      Vector3d frictionTorqueB = rB.cross(impulseVectorBTangent, new Vector3d()).mul(otherEntity.mesh.getInverseInertiaTensor());
      if (active) angularVelocity.sub(frictionTorqueA);
      if (otherEntity.active) otherEntity.angularVelocity.add(frictionTorqueB);
    }

    particleVector(Color.RED, centerOfMass, rA);
    particleVector(Color.MAROON, otherEntity.centerOfMass, rB);
    particleVector(Color.BLUE, collisionData.collisionPoint, collisionData.normal);
    particleVector(Color.YELLOW, collisionData.collisionPoint, relativeVelocity);
    particleVector(Color.GREEN, collisionData.collisionPoint.add(0.2, 0, 0), torqueA);
    particleVector(Color.GREEN, collisionData.collisionPoint.add(0.2, 0, 0), torqueB);
    particleVector(Color.ORANGE, collisionData.collisionPoint.add(0.2, 0, 0), impulseVectorA);
    particleVector(Color.ORANGE, collisionData.collisionPoint.add(0.2, 0, 0), impulseVectorB);

//    Vector3d velocityAtPointB = otherEntity.velocity.add(otherEntity.angularMomentum.cross(rB, new Vector3d()), new Vector3d());
//    System.out.println("relativeVelocity = " + relativeVelocity);
//    System.out.println("velocityAlongNormal = " + velocityAlongNormal);

//    L = Iω = r × p
//    p=mv
//    Known: r, I, L
//    Find: v
//    L = rmv
//    L/rm = v

//

//    System.out.println(this.hashCode() + " | " + otherEntity.hashCode());
//    System.out.println("collisionData = " + collisionData);
//        Position Resolution
//    System.out.println("centerOfMass = " + centerOfMass);
//    System.out.println("otherEntity.centerOfMass = " + otherEntity.centerOfMass);
//    System.out.println("inset = " + new Vector3d(collisionData.normal).mul(collisionData.distanceInset));
//    System.out.println("Inverse Mass = " + mesh.getInverseMass() + " | " + otherEntity.mesh.getInverseMass());
//    System.out.println("Added = " + (mesh.getInverseMass() + otherEntity.mesh.getInverseMass()));
//    System.out.println("move = " + move);
//    if (true) return;
//    System.out.println("centerOfMass = " + centerOfMass);
//    System.out.println("otherEntity.centerOfMass = " + otherEntity.centerOfMass);

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


    //    The bounciness / how much energy is conserved
    //    The impulse made of the velocity along the normal and the mass
//    System.out.println("impulse = " + impulse);
//    System.out.println("impulseVector = " + impulseVector);
//    System.out.println("velocity = " + velocity);
//    System.out.println("otherEntity.velocity = " + otherEntity.velocity);

    //    Rotational Torque
//    w'a = wa + J-1(r x jn)
//    w'b = wb - J-1(r x jn)
//    System.out.println("rB = " + rB);
//    System.out.println("torqueA = " + torqueA);
//    Vector3d torqueA = rA.cross(impulseVectorA, new Vector3d());
//    Vector3d torqueB = rB.cross(impulseVectorB, new Vector3d());
//    particleVector(collisionData.collisionPoint, torqueA, Particle.FLAME);
//    particleVector(collisionData.collisionPoint, torqueB, Particle.FLAME);
//    System.out.println("torqueB = " + torqueB);
//    if (active) angularMomentum.sub(torqueA);

    //    if (active) angularVelocity.add(new Vector3d(torqueA).mul(mesh.getInverseInertiaTensor()).mul(DELTA_TIME));
//    System.out.println("angularMomentum = " + angularMomentum);
//    if (otherEntity.active) otherEntity.angularMomentum.add(torqueB);

    //      otherEntity.angularVelocity.sub(new Vector3d(torqueB).mul(otherEntity.mesh.getInverseInertiaTensor()).mul(DELTA_TIME));
//    System.out.println("otherEntity.angularMomentum = " + otherEntity.angularMomentum);


//    // Friction stuff
//    relativeVelocity = otherEntity.velocity.sub(velocity, new Vector3d());
//    velocityAlongNormal = relativeVelocity.dot(collisionData.normal);
////    System.out.println("velocityAlongNormal = " + velocityAlongNormal);
//
////    System.out.println("collisionData.normal = " + collisionData.normal);
//    Vector3d tangent = relativeVelocity.sub(collisionData.normal.mul(velocityAlongNormal, new Vector3d()), new Vector3d());
//    if (tangent.lengthSquared() > 0.0001)
//      tangent.normalize();
////    System.out.println("tangent = " + tangent);
//    double velocityAlongTangent = relativeVelocity.dot(tangent);
////    System.out.println("velocityAlongTangent = " + velocityAlongTangent);
//
//    double mu = new Vector2d(staticFriction, otherEntity.staticFriction).length();
////    System.out.println("dynamicFriction = " + dynamicFriction);
//    double frictionImpulse = -velocityAlongTangent / (mesh.getInverseMass() + otherEntity.mesh.getInverseMass());
////    System.out.println("frictionImpulse = " + frictionImpulse);
//
//    Vector3d frictionImpulseVector;
//    if (Math.abs(frictionImpulse) < impulse * mu) {
//      frictionImpulseVector = new Vector3d(tangent).mul(frictionImpulse);
////      System.out.println("1 frictionImpulseVector = " + frictionImpulseVector);
//    } else {
//      mu = new Vector2d(dynamicFriction, otherEntity.dynamicFriction).length();
//      frictionImpulseVector = new Vector3d(tangent).mul(-impulse * mu);
////      System.out.println("2 frictionImpulseVector = " + frictionImpulseVector);
//    }
//
//    if (active)
//      velocity.sub(frictionImpulseVector.mul(mesh.getInverseMass(), new Vector3d()).mul(DELTA_TIME));
////    System.out.println("velocity = " + velocity);
//    if (otherEntity.active)
//      otherEntity.velocity.add(frictionImpulseVector.mul(otherEntity.mesh.getInverseMass()).mul(DELTA_TIME));
////    System.out.println("otherEntity.velocity = " + otherEntity.velocity);
//    STOP_NOW = true;
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

  public void particleVector(Color color, Vector3d location, Vector3d vector) {
    for (int i = 0; i < 10; i++) {
      Vector3d particleLocation = new Vector3d(location).add(new Vector3d(vector).mul(i / 10f));
      particle(color, particleLocation);
    }
  }

  public void particle(Particle particle, Vector3d location) {
    particle(particle, location, new Vector3d());
  }

  public void particle(Particle particle, Vector3d location, Vector3d offset) {
    if (DEBUG)
      particles.add(new ParticleData(particle, location.add(offset, new Vector3d())));
  }

  public void particle(Color color, Vector3d location, Vector3d offset) {
    Vector3d particleLocation = new Vector3d(location).add(offset);
    particles.add(new ColorParticleData(color, particleLocation));
//    WORLD.spawnParticle(Particle.REDSTONE, particleLocation.x, particleLocation.y, particleLocation.z, 0, 0, 0, 0, new Particle.DustOptions(color, 0.25f));
  }

  public void particle(Color color, Vector3d location) {
//    WORLD.spawnParticle(Particle.REDSTONE, location.x, location.y, location.z, 0, 0, 0, 0, new Particle.DustOptions(color, 0.25f));
    particles.add(new ColorParticleData(color, location));
  }

  public void showParticles() {
    for (ParticleData particleData : particles) {
      particleData.spawn(WORLD);
//      WORLD.spawnParticle(particleData.particle, particleData.location.x, particleData.location.y, particleData.location.z, 0, 0, 0, 0);
    }
  }

  public void clearParticles() {
    particles.clear();
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
