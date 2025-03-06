package com.niting.main;

public class Settings {
    static public float FOV = 67;

    static public float eyeHeight = 1.5f;   // meters

    static public float walkSpeed = 11.5f;    // m/s
    static public float jumpForce = 8f;
    static public float runFactor = 1.5f;    // m/s
    static public float turnSpeed = 120f;   // degrees/s
    static public float groundRayLength = 1.2f;

    static public boolean invertLook = false;
    static public float headBobDuration = 0.4f; // s
    static public float headBobHeight = 0.04f;  // m
    static public boolean freeLook = true;
    static public float degreesPerPixel = 0.1f; // mouse sensitivity

    static public float gravity = -6.8f; // meters / s^2

    static public float playerMass = 1.0f;
    static public float playerLinearDamping = 0.05f;
    static public float playerAngularDamping = 0.5f;

    static public float navHeight = 1.6f;       // should be about half the height of the characters
    static public boolean isChasing = false;

    static public boolean ENDED = false;

    static public final String GLTF_FILE = "models/tilemap.gltf";
    static public boolean lightOn;
}
