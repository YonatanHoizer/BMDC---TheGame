package engine;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

public class InputManager implements KeyListener {

    private final boolean[] keys = new boolean[256];

    public boolean W_key, S_Key, A_key, D_key, SPACE_key, ENTER_key, E_key;

    public void update() {
        W_key = keys[KeyEvent.VK_W] || keys[KeyEvent.VK_UP];
        S_Key = keys[KeyEvent.VK_S] || keys[KeyEvent.VK_DOWN];
        A_key = keys[KeyEvent.VK_A] || keys[KeyEvent.VK_LEFT];
        D_key = keys[KeyEvent.VK_D] || keys[KeyEvent.VK_RIGHT];
        SPACE_key = keys[KeyEvent.VK_SPACE];
        ENTER_key = keys[KeyEvent.VK_ENTER];
        E_key = keys[KeyEvent.VK_E];
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = true;
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) {}
}