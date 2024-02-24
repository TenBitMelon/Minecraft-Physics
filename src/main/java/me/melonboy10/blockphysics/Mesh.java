package me.melonboy10.blockphysics;

import lombok.Getter;
import org.bukkit.util.BoundingBox;
import org.joml.Matrix3d;
import org.joml.Vector3d;

public class Mesh {
  private @Getter BoundingBox[] regions;
  private @Getter Vector3d[] vertices;
  private @Getter Matrix3d inverseInertiaTensor;
  private @Getter float volume;

  public Mesh(BoundingBox[] regions) {
    this.regions = regions;
    recenter();
  }

  public void addRegion(BoundingBox region) {
    BoundingBox[] newRegions = new BoundingBox[regions.length + 1];
    System.arraycopy(regions, 0, newRegions, 0, regions.length);
    newRegions[regions.length] = region;
    regions = newRegions;

    recenter();
  }

  private void recenter() {
    calculateVolume();
    Vector3d centerOfMass = getCenterOfMass();
    for (BoundingBox region : regions) {
      region.shift(centerOfMass.x, centerOfMass.y, centerOfMass.z);
    }
    calculateInverseInertiaTensor();
    calculateVertices();
  }

  public BoundingBox isContained(Vector3d point) {
    for (BoundingBox region : regions) {
      if (region.contains(point.x, point.y, point.z)) return region;
    }
    return null;
  }

  private void calculateVertices() {
    vertices = new Vector3d[regions.length * 8];
    for (int i = 0; i < regions.length; i++) {
      BoundingBox region = regions[i];
      vertices[i * 8 + 0] = (new Vector3d((float) region.getMinX(), (float) region.getMinY(), (float) region.getMinZ()));
      vertices[i * 8 + 1] = (new Vector3d((float) region.getMaxX(), (float) region.getMinY(), (float) region.getMinZ()));
      vertices[i * 8 + 2] = (new Vector3d((float) region.getMinX(), (float) region.getMaxY(), (float) region.getMinZ()));
      vertices[i * 8 + 3] = (new Vector3d((float) region.getMaxX(), (float) region.getMaxY(), (float) region.getMinZ()));
      vertices[i * 8 + 4] = (new Vector3d((float) region.getMinX(), (float) region.getMinY(), (float) region.getMaxZ()));
      vertices[i * 8 + 5] = (new Vector3d((float) region.getMaxX(), (float) region.getMinY(), (float) region.getMaxZ()));
      vertices[i * 8 + 6] = (new Vector3d((float) region.getMinX(), (float) region.getMaxY(), (float) region.getMaxZ()));
      vertices[i * 8 + 7] = (new Vector3d((float) region.getMaxX(), (float) region.getMaxY(), (float) region.getMaxZ()));
    }
  }

  private void calculateVolume() {
    float volume = 0;
    for (BoundingBox region : regions) {
      Vector3d size = new Vector3d((float) region.getMaxX() - (float) region.getMinX(), (float) region.getMaxY() - (float) region.getMinY(), (float) region.getMaxZ() - (float) region.getMinZ());
      volume += size.x * size.y * size.z;
    }
    this.volume = volume;
  }

  private Vector3d getCenterOfMass() {
    Vector3d centerOfMass = new Vector3d();
    for (BoundingBox region : regions) {
      Vector3d size = new Vector3d((float) region.getMaxX() - (float) region.getMinX(), (float) region.getMaxY() - (float) region.getMinY(), (float) region.getMaxZ() - (float) region.getMinZ());
      centerOfMass.add(size.mul((float) region.getMaxX() + (float) region.getMinX(), (float) region.getMaxY() + (float) region.getMinY(), (float) region.getMaxZ() + (float) region.getMinZ()));
    }
    return centerOfMass.div(volume);
  }

  private void calculateInverseInertiaTensor() {
    Matrix3d inertiaTensor = new Matrix3d();
    float mass = volume;
    for (BoundingBox region : regions) {
      Vector3d size = new Vector3d((float) region.getMaxX() - (float) region.getMinX(), (float) region.getMaxY() - (float) region.getMinY(), (float) region.getMaxZ() - (float) region.getMinZ());
      inertiaTensor.m00 += mass * (size.y * size.y + size.z * size.z) / 12 + mass * (region.getCenter().toVector3d().sub(getCenterOfMass())).lengthSquared();
      inertiaTensor.m11 += mass * (size.x * size.x + size.z * size.z) / 12 + mass * (region.getCenter().toVector3d().sub(getCenterOfMass())).lengthSquared();
      inertiaTensor.m22 += mass * (size.x * size.x + size.y * size.y) / 12 + mass * (region.getCenter().toVector3d().sub(getCenterOfMass())).lengthSquared();
    }

    this.inverseInertiaTensor = inertiaTensor.invert();
  }

  public float getMass() {
    return volume;
  }
}
