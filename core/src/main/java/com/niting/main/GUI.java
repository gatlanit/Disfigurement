package com.niting.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GUI implements Disposable {

    // Tables
    private Table menuTable;
    private Table staminaTable;

    public Stage stage;
    private Skin skin;
    private World world;

    // UI Elements
    private Image cursorImg;
    private Label infoLabel;

    private TextButton play;
    private TextButton resume;
    private TextButton quit;

    private ProgressBar progressBar;
    private Label staminaLabel;
    private GameScreen screen;

    public GUI(World world, GameScreen screen) {
        this.screen = screen;
        this.world = world;
        stage = new Stage(new ScreenViewport());
        skin = Main.assets.skin;
        cursorImg = new Image(new Texture(Gdx.files.internal("images/cursor.png")));
        cursorImg.setSize(10, 10); // Set size as needed
    }

    private void rebuild() {
        stage.clear();

        // Cursor
        cursorImg.setPosition((Gdx.graphics.getWidth() - cursorImg.getWidth()) / 2f, (Gdx.graphics.getHeight() - cursorImg.getHeight()) / 2f);
        infoLabel = new Label("PRESS E", this.skin);
        infoLabel.setPosition(Gdx.graphics.getWidth()/ 2f - 60, Gdx.graphics.getHeight() / 2f - 60);
        stage.addActor(infoLabel);
        stage.addActor(cursorImg);

        // Menu
        menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.pad(40);
        menuTable.padTop(250);
        menuTable.defaults().space(50);

        TextButton resume = new TextButton("RESUME", this.skin);
        menuTable.add(resume).pad(10);

        menuTable.row();
        TextButton quit = new TextButton("QUIT", this.skin);
        menuTable.add(quit).pad(10);

        menuTable.pack();
        stage.addActor(menuTable);

        // Bottom row
        staminaTable = new Table();
        staminaTable.setFillParent(true);
        staminaTable.pad(40);
        staminaTable.defaults().space(50);

        staminaTable.bottom();
        staminaTable.row();
        staminaLabel = new Label("STAMINA", this.skin);
        staminaTable.add(staminaLabel);

        progressBar = new ProgressBar(0f, 5f, 0.01f, false, this.skin);
        progressBar.getStyle().background.setMinHeight(45f);
        progressBar.getStyle().knob.setMinHeight(45f);
        staminaTable.add(progressBar).size(475, 45);

        staminaTable.pack();
        stage.addActor(staminaTable);

        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.exit();
            }
        });

        resume.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                world.mainMenu = !world.mainMenu;
                screen.cursorHide = !screen.cursorHide;
                Gdx.input.setCursorCatched(screen.cursorHide);
            }
        });
    }

    public void render(float deltaTime) {
        updateLabels();
        stage.act(deltaTime);
        stage.draw();
    }

    private void updateLabels() {
        infoLabel.setVisible(world.getPlayerController().foundGenerator);

        if (world.mainMenu)
        {
            menuTable.setVisible(true);
            cursorImg.setVisible(false);
            infoLabel.setVisible(false);
        }
        else{
            menuTable.setVisible(false);
            cursorImg.setVisible(true);
        }

        if (world.getPlayerController().stamina >= 5)
        {
            progressBar.setVisible(false);
            staminaLabel.setVisible(false);
        }
        else{
            progressBar.setVisible(true);
            staminaLabel.setVisible(true);
        }

        progressBar.setValue(world.getPlayerController().stamina);
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        rebuild();
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
