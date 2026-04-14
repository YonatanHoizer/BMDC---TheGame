package story;

import ai.MovementAI;
import entities.NPC;
import entities.Player;
import ai.ScriptedMovementAI; // במידה ותצטרך לניווט של תלמידים
import npcs.Kroyzer;
import npcs.Miller;
import world.GameWorld;
import world.Zone;
import static main.Game.deltaTime;
import java.awt.Color;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;


public class LessonEvent extends GameState {

    private Zone classroom;
    private Zone playerSeat; // האזור הקטן שבו השחקן צריך לעמוד כדי "לשבת"

    // טיימרים
    private double timeToEnterClass = 15.0;
    private double surviveChaseTimer = 20.0;
    private double sleepTimer = 10.0; // 10 שניות של הירדמות

    // ניהול מסרים בזמן ההירדמות
    private double lessonMessageTimer = 0;
    private int lessonMessageIndex = 0;
    private String[] kroizerLessonMessages = {
            "וכמו שאנחנו רואים בסוגיה...",
            "רבא אומר כאן דבר מעניין מאוד...",
            "אבל תוספות מקשים על זה...",
            "..." // השחקן נרדם
    };

    // דמויות
    private Kroyzer kroyzer;
    private List<NPC> classNPCs = new ArrayList<>();
    private List<NPC> allSederNpcs = new ArrayList<>();
    private List<NPC> SomeSederNpcs = new ArrayList<>();
    private Miller miller;

    // ניהול מצבים
    private enum Phase {
        WAITING_TO_ENTER,
        IN_CLASS,
        FALLING_ASLEEP,
        KROIZER_CHASE,
        FINISHED
    }
    private Phase phase = Phase.WAITING_TO_ENTER;

    public LessonEvent(Zone classroom, Zone playerSeat,List<NPC> allSederNpcs ,Miller miller) {
        this.classroom = classroom;
        this.playerSeat = playerSeat;
        this.allSederNpcs = allSederNpcs;
        this.miller = miller;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.WAITING_TO_ENTER;
        completed = false;

        world.getPlayer().addMessage("הרב קרוייזר", "השיעור היום מתחיל עוד מעט בכיתה. כולם להזדרז להגיע!");
        world.audio.loadSound("מרדף קרוייזר","/sounds/עקיבא מרדף.wav");

        kroyzer = new Kroyzer(34 * 64, 26 * 64, 64, 64);
        world.addNPC(kroyzer);

        if (allSederNpcs != null) {
            for (int i = 0; i < allSederNpcs.size(); i++) {
                NPC npc = allSederNpcs.get(i);
                if (i < 3) {

                    // חשוב מאוד: יוצרים AI חדש לכל אחד!
                    ScriptedMovementAI walkAI = ScriptedMovementAI.createWalkToClassAI();
                    npc.setSpeed(350);
                    npc.setMovementAI(walkAI);

                    // מוסיפים לרשימת הרצים
                    SomeSederNpcs.add(npc);
                }
            }
        }

        NPC classNpc2 = new NPC(31 * 64, 31 * 64, 64, 64, 1, 4);
        NPC classNpc4 = new NPC(36 * 64, 31 * 64, 64, 64, 4, 4);
        NPC classNpc5 = new NPC(31 * 64, 28 * 64, 64, 64, 1, 4);
        NPC classNpc6 = new NPC(32 * 64, 28 * 64, 64, 64, 3, 4);
        NPC classNpc7 = new NPC(36 * 64, 28 * 64, 64, 64, 2, 4);
        NPC classNpc8 = new NPC(37 * 64, 28 * 64, 64, 64, 3, 4);
        classNPCs.add(classNpc2);
        classNPCs.add(classNpc4);
        classNPCs.add(classNpc5);
        classNPCs.add(classNpc6);
        classNPCs.add(classNpc7);
        classNPCs.add(classNpc8);
        world.addNPC(classNpc2);
        world.addNPC(classNpc4);
        world.addNPC(classNpc5);
        world.addNPC(classNpc6);
        world.addNPC(classNpc7);
        world.addNPC(classNpc8);

        world.getHUD().showTimer("זמן לכניסה לכיתה", 15);
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;

        Player player = world.getPlayer();

        switch (phase) {
            case WAITING_TO_ENTER:
                handleWaitingPhase(player, world);
                break;
            case IN_CLASS:
                handleInClassPhase(player, world);
                break;
            case FALLING_ASLEEP:
                handleSleepPhase(player, world);
                break;
            case KROIZER_CHASE:
                handleChasePhase(player, world);
                break;
            case FINISHED:
                break;
        }
    }

