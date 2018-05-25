package com.elster.jupiter.hsm.model;

import com.atos.worldline.jss.api.FunctionFailedException;

public class EncryptBaseException extends Exception {
    public EncryptBaseException(FunctionFailedException e) {
        super(e);
    }

    public EncryptBaseException(String msg) { super(msg);  }
}
