package story;

import ai.PatrolAI;
import entities.NPC;
import entities.Player;
import npcs.Kroyzer;
import npcs.Miller;
import world.GameWorld;
import world.Zone;
import hud.InteractiveDialogueBox; // ייבוא של התיבה החדשה

import java.util.ArrayList;
import java.util.List;

import static engine.Time.deltaTime;

public class ShacharitEvent extends GameState {

    private Zone beitMidrash;
    // טיימרים
    private double timeToArrive = 15.0;
    private double prayerDuration = 30.0;

    // NPC מיוחד לרישום
    private NPC registerNpc;
    private boolean isTalkingToStatic= false;// משתנה חדש לבדיקה אם אנחנו באמצע שיחה

    private Miller miller;
    private Kroyzer kroyzer;

    private List<NPC> worshippers = new ArrayList<>();

    private enum Phase {
        RACING_TO_PRAYER,
        PRAYING,
        WAITING_FOR_BREAKFAST_CONFIRMATION,
        TRANSITIONING,
        FINISHED
    }

    private Phase phase;

    public ShacharitEvent(Zone beitMidrash) {
        this.beitMidrash = beitMidrash;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.RACING_TO_PRAYER;
        completed = false;

        world.audio.loadSound("תפילה","/sounds/תפילת שחרית.wav");

        world.getPlayer().addMessage("הרב מילר", "אני בתפילה, כולם צריכים להגיע מיד ,\n מי שלא יהיה פה תוך חצי דקה ילקח לו הטלפון ל24 שעות!");

        miller = new Miller(16 * 64, 26 * 64, 64, 64);
        world.addNPC(miller);
        kroyzer = new Kroyzer(25 * 64, 26 * 64, 64, 64);
        world.addNPC(kroyzer);

        NPC MoveWorshipper1 = new NPC(17 * 64, 38 * 64, 64, 64, 5, 4);
        worshippers.add(MoveWorshipper1);
        NPC MoveWorshipper2 = new NPC(19 * 64, 38 * 64, 64, 64, 6, 4);
        worshippers.add(MoveWorshipper2);
        NPC MoveWorshipper3 = new NPC(22 * 64, 38 * 64, 64, 64, 3, 4);
        worshippers.add(MoveWorshipper3);

        NPC worshipper   = new NPC(15 * 64, 32 * 64, 64, 64, 4, 4);
        worshippers.add(worshipper);
        NPC worshipper1  = new NPC(14 * 64, 35 * 64, 64, 64, 2, 4);
        worshippers.add(worshipper1);
        NPC worshipper2  = new NPC(16 * 64, 29 * 64, 64, 64, 3, 4);
        worshippers.add(worshipper2);
        NPC worshipper3  = new NPC(20 * 64, 32 * 64, 64, 64, 1, 4);
        worshippers.add(worshipper3);
        NPC worshipper4  = new NPC(21 * 64, 35 * 64, 64, 64, 5, 4);
        worshippers.add(worshipper4);
        NPC worshipper5  = new NPC(20 * 64, 29 * 64, 64, 64, 5, 4);
        worshippers.add(worshipper5);
        NPC worshipper6  = new NPC(19 * 64, 38 * 64, 64, 64, 5, 4);
        worshippers.add(worshipper6);
        NPC worshipper7  = new NPC(24 * 64, 32 * 64, 64, 64, 3, 4);
        worshippers.add(worshipper7);
        NPC worshipper8  = new NPC(25 * 64, 35 * 64, 64, 64, 2, 4);
        worshippers.add(worshipper8);
        NPC worshipper9  = new NPC(25 * 64, 29 * 64, 64, 64, 4, 4);
        worshippers.add(worshipper9);
        NPC worshipper10 = new NPC(24 * 64, 38 * 64, 64, 64, 4, 4);
        worshippers.add(worshipper10);

        registerNpc = new NPC(15 * 64,38 * 64,64,64,1,4);
        registerNpc.setAlert(true);
        worshippers.add(registerNpc);

        for (int i = 0; i < worshippers.size(); i++) {
            world.addNPC(worshippers.get(i));
            if (i < 3){
                worshippers.get(i).setMovementAI(new PatrolAI(beitMidrash));
            }
        }
        worshippers.get(12).setAlert(true);
        worshippers.get(7).setAlert(true);

        world.getHUD().showTimer("זמן לכניסה לתפילה", 15);
    }



