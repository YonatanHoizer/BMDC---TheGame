package main;

import javax.swing.JFrame;
import util.DatabaseManager; // ייבוא של מחלקת הדאטה בייס החדשה

public class Main {

    public static void main(String[] args) {

        // 1. אתחול מסד הנתונים ב-Docker (יוצר את הטבלה אם היא לא קיימת)
        DatabaseManager.initDatabase();

        JFrame window = new JFrame("BMDC - the game");

        Game game = new Game();

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.add(game);
        window.pack();
        window.setLocationRelativeTo(null);
        window.setVisible(true);

        // 2. תחילת לולאת המשחק
        game.start();
    }
}