//specifying package wblut.hemesh allows access to protected members of HE_Mesh and other elements
package wblut.hemesh;


//Modifier extends the abstract base class HEB_Modifier
public class HEC_Polygon extends HEB_Creator{

  private int n;
  private double sideLength;
  private processing.core.PApplet home;

  //Default modifier
  public HEC_Polygon(processing.core.PApplet home) {
    super(home);
    this.home = home;
  }

  public HEC_Polygon setN(int n) {
    this.n = n;
    return this;
  }

  public HEC_Polygon setEdge(double E) {
    this.sideLength = E;
    return this;
  }

  @Override
    protected HE_Mesh createBase() {
      double[][] verts = new double[n+1][3];
      for(int i=0; i<n; i++) {
        verts[i][0] = sideLength * Math.cos(i * 2*Math.PI/n);
        verts[i][1] = sideLength * Math.sin(i * 2*Math.PI/n);
        verts[i][2] = 0;
      }
      verts[n][0] = 0; verts[n][1] = 0; verts[n][2] = 0;

      int[][] faces = new int[n][3];
      for(int i=0; i<n; i++) {
        faces[i][0] = i;
        faces[i][1] = (i+1)%n;
        faces[i][2] = n;
      }

      HEC_FaceList newFaceList = new HEC_FaceList(this.home);
      newFaceList.setFaces(faces);
      newFaceList.setVertices(verts);

      return new HE_Mesh(newFaceList);

    }
}
