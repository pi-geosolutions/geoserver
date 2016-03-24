package org.geoserver.fmte.web;

import org.geoserver.web.ComponentInfo;
import org.geoserver.web.GeoServerSecuredPage;

/**
 * Extension hook for template editor pages (should not show in menu)
 * @author Jean Pommier <jean.pommier@pi-geosolutions.fr>
 */
@SuppressWarnings("serial")
public class TemplateEditorPageInfo extends ComponentInfo<GeoServerSecuredPage> {
    // inherit everything from ComponentInfo
}