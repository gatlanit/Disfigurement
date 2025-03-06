package com.niting.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.niting.main.mainMenu.EndScreen;
import com.niting.main.mainMenu.MenuScreen;

public class Main extends Game {
    public static Assets assets;

    @Override
    public void create() {
        Gdx.app.log("Main", "create()");
        assets = new Assets();
        assets.finishLoading();
        setScreen(new MenuScreen(this));
    }

    @Override
    public void dispose() {
        Gdx.app.log("Main", "dispose()");
        assets.dispose();
        super.dispose();
    }
}
