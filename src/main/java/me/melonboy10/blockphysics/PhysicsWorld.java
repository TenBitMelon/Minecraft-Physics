package me.melonboy10.blockphysics;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class PhysicsWorld {
  public static float GRAVITY = 9.8f;
  public static final float ELASTICITY = 0.8f;
  public static final int STEPS_PER_SECOND = 60;
  //  public static final int STEPS_PER_SECOND = 20;
  public static float DELTA_TIME = 1 / (20f * STEPS_PER_SECOND);

  public static World WORLD;

  public static boolean STOP_NOW = false;
  public static boolean DEBUG = true;
  public static int DEBUG_LEVEL = -1;
  public static TextDisplay debugDisplay;
  private static ArrayList<Component> debugLines = new ArrayList<>();

  public static final ArrayList<PhysicsEntity> entities = new ArrayList<>();

  public PhysicsWorld() {
    PhysicsWorld.WORLD = Bukkit.getWorlds().get(0);
    PhysicsWorld.debugDisplay = PhysicsEntity.createTextDisplay(Bukkit.getOnlinePlayers().stream().findFirst().get().getLocation().add(0, 5, 0), "");
  }

  public static void addEntity(PhysicsEntity entity) {
    entities.add(entity);
  }

  public static void update() {
    for (PhysicsEntity entity : entities) {
      entity.showParticles();
    }

    if (STOP_NOW) return;

    debugLines.add(Component.text("Entities: %d".formatted(entities.size())));
    debugLines.add(Component.text("Delta Time: %f".formatted(DELTA_TIME)));
    double[] durrations = new double[STEPS_PER_SECOND];

    long startTimeTick = System.nanoTime();
    for (int t = 0; t < STEPS_PER_SECOND; t++) {
      long startTime = System.nanoTime();
      for (Iterator<PhysicsEntity> iterator = entities.iterator(); iterator.hasNext(); ) {
        PhysicsEntity entity = iterator.next();
        entity.update();

        /* IF OUT OF BOUNDS OR STOP THE SIMULATION*/
        if (entity.centerOfMass.y() < -100) {
          entity.destroy();
          iterator.remove();
        }
      }
      for (int i = 0; i < entities.size(); i++) {
        PhysicsEntity entity = entities.get(i);
        for (int j = i + 1; j < entities.size(); j++) {
          PhysicsEntity otherEntity = entities.get(j);

          PhysicsEntity.CollisionData collisionPoint = entity.collidesWith(otherEntity);
          if (collisionPoint != null) {
            entity.onCollision(otherEntity, collisionPoint);
          }
        }
      }
      long endTime = System.nanoTime();
      long duration = (endTime - startTime);
      durrations[t] = duration / 1000000f;
    }
    long endTimeTick = System.nanoTime();
    debugLines.add(Component.text("Update Time: %fms".formatted((endTimeTick - startTimeTick) / 1000000f)));
    debugLines.add(Component.text("Step Time: %fms".formatted(Arrays.stream(durrations).average().getAsDouble())));

    Component debugText = Component.text("Physics Engine");
    for (Component debugLine : debugLines) {
      debugText = debugText.append(Component.text("\n")).append(debugLine);
    }
    debugDisplay.text(debugText);
    debugLines.clear();
  }

  public static void addDebugLine(Component line) {
    debugLines.add(line);
  }

  public static void clear() {
    for (PhysicsEntity entity : entities) {
      entity.destroy();
    }
    entities.clear();
    debugDisplay.remove();
  }
}
