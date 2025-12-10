package com.duoc.race.model;

import com.badlogic.gdx.graphics.Texture;
import com.duoc.race.interfaces.Chocable;

public class Barrera extends Juego implements Chocable {


    public Barrera(float x, float y, Texture texture) {
        super(x, y, texture);
        this.width = 60;
        this.height = 100;
    }

    @Override
    public void update(float delta) {
        y -= 500 * delta;
    }

    @Override
    public void chocoEnLaCarrera() {
        System.out.println("!! PAFFF !! Choco contra un vehiculo");
    }
}
