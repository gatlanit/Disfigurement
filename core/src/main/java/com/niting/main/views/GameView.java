package com.niting.main.views;

/*

This class is for rendering, post-processing, and lighting

 */
import by.fxg.gdxpsx.g3d.PSXShaderProvider;
import by.fxg.gdxpsx.g3d.attributes.AttributePSXEffect;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessing;
import by.fxg.gdxpsx.postprocessing.PSXPostProcessingWrapper;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.DepthShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.crashinvaders.vfx.VfxManager;
import com.crashinvaders.vfx.effects.*;
import com.crashinvaders.vfx.effects.util.MixEffect;
import com.niting.main.*;
import com.niting.main.inputs.CameraController;
import com.niting.main.physics.CollisionShapeType;
import net.mgsx.gltf.scene3d.attributes.PBRCubemapAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRTextureAttribute;
import net.mgsx.gltf.scene3d.lights.DirectionalLightEx;
import net.mgsx.gltf.scene3d.lights.PointLightEx;
import net.mgsx.gltf.scene3d.scene.Scene;
import net.mgsx.gltf.scene3d.scene.SceneManager;
import net.mgsx.gltf.scene3d.scene.SceneSkybox;
import net.mgsx.gltf.scene3d.utils.EnvironmentUtil;
import net.mgsx.gltf.scene3d.utils.IBLBuilder;

public class GameView implements Disposable {
    private final World world;

    private final PerspectiveCamera cam;
    private final CameraController camController;

    private final SceneManager sceneManager;
    private final Cubemap diffuseCubemap;
    private final Cubemap environmentCubemap;
    private final Cubemap specularCubemap;
    private final Texture brdfLUT;
    private final Texture cursor;
    private final SceneSkybox skybox;

    private final PSXPostProcessing postProcessing;
    private final PSXPostProcessingWrapper wrapper;
    private final SpriteBatch spriteBatch;
    private final PointLightEx spotLight;

    private final VfxManager vfxManager;

    // Effects
    private final BloomEffect bloomEffect;
    private final CrtEffect crtEffect;
    private final FilmGrainEffect filmGrainEffect;
    private final VignettingEffect vignettingEffect;
    private final MotionBlurEffect motionBlurEffect;
    private final ChromaticAberrationEffect chromaticAberrationEffect;

    // bob specifics
    private float bobAngle;     // angle in the camera bob cycle (radians)
    private final float bobScale;     // scale factor for camera bobbing
    private float pulsationTime; // Track time for pulsation
    public boolean lightOn = true;

    public GUI gui;

    public GameView(World world, float bobScale, GameScreen screen) {
        spriteBatch = new SpriteBatch();

        gui = new GUI(world, screen);
        cursor = new Texture(Gdx.files.internal("images/cursor.png"));

        this.world = world;
        this.bobScale = bobScale;
        sceneManager = new SceneManager(new PSXShaderProvider(), new DepthShaderProvider());

        cam = new PerspectiveCamera(Settings.FOV, Gdx.graphics.getWidth(),  Gdx.graphics.getHeight());
        cam.position.set(0f, Settings.eyeHeight, 0f);
        cam.lookAt(0,Settings.eyeHeight,0);
        cam.near = 0.1f;
        cam.far = 300f;
        cam.update();

        sceneManager.setCamera(cam);
        camController = new CameraController(cam);

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
        motionBlurEffect = new MotionBlurEffect(Pixmap.Format.RGBA8888, MixEffect.Method.MIX, 0.85f);
        chromaticAberrationEffect = new ChromaticAberrationEffect(100);

        vfxManager.addEffect(crtEffect, 3);
        vfxManager.addEffect(filmGrainEffect, 2);
        vfxManager.addEffect(bloomEffect, 4);
        vfxManager.addEffect(vignettingEffect, 1);
        vfxManager.addEffect(chromaticAberrationEffect);
        vfxManager.addEffect(motionBlurEffect);

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

        sceneManager.setAmbientLight(0.01f); // og 0.01
        sceneManager.environment.set(new PBRTextureAttribute(PBRTextureAttribute.BRDFLUTTexture, brdfLUT));
        sceneManager.environment.set(PBRCubemapAttribute.createSpecularEnv(specularCubemap));
        sceneManager.environment.set(PBRCubemapAttribute.createDiffuseEnv(diffuseCubemap));
        sceneManager.environment.set(AttributePSXEffect.createVertexSnapping(24.5F)); //add vertex snapping effect with 4.0 strength
        sceneManager.environment.set(AttributePSXEffect.createTextureAffineMapping(1.0F));

        skybox = new SceneSkybox(environmentCubemap);
        sceneManager.setSkyBox(skybox);
    }

