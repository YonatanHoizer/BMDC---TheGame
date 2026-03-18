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
import static engine.Time.deltaTime;
import entities.Player;

public class DormitoryEvent extends GameState {

    private Zone dormRoom;
    private Zone bathroom;
    private Zone corridor;
    private Zone secondedormRoom;
    private Zone thirdDormRoom;

    private NPC npc1;
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

        npc1 = new NPC(25 * 64,52 * 64,64,64,3,1);
        npc1.setMovementAI(new PatrolAI(thirdDormRoom));
        world.addNPC(npc1);

        sanans = new Sanans(8 * 64, 47 * 64, 64, 64);
        sanansAI = ScriptedMovementAI.createDormitorySanansAI();
        sanans.setSanansScriptedMovement(sanansAI);
        world.addNPC(sanans);

        world.audio.loadSound("Sananes1" , "/sounds/בוקר טוב תביא את הטלפון.wav");
        world.audio.loadSound("Sananes3" , "/sounds/אני לא רוצה לקחת לך את הטלפון.wav");
        world.audio.loadSound("Sananes2" , "/sounds/ראית מה השעה עכשיו תרד למטה .wav");
        world.audio.setVolume("Sananes1", 0.8F);
        world.audio.setVolume("Sananes2", 0.8F);
        world.audio.setVolume("Sananes3", 0.8F);
        world.audio.play("Sananes1");
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
            moveSanansToOtherRoom();
            world.audio.stop("Sananes1");
            world.audio.play("Sananes2");
            return;
        }

        // סננס נכנס לחדר
        if (sanansInDorm) {
            if (playerInDorm) {
                failPlayer();
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
            world.audio.play("Sananes3");
        }
        // השחקן נכנס לחדר בזמן שסננס שם
        if (dormRoom.contains(px, py)) {
            failPlayer();
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
            failPlayer();
        }
        if (sanansAI.isFinished()) {
            System.out.println("שלב אחת הסתיים");
            finishEvent();
        }
    }

    private void moveSanansToOtherRoom() {
        sanansAI = ScriptedMovementAI.createDormitorySanansAI2();
        sanans.setSanansScriptedMovement(sanansAI);
    }

    private void failPlayer() {
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
        if (npc1 != null) {
            world.removeNPC(npc1);
        }
        world.audio.stopAll();
    }
}
