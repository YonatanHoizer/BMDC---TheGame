package story;

import ai.PatrolAI;
import entities.NPC;
import entities.Player;
import hud.InteractiveDialogueBox;
import npcs.Akiva;
import npcs.Kroyzer;
import npcs.Miller;
import npcs.Sanans;
import world.GameWorld;
import world.Zone;

import java.util.ArrayList;
import java.util.List;

import static engine.Time.deltaTime;

public class LunchEvent extends GameState {

    private Zone diningRoom;
    private Zone beitMidrash;
    private Zone path;

    // דמויות בחדר אוכל
    private Akiva akiva;
    private List<NPC> diningNPCs = new ArrayList<>();

    // דמויות סוף המשחק (בית מדרש)
    private Miller miller;
    private Kroyzer kroyzer;
    private Sanans sananes;
    private List<NPC> finaleNpcs = new ArrayList<>();
    private boolean isTalkingToStatic;

    // טיימרים ודגלים
    private double minchaTimer = 10.0;
    private boolean beitMidrashLoaded = false;
    private boolean exploreMessageShown = false;
    private boolean isAkivaDialogueActive = false;
    private boolean isAkivaPunchlineActive = false;

    private enum Phase {
        GOING_TO_LUNCH,
        IN_DINING_HALL,
        AKIVA_DIALOGUE,
        WAITING_FOR_MINCHA,
        FREE_ROAM,
        FINISHED
    }

    private Phase phase;

    public LunchEvent(Zone diningRoom, Zone beitMidrash, Zone path, Kroyzer kroyzer) {
        this.diningRoom = diningRoom;
        this.beitMidrash = beitMidrash;
        this.path = path;
        this.kroyzer = kroyzer;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.GOING_TO_LUNCH;
        completed = false;

        world.audio.loadSound("חדר אוכל", "/sounds/חדר אוכל סאונד.wav");
        world.audio.loadSound("חדר אוכל יציאה", "/sounds/חדר אוכל סאונד יציאה.wav");
        world.audio.loadSound("צרצרים", "/sounds/צרצרים.wav");

        // יוצרים את עקיבא מאחורי שולחן האוכל ומדליקים לו סימן קריאה
        akiva = new Akiva(26 * 64, 64, 64, 64);
        akiva.setAlert(true);
        world.addNPC(akiva);

        diningNPCs.add(new NPC(35 * 64,2 * 64,64,64,1,1));
        diningNPCs.add(new NPC(22 * 64,10* 64,64,64,3,1));
        diningNPCs.add(new NPC(25 * 64,11* 64,64,64,5,1));
        diningNPCs.add(new NPC(19 * 64,11* 64,64,64,4,1));
        diningNPCs.add(new NPC(20 * 64,7 * 64,64,64,3,1));
        diningNPCs.add(new NPC(30 * 64,5 * 64,64,64,4,4));
        diningNPCs.add(new NPC(23 * 64,14* 64,64,64,2,1));
        for (NPC npc: diningNPCs){
            npc.setMovementAI(new PatrolAI(diningRoom));
            world.addNPC(npc);
        }
        diningNPCs.get(4).setAlert(true);
        diningNPCs.get(4).setMovementAI(null);
        diningNPCs.get(5).setAlert(true);
        diningNPCs.get(5).setMovementAI(null);
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;

        Player player = world.getPlayer();

        switch (phase) {
            case GOING_TO_LUNCH:
                handleGoingToLunch(player, world);
                break;
            case IN_DINING_HALL:
                handleInDiningHall(player, world);
                handleStaticNpcDialogues(player,world);
                break;
            case AKIVA_DIALOGUE:
                handleAkivaDialogue(player, world);
                break;
            case WAITING_FOR_MINCHA:
                handleWaitingForMincha(player, world);
                handleStaticNpcDialogues(player,world);
                break;
            case FREE_ROAM:
                handleFreeRoam(player, world);
                handleStaticNpcDialogues(player,world);
                break;
            case FINISHED:
                break;
        }
    }

