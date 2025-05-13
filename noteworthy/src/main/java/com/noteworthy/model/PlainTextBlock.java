package com.noteworthy.model;

import java.util.Map;

public class PlainTextBlock implements ContentBlock {
    
    private String content;
    private Map<String, Boolean> formatting;
    private Map<String, Object> styles;
    
    public PlainTextBlock(){
        this.content = "";
    }

    public PlainTextBlock(String text){
        this.content = text;
    }

    @Override
    public String getTextContent(){
        return content;
    }

    public void setContent(String text){
        this.content = text;
    }

    public void appendText(String text){
        this.content = this.content.concat(text);
    }

    @Override
    public int length() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'length'");
    }

    @Override
    public void render() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'render'");
    }

    @Override
    public String getType() {
        return "[PlainText Block]";
    }

}
