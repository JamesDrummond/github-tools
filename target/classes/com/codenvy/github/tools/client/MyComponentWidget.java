package com.codenvy.github.tools.client;

import com.google.gwt.user.client.ui.Button;

public class MyComponentWidget extends Button {

    public static final String CLASSNAME = "mycomponent";

    public MyComponentWidget() {
        setText("This is MyComponent");
        setStyleName(CLASSNAME);
    }
}