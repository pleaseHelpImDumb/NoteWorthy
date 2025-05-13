package com.noteworthy.controller;

import com.noteworthy.model.*;
import java.util.regex.*;

public class DocumentManager {
    private static final Pattern LATEX_PATTERN = Pattern.compile("\\$\\$(.*?)\\$\\$", Pattern.DOTALL);
    
    public Document parseTextToDocument(String title, String textContent) {
        Document doc = new Document(title);
        parseContent(textContent, doc);
        return doc;
    }
    
    private void parseContent(String content, Document doc) {
        Matcher matcher = LATEX_PATTERN.matcher(content);
        int lastPos = 0;
        
        while (matcher.find()) {
            // Add plain text before LaTeX block
            if (matcher.start() > lastPos) {
                String plainText = content.substring(lastPos, matcher.start());
                doc.appendBlock(new PlainTextBlock(plainText));
            }
            
            // Add LaTeX block
            String latexContent = matcher.group(1);
            doc.appendBlock(new LaTeXBlock("$$" + latexContent + "$$"));
            
            lastPos = matcher.end();
        }
        
        // Add remaining plain text
        if (lastPos < content.length()) {
            String remainingText = content.substring(lastPos);
            doc.appendBlock(new PlainTextBlock(remainingText));
        }
    }
}