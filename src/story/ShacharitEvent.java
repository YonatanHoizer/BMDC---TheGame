package story;

import entities.NPC;
import world.GameWorld;
import world.Zone;
import hud.InteractiveDialogueBox; // ייבוא של התיבה החדשה

import java.util.ArrayList;
import java.util.List;

import static engine.Time.deltaTime;

public class ShacharitEvent extends GameState {

    private Zone beitMidrash;
    // טיימרים
    private double timeToArrive = 30.0;
    private double prayerDuration = 30.0;

    // NPC מיוחד לרישום
    private NPC registerNpc;
    private boolean spokeToRegisterNpc = false;
    private boolean isTalkingToRegisterNpc = false; // משתנה חדש לבדיקה אם אנחנו באמצע שיחה

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

        world.audio.loadSound("נחירות","/sounds/נחירות.wav");

        world.getPlayer().addMessage("הרב מילר", "אני בתפילה, כולם צריכים להגיע מיד ,\n מי שלא יהיה פה תוך חצי דקה ילקח לו הטלפון ל24 שעות!");

        registerNpc = new NPC(15 * 64,38 * 64,64,64,1,4);
        registerNpc.setAlert(true);
        world.addNPC(registerNpc);

        for (int j = 0; j < 3; j++){
            for (int i = 0; i < 6; i++) {
                int posX = (14 + (i / 2) * 4 + (i % 2)) * 64;
                int posY = (29 + (j * 3)) * 64;

                NPC worshipper = new NPC(posX, posY, 64, 64, (i + j) % 4, 4);
                world.addNPC(worshipper);
                worshippers.add(worshipper);
            }
        }
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
                break;

            case WAITING_FOR_BREAKFAST_CONFIRMATION:
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
            world.audio.loop("נחירות");
            phase = Phase.PRAYING;
            return;
        }

        if (timeToArrive <= 0) {
            failPlayerType2(world);
        }
    }

    private void handlePrayingPhase(GameWorld world) {
        InteractiveDialogueBox dBox = world.getHUD().getDialogueBox();

        // 1. בדיקה אם אנחנו רוצים להתחיל לדבר
        if (!spokeToRegisterNpc && !isTalkingToRegisterNpc && world.getPlayer().getDistanceSquared(registerNpc) < (100 * 100)) {
            if (world.getInput().E_key && dBox.isReady()) {

                world.getPlayer().setInDialogue(true);
                isTalkingToRegisterNpc = true;
                registerNpc.setAlert(false);

                List<String> lines = new ArrayList<>();
                lines.add("שמע, אני כותב לכולם וי, אתה לא באמת צריך להירשם.");
                lines.add("רק תעשה טובה, אל תספר למילר, כן?");
                dBox.startDialogue(lines);

            }
        }

        // 2. בדיקה אם השיחה הסתיימה
        if (isTalkingToRegisterNpc && !dBox.isVisible()) {
            world.getPlayer().setInDialogue(false);
            isTalkingToRegisterNpc = false;
        }

        // 3. קידום הטיימר - רק אם אנחנו *לא* באמצע שיחה!
        if (!isTalkingToRegisterNpc) {
            prayerDuration -= deltaTime;

            if (prayerDuration <= 0) {
                System.out.println("התפילה הסתיימה.");
                world.getPlayer().addMessage("עקיבא", "ארוחת הבוקר מוכנה בחדר האוכל. כולם לבוא! \n למעבר לחדר אוכל לחץ E");
                phase = Phase.WAITING_FOR_BREAKFAST_CONFIRMATION;
            }
        }
    }

    private void handleBreakfastTransition(GameWorld world) {
        if (world.getPlayer().isPhoneOpen()) {
            boolean readingBreakfastMsg = world.getHUD().getPhoneUI().isViewingMessageContaining("ארוחת הבוקר מוכנה");
            if (readingBreakfastMsg) {
                if (world.getInput().E_key){
                    phase = Phase.TRANSITIONING;
                }
            }
        }
    }

    private void handleTransition(GameWorld world) {
        float currentAlpha = world.getFadeAlpha();

        if (currentAlpha < 1.0f) {
            world.setFadeAlpha(currentAlpha + (float)deltaTime * 0.7f);

            if (world.audio != null) {
                world.audio.setVolume("נחירות", 1.0f - world.getFadeAlpha());
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

    private void failPlayerType2(GameWorld world) {
        System.out.println("נפסלת – סוג 2: לא הגעת לתפילה בזמן.");
        fail(2);
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

        // מחיקת כל המתפללים מהעולם
        for (NPC w : worshippers) {
            world.removeNPC(w);
        }
        worshippers.clear();

        world.audio.stop("נחירות");
    }
}

