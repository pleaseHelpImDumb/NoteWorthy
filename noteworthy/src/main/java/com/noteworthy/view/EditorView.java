package com.noteworthy.view;

import java.awt.Font;

import javax.swing.JTextArea;

public class EditorView extends JTextArea {
    public EditorView() {
        // Configure the text area
        setLineWrap(true);
        setWrapStyleWord(true);
        
        // Optional: Set default font or other properties
        setFont(new Font("Arial", Font.PLAIN, 14));
    }
}