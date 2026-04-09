package hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class TimerUI {

    private boolean visible = false;
    private double timeLeft = 0;
    private String prefixText = "";

    // מידות ומיקום (צד ימין למעלה)
    private final int width = 300;
    private final int height = 50;
    private final int x = 950;
    private final int y = 20;

    private int lastSecond = -1;
    private boolean needsTickSound = false;

    private final Font font = new Font("Arial", Font.BOLD, 20);

    // הפעלת הטיימר
    public void startTimer(String text, double timeInSeconds) {
        this.prefixText = text;
        this.timeLeft = timeInSeconds;
        this.lastSecond = (int) timeInSeconds; // שומרים את השנייה ההתחלתית
        this.needsTickSound = false;
        this.visible = true;
    }

    public void update(double deltaTime) {
        if (!visible) return;

        if (timeLeft > 0) {
            timeLeft -= deltaTime;

            // בדיקה האם עברה שנייה עגולה
            int currentSecond = (int) timeLeft;
            if (currentSecond < lastSecond && currentSecond >= 0) {
                needsTickSound = true; // מרים את הדגל!
            }
            lastSecond = currentSecond; // מעדכן את השנייה הקודמת

            if (timeLeft <= 0) {
                timeLeft = 0; // עוצר באפס
            }
        }
    }

    public void render(Graphics2D g) {
        if (!visible) return;

        // רקע שקוף עם פינות מעוגלות (כמו בשאר ה-HUD)
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, width, height, 15, 15);

        // מסגרת עדינה
        g.setColor(new Color(100, 100, 100));
        g.drawRoundRect(x, y, width, height, 15, 15);

        // המרת השניות לפורמט של דקות:שניות (MM:SS)
        int minutes = (int) (timeLeft / 60);
        int seconds = (int) (timeLeft % 60);
        String timeString = String.format("%02d:%02d", minutes, seconds);

        // חיבור הטקסט והזמן
        String displayText = prefixText + " : " + timeString;

        // אם נשאר פחות מ-10 שניות, נצבע את הטקסט באדום כדי להלחיץ את השחקן!
        if (timeLeft > 0 && timeLeft <= 5) {
            g.setColor(Color.RED);
        } else {
            g.setColor(Color.WHITE);
        }

        g.setFont(font);

        // מרכוז הטקסט בתוך התיבה
        FontMetrics fm = g.getFontMetrics(font);
        int textWidth = fm.stringWidth(displayText);
        int textX = x + (width - textWidth) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();

        g.drawString(displayText, textX, textY);
    }

    public boolean consumeTickSound() {
        if (needsTickSound) {
            needsTickSound = false; // מורידים את הדגל כדי שלא ינגן פעמיים
            return true;
        }
        return false;
    }

    // פונקציות עזר למערכת המשחק
    public void stopAndHide() {
        this.visible = false;
    }

    public boolean isTimeUp() {
        return visible && timeLeft <= 0;
    }

    public boolean isVisible() {
        return visible;
    }

    public double getTimeLeft() {
        return timeLeft;
    }
}