package com.niting.main.npc;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.niting.main.GameObject;
import com.niting.main.Settings;
import com.niting.main.World;
import com.niting.main.pathfinding.NavActor;

public class CookBehaviour extends Behaviour {

    private float chaseTimer;
    private final Vector3 direction = new Vector3();
    private final Vector3 targetDirection = new Vector3();
    private final Vector3 playerVector = new Vector3();
    public NavActor navActor;
    public boolean isChasing = false;
    public Array<Vector3> randPositions;
    private int index;
    private final Vector3 randVector = new Vector3();
    private float spotDistance;

    public CookBehaviour(GameObject go) {
        super(go);
        chaseTimer = 0f;
        go.body.setCapsuleCharacteristics();
        randPositions = new Array<>();
        randPositions.add(new Vector3(55, 0, -35));
        randPositions.add(new Vector3(63, 0, -89));
        randPositions.add(new Vector3(9.5f, 0, -80));
        randPositions.add(new Vector3(1, 0, -35));
        index = MathUtils.random(0, 3);
        nextPos();
    }

    private void nextPos()
    {
        index++;
        index %= 3;
    }

    private void fixValues()
    {
        randPositions.clear();
        randPositions.add(new Vector3(55, 0, -35));
        randPositions.add(new Vector3(63, 0, -89));
        randPositions.add(new Vector3(9.5f, 0, -80));
        randPositions.add(new Vector3(1, 0, -35));
    }

    public Vector3 getDirection() {
        return direction;
    }

    @Override
    public void update(World world, float deltaTime ) {
        playerVector.set(world.getPlayer().getPosition()).sub(go.getPosition());    // vector to player in a straight line
        float distance = playerVector.len();

        if (Settings.lightOn)
            spotDistance = 15f;
        else
            spotDistance = 5f;

        isChasing = checkPlayer(world, distance);
        if (isChasing) {
            if (navActor == null) {   // lazy init because we need world.navMesh
                navActor = new NavActor(world.navMesh);
            }

            Vector3 wayPoint = navActor.getWayPoint(go.getPosition(), world.getPlayer().getPosition());  // next point to aim for on the route to target

            // move towards waypoint general area for smart AI
            if (distance > spotDistance) {
                targetDirection.set(wayPoint).sub(go.getPosition());  // vector towards way point
                if (targetDirection.len() > 1f) {    // if we're at the way point, stop turning to avoid nervous jittering
                    targetDirection.y = 0;  // consider only vector in horizontal plane
                    targetDirection.nor();      // make unit vector
                    direction.slerp(targetDirection, 0.02f);            // smooth rotation towards target direction
                    go.body.applyForce(targetDirection.scl(32f));
                }
            }
            else { // Player is close to enemy use old navigation
                targetDirection.set(world.getPlayer().getPosition()).sub(go.getPosition());  // vector towards player
                targetDirection.y = 0;  // consider only vector in horizontal plane
                targetDirection.nor();      // make unit vector
                direction.set(targetDirection);
                direction.slerp(targetDirection, 0.02f);            // smooth rotation towards target direction
                go.body.applyForce(targetDirection.scl(32f));
                chaseTimer = 5f;
            }
        }
        else // roam
        {
            randVector.set(randPositions.get(index).sub(go.getPosition()));    // vector to player in a straight line
            distance = randVector.len();
            fixValues();

            if (navActor == null) {   // lazy init because we need world.navMesh
                navActor = new NavActor(world.navMesh);
            }

            Vector3 wayPoint = navActor.getWayPoint(go.getPosition(), (randPositions.get(index)));  // next point to aim for on the route to target

            // move towards waypoint
            targetDirection.set(wayPoint).sub(go.getPosition());  // vector towards way point
            if (targetDirection.len() > 1f) {    // if we're at the way point, stop turning to avoid nervous jittering
                targetDirection.y = 0;  // consider only vector in horizontal plane
                targetDirection.nor();      // make unit vector
                direction.slerp(targetDirection, 0.02f);            // smooth rotation towards target direction
                go.body.applyForce(targetDirection.scl(34f));
            }
            if (distance < 2f)
                nextPos();
        }
        chaseTimer = chaseTimer - deltaTime;
        Settings.isChasing = isChasing;
    }

    private boolean checkPlayer(World world, float distance) {
        return world.getPlayer().getPosition().z < -25 && (distance < spotDistance || chaseTimer > 0);
    }
}

