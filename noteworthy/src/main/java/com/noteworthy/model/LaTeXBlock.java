package com.noteworthy.model;

public class LaTeXBlock implements ContentBlock{

    private String equation;

    public LaTeXBlock(String equation){
        this.equation = equation;
    }

    public String getEquation() {
        return equation;
    }

    @Override
    public String getTextContent() {
        return equation;
    }

    @Override
    public int length() {
        return equation.length();
    }
    

    @Override
    public String getType() {
        return "[LaTeX Block]";
    }
    
}
