package world;

import engine.AudioManager;
import engine.Camera;
import engine.InputManager;
import entities.Entity;
import entities.NPC;
import entities.Player;
import hud.HUD;
import hud.InteractiveDialogueBox;
import npcs.Sanans;
import story.GameState;
import story.StoryManager;
import story.StoryState;
import ui.GameScreen;
import util.Rect;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class GameWorld {

    private double currentDeltaTime;
    private final int TILE_SIZE = 64;
    private float fadeAlpha = 0f;

    private Map map;
    private Player player;
    private List<NPC> npcs;
    private List<Entity> entities;
    private InputManager currentInput;
    private NPC creator;
    public AudioManager audio = new AudioManager();

    // ===== Story =====
    private StoryManager storyManager;
    private GameState activeState;
    private Sanans sanans;

    // ===== HUD =====
    private HUD hud;

    private Entity Zoro;
    private Entity CanVendingMachine;
    private Entity SnackVendingMachine;
    private boolean isWorldDialogueActive = false;
    private boolean isTakedToCreator = false;

    public GameWorld(Player player) {
        this.map = new Map();
        this.player = player;
        this.entities = new ArrayList<>();
        this.npcs = new ArrayList<>();

        this.audio.loadSound("notification", "/sounds/notification2.wav");
        this.audio.loadSound("צעדים","/sounds/צעדים.wav");
        this.audio.setVolume("צעדים",0.8F);
        this.player.setAudioManager(this.audio);

        this.audio.loadSound("תיקתוק","/sounds/תיקתוק.wav");
        this.audio.setVolume("תיקתוק", 0.8F);

        // אתחול HUD
        this.hud = new HUD(player);

        // אתחול StoryManager והסצנה הראשונה
        storyManager = new StoryManager(this);
        activeState = storyManager.startStory(StoryState.DORMITORY);

        try {
            BufferedImage canImg = ImageIO.read(getClass().getResourceAsStream("/images/can machine.png"));
            // עכשיו מעבירים את התמונה לבנאי
            CanVendingMachine = new Entity(49 * 64 - 30, 36 * 64, 96, 128, canImg);
            entities.add(CanVendingMachine);
        } catch (Exception e) {
            System.out.println("Error loading can machine image!");
        }

        try {
            BufferedImage snackImg = ImageIO.read(getClass().getResourceAsStream("/images/snack machine.png"));
            // עכשיו מעבירים את התמונה לבנאי
            SnackVendingMachine = new Entity(47 * 64 + 3, 36 * 64, 96, 128, snackImg);
            entities.add(SnackVendingMachine);
        } catch (Exception e) {
            System.out.println("Error loading snack machine image!");
        }

        try {
            BufferedImage zoroImg = ImageIO.read(getClass().getResourceAsStream("/images/zoro.png"));
            // עכשיו מעבירים את התמונה לבנאי
            Zoro = new Entity(50 * 64,1 * 64, 64, 64, zoroImg);
            entities.add(Zoro);
        } catch (Exception e) {
            System.out.println("Error loading zoro image!");
        }

        creator = new NPC(43 * 64,54 * 64,64,64,6,4);
        this.addNPC(creator);
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

        handleStaticNpcDialogues(player);
        if (hud.shouldPlayTimerTick()) {
            this.audio.play("תיקתוק");
        }

        //עידכון השלבים
        if (storyManager != null) {
            storyManager.update(deltaTime);
        }

        //System.out.println("x = " + ((int)player.getX())/64 + " ,y = " + ((int)player.getY())/64);
    }

    private void handleStaticNpcDialogues(Player player) {
        InteractiveDialogueBox dBox = this.getHUD().getDialogueBox();

        // 1. קודם כל, נבדוק אם יש דיאלוג פתוח כלשהו
        // (שים לב: צריך להיות לך גטר בשחקן שאומר אם הוא בשיחה)
        if (player.isInDialogue()) {
            // אם אנחנו (העולם) פתחנו את הדיאלוג, והתיבה נסגרה - נשחרר את השחקן
            if (isWorldDialogueActive && !dBox.isVisible()) {
                player.setInDialogue(false);
                isWorldDialogueActive = false;
            }
            // בכל מקרה, אם השחקן בדיאלוג (שלנו או של העלילה), לא נאפשר דיאלוג חדש!
            return;
        }

        // 2. רק אם השחקן פנוי, נבדוק אינטראקציות לפי סדר (else if מונע כפילויות)
        if (player.getDistanceSquared(Zoro) < (64 * 64)) {
            if (this.getInput().Z_key && dBox.isReady()) {
                startWorldDialogue(player, dBox, List.of("אני חושב שהלכתי לאיבוד..."));
            }
        }
        else if (player.getDistanceSquared(CanVendingMachine) < (96 * 96)) {
            if (this.getInput().Z_key && dBox.isReady()) {
                List<String> lines = new ArrayList<>();
                lines.add("הכנסת 6 שקל בשביל פחית קולה..");
                lines.add("המכונה בלעה אותם, העיקר כתוב 'עין רואה' וכו'");
                startWorldDialogue(player, dBox, lines);
            }
        }
        else if (player.getDistanceSquared(SnackVendingMachine) < (96 * 96)) {
            if (this.getInput().Z_key && dBox.isReady()) {
                startWorldDialogue(player, dBox, List.of("אתה מחליט לא לנסות את מזלך שנית."));
            }
        }
        else if (player.getDistanceSquared(creator) < (64 * 64)) {
            if (this.getInput().Z_key && dBox.isReady()) {
                if (!isTakedToCreator){
                    this.getHUD().showTopMessage("נראה שהוא בונה משחק על הישיבה ,עדיף לתת לו לסיים בשקט",8.0);
                    isTakedToCreator = true;
                }
            }
        }
    }

    // פונקציית עזר קטנה שעושה סדר וחוסכת שכפול קוד:
    private void startWorldDialogue(Player player, InteractiveDialogueBox dBox, List<String> text) {
        player.setInDialogue(true);
        isWorldDialogueActive = true;
        dBox.startDialogue(text);
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
        creator.Render(g);

        for (Entity entity : entities) {
            entity.Render(g);
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