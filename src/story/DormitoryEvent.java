package story;

import ai.ChaseAI;
import entities.NPC;
import hud.HUD;
import hud.MassageBoxTop;
import npcs.Sanans;
import ai.ScriptedMovementAI;
import world.GameWorld;
import world.Zone;
import ai.PatrolAI;
import ui.GameScreen;
import static main.Game.deltaTime;
import entities.Player;

import java.util.ArrayList;
import java.util.List;

public class DormitoryEvent extends GameState {

    private Zone dormRoom;
    private Zone bathroom;
    private Zone corridor;
    private Zone secondedormRoom;
    private Zone thirdDormRoom;

    private List<NPC> DormitoryNpcs = new ArrayList<>();
    private Sanans sanans;
    private ScriptedMovementAI sanansAI;

    private enum Phase {WAITING_FOR_SANANS, SANANS_IN_DORM, SANANS_MOVED_TO_OTHER_ROOM, FINISHED}

    private Phase phase = Phase.WAITING_FOR_SANANS;

    public DormitoryEvent(Zone dormRoom, Zone bathroom, Zone corridor, Zone secondeDormRoom, Zone thirdDormRoom) {
        this.dormRoom = dormRoom;
        this.bathroom = bathroom;
        this.corridor = corridor;
        this.secondedormRoom = secondeDormRoom;
        this.thirdDormRoom = thirdDormRoom;
    }

    @Override
    public void onEnter(GameWorld world) {
        phase = Phase.WAITING_FOR_SANANS;
        completed = false;

        DormitoryNpcs.add(new NPC(25 * 64,52 * 64,64,64,5,1));
        DormitoryNpcs.get(0).setMovementAI(new PatrolAI(thirdDormRoom));
        world.addNPC(DormitoryNpcs.get(0));
        DormitoryNpcs.add(new NPC(21 * 64,42 * 64,64,64,3,1));
        DormitoryNpcs.get(1).setMovementAI(new PatrolAI(secondedormRoom));
        world.addNPC(DormitoryNpcs.get(1));
        DormitoryNpcs.add(new NPC(26 * 64,50 * 64,64,64,4,1));
        DormitoryNpcs.get(2).setMovementAI(new PatrolAI(thirdDormRoom));
        world.addNPC(DormitoryNpcs.get(2));
        DormitoryNpcs.add(new NPC(17 * 64,55 * 64,64,64,4,4));
        world.addNPC(DormitoryNpcs.get(3));
        DormitoryNpcs.add(new NPC(14 * 64,55 * 64,64,64,4,4));
        world.addNPC(DormitoryNpcs.get(4));
        DormitoryNpcs.add(new NPC(12 * 64,51 * 64,64,64,4,1));
        world.addNPC(DormitoryNpcs.get(5));
        DormitoryNpcs.add(new NPC(10 * 64,41 * 64,64,64,4,1));
        world.addNPC(DormitoryNpcs.get(6));
        DormitoryNpcs.add(new NPC(16 * 64,45 * 64,64,64,4,4));
        world.addNPC(DormitoryNpcs.get(7));


        sanans = new Sanans(8 * 64, 47 * 64, 64, 64);
        sanansAI = ScriptedMovementAI.createDormitorySanansAI();
        sanans.setSanansScriptedMovement(sanansAI);
        world.addNPC(sanans);


        world.audio.loadSound("סננס","/sounds/סננס סאונד.wav");
        world.audio.play("סננס");
        world.getHUD().showTopMessage("סננס מתקרב לחדר! עליך להתחבא מהר לפני שהוא נכנס \n אתה יכול גם לצאת למסדרון כדי להתחמק ממנו", 5.0); // - ככה מוסיפים הודעה על המסך
    }

    @Override
    public void update(GameWorld world) {
        if (completed) return;

        switch (phase) {

            case WAITING_FOR_SANANS:
                handleWaitingPhase(world);
                break;

            case SANANS_IN_DORM:
                monitorDormDanger(world);
                break;

            case SANANS_MOVED_TO_OTHER_ROOM:
                monitorOtherRoomDanger(world);
                break;

            case FINISHED:
                break;
        }
    }

    private void handleWaitingPhase(GameWorld world) {

        float sx = sanans.getX();
        float sy = sanans.getY();

        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();

        boolean sanansInDorm = dormRoom.contains(sx, sy);
        boolean playerInDorm = dormRoom.contains(px, py);
        boolean playerInBathroom = bathroom.contains(px, py);
        boolean playerInCorridor = corridor.contains(px, py);

        // השחקן יצא למסדרון לפני שסננס נכנס
        if (playerInCorridor && !sanansInDorm) {
            phase = Phase.SANANS_MOVED_TO_OTHER_ROOM;
            moveSanansToOtherRoom1();
            return;
        }

        // סננס נכנס לחדר
        if (sanansInDorm) {
            if (playerInDorm) {
                failPlayer(world);
                return;
            }
            phase = Phase.SANANS_IN_DORM;
        }
    }

    private void monitorDormDanger(GameWorld world) {

        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();

        boolean sanansInDorm = dormRoom.contains(sanans.getX(), sanans.getY());

        if (!sanansInDorm) {
            moveSanansToOtherRoom();
            phase = Phase.SANANS_MOVED_TO_OTHER_ROOM;
        }
        // השחקן נכנס לחדר בזמן שסננס שם
        if (dormRoom.contains(px, py)) {
            failPlayer(world);
        }
    }

    private void monitorOtherRoomDanger(GameWorld world) {

        float sx = sanans.getX();
        float sy = sanans.getY();

        float px = world.getPlayer().getX();
        float py = world.getPlayer().getY();

        boolean sanansInSecondRoom = secondedormRoom.contains(sx, sy);

        boolean playerInSecondRoom = secondedormRoom.contains(px, py);

        // אם סננס נכנס לחדר השני והשחקן כבר שם → פסילה
        if (sanansInSecondRoom && playerInSecondRoom) {
            failPlayer(world);
        }
        if (sanansAI.isFinished()) {
            finishEvent();
        }
    }

    private void moveSanansToOtherRoom() {
        sanansAI = ScriptedMovementAI.createDormitorySanansAI2();
        sanans.setSanansScriptedMovement(sanansAI);
    }
    private void moveSanansToOtherRoom1() {
        sanansAI = ScriptedMovementAI.createDormitorySanansAI3();
        sanans.setSanansScriptedMovement(sanansAI);
    }

    private void failPlayer(GameWorld world) {
        world.audio.stopAll();
        fail(1);
    }

    private void finishEvent() {
        completed = true;
        phase = Phase.FINISHED;
    }

    @Override
    public void onExit(GameWorld world) {

        if (sanans != null) {
            sanans.deactivate();
            world.removeNPC(sanans);
        }
        for (NPC n : DormitoryNpcs) {
            world.removeNPC(n);
        }
        DormitoryNpcs.clear();

        world.audio.stop("סננס");
    }
}
