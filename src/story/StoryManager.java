package story;

import ai.PatrolAI;
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
    private Zone diningRoom = new Zone("חדר אוכל", 14 * 64, 64, 23 * 64, 14 * 64);
    private Zone path = new Zone("שביל", 0, 15 * 64, 35 * 64, 64 * 11);
    private Zone KroyzerClass = new Zone("קרוייזר כיתה", 29 * 64, 25 * 64, 10 * 64, 7 * 64);
    private Zone playerSeat = new Zone("מקום השחקן בכיתה", 32 * 64 + 16, 30 * 64, 32, 64);
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
        // קריאה לפונקציית העזר לבדיקה ושחזור נתונים לפני יצירת השלב
        initializeWorldDataForLoadedGame(state);

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
                return new LunchEvent(diningRoom,baitMidrash,path,kroyzer);
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

    /**
     * פונקציית עזר המאכלסת נתוני רקע חסרים במקרה של טעינת משחק ישירה לשלבים מתקדמים
     */
    private void initializeWorldDataForLoadedGame(StoryState state) {
        // שחזור נתונים עבור שלב LESSON (במידה והגענו מטעינת משחק ישירה ו-SEDER לא רץ)
        if (state == StoryState.LESSON) {
            if (this.miller == null) {
                // המיקום המקורי שנקבע ב-SEDER
                this.miller = new Miller(15 * 64, 30 * 64, 64, 64);
                miller.setMillerPatrolAI(new PatrolAI(baitMidrash));
                world.addNPC(miller);
            }
            if (this.studentsForClass == null || this.studentsForClass.isEmpty()) {
                this.studentsForClass = new ArrayList<>();
                // שחזור 4 תלמידי ה-Patrol הרגילים מתוך ה-SEDER
                this.studentsForClass.add(new NPC(10 * 64, 30 * 64, 64, 64, 3, 4));
                this.studentsForClass.add(new NPC(20 * 64, 33 * 64, 64, 64, 1, 4));
                this.studentsForClass.add(new NPC(16 * 64, 30 * 64, 64, 64, 4, 4));
                this.studentsForClass.add(new NPC(18 * 64, 29 * 64, 64, 64, 5, 4));
                // שחזור 2 תלמידי הדיאלוג מה-SEDER
                this.studentsForClass.add(new NPC(15 * 64, 35 * 64, 64, 64, 2, 4));
                this.studentsForClass.add(new NPC(28 * 64, 29 * 64, 64, 64, 4, 1));

                for (NPC npc : studentsForClass){
                    world.addNPC(npc);
                }
            }

        }

        // שחזור נתונים עבור שלב MINCHA (במידה והגענו מטעינת משחק ישירה ו-LUNCH לא רץ)
        if (state == StoryState.MINCHA) {
            if (this.miller == null) {
                this.miller = new Miller(19 * 64, 26 * 64, 64, 64);
                world.addNPC(miller);
            }
            if (this.kroyzer == null) {
                this.kroyzer = new Kroyzer(16 * 64, 26 * 64, 64, 64);
                world.addNPC(kroyzer);
            }
            if (this.sanans == null) {
                this.sanans = new Sanans(22 * 64, 26 * 64, 64, 64);
                world.addNPC(sanans);
            }
            if (this.studentsForClass == null || this.studentsForClass.isEmpty()) {
                this.studentsForClass = new ArrayList<>();
                // שחזור רשימת ה-finaleNpcs מתוך לוגיקת loadFinaleNPCs של LUNCH
                NPC MoveWorshipper1 = new NPC(17 * 64, 38 * 64, 64, 64, 4, 4);
                studentsForClass.add(MoveWorshipper1);
                NPC MoveWorshipper2 = new NPC(19 * 64, 38 * 64, 64, 64, 5, 4);
                studentsForClass.add(MoveWorshipper2);
                NPC MoveWorshipper3 = new NPC(22 * 64, 38 * 64, 64, 64, 4, 4);
                studentsForClass.add(MoveWorshipper3);

                NPC student1 = new NPC(25 * 64, 29 * 64, 64, 64, 1, 4);
                NPC student2 = new NPC(21 * 64, 35 * 64, 64, 64, 2, 4);
                student1.setAlert(false);
                student2.setAlert(false);
                studentsForClass.add(student1);
                studentsForClass.add(student2);

                NPC worshipper   = new NPC(15 * 64, 32 * 64, 64, 64, 3, 4);
                studentsForClass.add(worshipper);
                NPC worshipper1  = new NPC(14 * 64, 35 * 64, 64, 64, 4, 4);
                studentsForClass.add(worshipper1);
                NPC worshipper2  = new NPC(16 * 64, 29 * 64, 64, 64, 5, 4);
                studentsForClass.add(worshipper2);
                NPC worshipper3  = new NPC(20 * 64, 32 * 64, 64, 64, 1, 4);
                studentsForClass.add(worshipper3);
                NPC worshipper5  = new NPC(20 * 64, 29 * 64, 64, 64, 6, 4);
                studentsForClass.add(worshipper5);
                NPC worshipper6  = new NPC(19 * 64, 38 * 64, 64, 64, 3, 4);
                studentsForClass.add(worshipper6);
                NPC worshipper7  = new NPC(24 * 64, 32 * 64, 64, 64, 1, 4);
                studentsForClass.add(worshipper7);
                NPC worshipper8  = new NPC(25 * 64, 35 * 64, 64, 64, 4, 4);
                studentsForClass.add(worshipper8);
                NPC worshipper10 = new NPC(24 * 64, 38 * 64, 64, 64, 5, 4);
                studentsForClass.add(worshipper10);

                for (int i = 0; i < studentsForClass.size(); i++) {
                    world.addNPC(studentsForClass.get(i));
                    if (i < 4){
                        studentsForClass.get(i).setMovementAI(new PatrolAI(baitMidrash));
                    }
                }
            }
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