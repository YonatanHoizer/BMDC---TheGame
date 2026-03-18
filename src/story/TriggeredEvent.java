package story;

import world.GameWorld;
/**
 * אירוע שיכול "להתרחש" במהלך השלב,
 * מבוסס על GameState כך שניתן להשתמש בו כשלב פעיל בעלילה
 */
public abstract class TriggeredEvent extends GameState {

    protected boolean triggered = false;

    @Override
    public void update(GameWorld world) {
        if (!triggered && shouldTrigger(world)) {
            triggered = true;
            onTrigger(world);
            completed = true; // סמן את השלב כסתיים אחרי טריגר
        }
    }
    /** האם האירוע צריך להתרחש */
    public abstract boolean shouldTrigger(GameWorld world);
    /** מה קורה כאשר האירוע מתרחש */
    public abstract void onTrigger(GameWorld world);
}
