package com.noteworthy.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Document {
    
    //Content Storage
    private ArrayList<ContentBlock> blocks;
    //Docment Metadata
    private String title;
    private LocalDateTime creationDate;
    private LocalDateTime lastModifiedDate;
    private boolean isModified;
    //Formatting
    private double defaultFontSize;
    private String defaultFontFamily;
    //Cursor
    private Cursor cursor;

    /**
     * Constructs a new Document object with a default title based on the current date and time.
     * It initializes an empty list of content blocks, sets the creation and last modified dates to the current time,
     * and sets default font properties and a cursor.
     */
    public Document(){
        String currentTime = LocalDateTime.now().toString();
        this.title = (currentTime.substring(0,10) + "_" + currentTime.substring(11, 16));
        this.blocks = new ArrayList<ContentBlock>();
        this.creationDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.defaultFontSize = 12.0;
        this.defaultFontFamily = "Arial";
        this.cursor = new Cursor(this);
    }
    /**
     * Constructs a new Document object with a specified title.
     * It initializes an empty list of content blocks, sets the creation and last modified dates to the current time,
     * and sets default font properties and a cursor.
     *
     * @param importTitle The title to assign to this document.
     */
    public Document(String importTitle){
        this.title = importTitle;

        this.blocks = new ArrayList<ContentBlock>();
        this.creationDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.defaultFontSize = 12.0;
        this.defaultFontFamily = "Arial";
        this.cursor = new Cursor(this);
    }
    
    /**
     * Constructs a new Document object with a specified title and an initial list of content blocks.
     * It sets the creation and last modified dates to the current time and sets default font properties and a cursor.
     *
     * @param importTitle  The title to assign to this document.
     * @param importBlocks The initial list of content blocks for this document.
     */
    public Document(String importTitle, ArrayList<ContentBlock> importBlocks){
        this.title = importTitle;
        this.blocks = importBlocks;

        this.creationDate = LocalDateTime.now();
        this.lastModifiedDate = LocalDateTime.now();
        this.defaultFontSize = 12.0;
        this.defaultFontFamily = "Arial";
        this.cursor = new Cursor(this);
    }

    public ArrayList<ContentBlock> getBlocks(){
        return this.blocks;
    }

    public void insertBlock(ContentBlock insertion, int index){
        this.blocks.add(index, insertion);
    }

    public ContentBlock removeBlock(int index){
        return this.blocks.remove(index);
    }

    public void appendBlock(ContentBlock insertion){
        this.blocks.add(insertion);
    }

    public ContentBlock getBlock(int index){
        return this.blocks.get(index);
    }

    public int getBlockCount(){
        return this.blocks.size();
    }

    public Cursor getCursor(){
        return this.cursor;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String newTitle){
        this.title = newTitle;
    }

    public LocalDateTime getCreationDate(){
        return this.creationDate;
    }

    public LocalDateTime getLastModifiedDate(){
        return this.lastModifiedDate;
    }

    public boolean isModified(){
        return this.isModified;
    }

    public void setModified(boolean b){
        this.isModified = b;
    }

    /**
     * The getTextContent method concatenates the content of different types of blocks in a document
     * and returns the combined text content.
     * 
     * @return The `getTextContent` method returns a concatenated string of content from different
     * types of blocks in the `blocks` list. It concatenates the content of PlainTextBlock, the caption
     * of ImageBlock, the equation of LaTeXBlock, and the code of CodeBlock. The final concatenated
     * string is the document content that is returned by the method.
     */
    public String getTextContent(){
        String documentContent = "";
        for(int i = 0; i < blocks.size(); i++){
            ContentBlock indexedBlock = blocks.get(i);
            if (indexedBlock instanceof PlainTextBlock) {
                PlainTextBlock textBlock = (PlainTextBlock) indexedBlock;
                documentContent = documentContent.concat(textBlock.getTextContent());
            }
            else if (indexedBlock instanceof ImageBlock){
                ImageBlock imgBlock = (ImageBlock) indexedBlock;
                documentContent = documentContent.concat("\n" + imgBlock.getCaption());
            }
            else if (indexedBlock instanceof LaTeXBlock){
                LaTeXBlock latexBlock = (LaTeXBlock) indexedBlock;
                documentContent = documentContent.concat("\n" + latexBlock.getEquation());
            }
            else if (indexedBlock instanceof CodeBlock){
                CodeBlock codeBlock = (CodeBlock) indexedBlock;
                documentContent = documentContent.concat("\n" + codeBlock.getTextContent());
            }
        }
        return documentContent;
    }

    public void setContent(ArrayList<ContentBlock> newBlocks){
        this.blocks = newBlocks;
    }

    public void setDefaultFontFamily(String newFontFamily){
        this.defaultFontFamily = newFontFamily;
    }

    public String getDefaultFontFamily(){
        return this.defaultFontFamily;
    }

    public void setDefaultFontSize(double newFontSize){
        this.defaultFontSize = newFontSize; 
    }

    public double getDefaultFontSize(){
        return this.defaultFontSize;
    }

    public void debugPrintBlocks() {
        for(int i = 0; i < blocks.size(); i++){
            System.out.println("Type: " + blocks.get(i).getType() + " // Content: [" + blocks.get(i).getTextContent()+"]");
        }
    }
}
