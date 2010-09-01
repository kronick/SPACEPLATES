package kronick.spaceplates;

import java.util.ArrayList;

import wblut.geom.WB_Point;
import wblut.hemesh.*;
import processing.core.PApplet;
/**
 * Adds manufacturing and assembly metadata to HE_Mesh class
 *
 * @author Sam Kronick (kronick)
 *
 */
public class Plate {
  private HE_Mesh renderMesh;
  private HE_Face baseFace;
  protected ArrayList<Edge> edges;

  protected PApplet home;

  public Plate(PApplet h) {
    this.home = h;
    this.renderMesh = null;
  }

  public Plate setBase(HE_Face f) {
    this.baseFace = f;
    generateEdges();
    return this;
  }

  private void generateEdges() {
    edges = new ArrayList<Edge>();
    ArrayList<HE_Edge> e = baseFace.edges();
    for(int i=0; i<e.size(); i++) {
      edges.add(new Edge(this.home).setBase(e.get(i)));
    }
  }

  public Edge getEdge(HE_Edge e) {
    for(int i=0; i<edges.size(); i++) {
      if(edges.get(i).baseEdge == e)
        return edges.get(i);
    }
    return null;
  }

  public boolean isFace(HE_Face f) {
    return f == this.baseFace;
  }

  public void updateRenderMesh(boolean flatten) {
    // Loop through all edges, get geometry and combine. return
    ArrayList<HE_Vertex> verts = new ArrayList<HE_Vertex>();
    ArrayList<int[]> faces = new ArrayList<int[]>();
    for(int i=0; i<edges.size(); i++) {
      HE_Mesh edgeGeometry = edges.get(i).getGeometry();
      HE_Face[] newFaces = edgeGeometry.facesAsArray();
      for(int j=0; j<newFaces.length; j++) {
        int startIndex = verts.size();
        verts.addAll(newFaces[j].vertices());
        // Build a new face list
        int[] newFace = new int[newFaces[j].vertices().size()];
        for(int k=0; k<newFace.length; k++) {
          newFace[k] = startIndex+k;
        }
        faces.add(newFace);
      }
    }
    double[][] verticesArray = new double[verts.size()][3];
    int[][] facesArray = new int[faces.size()][];
    for(int i=0; i<verticesArray.length; i++) {
      if(!flatten) {
        verticesArray[i][0] = verts.get(i).x;
        verticesArray[i][1] = verts.get(i).y;
        verticesArray[i][2] = verts.get(i).z;
      }
      else {
        // 1. Find angle between normal and +z-axis
        double theta = baseFace.normal().get().angleTo(new WB_Point(0,0,1));
        // 2. Find axis of rotation
        WB_Point axis = baseFace.normal().get().cross(new WB_Point(0,0,1));
        // 3. Rotate each point, remove z-component and add to new vertices array
        WB_Point flatPoint = verts.get(i).get();
        flatPoint.rotateAboutAxis(theta, new WB_Point(0,0,0), axis);
        verticesArray[i][0] = flatPoint.x;
        verticesArray[i][1] = flatPoint.y;
        verticesArray[i][2] = 0;
      }
    }
    for(int i=0; i<facesArray.length; i++) {
      facesArray[i] = faces.get(i);
    }
    HEC_FaceList fl = new HEC_FaceList(home).setVertices(verticesArray).setFaces(facesArray);
    HE_Mesh me = new HE_Mesh(fl);

    renderMesh = me;
  }

  public void drawFaces() { this.drawFaces(true); }
  public void drawFaces(boolean update) {
    if(update) updateRenderMesh(false);
    renderMesh.drawFaces();
  }
  public void drawEdges() { this.drawEdges(true); }
  public void drawEdges(boolean update) {
    if(update) updateRenderMesh(false);
    renderMesh.drawEdges();
  }

  public void drawFacesFlat() { this.drawFacesFlat(true); }
  public void drawFacesFlat(boolean update) {
    if(update) updateRenderMesh(true);
    renderMesh.drawFaces();
  }
  public void drawEdgesFlat() { this.drawEdgesFlat(true); }
  public void drawEdgesFlat(boolean update) {
    if(update) updateRenderMesh(true);
    renderMesh.drawEdges();
  }
}
