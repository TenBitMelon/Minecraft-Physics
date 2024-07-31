package me.melonboy10.blockphysics.core;

import me.melonboy10.blockphysics.debugger.Debugger;
import me.melonboy10.blockphysics.debugger.ui.ControlsWindow;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class PhysicsWorld {
  public static float GRAVITY = 9.8f;
  public static final float ELASTICITY = 0.8f;
  public static final int STEPS_PER_SECOND = 60;
  //  public static final int STEPS_PER_SECOND = 1;
  public static float DELTA_TIME = 1 / (20f * STEPS_PER_SECOND);

  public static World WORLD = Bukkit.getWorlds().get(0);

  public static ControlsWindow debuggerControls = new ControlsWindow() {{
    addText("Physics Engine");
    addControl(new BooleanControl(Component.text("Stop"), () -> STOP_NOW, (value) -> STOP_NOW = value));
    addControl(new BooleanControl(Component.text("Debug Rendering"), () -> DEBUG, (value) -> DEBUG = value));
    addControl(new ButtonControl(Component.text("Clear"), () -> {
      PhysicsWorld.clear();
      return true;
    }));
    addControl(new CounterControl(Component.text("Debug Level"), () -> DEBUG_LEVEL, (value) -> DEBUG_LEVEL = value));
    //    addControl(new EnumControl<Display.Billboard>(Component.text("Debug Display"), () -> debugDisplay.getBillboard(), (value) -> debugDisplay.setBillboard(value)));
    addText(" ");
    addControl(new VariableControl(Component.text("Delta Time"), () -> DELTA_TIME));
    update();
  }};

  public static boolean STOP_NOW = false;
  public static boolean DEBUG = true;
  public static int DEBUG_LEVEL = 1;
  public static TextDisplay debugDisplay = createTextDisplay(Bukkit.getOnlinePlayers().stream().findFirst().get().getLocation().add(0, 5, 0), "");
  private static final ArrayList<Component> debugLines = new ArrayList<>();

  public static final ArrayList<PhysicsEntity> entities = new ArrayList<>();

  private PhysicsWorld() {
    throw new IllegalStateException("Utility class");
  }

  public static void addEntity(PhysicsEntity entity) {
    entities.add(entity);
  }

  public static void showParticles() {
    if (DEBUG)
      for (PhysicsEntity entity : entities) {
        entity.showParticles();
      }
  }

  public static void update() {
    if (STOP_NOW) return;

    for (PhysicsEntity entity : entities) {
      entity.clearParticles();
    }

    debugLines.add(Component.text("Entities: %d".formatted(entities.size())));
    debugLines.add(Component.text("Delta Time: %f".formatted(DELTA_TIME)));
    double[] durrations = new double[STEPS_PER_SECOND];

    long startTimeTick = System.nanoTime();
    for (int t = 0; t < STEPS_PER_SECOND; t++) {
      if (STOP_NOW) return;
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

    //    Debugger.

    //    debugLines.add(Component.text("Update Time: %fms".formatted((endTimeTick - startTimeTick) / 1000000f)));
    //    debugLines.add(Component.text("Step Time: %fms".formatted(Arrays.stream(durrations).average().getAsDouble())));
    //
    //    Component debugText = Component.text("Physics Engine");
    //    for (Component debugLine : debugLines) {
    //      debugText = debugText.append(Component.text("\n")).append(debugLine);
    //    }
    //    debugDisplay.text(debugText);
    //    debugLines.clear();
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

  public static BlockDisplay createBlockDisplay(Location location, BlockData blockData) {
    Location location1 = location.clone();
    location1.setPitch(0);
    location1.setYaw(0);
    BlockDisplay blockDisplay =
      (BlockDisplay) location.getWorld().spawnEntity(location1, EntityType.BLOCK_DISPLAY);
    blockDisplay.setBlock(blockData);
    blockDisplay.setTeleportDuration(1);
    blockDisplay.setInterpolationDuration(1);
    blockDisplay.setInterpolationDelay(-1);
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
