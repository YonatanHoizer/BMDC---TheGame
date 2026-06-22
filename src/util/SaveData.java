package util;

public class SaveData {
    public float playerX;
    public float playerY;
    public String currentState;
    public boolean playerHasMilk;

    // בנאי (Constructor) ליצירת שמירה חדשה מהמשחק
    public SaveData(float playerX, float playerY, String currentState, boolean playerHasMilk) {
        this.playerX = playerX;
        this.playerY = playerY;
        this.currentState = currentState;
        this.playerHasMilk = playerHasMilk;
    }
}