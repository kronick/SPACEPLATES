package kronick.spaceplates;

import java.awt.Frame;
import java.awt.event.*;
import java.util.Iterator;

import processing.core.*;
import processing.opengl.*;
import wblut.hemesh.*;
import wblut.geom.*;

import peasy.*;
import peasy.org.apache.commons.math.geometry.Vector3D;
import controlP5.*;

import picking.*;

public class SpacePlates extends PApplet {
  HE_Mesh startPoly;
  HE_Mesh surface;
  HE_Mesh projectedPoly;
  HE_Mesh planes;

  HE_Mesh plateMesh;
  HE_Mesh[] flatPlates;

  HangingMesh hangingMesh;
  HE_Mesh hangingPlateMesh;
  PlateStructure plateStructure;

  int rot = 0;
  float scaleFactor = 1;

  PeasyCam camera;
  ControlP5 controlP5;
  ControlWindow controlWindow;
  RadioButton primativeSelector;

  PFrame auxFrame;
  AuxSketch auxSketch;

  Picker picker;

  boolean showPoly = false;
  boolean showSurface = false;
  boolean showIntPoints = false;
  boolean showProjectionLines = false;
  boolean showTangentPlanes = false;
  boolean showPlates = true;

  int plateNumber = 0;

  double step = .1;

  static final int TETRAHEDRON = 4;
  static final int OCTAHEDRON = 6;
  static final int ICOSAHEDRON = 12;

  int currentPrimative;
  int currentSubdivisions;

  public static void main(String args[]) {
    PApplet.main(new String[] {"kronick.spaceplates.SpacePlates" });
  }

  public void setup(){
    size(600,600,OPENGL	);
    colorMode(HSB);
    smooth();

    PFrame auxFrame = new PFrame();

    // Set up Camera
    camera = new PeasyCam(this, 0,0,0, 500);

    // Set up GUI
    controlP5 = new ControlP5(this);
    controlP5.setAutoDraw(false);
    controlWindow = controlP5.addControlWindow("SPACE PLATES Control",100,100,400,200);
    controlWindow.hideCoordinates();
    //controlWindow.setUndecorated(true);
    primativeSelector = controlP5.addRadioButton("primativeSelector", 10, 10);
    primativeSelector.addItem("Tetrahedron", 4);
    primativeSelector.addItem("Octahedron", 6);
    primativeSelector.addItem("Icosahedron", 12);
    primativeSelector.moveTo(controlWindow);

    picker = new Picker(this);

    HE_Mesh hexaNet = new HE_Mesh(new HEC_Polygon(this).setN(5).setEdge(300));
    hexaNet.subdivide(new HES_Planar(), 3);
    hangingMesh = new HangingMesh();
    hangingMesh.setDefaultDamping(20).setDefaultStrength(1f).setBaseMesh(hexaNet).fixFreeEdges(true).setGravity(new WB_Point(0,0,-20)).setPerimeterTightness(10f);
    //hangingMesh.fixPoint(0).fixPoint(1).fixPoint(2).fixPoint(3).fixPoint(4).fixPoint(5).fixPoint(6).fixPoint(7);
    hangingMesh.fixPoint(5);
    //hangingMesh.setTightness(6, 3f);

    generate(TETRAHEDRON, 2);
  }

  void controlEvent(ControlEvent e) {
    if(e.name() == "primativeSelector") {
      if(currentPrimative != e.group().value()) {
        currentPrimative = (int)e.group().value();
        generate(currentPrimative, currentSubdivisions);
      }
    }
  }

  public void mouseClicked() {
    int id = picker.get(mouseX, mouseY);
    if(id > -1) {
      plateNumber = id;
    }
  }

  public void keyPressed() {
    if(key == '1') showPoly = !showPoly;
    if(key == '2') showSurface = !showSurface;
    if(key == '3') showIntPoints = !showIntPoints;
    if(key == '4') showProjectionLines = !showProjectionLines;
    if(key == '5') showTangentPlanes = !showTangentPlanes;
    if(key == '6') showPlates = !showPlates;

    if(key == '+' || key == '=') generate(currentPrimative, currentSubdivisions+1);
    if(key == '-') generate(currentPrimative, currentSubdivisions-1);

    if(key == 'q') generate(TETRAHEDRON, currentSubdivisions);
    if(key == 'w') generate(OCTAHEDRON, currentSubdivisions);
    if(key == 'e') generate(ICOSAHEDRON, currentSubdivisions);

    if(key == 'd') {
      hangingPlateMesh = new HE_Mesh(new HEC_BetterDual(this).setSearchLevel(2).setSource(hangingMesh.mesh).setClippingPlane(new WB_Plane(0,0,-50, 0,0,-1)));
    }

    if(key == CODED) {
      if(keyCode == LEFT) plateNumber--;
      if(keyCode == RIGHT) plateNumber++;
    }
  }

