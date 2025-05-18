package com.noteworthy.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.UndoableEditEvent;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.noteworthy.controller.EditorController;

public class WindowView {

    private File currentFile = null;
    private final JFrame window;
    private final FolderView folders;
    private final EditorView editor;
    private final RenderView render;
    private final JLabel fileNameLabel;
    private EditorController editorController;
    private ArrayList<String> codeLanguages;
    private String defaultLanguage = "Java";
    private boolean isDarkMode = false;

    // Autosave interval in milliseconds (e.g., 5 minutes)
    private static final int AUTOSAVE_INTERVAL = 5 * 60 * 1000;

    public WindowView() {
        FlatLightLaf.setup();
        window = new JFrame("NoteWorthy");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(1200, 800);

        //Add languages
        codeLanguages = new ArrayList<String>();
        codeLanguages.add("Java");
        codeLanguages.add("JavaScript");

        // Editor View
        editor = new EditorView();
        // Setup undo manager for editor
        UndoManager undoManager = new UndoManager();
        editor.getDocument().addUndoableEditListener((UndoableEditEvent e) -> {
            undoManager.addEdit(e.getEdit());
        });

        // Folder View
        folders = new FolderView("notes", this);
        JScrollPane folderView = new JScrollPane(folders);
        folderView.setPreferredSize(new Dimension(200, Short.MAX_VALUE));

        JPanel editorPanel = new JPanel(new BorderLayout());
        fileNameLabel = new JLabel("Untitled");
        fileNameLabel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        fileNameLabel.setFont(new Font(fileNameLabel.getFont().getName(), Font.BOLD, 12));
        fileNameLabel.setForeground(new Color(100, 100, 100));
        JScrollPane editorScroll = new JScrollPane(editor);
        editorScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        editorPanel.add(fileNameLabel, BorderLayout.NORTH);
        editorPanel.add(editorScroll, BorderLayout.CENTER);

        // Render View
        render = new RenderView();
        JPanel renderPanel = new JPanel(new BorderLayout());
        JLabel renderText = new JLabel("Rendered View");
        renderText.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        renderText.setFont(new Font(fileNameLabel.getFont().getName(), Font.BOLD, 12));
        renderText.setForeground(new Color(100, 100, 100));
        JScrollPane renderScroll = new JScrollPane(render);
        renderScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        renderScroll.setBorder(BorderFactory.createEmptyBorder());
        renderPanel.add(renderText, BorderLayout.NORTH);
        renderPanel.add(renderScroll, BorderLayout.CENTER);

        // Split Panes
        JSplitPane editRenderSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPanel, renderPanel);
        editRenderSplit.setResizeWeight(0.5);
        editRenderSplit.setContinuousLayout(true);
        editRenderSplit.setOneTouchExpandable(true);

