package com.elster.jupiter.issue.share;

import java.util.ArrayList;
import java.util.List;

import aQute.bnd.annotation.ConsumerType;
import aQute.bnd.annotation.ProviderType;

@ConsumerType
public interface IssueActionResult {
    
    boolean isSuccess();

    @ProviderType
    class DefaultActionResult implements IssueActionResult {
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

            public boolean isSuccess() {
                return success;
            }
        }

        private List<Action> actions;
        private Action current;

        public DefaultActionResult() {
            actions = new ArrayList<>();
            nextAction();
        }

        public List<Action> getActions() {
            return actions;
        }

        public void nextAction(String title) {
            current = new Action();
            current.title = title;
            actions.add(current);
        }

        public void nextAction() {
            this.nextAction(null);
        }

        public void success() {
            this.success(null);
        }

        public void success(String msg) {
            current.success = true;
            current.message = msg;
        }

        public void fail() {
            this.fail(null);
        }

        public void fail(String msg) {
            current.success = false;
            current.message = msg;
        }
        
        @Override
        public boolean isSuccess() {
            return actions.stream().allMatch(Action::isSuccess);
        }
    }
}
