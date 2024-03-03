package me.melonboy10.blockphysics;

// Collision Helpers

import org.joml.Vector3d;

public class Simplex {
  public Vector3d[] vertices = new Vector3d[4];
  public int dimension = 0;

  public void pushFront(Vector3d vertex) {
    vertices[3] = vertices[2];
    vertices[2] = vertices[1];
    vertices[1] = vertices[0];
    vertices[0] = vertex;
    dimension = Math.min(dimension + 1, 4);
  }

  public void set(Vector3d... vertex) {
    System.arraycopy(vertex, 0, vertices, 0, vertex.length);
    dimension = vertex.length;
  }

  public boolean containsOrigin(Vector3d direction) {
    return switch (dimension) {
      case 2 -> lineContainsOrigin(direction);
      case 3 -> triangleContainsOrigin(direction);
      case 4 -> tetrahedronContainsOrigin(direction);
      default -> false;
    };
  }

  public static boolean sameDirection(Vector3d a, Vector3d b) {
    return a.dot(b) > 0;
  }

  private boolean lineContainsOrigin(Vector3d direction) {
    Vector3d a = vertices[0];
    Vector3d b = vertices[1];

    Vector3d ab = b.sub(a, new Vector3d());
    Vector3d ao = a.negate(new Vector3d());

    if (sameDirection(ab, ao)) {
      direction.set(ab.cross(ao, new Vector3d()).cross(ab));
    } else {
      set(a);
      direction.set(ao);
    }

    return false;
  }

  private boolean triangleContainsOrigin(Vector3d direction) {
    Vector3d a = vertices[0];
    Vector3d b = vertices[1];
    Vector3d c = vertices[2];

    Vector3d ab = b.sub(a, new Vector3d());
    Vector3d ac = c.sub(a, new Vector3d());
    Vector3d ao = a.negate(new Vector3d());

    Vector3d abc = ab.cross(ac, new Vector3d());

    if (sameDirection(abc.cross(ac, new Vector3d()), ao)) {
      if (sameDirection(ac, ao)) {
        set(a, c);
        direction.set(ac.cross(ao, new Vector3d()).cross(ac));
      } else {
        set(a, b);
        return lineContainsOrigin(direction);
      }
    } else {
      if (sameDirection(ab.cross(abc, new Vector3d()), ao)) {
        set(a, b);
        return lineContainsOrigin(direction);
      } else {
        if (sameDirection(abc, ao)) {
          direction.set(abc);
        } else {
          set(a, c, b);
          direction.set(abc.negate(new Vector3d()));
        }
      }
    }

    return false;
  }

  private boolean tetrahedronContainsOrigin(Vector3d direction) {
    Vector3d a = vertices[0];
    Vector3d b = vertices[1];
    Vector3d c = vertices[2];
    Vector3d d = vertices[3];

    Vector3d ab = b.sub(a, new Vector3d());
    Vector3d ac = c.sub(a, new Vector3d());
    Vector3d ad = d.sub(a, new Vector3d());
    Vector3d ao = a.negate(new Vector3d());

    Vector3d abc = ab.cross(ac, new Vector3d());
    Vector3d acd = ac.cross(ad, new Vector3d());
    Vector3d adb = ad.cross(ab, new Vector3d());

    if (sameDirection(abc, ao)) {
      set(a, b, c);
      return triangleContainsOrigin(direction);
    }
    if (sameDirection(acd, ao)) {
      set(a, c, d);
      return triangleContainsOrigin(direction);
    }
    if (sameDirection(adb, ao)) {
      set(a, d, b);
      return triangleContainsOrigin(direction);
    }

    return true;
  }
}
