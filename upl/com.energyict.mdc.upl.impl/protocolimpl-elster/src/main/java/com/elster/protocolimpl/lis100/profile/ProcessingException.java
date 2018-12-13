package com.elster.protocolimpl.lis100.profile;

/**
 * error class for lis100 data processing errors
 *
 * User: heuckeg
 * Date: 25.02.11
 * Time: 10:39
 */
public class ProcessingException extends Throwable {

    public ProcessingException(String s) {
        super(s);
    }
}
