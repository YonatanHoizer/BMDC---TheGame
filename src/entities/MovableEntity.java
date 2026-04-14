package entities;

import world.GameWorld;
import util.Rect;
import java.awt.image.BufferedImage;

public abstract class MovableEntity extends Entity {

    protected float speed;
    protected float dx, dy;

    protected BufferedImage[] walkUp, walkDown, walkLeft, walkRight;
    protected String direction = "DOWN";
    protected int animationTick = 0;
    protected int animationFrame = 0;
    protected int animationSpeed = 15;

    public MovableEntity(float x, float y, int width, int height) {
        super(x, y, width, height, null);
        this.speed = 4.0f;
    }

    // הפעולה עכשיו מקבלת את העולם כדי לבדוק התנגשויות
    public void move(double deltaTime, GameWorld world) {
        float moveX = dx;
        float moveY = dy;

        if (moveX != 0 && moveY != 0) {
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;
        }

        float stepX = moveX * speed * (float) deltaTime;
        float stepY = moveY * speed * (float) deltaTime;

        // --- טיפול בציר X (עם בדיקת התנגשות) ---
        if (moveX != 0) {
            float nextX = x + stepX;
            // יצירת Rect זמני לבדיקה (במיקום ה-Y הנוכחי)
            Rect futureHitboxX = new Rect(nextX, y, width, height);
            if (world.canMoveTo(futureHitboxX)) {
                x = nextX;
            }
        }

        // --- טיפול בציר Y (עם בדיקת התנגשות) ---
        if (moveY != 0) {
            float nextY = y + stepY;
            // יצירת Rect זמני לבדיקה (במיקום ה-X המעודכן)
            Rect futureHitboxY = new Rect(x, nextY, width, height);
            if (world.canMoveTo(futureHitboxY)) {
                y = nextY;
            }
        }

        updateAnimation();
    }

    protected void updateAnimation() {
        // חישוב עוצמת התנועה בכל ציר
        float absDx = Math.abs(dx);
        float absDy = Math.abs(dy);

        // שינוי הכיוון רק אם יש תנועה כלשהי
        if (absDx > 0 || absDy > 0) {

            // לוגיקת ה-45 מעלות:
            // אם התנועה ב-Y חזקה יותר מהתנועה ב-X, נראה אנימציית מעלה/מטה
            if (absDy > absDx) {
                if (dy > 0) direction = "DOWN";
                else direction = "UP";
            }
            // אם התנועה ב-X חזקה יותר (או שווה), נראה אנימציית צד
            else {
                if (dx > 0) direction = "RIGHT";
                else direction = "LEFT";
            }

            // עדכון הפריימים של האנימציה
            animationTick++;
            if (animationTick >= animationSpeed) {
                animationTick = 0;
                animationFrame = (animationFrame + 1) % 2;
            }
        } else {
            // אם עומדים - חוזרים לפריים הראשון (עמידה)
            animationFrame = 0;
        }

        updateSpriteByDirection();
    }

    private void updateSpriteByDirection() {
        switch (direction) {
            case "UP":    if (walkUp != null)    sprite = walkUp[animationFrame]; break;
            case "DOWN":  if (walkDown != null)  sprite = walkDown[animationFrame]; break;
            case "LEFT":  if (walkLeft != null)  sprite = walkLeft[animationFrame]; break;
            case "RIGHT": if (walkRight != null) sprite = walkRight[animationFrame]; break;
        }
    }

    public void stop() { this.dx = 0; this.dy = 0; }
    public float getDx() { return dx; }
    public void setDx(float dx) { this.dx = dx; }
    public float getDy() { return dy; }
    public void setDy(float dy) { this.dy = dy; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
}