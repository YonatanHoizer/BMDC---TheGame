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

public class GameOverScreen extends Screen {

    private Game game;
    private int failId;
    private String failMessage;

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private int buttonWidth = 420;
    private int buttonHeight = 70;
    private int buttonX;
    private int buttonY;

    // פלטת צבעים מודרנית למסך הפסילה
    private static final Color COLOR_BG_CENTER = new Color(90, 10, 15);   // אדום דם עמוק במרכז
    private static final Color COLOR_BG_EDGE = new Color(20, 5, 5);       // כמעט שחור בקצוות
    private static final Color COLOR_TITLE = new Color(255, 65, 65);       // אדום בוהק ונקי לכותרת
    private static final Color COLOR_TEXT = new Color(240, 240, 245);     // לבן-אפרפר רך להודעות
    private static final Color COLOR_BTN_BG = new Color(45, 45, 50);      // רקע כפתור כהה ומודרני
    private static final Color COLOR_BTN_BORDER = new Color(255, 198, 93);  // מסגרת מוזהבת עדינה לכפתור המסומן

    // --- משתני אנימציה וחוויית משתמש (תואם לשאר המסכים המשודרגים) ---
    private double animationTime = 0;
    private double buttonScale = 0.5; // מתחיל קטן וגדל לחוויית כניסה
    private double introAlpha = 0.0;  // שקיפות ה-Intro לכניסה חלקה
    private static final double ANIMATION_SPEED = 6.0;

    // מערכת חלקיקים תואמת (הפעם באדום עמוק/אפרורי לאפקט של אפר/עשן של הפסילה)
    private float[] particleX = new float[25];
    private float[] particleY = new float[25];
    private float[] particleSpeed = new float[25];
    private float[] particleSize = new float[25];

    public GameOverScreen(Game game, InputManager input, int failId) {
        super(input);
        this.game = game;
        this.failId = failId;
        setFailMessage();

        // אתחול מערכת החלקיקים
        Random rand = new Random();
        for (int i = 0; i < particleX.length; i++) {
            particleX[i] = rand.nextFloat() * screenWidth;
            particleY[i] = rand.nextFloat() * screenHeight;
            particleSpeed[i] = 10f + rand.nextFloat() * 20f; // מהירות מעט איטית וכבדה יותר
            particleSize[i] = 2f + rand.nextFloat() * 5f;
        }
    }

    private void setFailMessage() {
        switch (failId) {
            case 1:
                failMessage = "סננס תפס אותך! עליך להתחבא מהר יותר או לצאת למסדרון.";
                break;
            case 2:
                failMessage = "איחרת לתפילה! הרב מילר ראה שלא הגעת והחרים לך את הטלפון ל24 שעות.";
                break;
            case 3:
                failMessage = "כנות לא מעניינת את עקיבא בשיט...";
                break;
            case 4:
                failMessage = "הרב מילר תפס אותך עם טלפון בבית מדרש";
                break;
            case 5:
                failMessage = "הרב קרוייזר תפס אותך אחרי שלא הגעת לשיעור שלו.";
                break;
            case 6:
                failMessage = "איי איי איי , כול כך קרובבב\nאם רק היית מקשיב יותר בשיעורים";
                break;
            case 7:
                failMessage = "מיהרת מידי ,זה משחק עם עלילה אחרי הכול.";
                break;
            case 8:
                failMessage = "אי אפשר סתם לדלג על שלבים אחי\nזה משחק עם עלילה בסופו של דבר.";
                break;
            case 9:
                failMessage = "קצת פחדני ממך להיתחבא פה ממילר אתה לא חושב?\nתתחמק ממנו בתוך הבית מדרש ,אחרת למה כתבתי את כול הקוד הזה ?!";
                break;
            case 10:
                failMessage = "היית צריך להישאר בבית מדרש \n יש סיום יפה למשחק , חבל שתפספס אותו";
                break;
            default:
                failMessage = "נפסלת! נסה שנית.";
                break;
        }
    }

