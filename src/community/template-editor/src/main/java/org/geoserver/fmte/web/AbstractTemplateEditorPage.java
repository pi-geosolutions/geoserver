package org.geoserver.fmte.web;

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.web.ComponentAuthorizer;
import org.geoserver.web.GeoServerSecuredPage;
import org.geotools.util.logging.Logging;

/**
 * Base page for creating/editing templates
 */
@SuppressWarnings("serial")
public abstract class AbstractTemplateEditorPage extends GeoServerSecuredPage {
    protected static final Logger LOGGER = Logging.getLogger(AbstractTemplateEditorPage.class);

    protected String msg_pathNotSet = "Error, resource path is undefined";

    private String fullname = "";

    protected String path;

    protected List<String> resourcePaths;

    protected String resourceType = "abstract";

    protected TemplateResourceObject tpl_header, tpl_content, tpl_footer;
    
    protected AbstractTemplateFormPanel headerFormPanel, contentFormPanel, footerFormPanel;

    public AbstractTemplateEditorPage() {
    }

    public AbstractTemplateEditorPage(PageParameters parameters) {
        this.resourcePaths = this.getResourcePaths(parameters);
        LOGGER.info("Resource path is " + path);
        this.fullname = this.buildResourceFullName();
        LOGGER.info("Resource full name is " + fullname);

        this.loadData();

        this.initComponents();

        /*
         * ResourceInfo resource = getResource(name, workspaceName); this.initComponents(resource);
         */
    }

    private void loadData() {
        try {
            readTemplates();
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("Oops, error retrieving template files");
        }
    }

    private void readTemplates() throws IOException {
        if (this.resourcePaths == null) {
            LOGGER.severe(msg_pathNotSet);
            return;
        }
        GeoServerResourceLoader loader = this.getCatalog().getResourceLoader();
        tpl_header = TemplateManager.readTemplate("header.ftl", resourcePaths, loader,
                getCharset());
        tpl_content = TemplateManager.readTemplate("content.ftl", resourcePaths, loader,
                getCharset());
        tpl_footer = TemplateManager.readTemplate("footer.ftl", resourcePaths, loader,
                getCharset());
    }

    /**
     * Builds a list of available paths where to look for templates
     * 
     * @param parameters URL parameters used to identify the resource and build the paths
     * @return List<String> List of paths (as Strings)
     */
    protected abstract List<String> getResourcePaths(PageParameters parameters);

    protected abstract String buildResourceFullName();

    protected void initComponents() {

        add(new Label( "name", Model.of(fullname)));
        add(new Label( "type", Model.of(resourceType)));
        
        headerFormPanel = new AbstractTemplateFormPanel("headerTplPanel",
                new CompoundPropertyModel(tpl_header));
        headerFormPanel.setParent(this);
        add(headerFormPanel);

        contentFormPanel = new AbstractTemplateFormPanel(
                "contentTplPanel", new CompoundPropertyModel(tpl_content));
        contentFormPanel.setParent(this);
        add(contentFormPanel);

        footerFormPanel = new AbstractTemplateFormPanel("footerTplPanel",
                new CompoundPropertyModel(tpl_footer));
        footerFormPanel.setParent(this);
        add(footerFormPanel);
    }

    protected void saveTemplate(TemplateResourceObject tpl) {
        GeoServerResourceLoader loader = getCatalog().getResourceLoader();
        String outcome = TemplateManager.saveTemplate(tpl, loader);
        LOGGER.fine(outcome);
    }

    protected void deleteTemplate(TemplateResourceObject tpl) {
        GeoServerResourceLoader loader = getCatalog().getResourceLoader();
        String outcome = "";
        try {
            outcome = TemplateManager.deleteTemplate(tpl, loader, getCharset());
        } catch (IOException e) {
            LOGGER.severe("Could not delete template file");
            LOGGER.severe(e.getMessage());
        }
        LOGGER.fine(outcome);
    }

    public String getCharset() {
        return this.getGeoServer().getSettings().getCharset();
    }

    @Override
    protected ComponentAuthorizer getPageAuthorizer() {
        return ComponentAuthorizer.WORKSPACE_ADMIN;
    }
}
