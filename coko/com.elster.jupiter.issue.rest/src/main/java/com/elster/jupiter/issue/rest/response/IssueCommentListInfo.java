package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.share.entity.IssueComment;

import java.util.ArrayList;
import java.util.List;

public class IssueCommentListInfo {
    private List<IssueCommentInfo> data;

    public IssueCommentListInfo() {
        this.data = new ArrayList<>();
    }

    public IssueCommentListInfo(List<IssueComment> list) {
        this();
        for (IssueComment comment : list) {
            data.add(new IssueCommentInfo(comment));
        }

    }

    public List<IssueCommentInfo> getData() {
        return data;
    }

    public void setData(List<IssueCommentInfo> comments) {
        this.data = comments;
    }
}
