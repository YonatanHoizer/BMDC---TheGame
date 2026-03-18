package story;

import ai.PatrolAI;
import entities.NPC;
import entities.Player;
import npcs.Miller; // <-- ייבוא מחלקת מילר החדשה!
import world.GameWorld;
import world.Zone;
import hud.InteractiveDialogueBox;

import java.util.ArrayList;
import java.util.List;

import static engine.Time.deltaTime;

public class SederEvent extends GameState {

    private Zone beitMidrash;
    private Zone dormRoom;

    // טיימרי סדר ראשיים
    private double sederDuration = 90.0;
    private boolean isSederPaused = false;

    // מערכת הודעות פיתיון
    private double messageIntervalTimer = 8.0;
    private int messagesReceived = 0;

    // משימת החלב
    private boolean playerHasMilk;
    private boolean milkQuestTriggered = false;
    private boolean milkQuestActive = false;
    private boolean milkQuestCompleted = false;
    private boolean waitingToReturnToBeitMidrash = false;
    private double YosefWaitTimer = 30.0;
    private NPC Yosef;
    private boolean isTalkingToYosef = false;

    // מילר (עכשיו משתמש במחלקה שלו!)
    private Miller miller;

    // NPCs סטטיים לדיבור
    private NPC talkativeNpc1;
    private NPC talkativeNpc2;
    private boolean isTalkingToStatic = false;

    private List<NPC> allSederNpcs = new ArrayList<>();

    private enum Phase { LEARNING, FINISHED }
    private Phase phase = Phase.LEARNING;

    public SederEvent(Zone beitMidrash, Zone dormRoom, boolean playerHasMilk) {
        this.beitMidrash = beitMidrash;
        this.dormRoom = dormRoom;
        this.playerHasMilk = playerHasMilk;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.LEARNING;
        completed = false;

        //world.audio.loadSound("לימוד", "/sounds/beit_midrash_noise.wav");
        //world.audio.loop("לימוד");

        world.getHUD().showTopMessage("הסדר התחיל! אסור שהרב מילר יראה אותך עם טלפון פתוח. שים עין איפה הוא מסתובב.",5.0);

        miller = new Miller(15 * 64, 30 * 64, 64, 64);
        miller.setMillerPatrolAI(new PatrolAI(beitMidrash));
        world.addNPC(miller);

        if (playerHasMilk) {
            Yosef = new NPC(10 * 64, 52 * 64, 64, 64, 2, 4);
            world.addNPC(Yosef);
        }

        talkativeNpc1 = new NPC(12 * 64, 35 * 64, 64, 64, 1, 4);
        talkativeNpc2 = new NPC(20 * 64, 35 * 64, 64, 64, 3, 4);
        world.addNPC(talkativeNpc1);
        world.addNPC(talkativeNpc2);
        allSederNpcs.add(talkativeNpc1);
        allSederNpcs.add(talkativeNpc2);

        // כאן תוכל להוסיף את שאר ה-NPCs בבית המדרש...
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;
        Player player = world.getPlayer();

        // 1. לוגיקת מילר - התקצרה לשורה אחת בלבד!
        if (miller.checkPhoneAndChase(player)) {
            fail(4);}

        // 2. לוגיקת משימת החלב
        handleMilkQuestLogic(player, world);

        // 3. אינטראקציות עם NPCs רגילים
        handleStaticNpcDialogues(player, world);

        // 4. לוגיקת סדר (טיימר והודעות)
        if (!isSederPaused) {
            sederDuration -= deltaTime;

            if (messagesReceived < 10) {
                messageIntervalTimer -= deltaTime;
                if (messageIntervalTimer <= 0) {
                    messagesReceived++;
                    player.addMessage("דרך חיים תלמידים", "הודעה חשובה מספר " + messagesReceived + " - כנסו לראות!");
                    messageIntervalTimer = 8.0;
                }
            }

            if (playerHasMilk && messagesReceived == 5 && !milkQuestTriggered) {
                milkQuestTriggered = true;
                milkQuestActive = true;
                isSederPaused = true;
                player.addMessage("יוסף משה", "אחי, יש מצב שאתה עולה רגע לחדר 3 בפנימייה ומביא לי את החלב? אני חייב נס קפה.");
            }

            if (sederDuration <= 0) {
                finishEvent();
            }
        }
    }

    private void handleMilkQuestLogic(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (milkQuestActive) {
            YosefWaitTimer -= deltaTime;

            if (player.getDistanceSquared(Yosef) < (80 * 80) && !isTalkingToYosef) {
                if (world.getInput().E_key && !dBox.isVisible()) {
                    player.setInDialogue(true);
                    isTalkingToYosef = true;
                    dBox.startDialogue(List.of("וואו תודה אחי! הצלת אותי עם החלב הזה. תחזור ללמוד."));
                }
            }

            if (isTalkingToYosef && !dBox.isVisible()) {
                player.setInDialogue(false);
                isTalkingToYosef = false;
                milkQuestActive = false;
                milkQuestCompleted = true;
                waitingToReturnToBeitMidrash = true;
            }

            if (YosefWaitTimer <= 0 && milkQuestActive) {
                milkQuestActive = false;
                player.addMessage("יוסף משה", "עזוב אחי, לא משנה, כבר הסתדרתי. תישאר בסדר.");
                isSederPaused = false;
            }
        }

        if (waitingToReturnToBeitMidrash) {
            if (beitMidrash.contains(player.getX(), player.getY())) {
                waitingToReturnToBeitMidrash = false;
                isSederPaused = false;
                System.out.println("השחקן חזר משימת החלב - הטיימר ממשיך!");
            }
        }
    }

    private void handleStaticNpcDialogues(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (!isTalkingToStatic && player.getDistanceSquared(talkativeNpc1) < (80 * 80)) {
            if (world.getInput().E_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                dBox.startDialogue(List.of("אל תפריע לי עכשיו, אני באמצע סוגיה קשה בבבא קמא."));
            }
        }

        if (!isTalkingToStatic && player.getDistanceSquared(talkativeNpc2) < (80 * 80)) {
            if (world.getInput().E_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                dBox.startDialogue(List.of("שמע האיוונט החדש בקלאש פסיכי אחי, ראית איך ריילי שיחק?"));
            }
        }


        if (isTalkingToStatic && !dBox.isVisible()) {
            player.setInDialogue(false);
            isTalkingToStatic = false;
        }
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
        System.out.println("הסדר נגמר בהצלחה!");
    }

    public List<NPC> getStudentsForClass() {
        List<NPC> movingStudents = new ArrayList<>();
        // ניקח למשל את 2 התלמידים הראשונים מהרשימה
        if (allSederNpcs.size() >= 3) {
            movingStudents.add(allSederNpcs.get(0));
            movingStudents.add(allSederNpcs.get(1));
            movingStudents.add(allSederNpcs.get(1));
        }
        return movingStudents;
    }

    @Override
    public void onExit(GameWorld world) {
        world.audio.stop("לימוד");

        if (Yosef != null) world.removeNPC(Yosef);

        world.getStoryManager().setStudentsForClass(this.allSederNpcs);
        world.getStoryManager().setMiller(this.miller);
    }
}