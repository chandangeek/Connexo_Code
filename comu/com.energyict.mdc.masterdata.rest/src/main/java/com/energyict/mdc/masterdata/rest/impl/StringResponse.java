package com.energyict.mdc.masterdata.rest.impl;

/**
 * String response wrapper class.
 */
public class StringResponse {

    private final String response;

    StringResponse(String value) {
        this.response = value;
    }

    public String getResponse(){
        return response;
    }
}
