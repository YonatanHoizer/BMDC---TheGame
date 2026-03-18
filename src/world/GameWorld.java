package world;

import engine.AudioManager;
import engine.Camera;
import engine.InputManager;
import entities.NPC;
import entities.Player;
import hud.HUD;
import story.GameState;
import story.StoryManager;
import story.StoryState;
import ui.GameScreen;
import util.Rect;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class GameWorld {

    private double currentDeltaTime;
    private final int TILE_SIZE = 64;
    private float fadeAlpha = 0f;

    private Map map;
    private Player player;
    private List<NPC> npcs;
    private InputManager currentInput;
    public AudioManager audio = new AudioManager();

    // ===== Story =====
    private StoryManager storyManager;
    private GameState activeState;

    // ===== HUD =====
    private HUD hud;

    public GameWorld(Player player) {
        this.map = new Map();
        this.player = player;
        this.npcs = new ArrayList<>();

        this.audio.loadSound("notification", "/sounds/notification2.wav");
        this.player.setAudioManager(this.audio);

        // אתחול HUD
        this.hud = new HUD(player);

        // אתחול StoryManager והסצנה הראשונה
        storyManager = new StoryManager(this);
        activeState = storyManager.startStory(StoryState.LUNCH);
    }

    // ================= UPDATE =================
    public void update(double deltaTime, InputManager input, GameScreen gameScreen) {
        this.currentDeltaTime = deltaTime;
        this.currentInput = input;

        // 1. Player
        player.update(input, gameScreen);
        player.move(deltaTime, this);

        // 2. NPCs
        for (NPC npc : npcs) {
            npc.update(this);
        }
        //עידכון השלבים
        if (storyManager != null) {
            storyManager.update(deltaTime);
        }

        //System.out.println("x = " + ((int)player.getX())/64 + " ,y = " + ((int)player.getY())/64);
    }

    // ================= MOVEMENT / COLLISION =================
    public boolean canMoveTo(Rect hitbox) {
        int maxWorldX = map.layout[0].length * TILE_SIZE;
        int maxWorldY = map.layout.length * TILE_SIZE;

        if (hitbox.getLeft() < 0 || hitbox.getRight() > maxWorldX ||
                hitbox.getTop() < 0 || hitbox.getBottom() > maxWorldY) {
            return false;
        }

        float padding = 8;

        return isWalkable(hitbox.getLeft() + padding, hitbox.getTop() + (hitbox.size.y / 2)) &&
                isWalkable(hitbox.getRight() - padding, hitbox.getTop() + (hitbox.size.y / 2)) &&
                isWalkable(hitbox.getLeft() + padding, hitbox.getBottom()) &&
                isWalkable(hitbox.getRight() - padding, hitbox.getBottom());
    }

    private boolean isWalkable(float worldX, float worldY) {
        int col = (int) (worldX / TILE_SIZE);
        int row = (int) (worldY / TILE_SIZE);

        if (row < 0 || row >= map.layout.length || col < 0 || col >= map.layout[0].length) {
            return false;
        }

        int tileIndex = map.layout[row][col];
        return map.tiles[tileIndex].isWalkable();
    }

    // ================= RENDER =================
    public void render(Graphics2D g, Camera camera) {
        map.draw(g, camera.getX(), camera.getY());

        if (getStoryManager() != null) {
            getStoryManager().render(g);
        }

        for (NPC npc : npcs) {
            npc.Render(g);
        }

        player.Render(g);
    }

    public float getFadeAlpha() { return fadeAlpha; }

    public void setFadeAlpha(float alpha) {
        // הגבלה שהערך תמיד יהיה בין 0 ל-1
        this.fadeAlpha = Math.max(0, Math.min(1, alpha));
    }

    // ================= API FOR STORY =================
    public void addNPC(NPC npc) {
        npcs.add(npc);
    }

    public void removeNPC(NPC npc) {
        npcs.remove(npc);
    }

    public List<NPC> getNPCs() {
        return npcs;
    }

    public Player getPlayer() {
        return player;
    }

    public Map getMap() {
        return map;
    }

    public double getDeltaTime() {
        return currentDeltaTime;
    }

    public StoryManager getStoryManager() {
        return storyManager;
    }

    public GameState getActiveState() {
        return activeState;
    }

    public void setActiveState(GameState state) {
        this.activeState = state;
    }

    public HUD getHUD() {
        return hud;
    }
    public void setHUD(HUD hud) {
        this.hud = hud;
    }

    public InputManager getInput() {
        return currentInput;
    }
}