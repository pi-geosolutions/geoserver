package org.geoserver.template.editor;

import org.apache.wicket.markup.html.basic.Label;
import org.geoserver.web.GeoServerBasePage;

public class TemplateEditorPage extends GeoServerBasePage {
    public TemplateEditorPage() {
        add(new Label("label", "Hello World!"));
    }
}
