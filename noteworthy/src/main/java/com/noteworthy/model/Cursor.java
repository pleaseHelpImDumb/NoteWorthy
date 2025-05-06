package com.noteworthy.model;

public class Cursor {
 
    private Document doc;

    private int pos;
    private int startSelection;
    private int endSelection;


    public Cursor(Document doc){
        this.doc = doc;
        this.pos = 0;
        this.startSelection = 0;
        this.endSelection = 0;
    }
}
