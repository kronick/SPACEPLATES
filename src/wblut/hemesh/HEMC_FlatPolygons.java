package wblut.hemesh;

import java.util.ArrayList;
import processing.core.PApplet;
import wblut.geom.*;

public class HEMC_FlatPolygons extends HEB_MultiCreator {

  HE_Mesh mesh;

  public HEMC_FlatPolygons(PApplet home) {
    super(home);
  }

  public HEMC_FlatPolygons setMesh(HE_Mesh me) {
    this.mesh = me;
    return this;
  }


  /* (non-Javadoc)
   * @see wblut.hemesh.HE_MultiCreator#create()
   */
  @Override
  public HE_Mesh[] create() {
    HE_Face[] faces = mesh.facesAsArray();
    HE_Mesh[] flatPolys = new HE_Mesh[faces.length];
    for(int i=0; i<faces.length; i++) {
      // 1. Find angle between normal and +z-axis
      double theta = faces[i].normal().get().angleTo(new WB_Point(0,0,1));
      // 2. Find axis of rotation
      WB_Point axis = faces[i].normal().get().cross(new WB_Point(0,0,1));
      // 3. Rotate each point, remove z-component and add to new vertices array
      ArrayList<HE_Vertex> verts = faces[i].vertices();
      double[][] flatVerts = new double[verts.size()][3];
      for(int j=0; j<verts.size(); j++) {
        WB_Point flatPoint = verts.get(j).get();
        flatPoint.rotateAboutAxis(theta, new WB_Point(0,0,0), axis);
        flatVerts[j][0] = flatPoint.x;
        flatVerts[j][1] = flatPoint.y;
        flatVerts[j][2] = 0;
      }
      // 4. Build face list and create new mesh, add to array
      int[][] flatFaces = new int[1][flatVerts.length];
      for(int j=0; j<flatVerts.length; j++) {
        flatFaces[0][j] = j;
      }
      HEC_FaceList facelist = new HEC_FaceList(home);
      facelist.setVertices(flatVerts);
      facelist.setFaces(flatFaces);

      flatPolys[i] = new HE_Mesh(facelist);
    }

    return flatPolys;
  }
}
