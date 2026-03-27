package entities;

import engine.AudioManager;
import engine.InputManager;
import hud.PhoneMessage;
import ui.Screen;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class Player extends MovableEntity {

    // מאפיינים ייחודיים לשחקן בלבד
    private boolean phoneOpen = false;
    private List<PhoneMessage> phoneMessages;
    private int unreadCount = 0;
    private boolean inDialogue = false;
    private AudioManager audio;
    private boolean isWalkingSoundPlaying = false;

    public Player(float x, float y) {
        super(x, y, 64, 64);

        this.phoneMessages = new ArrayList<>();
        this.speed = 350.0f;
        this.animationSpeed = 10;

        loadAnimations();
    }

    private void loadAnimations() {
        try {
            // טעינת הקבצים המקוריים שלך
            BufferedImage frontSheet = ImageIO.read(getClass().getResourceAsStream("/images/רגיל קדימה.png"));
            BufferedImage backSheet  = ImageIO.read(getClass().getResourceAsStream("/images/רגיל אחורה.png"));
            BufferedImage sideSheet  = ImageIO.read(getClass().getResourceAsStream("/images/רגיל צד.png"));

            // השמה למערכים שירשנו מ-MovableEntity
            walkDown = new BufferedImage[] {
                    frontSheet.getSubimage(0, 64, 64, 64),
                    frontSheet.getSubimage(0, 128, 64, 64)
            };

            walkUp = new BufferedImage[] {
                    backSheet.getSubimage(0, 0, 64, 64),
                    backSheet.getSubimage(0, 64, 64, 64)
            };

            walkRight = new BufferedImage[] {
                    sideSheet.getSubimage(0, 0, 64, 64),
                    sideSheet.getSubimage(64, 0, 64, 64)
            };

            walkLeft = new BufferedImage[] {
                    sideSheet.getSubimage(0, 64, 64, 64),
                    sideSheet.getSubimage(64, 64, 64, 64)
            };

            // קביעת ה-sprite ההתחלתי
            this.sprite = walkDown[0];

        } catch (Exception e) {
            System.out.println("Error loading player sprites: " + e.getMessage());
        }
    }

    public void update(InputManager input, Screen currentScreen) {
        if (!phoneOpen && !inDialogue) {
            handleMovement(input);
        }

        // לוגיקת טלפון
        if (input.ENTER_key && currentScreen.canPressEnter() && !inDialogue) {
            togglePhone();
            currentScreen.resetEnterTimer();
        }

        // ---- הוספנו את הקריאה לניהול קולות ההליכה ----
        handleFootstepsSound();
    }

    // הפעולה החדשה שבודקת מתי להדליק ולכבות את סאונד הצעדים
    private void handleFootstepsSound() {
        if (this.audio == null) return;

        // השחקן נחשב זז רק אם אחד מכיווני המהירות שלו שונה מאפס
        boolean isMoving = (this.dx != 0 || this.dy != 0);

        if (isMoving && !isWalkingSoundPlaying) {
            // אם הוא זז והסאונד לא פועל, נפעיל אותו בלופ
            this.audio.loop("צעדים");
            isWalkingSoundPlaying = true;
        }
        else if (!isMoving && isWalkingSoundPlaying) {
            // אם הוא עצר (או פתח טלפון/דיאלוג) והסאונד פועל, נעצור אותו מיד
            this.audio.stop("צעדים");
            isWalkingSoundPlaying = false;
        }
    }

    private void handleMovement(InputManager input) {
        this.dx = 0;
        this.dy = 0;
        if (input.W_key) dy = -1;
        if (input.S_Key) dy = 1;
        if (input.A_key) dx = -1;
        if (input.D_key) dx = 1;
    }

    // --- מערכת הודעות וטלפון (ללא שינוי לוגי) ---
    public void togglePhone() {
        phoneOpen = !phoneOpen;
        if (phoneOpen) {
            stop(); // הפעולה שירשנו מ-MovableEntity שמאפסת dx ו-dy
        }
        if (phoneOpen && unreadCount > 0) unreadCount--;
    }

    public void setInDialogue(boolean value) {
        this.inDialogue = value;
        if (inDialogue) stop();
    }

    public boolean isInDialogue(){
        return this.inDialogue;
    }

    public void addMessage(String sender, String text) {
        phoneMessages.add(new PhoneMessage(sender, text));
        if (!phoneOpen) unreadCount++;

        if (this.audio != null) {
            this.audio.play("notification"); // נגן את צליל ההתראה
        }
    }

    public boolean isPhoneOpen() { return phoneOpen; }

    public List<PhoneMessage> getPhoneMessages() { return phoneMessages; }

    public int getUnreadCount() { return unreadCount; }

    public void setAudioManager(AudioManager audio) {
        this.audio = audio;
    }
}