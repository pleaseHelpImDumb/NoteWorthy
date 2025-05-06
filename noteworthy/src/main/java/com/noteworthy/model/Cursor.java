package com.noteworthy.model;


/**
 * This had to be simplified and doesn't work as a normal cursor between characters.
 * Instead, it works as a blinking cursor between blocks. 
 * (No in-line editting for PlainText yet)
 */

public class Cursor {
 
    private Document doc;

    private int pos;
    private int startSelection;
    private int endSelection;


    /**
     * 
     * @param doc
     */
    public Cursor(Document doc){
        this.doc = doc;
        this.pos = 0;
        this.startSelection = 0;
        this.endSelection = 0;
    }

    /**
     * 
     * @return
     */
    public int getPosition(){
        return this.pos;
    }

    /**
     * 
     * @param newPos
     */
    public void setPosition(int newPos){
        this.pos = newPos;
    }

    /**
     * 
     * @return
     */
    public boolean hasSelection(){
        return !(startSelection == endSelection);
    }
    
    /**
     * 
     * @param left - the start of the selection
     * @param right - the end of the selection
     */
    public void setSelection(int left, int right){
        this.startSelection = left;
        this.endSelection = right;
    }

    /**
     * Clears selection by setting both indecies to 0
     */
    public void clearSelection(){
        this.startSelection = 0;
        this.endSelection = 0;
    }

    /**
     * 
     * @return
     */
    public int getSelectionStart(){
        return this.startSelection;
    }

    /**
     * 
     * @return
     */
    public int getSelectionEnd(){
        return this.endSelection;
    }

    public void moveLeft(){
        this.pos++;
    }

    public void moveRight(){
        this.pos--;
    }

    /**
     * Methods below are tricky to implement. Need to redo looking inside blocks and index
     */
    public void moveToStartOfLine(){

    }
    public void moveToEndOfLine(){

    }
    void moveToStartOfDocument(){
        this.pos = 0;
    }
    public void moveToEndOfDOcument(){

    }
    public int getLineNumber(){
        return doc.getBlockCount();
    }
}
