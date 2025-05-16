package com.noteworthy.model;

public class CodeBlock implements ContentBlock{

    private String code;

    public CodeBlock(){
        this.code = "";
    }
            
    
    public CodeBlock(String newCode){
        this.code = newCode;
    }

    @Override
    public String getTextContent() {
        return code;
    }

    @Override
    public int length() {
        return code.length();
    }
    
    @Override
    public String getType() {
        return "[Code Block]";
    }
    
}
