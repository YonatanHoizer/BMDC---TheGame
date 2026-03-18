package hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class MassageBoxTop {

    private String text = "";
    private boolean visible = false;
    private double timer = 0;
    private final double DISPLAY_TIME;

    private final int x = 350;
    private final int y = 20;
    private final int width = 580;
    private final int height = 100;

    // הגדרת הפונט כאן כדי שנוכל להשתמש בו גם בחישובים
    private final Font font = new Font("Arial", Font.BOLD, 16);

    public MassageBoxTop(double displayTimeSeconds) {
        this.DISPLAY_TIME = displayTimeSeconds;
    }

    public void show(String text, double timer) {
        this.text = text;
        this.visible = true;
        this.timer = timer;
    }

    public void update(double deltaTime) {
        if (!visible) return;
        timer -= deltaTime;
        if (timer <= 0) visible = false;
    }

    public void render(Graphics2D g) {
        if (!visible) return;

        // רקע שקוף עם פינות מעוגלות
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRoundRect(x, y, width, height, 20, 20);

        // הגדרות טקסט
        g.setColor(Color.WHITE);
        g.setFont(font);

        drawCenteredMultiLineText(g, text);
    }

    private void drawCenteredMultiLineText(Graphics2D g, String text) {
        FontMetrics metrics = g.getFontMetrics(font);
        int lineHeight = metrics.getHeight();

        // רשימה סופית של כל השורות שנצייר
        java.util.List<String> finalLines = new java.util.ArrayList<>();

        // 1. קודם כל נפצל לפי ירידות שורה ידניות שאתה הכנסת
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            // 2. עבור כל פסקה, נבצע פיצול אוטומטי לפי רוחב המלבן
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                // בדיקה אם המילה נכנסת בשורה הנוכחית
                if (metrics.stringWidth(currentLine + word) < width - 40) {
                    currentLine.append(word).append(" ");
                } else {
                    // השורה מלאה, נוסיף אותה ונפתח שורה חדשה בתוך אותה פסקה
                    finalLines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                }
            }
            // הוספת השארית של הפסקה
            finalLines.add(currentLine.toString().trim());
        }

        // חישוב מיקום אנכי (למרכז את כל גוש הטקסט בתוך ה-height)
        int totalHeight = finalLines.size() * lineHeight;
        int startY = y + ((height - totalHeight) / 2) + metrics.getAscent();

        for (String line : finalLines) {
            // חישוב מיקום אופקי (למרכז כל שורה בנפרד)
            int lineWidth = metrics.stringWidth(line);
            int startX = x + (width - lineWidth) / 2;

            g.drawString(line, startX, startY);
            startY += lineHeight;
        }
    }

    public boolean isVisible() { return visible; }
    public void hide() { this.visible = false; }
    public double getTimer() { return this.timer; }
}