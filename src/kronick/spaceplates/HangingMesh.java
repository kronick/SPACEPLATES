package kronick.spaceplates;

import wblut.hemesh.*;
import wblut.geom.*;
import traer.physics.*;

public class HangingMesh {

  public HE_Mesh mesh;
  public ParticleSystem physics;
  public float defaultMass = 100;
  public float defaultStrength = 1f;
  public float defaultDamping = 20;
  public float defaultLengthFactor = .01f;
  public WB_Point gravity;

  public HangingMesh() {
    physics = new ParticleSystem();
    physics.setIntegrator(ParticleSystem.RUNGE_KUTTA);
  }

  public HangingMesh setBaseMesh(HE_Mesh me) {
    mesh = me;
    regenerateStructure();
    return this;
  }

  public void regenerateStructure() {
    physics.clear();

    // Add all vertices as new particles
    HE_Vertex[] verts = mesh.verticesAsArray();
    for(int i=0; i<verts.length; i++) {
      physics.makeParticle(defaultMass, (float)verts[i].x, (float)verts[i].y, (float)verts[i].z);
    }

    // Add all edges as springs
    HE_Edge[] edges = mesh.edgesAsArray();
    for(int i=0; i<edges.length; i++) {
      // Find indices of vertices
      int a = 0; int b = 0;
      for(int j=0; j<verts.length; j++) {
        if(edges[i].v1() == verts[j]) a = j;
        if(edges[i].v2() == verts[j]) b = j;
      }
      physics.makeSpring(physics.getParticle(a), physics.getParticle(b), defaultStrength, defaultDamping, (float)edges[i].v1().distanceTo(edges[i].v2()) * defaultLengthFactor);
    }
  }

  public HangingMesh fixFreeEdges(boolean doit) {
    if(doit) {
      // Find all edges in the mesh structure that aren't connected to two faces, then make connected particles fixed
      HE_Edge[] edges = mesh.edgesAsArray();
      for(int i=0; i<edges.length; i++) {
        if(edges[i].f1() == null || edges[i].f2() == null) {
          physics.getParticle(getParticleIndex(edges[i].v1())).makeFixed();
          physics.getParticle(getParticleIndex(edges[i].v2())).makeFixed();
        }
      }
    }
    return this;
  }

  public HangingMesh setPerimeterTightness(float strength) {
    // Find all edges in the mesh structure that aren't connected to two faces, then make connected particles fixed
    HE_Edge[] edges = mesh.edgesAsArray();
    for(int i=0; i<edges.length; i++) {
      if(edges[i].f1() == null || edges[i].f2() == null) {
        physics.getSpring(i).setStrength(strength);
      }
    }
    return this;
  }

  public HangingMesh setTightness(int a, float strength) {
    Particle p = physics.getParticle(a);
    for(int i=0; i<physics.numberOfSprings(); i++) {
      if(physics.getSpring(i).getOneEnd() == p || physics.getSpring(i).getTheOtherEnd() == p) physics.getSpring(i).setStrength(strength);
    }
    return this;
  }

  public HangingMesh fixPoint(int i) {
    physics.getParticle(i).makeFixed();
    return this;
  }

  public HangingMesh freePoint(int i) {
    physics.getParticle(i).makeFree();
    return this;
  }

  public void tick() { this.tick(true); }

  public void tick(boolean update) {
    // Run the physics engine
    physics.tick();

    if(update) {
      // Update the mesh' vertices
      HE_Vertex[] verts = mesh.verticesAsArray();
      for(int i=0; i<physics.numberOfParticles() && i < verts.length; i++) {  // Number of particles and vertices should be the same but just in case...
        Particle p = physics.getParticle(i);
        verts[i].set(new HE_Vertex(p.position().x(), p.position().y(), p.position().z()));
      }
    }
  }

  public HangingMesh setDefaultDamping(float d) {
    this.defaultDamping = d;
    return this;
  }

  public HangingMesh setDefaultStrength(float d) {
    this.defaultStrength = d;
    return this;
  }

  public HangingMesh setGravity(WB_Point g) {
    physics.setGravity((float)g.x, (float)g.y, (float)g.z);
    return this;
  }

  public HangingMesh setDrag(float d) {
    physics.setDrag(d);
    return this;
  }

  private int getParticleIndex(HE_Vertex v) {
    HE_Vertex[] verts = mesh.verticesAsArray();
    for(int i=0; i<verts.length; i++) {
      if(v == verts[i]) return i;
    }
    return -1;
  }

  public void draw() {
    mesh.drawEdges();
  }

  /*
  private boolean samePoint(WB_Point a, WB_Point b) {
    // Used to see if two points are located within a small distance from each other
    return (a.distance2To(b) < HE.precision() * HE.precision());
  }

  private boolean samePoint(WB_Point a, float x, float y, float z) {
    // Used to see if two points are located within a small distance from each other
    return (a.distance2To(new WB_Point(x, y, z)) < HE.precision() * HE.precision());
  }
  */

}
