package me.melonboy10.blockphysics.debugger.ui;

import me.melonboy10.blockphysics.debugger.Debugger;
import me.melonboy10.blockphysics.util.TextDisplayBuilder;
import me.melonboy10.blockphysics.util.Utils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static java.awt.SystemColor.text;

public class ControlsWindow extends UIElement {

  private final ArrayList<Control<?>> controls = new ArrayList<>();
  TextDisplay display = new TextDisplayBuilder(Debugger.WORLD)
                          .billboard(Display.Billboard.FIXED)
                          .alignment(TextDisplay.TextAlignment.LEFT)
                          .lineWidth(2000)
                          .teleportDuration(1)
                          .build();

  public ControlsWindow() {
    super();
  }

  public void addControl(Control<?> control) {
    controls.add(control);
    dirty = true;
  }

  public void addText(String text) {
    controls.add(new TextControl(Component.text(text)));
    dirty = true;
  }

  public void update() {
    if (!dirty) return;
    dirty = false;

    int longestLine = 0;
    for (Control<?> control : controls) {
      longestLine = Math.max(longestLine, Utils.stringWidth(control.renderLabel().examinableName()));
    }

    TextComponent.Builder builder = Component.text();
    final char fillerChar = ' ';
    final int fillerCharWidth = 4;
    //    final char fillerChar = '.';
    //    final int fillerCharWidth = 2;
    for (Control<?> control : controls) {
      int charlength = Utils.stringWidth(PlainTextComponentSerializer.plainText().serialize(control.renderLabel()));
      int remainingLength = longestLine - charlength;
      int numSpaces = remainingLength / fillerCharWidth;
      int remainingPixels = remainingLength % fillerCharWidth;
      numSpaces -= remainingPixels;


      builder
        .append(control.renderLabel())
        .append(Component.text(String.valueOf(fillerChar).repeat(numSpaces)))
        .append(Component.text(String.valueOf(fillerChar).repeat(remainingPixels)).decoration(TextDecoration.BOLD, true))
        .append(control.renderValue())
        .append(Component.text("\n"));
    }
    builder.append(Component.text(" ".repeat(longestLine / fillerCharWidth + 10)));
    display.text(builder.build());
  }

  @Override
  public void destroy() {
    super.destroy();
    display.remove();
  }

  @Override
  public void setPosition(Vector3d position, double direction) {
    super.setPosition(position, direction);
    display.teleport(new Location(Debugger.WORLD, position.x, position.y, position.z));
    display.setRotation((float) direction - 180, 0);
  }

  public static class Control<T> {
    public Component label;
    public Supplier<T> getter;
    public Consumer<T> setter;

    public Control(Component label, Supplier<T> getter, Consumer<T> setter) {
      this.label = label;
      this.getter = getter;
      this.setter = setter;
    }

    public @NotNull Component renderLabel() {
      return label;
    }

    public @NotNull Component renderValue() {
      return Component.text(getter.get().toString());
    }
  }

  public static class BooleanControl extends Control<Boolean> {
    public BooleanControl(Component title, Supplier<Boolean> value, Consumer<Boolean> setter) {
      super(title, value, setter);
    }

    public @NotNull Component renderValue() {
      return Component.text(getter.get() ? "True" : "False");
    }
  }

  public static class ButtonControl extends Control<Boolean> {
    public ButtonControl(Component title, Supplier<Boolean> action) {
      super(title, () -> false, (v) -> action.get());
    }

    public @NotNull Component renderLabel() {
      return Component.text("");
    }

    public @NotNull Component renderValue() {
      return label;
    }
  }

  public static class CounterControl extends Control<Integer> {
    public CounterControl(Component title, Supplier<Integer> value, Consumer<Integer> setter) {
      super(title, value, setter);
    }

    public @NotNull Component renderValue() {
      return Component.text("←").append(Component.text(getter.get().toString())).append(Component.text("→"));
    }
  }

  public static class EnumControl<T extends Enum<T>> extends Control<T> {
    public EnumControl(Component title, Supplier<T> value, Consumer<T> setter) {
      super(title, value, setter);
    }

    public @NotNull Component renderValue() {
      return Component.text(getter.get().name());
    }
  }

  public static class TextControl extends Control<String> {
    public TextControl(TextComponent text) {
      super(text, () -> "", (v) -> {});
    }
  }

  public static class VariableControl extends Control<Object> {
    public VariableControl(Component title, Supplier<Object> value) {
      super(title, value, (v) -> {});
    }
  }
}
