package com.elster.jupiter.issue.share.entity;

import aQute.bnd.annotation.ProviderType;

@ProviderType
public interface IssueAssignee {

    long getId();
    String getType();
    String getName();
    long getVersion();

    public static class Types {
        private Types(){}

        public static final String USER = "USER";
    }
}