package me.melonboy10.blockphysics.debugger.ui;

import me.melonboy10.blockphysics.debugger.Debugger;
import me.melonboy10.blockphysics.util.Utils;
import me.melonboy10.blockphysics.util.TextDisplayBuilder;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.joml.Vector3d;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TextWindow extends UIElement {

  ArrayList<String> lines = new ArrayList<>();
  TextDisplay display = new TextDisplayBuilder(Debugger.WORLD).billboard(Display.Billboard.FIXED).alignment(TextDisplay.TextAlignment.LEFT).lineWidth(2000).teleportDuration(1).build();

  public void addLine(String line) {
    lines.add((line));
    dirty = true;
  }

  public void update() {
    if (!dirty) return;
    dirty = false;

    StringBuilder text = new StringBuilder();
    for (String line : lines) {
      text.append(line).append("\n");
    }
    text.deleteCharAt(text.length() - 1);
    display.text(Component.text(text.toString()));
  }

  @Override
  public void setPosition(Vector3d position, double direction) {
    super.setPosition(position, direction);
    display.teleport(new Location(Debugger.WORLD, position.x, position.y, position.z));
    display.setRotation((float) direction - 180, 0);
  }

  @Override
  public void destroy() {
    super.destroy();
    display.remove();
  }
}
