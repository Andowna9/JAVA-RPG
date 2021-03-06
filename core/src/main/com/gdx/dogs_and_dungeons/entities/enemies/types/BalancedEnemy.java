package com.gdx.dogs_and_dungeons.entities.enemies.types;

import com.badlogic.gdx.Gdx;
import com.gdx.dogs_and_dungeons.entities.enemies.Enemy;
import com.gdx.dogs_and_dungeons.managers.SpriteManager;

public class BalancedEnemy extends Enemy {

    private static final String TAG = BalancedEnemy.class.getSimpleName();

    private float timer = 0;

    public BalancedEnemy(String subtype) {

        super(subtype);

        type = Type.BALANCED;
    }

    @Override
    public void initEntity() {

        setHealth(4);

        setVelocity(2f, 2f);

        setState(State.WALKING);

        setDropCount(2);
    }

    @Override
    public void behave(float delta) {


        restringeField(3);

        timer += delta;

        if (timer >= 3) {

            Gdx.app.debug(TAG, "Cambio de direccion despues de: " + String.valueOf(timer) + " s "  +  currentPosition.y + currentPosition.x);

            timer = 0;

            setDirection(getRandomDirection());
        }

        if (SpriteManager.mapManager.isCollidingWithMap(this)) {

            setDirection(getOppositeDirection());
        }

    }

    public void restringeField(int i){

        float verticalLine1 = initialPosition.x - i;
        float verticalLine2 = initialPosition.x + i;
        float horizontalLine1 = initialPosition.y - i;
        float horizontalLine2 = initialPosition.y + i;

        if (currentPosition.x <= verticalLine1 || currentPosition.x >= verticalLine2){
            this.setDirection(this.getOppositeDirection());
        }
        if (currentPosition.y <= horizontalLine1 || currentPosition.y >= horizontalLine2){
            this.setDirection(this.getOppositeDirection());
        }
    }
}
