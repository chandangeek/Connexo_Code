package com.elster.jupiter.issue.share.entity;

public interface IssueAssignee {

    long getId();

    String getType();

    String getName();

    long getVersion();

    public static class Types {
        private Types(){}

        public static final String USER = "USER";
        public static final String GROUP = "GROUP";
        public static final String ROLE = "ROLE";

    }
}