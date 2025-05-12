package com.noteworthy.view;

import javax.swing.*;

import com.formdev.flatlaf.FlatLightLaf;




public class WindowView {

    private FolderView folders;
    private EditorView editor;
    private RenderView render;

    public WindowView(){
        FlatLightLaf.setup();
        JFrame window = new JFrame("NoteWorthy");
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

    private JMenuBar createMenuBar(){
        JMenuBar menuBar = new JMenuBar();

        
        JMenu fileMenu = new JMenu("File");
        JMenu saveMenu = new JMenu("Save");
        JMenu viewMenu = new JMenu("View");

        menuBar.add(fileMenu);
        menuBar.add(saveMenu);
        menuBar.add(viewMenu);


        return menuBar;
    }
}

