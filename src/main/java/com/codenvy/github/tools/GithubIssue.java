package com.codenvy.github.tools;
import org.eclipse.egit.github.core.Label;
import java.util.List;

public class GithubIssue {
    public String title;
    public int number; //Issue number
    public String milestone; //Keep this if prs are issues
    public List<Label> labels;
}
