package com.codenvy.github.tools;

import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")
@Widgetset("com.codenvy.github.tools.MyComponentWidgetset")
public class MyUI extends UI {
    
    final TextField milestone = new TextField();

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        milestone.setCaption("Type milestone number here");
        //final MyComponent mycomponent = new MyComponent();

        Button button = new Button("Get PR Changelogs");
        button.addClickListener( e -> {
            if(milestone.getValue().length()>0){
                
                layout.addComponent(githubChangelogs("codenvy", "codenvy",this.milestone.getValue()));
                layout.addComponent(githubChangelogs("eclipse", "che",this.milestone.getValue()));
            }
            else{
                milestone.setComponentError(new UserError("Error please enter a milestone."));
            }
            //layout.addComponent(new Label("Thanks " + name.getValue() 
            //        + ", it works!"));
        });
        
        layout.addComponents(milestone, button);        
        layout.setMargin(true);
        layout.setSpacing(true);
        
        setContent(layout);
    }
    
    private Label githubChangelogs(String repoOwner,String repoName,String milestone){
        final Label labelChangeLog = new Label();
        Client client = new Client();
        labelChangeLog.setContentMode(ContentMode.HTML);
        if(client.init(repoOwner,repoName,milestone)){
            String changelogs = "The follow are changelogs for "+repoOwner+":<br/>"+client.getChangeLog()+"<br/>";
            String notfound = "The follow are PRs without changelogs for "+repoOwner+":<br/>"+client.getNotFound()+"<br/>";
            labelChangeLog.setValue(changelogs+notfound);
            labelChangeLog.setImmediate(true);
            labelChangeLog.setSizeFull();
            this.milestone.setCaption("Type milestone number here");
        }
        else{
            this.milestone.setComponentError(new UserError("Error Something went wrong."));
        }
        return labelChangeLog;
    }

    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false,widgetset="com.codenvy.github.tools.MyComponentWidgetset")
    public static class MyUIServlet extends VaadinServlet {
    }
}
