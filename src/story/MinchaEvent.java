package story;

import ai.ChaseAI;
import ai.PatrolAI;
import entities.NPC;
import entities.Player;
import hud.InteractiveDialogueBox;
import npcs.Kroyzer;
import npcs.Miller;
import npcs.Sanans;
import world.GameWorld;
import world.Zone;

import java.util.List;
import java.util.Random;

import static engine.Time.deltaTime;

public class MinchaEvent extends GameState {

    private Zone beitMidrash;
    private Miller miller;
    private Kroyzer kroyzer;
    private Sanans sanans;
    private List<NPC> finaleNpcs;

    // טיימרים ודגלים
    private double prePrayerTimer = 30.0;
    private boolean isTensionStarted = false;
    private boolean isDialogueActive = false;
    private boolean questionAsked = false;
    private boolean isTalkingToStatic = false;
    private double victoryTimer = 5.0; // 5 שניות של המתנה

    // ניהול שאלות רנדומליות
    private int currentQuestionIndex = 0;
    private String[][] questions = {
            {"זה נהנה וזה לא חסר?", "חייב לשלם", "פטור מלשלם", "1"}, // 0 זו התשובה הנכונה
            {"מה הדין בשומר חינם שפשע?", "פטור", "חייב", "1"}, // 1 זו התשובה הנכונה
            {"מה השיעור של כזית לפי החזון איש?", "27 סמ''ק", "50 סמ''ק", "1"}
    };

    private enum Phase {
        PRAYING_FREE_ROAM,
        TENSION_BUILDUP,
        MILLER_APPROACHING,
        MILLER_CONFRONTATION,
        RESOLUTION,
        WAITING_FOR_VICTORY,
        FINISHED
    }

    private Phase phase;

    public MinchaEvent(Zone beitMidrash, Miller miller, Kroyzer kroyzer, Sanans sanans, List<NPC> finaleNpcs) {
        this.beitMidrash = beitMidrash;
        this.miller = miller;
        this.kroyzer = kroyzer;
        this.sanans = sanans;
        this.finaleNpcs = finaleNpcs;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.PRAYING_FREE_ROAM;
        completed = false;

        // טעינת סאונדים (וודא שהקובצים קיימים בתיקייה!)
        world.audio.loadSound("תפילה", "/sounds/תפילת מנחה.wav");
        world.audio.loadSound("מתח", "/sounds/סאונד לחץ מילר.wav");
        world.audio.loadSound("ניצחון", "/sounds/סאונד ניצחון.wav");

        world.audio.loop("תפילה");

        finaleNpcs.get(5).setAlert(true);
        finaleNpcs.get(9).setAlert(true);
        finaleNpcs.get(1).setAlert(true);
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;

        Player player = world.getPlayer();

        switch (phase) {
            case PRAYING_FREE_ROAM:
                handlePraying(player, world);
                break;
            case TENSION_BUILDUP:
                handleTensionBuildup(player, world);
                break;
            case MILLER_APPROACHING:
                handleMillerApproaching(player, world);
                break;
            case MILLER_CONFRONTATION:
                handleMillerConfrontation(player, world);
                break;
            case RESOLUTION:
                handleResolution(player, world);
                break;
            case WAITING_FOR_VICTORY:
                handleWaitingForVictory(world);
;                break;
            case FINISHED:
                break;
        }
    }

    private void handlePraying(Player player, GameWorld world) {
        prePrayerTimer -= deltaTime;
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (!isTalkingToStatic && player.getDistanceSquared(finaleNpcs.get(5)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                dBox.startDialogue(List.of("הנקניקיות האלה גמרו אותי סופית"));
                finaleNpcs.get(5).setAlert(false);
            }
        }

        if (!isTalkingToStatic && player.getDistanceSquared(finaleNpcs.get(9)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                dBox.startDialogue(List.of("מתקפת הטיטאנים זאת יצירת המופת הגדולה ביותר של המין האנושי"));
                finaleNpcs.get(9).setAlert(false);
            }
        }

        if (!isTalkingToStatic && player.getDistanceSquared(finaleNpcs.get(1)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                dBox.startDialogue(List.of("כן כן ,אפשר לדבר גם אם NPC שהולך ,זה עדיין בבנייה"));
            }
        }


        if (isTalkingToStatic && !dBox.isVisible()) {
            player.setInDialogue(false);
            isTalkingToStatic = false;
        }

        if (prePrayerTimer <= 0) {
            phase = Phase.TENSION_BUILDUP;
        }
    }

    private void handleTensionBuildup(Player player, GameWorld world) {
        if (!isTensionStarted) {
            isTensionStarted = true;

            // שינוי סאונד
            world.audio.stop("תפילה");
            world.audio.play("מתח");

            // נעילת שחקן
            player.setInDialogue(true);
            player.stop();

            // סיבוב כולם לכיוון השחקן!
            makeEveryoneFacePlayer(player);
            for (int i = 0; i < finaleNpcs.size(); i++) {
                if (i < 4){
                    finaleNpcs.get(i).setMovementAI(null);
                    finaleNpcs.get(i).stop();
                }
            }

            // יצירת מסלול דינמי למילר - שילך עד לשחקן

            miller.setMovementAI(new ChaseAI(player));
            miller.setSpeed(120f); // הליכה איטית ומלחיצה
        }

        // החשכת מסך איטית ל-0.5
        float currentAlpha = world.getFadeAlpha();
        if (currentAlpha < 0.4f) {
            world.setFadeAlpha(currentAlpha + (float)deltaTime * 0.3f);
        } else {
            phase = Phase.MILLER_APPROACHING;
        }
    }

