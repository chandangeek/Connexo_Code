/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.exception;


import com.elster.jupiter.bpm.rest.Errors;

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
