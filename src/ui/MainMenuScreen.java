package ui;

import engine.InputManager;
import util.DatabaseManager; // ייבוא של מנהל מסד הנתונים
import main.Game;
import util.SaveData; // ייבוא אובייקט השמירה

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.AffineTransform;
import java.util.Random;

public class MainMenuScreen extends Screen {

    private Game game;
    private String title;
    private int selectedButton;
    private long enterDelayStartTime;

    // מיקומים
    private int screenWidth = 1280;
    private int screenHeight = 720;
    private int buttonWidth = 420;
    private int buttonHeight = 70;

    private int buttonX;
    private int startButtonY;
    private int loadButtonY;        // מיקום ה-Y של הכפתור החדש
    private int explanationButtonY;

    // משתנה בוליאני לבדיקה האם קיימת שמירה בדאטה בייס
    private boolean hasSaveGame = false;
    private SaveData loadedSaveData = null; // שמירת המידע הטעון בזיכרון זמני

    // פלטת צבעים
    private static final Color COLOR_BG_CENTER = new Color(30, 40, 55);
    private static final Color COLOR_BG_EDGE = new Color(12, 16, 22);
    private static final Color COLOR_TITLE = new Color(100, 210, 255);
    private static final Color COLOR_BTN_BG_SEL = new Color(50, 55, 65);
    private static final Color COLOR_BTN_BG_UNSEL = new Color(30, 32, 38);
    private static final Color COLOR_BORDER_SEL = new Color(255, 198, 93);
    private static final Color COLOR_TEXT_UNSEL = new Color(180, 190, 200);
    private static final Color COLOR_BOTTOM_TEXT = new Color(140, 150, 165);

    // צבעים ייעודיים לכפתור מנוטרל (Disabled)
    private static final Color COLOR_BTN_DISABLED = new Color(20, 22, 25);
    private static final Color COLOR_TEXT_DISABLED = new Color(80, 90, 100);

    // מערכת משתני אנימציה (הוספת משתנה סקייל לכפתור השלישי)
    private double animationTime = 0;
    private double button0Scale = 0.0;
    private double button1Scale = 0.0;
    private double button2Scale = 0.0; // סקייל עבור הכפתור השלישי
    private double introAlpha = 0.0;
    private static final double ANIMATION_SPEED = 6.0;

    // משתנה דגל למניעת טיסה של הסמן ודיפדוף מהיר מדי
    private boolean menuSelectionKeyReleased = true;

    // מערכת חלקיקים
    private float[] particleX = new float[25];
    private float[] particleY = new float[25];
    private float[] particleSpeed = new float[25];
    private float[] particleSize = new float[25];

    public MainMenuScreen(Game game, InputManager input) {
        super(input);
        this.game = game;

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

        title = "BMDC - survival";
        selectedButton = 0; // ברירת מחדל: התחל משחק חדש

        // בדיקה דינמית מול ה-Docker MySQL האם יש קובץ שמירה קיים
        loadedSaveData = DatabaseManager.loadGame();
        if (loadedSaveData != null) {
            hasSaveGame = true;
            selectedButton = 0;
        } else {
            hasSaveGame = false;
        }

        buttonX = (screenWidth - buttonWidth) / 2;
        startButtonY = 280;
        loadButtonY = startButtonY + 90;
        explanationButtonY = loadButtonY + 90;

        animationTime = 0;
        button0Scale = 0.5;
        button1Scale = 0.5;
        button2Scale = 0.5;
        introAlpha = 0.0;
        menuSelectionKeyReleased = true; // איתחול נעילת המקשים בכל כניסה למסך
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);

        animationTime += deltaTime;

        introAlpha += (1.0 - introAlpha) * 4.0 * deltaTime;
        if (introAlpha > 1.0) introAlpha = 1.0;

        for (int i = 0; i < particleX.length; i++) {
            particleY[i] -= particleSpeed[i] * deltaTime;
            particleX[i] += Math.sin(animationTime + i) * 10 * deltaTime;

            if (particleY[i] < -10) {
                particleY[i] = screenHeight + 10;
                particleX[i] = (float) (Math.random() * screenWidth);
            }
        }

        // חישוב גודל היעד לכל 3 הכפתורים באופן אינדיבידואלי
        double targetScale0 = (selectedButton == 0) ? 1.08 : 1.0;
        double targetScale1 = (selectedButton == 1) ? 1.08 : 1.0;
        double targetScale2 = (selectedButton == 2) ? 1.08 : 1.0;

