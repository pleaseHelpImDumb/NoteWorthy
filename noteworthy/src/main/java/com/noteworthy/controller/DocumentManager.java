package com.noteworthy.controller;
import com.noteworthy.model.*;
import java.util.regex.*;

public class DocumentManager {
    private static final Pattern LATEX_PATTERN = Pattern.compile("\\$\\$(.*?)\\$\\$", Pattern.DOTALL);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(.*?)```", Pattern.DOTALL);
    private static final Pattern FILE_PATH_PATTERN = Pattern.compile(
        "(?:[A-Za-z]:\\\\[^\\n]+\\.(jpg|jpeg|png|gif|bmp)|" +  // Windows paths
        "(?:/|~)[^\\n]+\\.(jpg|jpeg|png|gif|bmp))",            // Unix paths
        Pattern.CASE_INSENSITIVE
    );
   
    public Document parseTextToDocument(String title, String textContent) {
        Document doc = new Document(title);
        parseContent(textContent, doc);
        return doc;
    }
   
    private void parseContent(String content, Document doc) {
        // Keep track of where we are in the content
        int currentPos = 0;
        
        while (currentPos < content.length()) {
            // Find the next LaTeX block
            Matcher latexMatcher = LATEX_PATTERN.matcher(content);
            latexMatcher.region(currentPos, content.length());
            boolean foundLatex = latexMatcher.find();
            
            // Find the next Code block
            Matcher codeMatcher = CODE_BLOCK_PATTERN.matcher(content);
            codeMatcher.region(currentPos, content.length());
            boolean foundCode = codeMatcher.find();
            
            // Find the next Image file path
            Matcher pathMatcher = FILE_PATH_PATTERN.matcher(content);
            pathMatcher.region(currentPos, content.length());
            boolean foundPath = pathMatcher.find();

            // If we found neither special block, add the rest as plain text and exit
            if (!foundLatex && !foundCode && !foundPath) {
                String remainingText = content.substring(currentPos);
                if (!remainingText.isEmpty()) {
                    doc.appendBlock(new PlainTextBlock(remainingText));
                }
                break;
            }
            
            // Determine which special block comes first
            int latexPos = foundLatex ? latexMatcher.start() : Integer.MAX_VALUE;
            int codePos = foundCode ? codeMatcher.start() : Integer.MAX_VALUE;
            int pathPos = foundPath ? pathMatcher.start() : Integer.MAX_VALUE;
            
            if (latexPos < codePos && latexPos < pathPos) {
                // LaTeX block comes first
                
                // Add plain text before the LaTeX block if any
                if (latexPos > currentPos) {
                    String plainText = content.substring(currentPos, latexPos);
                    doc.appendBlock(new PlainTextBlock(plainText));
                }
                
                // Add the LaTeX block
                String latexContent = latexMatcher.group(1);
                doc.appendBlock(new LaTeXBlock("$$" + latexContent + "$$"));
                
                // Update position
                currentPos = latexMatcher.end();
            } 
            else if (codePos < latexPos && codePos < pathPos) {
                // Code block comes first
                
                // Add plain text before the code block if any
                if (codePos > currentPos) {
                    String plainText = content.substring(currentPos, codePos);
                    doc.appendBlock(new PlainTextBlock(plainText));
                }
                
                // Add the code block
                String codeContent = codeMatcher.group(1);
                doc.appendBlock(new CodeBlock("```\n"+codeContent+"\n```"));
                
                // Update position
                currentPos = codeMatcher.end();
            }
            else {
                // Image path comes first
                
                // Add plain text before the image path if any
                if (pathPos > currentPos) {
                    String plainText = content.substring(currentPos, pathPos);
                    doc.appendBlock(new PlainTextBlock(plainText));
                }
                
                // Add the image block with the full path
                String imagePath = pathMatcher.group(0);
                doc.appendBlock(new ImageBlock(imagePath));
                
                // Update position
                currentPos = pathMatcher.end();
            }
        }
    }
}