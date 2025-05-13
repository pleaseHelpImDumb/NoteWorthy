package com.noteworthy.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.*;

import com.noteworthy.model.CodeBlock;
import com.noteworthy.model.ContentBlock;
import com.noteworthy.model.Document;
import com.noteworthy.model.ImageBlock;
import com.noteworthy.model.LaTeXBlock;
import com.noteworthy.model.PlainTextBlock;

public class DocumentManager {
    // Regex patterns for different block types
    private static final Pattern LATEX_PATTERN = Pattern.compile("\\$\\$(.*?)\\$\\$", Pattern.DOTALL);
    private static final Pattern CODE_PATTERN = Pattern.compile("```(\\w*)\\n(.*?)```", Pattern.DOTALL);
    private static final Pattern IMAGE_PATTERN = Pattern.compile("\\[(.*?)\\]\\((.*?)\\)");
    
    public Document parseTextToDocument(String title, String textContent) {
        Document newDoc = new Document(title);
        List<ContentBlock> blocks = parseContent(textContent);
        blocks.forEach(newDoc::appendBlock);
        return newDoc;
    }
    
    public List<ContentBlock> parseContent(String content) {
        List<ContentBlock> blocks = new ArrayList<>();
        int lastPos = 0;
        
        // Find all special blocks first
        List<MatchResult> allMatches = new ArrayList<>();
        allMatches.addAll(findAllMatches(LATEX_PATTERN, content));
        allMatches.addAll(findAllMatches(CODE_PATTERN, content));
        allMatches.addAll(findAllMatches(IMAGE_PATTERN, content));
        
        // Sort matches by their start position
        allMatches.sort((a, b) -> a.start() - b.start());
        
        // Process the content
        for (MatchResult match : allMatches) {
            // Add plain text before this match if there is any
            if (match.start() > lastPos) {
                String plainText = content.substring(lastPos, match.start());
                blocks.add(new PlainTextBlock(plainText));
            }
            
            /* OTHER PATTERNS WIP
            // Add the matched block
            String matchedContent = match.group(1);
            if (match.pattern() == LATEX_PATTERN) {
                blocks.add(new LaTeXBlock(matchedContent));
            } 
            else if (match.pattern() == CODE_PATTERN) {
                String language = match.group(1);
                String code = match.group(2);
                blocks.add(new CodeBlock(code));
            } 
            else if (match.pattern() == IMAGE_PATTERN) {
                String altText = match.group(1);
                String path = match.group(2);
                blocks.add(new ImageBlock(path, altText));
            }
            */
            
            lastPos = match.end();
        }
        
        // Add remaining plain text
        if (lastPos < content.length()) {
            String remainingText = content.substring(lastPos);
            blocks.add(new PlainTextBlock(remainingText));
        }
        
        return blocks;
    }
    
    private List<MatchResult> findAllMatches(Pattern pattern, String content) {
        List<MatchResult> matches = new ArrayList<>();
        Matcher matcher = pattern.matcher(content);
        
        while (matcher.find()) {
            matches.add(matcher.toMatchResult());
        }
        
        return matches;
    }
}