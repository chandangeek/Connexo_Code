package com.elster.jupiter.issue.rest.response;

public class IssueShortInfo {
    private long id;
    private String title;

    public IssueShortInfo(long id) {
        this.id = id;
    }

    public IssueShortInfo(long id, String title) {
        this.id = id;
        this.title = title;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
