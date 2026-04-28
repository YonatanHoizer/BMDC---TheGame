package ui;

import engine.InputManager;
import main.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;

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

    public GameOverScreen(Game game, InputManager input, int failId) {
        super(input);
        this.game = game;
        this.failId = failId;
        setFailMessage();
    }

    private void setFailMessage() {
        // כאן אתה מגדיר את סוגי הפסילות שלך
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
        buttonY = 520; // הורדתי את הכפתור קצת למטה (מ-500 ל-520) כדי לתת מקום להודעות בעלות 2 שורות
    }

    @Override
    public void render(Graphics2D g) {
        // רקע אדום כהה כדי לשדר "פסילה"
        g.setColor(new Color(60, 0, 0));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // כותרת גדולה "פסילה"
        g.setColor(Color.RED);
        g.setFont(new Font("Arial", Font.BOLD, 80));
        String title = "נפסלת!";
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g.drawString(title, titleX, 200);

        // ציור הודעת הפסילה הספציפית - עכשיו תומך בירידת שורות!
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 32));
        FontMetrics msgMetrics = g.getFontMetrics();

        // חותכים את ההודעה למערך של שורות בכל פעם שיש \n
        String[] lines = failMessage.split("\n");
        int startY = 330; // נקודת ההתחלה של הטקסט
        int lineHeight = msgMetrics.getHeight() + 10; // מרווח בין השורות

        for (String line : lines) {
            String trimmedLine = line.trim(); // מוריד רווחים מיותרים בתחילת/סוף השורה שנוצרו בגלל הפיצול
            int msgX = (screenWidth - msgMetrics.stringWidth(trimmedLine)) / 2;
            g.drawString(trimmedLine, msgX, startY);
            startY += lineHeight; // יורד שורה עבור הטקסט הבא
        }

        // ציור הכפתור לחזרה (מוגדר כ-true כי הוא תמיד מסומן - יש רק כפתור אחד)
        drawButton(g, "לחץ Enter לחזרה לתפריט הראשי", buttonX, buttonY, true);
    }

    private void drawButton(Graphics2D g, String text, int x, int y, boolean selected) {
        if (selected) g.setColor(new Color(60, 60, 60));
        else g.setColor(new Color(30, 30, 30));

        g.fillRoundRect(x, y, buttonWidth, buttonHeight, 20, 20);

        if (selected) g.setColor(Color.YELLOW);
        else g.setColor(Color.WHITE);

        g.drawRoundRect(x, y, buttonWidth, buttonHeight, 20, 20);

        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);

        // מרכוז הטקסט בתוך הכפתור
        FontMetrics fm = g.getFontMetrics();
        int textX = x + (buttonWidth - fm.stringWidth(text)) / 2;
        int textY = y + ((buttonHeight - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(text, textX, textY);
    }

    @Override
    public void handleInput(InputManager input) {
        if (System.currentTimeMillis() - enterLockTimer < 500 && !canPressEnter()) {
            return;
        }

        // כשיש רק כפתור אחד, רק מחכים לאנטר (או במקרה שלך E)
        if (input.E_key && canPressEnter()) {
            game.setScreen(new MainMenuScreen(game, input));
        }
    }
}