        JSplitPane folderSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, folderView, editRenderSplit);
        folderSplit.setContinuousLayout(true);

        window.add(folderSplit);

        // Menu Bar
        JMenuBar menuBar = createMenuBar();
        window.setJMenuBar(menuBar);

        // Root pane for hotkeys
        JComponent root = window.getRootPane();


        //Change to ctrl
        // Save Hotkey (CTRL + S)
        bindKey(root, KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK, "saveNote", e -> saveNoteToFile(window));
        // Open File Hotkey (CTRL + O)
        bindKey(root, KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK, "openNote", e -> openFile(window));
        // New File Hotkey (CTRL + N) with prompt
        bindKey(root, KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK, "newFile", e -> { if (confirmSaveIfNeeded()) createNewFile(); });
        // Quit Hotkey (CTRL + Q) with prompt
        bindKey(root, KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK, "quitApp", e -> { if (confirmSaveIfNeeded()) System.exit(0); });
        // Undo (CTRL + Z)
        bindKey(root, KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK, "undo", e -> { if (undoManager.canUndo()) undoManager.undo(); });
        // Redo (CTRL + Shift + Z)
        bindKey(root, KeyEvent.VK_Z, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, "redo", e -> { if (undoManager.canRedo()) undoManager.redo(); });
        // Select All (CTRL + SHIFT + A)
        bindKey(root, KeyEvent.VK_A, InputEvent.CTRL_DOWN_MASK | InputEvent.SHIFT_DOWN_MASK, "selectAll", e -> editor.selectAll());
        // Copy (CTRL + C)
        bindKey(root, KeyEvent.VK_C, InputEvent.CTRL_DOWN_MASK, "copy", e -> editor.copy());
        // Paste (CTRL + V)
        bindKey(root, KeyEvent.VK_V, InputEvent.CTRL_DOWN_MASK, "paste", e -> editor.paste());
        // Schedule autosave
        Timer autosaveTimer = new Timer(AUTOSAVE_INTERVAL, ae -> { if (currentFile != null) saveNoteToFile(window); });
        autosaveTimer.setRepeats(true);
        autosaveTimer.start();

        // Finalize window
        window.setLocationRelativeTo(null);
        window.setVisible(true);
        SwingUtilities.invokeLater(() -> {
            folderSplit.setDividerLocation(0.15);
            editRenderSplit.setDividerLocation(0.5);
        });

        // Controller init
        editorController = (currentFile == null)
            ? new EditorController()
            : new EditorController(fileNameLabel.getText());
    }

    private void bindKey(JComponent comp, int keyCode, int modifiers, String name, ActionListenerImpl action) {
        KeyStroke ks = KeyStroke.getKeyStroke(keyCode, modifiers);
        comp.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, name);
        comp.getActionMap().put(name, new AbstractAction() { public void actionPerformed(ActionEvent e) { action.handle(e); } });
    }

    private interface ActionListenerImpl { void handle(ActionEvent e); }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");

        JMenuItem newFileItem = new JMenuItem("New File");
        newFileItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_DOWN_MASK));
        newFileItem.addActionListener(e -> { if (confirmSaveIfNeeded()) createNewFile(); });
        fileMenu.add(newFileItem);

        JMenuItem openItem = new JMenuItem("Open...");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_DOWN_MASK));
        openItem.addActionListener(e -> openFile(window));
        fileMenu.add(openItem);

        JMenuItem saveItem = new JMenuItem("Save");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK));
        saveItem.addActionListener(e -> saveNoteToFile(window));
        fileMenu.add(saveItem);

        JMenuItem saveAsItem = new JMenuItem("Save As...");
        saveAsItem.addActionListener(e -> saveNoteToNewFile(window));
        fileMenu.add(saveAsItem);

        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        quitItem.addActionListener(e -> { if (confirmSaveIfNeeded()) System.exit(0); });
        fileMenu.add(quitItem);

        menuBar.add(fileMenu);
        
        // === Format Menu ===
        JMenu formatMenu = new JMenu("Format");

        // Bold
        JMenuItem boldItem = new JMenuItem("Bold");
        boldItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_B, InputEvent.CTRL_DOWN_MASK));
        boldItem.addActionListener(e -> wrapSelection("**", "**"));
        formatMenu.add(boldItem);

        // Italic
        JMenuItem italicItem = new JMenuItem("Italic");
        italicItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, InputEvent.CTRL_DOWN_MASK));
        italicItem.addActionListener(e -> wrapSelection("*", "*"));
        formatMenu.add(italicItem);

        // Underline (using HTML <u>…</u>)
        JMenuItem underlineItem = new JMenuItem("Underline");
        underlineItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, InputEvent.CTRL_DOWN_MASK));
        underlineItem.addActionListener(e -> wrapSelection("__", "__"));
        formatMenu.add(underlineItem);

        /* This functionality might not work with the render stuff in jav 
        // Highlight (using ==…==)
        JMenuItem highlightItem = new JMenuItem("Highlight");
        highlightItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_H, InputEvent.CTRL_DOWN_MASK));
        highlightItem.addActionListener(e -> wrapSelection("<mark>", "</mark>"));
        formatMenu.add(highlightItem);
        */

        //Equation
        JMenuItem equationItem = new JMenuItem("LaTeX");
        equationItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_DOWN_MASK));
        equationItem.addActionListener(e -> wrapSelection("$$", "$$"));
        formatMenu.add(equationItem);

        //Code
        JMenuItem codeItem = new JMenuItem("Code");
        codeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK));
        codeItem.addActionListener(e -> wrapSelection("```\n", "\n```"));
        formatMenu.add(codeItem);

        //Image
        JMenuItem insertImage = new JMenuItem("Insert Image");
        insertImage.addActionListener(e -> insertImage());
        formatMenu.add(insertImage);
    
        menuBar.add(formatMenu);

        //View/Render menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem renderItem = new JMenuItem("Render");
        renderItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK));
        renderItem.addActionListener(e -> renderDocument(editor));
        viewMenu.add(renderItem);
        
        JMenuItem themeToggleItem = new JMenuItem("Change Theme");
        themeToggleItem.addActionListener(e -> toggleTheme());
        viewMenu.add(themeToggleItem);
        menuBar.add(viewMenu);
        
        //Code language selection
        JMenu codeLanguage = new JMenu("Code");
        ButtonGroup langs = new ButtonGroup();
        for (String language : codeLanguages){
            JRadioButtonMenuItem langItem = new JRadioButtonMenuItem(language);
            langs.add(langItem);
            codeLanguage.add(langItem);

            if(language.equals(defaultLanguage)) {
                langItem.setSelected(true);
            }

            langItem.addActionListener(e -> {
            AbstractButton button = (AbstractButton) e.getSource();
            defaultLanguage = button.getText();
            System.out.println("Selected language: " + defaultLanguage); // Optional: For debugging 
            });
        }
        menuBar.add(codeLanguage);

        // About menu
        JMenu aboutMenu = new JMenu("Help");
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.addActionListener(e -> showAboutDialog());
        aboutMenu.add(aboutItem);
        menuBar.add(aboutMenu);

        return menuBar;
    }

    private void insertImage(){
        String imgPath = getImagePath(window);  //get img path
        if (!imgPath.isEmpty()){
            String content = editor.getText() + "\n" + imgPath+"\n"; //set document content + img path
            editorController.parseTextToDocument(fileNameLabel.getText(), content); //parse content + img into document
            editor.setText(content);
            render.updateContent(editorController.renderDocument(), defaultLanguage);
        }
        else {
            JOptionPane.showMessageDialog(window, "Could not find file.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    private String getImagePath(JFrame parent) {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png", "gif", "bmp");
        fileChooser.setFileFilter(filter);
        System.out.println("openDialog");
        int result = fileChooser.showOpenDialog(parent); // 'this' refers to your frame/component
        if (result == JFileChooser.APPROVE_OPTION) {
            // Get the selected file
            File selectedFile = fileChooser.getSelectedFile();
            System.out.println("Selected image: " + selectedFile.getAbsolutePath());
            return selectedFile.getAbsolutePath();
        }
        else return "";
    }
    private void toggleTheme() {
        if (isDarkMode) {
            FlatLightLaf.setup();
        } else {
            FlatDarkLaf.setup();
        }
        isDarkMode = !isDarkMode;
        SwingUtilities.updateComponentTreeUI(window);
    }

    private void showAboutDialog() {
        String message = "Note Worthy\n" +
        "Version 1.0\n" +
        "Created for SUNY Old Westbury's Spring 2025 Software Engineering Class\n" +
        "Team members: Adrian, Jackson, Mike";
        JOptionPane.showMessageDialog(window, message, "About", JOptionPane.INFORMATION_MESSAGE);
    }

    private boolean confirmSaveIfNeeded() {
        if (editor.getText().isEmpty()) return true;
        int choice = JOptionPane.showConfirmDialog(
            window,
            "You have unsaved changes. Save before continuing?",
            "Unsaved Changes",
            JOptionPane.YES_NO_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
            return false;
        }
        if (choice == JOptionPane.YES_OPTION) {
            saveNoteToFile(window);
        }
        return true;
    }

    //RENDER FUNCTION
    private void renderDocument(EditorView editedDoc) {
        String content = editedDoc.getText();
        //PARSE INTO BLOCKS & ADD TO DOCUMENT
        editorController.parseTextToDocument(fileNameLabel.getText(), content);
        if (content != null && !content.isEmpty()) {
            render.updateContent(editorController.renderDocument(), defaultLanguage);
        } else {
            render.updateContent("**No content to render!**");
        }
    }

    //Updates current file for FolderTreeView saving/creation
    public void updateCurrentFile(File newFile){
        this.currentFile = newFile;
    }
    
    private void saveNoteToFile(JFrame parentFrame) {
        try {
            if (currentFile == null) {
                JFileChooser fileChooser = createRestrictedFileChooser();
                int option = fileChooser.showSaveDialog(parentFrame);
                if (option == JFileChooser.APPROVE_OPTION) {
                    currentFile = fileChooser.getSelectedFile();
                    updateFileNameLabel();
                } else {
                    return;
                }
            }
            try (FileWriter fw = new FileWriter(currentFile)) {
                fw.write(editor.getText());
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
            editor.setText(readFileContent(currentFile));
            editorController.parseTextToDocument(fileNameLabel.getText(), editor.getText());
        }
    }

    public void folderTreeOpenFile(File openFile){
        currentFile = openFile;
        updateFileNameLabel();
        editor.setText(readFileContent(currentFile));
        editorController.parseTextToDocument(fileNameLabel.getText(), editor.getText());
    }

    private String readFileContent(File file) {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(window, "Error opening file.");
            e.printStackTrace();
        }
        return content.toString();
    }

    private void createNewFile() {
        editor.setText("");
        currentFile = null;
        updateFileNameLabel();
    }

    private void updateFileNameLabel() {
        fileNameLabel.setText(currentFile != null ? currentFile.getName() : "Untitled");
    }

    private JFileChooser createRestrictedFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Text & Markdown Files (*.txt, *.md)", "txt", "md"
        );
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);
        return fileChooser;
    }

    private void wrapSelection(String prefix, String suffix) {
        int start = editor.getSelectionStart();
        int end   = editor.getSelectionEnd();
        if (start == end) return; // nothing selected
        try {
            String selected = editor.getDocument()
                                .getText(start, end - start);
            String replaced = prefix + selected + suffix;
            editor.getDocument().remove(start, end - start);
            editor.getDocument().insertString(start, replaced, null);
        } catch (BadLocationException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new WindowView());
    }
}
