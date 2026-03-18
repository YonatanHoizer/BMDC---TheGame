package story;

import world.GameWorld;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public abstract class GameState {

    protected boolean completed = false;
    protected boolean failed = false;      // משתנה חדש: האם השחקן נכשל
    protected int failReason = 0;      // משתנה חדש: סיבת הפסילה

    protected List<Event> events = new ArrayList<>();

    public abstract void onEnter(GameWorld world);

    /** נקרא כל פריים בזמן שהשלב פעיל */
    public void update(GameWorld world) {
        if (completed) return; // הגנה: אם השלב נגמר, אל תעדכן אירועים נוספים

        // עדכון כל האירועים הפעילים
        List<Event> finished = new ArrayList<>();
        for (Event e : events) {
            e.update(world);
            if (e.isCompleted()) finished.add(e);
        }
        events.removeAll(finished);

        // אם אין יותר אירועים פעילים, השלב יכול להסתיים כהצלחה
        if (events.isEmpty() && !failed) {
            completed = true;
        }
    }

    public abstract void onExit(GameWorld world);

    public boolean isCompleted() { return completed; }

    // גטרים חדשים עבור ה-StoryManager
    public boolean isFailed() { return failed; }
    public int getFailReason() { return failReason; }

    // פונקציית עזר נוחה לשימוש בתוך השלבים
    protected void fail(int reason) {
        this.failed = true;
        this.completed = true;
        this.failReason = reason;
    }

    public interface Event {
        void update(GameWorld world);
        boolean isCompleted();
    }

    public void render(Graphics2D g) {}
}