package org.geoserver.fmte.web;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.fmte.contants.GeoServerConstants;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Paths;
import org.geoserver.rest.RestletException;
import org.geoserver.template.GeoServerTemplateLoader;
import org.geoserver.wms.featureinfo.HTMLFeatureInfoOutputFormat;
import org.geotools.util.logging.Logging;
import org.restlet.data.Status;

public class TemplateManager implements Serializable {
    private static final Logger LOGGER = Logging.getLogger(TemplateManager.class);

    public static TemplateResourceObject readTemplate(String filename, ResourceInfo resource,
            String charset) throws IOException {
        GeoServerTemplateLoader templateLoader = getGeoServerTemplateLoader();

        templateLoader.setResource(resource);

        Object _tpl = templateLoader.findTemplateSource(filename);
        Reader reader = templateLoader.getReader(_tpl, charset);
        String tplstring = IOUtils.toString(reader);

        TemplateResourceObject templ;
        String source = "default template";
        if (_tpl instanceof File) {
            String filepath = ((File) _tpl).getPath();
            source = filepath.substring(filepath.indexOf(GeoServerConstants.WORKSPACES));
        }
        templ = new TemplateResourceObject(tplstring, source, filename, resource.getName(),
                resource.getNamespace().getName());
        String destpath = Paths.path(GeoServerConstants.WORKSPACES,
                resource.getNamespace().getName(), resource.getStore().getName(),
                resource.getName(), filename);
        templ.setDestpath(destpath);
        return templ;
    }

    public static String saveTemplate(TemplateResourceObject tpl, GeoServerResourceLoader loader) {
        String msg;
        if (tpl.getDirty().equalsIgnoreCase("")) {
            msg = "Template unchanged. Nothing saved";
            return msg;
        }

        if (tpl.getDestpath() == null) {
            msg = "Undefined destination path. Doing nothing";
            return msg;
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(loader.get(tpl.getDestpath()).out());
            byte[] tplContent = tpl.getContent().getBytes();
            out.write(tplContent);
            out.flush();
            msg = "Template written to " + tpl.getDestpath();
        } catch (IOException e) {
            msg = "Undefined destination path. Doing nothing";
            throw new RestletException("Error creating file", Status.SERVER_ERROR_INTERNAL, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        tpl.setOriginalContent(tpl.getContent());
        tpl.setSrcpath(tpl.getDestpath()); //template is now local !
        return msg;
    }

    /*
     * Deletes the .tpl file if it is in the same folder than the layer (dedicated .tpl file)
     */
    public static String deleteTemplate(TemplateResourceObject tpl, GeoServerResourceLoader loader,
            ResourceInfo resource, String charset) throws IOException {
        String msg="";
         // We check if the file (resource) is dedicated to this layer : 
        // we won't allow to delete a shared template !
        if (tpl.getSrcpath().equalsIgnoreCase(tpl.getDestpath())) {
            loader.remove(tpl.getDestpath());
            LOGGER.info("remove file "+tpl.getDestpath());
            msg = "Removed file. ";
            
            //reload
            GeoServerTemplateLoader templateLoader = getGeoServerTemplateLoader();
            templateLoader.setResource(resource);
            Object _tpl = templateLoader.findTemplateSource(tpl.getFilename());
            Reader reader = templateLoader.getReader(_tpl, charset);
            String tplstring = IOUtils.toString(reader);

            tpl.setOriginalContent(tplstring); //so that reload will properly work
            tpl.setContent(tplstring);
            String source = "default template"; 
            if (_tpl instanceof File) {
                String filepath = ((File) _tpl).getPath();
                source = filepath.substring(filepath.indexOf(GeoServerConstants.WORKSPACES));
            } 
            tpl.setSrcpath(source);
            
            msg += "Reloading template config.";
        } else {
            msg = "Cannot remove file : not allowed (this template belgons to the datastore or upper)";
        }
        
        return msg;
    }

    public static GeoServerTemplateLoader getGeoServerTemplateLoader() throws IOException {
        GeoServerTemplateLoader templateLoader = new GeoServerTemplateLoader(
                HTMLFeatureInfoOutputFormat.class);
        // using HTMLFeatureInfoOutputFormat as caller to allow detection of the templates in
        // classpath, if no file is found
        return templateLoader;
    }

}
