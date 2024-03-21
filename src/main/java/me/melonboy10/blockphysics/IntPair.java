package me.melonboy10.blockphysics;

public record IntPair(int a, int b) {
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    IntPair intPair = (IntPair) o;

    if (a != intPair.a) return false;
    return b == intPair.b;
  }
}
