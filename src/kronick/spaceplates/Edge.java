package kronick.spaceplates;

import wblut.hemesh.*;
import wblut.geom.*;
import processing.core.PApplet;

public class Edge {
  HE_Edge baseEdge;

  Mate mate;
  protected PApplet home;

  public Edge(PApplet h) {
    this.home = h;
  }

  protected Edge setBase(HE_Edge e) {
    this.baseEdge = e;
    return this;
  }

  protected Edge setMate(Mate m) {
    this.mate = m;
    return this;
  }

  public HE_Mesh getGeometry() {
    return mate.getModifiedGeometry(this);
  }
}
