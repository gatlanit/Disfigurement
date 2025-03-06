package com.niting.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.math.Vector3;
import com.niting.main.physics.CollisionShapeType;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Populator {
    public static void populate(World world, SceneAsset sceneAsset) {
        world.clear();

        world.spawnObject(GameObjectType.TYPE_NAVMESH, "NAVMESH", null, CollisionShapeType.MESH, false, Vector3.Zero);

        world.spawnObject(GameObjectType.TYPE_STATIC, "Map", null, CollisionShapeType.MESH, false,Vector3.Zero);
        world.spawnObject(GameObjectType.TYPE_STATIC, "Floor", null, CollisionShapeType.BOX, false, Vector3.Zero);
        world.spawnObject(GameObjectType.TYPE_GATE, "Gate", null, CollisionShapeType.BOX, false, Vector3.Zero);

        for (Node node : sceneAsset.scene.model.nodes) { // all crates
            if (node.id.contains("Crate")) {
                world.spawnObject(GameObjectType.TYPE_STATIC, node.id, null, CollisionShapeType.BOX, false, Vector3.Zero);
            }
            if (node.id.contains("Generator"))
            {
                world.spawnObject(GameObjectType.TYPE_GENERATOR, node.id, null, CollisionShapeType.BOX, false, Vector3.Zero);
            }
        }

        GameObject go = world.spawnObject(GameObjectType.TYPE_PLAYER, "Player", null, CollisionShapeType.CAPSULE, true, new Vector3(0, 1.2f, 0));
        go.visible = false;
        world.setPlayer(go);
    }
}
