package org.geoserver.fmte.web;

import static org.geoserver.fmte.web.LayerProvider.NAME;
import static org.geoserver.fmte.web.LayerProvider.STORE;
import static org.geoserver.fmte.web.LayerProvider.TYPE;
import static org.geoserver.fmte.web.LayerProvider.WORKSPACE;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StoreInfo;
import org.geoserver.web.CatalogIconFactory;
import org.geoserver.web.ComponentAuthorizer;
import org.geoserver.web.GeoServerSecuredPage;
import org.geoserver.web.data.SelectionRemovalLink;
import org.geoserver.web.data.resource.ResourceConfigurationPage;
import org.geoserver.web.data.store.DataAccessEditPage;
import org.geoserver.web.wicket.GeoServerDataProvider.Property;
import org.geoserver.web.wicket.GeoServerDialog;
import org.geoserver.web.wicket.GeoServerTablePanel;
import org.geoserver.web.wicket.SimpleBookmarkableLink;

/**
 * Page listing layers, datastores and workspaces. Allows to view/set the associated templates 
 * (FreeMarker templates for getFeatureInfo HTML rendering)
 */
public class TemplatePage extends GeoServerSecuredPage {
    LayerProvider provider = new LayerProvider();
    GeoServerTablePanel<LayerInfo> table;
    GeoServerDialog dialog;
    SelectionRemovalLink removal;

    public TemplatePage() {
        final CatalogIconFactory icons = CatalogIconFactory.get();
        table = new GeoServerTablePanel<LayerInfo>("table", provider, false) {

            @Override
            protected Component getComponentForProperty(String id, IModel itemModel,
                    Property<LayerInfo> property) {
                if(property == TYPE) {
                    Fragment f = new Fragment(id, "iconFragment", TemplatePage.this);
                    f.add(new Image("layerIcon", icons.getSpecificLayerIcon((LayerInfo) itemModel.getObject())));
                    return f;
                } else if(property == WORKSPACE) {
                    return workspaceLink(id, itemModel);
                } else if(property == STORE) {
                    return storeLink(id, itemModel);
                } else if(property == NAME) {
                    return layerLink(id, itemModel);
                } 
                throw new IllegalArgumentException("Don't know a property named " + property.getName());
            }
                        
        };
        table.setOutputMarkupId(true);
        add(table);
    }
    

    private Component layerLink(String id, final IModel model) {
        IModel layerNameModel = NAME.getModel(model);
        String wsName = (String) WORKSPACE.getModel(model).getObject();
        String layerName = (String) layerNameModel.getObject();
        IModel storeModel = STORE.getModel(model);
        String storeName = (String) storeModel.getObject();
        return new SimpleBookmarkableLink(id, LayerTemplateEditorPage.class, layerNameModel, 
                ResourceConfigurationPage.NAME, layerName, 
                DataAccessEditPage.STORE_NAME, storeName,
                ResourceConfigurationPage.WORKSPACE, wsName);
    }

    private Component storeLink(String id, final IModel model) {
        IModel storeModel = STORE.getModel(model);
        String wsName = (String) WORKSPACE.getModel(model).getObject();
        String storeName = (String) storeModel.getObject();
        LayerInfo layer = (LayerInfo) model.getObject();
        StoreInfo store = layer.getResource().getStore();
        return new SimpleBookmarkableLink(id, StoreTemplateEditorPage.class, storeModel, 
                    DataAccessEditPage.STORE_NAME, storeName, 
                    DataAccessEditPage.WS_NAME, wsName);
    }

    private Component workspaceLink(String id, final IModel model) {
        IModel nameModel = WORKSPACE.getModel(model);
        return new SimpleBookmarkableLink(id, WorkspaceTemplateEditorPage.class, nameModel,
                DataAccessEditPage.WS_NAME, (String) nameModel.getObject());
    }
    

    @Override
    protected ComponentAuthorizer getPageAuthorizer() {
        return ComponentAuthorizer.WORKSPACE_ADMIN;
    }
}
