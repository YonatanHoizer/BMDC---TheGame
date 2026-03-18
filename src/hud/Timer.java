package hud;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

public class Timer {

    private double remainingTime;  // זמן שנותר בספרות עשרוניות (שניות)
    private int x, y;              // מיקום הציור על המסך
    private boolean running;       // האם הטיימר פעיל

    private Font font = new Font("Arial", Font.BOLD, 36);
    private Color color = Color.WHITE;


    public Timer(double timeSeconds) {
        this.remainingTime = timeSeconds;
        this.x = x;
        this.y = y;
        this.running = true;
    }

    public void update(double deltaTime) {
        if (!running) return;

        remainingTime -= deltaTime;
        if (remainingTime <= 0) {
            remainingTime = 0;
            running = false;
        }
    }

    public void render(Graphics2D g) {
        g.setFont(font);
        g.setColor(color);

        int displayTime = (int) Math.ceil(remainingTime); // מציג שניות שלמות
        g.drawString("Time: " + displayTime, x, y);
    }

    public boolean isFinished() {
        return !running;
    }

    public void reset(double newTime) {
        remainingTime = newTime;
        running = true;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void setFont(Font font) {
        this.font = font;
    }
}//ככה להשתמש
// יצירת טיימר של 10 שניות בחלק העליון של המסך
//Timer countdown = new Timer(10, 500, 50);
//
// בתוך update של המשחק
//countdown.update(deltaTime);
//
// בתוך render של המשחק
//countdown.render(g2);
//
// לבדוק אם נגמר הזמן
//if (countdown.isFinished()) {
//    // הזמן נגמר - בצע פעולה
//}
