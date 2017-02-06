package com.codenvy.github.tools;

import com.vaadin.ui.AbstractComponent;
import com.codenvy.github.tools.client.MyComponentServerRpc;
import com.vaadin.ui.Notification;
import com.vaadin.shared.MouseEventDetails;

public class MyComponent extends AbstractComponent {
        private int clickCount = 0;
        public MyComponent() {
            MyComponentServerRpc rpc =
            new MyComponentServerRpc() {
                public void clicked(MouseEventDetails mouseDetails) {
                    (new MyComponent()).notify(mouseDetails);
                }
            };
            
            registerRpc(rpc);
        }
        public void notify(MouseEventDetails mouseDetails){
            Notification.show("Clicked " + clickCount++  +" " + mouseDetails.getButtonName());
        };

}