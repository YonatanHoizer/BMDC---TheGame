package ai;

/**
 * מחלקת עזר לאלגוריתם A* המייצגת משבצת בודדת ברשת החיפוש
 */
public class Node {

    // מיקום המשבצת על גריד המפה (למשל: עמודה 10, שורה 26)
    public int col;
    public int row;

    // הציונים של המשבצת (הנוסחה: F = G + H)
    public int gCost; // מרחק מההתחלה
    public int hCost; // מרחק מוערך ליעד (Heuristic)
    public int fCost; // הציון הכולל (הכי נמוך = הכי טוב)

    // מצביע ל"אבא" - המשבצת שממנה הגענו אליה, כדי שנוכל לשחזר את המסלול לאחור
    public Node parent;

    // בנאי
    public Node(int col, int row) {
        this.col = col;
        this.row = row;
    }

    /**
     * פונקציית נוחות לעדכון הציון הכולל של המשבצת
     */
    public void calculateFCost() {
        this.fCost = this.gCost + this.hCost;
    }
}