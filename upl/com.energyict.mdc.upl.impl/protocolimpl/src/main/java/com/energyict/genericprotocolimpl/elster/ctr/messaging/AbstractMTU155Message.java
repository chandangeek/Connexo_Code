package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.elster.ctr.GprsRequestFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocol.MessageEntry;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:28:57
 */
public abstract class AbstractMTU155Message {

    private final GprsRequestFactory factory;
    private final Logger logger;
    private final Rtu rtu;
    private final StoreObject storeObject;

    public abstract boolean canExecuteThisMessage(MessageEntry messageEntry);

    public abstract void executeMessage(MessageEntry messageEntry) throws BusinessException;

    public AbstractMTU155Message(MTU155MessageExecutor messageExecutor) {
        this(messageExecutor.getFactory(), messageExecutor.getLogger(), messageExecutor.getRtu(), messageExecutor.getStoreObject());
    }

    public AbstractMTU155Message(GprsRequestFactory factory, Logger logger, Rtu rtu, StoreObject storeObject) {
        this.factory = factory;
        this.logger = logger == null ? Logger.getLogger(getClass().getName()) : logger;
        this.rtu = rtu;
        this.storeObject = storeObject;
    }

    protected boolean isMessageTag(String tag, String content) {
        return (content.indexOf("<" + tag) >= 0);
    }

    public GprsRequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        return logger;
    }

    public Rtu getRtu() {
        return rtu;
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }

}
