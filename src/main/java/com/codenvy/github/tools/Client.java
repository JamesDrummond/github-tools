package com.codenvy.github.tools;

import com.codenvy.github.tools.MyComponent;
import com.codenvy.github.tools.ChangeLog;

import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.GitHubRequest;
import org.eclipse.egit.github.core.client.GitHubResponse;
import org.eclipse.egit.github.core.service.RepositoryService; 
import org.eclipse.egit.github.core.IRepositoryIdProvider;
import org.eclipse.egit.github.core.PullRequest;
import org.eclipse.egit.github.core.Repository;
import org.eclipse.egit.github.core.SearchIssue;
import org.eclipse.egit.github.core.Milestone;
import org.eclipse.egit.github.core.SearchRepository;
import org.eclipse.egit.github.core.service.PullRequestService;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Iterator;
import java.util.ArrayList;

public class Client {
    final private String oauth2Token = "4f80e1681fd277c8b63f938cf7f13bde2d6959bf";
    private String milestone;
    private String url = "https://api.github.com";
    private String search = "#### Changelog";
    private String repoOwner;
    private String repoName;
    private List<PullRequest> prs = new ArrayList<PullRequest>();
    private List<ChangeLog> releaseNotes = new ArrayList<ChangeLog>();
    private List<ChangeLog> releaseNotesNotFound = new ArrayList<ChangeLog>();
    
    
    public Client(){
        
    }
    public boolean init(String repoOwner,String repoName,String milestone){
        this.repoOwner=repoOwner;
        this.repoName=repoName;
        this.milestone=milestone;
        if(this.milestone.isEmpty()){
            return false;
        }
        try {
            URL parsed = new URL(url);
            SearchIssue issue = new SearchIssue();
            GitHubClient githubclient = new GitHubClient(parsed.getHost(), parsed.getPort(), parsed.getProtocol());
            githubclient.setOAuth2Token(oauth2Token);
    		RepositoryService rs = new RepositoryService(githubclient);
    		IRepositoryIdProvider repository = rs.getRepository(this.repoOwner, this.repoName);
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
}
