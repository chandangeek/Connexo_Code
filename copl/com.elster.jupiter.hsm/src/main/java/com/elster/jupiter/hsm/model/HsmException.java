package com.elster.jupiter.hsm.model;

import com.atos.worldline.jss.api.FunctionFailedException;

public class HsmException extends Exception {
    public HsmException(FunctionFailedException e) {
        super(e);
    }
}
