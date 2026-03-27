package ui;

import engine.InputManager;
import main.Game;
import entities.Player;
import hud.HUD;
import story.StoryState;
import world.GameWorld;
import engine.Camera;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GameScreen extends Screen {

    private Game game;
    private GameWorld world;
    private Player player;
    private Camera camera;

    // נשאיר אותם כמשתנים פשוטים, נאתחל אותם בבנאי
    private int worldWidth;
    private int worldHeight;
    private final int screenWidth = 1280;
    private final int screenHeight = 720;

    public GameScreen(Game game, InputManager input) {
        super(input);
        this.game = game;

        this.player = new Player(20 * 64,52 * 64); //תחילת משחק
        //this.player = new Player(17 * 64,5 * 64); //חדר אוכל
        //this.player = new Player(5 * 64,32 * 64); //מול בית מדרש

        this.world = new GameWorld(player);
        this.camera = new Camera(screenWidth, screenHeight);
        this.worldWidth = world.getMap().layout[0].length * 64;
        this.worldHeight = world.getMap().layout.length * 64;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        // עדכון עולם
        world.update(deltaTime, input, this);

        // עדכון מצלמה עם הערכים שחושבו פעם אחת בבנאי
        camera.update(player, worldWidth, worldHeight);

        world.getHUD().handleInput(input);
        world.getHUD().update(deltaTime);

        if (world.getStoryManager().getState() == StoryState.GAME_OVER) {
            // שולפים את המספר ששמרנו (למשל 1)
            int reason = world.getStoryManager().getFailReason();

            // מחליפים את כל המסך של המשחק במסך הפסילה, ומעבירים לו את המספר
            game.setScreen(new GameOverScreen(game, input, reason));
        }
        else if (world.getStoryManager().getState() == StoryState.VICTORY) {
            // השחקן ניצח! מעבירים אותו למסך הניצחון
            game.setScreen(new victoryScreen(game, input));
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform();

        // תרגום המצלמה
        g.translate(-camera.getX(), -camera.getY());
        world.render(g, camera);

        g.setTransform(oldTransform);

        world.getHUD().render(g);

        float alpha = world.getFadeAlpha();
        if (alpha > 0) {
            g.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
            g.fillRect(0, 0,screenWidth,screenHeight);
        }
    }

    @Override
    public void handleInput(InputManager input) {
        // לוגיקה נוספת אם צריך
    }
}