    @Override
    public void onEnter() {
        super.onEnter();
        buttonX = (screenWidth - buttonWidth) / 2;
        buttonY = 520;

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

        // 2. עדכון חלקיקי האווירה ברקע (עולים למעלה באיטיות)
        for (int i = 0; i < particleX.length; i++) {
            particleY[i] -= particleSpeed[i] * deltaTime;
            particleX[i] += Math.sin(animationTime + i) * 8 * deltaTime;

            if (particleY[i] < -10) {
                particleY[i] = screenHeight + 10;
                particleX[i] = (float) (Math.random() * screenWidth);
            }
        }

        // 3. חישוב גודל הכפתור (נמצא תמיד במצב Selected במסך זה, אז הוא מוגדל קצת קבוע + פעימה עדינה)
        double targetScale = 1.06;
        buttonScale += (targetScale - buttonScale) * ANIMATION_SPEED * deltaTime;
    }

    @Override
    public void render(Graphics2D g) {
        // 1. הפעלת החלקת גרפיקה וטקסט (Anti-aliasing) למראה נקי בלי פיקסלים שבורים
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // 2. רקע מעבר צבעים מעגלי (Radial Gradient) אטמוספרי ומתוחכם
        Point2D center = new Point2D.Float(screenWidth / 2.0f, screenHeight / 2.0f);
        float radius = (float) Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight) / 2.0f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {COLOR_BG_CENTER, COLOR_BG_EDGE};
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        g.setPaint(gradient);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // --- ציור חלקיקי האווירה (בגוון אדמדם עמוק שתואם למסך הפסילה) ---
        g.setColor(new Color(255, 70, 70, (int) (45 * introAlpha)));
        for (int i = 0; i < particleX.length; i++) {
            g.fillOval((int) particleX[i], (int) particleY[i], (int) particleSize[i], (int) particleSize[i]);
        }

        // --- חישובי אנימציה לכותרת (פעימה קלה וריחוף דרמטי) ---
        double titleOffset = Math.sin(animationTime * 2.0) * 8;
        int baseTitleY = 200;
        int animatedTitleY = (int) (baseTitleY + titleOffset - (1.0 - introAlpha) * 40);

        // 3. כותרת גדולה "נפסלת!" עם צל שחור עמוק מאחורה לעומק
        String title = "נפסלת!";
        Font titleFont = new Font("Segoe UI", Font.BOLD, 88); // הגדלה קלה לנוכחות מודגשת
        g.setFont(titleFont);
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;

        // ציור הצל העמוק
        g.setColor(new Color(0, 0, 0, (int) (200 * introAlpha)));
        g.drawString(title, titleX + 4, animatedTitleY + 6);

        // ציור הכותרת עם התאמת שקיפות הכניסה
        g.setColor(new Color(COLOR_TITLE.getRed(), COLOR_TITLE.getGreen(), COLOR_TITLE.getBlue(), (int) (255 * introAlpha)));
        g.drawString(title, titleX, animatedTitleY);

        // --- חישוב הזזה קלה של התוכן מלמטה למעלה בזמן הכניסה ---
        int introContentOffset = (int) ((1.0 - introAlpha) * 25);
        int startY = 320 + introContentOffset;

        // 4. ציור הודעת הפסילה הספציפית (תומך ב-\n) עם פונט קריא ומודרני
        g.setColor(new Color(COLOR_TEXT.getRed(), COLOR_TEXT.getGreen(), COLOR_TEXT.getBlue(), (int) (230 * introAlpha)));
        g.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        FontMetrics msgMetrics = g.getFontMetrics();

        String[] lines = failMessage.split("\n");
        int lineHeight = msgMetrics.getHeight() + 12;

