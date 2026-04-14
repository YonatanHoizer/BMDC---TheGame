package main;

import engine.InputManager;
import ui.Screen;
import ui.MainMenuScreen;

import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;

/**
 * מחלקת המשחק המרכזית - מנהלת את ה-Loop ואת החלפת המסכים
 */
public class Game extends JPanel implements Runnable {

    public static double deltaTime = 0;
    private Thread gameThread;
    private boolean running = false;

    private InputManager input;
    private Screen currentScreen;

    // רזולוציה קבועה
    public final int WIDTH = 1280;
    public final int HEIGHT = 720;

    public Game() {
        this.input = new InputManager();

        // הגדרות ה-Panel
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(Color.BLACK);
        this.setDoubleBuffered(true);
        this.addKeyListener(input);
        this.setFocusable(true);
        this.requestFocus();

        // הגדרת מסך הפתיחה
        setScreen(new MainMenuScreen(this, input));
    }

    /**
     * פונקציה למעבר בין מסכים - קוראת ל-onExit של הישן ו-onEnter של החדש
     */
    public void setScreen(Screen newScreen) {
        if (currentScreen != null) {
            currentScreen.onExit();
        }
        currentScreen = newScreen;
        currentScreen.onEnter();
    }

    public void start() {
        if (running) return;
        running = true;
        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void run() {
        // Game Loop מבוסס זמן (60 FPS)
        double drawInterval = 1000000000.0 / 60.0;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (gameThread != null && running) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;
            lastTime = currentTime;
            if (delta >= 1) {
                // 1. קודם כל מעדכנים את הזמן הגלובלי
                deltaTime = 1.0 / 60.0;

                // 2. קוראים ל-update פעם אחת בלבד
                // ה-update הזה כבר יפעיל את player.update() שבסוף יפעיל את move()
                update(deltaTime);

                // 3. מציירים
                repaint();

                delta--;
            }
        }
    }

    private void update(double deltaTime) {
        // 1. עדכון מצב המקשים ב-InputManager
        input.update();
        // 2. עדכון המסך הנוכחי (לוגיקה וקלט)
        if (currentScreen != null) {
            currentScreen.update(deltaTime);
            currentScreen.handleInput(input);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // ציור המסך הנוכחי
        if (currentScreen != null) {
            currentScreen.render(g2);
        }

        g2.dispose();
    }
}