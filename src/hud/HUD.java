package hud;

import entities.Player;
import engine.InputManager;

import java.awt.*;

public class HUD {
    private Player player;
    private PhoneUI phoneUI;
    private MassageBoxTop topMessageBox;
    private InteractiveDialogueBox dialogueBox;

    public HUD(Player player) {
        this.player = player;
        this.phoneUI = new PhoneUI(player);
        this.topMessageBox = new MassageBoxTop(5);
        this.dialogueBox = new InteractiveDialogueBox();
    }

    public HUD(){
        this.player = null;
        this.phoneUI = null;
        this.topMessageBox = new MassageBoxTop(0);
        this.dialogueBox = new InteractiveDialogueBox();
    }

    public void update(double deltaTime) {
        topMessageBox.update(deltaTime);
        dialogueBox.update(deltaTime);
        phoneUI.update(deltaTime);
    }

    public void render(Graphics2D g) {
        phoneUI.render(g);
        topMessageBox.render(g);
        dialogueBox.render(g);
    }

    // --- מעטפת (Wrappers) לניהול הודעות ---

    // הודעה בטלפון - אנחנו מעדכנים את השחקן, ה-UI כבר ימשוך את זה משם לבד
    public void addPhoneMessage(String sender, String text) {
        player.addMessage(sender, text);
    }

    // בדיקה אם הטלפון פתוח - שואלים את השחקן
    public boolean isPhoneOpen() {
        return player.isPhoneOpen();
    }

    // הודעות מערכת/עולם - נשארות ב-HUD כי הן לא "מידע של השחקן"
    public void showTopMessage(String text,double timer) {
        topMessageBox.show(text,timer);
    }

    // חשוב: להעביר את הקלט ל-PhoneUI כדי שנוכל לדפדף בהודעות
    public void handleInput(InputManager input) {
        phoneUI.handleInput(input);
        dialogueBox.handleInput(input);
    }
    public void setTopMessageBox(MassageBoxTop box) {
        this.topMessageBox = box;
    }
    public MassageBoxTop getTopMessageBox() {
        return this.topMessageBox;
    }
    public PhoneUI getPhoneUI() {
        return this.phoneUI; // בהנחה שקראת למשתנה phoneUI
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
}


