package hud;

import engine.InputManager;
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

    // טיימר קטן למניעת דפדוף מהיר מדי (כמו שעשינו בטלפון)
    private double inputTimer = 0;
    private final double INPUT_DELAY = 0.2;

    private double cooldownTimer = 0.0;

    // מידות
    private final int x = 350;
    private final int y = 600;
    private final int width = 580;
    private final int height = 100;

    private final Font textFont = new Font("Arial", Font.BOLD, 18);
    // הוקטן גודל הפונט של האופציות מ-22 ל-16
    private final Font optionFont = new Font("Arial", Font.BOLD, 16);

    public void update(double deltaTime) {
        if (inputTimer > 0) inputTimer -= deltaTime;
        if (cooldownTimer > 0) cooldownTimer -= deltaTime;
    }

    // הפעלת דיאלוג רגיל (אפשר להעביר רשימה של שורות)
    public void startDialogue(List<String> lines) {
        this.pages = lines;
        this.currentPage = 0;
        this.isChoiceMode = false;
        this.visible = true;
        this.finalChoice = -1;
        this.inputTimer = INPUT_DELAY;
    }

    // הפעלת דיאלוג שדורש בחירה בסופו
    public void startDialogueWithChoice(String question, String option1, String option2) {
        this.pages = new ArrayList<>();
        this.pages.add(question);
        this.currentPage = 0;

        this.isChoiceMode = true;
        this.options = new String[]{option1, option2};
        this.selectedOption = 0; // ברירת מחדל: האופציה הראשונה
        this.finalChoice = -1;

        this.visible = true;
        this.inputTimer = INPUT_DELAY;
    }

    public void handleInput(InputManager input) {
        if (!visible || inputTimer > 0) return;

        // אם אנחנו במצב בחירה
        if (isChoiceMode && currentPage == pages.size() - 1) {
            // ניווט ימינה ושמאלה בין האופציות
            if (input.A_key || input.D_key) {
                selectedOption = (selectedOption == 0) ? 1 : 0;
                inputTimer = INPUT_DELAY;
            }
            // אישור בחירה
            if (input.E_key) {
                finalChoice = selectedOption;
                visible = false; // סוגרים את התיבה
                cooldownTimer = 0.5;
                inputTimer = INPUT_DELAY;
            }
        }
        // דיאלוג רגיל (מעבר לעמוד הבא)
        else {
            if (input.E_key) {
                currentPage++;
                if (currentPage >= pages.size()) {
                    visible = false;
                    cooldownTimer = 0.5;
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

        // חישוב גובה זמין לטקסט (אם אנחנו במצב בחירה, נשאיר מקום למטה לאופציות)
        int availableTextHeight = height;
        if (isChoiceMode && currentPage == pages.size() - 1) {
            availableTextHeight = height - 40;
        }

        g.setColor(Color.WHITE);
        g.setFont(textFont);
        drawCenteredMultiLineText(g, currentText, availableTextHeight);

        // ציור אופציות בחירה (אם רלוונטי)
        if (isChoiceMode && currentPage == pages.size() - 1) {
            g.setFont(optionFont);
            FontMetrics optMetrics = g.getFontMetrics(optionFont);
            int optionsY = y + height - 15; // מיקום האופציות בתחתית התיבה

            // הרכבת המחרוזות עם החץ למי שמסומן
            String opt1Text = (selectedOption == 0 ? "> " : "") + options[0];
            String opt2Text = (selectedOption == 1 ? "> " : "") + options[1];

            // אופציה 1 (צד ימין - מיושר לימין התיבה דינמית)
            if (selectedOption == 0) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            int opt1X = x + width - optMetrics.stringWidth(opt1Text) - 30; // 30 פיקסלים מהקיר הימני
            g.drawString(opt1Text, opt1X, optionsY);

            // אופציה 2 (צד שמאל - מיושר לשמאל התיבה דינמית)
            if (selectedOption == 1) g.setColor(Color.YELLOW);
            else g.setColor(Color.WHITE);
            int opt2X = x + 30; // 30 פיקסלים מהקיר השמאלי
            g.drawString(opt2Text, opt2X, optionsY);

        } else {
            // רמז להמשך דיאלוג
            g.setFont(new Font("Arial", Font.BOLD, 12));
            g.setColor(Color.LIGHT_GRAY);
            g.drawString("Press E ->", x + width - 120, y + height - 10);
        }
    }

    // הפעולה המשופרת שלך, עכשיו מקבלת גם availableHeight כדי למרכז נכון
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

        // המרכוז האנכי מחושב לפי availableHeight במקום height
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