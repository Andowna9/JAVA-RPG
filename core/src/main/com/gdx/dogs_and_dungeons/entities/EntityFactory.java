package com.gdx.dogs_and_dungeons.entities;

import com.gdx.dogs_and_dungeons.entities.enemies.BalancedEnemy;
import com.gdx.dogs_and_dungeons.entities.enemies.Enemy;
import com.gdx.dogs_and_dungeons.entities.enemies.HardEnemy;
import com.gdx.dogs_and_dungeons.entities.enemies.SimpleEnemy;
import com.gdx.dogs_and_dungeons.entities.player.Player;

public class EntityFactory {

    public static Player getPlayer() {

        return new Player(64,64,1.3f,1.3f);
    }

    public static Enemy getEnemy(String type) {

        switch (type) {


            case "Simple":

                return new SimpleEnemy(32,32, 1f, 1f, Entity.Direction.RIGHT);

            case "Balanced":

                return new BalancedEnemy(32, 32, 1f, 1f);


            case "Hard":

                return new HardEnemy(32, 32, 1f, 1f);
        }

        return null;
    }
}