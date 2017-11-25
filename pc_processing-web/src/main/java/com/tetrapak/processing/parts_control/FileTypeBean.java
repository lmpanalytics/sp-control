/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

/**
 * Manages file type to upload
 *
 * @author SEPALMM
 */
@Named(value = "fileTypeBean")
@RequestScoped
public class FileTypeBean {

    private String fileType;

    /**
     * Creates a new instance of FileTypeBean
     */
    public FileTypeBean() {
    }

    @PostConstruct
    public void init() {
//        System.out.println("I'm in FileTypeBean init() method");
        // Initialize filetype
        this.fileType = "";
    }

    public String getFileType() {
        return fileType;
    }

    private void setFileType(String fileType) {
        this.fileType = fileType;
//        System.out.printf("Selected file type: %s\n", this.fileType);
    }

}
