package ui;

import engine.InputManager;
import main.Game;

import java.awt.*;

public class victoryScreen extends Screen {

    private Game game;

    private int screenWidth = 1280;
    private int screenHeight = 720;

    private int buttonWidth = 420;
    private int buttonHeight = 70;
    private int buttonX;
    private int buttonY;

    public victoryScreen(Game game, InputManager input) {
        super(input);
        this.game = game;
    }

    @Override
    public void onEnter() {
        super.onEnter();
        buttonX = (screenWidth - buttonWidth) / 2;
        buttonY = 550; // מיקום הכפתור בחלק התחתון של המסך
    }

    @Override
    public void render(Graphics2D g) {
        // רקע כחול כהה/סגול כדי לשדר "הצלחה ויוקרה"
        g.setColor(new Color(10, 20, 50));
        g.fillRect(0, 0, screenWidth, screenHeight);

        // כותרת גדולה "ניצחון!"
        g.setColor(new Color(255, 215, 0)); // צבע זהב (Gold)
        g.setFont(new Font("Arial", Font.BOLD, 100));
        String title = "ניצחת!";
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleX = (screenWidth - titleMetrics.stringWidth(title)) / 2;
        g.drawString(title, titleX, 200);

        // הודעת הניצחון (אפשר לחלק לשתי שורות אם צריך)
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 36));
        String subTitle1 = "כל הכבוד! שרדת יום שלם בישיבה";
        String subTitle2 = "בלי להפקיד את הטלפון שלך.";

        FontMetrics msgMetrics = g.getFontMetrics();
        g.drawString(subTitle1, (screenWidth - msgMetrics.stringWidth(subTitle1)) / 2, 350);
        g.drawString(subTitle2, (screenWidth - msgMetrics.stringWidth(subTitle2)) / 2, 400);

        // ציור הכפתור לחזרה (מוגדר כ-true כי הוא תמיד מסומן)
        drawButton(g, "חזור לתפריט הראשי", buttonX, buttonY, true);
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

        if (input.ENTER_key && canPressEnter()) {
            game.setScreen(new MainMenuScreen(game, input));
        }
    }
}