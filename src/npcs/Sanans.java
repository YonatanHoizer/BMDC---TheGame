package npcs;

import ai.ScriptedMovementAI;
import entities.NPC;
import world.GameWorld;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Sanans extends NPC {

    private ScriptedMovementAI scriptedAI;
    private boolean active = true;

    public Sanans(float x, float y, int width, int height) {
        super(x, y, width, height, 0, 1);
    }

    @Override
    protected void loadAnimations() {
        loadSanansAnimationsByIndex(firstPosition);
    }

    private void loadSanansAnimationsByIndex(int position) {

        String[] paths = {
                "/images/סננס קדימה.png", // [0] גודל: 64x128
                "/images/סננס אחורה.png", // [1] גודל: 64x128
                "/images/סננס צד א.png", // [2] גודל: 64x64
                "/images/סננס צד ב.png", // [3] גודל: 64x64
                "/images/סננס צד ג.png", // [4] גודל: 64x64
                "/images/סננס צד ד.png"  // [5] גודל: 64x64
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

    public void setSanansScriptedMovement(ScriptedMovementAI ai) {
        this.movementAI = ai;
    }

    @Override
    public void update(GameWorld world) {
        if (!active) return;
        super.update(world);
    }

    public boolean isActive() {
        return active;
    }

    public void deactivate() {
        active = false;
    }
}