  public void generate(int prim, int subdivisions) {
    plateNumber = 0;
    currentPrimative = prim;
    currentSubdivisions = subdivisions;

    switch(prim) {
      case TETRAHEDRON:
        startPoly = new HE_Mesh(new HEC_Tetrahedron(this).setEdge(200));
        break;
      case ICOSAHEDRON:
        startPoly = new HE_Mesh(new HEC_Icosahedron(this).setEdge(200));
        break;
      case OCTAHEDRON:
        startPoly = new HE_Mesh(new HEC_Octahedron(this).setEdge(200));
        break;
      default:
    }

    startPoly.subdivide(new HES_Planar(), subdivisions);
    surface = new HE_Mesh(new HEC_Sphere(this).setRadius(200).setUFacets(20).setVFacets(20));

    // Copy the starting poly to initiate its projection
    projectedPoly = startPoly.get();
    HE_Vertex[] v = projectedPoly.verticesAsArray();
    for(int i=0; i<v.length; i++) {
      // Find projection vector, scale to step size
      WB_Point norm = v[i].get().normalize().mult(step);

      // Move this point along the normal until it's not inside the surface mesh anymore
      while(sqrt(sq((float)v[i].x) + sq((float)v[i].y) + sq((float)v[i].z)) < 200) {
        v[i].add(norm);
      }
    }

    plateMesh = new HE_Mesh(new HEC_BetterDual(this).setSearchLevel(2).setSource(projectedPoly));
    flatPlates = new HEMC_FlatPolygons(this).setMesh(plateMesh).create();

    plateStructure = new PlateStructure(auxSketch);
    plateStructure.setBase(plateMesh);
  }

  public void draw(){
    background(0);
    camera.beginHUD();
    lights();
    camera.endHUD();

    if(showPoly) {
      stroke(0,0,255,255);
      startPoly.drawEdges();
      fill(80,40,200,255);
      startPoly.drawFaces();
    }

    if(showIntPoints) {
      stroke(0,0,255,255);
      projectedPoly.drawEdges();
      fill(0,0,180,255);
      projectedPoly.drawFaces();
    }

    if(showSurface) {
      noStroke();
      fill(0,0,255,100);
      surface.drawFaces();
    }

    if(showPlates) {
      strokeWeight(4);
      stroke(80,255,255,255);
      plateMesh.drawEdges();
      noStroke();
      fill(0,0,0);

      try {
        Iterator<HE_Face> facesIt = plateMesh.fItr();
        int id = 0;
        while(facesIt.hasNext()) {
          picker.start(id++);
          plateMesh.drawFace(facesIt.next().key());
        }
      }
      catch (Exception e) { ; }
      picker.stop();

      fill(0,0,255);

      plateMesh.selection = new HE_Selection();
      plateMesh.selection.add(plateMesh.facesAsArray()[abs(plateNumber)%plateMesh.facesAsArray().length]);
      plateMesh.drawSelectedFaces();

      picker.resume();

    }
    if(showProjectionLines) {
      // Draw normals
      stroke(120,150,255);
      HE_Vertex[] v = startPoly.verticesAsArray();
      for(int i=0; i<v.length; i++) {
        WB_Point norm = v[i].get().normalize().mult(200);
        line((float)v[i].x, (float)v[i].y, (float)v[i].z, (float)v[i].x + (float)norm.x, (float)v[i].y + (float)norm.y, (float)v[i].z + (float)norm.z);
      }
    }

    noFill();
    stroke(0,0,255);
    strokeWeight(1);
    hangingMesh.tick();
    hangingMesh.draw();

    if(hangingPlateMesh != null) {
      stroke(0,150,255);
      strokeWeight(4);
      hangingPlateMesh.drawEdges();
      fill(0,0,0);
      noStroke();
      hangingPlateMesh.drawFaces();
    }

    camera.beginHUD();
    controlP5.draw();
    camera.endHUD();
	}

	public class PFrame extends Frame {
	  public PFrame() {
	    setBounds(100,500,400,400);
	    auxSketch = new AuxSketch();
	    add(auxSketch);
	    auxSketch.init();
	    setTitle("2D Geometry Viewer");
	    setVisible(true);
	  }
	}

	public class AuxSketch extends PApplet {
	  public void setup() {
	    size(400,400, OPENGL);
	    hint(DISABLE_OPENGL_2X_SMOOTH);
	    smooth();
	    colorMode(HSB);
	  }
	  public void draw(){
	    background(0,0,0);
	    try {
  	    translate(width/2,height/2);
  	    scale(2f);
  	    strokeWeight(1);
  	    stroke(0,0,255);
  	    plateStructure.getPlate(abs(plateNumber)%flatPlates.length).drawEdgesFlat();
  	    noStroke();
  	    fill(0,200,200);
  	    plateStructure.getPlate(abs(plateNumber)%flatPlates.length).drawFacesFlat();
	    }
	    catch(Exception e) {}
	  }
	}
}
