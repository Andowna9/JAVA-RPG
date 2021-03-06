package com.gdx.dogs_and_dungeons.managers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.gdx.dogs_and_dungeons.DogsAndDungeons;
import com.gdx.dogs_and_dungeons.entities.enemies.EnemyProperties;
import com.gdx.dogs_and_dungeons.objects.Item;
import com.gdx.dogs_and_dungeons.entities.Entity;
import com.gdx.dogs_and_dungeons.entities.EntityFactory;
import com.gdx.dogs_and_dungeons.entities.enemies.Enemy;
import com.gdx.dogs_and_dungeons.entities.npcs.NPC;
import com.gdx.dogs_and_dungeons.entities.npcs.types.Villager;
import com.gdx.dogs_and_dungeons.entities.player.Player;
import com.gdx.dogs_and_dungeons.entities.player.PlayerController;
import com.gdx.dogs_and_dungeons.entities.player.hud.PlayerHUD;
import com.gdx.dogs_and_dungeons.profiles.ProfileManager;
import com.gdx.dogs_and_dungeons.profiles.ProfileObserver;

import java.util.Iterator;

// Clase para gestionar las interacciones entre entidades

public class SpriteManager implements ProfileObserver {

    private static final String TAG = SpriteManager.class.getSimpleName();

    public static Player player;

    private PlayerController playerController;

    // Lista de enemigos

    Array<Enemy> enemies;

    // Lista de NPCs

    Array<NPC> npcs;

    // Gestor de mapa

    public static MapManager mapManager;

    // Gestor de objetos

    ItemManager itemManager;

    // Gestor de efectos de partículas

    public  static ParticleEffectsManager effectsManager;

    // Gestor de audio

    public static AudioManager audioManager;

    // Npc con el que se esá interactuando, solo 1 simultáneamente

    Villager interactingNPC;

    // Orden de renderizado de entidades dependiendo del punto medio del sprite

    Array<Entity> entities;

    // HUD del jugador

    public PlayerHUD playerHUD;

    // Referencia al juego

    public static DogsAndDungeons game_ref;

    DialogManager dialogManager;

    public SpriteManager(DogsAndDungeons game) {

        ProfileManager.getInstance().addObserver(this);

        game_ref = game;

        mapManager = new MapManager();

        // Guardamos la referencia estática a la instancia del map manager para que todas las entidades tengam acceso

        audioManager = new AudioManager();

        itemManager = new ItemManager(mapManager);

        effectsManager = new ParticleEffectsManager();

        dialogManager = new DialogManager();

        // Inicialización de lista de enemigos

        enemies = new Array<>();

        // Inicialización de lista de npcs

        npcs = new Array<>();

        // Creación del jugador

        player = EntityFactory.getPlayer();

        player.setPosition(22.5f,0);

        playerController = new PlayerController(player);

        entities = new Array<>();
    }

    // Inicialización en caso de reanudar la partida (más adelante con puntos de spawn)

    public void init() {

        // Inicialización del jugador

        player.initEntity();


        // Inicialización de NPCs

        npcs.clear();

        mapManager.spawnNPCs(npcs);

        // Inicialización de lista de entidades para renderizado dinámico

        entities.clear();

        entities.add(player);

        entities.addAll(npcs);

        entities.addAll(enemies);

    }

    // Actulización de enemigos

    void updateEnemies(float delta) {

        for (Iterator<Enemy> it = enemies.iterator();it.hasNext();) {

            Enemy e = it.next();

            // Actualización de enemigos

            e.update(delta);

            if (!mapManager.isCollidingWithMap(e)) e.updatePosition();

            e.behave(delta);

            // Si el enemigo ha perdido toda su salud, se elimina de la lista

            if (e.getHealth() <= 0) {

                    effectsManager.generateEffect(e.getCurrentPosition().x + 0.5f, e.getCurrentPosition().y + 0.5f, ParticleEffectsManager.EffectType.ENEMY_DEATH);

                    Gdx.app.debug(TAG, e.getClass().getSimpleName() + " eliminado por jugador!");

                    it.remove();

                    entities.removeValue(e,false);

                    // El enemigo suelta cierta cantidad de madera

                    for (int i = 1; i <= e.getDropCount(); i++) {

                        itemManager.items.add(new Item(e.getCurrentPosition().x, e.getCurrentPosition().y, Item.Type.WOOD));

                    }


                }
            }
        }


    // Actualización jugador

    private void updatePlayer(float delta) {

        // Procesado de input (teclas)

        playerController.processInput();

        // Actulización de animaciones y cajas de colisión

        player.update(delta);

        // Colisiones con el mapa y NPCs

       if (!mapManager.isCollidingWithMap(player)) {

           boolean collidesWithNpc = false;

           for (NPC npc: npcs) {

               if (player.isCollidingWithEntity(npc)) {

                   collidesWithNpc = true;

                   break;
               }
           }


           if (!collidesWithNpc) player.updatePosition();

       }

       // Interacciones npcs

        boolean interactingWithNPC = false;

        for (NPC npc: npcs) {

            if (!(npc instanceof Villager)) continue;

            interactingNPC = (Villager) npc;

            if (player.getCurrentPosition().dst(npc.getCurrentPosition()) < 1) {

                    interactingWithNPC = true;

                    break;

            }

        }

        player.setInteracting(interactingWithNPC);

        if (!interactingWithNPC) interactingNPC = null;

    }

    private void updateNPCs(float delta) {

        for (NPC npc: npcs) {

            npc.update(delta);

            if (!mapManager.isCollidingWithMap(npc)) npc.updatePosition();

            npc.behave(delta);
        }
    }

    // Actualización general

    public void update(float delta) {

       updateEnemies(delta);

        updateNPCs(delta);

       updatePlayer(delta);

       // Interacción entre jugador y objetos

        itemManager.itemsTriggered(player);

        // Interacción entre enemigos y jugador

        for (Enemy enemy: enemies) {

            player.checkAttack(enemy);

            if (player.isCollidingWithEntity(enemy) && !player.isBlinking()) {

                player.receiveDamageFrom(enemy);

                Gdx.app.debug(TAG,"Vida restante de jugador: " + player.getHealth());

                if (!player.isDead()) {

                    player.setBlinking(2f);

                }
            }
        }
    }

    public PlayerController getPlayerController() {

        playerController.reset();

        return playerController ;
    }


    public Player getPlayer() {

        return player;
    }

    // Inicialización de enemigos

    @Override
    public void onNotify(ProfileManager subject, ProfileEvent event) {

        if (event == ProfileEvent.SAVING_PROFILE) {

             Array<EnemyProperties> properties = new Array<>();

            for (Enemy e: enemies) {

                properties.add(e.getProperties());
            }

            subject.setProperty("Enemies Properties", properties);

        }

        else if (event == ProfileEvent.LOADING_PROFILE) {

            enemies.clear();

            Array<EnemyProperties> properties = subject.getProperty("Enemies Properties",Array.class,new Array());

            if (properties.isEmpty()) {

                // Inicialización a partir de información del mapa

                mapManager.spawnEnemies(enemies);
            }

            else {


                for (EnemyProperties p: properties) {

                    Enemy e = EntityFactory.getEnemy(p.type, p.subtype);

                    // Inicialización a partir de propiedades serializadass

                    e.setInitialPosition(p.initialPosition);

                    e.setDirection(p.direction);

                    e.initEntity();

                    enemies.add(e);

                }
            }

        }
    }
}
