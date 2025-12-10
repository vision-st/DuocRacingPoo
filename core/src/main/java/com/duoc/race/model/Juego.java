package com.duoc.race.model;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;


public abstract class Juego {

    protected float x;
    protected float y;
    protected float width;
    protected float height;
    public Texture texture;

    public Juego(float x, float y, Texture texture){
        this.x =x;
        this.y = y;
        this.texture = texture;
        this.width = texture.getWidth();
        this.height = texture.getHeight();
    }

    public abstract void update(float delta);

    public Rectangle getBounds(){
        return new Rectangle(x + 5, y +5, width -10, height -10);
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }
}
