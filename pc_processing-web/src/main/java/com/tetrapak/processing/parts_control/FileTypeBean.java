/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tetrapak.processing.parts_control;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.inject.Named;

/**
 * Manages file type to upload. The bean is session scoped to keep in session
 * the file type as the type needs to be accessible from the FileLoadBean.
 *
 * @author SEPALMM
 */
@Named(value = "fileTypeBean")
@SessionScoped
public class FileTypeBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private String fileType;

    /**
     * Creates a new instance of FileTypeBean
     */
    public FileTypeBean() {
    }

    @PostConstruct
    public void init() {
//        System.out.println("I'm in FileTypeBean init() method");
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
//        System.out.printf("Selected file type: %s\n", this.fileType);
    }

}
