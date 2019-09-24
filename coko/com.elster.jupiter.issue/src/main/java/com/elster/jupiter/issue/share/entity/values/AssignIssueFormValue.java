package com.elster.jupiter.issue.share.entity.values;

import com.elster.jupiter.properties.HasIdAndName;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.WorkGroup;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Optional;

public final class AssignIssueFormValue extends HasIdAndName {

    private final Optional<Boolean> checkbox;
    private final Optional<User> user;
    private final Optional<WorkGroup> workgroup;
    private final Optional<String> comment;

    public AssignIssueFormValue(final Boolean checkbox, final User user, final WorkGroup workgroup, final String comment) {
        this.checkbox = Optional.of(checkbox);
        this.user = user != null ? Optional.of(user) : Optional.empty();
        this.workgroup = workgroup != null ? Optional.of(workgroup) : Optional.empty();
        this.comment = comment != null ? Optional.of(comment) : Optional.empty();
    }

    @Override
    public String getId() {
        try {
            JSONObject jsonId = new JSONObject();
            jsonId.put("checkbox", checkbox.orElse(Boolean.FALSE));
            jsonId.put("userId", user.map(User::getId).orElse(-1L));
            jsonId.put("workgroupId", workgroup.map(WorkGroup::getId).orElse(-1L));
            jsonId.put("comment", comment.orElse(null));
            return jsonId.toString();
        } catch (JSONException e) {
            return "";
        }
    }

    @Override
    public String getName() {
        try {
            JSONObject jsonName = new JSONObject();
            jsonName.put("checkbox", checkbox.orElse(Boolean.FALSE));
            jsonName.put("userName", user.map(User::getName).orElse(""));
            jsonName.put("workgroupName", workgroup.map(WorkGroup::getName).orElse(""));
            jsonName.put("comment", comment.orElse(""));
            return jsonName.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public Optional<Boolean> getCheckbox() {
        return checkbox;
    }

    public Optional<User> getUser() {
        return user;
    }

    public Optional<WorkGroup> getWorkgroup() {
        return workgroup;
    }

    public Optional<String> getComment() {
        return comment;
    }
}
