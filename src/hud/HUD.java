package hud;

import entities.Player;
import engine.InputManager;
import world.GameWorld;

import java.awt.*;

public class HUD {
    private Player player;
    private PhoneUI phoneUI;
    private MassageBoxTop topMessageBox;
    private InteractiveDialogueBox dialogueBox;
    private TimerUI timerUI;

    public HUD(Player player) {
        this.player = player;
        this.phoneUI = new PhoneUI(player);
        this.topMessageBox = new MassageBoxTop(5);
        this.dialogueBox = new InteractiveDialogueBox();
        this.timerUI = new TimerUI();
    }

    public HUD(){
        this.player = null;
        this.phoneUI = null;
        this.topMessageBox = new MassageBoxTop(0);
        this.dialogueBox = new InteractiveDialogueBox();
        this.timerUI = new TimerUI();
    }

    public void update(double deltaTime) {
        topMessageBox.update(deltaTime);
        dialogueBox.update(deltaTime);
        timerUI.update(deltaTime);
        // במקרה של בנאי ריק (בלי שחקן), צריך לבדוק שהטלפון לא null
        if(phoneUI != null) {
            phoneUI.update(deltaTime);
        }
    }

    public void render(Graphics2D g) {
        phoneUI.render(g);
        topMessageBox.render(g);
        dialogueBox.render(g);
        timerUI.render(g);
    }

    // --- מעטפת (Wrappers) לניהול הודעות ---

    public void showTimer(String label, double seconds) {
        timerUI.startTimer(label, seconds);
    }

    public void hideTimer() {
        timerUI.stopAndHide();
    }

    public boolean isTimerDone() {
        return timerUI.isTimeUp();
    }

    public double getTimerTimeLeft() {
        return timerUI.getTimeLeft();
    }

    public boolean shouldPlayTimerTick() {
        if (timerUI != null && timerUI.isVisible()) {
            return timerUI.consumeTickSound();
        }
        return false;
    }

    // --- מעטפת (Wrappers) קיימים ---

    public void addPhoneMessage(String sender, String text) {
        if(player != null) player.addMessage(sender, text);
    }

    public boolean isPhoneOpen() {
        return player != null && player.isPhoneOpen();
    }

    public void showTopMessage(String text, double timer) {
        topMessageBox.show(text, timer);
    }

    public void handleInput(InputManager input ,GameWorld world) {
        if(phoneUI != null) phoneUI.handleInput(input);
        dialogueBox.handleInput(input, world);
    }

    public void setTopMessageBox(MassageBoxTop box) {
        this.topMessageBox = box;
    }
    public MassageBoxTop getTopMessageBox() {
        return this.topMessageBox;
    }
    public PhoneUI getPhoneUI() {
        return this.phoneUI;
    }
    public void setPhoneUI(PhoneUI phoneUI) {
        this.phoneUI = phoneUI;
    }
    public InteractiveDialogueBox getDialogueBox() {
        return dialogueBox;
    }
    public void setBottomMessageBox(InteractiveDialogueBox box) {
        this.dialogueBox = box;
    }
    public TimerUI getTimerUI() {
        return this.timerUI;
    }
}


