package com.niting.main.inputs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntIntMap;
import com.niting.main.*;
import com.niting.main.physics.CollisionShapeType;
import com.niting.main.physics.PhysicsRayCaster;

import java.util.Set;

public class PlayerController extends InputAdapter  {
    public int forwardKey = Input.Keys.W;
    public int backwardKey = Input.Keys.S;
    public int strafeLeftKey = Input.Keys.A;
    public int strafeRightKey = Input.Keys.D;
    public int forwardAlt = Input.Keys.UP;
    public int backwardAlt = Input.Keys.DOWN;
    public int turnLeftKey = Input.Keys.LEFT;
    public int turnRightKey = Input.Keys.RIGHT;
    public int jumpKey = Input.Keys.SPACE;
    public int runShiftKey = Input.Keys.SHIFT_LEFT;
    public int runShiftAlt = Input.Keys.SHIFT_RIGHT;
    public int interactKey = Input.Keys.E;

    private final IntIntMap keys = new IntIntMap();
    private final Vector3 linearForce;
    private final Vector3 forwardDirection;   // direction player is facing, move direction, in XZ plane
    private final Vector3 viewingDirection;   // look direction, is forwardDirection plus Y component
    private float mouseDeltaX;
    private float mouseDeltaY;
    private final PhysicsRayCaster rayCaster;
    private final Vector3 groundNormal = new Vector3();
    private final Vector3 tmp = new Vector3();
    private final Vector3 tmp2 = new Vector3();
    private final Vector3 tmp3 = new Vector3();

    private final Sound walkSound;
    private final Sound runSound;

    // Track whether sounds are playing
    private boolean isWalkSoundPlaying = false;
    private boolean isRunSoundPlaying = false;

    private boolean wasOnGround = true; // Track previous ground state

    public float stamina = 5f;

    private boolean canRun;
    private World world;
    public boolean foundGenerator;

    public PlayerController(PhysicsRayCaster rayCaster, World world)  {
        this.rayCaster = rayCaster;
        linearForce = new Vector3();
        forwardDirection = new Vector3();
        viewingDirection = new Vector3(0, 0, 0);
        reset();

        walkSound = Main.assets.sounds.WALK;
        runSound = Main.assets.sounds.RUN;

        this.world = world;
    }

    public void reset() {
        forwardDirection.set(1,0,0);
        viewingDirection.set(forwardDirection);
    }

    public Vector3 getViewingDirection() {
        return viewingDirection;
    }

    public Vector3 getForwardDirection() {
        return forwardDirection;
    }

    @Override
    public boolean keyDown (int keycode) {
        keys.put(keycode, keycode);
        return true;
    }

    @Override
    public boolean keyUp (int keycode) {
        keys.remove(keycode, 0);
        return true;
    }

