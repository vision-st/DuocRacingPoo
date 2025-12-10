package com.duoc.race.model;

import com.badlogic.gdx.graphics.Texture;
import com.duoc.race.interfaces.Chocable;

public class AutoEnemigo extends Juego implements Chocable {


    public AutoEnemigo(float x, float y, Texture texture) {
        super(x, y, texture);
        this.width = 60;
        this.height = 100;
    }

    @Override
    public void update(float delta) {
        y -= 700 * delta;
    }

    public void setX(float x){
        this.x = x;
    }

    public void setY(float y){
        this.y = y;
    }


    @Override
    public void chocoEnLaCarrera() {
        System.out.println("!! PAFFF !! Choco contra un vehiculo");
    }
}
