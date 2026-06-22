package util;

import util.SaveData;
import java.sql.*;

public class DatabaseManager {

    // פרטי ההתחברות לשרת ה-MySQL שרץ בתוך ה-Docker
    private static final String URL = "jdbc:mysql://localhost:3307/yeshiva_game?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    /**
     * פונקציה פנימית ליצירת חיבור למסד הנתונים
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * איתחול מסד הנתונים - יוצר את הטבלה אוטומטית אם היא לא קיימת בריצה הראשונה
     */
    public static void initDatabase() {
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tbl_savegame (" +
                "id INT PRIMARY KEY DEFAULT 1," + // תמיד נשמור על שורה אחת (מפתח קבוע 1) כדי לדרוס את השמירה הקודמת
                "player_x FLOAT NOT NULL," +
                "player_y FLOAT NOT NULL," +
                "current_state VARCHAR(50) NOT NULL," +
                "has_milk BOOLEAN NOT NULL" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error initializing database! Make sure Docker is running.");
            e.printStackTrace();
        }
    }

    /**
     * שמירת מצב המשחק (INSERT או UPDATE במידה וכבר קיימת שמירה)
     */
    public static void saveGame(SaveData data) {
        // שאילתת SQL מתוחכמת: מנסה להכניס שמירה, ואם ה-id כבר קיים (שזה תמיד קורה מהשמירה השנייה והלאה), היא פשוט מעדכנת את הערכים!
        String saveSQL = "INSERT INTO tbl_savegame (id, player_x, player_y, current_state, has_milk) " +
                "VALUES (1, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE " +
                "player_x = VALUES(player_x), " +
                "player_y = VALUES(player_y), " +
                "current_state = VALUES(current_state), " +
                "has_milk = VALUES(has_milk);";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(saveSQL)) {

            pstmt.setFloat(1, data.playerX);
            pstmt.setFloat(2, data.playerY);
            pstmt.setString(3, data.currentState);
            pstmt.setBoolean(4, data.playerHasMilk);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Failed to save game to database.");
            e.printStackTrace();
        }
    }

    /**
     * טעינת מצב המשחק מהדאטה בייס
     */
    public static SaveData loadGame() {
        String loadSQL = "SELECT * FROM tbl_savegame WHERE id = 1;";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(loadSQL)) {

            // אם נמצאה שורת שמירה בטבלה
            if (rs.next()) {
                float x = rs.getFloat("player_x");
                float y = rs.getFloat("player_y");
                String state = rs.getString("current_state");
                boolean hasMilk = rs.getBoolean("has_milk");
                return new SaveData(x, y, state, hasMilk);
            }

        } catch (SQLException e) {
            System.err.println("Failed to load game from database.");
            e.printStackTrace();
        }

        return null; // מחזיר null במידה ואין עדיין אף שמירה בדאטה בייס
    }
}