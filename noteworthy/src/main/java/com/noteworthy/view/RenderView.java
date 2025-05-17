package com.noteworthy.view;

import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.swing.JEditorPane;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import org.scilab.forge.jlatexmath.TeXConstants;
import org.scilab.forge.jlatexmath.TeXFormula;
import org.scilab.forge.jlatexmath.TeXIcon;

public class RenderView extends JEditorPane {
    private final File tempDir;
    private final Map<String, String> latexCache;
    private static final Pattern CODE_BLOCK_PATTERN = 
        Pattern.compile("```([^\\n]*)?\\n([\\s\\S]*?)\\n```", Pattern.MULTILINE);
    private static final Pattern IMAGE_PATH_PATTERN = Pattern.compile(
        "(?:[A-Za-z]:\\\\[^\\s]+\\.(jpg|jpeg|png|gif|bmp)|" +  // Windows paths
        "(?:/|~)[^\\s]+\\.(jpg|jpeg|png|gif|bmp))",            // Unix paths
        Pattern.CASE_INSENSITIVE
    );

    private String codeLanguage = "Java";
    
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
            styleSheet.addRule("body { font-family: Sans-Serif; padding: 10px; margin: 0; line-height: 1.5; }");
            styleSheet.addRule("img.latex { vertical-align: middle; margin: 5px 0; display: block; }");
            //styleSheet.addRule("img.embedded { max-width: 95%; margin: 10px 0; display: block; width: 300px;}");
        
            
            // Add code block styling
            styleSheet.addRule("pre.code { " +
                "background-color: #f5f5f5; " +
                "border: 1px solid #ccc; " +
                "border-radius: 4px; " +
                "padding: 8px; " +
                "font-family: 'Courier New', monospace; " +
                "font-size: 0.9em; " +
                "overflow: auto; " +
                "margin: 10px 0; " +
                "}");
            
            // Language-specific header styling
            styleSheet.addRule("div.code-header { " +
                "background-color: #e0e0e0; " +
                "border: 1px solid #ccc; " +
                "border-bottom: none; " +
                "border-top-left-radius: 4px; " +
                "border-top-right-radius: 4px; " +
                "padding: 4px 8px; " +
                "font-family: Sans-Serif; " +
                "font-size: 0.8em; " +
                "color: #555; " +
                "margin: 10px 0 -10px 0; " +
                "}");
                
            // Add language-specific highlighting (just basic colors for now)
            /* 
            styleSheet.addRule("pre.code-java .keyword { color: #7f0055; font-weight: bold; }");
            styleSheet.addRule("pre.code-java .string { color: #2a00ff; }");
            styleSheet.addRule("pre.code-java .comment { color: #3f7f5f; }");
            
            styleSheet.addRule("pre.code-javascript .keyword { color: #0000ff; font-weight: bold; }");
            styleSheet.addRule("pre.code-javascript .string { color: #008000; }");
            styleSheet.addRule("pre.code-javascript .comment { color: #808080; }");
            */
            
