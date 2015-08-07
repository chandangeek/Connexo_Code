package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.util.Checks;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class IssueAssigneeInfoAdapter extends XmlAdapter<String, IssueAssigneeInfo> {
    @Override
    public IssueAssigneeInfo unmarshal(String jsonValue) throws Exception {
        IssueAssigneeInfo empty =  new IssueAssigneeInfo(null, 0L, null);
        if (Checks.is(jsonValue).emptyOrOnlyWhiteSpace()) {
            return empty;
        }
        String[] values = jsonValue.split(":");  // "userId:userType" is the string format received from front-end
        return values.length < 2 ? empty : new IssueAssigneeInfo(values[1], Long.parseLong(values[0]), null);
    }

    @Override
    public String marshal(IssueAssigneeInfo assignee) throws Exception {
        if (assignee == null) {
            return null;
        }
        return assignee.getId() + ":" + assignee.getType();
    }
}
