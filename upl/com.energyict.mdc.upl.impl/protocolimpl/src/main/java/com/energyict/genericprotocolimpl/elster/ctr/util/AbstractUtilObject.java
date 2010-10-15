package com.energyict.genericprotocolimpl.elster.ctr.util;

import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 14-okt-2010
 * Time: 16:27:51
 */
public abstract class AbstractUtilObject {

    private final GprsRequestFactory requestFactory;
    private Logger logger;

    public AbstractUtilObject(GprsRequestFactory requestFactory, Logger logger) {
        this.requestFactory = requestFactory;
        this.logger = logger;
    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public Logger getLogger() {
        if (logger == null) {
            this.logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }
}
