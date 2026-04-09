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
    // מיקום וגודל - נשארו בדיוק כפי שביקשת
    private final int phoneIconX = 1175;
    private final int phoneIconY = 590;
    private final int phoneIconSize = 128;

    private final int phoneWindowX = 780;
    private final int phoneWindowY = 480;
    private final int phoneWindowWidth = 400;
    private final int phoneWindowHeight = 220;

    private double inputTimer = 0;
    private final double INPUT_DELAY = 0.2;

    public PhoneUI(Player player) {
        this.player = player;
        try {
            phoneIcon = ImageIO.read(getClass().getResource("/images/phon.png"));
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

        if (inputTimer > 0) return;

        List<PhoneMessage> messages = player.getPhoneMessages();
        if (messages.isEmpty()) return;

        boolean actionTaken = false;

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

        if (input.F_key) {
            messages.remove(selectedMessageIndex);
            if (selectedMessageIndex >= messages.size() && !messages.isEmpty()) {
                selectedMessageIndex = messages.size() - 1;
            }
            actionTaken = true;
        }

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

            int badgeX = (phoneIconX + phoneIconSize) - badgeWidth - 30;
            int badgeY = phoneIconY - 5;

            g.setColor(Color.WHITE);
            g.fillRoundRect(badgeX - 2, badgeY - 2, badgeWidth + 4, badgeHeight + 4, 24, 24);

            g.setColor(Color.RED);
            g.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 22, 22);

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

        // מסגרת עדינה מסביב לטלפון
        g.setColor(new Color(80, 80, 80));
        g.drawRoundRect(phoneWindowX, phoneWindowY, phoneWindowWidth, phoneWindowHeight, 20, 20);

        // כותרת "הודעות" - ממורכזת לאמצע החלון
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        FontMetrics fmTitle = g.getFontMetrics();
        int titleX = phoneWindowX + (phoneWindowWidth - fmTitle.stringWidth("הודעות")) / 2;
        g.drawString("הודעות", titleX, phoneWindowY + 30);

        // קו הפרדה עליון
        g.setColor(new Color(150, 150, 150, 100));
        g.drawLine(phoneWindowX + 20, phoneWindowY + 45, phoneWindowX + phoneWindowWidth - 20, phoneWindowY + 45);

        if (!messages.isEmpty()) {
            if (selectedMessageIndex >= messages.size()) selectedMessageIndex = messages.size() - 1;

            PhoneMessage msg = messages.get(selectedMessageIndex);

            // חיווי על מיקום ברשימה - מצד ימין למעלה
            g.setFont(new Font("Arial", Font.ITALIC, 14));
            g.setColor(Color.LIGHT_GRAY);
            String counterText = (selectedMessageIndex + 1) + " / " + messages.size();
            FontMetrics fmCounter = g.getFontMetrics();
            g.drawString(counterText, phoneWindowX + phoneWindowWidth - fmCounter.stringWidth(counterText) - 20, phoneWindowY + 30);

            // שם השולח - מודגש וממורכז לאמצע!
            g.setFont(new Font("Arial", Font.BOLD, 16));
            g.setColor(Color.CYAN);
            FontMetrics fmSender = g.getFontMetrics();
            int senderX = phoneWindowX + (phoneWindowWidth - fmSender.stringWidth(msg.sender)) / 2;
            g.drawString(msg.sender, senderX, phoneWindowY + 75);

            // תוכן ההודעה
            g.setFont(new Font("Arial", Font.PLAIN, 15));
            g.setColor(Color.WHITE);
            drawCenteredMultiLineText(g, msg.text, phoneWindowY + 110);

            // כיתוב הדרכה קטן בתחתית (ממורכז)
            g.setFont(new Font("Arial", Font.PLAIN, 12));
            g.setColor(Color.GRAY);
            String helpText = "לחץ C למחיקה";
            FontMetrics fmHelp = g.getFontMetrics();
            int helpX = phoneWindowX + (phoneWindowWidth - fmHelp.stringWidth(helpText)) / 2;
            g.drawString(helpText, helpX, phoneWindowY + phoneWindowHeight - 15);

        } else {
            // טקסט מצב ריק - ממורכז גם אופקית וגם אנכית למרכז הטלפון
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.setColor(Color.LIGHT_GRAY);
            String emptyText = "אין הודעות חדשות";
            FontMetrics fmEmpty = g.getFontMetrics();
            int emptyX = phoneWindowX + (phoneWindowWidth - fmEmpty.stringWidth(emptyText)) / 2;
            // חישוב המרכז האנכי (חצי מגובה החלון + חצי מגובה הפונט)
            int emptyY = phoneWindowY + (phoneWindowHeight / 2) + (fmEmpty.getAscent() / 2);
            g.drawString(emptyText, emptyX, emptyY);
        }
    }

    private void drawCenteredMultiLineText(Graphics2D g, String text, int startY) {
        FontMetrics metrics = g.getFontMetrics();
        // הוספתי 5 פיקסלים למרווח השורות (Line Spacing) כדי שהטקסט ינשום טוב יותר
        int lineHeight = metrics.getHeight() + 5;

        List<String> finalLines = new ArrayList<>();
        String[] paragraphs = text.split("\n");

        for (String paragraph : paragraphs) {
            String[] words = paragraph.split(" ");
            StringBuilder currentLine = new StringBuilder();

            for (String word : words) {
                if (metrics.stringWidth(currentLine + word) < phoneWindowWidth - 40) { // הגדלתי את השוליים משני הצדדים ל-40
                    currentLine.append(word).append(" ");
                } else {
                    finalLines.add(currentLine.toString().trim());
                    currentLine = new StringBuilder(word + " ");
                }
            }
            finalLines.add(currentLine.toString().trim());
        }

        // ציור כל שורה ממורכזת
        for (String line : finalLines) {
            int lineWidth = metrics.stringWidth(line);
            int startX = phoneWindowX + (phoneWindowWidth - lineWidth) / 2;
            g.drawString(line, startX, startY);
            startY += lineHeight;
        }
    }

    public boolean isViewingMessageContaining(String keyword) {
        if (!player.isPhoneOpen()) return false;

        List<PhoneMessage> messages = player.getPhoneMessages();
        if (messages.isEmpty()) return false;

        if (selectedMessageIndex >= messages.size()) {
            selectedMessageIndex = Math.max(0, messages.size() - 1);
        }

        PhoneMessage currentMsg = messages.get(selectedMessageIndex);
        return currentMsg.text.contains(keyword);
    }
}