package org.geoserver.fmte.web;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.fmte.contants.GeoServerConstants;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.platform.resource.Paths;
import org.geoserver.platform.resource.Resource;
import org.geoserver.rest.RestletException;
import org.geoserver.template.GeoServerTemplateLoader;
import org.geoserver.wms.featureinfo.HTMLFeatureInfoOutputFormat;
import org.geotools.util.logging.Logging;
import org.restlet.data.Status;

import freemarker.cache.ClassTemplateLoader;

public class TemplateManager implements Serializable {
    /** serialVersionUID */
    private static final long serialVersionUID = 7291211731171362557L;

    private static final Logger LOGGER = Logging.getLogger(TemplateManager.class);

    @Deprecated
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
        templ = new TemplateResourceObject(tplstring, source, "", resource.getName(),
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

        LOGGER.info("Saving template " + tpl.getFilename() + " to " + tpl.getSavePath());

        if (tpl.getSavePath() == null) {
            msg = "Undefined destination path. Doing nothing";
            return msg;
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(loader.get(tpl.getSavePath()).out());
            byte[] tplContent = tpl.getContent().getBytes();
            out.write(tplContent);
            out.flush();
            msg = "Template written to " + tpl.getSavePath();
        } catch (IOException e) {
            msg = "Undefined destination path. Doing nothing";
            throw new RestletException("Error creating file", Status.SERVER_ERROR_INTERNAL, e);
        } finally {
            IOUtils.closeQuietly(out);
        }
        tpl.setOriginalContent(tpl.getContent());
        tpl.setSrcpath(tpl.getDestpath()); // template is now local !
        return msg;
    }

    /*
     * Deletes the .tpl file if it is in the same folder than the layer (dedicated .tpl file)
     */
    @Deprecated
    public static String deleteTemplate(TemplateResourceObject tpl, GeoServerResourceLoader loader,
            ResourceInfo resource, String charset) throws IOException {
        String msg = "";
        // We check if the file (resource) is dedicated to this layer :
        // we won't allow to delete a shared template !
        if (tpl.getSrcpath().equalsIgnoreCase(tpl.getDestpath())) {
            loader.remove(tpl.getDestpath());
            LOGGER.info("remove file " + tpl.getDestpath());
            msg = "Removed file. ";

            // reload
            GeoServerTemplateLoader templateLoader = getGeoServerTemplateLoader();
            templateLoader.setResource(resource);
            Object _tpl = templateLoader.findTemplateSource(tpl.getFilename());
            Reader reader = templateLoader.getReader(_tpl, charset);
            String tplstring = IOUtils.toString(reader);

            tpl.setOriginalContent(tplstring); // so that reload will properly work
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

    @Deprecated
    public static GeoServerTemplateLoader getGeoServerTemplateLoader() throws IOException {
        GeoServerTemplateLoader templateLoader = new GeoServerTemplateLoader(
                HTMLFeatureInfoOutputFormat.class);
        // using HTMLFeatureInfoOutputFormat as caller to allow detection of the templates in
        // classpath, if no file is found
        return templateLoader;
    }

    public static TemplateResourceObject readTemplate(String tplName, List<String> resourcePaths,
            GeoServerResourceLoader loader, String charset) throws IOException {
        TemplateResourceObject template = null;

        Iterator<String> it = resourcePaths.iterator();
        String actualPath = "";
        String tplcontent = null;
        while (it.hasNext()) {
            String path = it.next();
            String tplpath = Paths.path(path, tplName);
            LOGGER.info("looking in " + tplpath);
            Resource res = loader.get(tplpath);
            if (res.getType() == Resource.Type.RESOURCE) {
                LOGGER.fine("Template path is " + tplpath);

                BufferedInputStream in = null;
                try {
                    in = new BufferedInputStream(res.in());
                    tplcontent = IOUtils.toString(in);
                    actualPath = path;

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                } finally {
                    IOUtils.closeQuietly(in);
                }

                break;
            }
        }
        if (tplcontent == null) {
            tplcontent = loadTemplateFromClassLoader(HTMLFeatureInfoOutputFormat.class, tplName,
                    charset);
            actualPath = "default/";
        }
        LOGGER.fine("Template content is " + tplcontent);
        template = new TemplateResourceObject(tplName, tplcontent, actualPath);
        template.setAvailablePaths(resourcePaths);
        template.setDestpath(resourcePaths.get(0));
        return template;
    }

    /*
     * Deletes the .tpl file if it is in the same folder than the layer (dedicated .tpl file)
     */
    public static String deleteTemplate(TemplateResourceObject tpl, GeoServerResourceLoader loader,
            String charset) throws IOException {
        String msg = "";
        // We check if the file (resource) is dedicated to this layer :
        // we won't allow to delete a shared template !
        if (tpl.getSrcpath().equalsIgnoreCase(tpl.getDestpath())) {
            loader.remove(tpl.getSavePath());
            LOGGER.info("remove file " + tpl.getSavePath());
            msg = "Removed file. ";

            // reload
            List<String> paths = tpl.getAvailablePaths();
            paths.remove(0);
            TemplateResourceObject t = TemplateManager.readTemplate(tpl.getFilename(), paths,
                    loader, charset);
            tpl.from(t);
            msg += "Reloading template config.";
        } else {
            msg = "Cannot remove file : not allowed (this template belongs to the datastore or upper)";
        }

        return msg;
    }

    private static String loadTemplateFromClassLoader(Class caller, String tplName, String charset)
            throws IOException {
        ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(caller, "");
        // ClassTemplateLoader classTemplateLoader = new ClassTemplateLoader(caller, "/org/geoserver/wms/featureinfo");
        // final effort to use a class resource
        if (classTemplateLoader != null) {
            Object source = classTemplateLoader.findTemplateSource(tplName);
            Reader reader = classTemplateLoader.getReader(source, charset);
            String tplstring = IOUtils.toString(reader);
            return tplstring;
        }
        return null;
    }
}
