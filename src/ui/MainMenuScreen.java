
package ui;

import engine.InputManager;
import main.Game;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class MainMenuScreen extends Screen {

    private Game game;

    private String title;

    private int selectedButton;

    private long enterDelayStartTime;

    // מיקומים (אפשר לשנות לפי הרזולוציה שלכם)
    private int screenWidth = 1280;
    private int screenHeight = 720;

    private int buttonWidth = 420;
    private int buttonHeight = 70;

    private int buttonX;
    private int startButtonY;
    private int explanationButtonY;

    public MainMenuScreen(Game game, InputManager input) {
        super(input);
        this.game = game;
    }

    @Override
    public void onEnter() {
        super.onEnter();
        enterDelayStartTime = System.currentTimeMillis();

        title = "BMDC - survival";
        selectedButton = 0;

        buttonX = (screenWidth - buttonWidth) / 2;
        startButtonY = 300;
        explanationButtonY = startButtonY + 110;
    }

    @Override
    public void update(double deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void render(Graphics2D g) {
        // רקע
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, screenWidth, screenHeight);

        // כותרת
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 52));
        g.drawString(title, 420, 160);

        // כפתור 1: התחל משחק
        drawButton(g,
                "התחל משחק",
                buttonX,
                startButtonY,
                selectedButton == 0
        );

        // כפתור 2: הסבר על המשחק
        drawButton(g,
                "הסבר על המשחק",
                buttonX,
                explanationButtonY,
                selectedButton == 1
        );

        // הוראות
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.LIGHT_GRAY);
        g.drawString("חצים למעלה/למטה, ENTER לבחירה", 500, 650);
    }

    private void drawButton(Graphics2D g, String text, int x, int y, boolean selected) {
        // רקע כפתור
        if (selected) {
            g.setColor(new Color(60, 60, 60));
        } else {
            g.setColor(new Color(30, 30, 30));
        }
        g.fillRoundRect(x, y, buttonWidth, buttonHeight, 20, 20);

        // מסגרת
        if (selected) {
            g.setColor(Color.YELLOW);
        } else {
            g.setColor(Color.WHITE);
        }
        g.drawRoundRect(x, y, buttonWidth, buttonHeight, 20, 20);

        // טקסט
        g.setFont(new Font("Arial", Font.BOLD, 28));
        g.setColor(Color.WHITE);
        g.drawString(text, x + 120, y + 45);
    }

    // הפעולה שמטפלת בקלט
    public void handleInput(InputManager input) {
        // חץ למעלה
        if (input.W_key && selectedButton > 0) {
            selectedButton--;  // רד לכפתור אחד למעלה
        }
        // חץ למטה
        if (input.S_Key && selectedButton < 1) {
            selectedButton++;  // עלה לכפתור אחד למטה
        }
        if (System.currentTimeMillis() - enterDelayStartTime < 500) {
            return;
        }
        if (input.E_key && canPressEnter()) {
            if (selectedButton == 0) {
                game.setScreen(new GameScreen(game, input));
            } else if (selectedButton == 1) {
                game.setScreen(new ExplanationScreen(game, input));
            }
        }
    }
}