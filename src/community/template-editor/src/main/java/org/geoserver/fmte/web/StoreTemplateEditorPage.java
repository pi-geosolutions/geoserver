/* (c) 2014 Open Source Geospatial Foundation - all rights reserved
* This code is licensed under the GPL 2.0 license, available at the root
* application directory.
*/

package org.geoserver.fmte.web;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.PageParameters;
import org.geoserver.fmte.contants.GeoServerConstants;
import org.geoserver.platform.resource.Paths;
import org.geoserver.web.data.store.DataAccessEditPage;

public class StoreTemplateEditorPage extends AbstractTemplateEditorPage {
    private String workspaceName;

    private String storeName = "";

    public StoreTemplateEditorPage() {
        super();
        // TODO Auto-generated constructor stub
    }

    public StoreTemplateEditorPage(PageParameters parameters) {
        super(parameters);
    }

    protected void init(PageParameters parameters) {
        workspaceName = parameters.getString(DataAccessEditPage.WS_NAME);
        storeName = parameters.getString(DataAccessEditPage.STORE_NAME);
        this.resourceType = "store";
    }

    @Override
    public List<String> getResourcePaths(PageParameters parameters) {
        if ((workspaceName == null)) {
            this.init(parameters);
        }
        List<String> pathList = new ArrayList<String>();
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES, workspaceName, storeName));
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES, workspaceName));
        pathList.add(Paths.path(GeoServerConstants.WORKSPACES));
        pathList.add(Paths.path("templates"));
        return pathList;
    }

    @Override
    protected String buildResourceFullName() {
        return workspaceName + ":" + storeName;
    }

}
