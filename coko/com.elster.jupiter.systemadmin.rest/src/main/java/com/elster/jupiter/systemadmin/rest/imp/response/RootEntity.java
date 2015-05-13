package com.elster.jupiter.systemadmin.rest.imp.response;

public class RootEntity <E>{
    private E data;

    public E getData() {
        return data;
    }

    public RootEntity(E data) {
        this.data = data;
    }
}
