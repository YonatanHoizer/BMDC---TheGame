package hud;

import engine.InputManager;
import world.GameWorld;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

public class InteractiveDialogueBox {

    private boolean visible = false;
    private List<String> pages = new ArrayList<>();
    private int currentPage = 0;

    // מערכת בחירות
    private boolean isChoiceMode = false;
    private String[] options;
    private int selectedOption = 0;
    private int finalChoice = -1; // -1 אומר שעוד לא נבחר כלום

    // טיימר קטן למניעת דפדוף מהיר מדי
    private double inputTimer = 0;
    private final double INPUT_DELAY = 0.2;

    private double cooldownTimer = 0.0;

    // מידות
    private final int x = 350;
    private final int y = 550;
    private final int width = 600;
    private final int height = 150;

    private final Font textFont = new Font("Arial", Font.BOLD, 20);
    private final Font optionFont = new Font("Arial", Font.BOLD, 20);

    public void update(double deltaTime) {
        if (inputTimer > 0) inputTimer -= deltaTime;
        if (cooldownTimer > 0) cooldownTimer -= deltaTime;
    }

    public void startDialogue(List<String> lines) {
        this.pages = lines;
        this.currentPage = 0;
        this.isChoiceMode = false;
        this.visible = true;
        this.finalChoice = -1;
        this.inputTimer = INPUT_DELAY;
    }

    public void startDialogueWithChoice(String question, String option1, String option2) {
        this.pages = new ArrayList<>();
        this.pages.add(question);
        this.currentPage = 0;

        this.isChoiceMode = true;
        this.options = new String[]{option1, option2};
        this.selectedOption = 0;
        this.finalChoice = -1;

        this.visible = true;
        this.inputTimer = INPUT_DELAY;
    }

    public void handleInput(InputManager input ,GameWorld world) {
        if (!visible || inputTimer > 0) return;
        if (world.getPlayer().isPhoneOpen()) return;
        // במצב בחירה
        if (isChoiceMode && currentPage == pages.size() - 1) {
            // ניווט ימינה ושמאלה
            if (input.A_key || input.D_key) {
                selectedOption = (selectedOption == 0) ? 1 : 0;
                inputTimer = INPUT_DELAY;
            }
            // אישור בחירה
            if (input.Z_key) {
                finalChoice = selectedOption;
                visible = false;
                cooldownTimer = 0.2;
                inputTimer = INPUT_DELAY;
            }
        }
        // דיאלוג רגיל
        else {
            if (input.Z_key) {
                currentPage++;
                if (currentPage >= pages.size()) {
                    visible = false;
                    cooldownTimer = 0.2;
                }
                inputTimer = INPUT_DELAY;
            }
        }
    }

    public void render(Graphics2D g) {
        if (!visible) return;

        // רקע שחור-שקוף עם מסגרת
        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(x, y, width, height, 15, 15);
        g.setColor(Color.WHITE);
        g.drawRoundRect(x, y, width, height, 15, 15);

        String currentText = pages.get(currentPage);

        // אם אנחנו במסך בחירה, שומרים אזור קבוע של 60 פיקסלים בתחתית לאופציות
        int availableTextHeight = height;
        if (isChoiceMode && currentPage == pages.size() - 1) {
            availableTextHeight = height - 60;
        }

        g.setColor(Color.WHITE);
        g.setFont(textFont);
        drawCenteredMultiLineText(g, currentText, availableTextHeight);

        // ציור אופציות
        if (isChoiceMode && currentPage == pages.size() - 1) {
            g.setFont(optionFont);

            // אנחנו מחלקים את תחתית התיבה ל-2 אזורים וירטואליים
            int boxHeight = 60; // גובה אזור האופציות
            int boxY = y + height - boxHeight; // נקודת התחלה אנכית של אזור האופציות
            int halfWidth = (width / 2) - 20; // רוחב מקסימלי לאופציה (חצי מהחלון פחות שוליים)

            // אופציה 1 (צד ימין - אינדקס 0)
            String opt1Text = (selectedOption == 0 ? "> " : "") + options[0];
            if (selectedOption == 0) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            int opt1BoxX = x + (width / 2) + 10; // מתחיל מהאמצע וימינה
            drawDynamicBlockOption(g, opt1Text, opt1BoxX, boxY, halfWidth, boxHeight);

            // אופציה 2 (צד שמאל - אינדקס 1)
            String opt2Text = (selectedOption == 1 ? "> " : "") + options[1];
            if (selectedOption == 1) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            int opt2BoxX = x + 10; // מתחיל מהצד השמאלי ועד האמצע
            drawDynamicBlockOption(g, opt2Text, opt2BoxX, boxY, halfWidth, boxHeight);

        } else {
            // רמז להמשך
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Press Z ->", x + width - 120, y + height - 10);
        }
    }

