package ai;

public class MovementStep {

    public enum StepType {MOVE_TO, WAIT}

    public StepType type;

    // MOVE_TO
    public float targetX;
    public float targetY;

    // WAIT
    public float duration;

    // צעד תנועה
    public MovementStep(float x, float y) {
        this.type = StepType.MOVE_TO;
        this.targetX = x;
        this.targetY = y;
    }

    // צעד המתנה
    public MovementStep(float duration) {
        this.type = StepType.WAIT;
        this.duration = duration;
    }
}
