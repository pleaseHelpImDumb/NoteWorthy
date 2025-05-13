package com.noteworthy.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.formdev.flatlaf.FlatLightLaf;
import com.noteworthy.controller.EditorController;




public class WindowView {

    private File currentFile = null;
    private JFrame window;
    private FolderView folders;
    private EditorView editor;
    private RenderView render;
    private JLabel fileNameLabel;

    //Controller
    private EditorController editorController;

    public WindowView(){
        FlatLightLaf.setup();
        window = new JFrame("NoteWorthy");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1200, 800);

        //Folder View
        folders = new FolderView();
        JScrollPane folderView = new JScrollPane(folders);
        folderView.setPreferredSize(new Dimension(200, Short.MAX_VALUE));

        //Editor View
        editor = new EditorView();
        JPanel editorPanel = new JPanel(new BorderLayout());
        fileNameLabel = new JLabel("Untitled");
        fileNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        fileNameLabel.setFont(new Font(fileNameLabel.getFont().getName(), Font.BOLD, 12));
        fileNameLabel.setForeground(new Color(100, 100, 100));
        
        //Editor View Panel
        JScrollPane editorScroll = new JScrollPane(editor);
        editorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editorPanel.add(fileNameLabel, BorderLayout.NORTH);
        editorPanel.add(editorScroll, BorderLayout.CENTER);
        
        //Render View
        render = new RenderView();
        JPanel renderPanel = new JPanel(new BorderLayout());
        JLabel renderText = new JLabel("Rendered View");
        renderText.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        renderText.setFont(new Font(fileNameLabel.getFont().getName(), Font.BOLD, 12));
        renderText.setForeground(new Color(100, 100, 100));  

        //Render View Panel
        JScrollPane renderScroll = new JScrollPane(render);
        renderScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        renderScroll.setBorder(BorderFactory.createEmptyBorder()); 
        renderPanel.add(renderText, BorderLayout.NORTH);
        renderPanel.add(renderScroll, BorderLayout.CENTER);


        //Edit/Render Split
        JSplitPane editRenderSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editRenderSplit.setLeftComponent(editorPanel);
        editRenderSplit.setRightComponent(renderPanel);
        editRenderSplit.setResizeWeight(0.5);
        editRenderSplit.setContinuousLayout(true);
        editRenderSplit.setOneTouchExpandable(true);

        //Folder / (Edit/Render) Split
        JSplitPane folderSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        folderSplit.setLeftComponent(folderView);
        folderSplit.setRightComponent(editRenderSplit);
        folderSplit.setContinuousLayout(true);

        window.add(folderSplit);
        //Menu Bar
        JMenuBar menuBar = createMenuBar();
        window.setJMenuBar(menuBar);

        window.setLocationRelativeTo(null);
        window.setVisible(true);

        
        SwingUtilities.invokeLater(() -> {
            folderSplit.setDividerLocation(0.15); // 15% for folders
            editRenderSplit.setDividerLocation(0.5); // 50-50 split
        });

        if(fileNameLabel.getText().isEmpty()){
            editorController = new EditorController();
        }
        else {
            editorController = new EditorController(fileNameLabel.getText());
        }

    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
    
        // === File Menu ===
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem newFileItem = new JMenuItem("New File");
        newFileItem.addActionListener(e -> createNewFile());
        fileMenu.add(newFileItem);
    
        JMenuItem openItem = new JMenuItem("Open...");
        openItem.addActionListener(e -> openFile(window));
        fileMenu.add(openItem);
    
        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.addActionListener(e -> saveNoteToFile(window));
        fileMenu.add(saveItem);
    
        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveNoteToNewFile(window));
        fileMenu.add(saveAsItem);
    
        menuBar.add(fileMenu);
    
        // === View Menu ===
        JMenu viewMenu = new JMenu("View");
        
        // Add render as a menu item under View
        JMenuItem renderItem = new JMenuItem("Render");
        renderItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        renderItem.addActionListener(e -> renderDocument(editor));
        viewMenu.add(renderItem);
        
        menuBar.add(viewMenu);
    
        return menuBar;
    }


    //Right now this processes the entire editor view and makes a new document inside editorController each time
    //For optimization later, maybe cache unchanged part and only process changes or something??
    private void renderDocument(EditorView editedDoc) {
        String content = editedDoc.getText();
        String escapedHTMLContent = escapeHTML(content).replace("\n","<br>");
        editorController.parseTextToDocument(fileNameLabel.getText(), escapedHTMLContent);
        if (content != null && !content.isEmpty()) {
            String renderedContent = editorController.renderDocument();
            render.updateContent(renderedContent);
        }
        else{
            render.updateContent("<html><body><em>No content to render!</em></body></html>");
        }
    }

    private String escapeHTML(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;");
    }

    private void saveNoteToFile(JFrame parentFrame) {
        try {
            if (currentFile == null) {
                // First time: show Save dialog
                JFileChooser fileChooser = createRestrictedFileChooser();
                int option = fileChooser.showSaveDialog(parentFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    currentFile = fileChooser.getSelectedFile();
                    updateFileNameLabel();
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
        JFileChooser fileChooser = createRestrictedFileChooser();
        int option = fileChooser.showSaveDialog(parentFrame);
        if (option == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            updateFileNameLabel();
            saveNoteToFile(parentFrame);
        }
    }

    private void openFile(JFrame parentFrame) {
        JFileChooser fileChooser = createRestrictedFileChooser();
        int option = fileChooser.showOpenDialog(parentFrame);
        if (option == JFileChooser.APPROVE_OPTION) {
            currentFile = fileChooser.getSelectedFile();
            updateFileNameLabel();
            editorController = new EditorController(fileNameLabel.getText());
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
        editorController.parseTextToDocument(fileNameLabel.getText(), editor.getText());
    }

    private void createNewFile() {
        editor.setText("");     // Clear editor
        currentFile = null;     // Reset the save location
        updateFileNameLabel();
    }

    //Update the file name
    private void updateFileNameLabel() {
        if (currentFile != null) {
            fileNameLabel.setText(currentFile.getName());
        } else {
            fileNameLabel.setText("Untitled");
        }
    }

    private JFileChooser createRestrictedFileChooser() {
    JFileChooser fileChooser = new JFileChooser();
    FileNameExtensionFilter filter = new FileNameExtensionFilter(
        "Text & Markdown Files (*.txt, *.md)", "txt", "md");
    fileChooser.setFileFilter(filter);
    fileChooser.setAcceptAllFileFilterUsed(false);
    return fileChooser;
}

}
