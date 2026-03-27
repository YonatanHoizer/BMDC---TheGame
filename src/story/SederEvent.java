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
    private double YosefWaitTimer = 20.0;
    private NPC Yosef;
    private boolean isTalkingToYosef = false;

    // מילר (עכשיו משתמש במחלקה שלו!)
    private Miller miller;

    // NPCs סטטיים לדיבור
    private NPC talkativeNpc1;
    private NPC talkativeNpc2;
    private NPC npc1;
    private NPC npc2;
    private NPC npc3;
    private NPC npc4;
    private boolean isTalkingToStatic = false;

    private List <NPC> allSederNpcs = new ArrayList<>();
    private List <String> texst = new ArrayList<>();

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

        world.audio.loadSound("לימוד", "/sounds/בית מדרש סאונד.wav");
        world.audio.loadSound("לימוד סיום", "/sounds/בית מדרש סאונד יציאה.wav");
        world.audio.setVolume("לימוד",0.8F);
        world.audio.loop("לימוד");

        world.getHUD().showTopMessage("הסדר התחיל! אסור שהרב מילר יראה אותך עם טלפון פתוח. שים עין איפה הוא מסתובב.",5.0);

        texst.add("יוסף : אביאל אני חולה על הלחיים שלך");
        texst.add("יפרח : @דהן תעיף אותו מהקבוצה");
        texst.add("אלכס : (גיף מפגר כלשהוא של לאונרדו דקאפריו)");
        texst.add("דהן : עזוב יפרח תן לו הוא צריך לפרוק פעם ב");
        texst.add("אביאל : זה מעבר להצלה בשלב הזה");
        texst.add("בינימין : (איזה קישור לאתר חדשות לא קיים בשביל כתבה בת שורה על הישיבה)");
        texst.add("יוסף : *יוסף העיף את בינימין מהקבוצה*");
        texst.add("יפרח : דיקטטורים צריכים ללמוד את הקבוצה הזאת..");
        texst.add("חגאי : מזל טוב לבינון לכבוד יום ההולדת 27 שתחיה עד..");
        texst.add("בינון : שחכתי מזה לגמרי");
        texst.add("");
        texst.add("");

        miller = new Miller(15 * 64, 30 * 64, 64, 64);
        miller.setMillerPatrolAI(new PatrolAI(beitMidrash));
        world.addNPC(miller);

        if (playerHasMilk) {
            Yosef = new NPC(12 * 64, 51 * 64, 64, 64, 2, 1);
            world.addNPC(Yosef);
        }

        talkativeNpc1 = new NPC(15 * 64, 35 * 64, 64, 64, 2, 4);
        talkativeNpc1.setAlert(true);
        talkativeNpc2 = new NPC(28 * 64, 29 * 64, 64, 64, 4, 1);
        talkativeNpc2.setAlert(true);
        npc1 = new NPC(10 * 64, 30 * 64, 64, 64, 3, 4);
        npc2 = new NPC(20 * 64, 33 * 64, 64, 64, 1, 4);
        npc3 = new NPC(16 * 64, 30 * 64, 64, 64, 4, 4);
        npc4 = new NPC(18 * 64, 29 * 64, 64, 64, 5, 4);
        allSederNpcs.add(npc1);
        allSederNpcs.add(npc2);
        allSederNpcs.add(npc3);
        allSederNpcs.add(npc4);
        for (NPC npc : allSederNpcs){
            npc.setMovementAI(new PatrolAI(beitMidrash));
            world.addNPC(npc);
        }
        allSederNpcs.add(talkativeNpc1);
        allSederNpcs.add(talkativeNpc2);
        world.addNPC(talkativeNpc1);
        world.addNPC(talkativeNpc2);
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;
        Player player = world.getPlayer();

        // 1. לוגיקת מילר - התקצרה לשורה אחת בלבד!
        if (miller.checkPhoneAndChase(player)) {
            world.audio.stopAll();
            fail(4);}

        // 2. לוגיקת משימת החלב
        handleMilkQuestLogic(player, world);

        // 3. אינטראקציות עם NPCs רגילים
        handleStaticNpcDialogues(player, world);

        // 4. לוגיקת סדר (טיימר והודעות)
        if (!isSederPaused) {
            sederDuration -= deltaTime;

            if (!texst.isEmpty()) {
                messageIntervalTimer -= deltaTime;
                if (messageIntervalTimer <= 0) {
                    player.addMessage("דרך חיים תלמידים", texst.get(messagesReceived));
                    messageIntervalTimer = 8.0;
                    messagesReceived ++;
                }
            }

            if (playerHasMilk && messagesReceived == 5 && !milkQuestTriggered) {
                milkQuestTriggered = true;
                milkQuestActive = true;
                isSederPaused = true;
                player.addMessage("יוסף משה", "אחי, יש מצב שאתה עולה רגע לחדר הראשון בפנימייה ומביא לי את החלב?");
            }

            if (sederDuration <= 0) {
                world.audio.stop("לימוד");
                world.audio.play("לימוד סיום");
                finishEvent();
            }
        }
    }

    private void handleMilkQuestLogic(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (milkQuestActive) {
            YosefWaitTimer -= deltaTime;

            if (player.getDistanceSquared(Yosef) < (64 * 64) && !isTalkingToYosef) {
                if (world.getInput().E_key && !dBox.isVisible()) {
                    player.setInDialogue(true);
                    isTalkingToYosef = true;
                    dBox.startDialogue(List.of("אחלן תודה נסיך , אין עליך."));
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
                waitingToReturnToBeitMidrash = true;
                player.addMessage("יוסף משה", "עזוב אחי, לא משנה, כבר הסתדרתי. נתחשבן אחר כך...");
                isSederPaused = false;
                texst.add(5,"יוסף : *יוסף העיף אותך מהקבוצה* ");
            }
        }

        if (waitingToReturnToBeitMidrash) {
            if (beitMidrash.contains(player.getX(), player.getY())) {
                waitingToReturnToBeitMidrash = false;
                isSederPaused = false;
            }
        }
    }

    private void handleStaticNpcDialogues(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (!isTalkingToStatic && player.getDistanceSquared(talkativeNpc1) < (64 * 64)) {
            if (world.getInput().E_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                talkativeNpc1.setAlert(false);
                dBox.startDialogue(List.of("אני מוכר 5 מיילי ב200 שקל , בא לך?"));
            }
        }

        if (!isTalkingToStatic && player.getDistanceSquared(talkativeNpc2) < (64 * 64)) {
            if (world.getInput().E_key && dBox.isReady()) {
                player.setInDialogue(true);
                isTalkingToStatic = true;
                talkativeNpc2.setAlert(false);
                dBox.startDialogue(List.of("שמע האיוונט החדש בקלאש פסיכי אחי, ראית את הלייב של ריילי?"));
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
    }

    @Override
    public void onExit(GameWorld world) {

        if (Yosef != null) world.removeNPC(Yosef);
        texst.clear();

        world.getStoryManager().setStudentsForClass(this.allSederNpcs);
        world.getStoryManager().setMiller(this.miller);
    }
}