package story;

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

    // דמויות בחדר אוכל
    private Akiva akiva;
    private List<NPC> diningNpcs = new ArrayList<>();

    // דמויות סוף המשחק (בית מדרש)
    private Miller miller;
    private Kroyzer kroyzer;
    private Sanans sananes;
    private List<NPC> finaleNpcs = new ArrayList<>();

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

    public LunchEvent(Zone diningRoom, Zone beitMidrash) {
        this.diningRoom = diningRoom;
        this.beitMidrash = beitMidrash;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.GOING_TO_LUNCH;
        completed = false;

        // יוצרים את עקיבא מאחורי שולחן האוכל ומדליקים לו סימן קריאה
        akiva = new Akiva(26 * 64, 64, 64, 64);
        akiva.setAlert(true);
        world.addNPC(akiva);

        // כאן תוכל להוסיף NPC רגילים שיושבים בחדר אוכל ולהכניס ל-diningNpcs...
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
                break;
            case AKIVA_DIALOGUE:
                handleAkivaDialogue(player, world);
                break;
            case WAITING_FOR_MINCHA:
                handleWaitingForMincha(player, world);
                break;
            case FREE_ROAM:
                handleFreeRoam(player, world);
                break;
            case FINISHED:
                break;
        }
    }

    private void handleGoingToLunch(Player player, GameWorld world) {
        // ברגע שהשחקן נכנס לחדר האוכל, זה הזמן להכין את בית המדרש!
        if (diningRoom.contains(player.getX(), player.getY())) {

            if (!beitMidrashLoaded) {
                loadFinaleNPCs(world);
                beitMidrashLoaded = true;
            }

            phase = Phase.IN_DINING_HALL;
        }
    }

    private void handleInDiningHall(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // השחקן ניגש לעקיבא
        if (!isAkivaDialogueActive && player.getDistanceSquared(akiva) < (120 * 120)) {
            if (world.getInput().E_key && dBox.isReady()) {

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
    }

    private void handleAkivaDialogue(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // מחכים לתשובה של השחקן (שלב הבחירה)
        if (isAkivaDialogueActive && !isAkivaPunchlineActive && dBox.isReady()) {
            int choice = dBox.getFinalChoice();
            if (choice != -1) {
                // לא משנה מה הוא בחר, התשובה זהה!
                dBox.startDialogue(List.of("חחחח אין שום דבר כזה, יש רק נקניקיות. תיהיה בריא!"));

                dBox.resetChoice();
                isAkivaPunchlineActive = true; // עוברים לפאנץ'
            }
        }

        // מחכים שהפאנץ' (ההודעה האחרונה) יסתיים
        if (isAkivaPunchlineActive && dBox.isReady()) {
            player.setInDialogue(false);
            isAkivaDialogueActive = false;
            isAkivaPunchlineActive = false;

            phase = Phase.WAITING_FOR_MINCHA; // עוברים להמתנה להודעה
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
        // שנה את המיקומים (x, y) לפי איך שאתה רוצה שהם יעמדו בבית המדרש לקראת הסוף
        miller = new Miller(15 * 64, 26 * 64, 64, 64);
        kroyzer = new Kroyzer(18 * 64, 26 * 64, 64, 64);
        sananes = new Sanans(22 * 64, 26 * 64, 64, 64);

        world.addNPC(miller);
        world.addNPC(kroyzer);
        world.addNPC(sananes);

        // הוספת עוד קצת תלמידים שיתנו אווירה
        NPC student1 = new NPC(12 * 64, 32 * 64, 64, 64, 1, 4);
        NPC student2 = new NPC(17 * 64, 32 * 64, 64, 64, 2, 4);
        world.addNPC(student1);
        world.addNPC(student2);

        finaleNpcs.add(student1);
        finaleNpcs.add(student2);
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
        System.out.println("סיום ארוחת צהריים - מעבר למנחה (סוף המשחק)!");
    }

    @Override
    public void onExit(GameWorld world) {
        // מנקים את חדר האוכל בלבד! משאירים את בית המדרש מוכן לשלב הבא
        if (akiva != null) world.removeNPC(akiva);
        for (NPC n : diningNpcs) world.removeNPC(n);
        diningNpcs.clear();

        // --- שים לב! ---
        // חייבים להעביר את מילר, קרויזר והשאר ל-StoryManager
        // כדי שהשלב הבא (MinchaEvent) יוכל לקבל אותם ולהשתמש בהם!
        // world.getStoryManager().setMiller(miller);
        // world.getStoryManager().setKroyzer(kroyzer);
        // וכו'...
    }
}