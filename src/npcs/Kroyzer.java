package npcs;

import ai.MovementAI;
import ai.ScriptedMovementAI;
import entities.NPC;
import entities.Player;
import world.GameWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Kroyzer extends NPC {

    private boolean active = true;

    private Player chaseTarget; // שומר את השחקן כדי לדעת את מי לרדוף אחר כך
    private boolean isChasing = false; // דגל שבודק אם אנחנו במצב מרדף כלשהו

    // טווחים ומהירויות של קרויזר
    private final float CATCH_RANGE = 60.0f;
    private final float NORMAL_SPEED = 100.0f;
    private final float CHASE_SPEED = 280.0f;

    public Kroyzer(float x, float y, int width, int height) {
        // צבע 0, מיקום התחלתי 1 (למטה)
        super(x, y, width, height, 0, 1);
        this.speed = NORMAL_SPEED;
    }

    @Override
    protected void loadAnimations() {
        loadKroizerAnimationsByIndex(firstPosition);
    }

    private void loadKroizerAnimationsByIndex(int position) {
        // שים לב לעדכן את הנתיבים בהתאם לתמונות האמיתיות של קרויזר
        String[] paths = {
                "/images/שחור קדימה.png",
                "/images/שחור אחורה.png",
                "/images/שחור צד.png"
        };

        try {
            BufferedImage frontSheet = ImageIO.read(getClass().getResourceAsStream(paths[0]));
            BufferedImage backSheet  = ImageIO.read(getClass().getResourceAsStream(paths[1]));
            BufferedImage sideSheet  = ImageIO.read(getClass().getResourceAsStream(paths[2]));

            walkDown  = new BufferedImage[] { frontSheet.getSubimage(0, 64, 64, 64), frontSheet.getSubimage(0, 128, 64, 64) };
            walkUp    = new BufferedImage[] { backSheet.getSubimage(0, 0, 64, 64), backSheet.getSubimage(0, 64, 64, 64) };
            walkRight = new BufferedImage[] { sideSheet.getSubimage(0, 0, 64, 64), sideSheet.getSubimage(64, 0, 64, 64) };
            walkLeft  = new BufferedImage[] { sideSheet.getSubimage(0, 64, 64, 64), sideSheet.getSubimage(64, 64, 64, 64) };

            switch (position) {
                case 1 -> this.sprite = walkDown[0];
                case 2 -> this.sprite = walkLeft[0];
                case 3 -> this.sprite = walkRight[0];
                case 4 -> this.sprite = walkUp[0];
            }

        } catch (Exception e) {
            System.out.println("Error loading Kroizer textures: " + e.getMessage());
        }
    }

    @Override
    public void update(GameWorld world) {
        if (!active) return;
        super.update(world);

        // --- הלוגיקה החדשה של מעבר בין יציאה מהכיתה למרדף ---
        if (isChasing && chaseTarget != null) {
            // בודקים אם כרגע מופעל עליו ה-AI המתוסרט (היציאה מהכיתה)
            if (this.movementAI instanceof ScriptedMovementAI) {
                ScriptedMovementAI script = (ScriptedMovementAI) this.movementAI;

                // ברגע שהוא מסיים להגיע לדלת הכיתה...
                if (script.isFinished()) {
                    // משחררים את הרסן! מחליפים ל-AI של מרדף אחרי השחקן
                    this.setMovementAI(new ai.ChaseAI(chaseTarget));
                }
            }
        }
    }

    public void startChase(Player player) {
        this.chaseTarget = player; // שומרים את השחקן בזיכרון
        this.isChasing = true;

        this.setAlert(true);
        this.setSpeed(CHASE_SPEED);

        // במקום לרדוף ישר, נותנים לו קודם את הפקודה לצאת מהכיתה!
        this.setMovementAI(ScriptedMovementAI.KroyzerGetOutOfClassAI());
    }

    public boolean hasCaughtPlayer(Player player) {
        return this.getDistanceSquared(player) <= (CATCH_RANGE * CATCH_RANGE);
    }

    public void stopChase() {
        this.isChasing = false;
        this.chaseTarget = null; // מוחקים את המטרה מהזיכרון
        this.setAlert(false);
        this.stop();
        this.setSpeed(NORMAL_SPEED);
        this.setMovementAI(ScriptedMovementAI.KroyzerGetOutOfClassAI());
    }

    public void deactivate() {
        active = false;
    }
}