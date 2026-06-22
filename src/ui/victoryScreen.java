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

public class victoryScreen extends Screen {

    private Game game;

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private int buttonWidth = 420;
    private int buttonHeight = 70;
    private int buttonX;
    private int buttonY;

    // פלטת צבעים מלכותית ויוקרתית למסך הניצחון
    private static final Color COLOR_BG_CENTER = new Color(25, 30, 75);   // כחול-סגול עמוק ומלכותי במרכז
    private static final Color COLOR_BG_EDGE = new Color(8, 10, 25);       // כמעט שחור בקצוות לעומק קולנועי
    private static final Color COLOR_TITLE = new Color(255, 215, 0);       // זהב מלכותי (Gold)
    private static final Color COLOR_TITLE_SHADOW = new Color(139, 101, 8, 120); // צל זהב כהה חצי שקוף
    private static final Color COLOR_TEXT = new Color(245, 247, 255);     // לבן-אפרפר רך ויוקרתי
    private static final Color COLOR_BTN_BG = new Color(45, 50, 70);       // רקע כפתור כהה עם גוון כחלחל
    private static final Color COLOR_BTN_BORDER = new Color(255, 198, 93);  // מסגרת זהב בהירה ומנצנצת לכפתור

    // --- משתני אנימציה וחוויית משתמש (תואם לשאר המסכים המשודרגים) ---
    private double animationTime = 0;
    private double buttonScale = 0.5; // מתחיל קטן לאפקט כניסה חגיגי
    private double introAlpha = 0.0;  // שקיפות ה-Intro לכניסה חלקה
    private static final double ANIMATION_SPEED = 6.0;

    // מערכת חלקיקים מוזהבים ומנצנצים לחגיגת הניצחון
    private float[] particleX = new float[35]; // מעט יותר חלקיקים לאפקט עשיר
    private float[] particleY = new float[35];
    private float[] particleSpeed = new float[35];
    private float[] particleSize = new float[35];
    private float[] particlePhase = new float[35]; // משמש לאפקט נצנוץ (Flicker) ייחודי לניצחון

    public victoryScreen(Game game, InputManager input) {
        super(input);
        this.game = game;

        // אתחול מערכת חלקיקי הזהב
        Random rand = new Random();
        for (int i = 0; i < particleX.length; i++) {
            particleX[i] = rand.nextFloat() * screenWidth;
            particleY[i] = rand.nextFloat() * screenHeight;
            particleSpeed[i] = 12f + rand.nextFloat() * 20f; // ריחוף חגיגי ואיטי
            particleSize[i] = 2f + rand.nextFloat() * 5f;
            particlePhase[i] = rand.nextFloat() * 6.28f; // שלב התחלתי של גל הסינוס לנצנוץ
        }
    }

    @Override
    public void onEnter() {
        super.onEnter();
        buttonX = (screenWidth - buttonWidth) / 2;
        buttonY = 550;

        animationTime = 0;
        buttonScale = 0.5;
        introAlpha = 0.0;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        animationTime += deltaTime;

        // 1. אנימציית Fade-in חלקה למסך כולו
        introAlpha += (1.0 - introAlpha) * 4.0 * deltaTime;
        if (introAlpha > 1.0) introAlpha = 1.0;

        // 2. עדכון חלקיקי הזהב המנצנצים ברקע
        for (int i = 0; i < particleX.length; i++) {
            particleY[i] -= particleSpeed[i] * deltaTime;
            particleX[i] += Math.sin(animationTime + i) * 12 * deltaTime;

            // אם חלקיק יצא מלמעלה, נחזיר אותו מלמטה
            if (particleY[i] < -10) {
                particleY[i] = screenHeight + 10;
                particleX[i] = (float) (Math.random() * screenWidth);
            }
        }

        // 3. חישוב גודל הכפתור (נמצא קבוע במצב נבחר במסך זה, גדל פנימה חלק + פעימה קלה)
        double targetScale = 1.06;
        buttonScale += (targetScale - buttonScale) * ANIMATION_SPEED * deltaTime;
    }

