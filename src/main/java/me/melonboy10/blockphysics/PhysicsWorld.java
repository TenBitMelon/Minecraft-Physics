package me.melonboy10.blockphysics;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.joml.Vector3f;

import java.util.ArrayList;

public class PhysicsWorld {
  public static final float GRAVITY = 1f;
  public static final float ELASTICITY = 0.8f;
  public static float DELTA_TIME = 1 / 400f;

  public static World WORLD;

  public static boolean STOP_NOW = false;
  public static boolean DEBUG = true;
  public static int DEBUG_LEVEL = 5;

  public static final ArrayList<PhysicsEntity> entities = new ArrayList<>();

  public void addEntity(PhysicsEntity entity) {
    entities.add(entity);
  }

  public void update() {
    if (STOP_NOW) return;
    for (PhysicsEntity entity : entities) {
      entity.update();
      if (entity.centerOfMass.y() < -100) entity.destroy();
      if (STOP_NOW) return;
    }
    for (int i = 0; i < entities.size(); i++) {
      PhysicsEntity entity = entities.get(i);
      for (int j = i + 1; j < entities.size(); j++) {
        PhysicsEntity otherEntity = entities.get(j);
        entity.collidesWith(otherEntity);
//        if (collisionPoint != null) {
//          entity.onCollision(otherEntity, collisionPoint);
//          otherEntity.onCollision(entity, collisionPoint);
//        }
      }
    }
  }

  public void clear() {
    for (PhysicsEntity entity : entities) {
      entity.destroy();
    }
    entities.clear();
  }
}
