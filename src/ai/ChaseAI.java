package ai;

import entities.NPC;
import entities.Player;
import world.GameWorld;

import static engine.Time.deltaTime; // ודא שהייבוא הזה קיים אצלך (או world.getDeltaTime())

public class ChaseAI implements MovementAI {

    private final Player target;

    // משתנים לזיהוי היתקעות
    private float lastX = 0, lastY = 0;
    private float stuckTimer = 0f;

    // משתנים למצב "התחמקות ממכשול"
    private float evadeTimer = 0f;
    private float evadeDirX = 0, evadeDirY = 0;

    public ChaseAI(Player target) {
        this.target = target;
    }

    @Override
    public void update(NPC npc, GameWorld world) {
        if (target == null) {
            npc.stop();
            return;
        }

        float dx = target.getX() - npc.getX();
        float dy = target.getY() - npc.getY();

        // 1. האם אנחנו כרגע באמצע "עקיפת מכשול"?
        if (evadeTimer > 0) {
            evadeTimer -= deltaTime;
            npc.moveTowards(evadeDirX, evadeDirY); // הולכים הצידה!

            // עדכון המיקום האחרון כדי שלא נחשוב שנתקענו בזמן העקיפה
            lastX = npc.getX();
            lastY = npc.getY();
            return; // עוצרים כאן ולא ממשיכים למרדף הרגיל
        }

        // 2. מרדף רגיל
        npc.moveTowards(dx, dy);

        // 3. מנגנון זיהוי היתקעות
        // בודקים כמה באמת זזנו מאז הפריים הקודם (מרחק ריבועי)
        float distMovedSq = (npc.getX() - lastX) * (npc.getX() - lastX) +
                (npc.getY() - lastY) * (npc.getY() - lastY);

        // אם בקושי זזנו (פחות מפיקסל אחד), סימן שנתקענו בקיר/שולחן!
        if (distMovedSq < 1.0f) {
            stuckTimer += deltaTime;

            // אם אנחנו תקועים כבר רבע שנייה ברציפות
            if (stuckTimer > 0.25f) {
                stuckTimer = 0f;
                evadeTimer = 0.7f; // מתחילים עקיפה של חצי שנייה

                // טריק מתמטי: וקטור מאונך (90 מעלות) לכיוון השחקן
                // אפשר ללכת שמאלה (dy, -dx) או ימינה (-dy, dx)
                if (Math.random() > 0.5) {
                    evadeDirX = dy;
                    evadeDirY = -dx;
                } else {
                    evadeDirX = -dy;
                    evadeDirY = dx;
                }
            }
        } else {
            // אם זזנו בסדר, מאפסים את טיימר ההיתקעות
            stuckTimer = 0f;
        }

        // 4. שומרים את המיקום הנוכחי לפריים הבא
        lastX = npc.getX();
        lastY = npc.getY();
    }
}