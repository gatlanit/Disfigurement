package com.niting.main;

import com.badlogic.gdx.*;
import com.badlogic.gdx.math.Vector3;
import com.niting.main.mainMenu.EndScreen;
import com.niting.main.pathfinding.NavMeshView;
import com.niting.main.physics.CollisionShapeType;
import com.niting.main.views.GameView;
import com.niting.main.views.GridView;
import com.niting.main.physics.PhysicsView;

public class GameScreen extends ScreenAdapter {
    InputMultiplexer im;

    private GameView gameView;
    private World world;

    protected Game game;

    public boolean cursorHide = true;

    public GameScreen(Game game)
    {
        this.game = game;
    }

    @Override
    public void show() {
        world = new World();
        Populator.populate(world, world.getSceneAsset());
        gameView = new GameView(world, 1f, this);

        im = new InputMultiplexer();
        Gdx.input.setInputProcessor(im);
        im.addProcessor(gameView.getCameraController());
        im.addProcessor(world.getPlayerController());
        im.addProcessor(gameView.gui.stage);

        // hide the mouse cursor and fix it to screen centre, so it doesn't go out the window canvas
        Gdx.input.setCursorCatched(cursorHide);
        Gdx.input.setCursorPosition(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
    }

    @Override
    public void render(float delta) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE))
        {
            world.mainMenu = !world.mainMenu;
            cursorHide = !cursorHide;
            Gdx.input.setCursorCatched(cursorHide);
        }

        world.update(delta);

        float moveSpeed = world.getPlayer().body.getVelocity().len();
        gameView.render(delta, moveSpeed);

        if (Settings.ENDED) {
            world.getPlayerController().stopSounds();
            game.setScreen(new EndScreen(game));
        }
    }

    @Override
    public void resize(int width, int height) {
        gameView.resize(width, height);
    }


    @Override
    public void hide() {
        dispose();
    }

    @Override
    public void dispose() {
        gameView.dispose();
        world.dispose();
    }
}