    private final PhysicsRayCaster.HitPoint hitPoint = new PhysicsRayCaster.HitPoint();

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // ignore big delta jump on start up or resize
        if(Math.abs(Gdx.input.getDeltaX()) >=100 && Math.abs(Gdx.input.getDeltaY()) >= 100)
            return true;
        mouseDeltaX = -Gdx.input.getDeltaX() * Settings.degreesPerPixel;
        mouseDeltaY = -Gdx.input.getDeltaY() * Settings.degreesPerPixel;
        //Gdx.app.log("Is On", String.valueOf(hitPoint.refObject.isOn));
        return true;
    }

    private void rotateView( float deltaX, float deltaY ) {
        viewingDirection.rotate(Vector3.Y, deltaX);

        if (!Settings.freeLook) {    // keep camera movement in the horizontal plane
            viewingDirection.y = 0;
            return;
        }
        if (Settings.invertLook)
            deltaY = -deltaY;

        // avoid gimbal lock when looking straight up or down
        Vector3 oldPitchAxis = tmp.set(viewingDirection).crs(Vector3.Y).nor();
        Vector3 newDirection = tmp2.set(viewingDirection).rotate(tmp, deltaY);
        Vector3 newPitchAxis = tmp3.set(tmp2).crs(Vector3.Y);
        if (!newPitchAxis.hasOppositeDirection(oldPitchAxis))
            viewingDirection.set(newDirection);
    }

    public void moveForward( float distance ){
        linearForce.set(forwardDirection).scl(distance);
    }

    private void strafe( float distance ){
        tmp.set(forwardDirection).crs(Vector3.Y);   // cross product
        tmp.scl(distance);
        linearForce.add(tmp);
    }

    boolean isRunning;

    public void update (GameObject player, float deltaTime ) {
        if (!world.mainMenu) {
            // derive forward direction vector from viewing direction
            forwardDirection.set(viewingDirection);
            forwardDirection.y = 0;
            forwardDirection.nor();

            // reset velocities
            linearForce.set(0, 0, 0);

            boolean isOnGround = rayCaster.isGrounded(player, player.getPosition(), Settings.groundRayLength, groundNormal);
            // disable gravity if player is on a slope
            if (isOnGround) {
                float dot = groundNormal.dot(Vector3.Y);
                player.body.geom.getBody().setGravityMode(dot >= 0.99f);
                //Gdx.app.log("isOnGround", player.getPosition().toString()+isOnGround+" N="+groundNormal.toString()+" is on slope: "+(dot<0.99f));
            } else {
                player.body.geom.getBody().setGravityMode(true);
                //Gdx.app.log("isOnGround", ""+isOnGround);
            }


            float moveSpeed = Settings.walkSpeed;
            if (keys.containsKey(runShiftKey) || keys.containsKey(runShiftAlt)) {
                if (canRun)
                    moveSpeed *= Settings.runFactor;
            }

            // mouse to move view direction
            rotateView(mouseDeltaX * deltaTime * Settings.turnSpeed, mouseDeltaY * deltaTime * Settings.turnSpeed);
            mouseDeltaX = 0;
            mouseDeltaY = 0;

            // note: most of the following is only valid when on ground, but we leave it to allow some fun cheating
            if (keys.containsKey(forwardKey) || keys.containsKey(forwardAlt))
                moveForward(deltaTime * moveSpeed);
            if (keys.containsKey(backwardKey) || keys.containsKey(backwardAlt))
                moveForward(-deltaTime * moveSpeed);
            if (keys.containsKey(strafeLeftKey))
                strafe(-deltaTime * Settings.walkSpeed);
            if (keys.containsKey(strafeRightKey))
                strafe(deltaTime * Settings.walkSpeed);
            if (keys.containsKey(turnLeftKey))
                rotateView(deltaTime * Settings.turnSpeed, 0);
            if (keys.containsKey(turnRightKey))
                rotateView(-deltaTime * Settings.turnSpeed, 0);

            if ((isOnGround && keys.containsKey(jumpKey))) {
                if (!canRun)
                    linearForce.y = Settings.jumpForce / 1.25f;
                else
                    linearForce.y = Settings.jumpForce;
                Main.assets.sounds.JUMP_UP.play(0.15f);
            }

            // Check if the player has just landed
            if (!wasOnGround && isOnGround) {
                Main.assets.sounds.JUMP_DOWN.play(0.1f);
            }

            wasOnGround = isOnGround;

            boolean isWalking = (keys.containsKey(forwardKey) || keys.containsKey(forwardAlt) ||
                keys.containsKey(backwardKey) || keys.containsKey(backwardAlt) ||
                keys.containsKey(strafeLeftKey) || keys.containsKey(strafeRightKey));

            isRunning = isWalking && (keys.containsKey(runShiftKey) || keys.containsKey(runShiftAlt)) && canRun;

            // Play and loop sounds based on movement state
            if (isRunning && isOnGround) {
                if (!isRunSoundPlaying) {
                    runSound.loop(0.1f);
                    isRunSoundPlaying = true;
                }
                if (isWalkSoundPlaying) {
                    walkSound.stop();
                    isWalkSoundPlaying = false;
                }
            } else if (isWalking && isOnGround) {
                if (!isWalkSoundPlaying) {
                    walkSound.loop(0.1f);
                    isWalkSoundPlaying = true;
                }
                if (isRunSoundPlaying) {
                    runSound.stop();
                    isRunSoundPlaying = false;
                }
            } else {
                // Stop all sounds if not walking or running
                if (isWalkSoundPlaying) {
                    walkSound.stop();
                    isWalkSoundPlaying = false;
                }
                if (isRunSoundPlaying) {
                    runSound.stop();
                    isRunSoundPlaying = false;
                }
            }

            world.rayCaster.findTarget(world.getPlayer().getPosition(), viewingDirection, hitPoint);
            foundGenerator = hitPoint.distance < 3f && hitPoint.refObject.type.isGenerator && !hitPoint.refObject.isOn;
            if (keys.containsKey(interactKey) && foundGenerator) {
                hitPoint.refObject.isOn = true;
                world.stats.numGenerators++;
                if (world.stats.numGenerators == 2)
                {
                    for (GameObject go: world.gameObjects)
                    {
                        if (go.type.isGate)
                        {
                            world.removeObject(go);
                        }
                    }
                    Main.assets.sounds.GATE.play(0.75f);
                    GameObject enemy = world.spawnObject(GameObjectType.TYPE_ENEMY, "Armature", null, CollisionShapeType.CAPSULE, false, Vector3.Zero);
                    enemy.scene.animationController.allowSameAnimation = true;
                    enemy.scene.animationController.setAnimation("Run", -1);
                    world.setEnemy(enemy);
                }
                Main.assets.sounds.INTERACT.play(0.34f);
            }

            linearForce.scl(80);
            player.body.applyForce(linearForce);
        }
        if (isRunning && stamina > 0)
            stamina = stamina - deltaTime / 2;
        else if (stamina <= 5)
            stamina = stamina + deltaTime;

        if (stamina < 0.001f) {
            canRun = false;
        } else if (stamina > 4.95f) {
            canRun = true;
        }
    }

    public void stopSounds()
    {
        if (Settings.ENDED)
        {
            if (isWalkSoundPlaying)
                walkSound.stop();
            if (isRunSoundPlaying)
                runSound.stop();

            isWalkSoundPlaying = false;
            isRunSoundPlaying = false;
        }
    }
}
