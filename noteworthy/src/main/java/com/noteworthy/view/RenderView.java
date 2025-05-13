package com.noteworthy.view;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.text.html.*;
import java.awt.*;

public class RenderView extends JEditorPane {
    public RenderView() {
        setContentType("text/html");
        setEditable(false);
        
        // Custom HTMLEditorKit with our factory
        HTMLEditorKit kit = new HTMLEditorKit(); 
        
        setEditorKit(kit);
        
    }

    public void updateContent(String renderedContent) {
        setText(renderedContent);
    }
}