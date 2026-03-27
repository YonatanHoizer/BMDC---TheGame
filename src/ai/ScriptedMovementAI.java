package ai;

import entities.NPC;
import world.GameWorld;

import static engine.Time.deltaTime;

import java.util.ArrayList;
import java.util.List;

public class ScriptedMovementAI implements MovementAI {

    private List<MovementStep> steps;
    private int currentStep = 0;
    private float timer = 0f;
    private boolean isLooping;

    public ScriptedMovementAI(List<MovementStep> steps) {
        this.steps = steps;
    }
    public ScriptedMovementAI(List<MovementStep> steps, boolean isLooping) {
        this.steps = steps;
        this.isLooping = isLooping;
    }

    @Override
    public void update(NPC npc, GameWorld world) {
        if (currentStep >= steps.size()) {
            if (isLooping) {
                reset(); // קסם! במקום לעצור, חוזרים ל-0 וממשיכים
            } else {
                npc.stop();
                return;
            }
        }

        MovementStep step = steps.get(currentStep);

        switch (step.type) {
            case MOVE_TO:
                float dx = step.targetX - npc.getX();
                float dy = step.targetY - npc.getY();

                if (Math.abs(dx) < 2 && Math.abs(dy) < 2) {
                    npc.stop();
                    currentStep++;
                } else {
                    npc.moveTowards(dx, dy);
                }
                break;

            case WAIT:
                npc.stop();
                timer += world.getDeltaTime();
                if (timer >= step.duration) {
                    timer = 0f;
                    currentStep++;
                }
                break;
        }
        if (currentStep >= steps.size() && !isLooping) {
            npc.stop();
        }
    }

    public void reset() {
        currentStep = 0;
        timer = 0f;
    }

    public boolean isFinished() {
        return currentStep >= steps.size();
    }

    public static ScriptedMovementAI createDormitorySanansAI() {
        List<MovementStep> steps = new ArrayList<>();

        steps.add(new MovementStep(12 * 64, 47 * 64));
        steps.add(new MovementStep(2f));
        steps.add(new MovementStep(12 * 64, 42 * 64));
        steps.add(new MovementStep(2f));
        steps.add(new MovementStep(12 * 64, 47 * 64));
        steps.add(new MovementStep(2f));
        steps.add(new MovementStep(20 * 64, 47 * 64));
        steps.add(new MovementStep(2f));
        steps.add(new MovementStep(20 * 64, 53 * 64));
        steps.add(new MovementStep(2f));
        steps.add(new MovementStep(20 * 64,47 * 64));

        return new ScriptedMovementAI(steps);
    }

    public static ScriptedMovementAI createDormitorySanansAI3() {
        List<MovementStep> steps = new ArrayList<>();

        steps.add(new MovementStep(12 * 64, 47 * 64));
        steps.add(new MovementStep(21 * 64, 47 * 64));
        steps.add(new MovementStep(3.0f));
        steps.add(new MovementStep(21 * 64, 42 * 64));
        steps.add(new MovementStep(5.0f));
        return new ScriptedMovementAI(steps);
    }

    public static ScriptedMovementAI createDormitorySanansAI2() {
        List<MovementStep> steps = new ArrayList<>();

        steps.add(new MovementStep(21 * 64, 47 * 64));
        steps.add(new MovementStep(3.0f));
        steps.add(new MovementStep(21 * 64, 42 * 64));
        steps.add(new MovementStep(5.0f));
        return new ScriptedMovementAI(steps);
    }

    public static ScriptedMovementAI createDiningHallAkivaAI() {
        List<MovementStep> steps = new ArrayList<>();

        steps.add(new MovementStep(3 * 64, 5 * 64));
        steps.add(new MovementStep(2.0f));
        steps.add(new MovementStep(3 * 64, 20 * 64));
        steps.add(new MovementStep(2.0f));

        // מחזירים את התסריט עם true! זה ירוץ לנצח.
        return new ScriptedMovementAI(steps, true);
    }

    public static ScriptedMovementAI createWalkToClassAI () {
        List<MovementStep> steps = new ArrayList<>();

        steps.add(new MovementStep(27 * 64, 25 * 64));
        steps.add(new MovementStep(27 * 64, 22 * 64));
        steps.add(new MovementStep(41 * 64, 22 * 64));
        steps.add(new MovementStep(41 * 64, 29 * 64));
        steps.add(new MovementStep(38 * 64, 29 * 64));
        steps.add(new MovementStep(38 * 64, 32 * 64));
        steps.add(new MovementStep(37 * 64, 32 * 64));
        steps.add(new MovementStep(37 * 64, 31 * 64));
        return new ScriptedMovementAI(steps);
    }

    public static ScriptedMovementAI KroyzerGetOutOfClassAI () {
        List<MovementStep> steps = new ArrayList<>();
        steps.add(new MovementStep(41 * 64, 30 * 64));

        return new ScriptedMovementAI(steps);
    }
}
