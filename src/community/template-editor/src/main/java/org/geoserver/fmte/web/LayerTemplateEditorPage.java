package org.geoserver.fmte.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.fmte.contants.GeoServerConstants;
import org.geoserver.platform.resource.Paths;
import org.geoserver.web.data.resource.ResourceConfigurationPage;
import org.geoserver.web.data.store.DataAccessEditPage;
import org.geotools.feature.NameImpl;

public class LayerTemplateEditorPage extends AbstractTemplateEditorPage {
    private String workspaceName;
    private String storeName = "";
    private String layerName = "";

    public LayerTemplateEditorPage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public LayerTemplateEditorPage(PageParameters parameters) {
        super(parameters);
        this.resourceType = "Layer";
    }

    protected void init(PageParameters parameters) {
        workspaceName = parameters.getString(ResourceConfigurationPage.WORKSPACE);
        storeName = parameters.getString(DataAccessEditPage.STORE_NAME);
        layerName = parameters.getString(ResourceConfigurationPage.NAME);
    }

    @Override
    public List<String> getResourcePaths(PageParameters parameters) {
        if ((workspaceName == null)) {
            this.init(parameters);
        }
        List<String> pathList = new ArrayList<String>();
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES, workspaceName, storeName,layerName));
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES, workspaceName, storeName));
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES, workspaceName));
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES));
        pathList.add(Paths.path("templates"));
        return pathList;
    }

    @Override
    protected String buildResourceFullName() {
        return workspaceName + ":" + layerName;
    }
}
