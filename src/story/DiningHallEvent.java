package story;

import ai.PatrolAI;
import ai.ScriptedMovementAI;
import entities.Entity;
import entities.NPC;
import npcs.Akiva;
import world.GameWorld;
import world.Zone;
import hud.InteractiveDialogueBox;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import static main.Game.deltaTime;

public class DiningHallEvent extends GameState {

    private Zone diningHall;
    private Zone path;

    // טיימרים ודגלים
    private double timeInDiningHall = 0;
    private boolean milkMissionReceived = false;
    private boolean sederMessageReceived = false;
    private double milkInteractionCooldown = 0;
    private boolean OutsideOfDiningHall = false;
    private boolean finishAkivaChase = false;


    // משימת החלב
    private Entity Milk;
    private boolean hasMilk = false;
    private boolean isMilkDialogueActive = false;

    // עקיבא והמרדף
    private Akiva akiva;
    private ScriptedMovementAI akivaAI;
    private boolean isAkivaDialogueActive = false;
    private boolean akivaDismissing = false; // בודק אם אנחנו בהודעת השחרור של עקיבא

    // רשימת ה-NPCs בחדר האוכל (כדי לנקות בסוף)
    private List<NPC> diningNPCs = new ArrayList<>();
    private boolean isTalkingToStatic = false;

    private enum Phase {
        IN_DINING_HALL,
        ON_PATH,
        AKIVA_CHASING,
        AKIVA_INTERROGATION,
        FINISHED
    }

    private Phase phase;

    public DiningHallEvent(Zone diningHall, Zone path) {
        this.diningHall = diningHall;
        this.path = path;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.IN_DINING_HALL;
        completed = false;
        timeInDiningHall = 0;

        world.audio.loadSound("חדר אוכל", "/sounds/חדר אוכל סאונד.wav");
        world.audio.loadSound("חדר אוכל יציאה", "/sounds/חדר אוכל סאונד יציאה.wav");
        world.audio.loadSound("עקיבא מרדף", "/sounds/עקיבא מרדף.wav");
        world.audio.loop("חדר אוכל");

        try {
            BufferedImage milkImg = ImageIO.read(getClass().getResourceAsStream("/images/חלב.png"));
            // עכשיו מעבירים את התמונה לבנאי
            Milk = new Entity(21 * 64, 2 * 64 - 10, 64, 64, milkImg);
        } catch (Exception e) {
            System.out.println("Error loading milk image!");
        }
        // יצירת עקיבא בשביל (נניח מחוץ לחדר האוכל)
        akiva = new Akiva(3 * 64, 7 * 64, 64, 64);
        akivaAI = ScriptedMovementAI.createDiningHallAkivaAI();
        akiva.setMovementAI(akivaAI);
        world.addNPC(akiva);

        diningNPCs.add(new NPC(35 * 64,2 * 64,64,64,1,1));
        diningNPCs.add(new NPC(22 * 64,10 * 64,64,64,3,4));
        diningNPCs.add(new NPC(25 * 64,11* 64,64,64,5,1));
        diningNPCs.add(new NPC(19 * 64,11* 64,64,64,4,1));
        diningNPCs.add(new NPC(20 * 64,8 * 64,64,64,3,1));
        diningNPCs.add(new NPC(28 * 64,4 * 64,64,64,4,1));
        diningNPCs.add(new NPC(23 * 64,14* 64,64,64,2,1));
        for (NPC npc: diningNPCs){
            npc.setMovementAI(new PatrolAI(diningHall));
            world.addNPC(npc);
        }
        diningNPCs.get(0).setAlert(true);
        diningNPCs.get(0).setMovementAI(null);
        diningNPCs.get(1).setAlert(true);
        diningNPCs.get(1).setMovementAI(null);
    }


    @Override
    public void update(GameWorld world) {
        if (completed) return;

        switch (phase) {
            case IN_DINING_HALL:
                handleDiningHallPhase(world);
                handleOutsideOfDiningHall(world);
                break;
            case ON_PATH:
                handlePathPhase(world);
                break;
            case AKIVA_CHASING:
                handleAkivaChase(world);
                break;
            case AKIVA_INTERROGATION:
                handleAkivaInterrogation(world);
                break;
            case FINISHED:
                break;
        }
    }

