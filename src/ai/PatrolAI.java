package ai;

import entities.NPC;
import world.GameWorld;
import world.Zone;
import util.Vactor2;

public class PatrolAI implements MovementAI {

    private final Zone zone;
    private Vactor2 target;
    private float waitTimer = 0;

    // משתנים לזיהוי חוסר תנועה (Stuck Detection)
    private float lastX, lastY;
    private float stuckCheckTimer = 0;

    private static final float ARRIVAL_RADIUS_SQ = 25f;
    private static final float WAIT_TIME = 8.0f;
    private static final float STUCK_THRESHOLD = 0.5f; // אם לא זז חצי פיקסל בפרק זמן מסוים
    private static final float CHECK_INTERVAL = 0.1f;   // נבדוק כל חצי שנייה

    public PatrolAI(Zone zone) {
        this.zone = zone;
        this.target = getRandomPointInZone();
    }

    @Override
    public void update(NPC npc, GameWorld world) {
        float dt = (float) world.getDeltaTime();

        // 1. המתנה בין יעדים
        if (waitTimer > 0) {
            waitTimer -= dt;
            npc.stop();
            return;
        }

        // 2. זיהוי חוסר תנועה
        stuckCheckTimer += dt;
        if (stuckCheckTimer >= CHECK_INTERVAL) {
            // חישוב המרחק שה-NPC עבר מאז הבדיקה האחרונה
            float distanceMoved = (float) Math.sqrt(Math.pow(npc.getX() - lastX, 2) + Math.pow(npc.getY() - lastY, 2));

            if (distanceMoved < STUCK_THRESHOLD) {
                // ה-NPC מנסה לזוז אבל לא מתקדם - נחליף יעד
                resetTarget();
                return;
            }

            // עדכון המיקום האחרון לבדיקה הבאה
            lastX = npc.getX();
            lastY = npc.getY();
            stuckCheckTimer = 0;
        }

        // 3. חישוב וקטור תנועה ליעד
        float dx = target.x - npc.getX();
        float dy = target.y - npc.getY();
        float distSq = dx * dx + dy * dy;

        // 4. הגעה ליעד
        if (distSq <= ARRIVAL_RADIUS_SQ) {
            resetTarget();
            return;
        }

        // 5. פקודת תנועה
        npc.moveTowards(dx, dy);
    }

    private void resetTarget() {
        this.target = getRandomPointInZone();
        this.waitTimer = WAIT_TIME;
        this.stuckCheckTimer = 0;
        // חשוב לאפס את המיקום האחרון כדי שלא יחשוב שהוא תקוע מיד בתחילת היעד החדש
    }

    private Vactor2 getRandomPointInZone() {
        float x = zone.getArea().getLeft() + (float)(Math.random() * zone.getArea().size.x);
        float y = zone.getArea().getTop() + (float)(Math.random() * zone.getArea().size.y);
        return new Vactor2(x, y);
    }
}