package me.melonboy10.blockphysics.debugger;

import me.melonboy10.blockphysics.debugger.ui.TextWindow;
import me.melonboy10.blockphysics.debugger.ui.UIElement;
import me.melonboy10.blockphysics.util.Utils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.joml.Vector3d;

import java.util.ArrayList;

public class Debugger {

  public static World WORLD = Bukkit.getWorlds().get(0);
  static ArrayList<UIElement> elements = new ArrayList<>();
  public static UIElement DRAGGED_ELEMENT = null;

  static {
    TextWindow mainDebugWindow = new TextWindow();
    mainDebugWindow.addLine("BlockPhysics Debugger");
    mainDebugWindow.addLine("Version: 1.0.0");
    mainDebugWindow.addLine("Author: Melonboy10");
    mainDebugWindow.addLine(" ");
    mainDebugWindow.addLine("This is the main debug window.");
    mainDebugWindow.addLine("You can add more windows by calling");
    mainDebugWindow.addLine("Debugger.addWindow(TextWindow window)");
    addElement(mainDebugWindow);
  }

  private Debugger() {
    throw new IllegalStateException("Utility class");
  }

  public static void organizeElementsAround(Vector3d position) {
    Vector3d offset = new Vector3d(position);
    for (UIElement element : elements) {
      element.setPosition(offset);
      //      offset.add(0, element.height + 4, 0);
    }
  }

  public static void addElement(UIElement element) {
    elements.add(element);
  }

  public static void start() {
    //    Later add a restore from file
  }

  public static void stop() {
    for (UIElement element : elements) {
      element.destroy();
    }
  }

  public static void update() {
    for (UIElement element : elements) {
      element.update();
    }
  }

  public static class DebugListener implements Listener {
    public static final DebugListener INSTANCE = new DebugListener();

    private DebugListener() {}

    @EventHandler(ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
      if (DRAGGED_ELEMENT != null) {
        double facing = Utils.roundRotation(event.getPlayer().getLocation().getYaw(), 8);
        DRAGGED_ELEMENT.setPosition(event.getPlayer().getLocation().getDirection().toVector3d().mul(2).add(event.getPlayer().getEyeLocation().toVector().toVector3d()), facing);
      }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
      for (UIElement element : elements) {
        if (element.onInteract(event)) {
          return;
        }
      }
    }
  }
}
