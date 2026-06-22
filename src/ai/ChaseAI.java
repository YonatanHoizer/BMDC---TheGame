package ai;

import entities.NPC;
import entities.Player;
import world.GameWorld;
import util.Rect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static main.Game.deltaTime;

public class ChaseAI implements MovementAI {

    private final Player target;
    private final int TILE_SIZE = 64;

    private List<Node> currentPath = new ArrayList<>();
    private int currentPathIndex = 0;

    private double pathCalcTimer = 0;
    // אינטרוול מהיר יותר (0.15 שניות) כדי להגיב בזמן אמת בלי רעידות
    private final double PATH_CALC_INTERVAL = 0.25;

    public ChaseAI(Player target) {
        this.target = target;
    }

    @Override
    public void update(NPC npc, GameWorld world) {
        if (target == null) {
            npc.stop();
            return;
        }

        // 1. ניהול טיימר חישוב המסלול
        pathCalcTimer -= deltaTime;
        if (pathCalcTimer <= 0) {
            calculatePath(npc, target, world);
            pathCalcTimer = PATH_CALC_INTERVAL;
        }

        // 2. תנועה רציפה וחלקה לאורך המסלול (ללא שום שיגור אגרסיבי!)
        if (currentPath != null && currentPathIndex < currentPath.size()) {

            Node targetNode = currentPath.get(currentPathIndex);

            // שאיפה למרכז המשבצת המותאם לגודל ה-NPC (מונע משיכה שמאלה בטור ישר)
            float targetPixelX = (targetNode.col * TILE_SIZE) + (TILE_SIZE / 2f) - (npc.getWidth() / 2f);
            float targetPixelY = (targetNode.row * TILE_SIZE) + (TILE_SIZE / 2f) - (npc.getHeight() / 2f);

            float dx = targetPixelX - npc.getX();
            float dy = targetPixelY - npc.getY();

            float dist = (float) Math.sqrt(dx * dx + dy * dy);
            float moveStep = npc.getSpeed() * (float) deltaTime;

            // אם הגענו קרוב מאוד למשבצת בפריים הזה, נעבור למשבצת הבאה במסלול
            // הורדנו לחלוטין את ה-setX ואצי פקודות השיגור! ה-NPC פשוט זז עם moveTowards
            if (dist <= moveStep || dist < 8.0f) {
                currentPathIndex++;
            } else {
                npc.moveTowards(dx, dy);
            }
        } else {
            // גיבוי (Fail-safe): אם אין מסלול, נעים ישירות לשחקן כדי למנוע קיפאון
            float dx = target.getX() - npc.getX();
            float dy = target.getY() - npc.getY();
            npc.moveTowards(dx, dy);
        }
    }

    private void calculatePath(NPC npc, Player player, GameWorld world) {
        // קביעת נקודת ההתחלה של ה-A* בצורה חכמה:
        // אם יש לנו מסלול קיים וה-NPC כבר צועד למשבצת כלשהי, נתחיל את החישוב החדש
        // מאותה משבצת שהוא הולך אליה (כדי שימשיך קדימה ולא יחשב לאחור!)
        int startCol, startRow;
        if (!currentPath.isEmpty() && currentPathIndex < currentPath.size()) {
            Node nextUpcomingNode = currentPath.get(currentPathIndex);
            startCol = nextUpcomingNode.col;
            startRow = nextUpcomingNode.row;
        } else {
            // אם אין מסלול בכלל, אין ברירה אלא לקחת את המיקום הנוכחי שלו
            startCol = (int) ((npc.getX() + npc.getWidth() / 2f) / TILE_SIZE);
            startRow = (int) ((npc.getY() + npc.getHeight() / 2f) / TILE_SIZE);
        }

        int targetCol = (int) ((player.getX() + player.getWidth() / 2f) / TILE_SIZE);
        int targetRow = (int) ((player.getY() + player.getHeight() / 2f) / TILE_SIZE);

        // אם כבר הגענו למשבצת של השחקן, נעצור את ה-A*
        if (startCol == targetCol && startRow == targetRow) {
            currentPath.clear();
            currentPathIndex = 0;
            return;
        }

        List<Node> openList = new ArrayList<>();
        List<Node> closedList = new ArrayList<>();

        Node startNode = new Node(startCol, startRow);
        startNode.gCost = 0;

        // תיקון באג 2: שינוי חישוב ה-H למרחק אווירי אמיתי (פיתגורס) במקום מנהטן!
        // זה יגרום ל-A* להעדיף קווים אלכסוניים יפים ולא "שני קווים ישרים (L)" בשטח פתוח
        startNode.hCost = (int) (Math.sqrt(Math.pow(startCol - targetCol, 2) + Math.pow(startRow - targetRow, 2)) * 10);
        startNode.calculateFCost();
        openList.add(startNode);

        int maxSearches = 300;
        int searches = 0;

        while (!openList.isEmpty() && searches < maxSearches) {
            searches++;

            Node currentNode = openList.get(0);
            for (int i = 1; i < openList.size(); i++) {
                Node n = openList.get(i);
                if (n.fCost < currentNode.fCost || (n.fCost == currentNode.fCost && n.hCost < currentNode.hCost)) {
                    currentNode = n;
                }
            }

            openList.remove(currentNode);
            closedList.add(currentNode);

            if (currentNode.col == targetCol && currentNode.row == targetRow) {
                buildPath(currentNode);
                return;
            }

            int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
            for (int[] dir : directions) {
                int neighborCol = currentNode.col + dir[0];
                int neighborRow = currentNode.row + dir[1];

                Rect testRect = new Rect(neighborCol * TILE_SIZE, neighborRow * TILE_SIZE, npc.getWidth(), npc.getHeight());

                if (!world.canMoveTo(testRect)) {
                    continue;
                }

                if (getNodeFromList(closedList, neighborCol, neighborRow) != null) {
                    continue;
                }

                int tentativeGCost = currentNode.gCost + 10; // הכפלה ב-10 בשביל דיוק מתמטי עם השורש
                Node neighbor = getNodeFromList(openList, neighborCol, neighborRow);

                if (neighbor == null) {
                    neighbor = new Node(neighborCol, neighborRow);
                    neighbor.gCost = tentativeGCost;
                    // חישוב מרחק אווירי מוכפל ב-10 לשכן
                    neighbor.hCost = (int) (Math.sqrt(Math.pow(neighborCol - targetCol, 2) + Math.pow(neighborRow - targetRow, 2)) * 10);
                    neighbor.calculateFCost();
                    neighbor.parent = currentNode;
                    openList.add(neighbor);
                } else if (tentativeGCost < neighbor.gCost) {
                    neighbor.gCost = tentativeGCost;
                    neighbor.calculateFCost();
                    neighbor.parent = currentNode;
                }
            }
        }
    }

    private void buildPath(Node targetNode) {
        currentPath.clear();
        currentPathIndex = 0;

        Node current = targetNode;
        while (current != null) {
            currentPath.add(current);
            current = current.parent;
        }

        Collections.reverse(currentPath);
    }

    private Node getNodeFromList(List<Node> list, int col, int row) {
        for (Node n : list) {
            if (n.col == col && n.row == row) {
                return n;
            }
        }
        return null;
    }
}