package com.duoc.race.model;

import com.badlogic.gdx.graphics.Texture;

public class AutoJugador extends Juego{


    public AutoJugador(float x, float y, Texture texture) {
        super(x, y, texture);
        this.width = 60;
        this.height = 100;
    }

    @Override
    public void update(float delta) {
        // TODO el movimiento lo controlamos desde el Main
    }

    public void setX(float x){
        this.x = x;
    }

    public void setY(float y){
        this.y = y;
    }

}
