package a4;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JFrame;

// Simple input manager that can be called every frame

public class InputManager implements KeyListener{
    // create space for all possible keys
    private final boolean[] keys = new boolean[256];

    public InputManager(JFrame frame) {
        frame.addKeyListener(this);
        frame.setFocusable(true);
    }

    // check if a key is pressed
    public boolean isKeyPressed(int keyCode) {
        return keys[keyCode];
    }

    // set corresponding array value to true when key is pressed
    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >=0 && keyCode < keys.length) {
            keys[keyCode] = true;
        }
    }

    // set corresponging array value to false when key is released
    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();
        if (keyCode >=0 && keyCode < keys.length) {
            keys[keyCode] = false;
        }
    }
    
    @Override
    public void keyTyped(KeyEvent e) {}
}
