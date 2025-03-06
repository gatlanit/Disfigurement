package com.niting.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Disposable;
import net.mgsx.gltf.loaders.gltf.GLTFAssetLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class Assets implements Disposable {

    public class AssetSounds {

        // constants for single sounds effects in game
        public final Sound WALK;
        public final Sound RUN;

        public final Sound JUMP_UP;
        public final Sound JUMP_DOWN;
        public final Sound LIGHTER_ON;
        public final Sound LIGHTER_OFF;
        public final Sound THUNDER1;
        public final Sound THUNDER2;
        public final Sound THUNDER3;
        public final Sound GATE;
        public final Sound INTERACT;
        public final Sound CHASE;

        public AssetSounds() {
            WALK = assets.get("audio/Walk.wav");
            RUN = assets.get("audio/Run.wav");

            JUMP_UP  = assets.get("audio/JumpUp.wav");
            JUMP_DOWN  = assets.get("audio/JumpDown.wav");
            LIGHTER_ON  = assets.get("audio/LighterOn.wav");
            LIGHTER_OFF = assets.get ("audio/LighterOff.wav");
            THUNDER1 = assets.get("audio/Thunder1.wav");
            THUNDER2 = assets.get("audio/Thunder2.wav");
            THUNDER3 = assets.get("audio/Thunder3.wav");
            GATE = assets.get("audio/GateOpen.wav");
            INTERACT = assets.get("audio/Interact.wav");
            CHASE = assets.get("audio/Stinger.wav");
        }
    }

    public AssetSounds sounds;
    public SceneAsset sceneAsset;
    public Skin skin;
    public Skin menuSkin;
    private AssetManager assets;
    public Texture crosshair;

    public Assets() {
        Gdx.app.log("Assets constructor", "");
        assets = new AssetManager();

        assets.load("skin/vhs-ui.json", Skin.class);
        assets.load("menuSkin/vhs-ui.json", Skin.class);

        assets.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
        assets.load( Settings.GLTF_FILE, SceneAsset.class);

        assets.load("audio/JumpUp.wav", Sound.class);
        assets.load("audio/JumpDown.wav", Sound.class);
        assets.load("audio/LighterOn.wav", Sound.class);
        assets.load("audio/LighterOff.wav", Sound.class);
        assets.load("audio/Thunder1.wav", Sound.class);
        assets.load("audio/Thunder2.wav", Sound.class);
        assets.load("audio/Thunder3.wav", Sound.class);
        assets.load("audio/GateOpen.wav", Sound.class);
        assets.load("audio/Interact.wav", Sound.class);
        assets.load("audio/Stinger.wav", Sound.class);

        assets.load("audio/Walk.wav", Sound.class);
        assets.load("audio/Run.wav", Sound.class);

        assets.load("images/cursor.png", Texture.class);
    }

    public void finishLoading() {
        assets.finishLoading();
        initConstants();
    }

    private void initConstants() {
        sounds = new AssetSounds();
        skin = assets.get("skin/vhs-ui.json");
        menuSkin = assets.get("menuSkin/vhs-ui.json");
        sceneAsset = assets.get(Settings.GLTF_FILE);
        crosshair = assets.get("images/cursor.png");
    }

    public <T> T get(String name ) {
        return assets.get(name);
    }

    @Override
    public void dispose() {
        Gdx.app.log("Assets dispose()", "");
        assets.dispose();
        assets = null;
    }
}
