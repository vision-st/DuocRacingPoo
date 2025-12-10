package com.duoc.race.model;

import com.badlogic.gdx.graphics.Texture;
import com.duoc.race.interfaces.Chocable;

public class Nube extends Juego {

    private static final float CLOUD_SPEED = 120f;

    public Nube(float x, float y, Texture texture) {
        super(x, y, texture);
    }

    @Override
    public void update(float delta) {
        y -= CLOUD_SPEED * delta;
    }


}
