package kronick.spaceplates;

import wblut.hemesh.*;
import processing.core.PApplet;
/**
 * Interface for defining mating operations
 *
 * @author Sam Kronick (kronick)
 *
 */
public class Mate {
  Edge e1, e2;
  PApplet home;

  public Mate(PApplet h) {
    this.home = h;
    e1 = null;
    e2 = null;
  }

  public void setEdges(Edge e1, Edge e2) {
    setEdge1(e1); setEdge2(e2);
  }

  public void setEdge1(Edge e1) {
    this.e1 = e1;
    this.e1.setMate(this);
  }
  public void setEdge2(Edge e2) {
    this.e2 = e2;
    this.e2.setMate(this);
  }

  public Edge[] getEdges() {
    Edge[] edges = {e1, e2};
    return edges;
  }

  HE_Mesh getModifiedGeometry(Edge e) {
    return null;
  }
  // Hardware[] getHardware();
}
