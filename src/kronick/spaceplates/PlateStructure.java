package kronick.spaceplates;

import wblut.hemesh.*;
import processing.core.PApplet;
import java.util.*;

public class PlateStructure {
  static final int BEVEL_MATE = 0;
  static final int FOLDEDTAB_MATE = 1;

  protected PApplet home;
  private Plate[] plates;
  private ArrayList<Mate> mates;
  private int defaultMateType = 0;

  private HE_Mesh base;


  public PlateStructure(PApplet home) {
    this.home = home;
  }


  public PlateStructure setBase(HE_Mesh b) {
    this.base = b;
    this.mates = new ArrayList<Mate>();
    // Break down to its faces, create a Plate instance for each face
    HE_Face[] baseFaces = base.facesAsArray();
    plates = new Plate[baseFaces.length];
    for(int i=0; i<baseFaces.length; i++) {
      plates[i] = new Plate(home).setBase(baseFaces[i]);
    }
    HE_Edge[] baseHEEdges = base.edgesAsArray();
    for(int i=0; i<baseHEEdges.length; i++) {
      // Create a new mate of the default type for this edge
      Mate newMate;
      switch (defaultMateType) {
        case BEVEL_MATE: default:
          newMate = new BevelMate(home);
          break;
        //case FOLDEDTAB_MATE:
      }
      // Find the plates attached to each edge, update mate with references to edge
      for(int j=0; j<plates.length; j++) {
        if(plates[j].isFace(baseHEEdges[i].f1()))
          newMate.setEdge1(plates[j].getEdge(baseHEEdges[i]));
        if(plates[j].isFace(baseHEEdges[i].f2()))
          newMate.setEdge2(plates[j].getEdge(baseHEEdges[i]));
      }
      mates.add(newMate);
    }
    return this;
  }

  Plate getPlate(int i) {
    return this.plates[i];
  }
}
