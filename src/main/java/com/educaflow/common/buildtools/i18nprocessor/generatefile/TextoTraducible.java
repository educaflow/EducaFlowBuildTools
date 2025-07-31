/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.i18nprocessor.generatefile;

import com.opencsv.bean.CsvBindByName;

/**
 *"key","message","comment","context"
 * @author logongas
 */
public class TextoTraducible {
    @CsvBindByName(column = "key", required = true)
    private String key;

    @CsvBindByName(column = "message")
    private String message;

    @CsvBindByName(column = "comment")
    private String comment;
    
    @CsvBindByName(column = "context")
    private String context;    

    
    
    
    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the nombre
     */
    public String getMessage() {
        return message;
    }

    /**
     * @param message the message to set
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * @return the comment
     */
    public String getComment() {
        return comment;
    }

    /**
     * @param comment the comment to set
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @param context the context to set
     */
    public void setContext(String context) {
        this.context = context;
    }
    
    
    
}
