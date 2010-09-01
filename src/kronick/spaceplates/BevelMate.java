package kronick.spaceplates;

import wblut.geom.WB_Point;
import wblut.hemesh.HEC_FaceList;
import wblut.hemesh.HE_Mesh;

import processing.core.PApplet;

public class BevelMate extends Mate {
  Edge e1, e2;

  public BevelMate(PApplet h) {
    super(h);
  }


  @Override
  public HE_Mesh getModifiedGeometry(Edge e) {
    WB_Point p1 = e.baseEdge.v1();
    WB_Point p4 = e.baseEdge.v2();
    WB_Point p2 = new WB_Point(p1);
    WB_Point p3 = new WB_Point(p4);

    double length = p1.distanceTo(p4);

    WB_Point norm = e.baseEdge.normal().normalize();
    WB_Point tang = e.baseEdge.tangent().normalize();
    p2.move(norm.get().mult(40));
    p2.move(tang.get().mult(length/2 > 5 ? 5 : length/2));
    p3.move(norm.get().mult(40));
    p3.move(tang.get().mult(length/2 > 5 ? -5 : -length/2));

    double[][] verts = {{p1.x, p1.y, p1.z}, {p2.x, p2.y, p2.z}, {p3.x, p3.y, p3.z}, {p4.x, p4.y, p4.z}};
    int[][] faces = {{0,1}, {1,2}, {2,3}, {0,3}}; // Do this as line segments to avoid closing the polygon!
    return new HE_Mesh(new HEC_FaceList(home).setFaces(faces).setVertices(verts));
  }

}
