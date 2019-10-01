package com.elster.jupiter.issue.share.entity.values;

import com.elster.jupiter.issue.share.entity.IssueStatus;
import com.elster.jupiter.properties.HasIdAndName;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public final class CloseIssueFormValue extends HasIdAndName {

    private final Optional<Boolean> checkbox;
    private final Optional<IssueStatus> issueStatus;
    private final Optional<String> comment;

    public CloseIssueFormValue(Boolean checkbox, IssueStatus issueStatus, String comment) {
        this.checkbox = checkbox != null ? Optional.of(checkbox) : Optional.of(Boolean.FALSE);
        this.issueStatus = issueStatus != null ? Optional.of(issueStatus) : Optional.empty();
        this.comment = comment != null ? Optional.of(comment) : Optional.empty();
    }

    @Override
    public String getId() {
        try {
            JSONObject jsonId = new JSONObject();
            jsonId.put("checkbox", checkbox.orElse(Boolean.FALSE));
            jsonId.put("issueStatusId", issueStatus.map(IssueStatus::getKey).orElse("status.forwarded"));
            jsonId.put("comment", comment.orElse(null));
            return jsonId.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    @Override
    public String getName() {
        try {
            JSONObject jsonId = new JSONObject();
            jsonId.put("checkbox", checkbox.orElse(Boolean.FALSE));
            jsonId.put("issueStatusName", issueStatus.map(IssueStatus::getName).orElse(""));
            jsonId.put("comment", comment.orElse(""));
            return jsonId.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Optional<Boolean> getCheckbox() {
        return checkbox;
    }

    public Optional<IssueStatus> getIssueStatus() {
        return issueStatus;
    }

    public Optional<String> getComment() {
        return comment;
    }
}
