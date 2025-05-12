package com.noteworthy.view;

import javax.swing.*;
import javax.swing.text.html.HTMLEditorKit;
import java.awt.*;

public class RenderView extends JEditorPane {
    public RenderView() {
        // Set up as an HTML-capable pane for rendering
        setContentType("text/html");
        setEditable(false);
        
        // Use HTMLEditorKit for better rendering
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        setEditorKit(htmlKit);
    }

    // Method to update the rendered view
    public void updateRendering(String markdownText) {
        // TODO: Implement Markdown/LaTeX to HTML conversion
        // For now, just display raw text as HTML
        setText("<html><body>" + markdownText.replace("\n", "<br>") + "</body></html>");
    }
}