package ui;

import java.awt.Graphics2D;
import engine.InputManager;

public abstract class Screen {

    protected InputManager input;

    protected double enterLockTimer = 0;

    public Screen(InputManager input) {
        this.input = input;
    }

    public void onEnter() {
        enterLockTimer = 0.4;
    }

    public void onExit() {
    }

    public void update(double deltaTime) {
        if (enterLockTimer > 0) {
            enterLockTimer -= deltaTime;
        }
    }

    public void resetEnterTimer() {
        this.enterLockTimer = 0.2; // חצי שנייה של חסימה
    }

    public boolean canPressEnter() {
        return enterLockTimer <= 0;
    }

    public abstract void render(Graphics2D g);

    public void handleInput(InputManager input) {};
}