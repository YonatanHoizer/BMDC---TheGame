package engine;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    private Map<String, Clip> sounds = new HashMap<>();

    // 1. טעינת הסאונד לזיכרון
    public void loadSound(String name, String path) {
        if (sounds.containsKey(name)) return; // מונע טעינה כפולה

        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                System.err.println("Error: Could not find sound file at " + path);
                return;
            }
            InputStream bufferedIn = new BufferedInputStream(is);
            AudioInputStream audioIn = AudioSystem.getAudioInputStream(bufferedIn);

            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            sounds.put(name, clip);

        } catch (Exception e) {
            System.err.println("Error loading sound: " + name);
            e.printStackTrace();
        }
    }

    // 2. ניגון סאונד פעם אחת (מעולה לאפקטים: לחיצת כפתור, קבלת הודעה)
    public void play(String name) {
        Clip clip = sounds.get(name);
        if (clip != null) {
            clip.setFramePosition(0); // מחזיר להתחלה
            clip.start();
        }
    }

    // 3. ניגון סאונד בלופ (מעולה למוזיקת רקע או תפילה ארוכה)
    public void loop(String name) {
        Clip clip = sounds.get(name);
        if (clip != null) {
            clip.setFramePosition(0);
            clip.loop(Clip.LOOP_CONTINUOUSLY); // יתנגן לנצח עד שנעצור אותו
        }
    }

    // 4. עצירת סאונד ספציפי
    public void stop(String name) {
        Clip clip = sounds.get(name);
        if (clip != null && clip.isRunning()) {
            clip.stop();
        }
    }

    // 5. עצירת כל הסאונדים (טוב למעבר בין שלבים או מסך Game Over)
    public void stopAll() {
        for (Clip clip : sounds.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
    }

    // 6. שינוי עוצמת השמע (ערכים בין 0.0 ל-1.0)
    public void setVolume(String name, float volume) {
        Clip clip = sounds.get(name);
        if (clip != null) {
            try {
                FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                // המרה מליניארי (0.0-1.0) לדציבלים (Decibels)
                float range = gainControl.getMaximum() - gainControl.getMinimum();
                float gain = (range * volume) + gainControl.getMinimum();
                gainControl.setValue(gain);
            } catch (IllegalArgumentException e) {
                System.out.println("Volume control not supported for this sound.");
            }
        }
    }
}