    @Override
    public void render(Graphics2D g) {
        // 1. הפעלת החלקת גרפיקה וטקסט (Anti-aliasing) למראה מלוטש ומקצועי
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 2. רקע מעבר צבעים מעגלי (Radial Gradient) אטמוספרי ועמוק
        Point2D center = new Point2D.Float(screenWidth / 2.0f, screenHeight / 2.0f);
        float radius = (float) Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight) / 2.0f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {COLOR_BG_CENTER, COLOR_BG_EDGE};
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        g.setPaint(gradient);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // --- ציור חלקיקי זהב מנצנצים (Sparkles) ---
        for (int i = 0; i < particleX.length; i++) {
            // חישוב נצנוץ ייחודי לכל חלקיק באמצעות גל סינוס המשתנה לפי ה-Phase שלו
            double flicker = (Math.sin(animationTime * 4.0 + particlePhase[i]) + 1.0) / 2.0;
            int particleAlpha = (int) ((30 + flicker * 90) * introAlpha);
            particleAlpha = Math.max(0, Math.min(255, particleAlpha));

            g.setColor(new Color(255, 225, 120, particleAlpha));
            g.fillOval((int) particleX[i], (int) particleY[i], (int) particleSize[i], (int) particleSize[i]);
        }

        // --- חישובי אנימציה לכותרת הענקית (ריחוף גלי רך וחגיגי) ---
        double titleOffset = Math.sin(animationTime * 1.5) * 10;
        int baseTitleY = 200;
        int animatedTitleY = (int) (baseTitleY + titleOffset - (1.0 - introAlpha) * 50);

        // 3. כותרת ענקית "ניצחת!" עם פונט מודרני ואפקט צל מוזהב כפול
        String title = "ניצחת!";
        Font titleFont = new Font("Segoe UI", Font.BOLD, 108); // נוכחות מוגדלת וחגיגית
        g.setFont(titleFont);
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;

        // ציור צל הכותרת לעומק (מותאם ל-Intro)
        g.setColor(new Color(COLOR_TITLE_SHADOW.getRed(), COLOR_TITLE_SHADOW.getGreen(), COLOR_TITLE_SHADOW.getBlue(), (int) (150 * introAlpha)));
        g.drawString(title, titleX + 4, animatedTitleY + 6);

        // אפקט זוהר אחורי רך לכותרת
        g.setColor(new Color(255, 215, 0, (int) (35 * introAlpha)));
        g.drawString(title, titleX - 2, animatedTitleY - 2);

        // ציור הכותרת המוזהבת עצמה
        g.setColor(new Color(COLOR_TITLE.getRed(), COLOR_TITLE.getGreen(), COLOR_TITLE.getBlue(), (int) (255 * introAlpha)));
        g.drawString(title, titleX, animatedTitleY);

        // --- חישוב הזזה קלה של התוכן מלמטה למעלה בזמן הכניסה ---
        int introContentOffset = (int) ((1.0 - introAlpha) * 30);
        int subTitle1Y = 350 + introContentOffset;
        int subTitle2Y = 405 + introContentOffset;

        // 4. הודעת הניצחון בפונט משופר, נקי וקריא עם צל טקסט עדין לקריאות מושלמת
        g.setFont(new Font("Segoe UI", Font.PLAIN, 36));
        FontMetrics msgMetrics = g.getFontMetrics();

        String subTitle1 = "כל הכבוד! שרדת יום שלם בישיבה";
        String subTitle2 = "בלי להפקיד את הטלפון שלך.";

        int sub1X = (screenWidth - msgMetrics.stringWidth(subTitle1)) / 2;
        int sub2X = (screenWidth - msgMetrics.stringWidth(subTitle2)) / 2;

