package com.noteworthy.model;

public class PlainTextBlock implements ContentBlock {
    
    private String content;

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
        return content.length();
    }

    @Override
    public String getType() {
        return "[PlainText Block]";
    }

}
