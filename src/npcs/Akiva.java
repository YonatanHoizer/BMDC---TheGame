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
                "/images/עקיבא קדימה.png",
                "/images/עקיבא אחורה.png",
                "/images/עקיבא צד א.png",
                "/images/עקיבא צד ב.png",
                "/images/עקיבא צד ג.png",
                "/images/עקיבא צד ד.png"
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