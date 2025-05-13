package com.noteworthy.controller;

import com.noteworthy.model.Document;
import com.noteworthy.view.WindowView;

public class EditorController {

    private WindowView window;
    private Document doc;
    private DocumentManager docManager;

    public EditorController(){
        this.doc = new Document();
        docManager = new DocumentManager();
    }
    public EditorController(String docTitle){
        this.doc = new Document(docTitle);
        docManager = new DocumentManager();
    }

    public Document parseTextToDocument(String title, String textContent){
        doc = docManager.parseTextToDocument(title, textContent);
        return doc;
    }

    public String renderDocument(){
        doc.debugPrintBlocks();
        return doc.getTextContent();
    }

    public String processContent(String content) {
        return content;
    }
    
}
