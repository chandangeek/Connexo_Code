package com.elster.jupiter.appserver;

import java.text.MessageFormat;

public class UnknownAppServerNameException extends RuntimeException {

    public UnknownAppServerNameException(String appServerName) {
        super(MessageFormat.format("AppServer with name ''{0}'' is unknown", appServerName));
    }
}
