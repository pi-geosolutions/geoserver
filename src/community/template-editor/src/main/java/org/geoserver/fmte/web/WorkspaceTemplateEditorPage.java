package org.geoserver.fmte.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.geoserver.fmte.contants.GeoServerConstants;
import org.geoserver.platform.resource.Paths;
import org.geoserver.web.data.store.DataAccessEditPage;

public class WorkspaceTemplateEditorPage extends AbstractTemplateEditorPage {
    private String workspaceName;

    public WorkspaceTemplateEditorPage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public WorkspaceTemplateEditorPage(PageParameters parameters) {
        super(parameters);
        this.resourceType = "Workspace";
    }

    protected void init(PageParameters parameters) {
        workspaceName = parameters.getString(DataAccessEditPage.WS_NAME);
    }

    @Override
    public List<String> getResourcePaths(PageParameters parameters) {
        if ((workspaceName == null)) {
            this.init(parameters);
        }
        List<String> pathList = new ArrayList<String>();
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES, workspaceName));
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES));
        pathList.add(Paths.path("templates"));
        return pathList;
    }

    @Override
    protected String buildResourceFullName() {
        return workspaceName;
    }

}
