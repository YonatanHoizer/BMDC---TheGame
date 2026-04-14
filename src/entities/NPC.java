package entities;

import ai.MovementAI;
import world.GameWorld;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;

import static main.Game.deltaTime;

public class NPC extends MovableEntity {

    protected float visionRange = 570.0f;
    protected boolean alert = false;
    protected MovementAI movementAI;
    protected int colorIndex;
    protected int firstPosition;

    public NPC(float x, float y, int width, int height,int colorIndex,int firstPosition) {
        super(x, y, width, height);
        this.speed = 150.0f;
        this.colorIndex = colorIndex;
        this.firstPosition = firstPosition;
        loadAnimations();
    }

    protected void loadAnimations() {
        loadAnimationsByIndex(colorIndex, firstPosition);
    }

    private void loadAnimationsByIndex(int index ,int position) {
        String npcType1 = "";
        String npcType2 = "";
        String npcType3 = "";
        try {
            switch (index){
                case 1:
                    npcType1 = "/images/בלונדיני קדימה.png";
                    npcType2 = "/images/בלונדיני אחורה.png";
                    npcType3 = "/images/בלונדיני צד.png";
                    break;
                case 2:
                    npcType1 = "/images/חום קדימה.png";
                    npcType2 = "/images/חום אחורה.png";
                    npcType3 = "/images/חום צד.png";
                    break;
                case 3:
                    npcType1 = "/images/ג'ינג'י קדימה.png";
                    npcType2 = "/images/ג'ינג'י אחורה.png";
                    npcType3 = "/images/ג'ינג'י צד.png";
                    break;
                case 4:
                    npcType1 = "/images/שחור קדימה.png";
                    npcType2 = "/images/שחור אחורה.png";
                    npcType3 = "/images/שחור צד.png";
                    break;
                case 5:
                    npcType1 = "/images/צול קדימה.png";
                    npcType2 = "/images/צול אחורה.png";
                    npcType3 = "/images/צול צד.png";
                    break;
                case 6:
                    npcType1 = "/images/רגיל קדימה.png";
                    npcType2 = "/images/רגיל אחורה.png";
                    npcType3 = "/images/רגיל צד.png";
                    break;
            }

            BufferedImage frontSheet = ImageIO.read(getClass().getResourceAsStream(npcType1));
            BufferedImage backSheet  = ImageIO.read(getClass().getResourceAsStream(npcType2));
            BufferedImage sideSheet  = ImageIO.read(getClass().getResourceAsStream(npcType3));

            // חיתוך האנימציות (זהה ללוגיקה של השחקן)
            walkDown = new BufferedImage[] {
                    frontSheet.getSubimage(0, 0, 64, 64),
                    frontSheet.getSubimage(0, 64, 64, 64)
            };

            walkUp = new BufferedImage[] {
                    backSheet.getSubimage(0, 0, 64, 64),
                    backSheet.getSubimage(0, 64, 64, 64)
            };

            walkRight = new BufferedImage[] {
                    sideSheet.getSubimage(0, 0, 64, 64),
                    sideSheet.getSubimage(64, 0, 64, 64)
            };

            walkLeft = new BufferedImage[] {
                    sideSheet.getSubimage(0, 64, 64, 64),
                    sideSheet.getSubimage(64, 64, 64, 64)
            };

            switch (position) {
                case 1 :
                    this.direction = "DOWN";
                    this.sprite = walkDown[0];
                    break;
                case 2 :
                    this.direction = "LEFT";
                    this.sprite = walkLeft[0];
                    break;
                case 3 :
                    this.direction = "RIGHT";
                    this.sprite = walkRight[0];
                    break;
                case 4 :
                    this.direction = "UP";
                    this.sprite = walkUp[0];
                    break;
            }

        } catch (Exception e) {
            System.out.println("Error loading NPC color index " + index + ": " + e.getMessage());
        }
    }

    public void setMovementAI(MovementAI ai) {
        this.movementAI = ai;
    }

    public void update(GameWorld world) {
        if (movementAI != null) {
            movementAI.update(this, world);
        }
        // move(deltaTime) כבר מעדכן גם את האנימציה בתוך MovableEntity
        move(deltaTime,world);
    }


    public void moveTowards(float dx, float dy) {
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len == 0) {
            stop();
            return;
        }
        // נרמול וקביעת ה-dx/dy שיפעילו את האנימציה ב-move
        setDx(dx / len);
        setDy(dy / len);
    }

    // בדיקה אם NPC "רואה" יעד כללי
    public boolean canSee(Entity target) {
        float dx = target.getX() - x;
        float dy = target.getY() - y;
        return (dx * dx + dy * dy) <= visionRange * visionRange;
    }

    public void setAlert(boolean value) {
        alert = value;
    }

    public void setFirstPosition(int position) {
        this.firstPosition = position;

        // הגנה: אם האנימציות עדיין לא נטענו, אל תנסה להחליף תמונה
        if (walkDown == null || walkUp == null || walkLeft == null || walkRight == null) {
            return;
        }

        // חובה לעדכן גם את התמונה (sprite) וגם את מחרוזת הכיוון (direction)!
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
    }

    public boolean isAlert() {
        return alert;
    }

    public MovementAI getMovementAI() {
        return this.movementAI;
    }

    @Override
    public void Render(Graphics g) {
        // מצייר את ה-sprite הנוכחי (האנימציה)
        super.Render(g);

        // מוסיף את סימן האזהרה מעל הראש
        if (alert) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.drawString("!", (int) x + width / 2 - 5, (int) y - 5);
        }
    }
}