package me.melonboy10.blockphysics.debugger.ui;

import lombok.Setter;
import me.melonboy10.blockphysics.debugger.Debugger;
import me.melonboy10.blockphysics.util.InteractionBuilder;
import me.melonboy10.blockphysics.util.TextDisplayBuilder;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Display;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.joml.Vector3d;

public abstract class UIElement {

  TextDisplay anchorText;
  Interaction anchorInteraction;
  Vector3d position;
  double direction = 180;

  public int width = 0;
  public int height = 0;
  public boolean dirty = true;

  public UIElement() {
    anchorText = new TextDisplayBuilder(Debugger.WORLD).text("≡≡≡").billboard(Display.Billboard.VERTICAL).teleportDuration(1).build(); // Braille Pattern "⠿" (U+283F) or Full Square "⬛" (U+2B1B) or Identical to "≡" (U+2261)
    anchorInteraction = new InteractionBuilder(Debugger.WORLD).width(0.5).height(0.3).responsive(true).build();
    Debugger.addElement(this);
  }

  public void setPosition(Vector3d position) {
    setPosition(position, direction);
  }

  public void setPosition(Vector3d position, double direction) {
    this.position = position;
    this.direction = direction;
    anchorText.teleport(anchorText.getLocation().set(position.x, position.y - 0.3, position.z));
    anchorInteraction.teleport(anchorInteraction.getLocation().set(position.x, position.y - 0.25, position.z));
  }

  public void destroy() {
    anchorText.remove();
    anchorInteraction.remove();
  }

  public boolean onInteract(PlayerInteractEntityEvent event) {
    if (event.getRightClicked().equals(anchorInteraction)) {
      if (Debugger.DRAGGED_ELEMENT == null) Debugger.DRAGGED_ELEMENT = this;
      else Debugger.DRAGGED_ELEMENT = null;

      return true;
    }
    return false;
  }

  public abstract void update();
}
