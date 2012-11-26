package com.energyict.genericprotocolimpl.elster.ctr.messaging;

import com.energyict.cbo.BusinessException;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.genericprotocolimpl.elster.ctr.RequestFactory;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRFirmwareUpgradeTimeOutException;
import com.energyict.mdw.core.Device;
import com.energyict.protocol.MessageEntry;

import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 27-dec-2010
 * Time: 9:28:57
 */
public abstract class AbstractMTU155Message {

    private final RequestFactory factory;
    private final Logger logger;
    private final Device rtu;
    private final StoreObject storeObject;

    public abstract boolean canExecuteThisMessage(MessageEntry messageEntry);

    public abstract void executeMessage(MessageEntry messageEntry) throws BusinessException, CTRFirmwareUpgradeTimeOutException;

    public AbstractMTU155Message(MTU155MessageExecutor messageExecutor) {
        this(messageExecutor.getFactory(), messageExecutor.getLogger(), messageExecutor.getRtu(), messageExecutor.getStoreObject());
    }

    public AbstractMTU155Message(RequestFactory factory, Logger logger, Device rtu, StoreObject storeObject) {
        this.factory = factory;
        this.logger = logger == null ? Logger.getLogger(getClass().getName()) : logger;
        this.rtu = rtu;
        this.storeObject = storeObject;
    }

    protected boolean isMessageTag(String tag, String content) {
        return (content.indexOf("<" + tag) >= 0);
    }

    public RequestFactory getFactory() {
        return factory;
    }

    public Logger getLogger() {
        return logger;
    }

    public Device getRtu() {
        return rtu;
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }

}
