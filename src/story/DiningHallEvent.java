package story;

import ai.ChaseAI;
import ai.MovementAI;
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

import static engine.Time.deltaTime;

public class DiningHallEvent extends GameState {

    private Zone diningHall;
    private Zone path;

    // טיימרים ודגלים
    private double timeInDiningHall = 0;
    private boolean milkMissionReceived = false;
    private boolean sederMessageReceived = false;
    private double milkInteractionCooldown = 0;


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

            try {
                BufferedImage milkImg = ImageIO.read(getClass().getResourceAsStream("/images/milk.png"));
                // עכשיו מעבירים את התמונה לבנאי
                Milk = new Entity(21 * 64, 2 * 64, 64, 64, milkImg);
            } catch (Exception e) {
                System.out.println("Error loading milk image!");
            }
            // יצירת עקיבא בשביל (נניח מחוץ לחדר האוכל)
            akiva = new Akiva(3 * 64, 7 * 64, 64, 64);
            akivaAI = ScriptedMovementAI.createDiningHallAkivaAI();
            akiva.setMovementAI(akivaAI);
            world.addNPC(akiva);

        // כאן תוכל להוסיף את שאר ה-NPCs שיושבים ומסתובבים בחדר האוכל בלולאה
        // ולדחוף אותם לתוך diningNPCs...
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;

        switch (phase) {
            case IN_DINING_HALL:
                handleDiningHallPhase(world);
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
            world.getPlayer().addMessage("יוסף משה", "אחי, אתה בחדר אוכל? תעשה טובה תביא לי איזה קרטון חלב לחדר.");
            milkMissionReceived = true;
        }

        // 2. הודעה על תחילת הסדר (אחרי 60 שניות)
        if (timeInDiningHall >= 20 && !sederMessageReceived) {
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
        if (milkMissionReceived && !hasMilk && !isMilkDialogueActive && nearMilk&& milkInteractionCooldown <= 0) {
            if (world.getInput().E_key && !dBox.isVisible()) {
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

        // 4. יציאה מחדר האוכל (מתחילים ללכת בשביל)
        if (!diningHall.contains(px, py) && sederMessageReceived) {
            phase = Phase.ON_PATH;
        }
    }

    private void handlePathPhase(GameWorld world) {
        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();

        // אם השחקן הגיע לבית המדרש (שרד את השביל)
        if (path.contains(px, py)) {
            finishEvent();
            return;
        }

        // אם השחקן גנב חלב ועקיבא רואה אותו
        if (hasMilk && akiva.canSee(world.getPlayer())) {
            phase = Phase.AKIVA_CHASING;
            akiva.setMovementAI(null);
            world.getHUD().showTopMessage("עקיבא קלט אותך!", 2.0);
            // אפשר להוסיף פה סאונד של מתח/מרדף!
        }
    }

    private void handleAkivaChase(GameWorld world) {

        // עקיבא במרדף
        akiva.chasePlayer(world.getPlayer());

        // אם תפס אותנו - עוברים לחקירה
        if (akiva.hasCaughtPlayer(world.getPlayer())) {
            akiva.calmDown(); // <--- התיקון הקריטי! מכבה את ה-AI של המרדף ועוצר אותו
            phase = Phase.AKIVA_INTERROGATION;
        }
    }

    private void handleAkivaInterrogation(GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // 1. מתחילים את החקירה
        if (!isAkivaDialogueActive && !akivaDismissing) {
            world.getPlayer().setInDialogue(true);
            isAkivaDialogueActive = true;

            dBox.startDialogueWithChoice(
                    "עצור מיד! ראיתי שלקחת משהו מחדר האוכל. גנבת חלב?!",
                    "זה מים קרים",
                    "אהה... כן, מצטער"
            );
        }

        // 2. מחכים לתשובה של השחקן (שמנו לב שזה Ready ולא רק visible)
        if (isAkivaDialogueActive && dBox.isReady()) { // <--- תיקון לטיימר ההתקררות!
            int choice = dBox.getFinalChoice();
            if (choice != -1) {
                if (choice == 0) {
                    // התשובה הנכונה
                    dBox.startDialogue(List.of("מים קרים אה? בסדר, אבל שאני לא אתפוס אותך פעם הבאה. סע לבית מדרש."));
                    hasMilk = false;
                    akivaDismissing = true;
                    // לא צריך akiva.setAlert(false) כי calmDown כבר עשה את זה :)
                } else if (choice == 1) {
                    fail(3);
                }

                dBox.resetChoice();
                isAkivaDialogueActive = false;
            }
        }

        // 3. מחכים שההודעה האחרונה של עקיבא תסתיים (שוב, עם isReady להגנה מספאם)
        if (akivaDismissing && dBox.isReady()) { // <--- תיקון!
            world.getPlayer().setInDialogue(false);

            // מחזירים לעקיבא את הפטרול הרגיל שלו!
            akiva.setSpeed(150.0f);
            akiva.setMovementAI(ScriptedMovementAI.createDiningHallAkivaAI());
            phase = Phase.ON_PATH;
            akivaDismissing = false;
        }
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
        System.out.println("סיום שלב חדר האוכל - מעבר לסדר בוקר.");
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
        // בודקים שהחלב קיים (עוד לא נלקח על ידי השחקן)
        if (Milk != null) {
            Milk.Render(g); // קוראים לפעולת הציור המקורית של Entity!
        }
    }
}
