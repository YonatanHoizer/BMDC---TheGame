package npcs;

import ai.MovementAI;
import entities.NPC;
import entities.Player;
import world.GameWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Miller extends NPC {

    private boolean active = true;
    private MovementAI baseAI; // שומר את הפטרול הרגיל שלו כדי שיוכל לחזור אליו

    // טווחים ומהירויות של מילר
    private final float DETECTION_RANGE = 400.0f;
    private final float CATCH_RANGE = 60.0f;
    private final float NORMAL_SPEED = 150.0f;
    private final float CHASE_SPEED = 280.0f;

    public Miller(float x, float y, int width, int height) {
        // צבע 0, מיקום התחלתי 1 (למטה)
        super(x, y, width, height, 0, 1);
        this.speed = NORMAL_SPEED;
    }

    @Override
    protected void loadAnimations() {
        loadMillerAnimationsByIndex(firstPosition);
    }

    private void loadMillerAnimationsByIndex(int position) {
        // שים לב לעדכן את הנתיבים בהתאם לתמונות האמיתיות של מילר
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
            System.out.println("Error loading Miller textures: " + e.getMessage());
        }
    }

    public void setMillerPatrolAI(MovementAI ai) {
        this.baseAI = ai;
        this.setMovementAI(ai);
    }

    @Override
    public void update(GameWorld world) {
        if (!active) return;
        super.update(world);
    }

    // הלוגיקה הייחודית של מילר! מחזיר true אם הוא תפס את השחקן
    public boolean checkPhoneAndChase(Player player) {
        boolean phoneOpen = player.isPhoneOpen();
        float distSq = this.getDistanceSquared(player);

        // אם השחקן עם טלפון פתוח ובטווח זיהוי
        if (phoneOpen && distSq <= (DETECTION_RANGE * DETECTION_RANGE)) {

            this.setAlert(true);
            this.setSpeed(CHASE_SPEED);

            // במקום לחשב תזוזה ידנית, פשוט מלבישים עליו את ה-ChaseAI!
            // הבדיקה (instanceof) מוודאת שלא ניצור AI חדש בכל פריים סתם
            if (!(this.movementAI instanceof ai.ChaseAI)) {
                this.setMovementAI(new ai.ChaseAI(player));
            }

            // אם תפס אותו (מרחק קטן מ-60)
            if (distSq <= (CATCH_RANGE * CATCH_RANGE)) {
                return true; // נתפס!
            }
        }
        else {
            // השחקן סגר את הטלפון או התרחק - מילר נרגע
            if (this.isAlert()) {
                this.setAlert(false);
                this.stop();
                this.setSpeed(NORMAL_SPEED);

                // הנה למה היינו חייבים את baseAI: מחזירים אותו לפטרול השגרה!
                this.setMovementAI(baseAI);
            }
        }

        return false; // לא נתפס
    }

    public void deactivate() {
        active = false;
    }
}