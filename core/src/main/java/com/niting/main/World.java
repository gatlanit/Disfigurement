package com.niting.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.github.antzGames.gdx.ode4j.ode.DJoint;
import com.niting.main.npc.CookBehaviour;
import com.niting.main.inputs.PlayerController;
import com.niting.main.pathfinding.NavMesh;
import com.niting.main.pathfinding.NavMeshBuilder;
import com.niting.main.pathfinding.NavNode;
import com.niting.main.physics.*;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class World implements Disposable {

    public final Array<GameObject> gameObjects;
    private GameObject player;
    private GameObject enemy;
    public boolean enemyChase = false;
    private final SceneAsset sceneAsset;
    private boolean isDirty;
    private final PhysicsWorld physicsWorld;
    private final PhysicsBodyFactory factory;
    private final PlayerController playerController;
    public final PhysicsRayCaster rayCaster;
    private final Music ambiance;
    private final Sound chase;

    public boolean mainMenu = false;
    public final GameStats stats;

    private float seconds = 60f;

    public NavMesh navMesh;
    public NavNode navNode;
    boolean stingerPlaying;

    public World() {
        gameObjects = new Array<>();
        stats = new GameStats();
        sceneAsset = Main.assets.sceneAsset;                // <----- use Main.assets
        isDirty = true;
        physicsWorld = new PhysicsWorld(this);
        factory = new PhysicsBodyFactory(physicsWorld);
        rayCaster = new PhysicsRayCaster(physicsWorld);
        playerController = new PlayerController(rayCaster, this);

        // For specifically the main game
        ambiance = Gdx.audio.newMusic(Gdx.files.internal("audio/Rain.wav"));
        ambiance.setVolume(0.25f);
        ambiance.setLooping(true);
        ambiance.play();

        chase = Main.assets.sounds.CHASE;
        stingerPlaying = false;
    }

    public boolean isDirty(){
        return isDirty;
    }

    public void clear() {
        physicsWorld.reset();
        playerController.reset();
        stats.reset();

        gameObjects.clear();
        player = null;
        navMesh = null;
    }
    public int getNumGameObjects() {
        return gameObjects.size;
    }

    public SceneAsset getSceneAsset(){
        return sceneAsset;
    }

    public GameObject getGameObject(int index) {
        return gameObjects.get(index);
    }

    public GameObject getPlayer() {
        return player;
    }

    public GameObject getEnemy() {return enemy;}

    public void setPlayer( GameObject player ){
        this.player = player;
        player.body.setCapsuleCharacteristics();
    }

    public void setEnemy( GameObject enemy ){
        this.enemy = enemy;
        //enemy.scene.animationController.allowSameAnimation = true;
        //enemy.scene.animationController.setAnimation("Run", -1);
    }

    public PlayerController getPlayerController() {
        return playerController;
    }

    public GameObject spawnObject(GameObjectType type, String name, String proxyName, CollisionShapeType shapeType, boolean resetPosition, Vector3 position){
        Scene scene = loadNode( name, resetPosition, position );
        ModelInstance collisionInstance = scene.modelInstance;
        if(proxyName != null) {
            Scene proxyScene = loadNode( proxyName, resetPosition, position );
            collisionInstance = proxyScene.modelInstance;
        }
        PhysicsBody body = null;
        if(type == GameObjectType.TYPE_NAVMESH){
            navMesh = NavMeshBuilder.build(scene.modelInstance);
            return null;
        }
        body = factory.createBody(collisionInstance, shapeType, type.isStatic);
        GameObject go = new GameObject(type, scene, body);
        gameObjects.add(go);

        return go;
    }

    private Scene loadNode( String nodeName, boolean resetPosition, Vector3 position ) {
        Scene scene = new Scene(sceneAsset.scene, nodeName);
        if(scene.modelInstance.nodes.size == 0)
            throw new RuntimeException("Cannot find node in GLTF file: " + nodeName);
        applyNodeTransform(resetPosition, scene.modelInstance, scene.modelInstance.nodes.first());         // incorporate nodes' transform into model instance transform
        scene.modelInstance.transform.translate(position);
        return scene;
    }

    private void applyNodeTransform(boolean resetPosition, ModelInstance modelInstance, Node node ){
        if(!resetPosition)
            modelInstance.transform.mul(node.globalTransform);
        node.translation.set(0,0,0);
        node.scale.set(1,1,1);
        node.rotation.idt();
        modelInstance.calculateTransforms();
    }

    public void removeObject(GameObject gameObject){
        gameObjects.removeValue(gameObject, true);
        gameObject.dispose();
        isDirty = true;     // list of game objects has changed
    }

    public void update( float deltaTime ) {
        if (Settings.isChasing && !stingerPlaying) {
            chase.loop(0.22f);
            stingerPlaying = true;
        }
        else if (!Settings.isChasing && stingerPlaying){
            chase.stop();
            stingerPlaying = false;
        }

        playerController.update(player, deltaTime);
        physicsWorld.update();
        syncToPhysics();
        for(GameObject go : gameObjects)
            go.update(this, deltaTime);

        seconds = seconds - deltaTime;
        if (seconds <= 0f)
        {
            chanceThunder();
        }

        if (stats.numGenerators >=5)
        {
            Settings.ENDED = true;
            stopSounds();
        }
    }

    private void chanceThunder()
    {
        if (MathUtils.random(1, 4) == 1)
        {
            int randThunder = MathUtils.random(0,3);
            switch(randThunder) {
                case 0:
                    // Code to handle case when randThunder is 0
                    Main.assets.sounds.THUNDER1.play(0.7f); // 0.7
                    break;
                case 1:
                    // Code to handle case when randThunder is 1
                    Main.assets.sounds.THUNDER2.play(0.65f); // 0.65
                    break;
                case 2:
                    // Code to handle case when randThunder is 2
                    Main.assets.sounds.THUNDER3.play(0.65f); // 0.65
                    break;
            }
        }
        seconds = 120f;
    }

    private void syncToPhysics() {
        for(GameObject go : gameObjects){
            if( go.body.geom.getBody() != null) {
                if(go.type == GameObjectType.TYPE_PLAYER){
                    // use information from the player controller, since the rigid body is not rotated.
                    player.scene.modelInstance.transform.setToRotation(Vector3.Z, playerController.getForwardDirection());
                    player.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                }
                else if(go.type == GameObjectType.TYPE_ENEMY){
                    CookBehaviour ei = (CookBehaviour) go.behaviour;
                    go.scene.modelInstance.transform.setToRotation(Vector3.Z, ei.getDirection());
                    go.scene.modelInstance.transform.setTranslation(go.body.getPosition());
                    enemyChase = ei.isChasing;
                }
                else
                    go.scene.modelInstance.transform.set(go.body.getPosition(), go.body.getOrientation());
            }
        }
    }

    private final Vector3 dir = new Vector3();
    private final Vector3 spawnPos = new Vector3();
    private final Vector3 shootDirection = new Vector3();

    public void onCollision(GameObject go1, GameObject go2){
        // try either order
        handleCollision(go1, go2);
        handleCollision(go2, go1);
    }

    private void handleCollision(GameObject go1, GameObject go2){
        if(go1.type.isPlayer && go2.type.canPickup){
            pickup(go1, go2);
        }
        if(go1.type.isEnemy && go2.type.isPlayer)   // Player dies if this happens
        {
            Gdx.app.exit();
        }

    }

    private void stopSounds()
    {
        ambiance.stop();
        if (stingerPlaying)
        {
            chase.stop();
        }
    }

    // For now, removes object, will make it pickup into own game view along with giving lighter flashlight
    private void pickup(GameObject character, GameObject pickup){
        removeObject(pickup);
    }

    @Override
    public void dispose() {
        physicsWorld.dispose();
        rayCaster.dispose();
        ambiance.dispose();
    }
}
