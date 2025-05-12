package com.noteworthy;
import javax.swing.SwingUtilities;
import com.noteworthy.view.*;



public class NoteWorthy {

    private WindowView mainWindow;

    public NoteWorthy() {
        mainWindow = new WindowView();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            NoteWorthy app = new NoteWorthy();
        });
    }
}