package util;

import java.sql.*;

public class DatabaseManager {

    // הקובץ game_save.db ייווצר אוטומטית בתיקיית השורש של המשחק
    private static final String URL = "jdbc:sqlite:game_save.db";

    /**
     * פונקציה פנימית ליצירת חיבור למסד הנתונים של SQLite
     */
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /**
     * איתחול מסד הנתונים
     */
    public static void initDatabase() {
        // ב-SQLite אנחנו משתמשים ב-REAL (למספרים עם נקודה עשרונית) וב-INTEGER (לבוליאנים)
        String createTableSQL = "CREATE TABLE IF NOT EXISTS tbl_savegame (" +
                "id INTEGER PRIMARY KEY DEFAULT 1," +
                "player_x REAL NOT NULL," +
                "player_y REAL NOT NULL," +
                "current_state TEXT NOT NULL," +
                "has_milk INTEGER NOT NULL" +
                ");";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute(createTableSQL);
        } catch (SQLException e) {
            System.err.println("Error initializing SQLite database!");
            e.printStackTrace();
        }
    }

    /**
     * שמירת מצב המשחק - שימוש ב-REPLACE כדי לדרוס את השמירה הקודמת
     */
    public static void saveGame(SaveData data) {
        // REPLACE INTO מבצע אוטומטית DELETE ואז INSERT אם ה-ID קיים
        String saveSQL = "REPLACE INTO tbl_savegame (id, player_x, player_y, current_state, has_milk) " +
                "VALUES (1, ?, ?, ?, ?);";

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(saveSQL)) {
            pstmt.setFloat(1, data.playerX);
            pstmt.setFloat(2, data.playerY);
            pstmt.setString(3, data.currentState);
            pstmt.setBoolean(4, data.playerHasMilk);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Failed to save game to SQLite.");
            e.printStackTrace();
        }
    }

    /**
     * טעינת מצב המשחק
     */
    public static SaveData loadGame() {
        String loadSQL = "SELECT * FROM tbl_savegame WHERE id = 1;";

        try (Connection conn = getConnection(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(loadSQL)) {

            if (rs.next()) {
                return new SaveData(
                        rs.getFloat("player_x"),
                        rs.getFloat("player_y"),
                        rs.getString("current_state"),
                        rs.getBoolean("has_milk")
                );
            }
        } catch (SQLException e) {
            System.err.println("Failed to load game from SQLite.");
            e.printStackTrace();
        }
        return null;
    }
}