package com.elster.jupiter.hsm.model;

import com.atos.worldline.jss.api.FunctionFailedException;
import com.atos.worldline.jss.api.key.UnsupportedKEKEncryptionMethodException;

public class EncryptBaseException extends Exception {
    public EncryptBaseException(Throwable e) {
        super(e);
    }

    public EncryptBaseException(String msg) { super(msg);  }

    public EncryptBaseException(String s, Throwable e) { super(s, e);
    }
}
