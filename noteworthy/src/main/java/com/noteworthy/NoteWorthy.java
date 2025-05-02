package com.noteworthy;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import java.time.LocalDateTime;

import com.formdev.flatlaf.FlatLightLaf;
import com.noteworthy.controller.DocumentManager;
import com.noteworthy.model.Document;

public class NoteWorthy {

    /* 
    private WindowView mainWindow;
    private DocumentManager documentManager;
    private FileHandler fileHandler;

    public NoteWorthy() {
        // Initialize controllers
        documentManager = new DocumentManager();
        fileHandler = new FileHandler();
        
        // Create initial document
        Document initialDocument = new Document("Untitled");
        documentManager.addDocument(initialDocument);
        
        // Initialize main window
        mainWindow = new WindowView();
        mainWindow.displayWindowView();
    }

    private static void initializeApp() {
        // Set the FlatLaf look and feel
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            System.err.println("Failed to initialize FlatLaf look and feel");
            e.printStackTrace();
        }
        
        // Set some global UI properties
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 10);
    }
    */
    public static void main(String[] args) {

        /* 
        SwingUtilities.invokeLater(() -> {
            initializeApp();
            
            NoteWorthy app = new NoteWorthy();
            
            // Uncomment to load previous session or a specific file
            // app.loadLastSession();
        });
        */


        FlatLightLaf.setup();
        // 1. Create the main window (JFrame)
        JFrame frame = new JFrame("NoteWorthy");

        // 2. Create a simple Swing component (JLabel in this case)
        JLabel label = new JLabel("notes go here : )");

        label.setHorizontalAlignment(SwingConstants.CENTER);
        // 3. Add the component to the content pane of the frame
        frame.getContentPane().add(label);

        // 4. Set basic properties for the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // What happens when the window is closed
        frame.setSize(600, 400); // Set the initial size (width, height)
        frame.setLocationRelativeTo(null); // Center the window on the screen

        // 5. Make the frame visible
        frame.setVisible(true);

    }
}