    private void handleGoingToLunch(Player player, GameWorld world) {
        // ברגע שהשחקן נכנס לחדר האוכל, זה הזמן להכין את בית המדרש!
        if (diningRoom.contains(player.getX(), player.getY())) {

            if (!beitMidrashLoaded) {
                world.removeNPC(kroyzer);
                loadFinaleNPCs(world);
                beitMidrashLoaded = true;
            }

            world.audio.loop("חדר אוכל");
            phase = Phase.IN_DINING_HALL;
        }
    }

    private void handleStaticNpcDialogues(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        if (!isTalkingToStatic && player.getDistanceSquared(diningNPCs.get(5)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {
                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                diningNPCs.get(5).setAlert(false);
                dBox.startDialogue(List.of("עקיבא צריך לשקול קריירה בסטאנדאפ בחיי.."));
            }
        }

        if (!isTalkingToStatic && player.getDistanceSquared(diningNPCs.get(4)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {
                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                diningNPCs.get(4).setAlert(false);
                List<String> lines = new ArrayList<>();
                lines.add("בא לך להזמין שפע וברכה ?");
                lines.add("אה  , אכלת שם כבר שש פעמיים השבוע ? \n (למרות שרק יום שני דאימ...)");
                dBox.startDialogue(lines);
            }
        }

        if (isTalkingToStatic && !dBox.isVisible()) {
            world.getPlayer().setInDialogue(false);
            isTalkingToStatic = false;
        }
    }



    private void handleInDiningHall(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // השחקן ניגש לעקיבא
        if (!isAkivaDialogueActive && player.getDistanceSquared(akiva) < (120 * 120)) {
            if (world.getInput().Z_key && dBox.isReady()) {

                player.setInDialogue(true);
                isAkivaDialogueActive = true;
                akiva.setAlert(false); // מכבים את סימן הקריאה

                dBox.startDialogueWithChoice(
                        "אהלן!   מה תרצה לאכול לצהריים?",
                        "סטייק אנטריקוט",
                        "פילה סלמון אפוי"
                );

                phase = Phase.AKIVA_DIALOGUE;
            }
        }

        if (path.contains(player.getX(),player.getY())){
            world.audio.stopAll();
            fail(8);
        }
    }

    private void handleAkivaDialogue(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // מחכים לתשובה של השחקן (שלב הבחירה)
        if (isAkivaDialogueActive && !isAkivaPunchlineActive && dBox.isReady()) {
            int choice = dBox.getFinalChoice();
            if (choice != -1) {
                // לא משנה מה הוא בחר, התשובה זהה!
                dBox.startDialogue(List.of("חחחח אין שום דבר כזה, יש רק נקניקיות. תיהיה בריא!"));
                world.audio.play("צרצרים");
                dBox.resetChoice();
                isAkivaPunchlineActive = true;
            }
        }

        if (isAkivaPunchlineActive && dBox.isReady()) {
            player.setInDialogue(false);
            isAkivaDialogueActive = false;
            isAkivaPunchlineActive = false;

            phase = Phase.WAITING_FOR_MINCHA;
        }
    }

    private void handleWaitingForMincha(Player player, GameWorld world) {
        minchaTimer -= deltaTime;

        // אחרי 5 שניות, שולחים הודעה בטלפון
        if (minchaTimer <= 0) {
            player.addMessage("הרב מילר", "תפילת מנחה מתחילה בקרוב בבית המדרש. נא להגיע!");

            phase = Phase.FREE_ROAM;
        }
    }

    private void handleFreeRoam(Player player, GameWorld world) {
        // אם השחקן יצא מחדר האוכל ועדיין לא הראינו לו את ההודעה
        if (!diningRoom.contains(player.getX(), player.getY()) && !exploreMessageShown) {
            world.getHUD().showTopMessage("זה הזמן לחקור את המפה. כשאתה מוכן לסיום המשחק, היכנס לבית המדרש.", 6.0);
            exploreMessageShown = true;
            world.audio.stop("חדר אוכל");
            world.audio.play("חדר אוכל יציאה");
        }

        // המעבר לשלב האחרון - כניסה לבית המדרש
        if (beitMidrash.contains(player.getX(), player.getY())) {
            finishEvent();
        }
    }

    /**
     * פעולת עזר שטוענת את כל הדמויות לתוך בית המדרש בזמן שהשחקן רחוק
     */
    private void loadFinaleNPCs(GameWorld world) {

        miller  = new Miller (19 * 64, 26 * 64, 64, 64);
        kroyzer = new Kroyzer(16 * 64, 26 * 64, 64, 64);
        sananes = new Sanans (22 * 64, 26 * 64, 64, 64);

        world.addNPC(miller);
        world.addNPC(kroyzer);
        world.addNPC(sananes);

        // הוספת עוד קצת תלמידים שיתנו אווירה
        NPC MoveWorshipper1 = new NPC(17 * 64, 38 * 64, 64, 64, 4, 4);
        finaleNpcs.add(MoveWorshipper1);
        NPC MoveWorshipper2 = new NPC(19 * 64, 38 * 64, 64, 64, 5, 4);
        finaleNpcs.add(MoveWorshipper2);
        NPC MoveWorshipper3 = new NPC(22 * 64, 38 * 64, 64, 64, 4, 4);
        finaleNpcs.add(MoveWorshipper3);

        NPC student1 = new NPC(25 * 64, 29 * 64, 64, 64, 1, 4);
        NPC student2 = new NPC(21 * 64, 35 * 64, 64, 64, 2, 4);
        student1.setAlert(false);
        student2.setAlert(false);
        finaleNpcs.add(student1);
        finaleNpcs.add(student2);

        NPC worshipper   = new NPC(15 * 64, 32 * 64, 64, 64, 3, 4);
        finaleNpcs.add(worshipper);
        NPC worshipper1  = new NPC(14 * 64, 35 * 64, 64, 64, 4, 4);
        finaleNpcs.add(worshipper1);
        NPC worshipper2  = new NPC(16 * 64, 29 * 64, 64, 64, 5, 4);
        finaleNpcs.add(worshipper2);
        NPC worshipper3  = new NPC(20 * 64, 32 * 64, 64, 64, 1, 4);
        finaleNpcs.add(worshipper3);
        NPC worshipper5  = new NPC(20 * 64, 29 * 64, 64, 64, 6, 4);
        finaleNpcs.add(worshipper5);
        NPC worshipper6  = new NPC(19 * 64, 38 * 64, 64, 64, 3, 4);
        finaleNpcs.add(worshipper6);
        NPC worshipper7  = new NPC(24 * 64, 32 * 64, 64, 64, 1, 4);
        finaleNpcs.add(worshipper7);
        NPC worshipper8  = new NPC(25 * 64, 35 * 64, 64, 64, 4, 4);
        finaleNpcs.add(worshipper8);
        NPC worshipper10 = new NPC(24 * 64, 38 * 64, 64, 64, 5, 4);
        finaleNpcs.add(worshipper10);

        for (int i = 0; i < finaleNpcs.size(); i++) {
            world.addNPC(finaleNpcs.get(i));
            if (i < 4){
                finaleNpcs.get(i).setMovementAI(new PatrolAI(beitMidrash));
            }
        }
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
    }

    @Override
    public void onExit(GameWorld world) {
        // מנקים את חדר האוכל בלבד! משאירים את בית המדרש מוכן לשלב הבא
        if (akiva != null) world.removeNPC(akiva);
        for (NPC n : diningNPCs) world.removeNPC(n);
        diningNPCs.clear();

        world.getStoryManager().setStudentsForClass(this.finaleNpcs);
        world.getStoryManager().setMiller(this.miller);
        world.getStoryManager().setKroyzer(this.kroyzer);
        world.getStoryManager().setSanans(this.sananes);
    }
}