package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.rest.impl.MessageSeeds;

public class Errors{
    public String id;
    public String msg;
    public Errors(){

    }

    public Errors(String id, String message){
        this.id = id;
        this.msg = message;
    }
}