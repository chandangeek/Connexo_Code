/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.rest;

import java.util.ArrayList;
import java.util.List;

public class LocalizedFieldException {

    public boolean success;
    public List<Errors> errors = new ArrayList<>();
    public LocalizedFieldException(){
    }

    public LocalizedFieldException(List<Errors> errors){
        this.errors = errors;
    }

}
