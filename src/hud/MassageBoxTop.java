package hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class MassageBoxTop {

    private String text = "";
    private boolean visible = false;
    private double timer = 0;
    private final double DISPLAY_TIME;

    // מיקומים ומידות משופרים
    private final int x = 290;  // הוזז כדי להישאר ממורכז עם הרוחב החדש
    private final int y = 25;
    private final int width = 700;  // הוגדל מ-580
    private final int height = 140; // הוגדל מ-100

    // גופן מוגדל ל-20 כפי שביקשת
    private final Font font = new Font("Arial", Font.BOLD, 20);

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

        // רקע שחור חצי-שקוף עם פינות מעוגלות
        g.setColor(new Color(0, 0, 0, 190)); // מעט כהה יותר לשיפור הקריאות
        g.fillRoundRect(x, y, width, height, 25, 25);

        // מסגרת לבנה עדינה סביב החלון להוספת עומק
        g.setColor(new Color(255, 255, 255, 50));
        g.drawRoundRect(x, y, width, height, 25, 25);

        // הגדרות טקסט
        g.setColor(Color.WHITE);
        g.setFont(font);

        drawCenteredMultiLineText(g, text);
    }

    private void drawCenteredMultiLineText(Graphics2D g, String text) {
        FontMetrics metrics = g.getFontMetrics(font);
        // מרווח שורות מעט גדול יותר לגופן 20
        int lineHeight = metrics.getHeight() + 4;

        List<String> finalLines = new ArrayList<>();

        // פיצול לפי ירידות שורה ידניות (\n)
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                // בדיקה אם המילה נכנסת בשורה הנוכחית (עם שוליים של 50 פיקסלים)
                if (metrics.stringWidth(currentLine + word) < width - 100) {
                    currentLine.append(word).append(" ");
                } else {
                    finalLines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                }
            }
            finalLines.add(currentLine.toString().trim());
        }

        // חישוב מיקום אנכי (מרכוז גוש הטקסט בתוך הגובה החדש 140)
        int totalHeight = finalLines.size() * lineHeight;
        int startY = y + ((height - totalHeight) / 2) + metrics.getAscent();

        for (String line : finalLines) {
            // מרכוז אופקי של כל שורה בנפרד
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