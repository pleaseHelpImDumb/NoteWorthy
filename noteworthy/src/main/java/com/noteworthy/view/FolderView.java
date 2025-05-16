package com.noteworthy.view;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

public class FolderView extends JPanel {
    private JTree tree;
    private DefaultTreeModel treeModel;
    private File rootFolder;
    private WindowView winView;
    
    public FolderView(String rootPath, WindowView windowView) {
        setLayout(new BorderLayout());
        this.winView = windowView;
        rootFolder = new File(rootPath);
        if (!rootFolder.exists()) {
            rootFolder.mkdirs();
        }
        
        // Root node
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode(rootFolder);
        treeModel = new DefaultTreeModel(rootNode);
        
        // Tree stuff
        tree = new JTree(treeModel);
        tree.setRootVisible(true);
        tree.setCellRenderer(new NotesTreeCellRenderer());
        setPreferredSize(new Dimension(200, 0));
        populateTree(rootNode, rootFolder);
        tree.expandRow(0);
        JScrollPane scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
        
        
        //MOUSE CLICK ON TREE LISTENER
        //Opens files in the tree w/ a double-click
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // Check for double-click
                if (e.getClickCount() == 2) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                        tree.getLastSelectedPathComponent();
                    
                    if (node == null) return;
                    
                    Object nodeInfo = node.getUserObject();
                    if (nodeInfo instanceof File) {
                        File selectedFile = (File) nodeInfo;
                        if (selectedFile.isFile()) {
                            winView.folderTreeOpenFile(selectedFile);
                            winView.updateCurrentFile(selectedFile);
                            System.out.println("Opening file: " + selectedFile.getAbsolutePath());
                        } else {
                            System.out.println("Selected directory: " + selectedFile.getAbsolutePath());
                            TreePath path = tree.getSelectionPath();
                            if (tree.isExpanded(path)) {
                                tree.collapsePath(path);
                            } else {
                                tree.expandPath(path);
                            }
                        }
                    }
                }
            }
        });
        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)
                    tree.getLastSelectedPathComponent();
                if (node == null) return; 
                Object nodeInfo = node.getUserObject();
                if (nodeInfo instanceof File) {
                    File selectedFile = (File) nodeInfo;
                    System.out.println("Selected: " + selectedFile.getAbsolutePath());
                }
            }
        });
        
        // Add popup menu for context actions
        JPopupMenu popupMenu = createContextMenu();
        tree.setComponentPopupMenu(popupMenu);
    }
    
    /**
     * Populates the tree model with files and directories
     */
    private void populateTree(DefaultMutableTreeNode parent, File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            Arrays.sort(files, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    if (f1.isDirectory() && !f2.isDirectory()) {
                        return -1;
                    } else if (!f1.isDirectory() && f2.isDirectory()) {
                        return 1;
                    } else {
                        return f1.getName().compareToIgnoreCase(f2.getName());
                    }
                }
            });
            
            for (File file : files) {
                if (file.isHidden()) continue;
                
                DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
                parent.add(node);
                
                if (file.isDirectory()) {
                    populateTree(node, file);
                }
            }
        }
    }
    
    /**
     * Creates the context menu for actions on tree nodes
     */
    private JPopupMenu createContextMenu() {
        JPopupMenu menu = new JPopupMenu();
        
        JMenuItem refreshItem = new JMenuItem("Refresh");
        refreshItem.addActionListener(e -> refreshTree());
        
        JMenuItem createFolderItem = new JMenuItem("New Folder");
        createFolderItem.addActionListener(e -> createNewFolder());
        
        JMenuItem createNoteItem = new JMenuItem("New Note");
        createNoteItem.addActionListener(e -> createNewNote());

        JMenuItem deleteItem = new JMenuItem("Delete");
        deleteItem.addActionListener(e -> deleteSelectedItem());
        
        menu.add(refreshItem);
        menu.add(createFolderItem);
        menu.add(createNoteItem);
        menu.addSeparator();
        menu.add(deleteItem);
        return menu;
    }
    
    private void deleteSelectedItem() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) 
            tree.getLastSelectedPathComponent();
        
        if (selectedNode == null) {
            JOptionPane.showMessageDialog(this, 
                "Please select an item to delete.", 
                "No Selection", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Get the file or directory
        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof File)) return;
        
        File fileToDelete = (File) userObject;
        String itemType = fileToDelete.isDirectory() ? "folder" : "file";
        String itemName = fileToDelete.getName();
        
        // Confirm deletion
        int choice = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the " + itemType + " \"" + itemName + "\"?\n" +
            (fileToDelete.isDirectory() ? "All contents will be permanently deleted." : "This action cannot be undone."),
            "Confirm Deletion",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        if (choice != JOptionPane.YES_OPTION) {
            return; // User cancelled
        }
        
        // Perform deletion
        boolean success = false;
        
        if (fileToDelete.isDirectory()) {
            success = deleteDirectory(fileToDelete);
        } else {
            success = fileToDelete.delete();
        }
        
        // Handle result
        if (success) {
            DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if (parentNode != null) {
                treeModel.removeNodeFromParent(selectedNode);
                // Select the parent node
                tree.setSelectionPath(new TreePath(parentNode.getPath()));
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                "Could not delete the " + itemType + ".\nIt may be in use by another application.",
                "Deletion Failed",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    private boolean deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        return directory.delete();
    }

    /**
     * Refreshes the tree view
     */
    public void refreshTree() {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.removeAllChildren();
        populateTree(rootNode, rootFolder);
        treeModel.reload();
        tree.expandRow(0);
    }
    
    /**
     * Creates a new folder in the selected directory
     */
    private void createNewFolder() {
        DefaultMutableTreeNode selectedNode = getSelectedDirectoryNode();
        if (selectedNode == null) return;
        
        String folderName = JOptionPane.showInputDialog(this, "Enter folder name:");
        if (folderName == null || folderName.trim().isEmpty()) return;
        
        File parentDir = (File) selectedNode.getUserObject();
        File newFolder = new File(parentDir, folderName);
        
        if (newFolder.exists()) {
            JOptionPane.showMessageDialog(this, "A folder with this name already exists.");
            return;
        }
        
        if (newFolder.mkdir()) {
            DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newFolder);
            treeModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
            tree.scrollPathToVisible(tree.getSelectionPath().pathByAddingChild(newNode));
        } else {
            JOptionPane.showMessageDialog(this, "Failed to create folder.");
        }
    }
    
    /**
     * Creates a new note in the selected directory
     */
    private void createNewNote() {
        DefaultMutableTreeNode selectedNode = getSelectedDirectoryNode();
        if (selectedNode == null) return;
        
        String noteName = JOptionPane.showInputDialog(this, "Enter note name:");
        if (noteName == null || noteName.trim().isEmpty()) return;
        
        // Add .md extension if not provided
        if (!noteName.toLowerCase().endsWith(".md")) {
            noteName += ".md";
        }
        
        File parentDir = (File) selectedNode.getUserObject();
        File newNote = new File(parentDir, noteName);
        
        if (newNote.exists()) {
            JOptionPane.showMessageDialog(this, "A note with this name already exists.");
            return;
        }
        
        try {
            if (newNote.createNewFile()) {
                DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newNote);
                treeModel.insertNodeInto(newNode, selectedNode, selectedNode.getChildCount());
                tree.scrollPathToVisible(tree.getSelectionPath().pathByAddingChild(newNode));
                // Here you would typically open the new note in the editor
            } else {
                JOptionPane.showMessageDialog(this, "Failed to create note.");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error creating note: " + e.getMessage());
        }
        winView.updateCurrentFile(newNote);
    }
    
    /**
     * Gets the currently selected directory node
     */
    private DefaultMutableTreeNode getSelectedDirectoryNode() {
        DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) 
            tree.getLastSelectedPathComponent();
        
        if (selectedNode == null) {
            // If nothing is selected, use the root
            selectedNode = (DefaultMutableTreeNode) treeModel.getRoot();
        }
        
        File file = (File) selectedNode.getUserObject();
        if (!file.isDirectory()) {
            // If a file is selected, use its parent directory
            selectedNode = (DefaultMutableTreeNode) selectedNode.getParent();
            if (selectedNode == null) {
                selectedNode = (DefaultMutableTreeNode) treeModel.getRoot();
            }
        }
        
        return selectedNode;
    }
    
    /**
     * Custom cell renderer for the tree
     */
    private class NotesTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof File) {
                File file = (File) node.getUserObject();
                setText(file.getName());
                
                if (file.isDirectory()) {
                    if (node.equals(tree.getModel().getRoot())) {
                        setText("Notes"); 
                    }
                    setIcon(expanded ? getOpenIcon() : getClosedIcon());
                } else {
                    setIcon(getLeafIcon());
                }
            }
            
            return this;
        }
    }
    
    /**
     * Returns the root folder for this view
     */
    public File getRootFolder() {
        return rootFolder;
    }
}