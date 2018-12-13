/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.issue.rest.response.issue.IssueShortInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ActionInfo {
    private final List<IssueShortInfo> success;
    private final Map<String, ActionFailInfo> fails;

    public ActionInfo(){
        success = new ArrayList<>();
        fails = new HashMap<>();
    }

    public void addSuccess(long id){
        success.add(new IssueShortInfo(id));
    }

    public void addSuccess(long id, String title){
        success.add(new IssueShortInfo(id, title));
    }

    public void addFail(String reason, long id, String title){
        if (reason != null) {
            ActionFailInfo failsWithSameReason = fails.get(reason);
            if (failsWithSameReason == null) {
                failsWithSameReason = new ActionFailInfo();
                failsWithSameReason.setReason(reason);
                fails.put(reason, failsWithSameReason);
            }
            failsWithSameReason.add(new IssueShortInfo(id, title));
        }
    }

    public List<IssueShortInfo> getSuccess() {
        return Collections.unmodifiableList(success);
    }

    public List<ActionFailInfo> getFailure() {
        return new ArrayList<>(fails.values());
    }

}