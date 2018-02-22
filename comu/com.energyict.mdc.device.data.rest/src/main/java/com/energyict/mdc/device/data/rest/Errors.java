/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

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
