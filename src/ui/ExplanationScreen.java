package ui;

import engine.InputManager;
import main.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.util.Random;

public class ExplanationScreen extends Screen {

    private Game game;

    private long enterDelayStartTime;

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private String title;
    private String[] lines;

    // פלטת צבעים מודרנית (במקום צבעי היסוד הבוהקים)
    private static final Color COLOR_BG_CENTER = new Color(24, 28, 36);   // כחול-כהה עמוק למרכז הרקע
    private static final Color COLOR_BG_EDGE = new Color(12, 14, 18);     // כמעט שחור לקצוות המסך
    private static final Color COLOR_TITLE = new Color(100, 210, 255);    // כחול ניאון רך לכותרת
    private static final Color COLOR_HEADER = new Color(255, 198, 93);    // צהוב-זהב מעודן לכותרות המשנה
    private static final Color COLOR_TEXT = new Color(230, 235, 245);     // לבן-אפרפר רך לקריאה נוחה (לא שורף בעיניים)
    private static final Color COLOR_BOTTOM = new Color(150, 160, 175);   // אפור ניטרלי לטקסט היציאה

    // --- משתני אנימציה וחוויית משתמש (תואם לעמוד הראשי) ---
    private double animationTime = 0;
    private double introAlpha = 0.0;   // שקיפות כללית לכניסה חלקה (Fade-in)

    // מערכת חלקיקים תואמת לעמוד הראשי לאפקט המשכיות
    private float[] particleX = new float[25];
    private float[] particleY = new float[25];
    private float[] particleSpeed = new float[25];
    private float[] particleSize = new float[25];

    public ExplanationScreen(Game game, InputManager input) {
        super(input);
        this.game = game;

        // אתחול החלקיקים המרחפים ברקע
        Random rand = new Random();
        for (int i = 0; i < particleX.length; i++) {
            particleX[i] = rand.nextFloat() * screenWidth;
            particleY[i] = rand.nextFloat() * screenHeight;
            particleSpeed[i] = 15f + rand.nextFloat() * 25f;
            particleSize[i] = 2f + rand.nextFloat() * 4f;
        }
    }

    @Override
    public void onEnter() {
        super.onEnter();
        enterDelayStartTime = System.currentTimeMillis();
        title = "הסבר על המשחק";

        lines = new String[] {
                "מטרה:",
                "לעבור יום בישיבה בלי להפקיד את הטלפון בבוקר.",
                "",
                "שליטה:",
                "תנועת השחקן - חיצים",
                "Z - אינטראקציה עם דמויות או חפצים",
                "    X - פתיחה/סגירה של הטלפון",
                "   C - מחיקת הודעות בטלפון"
        };

        animationTime = 0;
        introAlpha = 0.0;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        animationTime += deltaTime;

        // 1. אנימציית Fade-in חלקה למסך כולו בעת הכניסה
        introAlpha += (1.0 - introAlpha) * 4.0 * deltaTime;
        if (introAlpha > 1.0) introAlpha = 1.0;

        // 2. עדכון חלקיקי הרקע (ריחוף למעלה, כמו בתפריט הראשי)
        for (int i = 0; i < particleX.length; i++) {
            particleY[i] -= particleSpeed[i] * deltaTime;
            particleX[i] += Math.sin(animationTime + i) * 10 * deltaTime;

            if (particleY[i] < -10) {
                particleY[i] = screenHeight + 10;
                particleX[i] = (float) (Math.random() * screenWidth);
            }
        }
    }

    @Override
    public void render(Graphics2D g) {
        // 1. הפעלת החלקת פונטים וגרפיקה מוגברת (Anti-aliasing) מוגברת ומקצועית
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 2. יצירת רקע מעבר צבעים מעגלי (Vignette / Radial Gradient) עדין
        Point2D center = new Point2D.Float(screenWidth / 2.0f, screenHeight / 2.0f);
        float radius = (float) Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight) / 2.0f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {COLOR_BG_CENTER, COLOR_BG_EDGE};
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        g.setPaint(gradient);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // --- ציור חלקיקי האווירה ברקע (בשילוב שקיפות ה-Intro) ---
        g.setColor(new Color(100, 210, 255, (int) (60 * introAlpha)));
        for (int i = 0; i < particleX.length; i++) {
            g.fillOval((int) particleX[i], (int) particleY[i], (int) particleSize[i], (int) particleSize[i]);
        }

