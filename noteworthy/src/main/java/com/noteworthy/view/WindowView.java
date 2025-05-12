package com.noteworthy.view;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import com.formdev.flatlaf.FlatLightLaf;




public class WindowView {

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
    
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
    
        JMenu saveMenu = new JMenu("Save");
        saveMenu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                saveNoteToFile(window);
            }
        });
        menuBar.add(saveMenu);
    
        JMenu viewMenu = new JMenu("View");

        menuBar.add(viewMenu);
    
        return menuBar;
    }

    private void saveNoteToFile(JFrame parentFrame) {
    JFileChooser fileChooser = new JFileChooser();
    int option = fileChooser.showSaveDialog(parentFrame);
    if (option == JFileChooser.APPROVE_OPTION) {
        File file = fileChooser.getSelectedFile();
        try (FileWriter fw = new FileWriter(file)) {
            String noteContent = editor.getText(); // assumes your EditorView has a getText() method
            fw.write(noteContent);
            JOptionPane.showMessageDialog(parentFrame, "Note saved!");
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(parentFrame, "Error saving note.");
            ex.printStackTrace();
        }
    }
}
}



