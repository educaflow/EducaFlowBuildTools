/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.educaflow.common.buildtools.files.tipoexpediente;

/**
 *
 * @author logongas
 */
public class TipoDocumentoPdf {
    
    private String enumValue;
    private String fileName;

    public TipoDocumentoPdf(String enumValue, String fileName) {
        this.enumValue = enumValue;
        this.fileName = fileName;
    }

    
    
    /**
     * @return the enumValue
     */
    public String getEnumValue() {
        return enumValue;
    }

    /**
     * @param enumValue the enumValue to set
     */
    public void setEnumValue(String enumValue) {
        this.enumValue = enumValue;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @param fileName the fileName to set
     */
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
}
