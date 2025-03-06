package com.niting.main.mainMenu;

import by.fxg.gdxpsx.g3d.PSXShaderProvider;
import by.fxg.gdxpsx.g3d.attributes.AttributePSXEffect;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessing;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessingWrapper;
import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.loaders.MusicLoader;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.*;
import com.crashinvaders.vfx.effects.util.MixEffect;
import com.niting.main.GUI;
import com.niting.main.GameScreen;
import com.niting.main.Main;
import com.niting.main.Settings;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneAsset;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class EndScreen extends ScreenAdapter {
    public final Color BACKGROUND_COLOUR = new Color(244f/255f, 219f/255f, 214f/255f, 1.0f);

    public Game game;

    private OrthographicCamera cam;
    private Texture end;
    private SpriteBatch spriteBatch;

    private VfxManager vfxManager;

    // Effects
    private BloomEffect bloomEffect;
    private CrtEffect crtEffect;
    private FilmGrainEffect filmGrainEffect;
    private VignettingEffect vignettingEffect;

    private Music music;

    public EndScreen(Game game)
    {
        this.game = game;
        music = Gdx.audio.newMusic(Gdx.files.internal("audio/End.wav"));
        music.setVolume(0.35f);
        music.play();
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();

        cam = new OrthographicCamera();
        cam.position.set(50f, 3f, 27f);
        cam.lookAt(0,-10f,0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        end = new Texture(Gdx.files.internal("images/end.png"));

        // GDX-VFX
        vfxManager = new VfxManager(Pixmap.Format.RGBA8888);

        crtEffect = new CrtEffect();
        filmGrainEffect = new FilmGrainEffect();
        bloomEffect = new BloomEffect();
        vignettingEffect = new VignettingEffect(false);

        vfxManager.addEffect(crtEffect, 3);
        vfxManager.addEffect(filmGrainEffect, 2);
        vfxManager.addEffect(bloomEffect, 4);
        //vfxManager.addEffect(vignettingEffect, 1);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            Gdx.app.exit();
        }

        updateEffects(delta);

        cam.update();

        vfxManager.cleanUpBuffers();

        // Rendering 2d
        vfxManager.beginInputCapture();
        spriteBatch.begin();
        ScreenUtils.clear(BACKGROUND_COLOUR, false);
        spriteBatch.draw(end, (Gdx.graphics.getWidth() - end.getWidth()) / 2f, (Gdx.graphics.getHeight() - end.getHeight()) / 2f);
        spriteBatch.end();
        vfxManager.endInputCapture();

        vfxManager.applyEffects();
        vfxManager.renderToScreen();
    }

    private void updateEffects(float delta)
    {
        crtEffect.update(delta);
        filmGrainEffect.update(delta);
        bloomEffect.update(delta);
        vignettingEffect.update(delta);
    }

    @Override
    public void resize(int width, int height) {
        vfxManager.resize(width, height);
    }


    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        vfxManager.dispose();
        spriteBatch.dispose();
        end.dispose();

        filmGrainEffect.dispose();
        crtEffect.dispose();
        bloomEffect.dispose();
        vignettingEffect.dispose();

        music.dispose();
    }
}