    private void handleMillerApproaching(Player player, GameWorld world) {
        // בודקים אם מילר סיים ללכת לשחקן
        if (miller.getDistanceSquared(player) < (64 * 64)) {
            miller.stop();
            miller.setMovementAI(null);
            phase = Phase.MILLER_CONFRONTATION;
        }
    }

    private void handleMillerConfrontation(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (!isDialogueActive) {
            isDialogueActive = true;
            dBox.startDialogue(List.of("אני יודע שלא הפקדת היום את הטלפון שלך."));
        }

        // ברגע שההודעה הראשונה נסגרת, שואלים את השאלה
        if (isDialogueActive && !questionAsked && dBox.isReady() && !dBox.isVisible()) {
            questionAsked = true;

            // בחירת שאלה רנדומלית
            Random rand = new Random();
            currentQuestionIndex = rand.nextInt(questions.length);
            String[] q = questions[currentQuestionIndex];

            dBox.startDialogueWithChoice(q[0], q[1], q[2]);
            world.audio.stop("מתח");
        }

        // בדיקת התשובה של השחקן
        if (questionAsked) {
            int choice = dBox.getFinalChoice();
            if (choice != -1) {
                int correctChoice = Integer.parseInt(questions[currentQuestionIndex][3]);

                dBox.resetChoice();

                if (choice == correctChoice) {
                    // תשובה נכונה!
                    dBox.startDialogue(List.of("... יפה. לפחות אתה לומד. תחזור למקום שלך."));
                    phase = Phase.RESOLUTION;
                    world.audio.play("ניצחון");
                } else {
                    // תשובה שגויה - פסילה!
                    world.audio.stopAll();
                    fail(6);
                }
            }
        }
    }

    private void handleResolution(Player player, GameWorld world) {
        // החזרת המסך לצבע רגיל
        float currentAlpha = world.getFadeAlpha();
        if (currentAlpha > 0f) {
            world.setFadeAlpha(currentAlpha - (float)deltaTime * 0.6f);
        }

        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // כשהדיאלוג של הניצחון מסתיים
        if (dBox.isReady() && !dBox.isVisible()) {
            // מנגנים מוזיקת ניצחון פעם אחת (לא בלופ)

            miller.setMovementAI(new PatrolAI(beitMidrash));
            for (int i = 0; i < finaleNpcs.size(); i++) {
                world.addNPC(finaleNpcs.get(i));
                if (i < 4){
                    finaleNpcs.get(i).setSpeed(150);
                    finaleNpcs.get(i).setMovementAI(new PatrolAI(beitMidrash));
                }
            }


            // משחררים את השחקן
            player.setInDialogue(false);

            world.getHUD().showTopMessage("התפילה נגמרה. כולם מקבלים את הטלפונים. ניצחת!", 5.0);

            phase = Phase.WAITING_FOR_VICTORY;
        }
    }

    private void handleWaitingForVictory(GameWorld world) {
        victoryTimer -= deltaTime; // סופרים לאחור בלי להקפיא את המשחק!

        // אחרי 5 שניות (כשההודעה למעלה נעלמת), מסיימים את השלב
        if (victoryTimer <= 0) {
            finishEvent(world);
        }
    }

    private void finishEvent(GameWorld world) {
        completed = true;
        phase = Phase.FINISHED;
        world.audio.stopAll();
    }

    // --- פעולת עזר: לגרום לכולם להסתכל על השחקן ---
    private void makeEveryoneFacePlayer(Player player) {
        faceEntity(kroyzer, player);
        faceEntity(sanans, player);
        for (NPC npc : finaleNpcs) {
            faceEntity(npc, player);
        }
    }

    private void faceEntity(NPC npc, Player target) {
        if (npc == null || target == null) return;

        float dx = target.getX() - npc.getX();
        float dy = target.getY() - npc.getY();

        // חישוב הציר הדומיננטי (האם השחקן יותר רחוק אופקית או אנכית)
        if (Math.abs(dx) > Math.abs(dy)) {
            // ימינה או שמאלה
            if (dx > 0) npc.setFirstPosition(3);
            else npc.setFirstPosition(2);
        } else {
            // למעלה או למטה
            if (dy > 0) npc.setFirstPosition(1);
            else npc.setFirstPosition(4);
        }
    }

    @Override
    public void onExit(GameWorld world) {
        world.audio.stop("מתח");
        world.audio.stop("תפילה");
        if (miller != null){
            world.removeNPC(miller);
        }
        if (kroyzer != null){
            world.removeNPC(kroyzer);
        }
        if (sanans != null){
            world.removeNPC(sanans);
        }
        for (NPC npc :finaleNpcs){
            world.removeNPC(npc);
        }
        finaleNpcs.clear();
    }
}