package npcs;

import ai.ChaseAI;
import entities.NPC;
import entities.Player;
import world.GameWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Akiva extends NPC {

    private boolean active = true;

    // מחקנו מכאן את המשתנה chaseAI כי אין בו צורך!

    public Akiva(float x, float y, int width, int height) {
        super(x, y, width, height, 0, 1);
    }

    @Override
    protected void loadAnimations() {
        loadAkivaAnimationsByIndex(firstPosition);
    }

    private void loadAkivaAnimationsByIndex(int position) {
        // ... הקוד הקיים של טעינת האנימציות (הוא מצוין ונשאר בדיוק אותו דבר) ...
        String[] paths = {
                "/images/בלונדיני קדימה.png",
                "/images/בלונדיני אחורה.png",
                "/images/בלונדיני צד.png"
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
                case 1 -> this.direction = "DOWN";
                case 2 -> this.direction = "LEFT";
                case 3 -> this.direction = "RIGHT";
                case 4 -> this.direction = "UP";
            }

            switch (position) {
                case 1 -> this.sprite = walkDown[0];
                case 2 -> this.sprite = walkLeft[0];
                case 3 -> this.sprite = walkRight[0];
                case 4 -> this.sprite = walkUp[0];
            }

        } catch (Exception e) {
            System.out.println("Error loading Akiva textures: " + e.getMessage());
        }
    }

    @Override
    public void update(GameWorld world) {
        if (!active) return;
        super.update(world);
    }

    // --- פעולות ייחודיות לעקיבא ---

    /**
     * הפעלת מצב זעם (מרדף אחרי השחקן) - הופך את עקיבא למהיר ומסוכן
     */
    public void chasePlayer(Player player) {
        this.setSpeed(300.0f); // עקיבא נהיה מהיר מאוד
        this.setAlert(true);   // מדליק את סימן הקריאה מעל הראש

        // התיקון הקריטי: מוודאים שאנחנו לא יוצרים AI חדש בכל פריים!
        if (!(this.movementAI instanceof ChaseAI)) {
            this.setMovementAI(new ChaseAI(player)); // הזרקת ה-AI הרלוונטי בדיוק כשצריך
        }
    }

    /**
     * בדיקה אם הוא הצליח לתפוס את השחקן (לפי רדיוס)
     */
    public boolean hasCaughtPlayer(Player player) {
        float distSq = this.getDistanceSquared(player);
        return distSq < (60 * 60); // רדיוס תפיסה
    }

    /**
     * הרגעה (במידה והשחקן שיקר בהצלחה)
     */
    public void calmDown() {
        this.setAlert(false);
        this.setSpeed(150.0f); // חוזר למהירות רגילה
        this.stop(); // עוצר את התנועה הפיזית באותו רגע
        this.setMovementAI(null); // <--- השורה הקריטית! מוחקת את מוח המרדף
    }

    public void deactivate() {
        active = false;
    }
}