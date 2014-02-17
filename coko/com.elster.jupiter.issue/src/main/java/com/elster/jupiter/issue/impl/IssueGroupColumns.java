package com.elster.jupiter.issue.impl;

public enum IssueGroupColumns {
    REASON{
        @Override
        public String getDisplayName() {
            return "Reason";
        }
    };

    public abstract String getDisplayName();

    String getInternalName(){
        return toString();
    }

    public static IssueGroupColumns fromString(String text) {
        if (text != null) {
            for (IssueGroupColumns column : IssueGroupColumns.values()) {
                if (column.name().equalsIgnoreCase(text)) {
                    return column;
                }
            }
        }
        return null;
    }
}
