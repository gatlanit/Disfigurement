package com.niting.main.pathfinding;

// node of the navigation mesh
// i.e. a triangle which is connected to other nodes

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

public class NavNode {
    public final int id;                    // for debugging
    public final Vector3 p0, p1, p2;        // the corners
    public Array<NavNode> neighbours;       // connection to other nodes
    public Vector3 normal;                  // normal vector
    private float d;                        // for plane equation
    public final Vector3 centre;            // centre point
    private Vector3 p = new Vector3();      // tmp var
    public int steps;                       // for use in search algo
    public NavNode prev;                    // for use in search algo


    public NavNode( int id, Vector3 a, Vector3 b, Vector3 c) {
        this.id = id;
        p0 = new Vector3(a);
        p1 = new Vector3(b);
        p2 = new Vector3(c);
        neighbours = new Array<>(3);

        centre = new Vector3(a).add(b).add(c).scl(1/3f);

        normal = new Vector3();
        Vector3 t1 = new Vector3(a);
        t1.sub(b);
        Vector3 t2 = new Vector3(c);
        t2.sub(b);
        normal.set(t2.crs(t1)).nor();      // use cross product of two edges to get normal vector (direction depends on winding order)

        // use a point on the plane (a) to find the distance value d of the plane equation: Ax + By + Cz + d = 0, where (A,B,C) is the normal
        d = -(normal.x*a.x + normal.y*a.y + normal.z*a.z);
    }

    public void addNeighbour( NavNode nbor  ){
        neighbours.add(nbor);
    }

    // https://stackoverflow.com/questions/2049582/how-to-determine-if-a-point-is-in-a-2d-triangle
    //
    public boolean isPointInTriangle(Vector3 point, float maxDist)
    {
        // project point onto plane of the triangle
        float distanceToPlane = point.dot(normal)+d;
        if(distanceToPlane < 0) // point needs to be above the plane
            return false;
        if(distanceToPlane > maxDist)       // triangle too far below point, discard
            return false;
        p.set(normal).scl(-distanceToPlane);                // vector from point to plane, subtract this from point
        p.add(point);

        return Intersector.isPointInTriangle(p, p0, p1, p2);
    }
}
