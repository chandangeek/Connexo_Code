package com.elster.genericprotocolimpl.dlms.ek280.executors;

import com.elster.genericprotocolimpl.dlms.ek280.EK280;
import com.elster.genericprotocolimpl.dlms.ek280.StoreObject;
import com.elster.genericprotocolimpl.dlms.ek280.journal.MeterAmrLogging;
import com.elster.protocolimpl.dlms.Dlms;
import com.energyict.mdw.core.Rtu;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Copyrights
 * Date: 9/06/11
 * Time: 8:55
 */
public abstract class AbstractExecutor<T> {

    private final EK280 ek280;
    private MeterAmrLogging meterAmrLogging;

    public abstract void execute(T objectToExecute) throws IOException;

    public AbstractExecutor(AbstractExecutor executor) {
        this.ek280 = executor.getEk280();
        this.meterAmrLogging = executor.getMeterAmrLogging();
    }

    public AbstractExecutor(EK280 ek280) {
        this.ek280 = ek280;
        this.meterAmrLogging = null;
    }

    public Logger getLogger() {
        return getEk280().getLogger();
    }

    public Rtu getRtu() {
        return getEk280().getRtu();
    }

    public EK280 getEk280() {
        return ek280;
    }

    public StoreObject getStoreObject() {
        return getEk280().getStoreObject();
    }

    public Dlms getDlmsProtocol() {
        return getEk280().getDlmsProtocol();
    }

    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
    }

    public void severe(String message) {
        getLogger().severe(message);
        getMeterAmrLogging().logInfo(message);
    }

    public void severe(Exception e) {
        severe(e.getMessage());
    }

    public void info(String message) {
        getLogger().info(message);
        getMeterAmrLogging().logInfo(message);
    }

    public void info(Exception e) {
        info(e.getMessage());
    }

}
