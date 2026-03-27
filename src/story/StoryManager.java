package story;

import entities.NPC;
import npcs.Kroyzer;
import npcs.Miller;
import npcs.Sanans;
import world.GameWorld;
import world.Zone;

import java.util.ArrayList;
import java.util.List;
import java.awt.*;


public class StoryManager {

    private StoryState currentState;
    private GameState activeState; // השלב העלילתי הפעיל
    private GameWorld world;
    private Zone dormRoom = new Zone("חדר", 16 * 64, 50 * 64, 7 * 64, 6 * 64);
    private Zone secondedormRoom = new Zone("חדר שני", 19 * 64, 40 * 64, 7 * 64, 6 * 64);
    private Zone thirdDormRoom = new Zone("חדר שלישי", 24 * 64, 50 * 64, 5 * 64, 6 * 64);
    private Zone bathroom = new Zone("שירותים", 13 * 64, 50 * 64, 2 * 64, 2 * 64);
    private Zone corridor = new Zone("מסדרון", 6 * 64, 46 * 64, 32 * 64, 3 * 64);
    private Zone baitMidrash = new Zone("בית מדרש", 10 * 64, 26 * 64, 18 * 64, 13 * 64);
    private Zone diningRoom = new Zone("חדר אוכל", 14 * 64, 64, 23 * 64, 16 * 64);
    private Zone path = new Zone("שביל", 64, 19 * 64, 40 * 64, 64);
    private Zone KroyzerClass = new Zone("קרוייזר כיתה", 29 * 64, 25 * 64, 10 * 64, 7 * 64);
    private Zone playerSeat = new Zone("מקום השחקן בכיתה", 32 * 64, 30 * 64, 8, 64);
    private int currentFailReason = 0;
    private boolean playerHasMilk = false;
    private List <NPC> studentsForClass = new ArrayList<>();
    private Miller miller;
    private Sanans sanans;
    private Kroyzer kroyzer;


    // ======== CONSTRUCTOR ========
    public StoryManager(GameWorld world) {
        this.world = world;
    }

    // ======== UPDATE ========
    public void update(double deltaTime) {
        if (activeState != null) {
            activeState.update(world);

            // ברגע שהשלב מדווח שהוא סיים
            if (activeState.isCompleted()) {

                // בודקים אם הסיום הוא בעצם פסילה
                if (activeState.isFailed()) {
                    // לוקחים את מספר הפסילה שהגדרת (למשל 1)
                    currentFailReason = activeState.getFailReason();

                    activeState.onExit(world);
                    currentState = StoryState.GAME_OVER;
                    activeState = null; // מנקים את השלב
                }
                else {
                    // סיום מוצלח - עוברים לשלב הבא
                    activeState.onExit(world);
                    nextState();
                    if (currentState == StoryState.VICTORY) {
                        activeState = null; // מנקים כדי ש-GameScreen יטפל בזה
                    }
                }
            }
        }
    }

    public int getFailReason() {
        return currentFailReason;
    }

    // ======== STORY CONTROL ========

    /**
     * התחלת עלילה בהתאם ל-StoryState
     */
    public GameState startStory(StoryState state) {
        currentState = state;
        activeState = createStateByStoryState(state);
        if (activeState != null) activeState.onEnter(world);
        return activeState;
    }

    /**
     * מעבר לשלב הבא בעלילה
     */
    public GameState nextState() {
        if (currentState == null) return null;

        StoryState next = null;
        switch (currentState) {
            case DORMITORY:      next = StoryState.SHACHARIT;      break;
            case SHACHARIT:      next = StoryState.DINING_HALL;    break;
            case DINING_HALL:    next = StoryState.SEDER;          break;
            case SEDER:          next = StoryState.LESSON;         break;
            case LESSON:         next = StoryState.LUNCH;          break;
            case LUNCH:          next = StoryState.MINCHA;         break;
            case MINCHA:         next = StoryState.VICTORY;        break;
            default:             next = StoryState.GAME_OVER;      break;
        }

        if (next != null) {
            currentState = next;
            activeState = createStateByStoryState(next);
            if (activeState != null) activeState.onEnter(world);
            return activeState;
        }
        return null;
    }
    /**
     * מעבר ידני לשלב מסוים
     */
    public void setActiveState(GameState state) {
        this.activeState = state;
        if (activeState != null) activeState.onEnter(world);
    }

    public StoryState getState() {
        return currentState;
    }

    public GameState getActiveState() {
        return activeState;
    }

    // ======== FACTORY METHOD ========
    private GameState createStateByStoryState(StoryState state) {
        switch (state) {
            case DORMITORY:
                return new DormitoryEvent(dormRoom, bathroom, corridor, secondedormRoom, thirdDormRoom);
            case SHACHARIT:
                return new ShacharitEvent(baitMidrash);
            case DINING_HALL:
                return new DiningHallEvent(diningRoom,path);
            case SEDER:
                return new SederEvent(baitMidrash,dormRoom,playerHasMilk);
            case LESSON:
                return new LessonEvent(KroyzerClass, playerSeat, studentsForClass,miller);
            case LUNCH:
                return new LunchEvent(diningRoom,baitMidrash,kroyzer);
            case MINCHA:
                return new MinchaEvent(baitMidrash, miller, kroyzer, sanans,studentsForClass);
            case VICTORY:
                return null;
            case GAME_OVER:
                return null;
            default:
                return null;
        }
    }

    public void render(Graphics2D g) {
        if (activeState != null) {
            activeState.render(g);
        }
    }

    public void setPlayerHasMilk(boolean hasMilk) {
        this.playerHasMilk = hasMilk;
    }

    public boolean isPlayerHasMilk() {
        return playerHasMilk;
    }

    public void setStudentsForClass(List<NPC> students) {
        this.studentsForClass = students;
    }

    public List<NPC> getStudentsForClass() {
        return studentsForClass;
    }

    public void setMiller(Miller miller) { this.miller = miller; }
    public Miller getMiller() { return miller; }
    public void setSanans(Sanans sanans) { this.sanans = sanans; }
    public Sanans getSanans() { return sanans; }
    public void setKroyzer(Kroyzer kroyzer) { this.kroyzer = kroyzer; }
    public Kroyzer getKroyzer() { return kroyzer; }
}
