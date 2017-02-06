package com.codenvy.github.tools.client;

import com.codenvy.github.tools.MyComponent;
import com.codenvy.github.tools.client.MyComponentServerRpc;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Widget;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.vaadin.shared.MouseEventDetails;
import com.vaadin.client.MouseEventDetailsBuilder;

@Connect(com.codenvy.github.tools.MyComponent.class)
public class MyComponentConnector extends AbstractComponentConnector {
    @Override
    protected Widget createWidget() {
        return GWT.create(MyComponentWidget.class);
    }
    //optional
    @Override
    public MyComponentWidget getWidget() {
        return (MyComponentWidget) super.getWidget();
    }
    
    public MyComponentConnector() {
        getWidget().addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                final MouseEventDetails mouseDetails =
                     MouseEventDetailsBuilder
                         .buildMouseEventDetails(
                                 event.getNativeEvent(),
                                 getWidget().getElement());
                MyComponentServerRpc rpc =
                    getRpcProxy(MyComponentServerRpc.class);

                // Make the call
                rpc.clicked(mouseDetails);
            }
        });
    }
}