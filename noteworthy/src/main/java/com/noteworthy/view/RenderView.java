package com.noteworthy.view;

import javax.swing.*;
import javax.swing.text.html.*;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXIcon;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;
import java.util.HashMap;
import java.util.Map;

public class RenderView extends JEditorPane {
    private File tempDir;
    private Map<String, String> latexCache;
    
    public RenderView() {
        setContentType("text/html");
        setEditable(false);
        this.latexCache = new HashMap<>();
        
        // Create temp directory for LaTeX images
        this.tempDir = createTempDirectory();
        tempDir.deleteOnExit();
        
        // Configure HTMLEditorKit with proper styling
        HTMLEditorKit kit = new HTMLEditorKit();
        StyleSheet styleSheet = kit.getStyleSheet();
        styleSheet.addRule("body { "
            + "font-family: Sans-Serif; "
            + "padding: 10px; "
            + "margin: 0; "
            + "line-height: 1.5; "
            + "}");
        styleSheet.addRule("img.latex { "
            + "vertical-align: middle; "
            + "margin: 5px 0; "
            + "display: block; "
            + "}");
        setEditorKit(kit);
    }

    public void updateContent(String content) {
        String html = convertToHtml(content);
        setText(html);
    }

    private String convertToHtml(String content) {
        if (content == null || content.isEmpty()) {
            return "<html><body><em>No content to render</em></body></html>";
        }

        // Normalize line endings first
        content = content.replace("\r\n", "\n");
        
        StringBuilder html = new StringBuilder("<html><body>");
        
        // Split content while handling LaTeX blocks
        String[] parts = content.split("(?=\\$\\$)|(?<=\\$\\$)");
        boolean isLatex = false;
        
        for (String part : parts) {
            if (part.equals("$$")) {
                isLatex = !isLatex;
                continue;
            }
            
            if (isLatex) {
                // Process LaTeX content
                try {
                    String latexImage = renderLatexToImage(part.trim());
                    html.append("<img class='latex' src='")
                       .append(latexImage)
                       .append("' alt='")
                       .append(escapeHtml(part))
                       .append("'/><br>");  // Explicit <br> after LaTeX
                } catch (Exception e) {
                    html.append("<span style='color:red'>[LaTeX Error]</span><br>");
                }
            } else {
                // Process plain text - preserve all formatting
                html.append(escapeHtml(part).replace("\n", "<br>"));
            }
        }
        
        html.append("</body></html>");
        return html.toString();
    }

    private String renderLatexToImage(String latex) throws Exception {
        // Check cache first
        if (latexCache.containsKey(latex)) {
            return latexCache.get(latex);
        }
        
        // Render new LaTeX image
        TeXFormula formula = new TeXFormula(latex);
        TeXIcon icon = formula.createTeXIcon(TeXConstants.STYLE_DISPLAY, 20);
        icon.setInsets(new Insets(5, 0, 5, 0)); // Add padding
        
        BufferedImage image = new BufferedImage(
            icon.getIconWidth(), 
            icon.getIconHeight(), 
            BufferedImage.TYPE_INT_ARGB);
        
        Graphics2D g2 = image.createGraphics();
        icon.paintIcon(null, g2, 0, 0);
        g2.dispose();
        
        File tempFile = File.createTempFile("latex_", ".png", tempDir);
        ImageIO.write(image, "png", tempFile);
        
        String imageUrl = tempFile.toURI().toURL().toString();
        latexCache.put(latex, imageUrl);
        return imageUrl;
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;");
    }

    private File createTempDirectory() {
        try {
            File tempDir = File.createTempFile("noteworthy_", "");
            tempDir.delete();
            tempDir.mkdir();
            return tempDir;
        } catch (Exception e) {
            throw new RuntimeException("Could not create temp directory", e);
        }
    }
    
    public void cleanup() {
        // Clean up temp files when closing
        for (File file : tempDir.listFiles()) {
            file.delete();
        }
        tempDir.delete();
    }
}