    private void handleDiningHallPhase(GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();
        timeInDiningHall += deltaTime;

        if (milkInteractionCooldown > 0) {
            milkInteractionCooldown -= deltaTime;
        }

        // 1. הודעת בונוס לחלב (אחרי 20 שניות)
        if (timeInDiningHall >= 5 && !milkMissionReceived) {
            world.getPlayer().addMessage("יוסף משה", "אחי, אתה בחדר אוכל?\n תעשה טובה תביא לי איזה קרטון חלב לחדר.");
            milkMissionReceived = true;
        }

        // 2. הודעה על תחילת הסדר (אחרי 60 שניות)
        if (timeInDiningHall >= 12 && !sederMessageReceived) {
            world.getPlayer().addMessage("הרב מילר", "סדר בוקר מתחיל בבית המדרש.");
            sederMessageReceived = true;
        }

        // 3. אינטראקציה עם שולחן החלב
        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();
        boolean nearMilk = false;

        if (Milk != null){
             nearMilk = (Math.abs(px - Milk.getX()) < 64 && Math.abs(py - Milk.getY()) < 64);
        }
        if (milkMissionReceived && !hasMilk && !isMilkDialogueActive && nearMilk && milkInteractionCooldown <= 0) {
            if (world.getInput().Z_key && !dBox.isVisible() && !world.getPlayer().isPhoneOpen()) {
                world.getPlayer().setInDialogue(true);
                isMilkDialogueActive = true;
                dBox.startDialogueWithChoice("זה קרטון החלב פג תוקף כרגיל. לקחת אותו?", "כן, ברור", "לא, אסור לגנוב");
            }
        }

        // קבלת התשובה לגבי החלב
        if (isMilkDialogueActive && !dBox.isVisible()) {
            int choice = dBox.getFinalChoice();
            if (choice != -1) {
                if (choice == 0) {
                    hasMilk = true;
                    world.getHUD().showTopMessage("לקחת את החלב. כדאי שתיזהר בחוץ...", 3.0);
                    Milk = null;
                }
                else if (choice == 1) {
                    milkInteractionCooldown = 1.0;
                }
                dBox.resetChoice();
                world.getPlayer().setInDialogue(false);
                isMilkDialogueActive = false;
            }
        }

        if (!isTalkingToStatic && world.getPlayer().getDistanceSquared(diningNPCs.get(0)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady() && !world.getPlayer().isPhoneOpen()) {
                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                diningNPCs.get(0).setAlert(false);
                dBox.startDialogue(List.of("שמע בדרך כלל יש פה לפחות קרונפלקס..."));
            }
        }

        if (!isTalkingToStatic && world.getPlayer().getDistanceSquared(diningNPCs.get(1)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady() && !world.getPlayer().isPhoneOpen()) {
                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                diningNPCs.get(1).setAlert(false);
                dBox.startDialogue(List.of("למישהוא לא היה כוח לצייר כיסאות כנראה"));
            }
        }

        if (isTalkingToStatic && !dBox.isVisible()) {
            world.getPlayer().setInDialogue(false);
            isTalkingToStatic = false;
        }

        // 4. יציאה מחדר האוכל (מתחילים ללכת בשביל)
        if (!diningHall.contains(px, py)) {
            if (timeInDiningHall >= 15.0) {
                phase = Phase.ON_PATH;
                world.audio.stop("חדר אוכל");
                world.audio.play("חדר אוכל יציאה");
            }else {
                if (!OutsideOfDiningHall){
                    OutsideOfDiningHall = true;
                    world.audio.stop("חדר אוכל");
                    world.audio.play("חדר אוכל יציאה");
                }
            }
        }
    }

    private void handleOutsideOfDiningHall(GameWorld world){
        if (OutsideOfDiningHall){
            float px = world.getPlayer().getX();
            float py = world.getPlayer().getY();
            if (diningHall.contains(px,py)){
                world.audio.loop("חדר אוכל");
                world.audio.stop("חדר אוכל יציאה");
                OutsideOfDiningHall = false;
            }
            if (path.contains(px,py)){
                fail(7);
                world.audio.stopAll();
            }
        }
    }


    private void handlePathPhase(GameWorld world) {
        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();

        // אם השחקן הגיע לבית המדרש (שרד את השביל)
        if (path.contains(px, py) && finishAkivaChase) {
            finishEvent();
            return;
        }

        // אם השחקן גנב חלב ועקיבא רואה אותו
        if (hasMilk && akiva.canSee(world.getPlayer())) {
            phase = Phase.AKIVA_CHASING;
            akiva.setMovementAI(null);
            world.getHUD().showTopMessage("עקיבא קלט אותך!", 2.0);
            world.audio.loop("עקיבא מרדף");
            world.getPlayer().setSpeed(250.0f);
        }
    }

    private void handleAkivaChase(GameWorld world) {
        // עקיבא במרדף
        akiva.chasePlayer(world.getPlayer());

        // אם תפס אותנו - עוברים לחקירה
        if (akiva.hasCaughtPlayer(world.getPlayer())) {
            akiva.calmDown();
            phase = Phase.AKIVA_INTERROGATION;
            world.audio.stop("עקיבא מרדף");
        }
    }

    private void handleAkivaInterrogation(GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // 1. מתחילים את החקירה
        if (!isAkivaDialogueActive && !akivaDismissing) {
            world.getPlayer().setInDialogue(true);
            isAkivaDialogueActive = true;

            dBox.startDialogueWithChoice(
                    "זה נראה שאתה מחביא משהוא בחולצה , זה לא חלב נכון?!",
                    "חנוך ביקש ממני להביא אחד לפינת קפה",
                    "אהה... כן, רציתי לקחת אחד לחדר"
            );
        }

        if (isAkivaDialogueActive && dBox.isReady()) {
            int choice = dBox.getFinalChoice();
            if (choice != -1) {
                if (choice == 0) {
                    // התשובה הנכונה
                    dBox.startDialogue(List.of("אה לפינת קפה, טוב בסדר אם חנוך ביקש..."));
                    hasMilk = false;
                    akivaDismissing = true;
                } else if (choice == 1) {
                    world.audio.stopAll();
                    fail(3);
                }

                dBox.resetChoice();
                isAkivaDialogueActive = false;
            }
        }

        if (akivaDismissing && dBox.isReady()) {
            world.getPlayer().setInDialogue(false);

            // מחזירים לעקיבא את הפטרול הרגיל שלו!
            akiva.setSpeed(150.0f);
            akiva.setMovementAI(ScriptedMovementAI.createDiningHallAkivaAI());
            phase = Phase.ON_PATH;
            akivaDismissing = false;
            finishAkivaChase = true;
            world.getPlayer().setSpeed(350.0f);
        }
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
    }

    @Override
    public void onExit(GameWorld world) {
        if (Milk == null){
            world.getStoryManager().setPlayerHasMilk(true);
        }

        if (akiva != null) world.removeNPC(akiva);
        for (NPC n : diningNPCs) world.removeNPC(n);
        diningNPCs.clear();
    }

    @Override
    public void render(Graphics2D g) {
        if (Milk != null) {
            Milk.Render(g);
        }
    }
}