package com.elster.jupiter.appserver;

import com.elster.jupiter.util.exception.BaseException;

import java.text.MessageFormat;

public class UnknownAppServerNameException extends BaseException {

    public UnknownAppServerNameException(String appServerName) {
        super(ExceptionTypes.UNKOWN_APPSERVER_NAME, MessageFormat.format("AppServer with name ''{0}'' is unknown", appServerName));
        set("appSerevrName", appServerName);
    }
}
