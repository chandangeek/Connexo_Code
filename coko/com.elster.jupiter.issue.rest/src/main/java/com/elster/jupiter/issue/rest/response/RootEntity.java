package com.elster.jupiter.issue.rest.response;

@Deprecated
public class RootEntity <E> {
    private E data;

    public E getData() {
        return data;
    }

    public RootEntity(E data) {
        this.data = data;
    }
}
