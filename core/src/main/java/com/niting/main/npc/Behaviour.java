package com.niting.main.npc;

import com.niting.main.GameObject;
import com.niting.main.World;

public class Behaviour {
    protected final GameObject go;

    protected Behaviour(GameObject go) {
        this.go = go;
    }

    public void update(World world, float deltaTime ) { }

    // factory for Behaviour instance depending on object type
    public static Behaviour createBehaviour(GameObject go){
        if(go.type.isEnemy)
            return new CookBehaviour(go);
        return null;
    }
}