            setEditorKit(kit);
        }

    public void updateContent(String content) {
        String html = convertToHtml(content);
        setText(html);
    }

    public void updateContent(String content, String defLang) {
        String html = convertToHtml(content);
        codeLanguage = defLang;
        setText(html);
    }

    private String convertToHtml(String content) {
        if (content == null || content.isEmpty()) {
            return "<html><body><em>No content to render</em></body></html>";
        }

        // Normalize line endings first
        content = content.replace("\r\n", "\n");
        
        // Process code blocks first and replace them with placeholders
        Map<String, String> codeBlockReplacements = new HashMap<>();
        Matcher codeBlockMatcher = CODE_BLOCK_PATTERN.matcher(content);
        StringBuffer sb = new StringBuffer();
        int codeBlockCount = 0;
        
        while (codeBlockMatcher.find()) {
            String language = codeBlockMatcher.group(1).trim();
            String codeContent = codeBlockMatcher.group(2);
            String placeholder = "CODE_BLOCK_" + codeBlockCount + "_PLACEHOLDER";
            codeBlockCount++;
            
            String processedCodeBlock = renderCodeBlock(codeContent, language);
            codeBlockReplacements.put(placeholder, processedCodeBlock);
            codeBlockMatcher.appendReplacement(sb, placeholder);
        }
        codeBlockMatcher.appendTail(sb);
        content = sb.toString();
        
        // Process image paths and replace with placeholders
        Map<String, String> imageReplacements = new HashMap<>();
        Matcher imagePathMatcher = IMAGE_PATH_PATTERN.matcher(content);
        sb = new StringBuffer();
        int imageCount = 0;
        
        while (imagePathMatcher.find()) {
            String imagePath = imagePathMatcher.group(0);
            String placeholder = "IMAGE_" + imageCount + "_PLACEHOLDER";
            imageCount++;
            
            // Create HTML img tag for the image path
            String imgTag = renderImagePath(imagePath);
            imageReplacements.put(placeholder, imgTag);
            imagePathMatcher.appendReplacement(sb, placeholder);
        }
        imagePathMatcher.appendTail(sb);
        content = sb.toString();

        StringBuilder html = new StringBuilder("<html><body>");
        
        // Split content while handling LaTeX blocks
        String[] parts = content.split("(?=\\$\\$)|(?<=\\$\\$)");
        boolean isLatex = false;
        
        for (String part : parts) {
            if ("$$".equals(part)) {
                isLatex = !isLatex;
                continue;
            }
            
            if (isLatex) {
                try {
                    String latexImage = renderLatexToImage(part.trim());
                    html.append("<img class='latex' src='")
                       .append(latexImage)
                       .append("' alt='")
                       .append(escapeHtml(part))
                       .append("'/>");
                } catch (Exception e) {
                    html.append("<span style='color:red'>[LaTeX Error: ")
                        .append(escapeHtml(e.getMessage()))
                        .append("]</span><br>");
                }
            } else {
                // Process plain text with markdown-like formatting
                String safe = escapeHtml(part).replace("\n", "<br>");
                safe = safe.replaceAll("\\*\\*(.+?)\\*\\*", "<strong>$1</strong>");
                safe = safe.replaceAll("\\*(.+?)\\*", "<em>$1</em>");
                safe = safe.replaceAll("__(.+?)__", "<u>$1</u>");

                
                // Restore code blocks from placeholders
                for (Map.Entry<String, String> entry : codeBlockReplacements.entrySet()) {
                    safe = safe.replace(entry.getKey(), entry.getValue());
                }

                // Restore image paths from placeholders
                for (Map.Entry<String, String> entry : imageReplacements.entrySet()) {
                    safe = safe.replace(entry.getKey(), entry.getValue());
                }
                
                html.append(safe);
            }
        }
        
        html.append("</body></html>");
        return html.toString();
    }

    /*
     * Returns a String of an imagePath into an HTML element
     */ 
    private String renderImagePath(String imagePath) {
        File imageFile = new File(imagePath);
        String fileUrl;
        
        try {
            fileUrl = imageFile.toURI().toURL().toString();
        } catch (Exception e) {
            fileUrl = imagePath;
        }
        
        return "<img class='embedded' src='" + fileUrl + "' alt='Image: " 
               + new File(imagePath).getName() + "'>";
    }

    /**
     * Renders a code block with optional syntax highlighting
     */
    private String renderCodeBlock(String code, String language) {
        StringBuilder codeHtml = new StringBuilder();
        
        // Add language header if specified
        if (language != null && !language.isEmpty()) {
            codeHtml.append("<div class='code-header'>")
                   .append(escapeHtml(language))
                   .append("</div>");
        }
        
        codeHtml.append("<pre class='code");
        
        // Add language-specific class if available
        if (language != null && !language.isEmpty()) {
            codeHtml.append(" code-").append(escapeHtml(language.toLowerCase()));
        }
        
        codeHtml.append("'>");
        
        // Apply basic syntax highlighting based on language
        if ("java".equalsIgnoreCase(language)) {
            code = applyJavaSyntaxHighlighting(code);
        } else if ("javascript".equalsIgnoreCase(language) || "js".equalsIgnoreCase(language)) {
            code = applyJavaScriptSyntaxHighlighting(code);
        } else {
            code = escapeHtml(code);
        }
        
        codeHtml.append(code)
               .append("</pre>");
        
        return codeHtml.toString();
    }
    
    /**
     * Basic Java syntax highlighting
     */
    private String applyJavaSyntaxHighlighting(String code) {
        String escaped = escapeHtml(code);
        
        // Java keywords
        String[] keywords = {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new",
            "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while"
        };
        
        for (String keyword : keywords) {
            // Ensure we're matching whole words only
            escaped = escaped.replaceAll("\\b" + keyword + "\\b", "<span class='keyword'>" + keyword + "</span>");
        }
        
        // String literals - this is very basic and doesn't handle escapes properly
        escaped = escaped.replaceAll("\"([^\"]*)\"", "<span class='string'>\"$1\"</span>");
        
        // Single-line comments
        escaped = escaped.replaceAll("//([^<br>]*)", "<span class='comment'>//$1</span>");
        
        return escaped;
    }
    
    /**
     * Basic JavaScript syntax highlighting
     */
    private String applyJavaScriptSyntaxHighlighting(String code) {
        String escaped = escapeHtml(code);
        
        // JavaScript keywords
        String[] keywords = {
            "async", "await", "break", "case", "catch", "class", "const", "continue", "debugger", "default",
            "delete", "do", "else", "export", "extends", "false", "finally", "for", "function", "if",
            "import", "in", "instanceof", "let", "new", "null", "return", "super", "switch", "this",
            "throw", "true", "try", "typeof", "var", "void", "while", "with", "yield"
        };
        
        for (String keyword : keywords) {
            // Ensure we're matching whole words only
            escaped = escaped.replaceAll("\\b" + keyword + "\\b", "<span class='keyword'>" + keyword + "</span>");
        }
        
        // String literals - very basic handling
        escaped = escaped.replaceAll("\"([^\"]*)\"", "<span class='string'>\"$1\"</span>");
        escaped = escaped.replaceAll("'([^']*)'", "<span class='string'>'$1'</span>");
        
        // Single-line comments
        escaped = escaped.replaceAll("//([^<br>]*)", "<span class='comment'>//$1</span>");
        
        return escaped;
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
            File temp = File.createTempFile("noteworthy_", "");
            temp.delete();
            temp.mkdir();
            return temp;
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