    /**
     * פונקציה חדשה וחכמה לציור אופציות.
     * במקום ליישר כל שורה בנפרד, היא מחשבת את "בלוק" הטקסט, ממקמת אותו במרכז האזור המותר לו,
     * ומתחילה את כל השורות מאותה נקודת X, כך שהשורה השנייה תמיד תהיה בדיוק מתחת לראשונה (ומתחת לחץ הבחירה).
     */
    private void drawDynamicBlockOption(Graphics2D g, String text, int boxX, int boxY, int boxWidth, int boxHeight) {
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();

        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        // חיתוך המילים לשורות לפי רוחב הקופסה המותר
        for (String word : words) {
            if (metrics.stringWidth(currentLine + word) <= boxWidth) {
                currentLine.append(word).append(" ");
            } else {
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word + " ");
            }
        }
        lines.add(currentLine.toString().trim());

        // מציאת השורה הכי ארוכה בתוך הבלוק הזה ספציפית
        int actualBlockWidth = 0;
        for (String line : lines) {
            int w = metrics.stringWidth(line);
            if (w > actualBlockWidth) {
                actualBlockWidth = w;
            }
        }

        // חישוב הגובה הכולל של בלוק הטקסט (כמה שורות שנוצרו)
        int blockHeight = lines.size() * lineHeight;

        // חישוב נקודות ההתחלה כדי למרכז את הבלוק כולו במרכז התיבה המוקצית לו!
        int startX = boxX + (boxWidth - actualBlockWidth) / 2;
        int startY = boxY + (boxHeight - blockHeight) / 2 + metrics.getAscent();

        // ציור כל השורות מאותו startX בדיוק - זה מונע את הבעיה של שורה בורחת
        for (String line : lines) {
            g.drawString(line, startX, startY);
            startY += lineHeight;
        }
    }

    // ציור הודעות הדיאלוג (השאלה עצמה)
    private void drawCenteredMultiLineText(Graphics2D g, String text, int availableHeight) {
        FontMetrics metrics = g.getFontMetrics(textFont);
        int lineHeight = metrics.getHeight();

        java.util.List<String> finalLines = new java.util.ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (metrics.stringWidth(currentLine + word) < width - 40) {
                    currentLine.append(word).append(" ");
                } else {
                    finalLines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                }
            }
            finalLines.add(currentLine.toString().trim());
        }

        int totalHeight = finalLines.size() * lineHeight;
        int startY = y + ((availableHeight - totalHeight) / 2) + metrics.getAscent();

        for (String line : finalLines) {
            int lineWidth = metrics.stringWidth(line);
            int startX = x + (width - lineWidth) / 2;

            g.drawString(line, startX, startY);
            startY += lineHeight;
        }
    }

    public boolean isVisible() { return visible; }
    public int getFinalChoice() { return finalChoice; }
    public void resetChoice() { finalChoice = -1; }
    public boolean isReady() {
        return !visible && cooldownTimer <= 0;
    }
}