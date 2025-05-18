package com.noteworthy.model;

public class ImageBlock implements ContentBlock {


    private String imgPath;

    public ImageBlock(){
        imgPath = "";
    }

    public ImageBlock(String path){
        imgPath = path;
    }

    public String getCaption(){
       return "[Image Block]";
    }

    @Override
    public String getTextContent() {
        return imgPath;
    }

    @Override
    public int length() {
        return imgPath.length();
    }

    @Override
    public String getType() {
        return "[Image Block]";
    }
    
}
