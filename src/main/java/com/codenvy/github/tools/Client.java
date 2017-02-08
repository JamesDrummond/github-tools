package com.codenvy.github.tools;

import com.codenvy.github.tools.MyComponent;
import com.codenvy.github.tools.ChangeLog;
import com.codenvy.github.tools.GithubIssue;

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
import org.eclipse.egit.github.core.Label;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.PullRequestService;
import org.eclipse.egit.github.core.service.IssueService;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Client {
    public String oauth2Token = "";
    private String milestone;
    private String url = "https://api.github.com";
    private String search = "#### Changelog";
    private String repoOwner;
    private String repoName;
    private String label;
    private List<PullRequest> prs = new ArrayList<PullRequest>();
    private List<ChangeLog> releaseNotes = new ArrayList<ChangeLog>();
    private List<ChangeLog> releaseNotesNotFound = new ArrayList<ChangeLog>();
    private List<GithubIssue> githubIssues = new ArrayList<GithubIssue>();
    private GitHubClient githubclient;
    private URL parsed;
    private IRepositoryIdProvider repository;
    
    public Client(){
        
    }
    
    public boolean init(String repoOwner,String repoName){
        this.repoOwner=repoOwner;
        this.repoName=repoName;
        try {
        parsed = new URL(url);
        githubclient = new GitHubClient(parsed.getHost(), parsed.getPort(), parsed.getProtocol());
        githubclient.setOAuth2Token(oauth2Token);
		RepositoryService rs = new RepositoryService(githubclient);
		repository = rs.getRepository(this.repoOwner, this.repoName);
		} catch(IOException e) {
             System.out.println("Error in GitHub request "+e.getMessage());
             return false;
        }
        return true;
	}

    public boolean initPRmilestone(String milestone){
        this.milestone=milestone;
        if(this.milestone.isEmpty()){
            return false;
        }
        try {
    		List<PullRequest> prsAll = (new PullRequestService(githubclient)).getPullRequests(repository,"closed");
    		Iterator<PullRequest> itr = prsAll.iterator();
    		while(itr.hasNext()) {
                PullRequest pr = itr.next();
                if(pr.getMilestone()!=null){
                    if(pr.getMilestone().getTitle().equals(this.milestone) ){
                        prs.add(pr);
                    }                    
                }
            }
            itr = prs.iterator();
    		while(itr.hasNext()) {
    		    ChangeLog cl = new ChangeLog();
    		    PullRequest pr = itr.next();
                if(pr.getBody()!=null){
                    int index = pr.getBody().indexOf(search);   
                    if( index > 0 ){
                        String changeLog = pr.getBody().substring(index+search.length()+2);//2 is to account for /r/n
                        index = changeLog.indexOf("\r\n");   
                        if( index > 0 ){
                            changeLog = changeLog.substring(0, index);
                        }
                        
                        cl.changelog = changeLog;
                        cl.milestone = pr.getMilestone().getTitle();
                        cl.number = pr.getNumber();
                        releaseNotes.add(cl);
                    }
                }
                if( cl.milestone == null ){
                    cl.milestone = pr.getMilestone().getTitle();
                    cl.number = pr.getNumber();
                    releaseNotesNotFound.add(cl);
                }
		    }
		    
        } catch(IOException e) {
             System.out.println("Error in GitHub request "+e.getMessage());
             return false;
        }
        return true;
    }
    
    public boolean initIssues(String label,String excludeLabel){
        this.label=label;//"severity/P1"
        if(this.label.isEmpty()){
            return false;
        }
        try {
            Map<String, String> dataFilter = new HashMap<String, String>();
            dataFilter.put(IssueService.FILTER_LABELS,this.label);
    		List<Issue> issuesFiltered = (new IssueService(githubclient)).getIssues(repository,dataFilter);
    		Iterator<Issue> itr = issuesFiltered.iterator();
    		while(itr.hasNext()) {
    		    boolean foundExcludeLabel = false;
    		    GithubIssue is = new GithubIssue();
    		    Issue ri = itr.next();
    		    if( ! ri.getLabels().isEmpty() ){
    		        Iterator<Label> itrLabel = ri.getLabels().iterator();
    		        while(itrLabel.hasNext()) {
        		        if(itrLabel.next().getName().equals(excludeLabel) ){   
                            foundExcludeLabel = true;
                        }
                    }
                    if(!foundExcludeLabel){
                        if(ri.getTitle()!=null){
                            is.title = ri.getTitle();
                        }
                        is.labels = ri.getLabels();
                        is.number = ri.getNumber();
                        githubIssues.add(is);
                    }

                }
		    }
		    
        } catch(IOException e) {
             System.out.println("Error in GitHub request "+e.getMessage());
             return false;
        }
        return true;
    }
    
    public String getChangeLog (){
        String changeLogStr="";
        Iterator<ChangeLog> itrRel = releaseNotes.iterator();		    
		while(itrRel.hasNext()) {
		    ChangeLog cl = itrRel.next();
		    changeLogStr=changeLogStr+"- "+cl.changelog+" <a href='https://github.com/"+repoOwner+"/"+repoName+"/pull/"+cl.number+"'>["+cl.number+"]</a>(https://github.com/"+repoOwner+"/"+repoName+"/pull/"+cl.number+")<br/>";
	    }
        return changeLogStr;
    }
    public String getNotFound (){
        String changeLogStr="";
        Iterator<ChangeLog> itrRel = releaseNotesNotFound.iterator();		    
		while(itrRel.hasNext()) {
		    ChangeLog cl = itrRel.next();
		    changeLogStr="<a href='https://github.com/"+repoOwner+"/"+repoName+"/pull/"+cl.number+"'>["+cl.number+"]</a>(https://github.com/"+repoOwner+"/"+repoName+"/pull/"+cl.number+")<br/>";
	    }
        return changeLogStr;
    }
    
    public String getIssues (){
        String issuesStr="";
        Iterator<GithubIssue> itrIs = githubIssues.iterator();		    
		while(itrIs.hasNext()) {
		    GithubIssue is = itrIs.next();
		    issuesStr=issuesStr+"- [ ] "+is.title+" <a href='https://github.com/"+repoOwner+"/"+repoName+"/issues/"+is.number+"'>["+is.number+"]</a>(https://github.com/"+repoOwner+"/"+repoName+"/issues/"+is.number+")<br/>";
	    }
        return issuesStr;
    }
}