    private void handleWaitingPhase(Player player, GameWorld world) {
        timeToEnterClass -= deltaTime;

        if (!SomeSederNpcs.isEmpty()) {
            boolean anyoneFinished = false;
        }

        // 1. השחקן נכנס לכיתה בזמן
        if (classroom.contains(player.getX(), player.getY())) {
            phase = Phase.IN_CLASS;
            world.getHUD().hideTimer();
            world.getHUD().showTopMessage("הרב קרויזר: 'ברוך הבא, שב במקומך בבקשה.'", 4.0);
            return;
        }

        // 2. השחקן איחר - מתחיל מרדף!
        if (timeToEnterClass <= 0) {
            phase = Phase.KROIZER_CHASE;
            world.getHUD().hideTimer();
            world.getHUD().showTopMessage("איחרת לשיעור! קרויזר בעקבותיך. שרוד 20 שניות!", 4.0);
            kroyzer.startChase(player);
            world.audio.loop("מרדף קרוייזר");
        }
    }

    private void handleInClassPhase(Player player, GameWorld world) {
        // אם השחקן הגיע למקום שלו והתיישב
        if (playerSeat.contains(player.getX(), player.getY())) {
            phase = Phase.FALLING_ASLEEP;
            player.setInDialogue(true); // נועלים את תנועת השחקן
            player.stop();
        }
    }

    private void handleSleepPhase(Player player, GameWorld world) {
        sleepTimer -= deltaTime;
        lessonMessageTimer -= deltaTime;

        // מקפיצים הודעות של השיעור כל 2.5 שניות
        if (lessonMessageTimer <= 0 && lessonMessageIndex < kroizerLessonMessages.length) {
            world.getHUD().showTopMessage("הרב קרויזר: " + kroizerLessonMessages[lessonMessageIndex], 2.5);
            lessonMessageIndex++;
            lessonMessageTimer = 2.5;
        }

        if (sleepTimer <= 3.0) {
            float currentAlpha = world.getFadeAlpha();
            // מעלים את רמת השחור בהדרגה (כמו שעשית בשחרית)
            if (currentAlpha < 1.0f) {
                world.setFadeAlpha(currentAlpha + (float)deltaTime * 0.4f);
            }
        }

        // כשהטיימר מגיע ל-0: "התעוררות" (Time Skip)
        if (sleepTimer <= 0) {

            if (kroyzer != null) {
                world.removeNPC(kroyzer);
            }
            if (miller != null) {
                world.removeNPC(miller);
            }
            for (NPC npc : classNPCs) {
                world.removeNPC(npc);
            }
            classNPCs.clear();
            for (NPC npc : allSederNpcs) {
                world.removeNPC(npc);
            }
            allSederNpcs.clear();
            for (NPC npc : SomeSederNpcs) {
                world.removeNPC(npc);
            }
            SomeSederNpcs.clear();

            // משחררים את השחקן ומודיעים על ארוחת צהריים
            player.setInDialogue(false);
            finishEventWithLunch(world);
            world.setFadeAlpha(0f);
            world.getHUD().showTopMessage("לא היית מסוגל להשאיר את העיינים פתוחות יותר מכמה שניות ונרדמת, כולם כבר הלכו", 4.0);
        }
    }

    private void handleChasePhase(Player player, GameWorld world) {
        surviveChaseTimer -= deltaTime;

        // 1. קרויזר תפס את השחקן
        if (kroyzer.hasCaughtPlayer(player)) {
            fail(5); // פסילה סוג 5
            world.audio.stopAll();
            return;
        }

        // 2. השחקן שרד 30 שניות!
        if (surviveChaseTimer <= 0) {
            kroyzer.stopChase();
            world.audio.stop("מרדף קרוייזר");
            world.getHUD().showTopMessage("שרדת! קרויזר התייאש וחזר לכיתה.", 4.0);
            finishEventWithLunch(world);
        }
    }

    private void finishEventWithLunch(GameWorld world) {
        completed = true;
        phase = Phase.FINISHED;
        world.getPlayer().addMessage("עקיבא", "ארוחת צהריים עכשיו בחדר אוכל ,מי שלא יגיע בזמן לא ישאר לו.");
    }

    @Override
    public void onExit(GameWorld world) {
        // ניקוי ביטחון למקרה שסיימנו בגלל מרדף ולא בגלל שינה

        if (miller != null) {
            world.removeNPC(miller);
        }
        for (NPC npc : classNPCs) {
            world.removeNPC(npc);
        }
        classNPCs.clear();
        for (NPC npc : allSederNpcs) {
            world.removeNPC(npc);
        }
        allSederNpcs.clear();
        for (NPC npc : SomeSederNpcs) {
            world.removeNPC(npc);
        }
        SomeSederNpcs.clear();

        if (kroyzer != null){
            world.getStoryManager().setKroyzer(this.kroyzer);
        }

    }
}