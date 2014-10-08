package com.elster.jupiter.issue.share.cep.controls;

import com.elster.jupiter.issue.share.cep.IssueActionResult;

import java.util.ArrayList;
import java.util.List;

public class DefaultActionResult implements IssueActionResult {
    public static class Action {
        private String title;
        private String message;
        private boolean success;

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public boolean wasSuccessed() {
            return success;
        }
    }

    private List<Action> actions;
    private Action current;

    public DefaultActionResult() {
        actions = new ArrayList<>();
        nextAction();
    }

    public void nextAction(String title){
        current = new Action();
        current.title = title;
        actions.add(current);
    }

    public void nextAction(){
        this.nextAction(null);
    }

    public void success(){
        this.success(null);
    }

    public void success(String msg){
        current.success = true;
        current.message = msg;
    }

    public void fail(){
        this.fail(null);
    }

    public void fail(String msg){
        current.success = false;
        current.message = msg;
    }
}
