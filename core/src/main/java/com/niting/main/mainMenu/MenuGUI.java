package com.niting.main.mainMenu;

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
import com.niting.main.GameScreen;
import com.niting.main.Main;

public class MenuGUI implements Disposable {

    // Tables
    private Table titleTable;
    private Table menuTable;

    public Stage stage;
    private Skin skin;

    private Label title;

    private TextButton play;
    private TextButton quit;

    private MenuScreen screen;

    public MenuGUI(MenuScreen screen) {
        this.screen = screen;
        stage = new Stage(new ScreenViewport());
        skin = Main.assets.menuSkin;
    }

    private void rebuild() {
        stage.clear();

        titleTable = new Table();
        titleTable.setFillParent(true);

        title = new Label("DISFIGUREMENT", this.skin);
        titleTable.add(title).padBottom(400);

        // Menu
        menuTable = new Table();
        menuTable.setFillParent(true);
        menuTable.pad(40);
        menuTable.padTop(250);
        menuTable.defaults().space(50);

        TextButton play = new TextButton("PLAY", this.skin);
        menuTable.add(play).pad(10);

        menuTable.row();
        TextButton quit = new TextButton("QUIT", this.skin);
        menuTable.add(quit).pad(10);

        titleTable.pack();
        menuTable.pack();
        stage.addActor(titleTable);
        stage.addActor(menuTable);

        quit.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                super.clicked(event, x, y);
                Gdx.app.exit();
            }
        });

        play.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                screen.game.setScreen(new GameScreen(screen.game));
            }
        });
    }

    public void render(float deltaTime) {
        stage.act(deltaTime);
        stage.draw();
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
