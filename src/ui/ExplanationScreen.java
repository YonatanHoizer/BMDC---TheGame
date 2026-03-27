package ui;

import engine.InputManager;
import main.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

public class ExplanationScreen extends Screen {

    private Game game;

    private long enterDelayStartTime;

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private String title;
    private String[] lines;

    public ExplanationScreen(Game game, InputManager input) {
        super(input);
        this.game = game;
    }

    @Override
    public void onEnter() {
        super.onEnter();
        enterDelayStartTime = System.currentTimeMillis();
        title = "הסבר על המשחק";

        // סידרתי קצת את הטקסט שיהיה אחיד וברור יותר
        lines = new String[] {
                "מטרה:",
                "לעבור יום בישיבה בלי להפקיד את הטלפון בבוקר.",
                "",
                "שליטה:",
                "חצים או W,A,S,D - תנועת השחקן",
                "E - אינטראקציה עם דמויות או חפצים",
                "Enter - פתיחה/סגירה של הטלפון",
                "מקש רווח - מחיקת הודעות בטלפון"
        };
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void render(Graphics2D g) {
        // ציור הרקע השחור
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // ציור הכותרת הראשית (ממורכזת)
        Font titleFont = new Font("Arial", Font.BOLD, 48);
        g.setColor(Color.WHITE);
        drawCenteredString(g, title, 120, titleFont);

        // הגדרת פונטים לשורות הטקסט
        Font headerFont = new Font("Arial", Font.BOLD, 30);
        Font textFont = new Font("Arial", Font.PLAIN, 26);

        int startY = 220;
        int lineHeight = 45; // קצת יותר מרווח בין השורות לקריאה נוחה

        // מעבר על כל שורות הטקסט וציור שלהן
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                continue; // מדלגים על שורות ריקות (אבל שומרים על הרווח שלהן בזכות המכפלה למטה)
            }

            // צביעה שונה לכותרות כדי שייראה מקצועי יותר
            if (lines[i].endsWith(":")) {
                g.setColor(Color.YELLOW);
                drawCenteredString(g, lines[i], startY + (i * lineHeight), headerFont);
            } else {
                g.setColor(Color.WHITE);
                drawCenteredString(g, lines[i], startY + (i * lineHeight), textFont);
            }
        }

        // ציור טקסט החזרה לתפריט בתחתית
        Font bottomFont = new Font("Arial", Font.PLAIN, 20);
        g.setColor(Color.LIGHT_GRAY);
        drawCenteredString(g, "ENTER - לחזרה לתפריט", 680, bottomFont);
    }

    /**
     * פונקציית עזר מצוינת שתמיד תמרכז לך טקסט על המסך.
     * מומלץ לשמור אותה, היא תעזור לך בעוד המון מקומות!
     */
    private void drawCenteredString(Graphics2D g, String text, int y, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        // חישוב ה-X המדויק: אמצע המסך פחות חצי מרוחב הטקסט
        int x = (screenWidth - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    public void handleInput(InputManager input) {
        if (System.currentTimeMillis() - enterDelayStartTime < 500) {
            return;
        }
        if (input.ENTER_key && canPressEnter()) {
            game.setScreen(new MainMenuScreen(game, input));
        }
    }
}