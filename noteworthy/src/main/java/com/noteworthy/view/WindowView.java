package com.noteworthy.view;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.formdev.flatlaf.FlatLightLaf;




public class WindowView {

    private File currentFile = null;
    private JFrame window;
    private FolderView folders;
    private EditorView editor;
    private RenderView render;

    public WindowView(){
        FlatLightLaf.setup();
        window = new JFrame("NoteWorthy");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1200, 800);

        //Folder
        folders = new FolderView();
        JScrollPane folderView = new JScrollPane(folders);

        //Editor
        editor = new EditorView();
        JScrollPane docEditView = new JScrollPane(editor);
        
        //Render
        render = new RenderView();
        JScrollPane renderView = new JScrollPane(render);

        JSplitPane editRenderSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        editRenderSplit.setLeftComponent(docEditView);
        editRenderSplit.setRightComponent(renderView);
        editRenderSplit.setResizeWeight(0.5);

        JSplitPane folderSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        folderSplit.setLeftComponent(folderView);
        folderSplit.setRightComponent(editRenderSplit);
        
        editRenderSplit.setDividerLocation(600);
        folderSplit.setDividerLocation(200);

        window.add(folderSplit);

        //Menu Bar
        JMenuBar menuBar = createMenuBar();
        window.setJMenuBar(menuBar);

        window.setLocationRelativeTo(null);
        window.setVisible(true);

    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // === File Menu ===
        JMenu fileMenu = new JMenu("File");

        // Open
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile(window));
        fileMenu.add(openItem);

        // Save (uses remembered file)
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveNoteToFile(window));
        fileMenu.add(saveItem);

        // Save As (always prompts)
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveNoteToNewFile(window));
        fileMenu.add(saveAsItem);

        // Add menus to menu bar
        menuBar.add(fileMenu);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        menuBar.add(viewMenu);

        return menuBar;
    }

    private void saveNoteToFile(JFrame parentFrame) {
        try {
            if (currentFile == null) {
                // First time: show Save dialog
                JFileChooser fileChooser = new JFileChooser();
                int option = fileChooser.showSaveDialog(parentFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    currentFile = fileChooser.getSelectedFile();
                } else {
                    return; // User canceled
                }
            }
    
            // Write to the selected file
            try (FileWriter fw = new FileWriter(currentFile)) {
                String noteContent = editor.getText();
                fw.write(noteContent);
                JOptionPane.showMessageDialog(parentFrame, "File saved.");
            }
    
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parentFrame, "Error saving file.");
            ex.printStackTrace();
        }
    }

    private void saveNoteToNewFile(JFrame parentFrame) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showSaveDialog(parentFrame);
        if (option == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            saveNoteToFile(parentFrame);
        }
    }

    private void openFile(JFrame parentFrame) {
        JFileChooser fileChooser = new JFileChooser();
        int option = fileChooser.showOpenDialog(parentFrame);
        if (option == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            try (BufferedReader reader = new BufferedReader(new FileReader(currentFile))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                editor.setText(content.toString());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(parentFrame, "Error opening file.");
                ex.printStackTrace();
            }
        }
    }
}



