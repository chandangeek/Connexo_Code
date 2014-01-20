package com.elster.jupiter.appserver;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.exception.BaseException;

public class UnknownAppServerNameException extends BaseException {

    public UnknownAppServerNameException(String appServerName, Thesaurus thesaurus) {
        super(ExceptionTypes.UNKOWN_APPSERVER_NAME, buildMessage(appServerName, thesaurus));
        set("appServerName", appServerName);
    }

    private static String buildMessage(String appServerName, Thesaurus thesaurus) {
        return thesaurus.getFormat(MessageSeeds.APPSERVER_NAME_UNKNOWN).format(appServerName);
    }

}
