package com.codenvy.github.tools;

import com.codenvy.github.tools.Client;

import javax.servlet.annotation.WebServlet;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.annotations.Widgetset;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.UserError;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.shared.ui.ContentMode;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.RepositoryService; 
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.RepositoryIssue;
import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.IssueService;

/**
 * This UI is the application entry point. A UI may either represent a browser window 
 * (or tab) or some part of a html page where a Vaadin application is embedded.
 * <p>
 * The UI is initialized using {@link #init(VaadinRequest)}. This method is intended to be 
 * overridden to add component to the user interface and initialize non-component functionality.
 */
@Theme("mytheme")  
public class MyUI extends UI {
    
    final TextField milestone = new TextField();
    final TextField label = new TextField();
    final TextField excludeLabel = new TextField();
    final TextField oauth2Token = new TextField();
    private Client client;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        client = new Client();
        final VerticalLayout layout = new VerticalLayout();
        oauth2Token.setCaption("Enter required oauth token here");
        milestone.setCaption("Type milestone number here");
        label.setCaption("Type label to filter here");
        excludeLabel.setCaption("Type exclude label filter here");
        //final MyComponent mycomponent = new MyComponent();
        Button buttonPR = new Button("Get PR Changelogs");
        Button buttonLabel = new Button("Get Filtered Issues");
        buttonPR.addClickListener( e -> {
            if(oauth2Token.getValue().length()>0){
                String oauth2TokenStr = oauth2Token.getValue();
                if(milestone.getValue().length()>0){
                    layout.addComponent(githubChangelogs(oauth2TokenStr,"codenvy", "codenvy",this.milestone.getValue()));
                    layout.addComponent(githubChangelogs(oauth2TokenStr,"eclipse", "che",this.milestone.getValue()));
                    layout.addComponent(githubChangelogs(oauth2TokenStr,"eclipse", "che-dockerfiles",this.milestone.getValue()));
                }
                else{
                    milestone.setComponentError(new UserError("Error please enter a milestone."));
                }
            }
            else{
                oauth2Token.setComponentError(new UserError("Error please enter a required oauth token."));
            }

        });
        
    
        buttonLabel.addClickListener( e -> {
            
            if(oauth2Token.getValue().length()>0){
                String oauth2TokenStr = oauth2Token.getValue();
                if(label.getValue().length()>0 && excludeLabel.getValue().length()>0){
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"codenvy", "codenvy",this.label.getValue(),this.excludeLabel.getValue()));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"codenvy", "infrastructure",this.label.getValue(),this.excludeLabel.getValue()));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"codenvy", "enterprise",this.label.getValue(),this.excludeLabel.getValue()));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"eclipse", "che",this.label.getValue(),this.excludeLabel.getValue()));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"eclipse", "che-dockerfiles",this.label.getValue(),this.excludeLabel.getValue()));
                }
                else if(label.getValue().length()>0){
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"codenvy", "codenvy",this.label.getValue(),""));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"codenvy", "infrastructure",this.label.getValue(),this.excludeLabel.getValue()));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"codenvy", "enterprise",this.label.getValue(),this.excludeLabel.getValue()));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"eclipse", "che",this.label.getValue(),""));
                    layout.addComponent(githubIssuesWithLabel(oauth2TokenStr,"eclipse", "che-dockerfiles",this.label.getValue(),""));
                }
                else{
                    label.setComponentError(new UserError("Error please enter a label to filter with."));
                }
            }
            else{
                oauth2Token.setComponentError(new UserError("Error please enter a required oauth token."));
            }
        });
        layout.addComponents(oauth2Token,milestone,label,excludeLabel,buttonPR,buttonLabel);        
        layout.setMargin(true);
        layout.setSpacing(true);
        
        setContent(layout);
    }
    
    private Label githubChangelogs(String oauth2TokenStr,String repoOwner,String repoName,String milestone){
        final Label labelChangeLog = new Label();
        client.oauth2Token=oauth2TokenStr;
        labelChangeLog.setContentMode(ContentMode.HTML);
        if(client.init(repoOwner,repoName)){
            if(client.initPRmilestone(milestone)){
                String changelogs = "The follow are changelogs for "+repoOwner+"/"+repoName+":<br/>"+client.getChangeLog()+"<br/>";
                String notfound = "The follow are PRs without changelogs for "+repoOwner+"/"+repoName+":<br/>"+client.getNotFound()+"<br/>";
                labelChangeLog.setValue(changelogs+notfound);
                labelChangeLog.setSizeFull();
                this.milestone.setCaption("Type milestone number here");
            }
            else{
                this.milestone.setComponentError(new UserError("Error Something went wrong."));
            }
        }

        
        return labelChangeLog;
    }
    
    private Label githubIssuesWithLabel(String oauth2TokenStr,String repoOwner,String repoName,String labelStr,String excludeLabelStr){
        final Label labelChangeLog = new Label();
        Client client = new Client();
        client.oauth2Token=oauth2TokenStr;
        labelChangeLog.setContentMode(ContentMode.HTML);
        if(client.init(repoOwner,repoName)){
            if(client.initIssues(labelStr,excludeLabelStr)){
                String changelogs = "The follow are issues for "+repoOwner+":<br/>"+client.getIssues()+"<br/>";
                labelChangeLog.setValue(changelogs);
                labelChangeLog.setSizeFull();
                this.label.setCaption("Type label to filter here");
                this.excludeLabel.setCaption("Type exclude label filter here");
            }
            else{
                this.label.setComponentError(new UserError("Error Something went wrong."));
            }
        }
        
        return labelChangeLog;
    }
    
    @WebServlet(urlPatterns = "/*", name = "MyUIServlet", asyncSupported = true)
    @VaadinServletConfiguration(ui = MyUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }

}
