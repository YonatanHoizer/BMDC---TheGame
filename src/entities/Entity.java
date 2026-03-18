package entities;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Entity {
    protected float x, y;
    protected int width, height; // הוסר ה-static
    public BufferedImage sprite;
    protected Rectangle bounds;

    public Entity(float x, float y, int width, int height, BufferedImage sprite) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.sprite = sprite;
        this.bounds = new Rectangle(0, 0, width, height);
    }

    public void Render(Graphics g) {
        if (sprite != null) {
            g.drawImage(sprite, (int) x, (int) y, width, height, null);
        }
    }

    public Rectangle getBounds() {
        // הגדרת ה-Hitbox להיות החצי התחתון של הדמות (בשביל עומק)
        bounds.x = (int) x + 5;
        bounds.y = (int) y + (height / 2);
        bounds.width = width - 10;
        bounds.height = height / 2;
        return bounds;
    }

    /**
     * מחשב את המרחק הריבועי (כדי לחסוך ביצועים) בין מרכז האובייקט הזה למרכז של אובייקט אחר
     */
    public float getDistanceSquared(Entity other) {
        if (other == null) return Float.MAX_VALUE; // הגנה מקריסות אם האובייקט נמחק (כמו החלב)

        // חישוב המרכז של האובייקט שלנו
        float thisCenterX = this.x + (this.width / 2.0f);
        float thisCenterY = this.y + (this.height / 2.0f);

        // חישוב המרכז של האובייקט השני
        float otherCenterX = other.getX() + (other.getWidth() / 2.0f);
        float otherCenterY = other.getY() + (other.getHeight() / 2.0f);

        float dx = thisCenterX - otherCenterX;
        float dy = thisCenterY - otherCenterY;

        return dx * dx + dy * dy;
    }

    // Getters and Setters
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}