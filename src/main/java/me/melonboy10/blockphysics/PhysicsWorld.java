package me.melonboy10.blockphysics;

import net.minecraft.util.Tuple;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Iterator;

public class PhysicsWorld {
  public static float GRAVITY = 9.8f;
  public static final float ELASTICITY = 0.8f;
  public static final int STEPS_PER_SECOND = 20;
  public static float DELTA_TIME = 1 / (20f * STEPS_PER_SECOND);

  public static World WORLD;

  public static boolean STOP_NOW = false;
  public static boolean DEBUG = false;
  public static int DEBUG_LEVEL = 5;
  public static TextDisplay debugDisplay;

  public static final ArrayList<PhysicsEntity> entities = new ArrayList<>();

  public void addEntity(PhysicsEntity entity) {
    entities.add(entity);
  }

  public void update() {
    //    if (STOP_NOW) return;

    long startTime = System.nanoTime();
    for (Iterator<PhysicsEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
      PhysicsEntity entity = iterator.next();
      entity.update();

      /** IF OUT OF BOUNDS OR STOP THE SIMULATION*/
      if (entity.centerOfMass.y() < -100) {
        entity.destroy();
        iterator.remove();
      }
      ;
      //      if (entity.centerOfMass.y() < -100) entity.destroy();
      //      if (STOP_NOW) return;
    }
    for (int i = 0; i < entities.size(); i++) {
      PhysicsEntity entity = entities.get(i);
      for (int j = i + 1; j < entities.size(); j++) {
        PhysicsEntity otherEntity = entities.get(j);

        Tuple<Vector3d, Vector3d> collisionPoint = entity.collidesWith(otherEntity);
        if (collisionPoint != null) {
          entity.onCollision(otherEntity, collisionPoint.getA(), collisionPoint.getB());
        }


        //        entity.collidesWith(otherEntity);
        //        if (collisionPoint != null) {
        //          entity.onCollision(otherEntity, collisionPoint);
        //          otherEntity.onCollision(entity, collisionPoint);
        //        }
      }
    }
    long endTime = System.nanoTime();
    long duration = (endTime - startTime);
    debugDisplay.setText("""
      Physics Engine
      Entities: %d
      Delta Time: %f
      Duration: %fms
      """.formatted(entities.size(), DELTA_TIME, duration / 1000000f));

  }

  public void clear() {
    for (PhysicsEntity entity : entities) {
      entity.destroy();
    }
    entities.clear();
    debugDisplay.remove();
  }
}
