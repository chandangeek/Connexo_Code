package com.elster.jupiter.issue.share.entity;


public interface IssueStatus extends Entity {

    String getName();

    void setName(String name);

    boolean isFinal();

    void setFinal(boolean isFinal);
}
