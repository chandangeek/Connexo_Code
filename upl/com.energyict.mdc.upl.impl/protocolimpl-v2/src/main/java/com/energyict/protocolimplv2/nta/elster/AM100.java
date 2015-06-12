package com.energyict.protocolimplv2.nta.elster;

import com.energyict.cbo.ConfigurationSupport;
import com.energyict.dlms.DLMSCache;
import com.energyict.mdc.channels.serial.modem.rxtx.RxTxAtModemConnectionType;
import com.energyict.mdc.channels.serial.modem.serialio.SioAtModemConnectionType;
import com.energyict.mdc.tasks.ConnectionType;
import com.energyict.protocolimplv2.nta.dsmr23.eict.WebRTUKP;

import java.util.List;
import java.util.logging.Level;

/**
 * The AM100 implementation of the NTA spec
 *
 * @author sva
 * @since 30/10/12 (9:58)
 */
public class AM100 extends WebRTUKP {

    private AM100DlmsProperties dlmsProperties;

    @Override
    protected void checkCacheObjects() {
        boolean readCache = getDlmsSessionProperties().isReadCache();

        if (getDlmsCache() == null || getDlmsCache().getObjectList() == null || readCache) {
            getLogger().log(Level.INFO, readCache ? "ReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
            setDlmsCache(new DLMSCache());
            readObjectList();
            getDlmsCache().saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().log(Level.INFO, "Cache exist, will not be read!");
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(getDlmsCache().getObjectList());

        }
    }

    private DLMSCache getDlmsCache() {
        return dlmsCache;
    }

    private void setDlmsCache(DLMSCache dlmsCache) {
        this.dlmsCache = dlmsCache;
    }

    protected ConfigurationSupport getDlmsConfigurationSupport() {
        if (dlmsConfigurationSupport == null) {
            dlmsConfigurationSupport = new AM100ConfigurationSupport();
        }
        return dlmsConfigurationSupport;
    }

    public AM100DlmsProperties getDlmsSessionProperties() {
        if (dlmsProperties == null) {
            dlmsProperties = new AM100DlmsProperties();
        }
        return dlmsProperties;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AS220/AS1440 AM100 DLMS (PRE-NTA)";
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    /**
     * The AM100 also supports the AT modem
     */
    @Override
    public List<ConnectionType> getSupportedConnectionTypes() {
        List<ConnectionType> result = super.getSupportedConnectionTypes();
        result.add(new SioAtModemConnectionType());
        result.add(new RxTxAtModemConnectionType());
        return result;
    }
}