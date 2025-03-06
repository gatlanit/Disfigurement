package com.niting.main.inputs;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector3;

public class CameraController extends InputAdapter {

    private final Camera camera;

    public CameraController(Camera camera ) {
        this.camera = camera;
    }

    public void update ( Vector3 playerPosition, Vector3 viewDirection ) {
        camera.position.set(playerPosition.x, playerPosition.y + 0.5f, playerPosition.z);
        camera.direction.set(viewDirection);
        camera.update(true);
    }
}
