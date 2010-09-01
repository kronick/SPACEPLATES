//specifying package wblut.hemesh allows access to protected members of HE_Mesh and other elements
package wblut.hemesh;
import wblut.geom.*;
import java.util.*;


//Modifier extends the abstract base class HEB_Modifier
public class HEC_BetterDual extends HEB_Creator{


  private double threshold = .001;
  private HE_Mesh mesh;

  private int searchLevel = 1;

  //Default modifier
  public HEC_BetterDual(processing.core.PApplet home) {
    super(home);
  }

  public HEC_BetterDual setSource(HE_Mesh m) {
    this.mesh = m;
    return this;
  }

  public HEC_BetterDual setSearchLevel(int n) {
    this.searchLevel = n;
    return this;
  }

  //modify is called when the entire mesh is the target
  @SuppressWarnings("static-access")
@Override
    protected HE_Mesh createBase() {
      // Go through each vertex, find vertex normal, create WB_Polygon around this point,
      // Find neighboring vertices, find their normals, create WB_Polygons and use these to slice first vertex' poly

      ArrayList<WB_Point> newVertices = new ArrayList<WB_Point>();
      ArrayList<int[]> newFaces    = new ArrayList<int[]>();

      ArrayList<WB_Point> extraVertices = new ArrayList<WB_Point>(); // for debugging

      HE_Vertex v = mesh.firstVertex();
      for(int i=0; i<mesh.numberOfVertices(); i++){
        // Each original vertex will turn into a face.
        // To build a face, we'll need to find the unordered edges of the face's polygon
        // Then order the edges to form a closed loop, check the normal,
        // and re-reference the vertices to the mesh's indices, adding new vertices if they don't exist yet/
        if(v.normal() != null && v.order() > 2) {  // First check to see this point has a normal that can be calculated.
          mesh._home.println("Checking vertex #" + (i+1));
          ArrayList<WB_Point[]> newEdges   = new ArrayList<WB_Point[]>();
          WB_Plane normalPlane = new WB_Plane(v, v.normal()); // Normal plane
          boolean dimple = false;

          // -------------------- Find neighbors and neighbor normal planes

          HE_Vertex[] neighbors = {};
          neighbors = v.vertices().toArray(neighbors);   // Only check neighbor vertices

          if(this.searchLevel > 0) {
            for(int n=1; n<this.searchLevel; n++) {
              // Also find neighbors' neighbors
              ArrayList<HE_Vertex> neighborsNeighborsList = new ArrayList<HE_Vertex>();
              for(int j=0; j<neighbors.length; j++) {
                ArrayList<HE_Vertex> nn = neighbors[j].vertices();
                for(int k=0; k<nn.size(); k++) {
                  if(!v.vertices().contains(nn.get(k)) && !neighborsNeighborsList.contains(nn.get(k)))
                    neighborsNeighborsList.add(nn.get(k));
                }
              }
              neighbors = (HE_Vertex[])mesh._home.concat(neighbors, neighborsNeighborsList.toArray());
            }
          }
          else neighbors = mesh.verticesAsArray();                // Check all vertices

          WB_Plane[] neighborNormalPlanes = new WB_Plane[neighbors.length];
          for(int j=0; j<neighbors.length; j++) {
            neighborNormalPlanes[j] = new WB_Plane(neighbors[j], neighbors[j].normal());
          }

          // -------------------- Find lines of intersection between planes

          WB_Line[] linesOfIntersection = new WB_Line[neighbors.length];
          for(int j=0; j<neighborNormalPlanes.length; j++) {
            int pol1 = neighborNormalPlanes[j].side(v);
            int pol2 = normalPlane.side(neighbors[j]);

            if(!(pol1 > 0 && pol2 < 0) && !(pol1 < 0 && pol2 > 0)) {
              WB_PlanePlaneIntersection inter = new WB_PlanePlaneIntersection().setPlane1(normalPlane).setPlane2(neighborNormalPlanes[j]);
              if(inter.type() == WB.INTERSECTING) {
                linesOfIntersection[j] = inter.L();
              }
            }
            else {
              //dimple = true;
            }
          }

          if(!dimple) {
            //mesh._home.println(linesOfIntersection.length + " lines of intersection");

            // -------------------- Find points of intersection between lines

            // Keep only points on opposite (not-normal) side of neighbor normal planes
            // TODO: The criteria for which points to keep should depend on curvature (concave vs convex) at this point
            // This has to be an ArrayList as it is not certain how many points will be kept
            ArrayList<WB_Point> pointsOfIntersection = new ArrayList<WB_Point>();
            for(int j=0; j<linesOfIntersection.length; j++) {
              WB_Point[] newEdge = new WB_Point[2];
              boolean set1 = false;
              boolean set2 = false;
              for(int k=0; k<linesOfIntersection.length; k++) {  // More efficient would be k=j+1 since this would only find one point, but we need 2!
                WB_LineLineIntersection inter = new WB_LineLineIntersection().setLine1(linesOfIntersection[j]).setLine2(linesOfIntersection[k]);
                WB_Point candidate = inter.p1();

                extraVertices.add(candidate);

                // -------------------- Trim extraneous points

                if(candidate != null) {
                  boolean keep = true;
                  for(int n=0; n<neighborNormalPlanes.length; n++) {
                    int pol1 = neighborNormalPlanes[n].side(v);
                    int pol2 = normalPlane.side(neighbors[n]);

                    if(!(pol1 > 0 && pol2 < 0)  && !(pol1 < 0 && pol2 > 0)) {
                      if((neighborNormalPlanes[n].side(candidate)) * -pol2 > 0) {
                        keep = false;
                        break;
                      }
                    }
                  }
                  if(keep) {
                    if(!set1) {
                      newEdge[0] = candidate;
                      set1 = true;
                    }
                    else {
                      newEdge[1] = candidate;
                      set2 = true;
                    }
                    pointsOfIntersection.add(candidate);
                  }
                }
              }
              if(set1 && set2 && newEdge[0].distanceTo(newEdge[1]) > threshold) {
                newEdges.add(newEdge);
              }
            }

            if(newEdges.size() > 0) {

              // -------------------- Clean duplicate points
              // Run through each of the points of intersection that were found
              // Check them against the previously-found list of vertices.
              // This will be done again by HEC_Facelist, but alas, it is needed to make a closed polygon

              for(int j=0; j<pointsOfIntersection.size(); j++) {
                boolean keep = true;
                for(int k=0; k<newVertices.size(); k++) {
                  if(((WB_Point)pointsOfIntersection.get(j)).distanceTo((WB_Point)newVertices.get(k)) < threshold) {
                    keep = false;
                    break;
                  }
                }
                if(keep) {
                  newVertices.add(pointsOfIntersection.get(j));

                }
              }

              // -------------------- Index edges to newVertices array

              // Now look at the vertices stored in each edge and create a new array with indices to the newVertices ArrayList
              // TODO: Some optimization could probably happen here by combining some of these steps...
              int[][] unsortedPolygon = new int[newEdges.size()][2];
              for(int j=0; j<newEdges.size(); j++) {
                for(int k=0; k<newVertices.size(); k++) {
                  if(((WB_Point[])newEdges.get(j))[0].distanceTo((WB_Point)newVertices.get(k)) < threshold)
                    unsortedPolygon[j][0] = k;
                  if(((WB_Point[])newEdges.get(j))[1].distanceTo((WB_Point)newVertices.get(k)) < threshold)
                    unsortedPolygon[j][1] = k;
                }
              }


              // Debug print out unordered list
              for(int j=0; j<unsortedPolygon.length; j++) {
                mesh._home.println(unsortedPolygon[j][0] + "->" + unsortedPolygon[j][1]);
              }


              // -------------------- Sort the list of edges to form a closed loop

              int[][] sortedPolygon = new int[unsortedPolygon.length][2];
              for(int j=0; j<unsortedPolygon.length; j++) {
                if(j==0) sortedPolygon[j] = unsortedPolygon[j];
                else {
                  int tail = sortedPolygon[j-1][1]; // The tail of this edge is the last edge's tip (index 1)
                  //Find edge that contains this tail index but isn't the same as the previous edge (or its reverse)
                  for(int k=0; k<unsortedPolygon.length; k++) {
                    if((unsortedPolygon[k][0] == tail || unsortedPolygon[k][1] == tail) &&
                       !(unsortedPolygon[k][0] == sortedPolygon[j-1][0] && unsortedPolygon[k][1] == sortedPolygon[j-1][1]) &&
                       !(unsortedPolygon[k][1] == sortedPolygon[j-1][0] && unsortedPolygon[k][0] == sortedPolygon[j-1][1])) {
                      sortedPolygon[j][0] = tail;
                      sortedPolygon[j][1] = unsortedPolygon[k][0] == tail ? unsortedPolygon[k][1] : unsortedPolygon[k][0];
                    }
                  }
                }
              }


              // Debug print out supposedly ordered list
              mesh._home.println("Sorted: ");
              for(int j=0; j<sortedPolygon.length; j++) {
                mesh._home.println(sortedPolygon[j][0] + "->" + sortedPolygon[j][1]);
              }


              if(newEdges.size() > 2) {
                // -------------------- Collapse 2D polygon array to 1D face array
                int[] newFace = new int[sortedPolygon.length];
                for(int j=0; j<newFace.length; j++) {
                  newFace[j] = sortedPolygon[j][1];
                }

                // -------------------- Check that the sortedPolygon's normal aligns with the vertex normal
                // NOTE: This takes a simple cross product to determine normal. This only works with CONVEX polygons.
                // TODO: Find a maximum point first, then take the cross product around here to get normal based on convex region
                WB_Point p0 = (WB_Point)newVertices.get(newFace[0]);
                WB_Point p1 = (WB_Point)newVertices.get(newFace[1]);
                WB_Point p2 = (WB_Point)newVertices.get(newFace[2]);

                WB_Point faceNormal = p1.subAndCopy(p0).cross(p2.subAndCopy(p1));
                if(faceNormal.dot(v.normal()) < 0) {
                  newFace = mesh._home.reverse(newFace);
                }
                newFaces.add(newFace);
              }
            }
            mesh._home.println("Polygon with " + newEdges.size() + " edges created.");
          }
        }
        // Move on to the next vertex
        v=mesh.nextVertex();
      }

      //newVertices.addAll(extraVertices);

      // Convert vertices and faces lists to arrays
      double[][] verts = new double[newVertices.size()][3];
      for(int i=0; i<newVertices.size(); i++) {
        WB_Point p = (WB_Point)newVertices.get(i);
        try {
          verts[i][0] = p.x;
          verts[i][1] = p.y;
          verts[i][2] = p.z;
        }
        catch(NullPointerException e) {
          verts[i][0] = 0;
          verts[i][1] = 0;
          verts[i][2] = 0;
        }
      }

      int[][] faceArray = new int[newFaces.size()][];
      for(int i=0; i<newFaces.size(); i++) {
        faceArray[i] = (int[])newFaces.get(i);
      }

      HEC_FaceList newFaceList = new HEC_FaceList(mesh._home);
      newFaceList.setFaces(faceArray);
      newFaceList.setVertices(verts);

      HE_Mesh me = new HE_Mesh(newFaceList);
      //WB_Point offset = ((WB_Point)newVertices.get(0)).subAndCopy(me.verticesAsArray()[0]).mult(-1);
      //me.move(offset);
      return me;

    }
}
