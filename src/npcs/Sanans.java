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
        super(x, y, width, height, 0, 3);
    }

    @Override
    protected void loadAnimations() {
        loadSanansAnimationsByIndex(firstPosition);
    }

    private void loadSanansAnimationsByIndex(int position) {

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

            // קביעת הספראייט הראשוני בהתאם למיקום התחלתי
            switch (position) {
                case 1 -> this.sprite = walkDown[0];
                case 2 -> this.sprite = walkLeft[0];
                case 3 -> this.sprite = walkRight[0];
                case 4 -> this.sprite = walkUp[0];
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
