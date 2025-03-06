package com.niting.main;

public class GameObjectType {
    public final static GameObjectType TYPE_STATIC = new GameObjectType("static", true, false, false, false, false, false, false);
    public final static GameObjectType TYPE_GATE = new GameObjectType("static", true, false, false, false, false, true, false);
    public final static GameObjectType TYPE_GENERATOR = new GameObjectType("generator", true, false, false, false,true, false, false);
    public final static GameObjectType TYPE_PLAYER = new GameObjectType("player", false, true, false, false, false, false, false);
//    public final static GameObjectType TYPE_LIGHTER = new GameObjectType("lighter", false, false, true, false);
    public final static GameObjectType TYPE_DYNAMIC = new GameObjectType("dynamic", false, false, false, false, false, false, false);
    public final static GameObjectType TYPE_ENEMY = new GameObjectType("enemy", false, false, false, true, false, false, false);
    public final static GameObjectType TYPE_NAVMESH = new GameObjectType("NAVMESH", true, false, false, false,false, false, true);


    public String typeName;
    public boolean isStatic;
    public boolean isPlayer;
    public boolean canPickup;
    public boolean isEnemy;
    public boolean isGenerator;
    public boolean isGate;
    public boolean isNavMesh;

    public GameObjectType(String typeName, boolean isStatic, boolean isPlayer, boolean canPickup, boolean isEnemy, boolean isGenerator, boolean isGate, boolean isNavMesh) {
        this.typeName = typeName;
        this.isStatic = isStatic;
        this.isPlayer = isPlayer;
        this.canPickup = canPickup;
        this.isEnemy = isEnemy;
        this.isGenerator = isGenerator;
        this.isGate = isGate;
        this.isNavMesh = isNavMesh;
    }
}
