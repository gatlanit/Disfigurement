package com.niting.main.mainMenu;

import by.fxg.gdxpsx.g3d.PSXShaderProvider;
import by.fxg.gdxpsx.g3d.attributes.AttributePSXEffect;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessing;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessingWrapper;
import com.badlogic.gdx.*;
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

public class MenuScreen extends ScreenAdapter {
    public final Color BACKGROUND_COLOUR = new Color(153f/255f, 255f/255f, 236f/255f, 1.0f);

    public Game game;

    private PerspectiveCamera cam;
    private SceneManager sceneManager;
    private SceneAsset sceneAsset;
    private Cubemap diffuseCubemap;
    private Cubemap environmentCubemap;
    private Cubemap specularCubemap;
    private Texture brdfLUT;
    private Texture cursor;
    private SceneSkybox skybox;

    private PSXPostProcessing postProcessing;
    private PSXPostProcessingWrapper wrapper;
    private SpriteBatch spriteBatch;
    private PointLightEx spotLight;

    private VfxManager vfxManager;

    // Effects
    private BloomEffect bloomEffect;
    private CrtEffect crtEffect;
    private FilmGrainEffect filmGrainEffect;
    private VignettingEffect vignettingEffect;

    public final Music ambiance;
    public final Music theme;

    private float seconds = 10f;
    private MenuGUI gui;
    InputMultiplexer im;

    public MenuScreen(Game game)
    {
        this.game = game;
        // For specifically the main game
        ambiance = Gdx.audio.newMusic(Gdx.files.internal("audio/Rain.wav"));
        ambiance.setVolume(0.15f);
        ambiance.setLooping(true);
        ambiance.play();

        theme = Gdx.audio.newMusic(Gdx.files.internal("audio/MainMenu.wav"));
        theme.setVolume(0.65f);
        theme.setLooping(true);
        theme.play();
    }

    @Override
    public void show() {
        spriteBatch = new SpriteBatch();

        gui = new MenuGUI(this);

        im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        im.addProcessor(gui.stage);

        cam = new PerspectiveCamera(84, Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
        cam.position.set(50f, 3f, 27f);
        cam.lookAt(0,-10f,0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        sceneManager = new SceneManager(new PSXShaderProvider(), new DepthShaderProvider());
        sceneAsset = new GLTFLoader().load(Gdx.files.internal("models/tilemap.gltf")); // change
        Scene scene = new Scene(sceneAsset.scene);
        sceneManager.addScene(scene);

        sceneManager.setCamera(cam);

        // post-processing
        postProcessing = new PSXPostProcessing();
        postProcessing.setDefaultParametersWithResolution();
        postProcessing.setInputResolution(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f);
        postProcessing.setResolutionDownscalingFactor(4f);
        postProcessing.compile(false); // To compile dynamic shader

        wrapper = new PSXPostProcessingWrapper(postProcessing);
        wrapper.createFrameBuffer(Pixmap.Format.RGBA8888, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true); // create frame buffer with default parameters

        // GDX-VFX
        vfxManager = new VfxManager(Pixmap.Format.RGBA8888);

        crtEffect = new CrtEffect();
        filmGrainEffect = new FilmGrainEffect();
        bloomEffect = new BloomEffect();
        vignettingEffect = new VignettingEffect(false);

        vfxManager.addEffect(crtEffect, 3);
        vfxManager.addEffect(filmGrainEffect, 2);
        vfxManager.addEffect(bloomEffect, 4);
        vfxManager.addEffect(vignettingEffect, 1);

        // lighting
        DirectionalLightEx light = new DirectionalLightEx();
        light.set(Color.BLACK, new Vector3(0, -1, 0));
        light.intensity = 0f;

        sceneManager.environment.add(light);

        spotLight = new PointLightEx();
        spotLight.set(new Color(189 / 255f, 112f / 255f, 40 / 255f, 1f), cam.position, 25f);
        sceneManager.environment.add(spotLight);

        // setup quick IBL (image based lighting)
        IBLBuilder iblBuilder = IBLBuilder.createIndoor(light);

        // This texture is provided by the library, no need to have it in your assets.
        brdfLUT = new Texture(Gdx.files.classpath("net/mgsx/gltf/shaders/brdfLUT.png"));

        environmentCubemap = EnvironmentUtil.createCubemap(new InternalFileHandleResolver(),
            "images/Skybox_", ".png", EnvironmentUtil.FACE_NAMES_FULL); // skybox

        diffuseCubemap = iblBuilder.buildIrradianceMap(256);
        specularCubemap = iblBuilder.buildRadianceMap(10);
        iblBuilder.dispose();

        sceneManager.setAmbientLight(0.01f);
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        sceneManager.environment.set(AttributePSXEffect.createVertexSnapping(24.5F)); //add vertex snapping effect with 4.0 strength
        sceneManager.environment.set(AttributePSXEffect.createTextureAffineMapping(1.0F));

        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            Gdx.app.exit();
        }


        cam.rotate(Vector3.Y, 15 * delta);

        cam.update();
        sceneManager.update(delta);
        // render
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        vfxManager.cleanUpBuffers();

        // Rendering 3d
        wrapper.beginFrameBufferCapture();
        sceneManager.render();
        wrapper.endFrameBufferCapture();

        // Rendering 2d
        vfxManager.beginInputCapture();
        wrapper.drawPostProcessedTexture(spriteBatch);
        gui.render(delta);
        vfxManager.endInputCapture();

        vfxManager.applyEffects();
        vfxManager.renderToScreen();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            ambiance.stop();
            theme.stop();
            game.setScreen(new GameScreen(game));
        }

        seconds = seconds - delta;
        if (seconds <= 0f)
        {
            chanceThunder();
        }
    }

    private void chanceThunder()
    {
        if (MathUtils.random(1, 2) == 1)
        {
            int randThunder = MathUtils.random(0,2);
            switch(randThunder) {
                case 0:
                    // Code to handle case when randThunder is 0
                    Main.assets.sounds.THUNDER1.play(0.1f);
                    break;
                case 1:
                    // Code to handle case when randThunder is 1
                    Main.assets.sounds.THUNDER2.play(0.1f);
                    break;
                case 2:
                    // Code to handle case when randThunder is 2
                    Main.assets.sounds.THUNDER3.play(0.1f);
                    break;
            }
        }
        seconds = 15f;
    }

    @Override
    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);
        vfxManager.resize(width, height);
        gui.resize(width, height);
    }


    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        vfxManager.dispose();
        sceneManager.dispose();
        spriteBatch.dispose();
        postProcessing.dispose();
        wrapper.dispose();
        environmentCubemap.dispose();
        diffuseCubemap.dispose();
        specularCubemap.dispose();
        brdfLUT.dispose();
        skybox.dispose();;

        filmGrainEffect.dispose();
        crtEffect.dispose();
        bloomEffect.dispose();
        vignettingEffect.dispose();

        ambiance.dispose();
        gui.dispose();
        theme.dispose();
    }
}