        for (String line : lines) {
            String trimmedLine = line.trim();
            int msgX = (screenWidth - msgMetrics.stringWidth(trimmedLine)) / 2;

            // צל עדין מאחורי טקסט הפסילה לקריאות מקסימלית על הרקע האדום
            g.setColor(new Color(0, 0, 0, (int) (120 * introAlpha)));
            g.drawString(trimmedLine, msgX + 1, startY + 2);

            g.setColor(new Color(COLOR_TEXT.getRed(), COLOR_TEXT.getGreen(), COLOR_TEXT.getBlue(), (int) (240 * introAlpha)));
            g.drawString(trimmedLine, msgX, startY);
            startY += lineHeight;
        }

        // 5. ציור הכפתור לחזרה (משתמש ב-buttonScale ובאפקט המיקום הדינמי)
        drawButton(g, "לחץ Enter לחזרה לתפריט הראשי", buttonX, buttonY + introContentOffset, true, buttonScale);
    }

    private void drawButton(Graphics2D g, String text, int x, int y, boolean selected, double scale) {
        // חישוב מידות דינמיות מהמרכז כדי שהכפתור יתרחב באופן שווה
        int scaledWidth = (int) (buttonWidth * scale);
        int scaledHeight = (int) (buttonHeight * scale);
        int scaledX = x - (scaledWidth - buttonWidth) / 2;
        int scaledY = y - (scaledHeight - buttonHeight) / 2;

        int alpha = (int) (255 * introAlpha);

        // אפקט צל זוהר אחורי (Drop Shadow) מוזהב
        g.setColor(new Color(255, 198, 93, (int) (35 * introAlpha)));
        g.fillRoundRect(scaledX - 6, scaledY - 6, scaledWidth + 12, scaledHeight + 12, 28, 28);

        // צביעת פנים הכפתור
        g.setColor(new Color(COLOR_BTN_BG.getRed(), COLOR_BTN_BG.getGreen(), COLOR_BTN_BG.getBlue(), alpha));
        g.fillRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);

        // ציור מסגרת הכפתור - פועמת בצורה חלקה
        int pulseAlpha = (int) ((200 + Math.sin(animationTime * 5.0) * 55) * introAlpha);
        pulseAlpha = Math.max(0, Math.min(255, pulseAlpha));
        g.setColor(new Color(COLOR_BTN_BORDER.getRed(), COLOR_BTN_BORDER.getGreen(), COLOR_BTN_BORDER.getBlue(), pulseAlpha));
        g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);

        // מסגרת פנימית דקה נוספת למראה המלוטש
        g.setColor(new Color(255, 255, 255, (int) (70 * introAlpha)));
        g.drawRoundRect(scaledX + 1, scaledY + 1, scaledWidth - 2, scaledHeight - 2, 22, 22);

        // כתיבת הטקסט בתוך הכפתור (ממורכז אופקית ואנכית)
        int fontSize = (int) (24 * scale);
        g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));

        FontMetrics fm = g.getFontMetrics();
        int textX = scaledX + (scaledWidth - fm.stringWidth(text)) / 2;
        int textY = scaledY + ((scaledHeight - fm.getHeight()) / 2) + fm.getAscent();

        // צל קטן לטקסט
        g.setColor(new Color(0, 0, 0, (int) (150 * introAlpha)));
        g.drawString(text, textX + 1, textY + 2);

        // צבע הטקסט פועם קלות יחד עם המסגרת
        int textGlow = (int) (220 + Math.sin(animationTime * 5.0) * 35);
        g.setColor(new Color(255, textGlow, 200, alpha));
        g.drawString(text, textX, textY);
    }

    @Override
    public void handleInput(InputManager input) {
        // חסימת קלט קלה בזמן שהאינטרו המהיר רץ
        if (introAlpha < 0.8) return;

        if (System.currentTimeMillis() - enterLockTimer < 500 && !canPressEnter()) {
            return;
        }

        if (input.E_key && canPressEnter()) {
            game.setScreen(new MainMenuScreen(game, input));
        }
    }
}