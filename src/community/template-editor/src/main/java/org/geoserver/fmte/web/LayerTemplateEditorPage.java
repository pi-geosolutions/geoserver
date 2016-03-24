package org.geoserver.fmte.web;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.geoserver.catalog.AttributeTypeInfo;
import org.geoserver.catalog.CoverageDimensionInfo;
import org.geoserver.catalog.CoverageInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.platform.GeoServerResourceLoader;
import org.geoserver.web.ComponentAuthorizer;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.data.resource.ResourceConfigurationPage;
import org.geotools.feature.NameImpl;
import org.geotools.util.logging.Logging;

/**
 * Base page for creating/editing templates
 */
@SuppressWarnings("serial")
public class LayerTemplateEditorPage extends GeoServerSecuredPage {
    private static final Logger LOGGER = Logging.getLogger(LayerTemplateEditorPage.class);

    private String name = "";

    private String wsName = "";

    private TemplateResourceObject tpl_header, tpl_content, tpl_footer;

    public LayerTemplateEditorPage() {
    }

    public LayerTemplateEditorPage(PageParameters parameters) {
        String workspaceName = parameters.getString(ResourceConfigurationPage.WORKSPACE);
        String layerName = parameters.getString(ResourceConfigurationPage.NAME);

        this.name = layerName;
        this.wsName = workspaceName;

        // setDefaultModel(new CompoundPropertyModel(this));

        add(new Label("name", Model.of(this.name)));
        add(new Label("wsName", Model.of(this.wsName)));

        ResourceInfo resource = getResource(layerName, workspaceName);

        RepeatingView attributesList = new RepeatingView("attributesList");
        if (resource instanceof FeatureTypeInfo) {
            FeatureTypeInfo featureTypeInfo = (FeatureTypeInfo) resource;
            Iterator<AttributeTypeInfo> it = featureTypeInfo.getAttributes().iterator();
            while (it.hasNext()) {
                AttributeTypeInfo info = it.next();
                attributesList.add(new Label(attributesList.newChildId(), info.getName()));
            }
        } else if (resource instanceof CoverageInfo) {
            CoverageInfo coverageInfo = (CoverageInfo) resource;
            Iterator<CoverageDimensionInfo> it = coverageInfo.getDimensions().iterator();
            while (it.hasNext()) {
                CoverageDimensionInfo info = it.next();
                attributesList.add(new Label(attributesList.newChildId(), info.getName()));
            }
        }

        add(attributesList);

        try {
            readTemplates(resource);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.info("Oops, error retrieving template files");
        }

        TemplateFormPanel headerFormPanel = new TemplateFormPanel("headerTplPanel",
                new CompoundPropertyModel(tpl_header));
        headerFormPanel.setParent(this);
        add(headerFormPanel);

        TemplateFormPanel contentFormPanel = new TemplateFormPanel("contentTplPanel",
                new CompoundPropertyModel(tpl_content));
        contentFormPanel.setParent(this);
        add(contentFormPanel);

        TemplateFormPanel footerFormPanel = new TemplateFormPanel("footerTplPanel",
                new CompoundPropertyModel(tpl_footer));
        footerFormPanel.setParent(this);
        add(footerFormPanel);
    }

    private void readTemplates(ResourceInfo resource) throws IOException {
        tpl_header = TemplateManager.readTemplate("header.ftl", resource, getCharset());
        tpl_content = TemplateManager.readTemplate("content.ftl", resource, getCharset());
        tpl_footer = TemplateManager.readTemplate("footer.ftl", resource, getCharset());
    }

    protected void saveTemplate(TemplateResourceObject tpl) {
        GeoServerResourceLoader loader = getCatalog().getResourceLoader();
        String outcome = TemplateManager.saveTemplate(tpl, loader);
        LOGGER.fine(outcome);
    }

    protected void deleteTemplate(TemplateResourceObject tpl) {
        GeoServerResourceLoader loader = getCatalog().getResourceLoader();
        ResourceInfo resource = getResource(this.name, this.wsName);
        String outcome = "";
        try {
            outcome = TemplateManager.deleteTemplate(tpl, loader, resource, getCharset());
        } catch (IOException e) {
            LOGGER.severe("Could not delete template file");
            LOGGER.severe(e.getMessage());
        }
        LOGGER.fine(outcome);
    }

    public String getCharset() {
        return this.getGeoServer().getSettings().getCharset();
    }

    public ResourceInfo getResource(String layerName, String workspaceName) {
        LOGGER.info("[getResource] layername=" + layerName + " , workspacename=" + workspaceName);
        LayerInfo layer;
        if (workspaceName != null) {
            NamespaceInfo ns = getCatalog().getNamespaceByPrefix(workspaceName);
            if (ns == null) {
                // unlikely to happen, requires someone making modifications on the workspaces
                // with a layer page open in another tab/window
                throw new RuntimeException("Could not find workspace " + workspaceName);
            }
            String nsURI = ns.getURI();
            layer = getCatalog().getLayerByName(new NameImpl(nsURI, layerName));
        } else {
            layer = getCatalog().getLayerByName(layerName);
        }

        if (layer == null) {
            /*
             * error(new ParamResourceModel("ResourceConfigurationPage.notFound", this, layerName) .getString());
             */
            setResponsePage(returnPage);
            return null;
        }

        LOGGER.fine("successfully loaded layer " + name + ", type " + layer.getType().toString());
        ResourceInfo resource = layer.getResource();
        return resource;
    }

    @Override
    protected ComponentAuthorizer getPageAuthorizer() {
        return ComponentAuthorizer.WORKSPACE_ADMIN;
    }
}
