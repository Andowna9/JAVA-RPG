package com.gdx.dogs_and_dungeons.managers;
import com.badlogic.gdx.Gdx;
import com.gdx.dogs_and_dungeons.DogsAndDungeons;
import com.gdx.dogs_and_dungeons.screens.MainGameScreen;

 // Clase para gestionar los distintos estados del juego

public class GameStateManager {

    public enum GameState {
        PLAYING,
        GAME_OVER
    }

    private GameState currentGameState;

    private SpriteManager spriteManager;

    private MainGameScreen gameScreen;


    public GameStateManager(MainGameScreen gameScreen, SpriteManager spriteManager) {

        this.spriteManager = spriteManager;

        this.gameScreen = gameScreen;

        // El estado por defecto es jugando

        currentGameState = GameState.PLAYING;

    }

    public void setCurrentGameState(GameState g) {

        currentGameState = g;
    }

    private void changeState() {

        if ( currentGameState == GameState.PLAYING && spriteManager.player.isDead()) {

            Gdx.input.setInputProcessor(null);

            setCurrentGameState(GameState.GAME_OVER);
        }


    }

    // De momento solo tenemos un estado, pero en caso de tener más, podríamos crear clases estado
    // y guardar instancias en una lista para que el estadp se actualice de forma más dinámica

    public void updateState(float delta) {

        changeState();

        switch (currentGameState) {

            case PLAYING:

                spriteManager.update(delta);

                break;

            case GAME_OVER:

                spriteManager.updateEnemies(delta);

                gameScreen.switchScreen(DogsAndDungeons.gameOverScreen,delta);

                break;
        }


    }
}
