package ui;

import engine.InputManager;
import main.Game;
import entities.Player;
import story.StoryState;
import world.GameWorld;
import engine.Camera;
import util.SaveData;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class GameScreen extends Screen {

    private Game game;
    private GameWorld world;
    private Player player;
    private Camera camera;

    private int worldWidth;
    private int worldHeight;
    private final int screenWidth = 1280;
    private final int screenHeight = 720;

    // משתנים למצב עצירה (Pause)
    private boolean isPaused = false;
    private int pauseSelector = 0;
    private boolean escKeyReleased = true;
    private boolean selectionKeyReleased = true;

    // --- בנאי 1: למשחק חדש לחלוטין ---
    public GameScreen(Game game, InputManager input) {
        super(input);
        this.game = game;

        this.player = new Player(20 * 64, 53 * 64); // מיקום ברירת מחדל של תחילת המשחק
        //this.player = new Player(10 * 64, 30 * 64); //בית מדרש
        //this.player = new Player(10 * 64, 8 * 64); //חדר אוכל

        this.world = new GameWorld(player);
        this.camera = new Camera(screenWidth, screenHeight);
        this.worldWidth = world.getMap().layout[0].length * 64;
        this.worldHeight = world.getMap().layout.length * 64;

        this.world.getStoryManager().startStory(StoryState.DORMITORY);
    }

    // --- בנאי 2: לטעינת משחק קיים מהדאטה בייס (הפתרון החדש) ---
    public GameScreen(Game game, InputManager input, SaveData saveData) {
        super(input);
        this.game = game;

        // 1. יצירת השחקן ישירות במיקום המדויק מהשמירה
        this.player = new Player(saveData.playerX, saveData.playerY);

        this.world = new GameWorld(player);
        this.camera = new Camera(screenWidth, screenHeight);
        this.worldWidth = world.getMap().layout[0].length * 64;
        this.worldHeight = world.getMap().layout.length * 64;

        // 2. עדכון המשתנים העלילתיים בתוך ה-StoryManager
        world.getStoryManager().setPlayerHasMilk(saveData.playerHasMilk);

        // 3. הזנקת השלב העלילתי הנכון מהשמירה
        StoryState savedState = StoryState.valueOf(saveData.currentState);
        world.getStoryManager().startStory(savedState);
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        handleInput(input);

        if (isPaused) {
            world.getHUD().update(deltaTime);
            return;
        }

        world.update(deltaTime, input, this);
        camera.update(player, worldWidth, worldHeight);

        world.getHUD().handleInput(input, world);
        world.getHUD().update(deltaTime);

        if (world.getStoryManager().getState() == StoryState.GAME_OVER) {
            int reason = world.getStoryManager().getFailReason();
            game.setScreen(new GameOverScreen(game, input, reason));
        }
        else if (world.getStoryManager().getState() == StoryState.VICTORY) {
            game.setScreen(new victoryScreen(game, input));
        }
    }

    @Override
    public void render(Graphics2D g) {
        AffineTransform oldTransform = g.getTransform();

        // תרגום המצלמה ורינדור העולם והשחקן
        g.translate(-camera.getX(), -camera.getY());
        world.render(g, camera);

        g.setTransform(oldTransform);
        world.getHUD().render(g);

        // אפקט Fade
        float alpha = world.getFadeAlpha();
        if (alpha > 0) {
            g.setColor(new Color(0, 0, 0, (int)(alpha * 255)));
            g.fillRect(0, 0, screenWidth, screenHeight);
        }

        // --- רינדור תפריט העצירה (Pause Menu) ---
        if (isPaused) {
            // 1. הכהיית המסך באפקט חצי שקוף
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, screenWidth, screenHeight);

            // 2. ציור רקע חלון התפריט במרכז המסך (הגדלנו את הגובה ל-310 בשביל הטקסט הנוסף)
            g.setColor(new Color(40, 40, 40));
            g.fillRoundRect(440, 210, 400, 310, 25, 25);

            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(3));
            g.drawRoundRect(440, 210, 400, 310, 25, 25);

            // 3. כתיבת כותרת התפריט
            g.setFont(new Font("Arial", Font.BOLD, 32));
            String title = "המשחק מושהה";
            int titleWidth = g.getFontMetrics().stringWidth(title);
            g.drawString(title, 440 + (400 - titleWidth) / 2, 265);

            // 4. ציור האפשרויות הדינמיות
            g.setFont(new Font("Arial", Font.PLAIN, 22));

            // אופציה 0: המשך משחק
            String opt1 = "המשך משחק";
            if (pauseSelector == 0) {
                g.setColor(Color.YELLOW);
                opt1 = "> " + opt1 + " <";
            } else {
                g.setColor(Color.WHITE);
            }
            int opt1Width = g.getFontMetrics().stringWidth(opt1);
            g.drawString(opt1, 440 + (400 - opt1Width) / 2, 330);

            // אופציה 1: שמור וצא לתפריט
            String opt2 = "שמור וצא לתפריט";
            if (pauseSelector == 1) {
                g.setColor(Color.YELLOW);
                opt2 = "> " + opt2 + " <";
            } else {
                g.setColor(Color.WHITE);
            }
            int opt2Width = g.getFontMetrics().stringWidth(opt2);
            g.drawString(opt2, 440 + (400 - opt2Width) / 2, 380);

            // אופציה 2: יציאה ללא שמירה
            String opt3 = "יציאה ללא שמירה";
            if (pauseSelector == 2) {
                g.setColor(Color.YELLOW);
                opt3 = "> " + opt3 + " <";
            } else {
                g.setColor(Color.WHITE);
            }
            int opt3Width = g.getFontMetrics().stringWidth(opt3);
            g.drawString(opt3, 440 + (400 - opt3Width) / 2, 430);

            // --- התוספת החדשה: טקסט הוראה בתחתית חלון הפאוז ---
            g.setFont(new Font("Arial", Font.PLAIN, 16)); // פונט קטן ועדין יותר
            g.setColor(new Color(180, 190, 200)); // צבע אפור-כסף מעט עמום שלא יציק בעין
            String instruction = "לבחירה לחץ ENTER";
            int instrWidth = g.getFontMetrics().stringWidth(instruction);
            g.drawString(instruction, 440 + (400 - instrWidth) / 2, 490);
        }
    }

    @Override
    public void handleInput(InputManager input) {
        if (input.ESC_key) {
            if (escKeyReleased) {
                isPaused = !isPaused;
                escKeyReleased = false;
                pauseSelector = 0;

                if (isPaused) {
                    player.stop();
                    world.audio.pauseAll();
                } else {
                    world.audio.resumeAll();
                }
            }
        } else {
            escKeyReleased = true;
        }

        if (isPaused) {
            if (input.S_Key) {
                if (selectionKeyReleased) {
                    pauseSelector = (pauseSelector + 1) % 3;
                    selectionKeyReleased = false;
                }
            }
            else if (input.W_key) {
                if (selectionKeyReleased) {
                    pauseSelector = (pauseSelector - 1 + 3) % 3;
                    selectionKeyReleased = false;
                }
            }
            else {
                selectionKeyReleased = true;
            }

            if (input.E_key) {
                if (pauseSelector == 0) {
                    isPaused = false;
                    world.audio.resumeAll();
                }
                else if (pauseSelector == 1) {
                    SaveData save = new SaveData(
                            player.getX(),
                            player.getY(),
                            world.getStoryManager().getState().toString(),
                            world.getStoryManager().isPlayerHasMilk()
                    );

                    world.audio.stopAll();
                    util.DatabaseManager.saveGame(save);
                    game.setScreen(new MainMenuScreen(game, input));
                }
                else if (pauseSelector == 2) {
                    world.audio.stopAll();
                    game.setScreen(new MainMenuScreen(game, input));
                }
            }
        }
    }

    // --- פונקציות גישה (Getters) למקרה שספריות אחרות יצטרכו בעתיד ---
    public Player getPlayer() { return this.player; }
    public GameWorld getWorld() { return this.world; }
}