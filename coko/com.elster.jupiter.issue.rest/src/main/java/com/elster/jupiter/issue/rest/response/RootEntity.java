package com.elster.jupiter.issue.rest.response;


public class RootEntity <E> {
    E data;

    public E getData() {
        return data;
    }

    public RootEntity(E data) {
        this.data = data;
    }
}