        // אינטרפולציה חלקה (Lerp)
        button0Scale += (targetScale0 - button0Scale) * ANIMATION_SPEED * deltaTime;
        button1Scale += (targetScale1 - button1Scale) * ANIMATION_SPEED * deltaTime;
        button2Scale += (targetScale2 - button2Scale) * ANIMATION_SPEED * deltaTime;
    }

    @Override
    public void render(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Point2D center = new Point2D.Float(screenWidth / 2.0f, screenHeight / 2.0f);
        float radius = (float) Math.sqrt(screenWidth * screenWidth + screenHeight * screenHeight) / 2.0f;
        float[] dist = {0.0f, 1.0f};
        Color[] colors = {COLOR_BG_CENTER, COLOR_BG_EDGE};
        RadialGradientPaint gradient = new RadialGradientPaint(center, radius, dist, colors);
        g.setPaint(gradient);
        g.fillRect(0, 0, screenWidth, screenHeight);

        g.setColor(new Color(100, 210, 255, (int) (60 * introAlpha)));
        for (int i = 0; i < particleX.length; i++) {
            g.fillOval((int) particleX[i], (int) particleY[i], (int) particleSize[i], (int) particleSize[i]);
        }

        double titleOffset = Math.sin(animationTime * 1.8) * 12;
        int baseTitleY = 140;
        int animatedTitleY = (int) (baseTitleY + titleOffset - (1.0 - introAlpha) * 50);

        Font titleFont = new Font("Segoe UI", Font.BOLD, 68);
        g.setFont(titleFont);
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;

        g.setColor(new Color(0, 0, 0, (int) (180 * introAlpha)));
        g.drawString(title, titleX + 4, animatedTitleY + 6);

        g.setColor(new Color(100, 210, 255, (int) (40 * introAlpha)));
        g.drawString(title, titleX - 2, animatedTitleY - 2);

        g.setColor(new Color(COLOR_TITLE.getRed(), COLOR_TITLE.getGreen(), COLOR_TITLE.getBlue(), (int) (255 * introAlpha)));
        g.drawString(title, titleX, animatedTitleY);

        int introButtonOffset = (int) ((1.0 - introAlpha) * 40);

        // כפתור 0: התחל משחק (חדש) - תמיד פעיל
        drawButton(g, "משחק חדש", buttonX, startButtonY + introButtonOffset, selectedButton == 0, button0Scale, true);

        // כפתור 1: המשך משחק - מושפע ישירות מהדגל האם קיימת שמירה (hasSaveGame)
        drawButton(g, "המשך משחק", buttonX, loadButtonY + introButtonOffset, selectedButton == 1, button1Scale, hasSaveGame);

        // כפתור 2: הסבר על המשחק - תמיד פעיל
        drawButton(g, "הסבר על המשחק", buttonX, explanationButtonY + introButtonOffset, selectedButton == 2, button2Scale, true);

        int bottomAlpha = (int) ((150 + Math.sin(animationTime * 2.5) * 105) * introAlpha);
        bottomAlpha = Math.max(0, Math.min(255, bottomAlpha));
        Color animatedBottomColor = new Color(COLOR_BOTTOM_TEXT.getRed(), COLOR_BOTTOM_TEXT.getGreen(), COLOR_BOTTOM_TEXT.getBlue(), bottomAlpha);

        Font bottomFont = new Font("Segoe UI", Font.PLAIN, 22);
        g.setFont(bottomFont);
        g.setColor(animatedBottomColor);
        FontMetrics bottomMetrics = g.getFontMetrics();
        int bottomX = (screenWidth - bottomMetrics.stringWidth("חצים למעלה/למטה, ENTER לבחירה")) / 2;
        g.drawString("חצים למעלה/למטה, ENTER לבחירה", bottomX, 660);
    }

    private void drawButton(Graphics2D g, String text, int x, int y, boolean selected, double scale, boolean enabled) {
        int scaledWidth = (int) (buttonWidth * scale);
        int scaledHeight = (int) (buttonHeight * scale);
        int scaledX = x - (scaledWidth - buttonWidth) / 2;
        int scaledY = y - (scaledHeight - buttonHeight) / 2;

        int alpha = (int) (255 * introAlpha);

        if (selected && enabled) {
            g.setColor(new Color(255, 198, 93, (int) (40 * introAlpha)));
            g.fillRoundRect(scaledX - 6, scaledY - 6, scaledWidth + 12, scaledHeight + 12, 28, 28);
        }

        if (!enabled) {
            g.setColor(new Color(COLOR_BTN_DISABLED.getRed(), COLOR_BTN_DISABLED.getGreen(), COLOR_BTN_DISABLED.getBlue(), (int) (120 * introAlpha)));
        } else if (selected) {
            g.setColor(new Color(COLOR_BTN_BG_SEL.getRed(), COLOR_BTN_BG_SEL.getGreen(), COLOR_BTN_BG_SEL.getBlue(), alpha));
        } else {
            g.setColor(new Color(COLOR_BTN_BG_UNSEL.getRed(), COLOR_BTN_BG_UNSEL.getGreen(), COLOR_BTN_BG_UNSEL.getBlue(), (int) (180 * introAlpha)));
        }
        g.fillRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);

        if (!enabled) {
            g.setColor(new Color(60, 65, 70, (int) (50 * introAlpha)));
            g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);
        } else if (selected) {
            int pulseAlpha = (int) ((200 + Math.sin(animationTime * 6.0) * 55) * introAlpha);
            pulseAlpha = Math.max(0, Math.min(255, pulseAlpha));
            g.setColor(new Color(COLOR_BORDER_SEL.getRed(), COLOR_BORDER_SEL.getGreen(), COLOR_BORDER_SEL.getBlue(), pulseAlpha));
            g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);

            g.setColor(new Color(255, 255, 255, (int) (80 * introAlpha)));
            g.drawRoundRect(scaledX + 1, scaledY + 1, scaledWidth - 2, scaledHeight - 2, 22, 22);
        } else {
            g.setColor(new Color(100, 110, 120, (int) (70 * introAlpha)));
            g.drawRoundRect(scaledX, scaledY, scaledWidth, scaledHeight, 24, 24);
        }

        int fontSize = (int) (26 * scale);
        g.setFont(new Font("Segoe UI", Font.BOLD, fontSize));

        FontMetrics fm = g.getFontMetrics();
        int textX = scaledX + (scaledWidth - fm.stringWidth(text)) / 2;
        int textY = scaledY + ((scaledHeight - fm.getHeight()) / 2) + fm.getAscent();

        g.setColor(new Color(0, 0, 0, (int) (120 * introAlpha)));
        g.drawString(text, textX + 1, textY + 2);

        if (!enabled) {
            g.setColor(new Color(COLOR_TEXT_DISABLED.getRed(), COLOR_TEXT_DISABLED.getGreen(), COLOR_TEXT_DISABLED.getBlue(), alpha));
        } else if (selected) {
            int textGlow = (int) (210 + Math.sin(animationTime * 6.0) * 45);
            g.setColor(new Color(255, textGlow, 180, alpha));
        } else {
            g.setColor(new Color(COLOR_TEXT_UNSEL.getRed(), COLOR_TEXT_UNSEL.getGreen(), COLOR_TEXT_UNSEL.getBlue(), alpha));
        }
        g.drawString(text, textX, textY);
    }

    @Override
    public void handleInput(InputManager input) {
        if (introAlpha < 0.8) return;

        // ניהול ניווט חץ למעלה (W) עם מנגנון נעילה
        if (input.W_key) {
            if (menuSelectionKeyReleased) {
                if (selectedButton > 0) {
                    selectedButton--;
                } else {
                    selectedButton = 2; // מעבר מחזורי לסוף
                }

                // לוגיקה משפרת חווית משתמש: אם הגענו לכפתור "המשך משחק" והוא מנוטרל, נדלג תחנה נוספת למעלה
                if (selectedButton == 1 && !hasSaveGame) {
                    selectedButton = 0;
                }

                menuSelectionKeyReleased = false; // נעילת הקלט עד לשחרור המקש
            }
        }
        // ניהול ניווט חץ למטה (S) עם מנגנון נעילה
        else if (input.S_Key) {
            if (menuSelectionKeyReleased) {
                if (selectedButton < 2) {
                    selectedButton++;
                } else {
                    selectedButton = 0; // חזרה מחזורית להתחלה
                }

                // לוגיקה משפרת חווית משתמש: אם הגענו לכפתור "המשך משחק" והוא מנוטרל, נדלג תחנה נוספת למטה
                if (selectedButton == 1 && !hasSaveGame) {
                    selectedButton = 2;
                }

                menuSelectionKeyReleased = false; // נעילת הקלט עד לשחרור המקש
            }
        }
        // שחרור הנעילה ברגע שאין שום מקש תנועה לחוץ
        else {
            menuSelectionKeyReleased = true;
        }

        if (System.currentTimeMillis() - enterDelayStartTime < 500) {
            return;
        }

        // לחיצה על מקש ה-ENTER
        if (input.E_key && canPressEnter()) {
            if (selectedButton == 0) {
                game.setScreen(new GameScreen(game, input));
            }
            else if (selectedButton == 1 && hasSaveGame && loadedSaveData != null) {
                game.setScreen(new GameScreen(game, input, loadedSaveData));
            }
            else if (selectedButton == 2) {
                game.setScreen(new ExplanationScreen(game, input));
            }
        }
    }
}