        // צללים לטקסט
        g.setColor(new Color(0, 0, 0, (int) (120 * introAlpha)));
        g.drawString(subTitle1, sub1X + 1, subTitle1Y + 2);
        g.drawString(subTitle2, sub2X + 1, subTitle2Y + 2);

        // הטקסט המרכזי
        g.setColor(new Color(COLOR_TEXT.getRed(), COLOR_TEXT.getGreen(), COLOR_TEXT.getBlue(), (int) (240 * introAlpha)));
        g.drawString(subTitle1, sub1X, subTitle1Y);
        g.drawString(subTitle2, sub2X, subTitle2Y);

        // 5. ציור הכפתור המעוצב לחזרה (משתמש ב-buttonScale ובמיקום הדינמי)
        drawButton(g, "חזור לתפריט הראשי", buttonX, buttonY + introContentOffset, true, buttonScale);
    }

    private void drawButton(Graphics2D g, String text, int x, int y, boolean selected, double scale) {
        // חישוב מידות דינמיות מהמרכז כדי שהתרחבות הכפתור תהיה סימטרית
        int scaledWidth = (int) (buttonWidth * scale);
        int scaledHeight = (int) (buttonHeight * scale);
        int scaledX = x - (scaledWidth - buttonWidth) / 2;
        int scaledY = y - (scaledHeight - buttonHeight) / 2;

        int alpha = (int) (255 * introAlpha);

        // אפקט צל זוהר אחורי (Drop Shadow) מלכותי בצבע זהב
        g.setColor(new Color(255, 198, 93, (int) (40 * introAlpha)));
        g.fillRoundRect(scaledX - 6, scaledY - 6, scaledWidth + 12, scaledHeight + 12, 28, 28);

        // צביעת פנים הכפתור
        g.setColor(new Color(COLOR_BTN_BG.getRed(), COLOR_BTN_BG.getGreen(), COLOR_BTN_BG.getBlue(), alpha));
        g.fillRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);

        // ציור מסגרת הכפתור - פועמת בצורה חלקה ויוקרתית
        int pulseAlpha = (int) ((200 + Math.sin(animationTime * 5.5) * 55) * introAlpha);
        pulseAlpha = Math.max(0, Math.min(255, pulseAlpha));
        g.setColor(new Color(COLOR_BTN_BORDER.getRed(), COLOR_BTN_BORDER.getGreen(), COLOR_BTN_BORDER.getBlue(), pulseAlpha));
        g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);

        // מסגרת פנימית דקה נוספת לטאץ' מנצח
        g.setColor(new Color(255, 255, 255, (int) (80 * introAlpha)));
        g.drawRoundRect(scaledX + 1, scaledY + 1, scaledWidth - 2, scaledHeight - 2, 22, 22);

        // כתיבת הטקסט בתוך הכפתור (ממורכז אופקית ואנכית)
        int fontSize = (int) (26 * scale);
        g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));

        FontMetrics fm = g.getFontMetrics();
        int textX = scaledX + (scaledWidth - fm.stringWidth(text)) / 2;
        int textY = scaledY + ((scaledHeight - fm.getHeight()) / 2) + fm.getAscent();

        // צל קטן לטקסט
        g.setColor(new Color(0, 0, 0, (int) (130 * introAlpha)));
        g.drawString(text, textX + 1, textY + 2);

        // צבע הטקסט פועם בזהב רך בשילוב המסגרת
        int textGlow = (int) (225 + Math.sin(animationTime * 5.5) * 30);
        g.setColor(new Color(255, textGlow, 180, alpha));
        g.drawString(text, textX, textY);
    }

    @Override
    public void handleInput(InputManager input) {
        // חסימת קלט קלה למניעת דילוג בטעות לפני שהאינטרו מסתיים
        if (introAlpha < 0.8) return;

        if (System.currentTimeMillis() - enterLockTimer < 500 && !canPressEnter()) {
            return;
        }

        if (input.E_key && canPressEnter()) {
            game.setScreen(new MainMenuScreen(game, input));
        }
    }
}