        // --- חישובי אנימציה לכותרת הראשית (ריחוף גלי חלק ואפקט כניסה) ---
        double titleOffset = Math.sin(animationTime * 1.8) * 10;
        int baseTitleY = 120;
        int animatedTitleY = (int) (baseTitleY + titleOffset - (1.0 - introAlpha) * 40);

        // 3. ציור כותרת ראשית עם צל כפול יוקרתי
        Font titleFont = new Font("Segoe UI", Font.BOLD, 54);

        // צל עמוק וכהה
        g.setColor(new Color(0, 0, 0, (int) (180 * introAlpha)));
        drawCenteredString(g, title, animatedTitleY + 4, titleFont);

        // הכותרת עצמה
        g.setColor(new Color(COLOR_TITLE.getRed(), COLOR_TITLE.getGreen(), COLOR_TITLE.getBlue(), (int) (255 * introAlpha)));
        drawCenteredString(g, title, animatedTitleY, titleFont);

        // 4. פונטים משופרים לתוכן
        Font headerFont = new Font("Segoe UI", Font.BOLD, 32);
        Font textFont = new Font("Segoe UI", Font.PLAIN, 26);

        // חישוב הזזה קלה של התוכן מלמטה למעלה בזמן הכניסה
        int introContentOffset = (int) ((1.0 - introAlpha) * 30);
        int startY = 230 + introContentOffset;
        int lineHeight = 48;

        // 5. לולאת ציור השורות (מותאמת לשקיפות ה-Intro)
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].isEmpty()) {
                continue;
            }

            int currentY = startY + (i * lineHeight);

            if (lines[i].endsWith(":")) {
                // כותרות משנה (מטרה:, שליטה:) - מקבלות צבע זהב עם צל קל
                g.setColor(new Color(0, 0, 0, (int) (100 * introAlpha)));
                drawCenteredString(g, lines[i], currentY + 2, headerFont);

                g.setColor(new Color(COLOR_HEADER.getRed(), COLOR_HEADER.getGreen(), COLOR_HEADER.getBlue(), (int) (255 * introAlpha)));
                drawCenteredString(g, lines[i], currentY, headerFont);
            } else {
                // שורות הסבר רגילות - טקסט רך וקריא
                g.setColor(new Color(COLOR_TEXT.getRed(), COLOR_TEXT.getGreen(), COLOR_TEXT.getBlue(), (int) (230 * introAlpha)));
                drawCenteredString(g, lines[i], currentY, textFont);
            }
        }

        // --- חישוב אנימציית הבהוב (Fade) עדינה להוראות היציאה למטה ---
        int bottomAlpha = (int) ((140 + Math.sin(animationTime * 2.5) * 115) * introAlpha);
        bottomAlpha = Math.max(0, Math.min(255, bottomAlpha));
        Color animatedBottomColor = new Color(COLOR_BOTTOM.getRed(), COLOR_BOTTOM.getGreen(), COLOR_BOTTOM.getBlue(), bottomAlpha);

        // 6. טקסט תחתית לחזרה לתפריט
        Font bottomFont = new Font("Segoe UI", Font.PLAIN, 22);
        g.setColor(animatedBottomColor);
        drawCenteredString(g, "ENTER - לחזרה לתפריט", 670, bottomFont);
    }

    private void drawCenteredString(Graphics2D g, String text, int y, Font font) {
        g.setFont(font);
        FontMetrics metrics = g.getFontMetrics(font);
        int x = (screenWidth - metrics.stringWidth(text)) / 2;
        g.drawString(text, x, y);
    }

    public void handleInput(InputManager input) {
        // מניעת לחיצה מהירה מדי לפני שהאינטרו הסתיים
        if (introAlpha < 0.8) return;

        if (System.currentTimeMillis() - enterDelayStartTime < 500) {
            return;
        }
        if (input.E_key && canPressEnter()) {
            game.setScreen(new MainMenuScreen(game, input));
        }
    }
}