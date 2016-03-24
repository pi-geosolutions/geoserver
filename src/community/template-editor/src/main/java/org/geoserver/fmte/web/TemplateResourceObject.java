package org.geoserver.fmte.web;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serializable;

import org.apache.commons.io.IOUtils;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Paths;
import org.geoserver.rest.RestletException;
import org.restlet.data.Status;

public class TemplateResourceObject implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = 5135224484018193841L;

    private String filename, layername, workspacename;
    
    private String originalContent;
    private String content;
    
    private String dirty=""; //marker telling if content has changed (needs saving)

    private String srcpath;

    private String destpath;

    public TemplateResourceObject(String tpl, String source, String filename, 
            String layername, String wsname) {
        this.originalContent = tpl;
        this.content = tpl;
        this.srcpath = source;
        this.filename = filename;
        this.layername = layername;
        this.workspacename = wsname;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
    
    public String getOriginalContent() {
        return originalContent;
    }

    public void setOriginalContent(String content) {
        this.originalContent = content;
    }
    
    public void resetContent() {
        this.content = this.originalContent;
    }

    public String getDirty() {
        return dirty;
    }

    public void setDirty(String dirty) {
        this.dirty = dirty;
    }

    public String getSrcpath() {
        return srcpath;
    }

    public String getDestpath() {
        return destpath;
    }

    public void setDestpath(String destpath) {
        this.destpath = destpath;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getLayername() {
        return layername;
    }

    public void setLayername(String layername) {
        this.layername = layername;
    }

    public String getWorkspacename() {
        return workspacename;
    }

    public void setWorkspacename(String workspacename) {
        this.workspacename = workspacename;
    }

    public void setSrcpath(String srcpath) {
        this.srcpath = srcpath;
    }
}
