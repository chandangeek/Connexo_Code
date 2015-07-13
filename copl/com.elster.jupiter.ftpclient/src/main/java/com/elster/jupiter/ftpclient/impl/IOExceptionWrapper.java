package com.elster.jupiter.ftpclient.impl;

import java.io.IOException;

class IOExceptionWrapper extends RuntimeException {
    public IOExceptionWrapper(IOException cause) {
        super(cause);
    }

    @Override
    public synchronized IOException getCause() {
        return (IOException) super.getCause();
    }
}
