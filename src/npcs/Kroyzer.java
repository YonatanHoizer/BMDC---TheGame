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
    private final float CHASE_SPEED = 300.0f;

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
                "/images/קרוייזר קדימה.png",
                "/images/קרוייזר אחורה.png",
                "/images/קרוייזר צד א.png",
                "/images/קרוייזר צד ב.png",
                "/images/קרוייזר צד ג.png",
                "/images/קרוייזר צד ד.png"
        };

        try {
            // טעינת הגיליונות הראשיים (קדימה ואחורה)
            BufferedImage frontSheet = ImageIO.read(getClass().getResourceAsStream(paths[0]));
            BufferedImage backSheet  = ImageIO.read(getClass().getResourceAsStream(paths[1]));

            // טעינת התמונות הבודדות של הצדדים
            BufferedImage sideRight1 = ImageIO.read(getClass().getResourceAsStream(paths[2]));
            BufferedImage sideRight2 = ImageIO.read(getClass().getResourceAsStream(paths[3]));
            BufferedImage sideLeft1  = ImageIO.read(getClass().getResourceAsStream(paths[4]));
            BufferedImage sideLeft2  = ImageIO.read(getClass().getResourceAsStream(paths[5]));

            // --- חיתוך ושיבוץ האנימציות ---

            // קדימה: בגלל שמחקת את ה-64 פיקסלים הריקים, אנחנו מתחילים לחתוך מ-0 ולא מ-64!
            walkDown = new BufferedImage[] {
                    frontSheet.getSubimage(0, 0, 64, 64),
                    frontSheet.getSubimage(0, 64, 64, 64)
            };

            // אחורה נשאר אותו דבר
            walkUp = new BufferedImage[] {
                    backSheet.getSubimage(0, 0, 64, 64),
                    backSheet.getSubimage(0, 64, 64, 64)
            };

            // צדדים: מכניסים את התמונות הבודדות ישירות למערך בלי לחתוך
            walkRight = new BufferedImage[] { sideRight1, sideRight2 };
            walkLeft  = new BufferedImage[] { sideLeft1, sideLeft2 };

            // קביעת הכיוון והספראייט הראשוני בהתאם למיקום התחלתי
            switch (position) {
                case 1:
                    this.direction = "DOWN";
                    this.sprite = walkDown[0];
                    break;
                case 2:
                    this.direction = "LEFT";
                    this.sprite = walkLeft[0];
                    break;
                case 3:
                    this.direction = "RIGHT";
                    this.sprite = walkRight[0];
                    break;
                case 4:
                    this.direction = "UP";
                    this.sprite = walkUp[0];
                    break;
            }

        } catch (Exception e) {
            System.out.println("Error loading Sanans textures: " + e.getMessage());
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