    public PerspectiveCamera getCamera() {
        return cam;
    }

    public CameraController getCameraController() {
        return camController;
    }

    public void refresh() {
        sceneManager.getRenderableProviders().clear();        // remove all scenes

        // add scene for each game object
        int num = world.getNumGameObjects();
        for(int i = 0; i < num; i++){
            Scene scene = world.getGameObject(i).scene;
            if(world.getGameObject(i).visible)
                sceneManager.addScene(scene, false);
        }
    }

    public void render(float delta, float speed) {
        camController.update(world.getPlayer().getPosition(), world.getPlayerController().getViewingDirection());
        if(world.isDirty())
            refresh();
        addHeadBob(delta, speed);
        cam.update();
        sceneManager.update(delta);
        spotLight.position.set(cam.position);

        bobAngle %= (float) (Math.PI * 2);

        // Light pulsing effect
        updateLight(delta);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F))
        {
            lightOn = !lightOn;
            if (lightOn)
            {
                Main.assets.sounds.LIGHTER_ON.play(0.1f);
            }
            else {
                Main.assets.sounds.LIGHTER_OFF.play(0.1f);
            }
        }

        Settings.lightOn = lightOn; // update settings
        // Clear
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);

        if (world.enemyChase)
        {
            chromaticAberrationEffect.setDisabled(false);
            motionBlurEffect.setDisabled(false);
        }
        else
        {
            chromaticAberrationEffect.setDisabled(true);
            motionBlurEffect.setDisabled(true);
        }

        updateEffects(delta);

        vfxManager.cleanUpBuffers();

        // Rendering

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
    }

    public void resize(int width, int height) {
        sceneManager.updateViewport(width, height);
        vfxManager.resize(width, height);
        gui.resize(width, height);
    }

    private void updateEffects(float delta)
    {
        crtEffect.update(delta);
        filmGrainEffect.update(delta);
        bloomEffect.update(delta);
        vignettingEffect.update(delta);
        motionBlurEffect.update(delta);
        chromaticAberrationEffect.update(delta);
    }

    private void updateLight(float delta)
    {
        if (lightOn)
        {
            pulsationTime += delta;
            float pulsationPeriod = 2.0f;
            float intensity = (float)Math.sin(2 * Math.PI * pulsationTime / pulsationPeriod);
            intensity = ((intensity + 1) / 2) * 10 + 20;
            spotLight.intensity = intensity;
        }
        else
        {
            spotLight.intensity = 0;
        }
    }

    private void addHeadBob(float deltaTime, float speed) {
        if ( speed > 0.1f ) {
        bobAngle += (float) (speed * deltaTime * Math.PI / Settings.headBobDuration);
        // move the head up and down in a sine wave f(x) = sin(x * 0.001 * pi / 0.4)
        }
        else {
            bobAngle += (float) (0.5f * deltaTime * Math.PI / Settings.headBobDuration);
            // move the head up and down in a sine wave
        }
        cam.position.y += bobScale * Settings.headBobHeight * (float)Math.sin(bobAngle);
    }

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
        motionBlurEffect.dispose();
        chromaticAberrationEffect.dispose();

        gui.dispose();
        cursor.dispose();
    }
}