    @Override
    public void update(GameWorld world) {
        if (completed) return;

        switch (phase) {
            case RACING_TO_PRAYER:
                handleRacingPhase(world);
                break;

            case PRAYING:
                handlePrayingPhase(world);
                handleStaticNpcDialogues(world.getPlayer(),world);
                break;

            case WAITING_FOR_BREAKFAST_CONFIRMATION:
                handleStaticNpcDialogues(world.getPlayer(),world);
                handleBreakfastTransition(world);
                break;

            case TRANSITIONING:
                handleTransition(world);
                break;

            case FINISHED:
                break;
        }
    }

    private void handleRacingPhase(GameWorld world) {
        timeToArrive -= deltaTime;

        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();

        if (beitMidrash.contains(px, py)) {
            world.audio.loop("תפילה");
            phase = Phase.PRAYING;
            world.getHUD().hideTimer();
            return;
        }

        if (timeToArrive <= 0) {
            world.audio.stopAll();
            fail(2);
        }
    }

    private void handleStaticNpcDialogues(Player player, GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // 1. בדיקה אם אנחנו רוצים להתחיל לדבר
        if (!isTalkingToStatic && world.getPlayer().getDistanceSquared(registerNpc) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {

                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                registerNpc.setAlert(false);

                List<String> lines = new ArrayList<>();
                lines.add("שמע, אני כותב לכולם וי, אתה לא באמת צריך להירשם.");
                lines.add("רק תעשה טובה, אל תספר למילר, כן?");
                dBox.startDialogue(lines);

            }
        }

        if (!isTalkingToStatic && world.getPlayer().getDistanceSquared(worshippers.get(12)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {

                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                worshippers.get(12).setAlert(false);
                dBox.startDialogue(List.of("שמע אחי היה רבע גמר ליגת האלופות אתמול,\n אלא שהקקות האלו החליטו לשחק ב4 בלילה, קיצר אני גמור מת"));
            }
        }

        if (!isTalkingToStatic && world.getPlayer().getDistanceSquared(worshippers.get(7)) < (64 * 64)) {
            if (world.getInput().Z_key && dBox.isReady()) {

                world.getPlayer().setInDialogue(true);
                isTalkingToStatic = true;
                worshippers.get(7).setAlert(false);
                dBox.startDialogue(List.of("מה הקטע של הפינת קפה אם בחיים אין בה \n לא קפה, לא חלב, לא כפיות, ולא סוכר?!"));
            }
        }

        // 2. בדיקה אם השיחה הסתיימה
        if (isTalkingToStatic && !dBox.isVisible()) {
            world.getPlayer().setInDialogue(false);
            isTalkingToStatic = false;
        }
    }

    private void handlePrayingPhase(GameWorld world) {
        prayerDuration -= deltaTime;

        if (prayerDuration <= 0) {
            world.getPlayer().addMessage("עקיבא", "ארוחת הבוקר מוכנה בחדר האוכל. כולם לבוא! \n למעבר לחדר אוכל לחץ ENTER");
            phase = Phase.WAITING_FOR_BREAKFAST_CONFIRMATION;
        }
    }

    private void handleBreakfastTransition(GameWorld world) {
        if (world.getInput().E_key){
            phase = Phase.TRANSITIONING;
        }
    }

    private void handleTransition(GameWorld world) {
        float currentAlpha = world.getFadeAlpha();
        world.getPlayer().setPhoneOpen(false);
        if (currentAlpha < 1.0f) {
            world.setFadeAlpha(currentAlpha + (float)deltaTime * 0.7f);

            if (world.audio != null) {
                world.audio.setVolume("תפילה", 1.0f - world.getFadeAlpha());
            }
        }
        else {
            teleportToDiningHall(world);
            world.setFadeAlpha(0);
            finishEvent();
        }
    }

    private void teleportToDiningHall(GameWorld world) {
        world.getPlayer().setX(16 * 64);
        world.getPlayer().setY(8 * 64);
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
    }

    @Override
    public void onExit(GameWorld world) {
        if (registerNpc != null) {
            world.removeNPC(registerNpc);
        }
        if (miller != null) {
            world.removeNPC(miller);
        }
        if (kroyzer != null) {
            world.removeNPC(kroyzer);
        }

        // מחיקת כל המתפללים מהעולם
        for (NPC w : worshippers) {
            world.removeNPC(w);
        }
        worshippers.clear();

        world.audio.stop("תפילה");
    }
}

