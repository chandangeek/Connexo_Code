package com.energyict.genericprotocolimpl.elster.ctr;

import org.junit.Test;

import java.io.*;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 17:29:49
 */
public abstract class CtrTest {

    private Logger logger = null;
    private ByteArrayOutputStream nullOut = null;

    protected Logger getLogger() {
        if (logger == null) {
            Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    protected OutputStream getNullOut() {
        if (nullOut == null) {
            nullOut = new ByteArrayOutputStream();
        }
        return nullOut;
    }

    protected GprsRequestFactory getDummyRequestFactory(byte[] rawResponse) {
        ByteArrayInputStream in = new ByteArrayInputStream(rawResponse);
        return new GprsRequestFactory(in, getNullOut(), getLogger(), new MTU155Properties());
    }

}
