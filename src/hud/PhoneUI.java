package hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import engine.InputManager;
import entities.Player;

public class PhoneUI {

    private Player player;
    private BufferedImage phoneIcon;
    private int selectedMessageIndex = 0;
    // מיקום וגודל
    private final int phoneIconX = 1175;
    private final int phoneIconY = 590;
    private final int phoneIconSize = 128;

    private final int phoneWindowX = 880;
    private final int phoneWindowY = 550;
    private final int phoneWindowWidth = 300;
    private final int phoneWindowHeight = 150;

    private double inputTimer = 0;
    private final double INPUT_DELAY = 0.2;

    public PhoneUI(Player player) {
        this.player = player;
        try {
            phoneIcon = ImageIO.read(getClass().getResource("/hud/phon.png"));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    public void update(double deltaTime) {
        if (inputTimer > 0) {
            inputTimer -= deltaTime;
        }
    }

    public void handleInput(InputManager input) {
        if (!player.isPhoneOpen()) return;

        // אם הטיימר עדיין פעיל, אנחנו לא עושים כלום (חוסם דפדוף מהיר)
        if (inputTimer > 0) return;

        List<PhoneMessage> messages = player.getPhoneMessages();
        if (messages.isEmpty()) return;

        boolean actionTaken = false; // משתנה עזר כדי לדעת אם בוצעה פעולה

        if (input.W_key) {
            selectedMessageIndex--;
            if (selectedMessageIndex < 0) selectedMessageIndex = 0;
            actionTaken = true;
        }

        if (input.S_Key) {
            selectedMessageIndex++;
            if (selectedMessageIndex >= messages.size()) {
                selectedMessageIndex = messages.size() - 1;
            }
            actionTaken = true;
        }

        if (input.SPACE_key) {
            messages.remove(selectedMessageIndex);
            // תיקון קריטי: אם מחקנו את ההודעה האחרונה, נזוז אחד אחורה
            if (selectedMessageIndex >= messages.size() && !messages.isEmpty()) {
                selectedMessageIndex = messages.size() - 1;
            }
            actionTaken = true;
        }

        // אם בוצעה פעולה (דפדוף או מחיקה), נפעיל את הטיימר
        if (actionTaken) {
            inputTimer = INPUT_DELAY;
        }
    }

    public void render(Graphics2D g) {
        drawPhoneIcon(g);

        if (player.isPhoneOpen()) {
            drawPhoneWindow(g);
        }
    }

    private void drawPhoneIcon(Graphics2D g) {
        if (phoneIcon != null) {
            g.drawImage(phoneIcon, phoneIconX, phoneIconY, phoneIconSize, phoneIconSize, null);
        }

        int countToShow = player.getPhoneMessages().size();
        if (countToShow > 0) {
            String countText = String.valueOf(countToShow);
            g.setFont(new Font("Arial", Font.BOLD, 14));
            FontMetrics fm = g.getFontMetrics();

            int textWidth = fm.stringWidth(countText);
            int badgeWidth = Math.max(22, textWidth + 10);
            int badgeHeight = 22;

            // המיקום המנצח:
            int badgeX = (phoneIconX + phoneIconSize) - badgeWidth - 30;
            int badgeY = phoneIconY - 5;

            // אופציונלי: מסגרת לבנה קטנה כדי להפריד את האדום מהרקע
            g.setColor(Color.WHITE);
            g.fillRoundRect(badgeX - 2, badgeY - 2, badgeWidth + 4, badgeHeight + 4, 24, 24);

            // הבועה האדומה
            g.setColor(Color.RED);
            g.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 22, 22);

            // המספר
            g.setColor(Color.WHITE);
            int textX = badgeX + (badgeWidth - textWidth) / 2;
            int textY = badgeY + ((badgeHeight - fm.getHeight()) / 2) + fm.getAscent();
            g.drawString(countText, textX, textY);
        }
    }

    private void drawPhoneWindow(Graphics2D g) {
        List<PhoneMessage> messages = player.getPhoneMessages();

        // רקע חלון הטלפון
        g.setColor(new Color(20, 20, 20, 230));
        g.fillRoundRect(phoneWindowX, phoneWindowY, phoneWindowWidth, phoneWindowHeight, 20, 20);

        // כותרת
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("הודעות", phoneWindowX + 20, phoneWindowY + 30);

        if (!messages.isEmpty()) {
            if (selectedMessageIndex >= messages.size()) selectedMessageIndex = messages.size() - 1;

            PhoneMessage msg = messages.get(selectedMessageIndex);

            // שולח (מיושר שמאלה כדי לשמור על עיצוב של טלפון)
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.CYAN);
            g.drawString(msg.sender, phoneWindowX + 20, phoneWindowY + 60); // הרמתי קצת כדי לתת מקום לטקסט

            // תוכן ההודעה (משתמש בפונקציה המשופרת)
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.setColor(Color.WHITE);
            drawCenteredMultiLineText(g, msg.text, phoneWindowY + 80); // מתחיל קצת מתחת לשולח

            // חיווי על מיקום ברשימה
            g.setFont(new Font("Arial", Font.ITALIC, 12));
            g.drawString((selectedMessageIndex + 1) + " / " + messages.size(), phoneWindowX + phoneWindowWidth - 60, phoneWindowY + 30);
        } else {
            g.setFont(new Font("Arial", Font.PLAIN, 14));
            g.drawString("אין הודעות חדשות", phoneWindowX + 20, phoneWindowY + 80);
        }
    }

    // הפונקציה המשופרת שתומכת ב-\n, ירידת שורות אוטומטית ומרכוז אופקי
    private void drawCenteredMultiLineText(Graphics2D g, String text, int startY) {
        FontMetrics metrics = g.getFontMetrics();
        int lineHeight = metrics.getHeight();

        List<String> finalLines = new ArrayList<>();

        // פיצול לפי \n ידני שאתה כותב בהודעה
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                // נשמור על שוליים פנימיים כדי שהטקסט לא ייגע בקצוות הטלפון (רוחב הטלפון פחות 20)
                if (metrics.stringWidth(currentLine + word) < phoneWindowWidth - 20) {
                    currentLine.append(word).append(" ");
                } else {
                    finalLines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                }
            }
            finalLines.add(currentLine.toString().trim());
        }

        // ציור כל שורה ממורכזת לאמצע חלון הטלפון
        for (String line : finalLines) {
            int lineWidth = metrics.stringWidth(line);
            // חישוב המרכז של הטלפון
            int startX = phoneWindowX + (phoneWindowWidth - lineWidth) / 2;

            g.drawString(line, startX, startY);
            startY += lineHeight;
        }
    }

    public boolean isViewingMessageContaining(String keyword) {
        // אם הטלפון סגור, בטוח לא רואים את ההודעה
        if (!player.isPhoneOpen()) return false;

        List<PhoneMessage> messages = player.getPhoneMessages();
        if (messages.isEmpty()) return false;

        // הגנה מחריגת אינדקס (במקרה שנמחקו הודעות)
        if (selectedMessageIndex >= messages.size()) {
            selectedMessageIndex = Math.max(0, messages.size() - 1);
        }

        // שולפים את ההודעה שהשחקן קורא ממש עכשיו
        PhoneMessage currentMsg = messages.get(selectedMessageIndex);

        // בודקים אם התוכן שלה מכיל את מילת המפתח שחיפשנו
        return currentMsg.text.contains(keyword);
    }
}