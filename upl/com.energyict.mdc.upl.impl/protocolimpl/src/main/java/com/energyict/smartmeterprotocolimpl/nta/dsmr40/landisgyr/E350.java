package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

import java.io.IOException;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2011
 * Time: 12:04:50
 */
public class E350 extends AbstractSmartNtaProtocol {

    private LoadProfileBuilder loadProfileBuilder;

    @Override
    public MessageProtocol getMessageProtocol() {
        return new Dsmr40Messaging(new Dsmr40MessageExecutor(this));
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr40Properties();
        }
        return this.properties;
    }

    @Override
    public String getVersion() {
        return "$Date$";
    }

    @Override
    public Dsmr23RegisterFactory getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new DSMR40RegisterFactory(this);
        }
        return this.registerFactory;
    }

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced.<br>
     * <br>
     * <p/>
     * The AM100 module does not have the checkConfigParameter in his objectlist, thus to prevent reading the
     * objectlist each time we read the device, we will go for the following approach:<br>
     * 1/ check if the cache exists, if it does exist, go to step 2, if not go to step 3    <br>
     * 2/ is the custom property forcedToReadCache enabled? If yes then go to step 3, else exit    <br>
     * 3/ readout the objectlist    <br>
     *
     * @throws java.io.IOException
     */
    @Override
    protected void checkCacheObjects() throws IOException {
        if ((((DLMSCache) getCache()).getObjectList() == null) || ((Dsmr40Properties) getProperties()).getForcedToReadCache()) {
            getLogger().info(((Dsmr40Properties) getProperties()).getForcedToReadCache() ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
            requestConfiguration();
            ((DLMSCache) getCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
        } else {
            getLogger().info("Cache exist, will not be read!");
            getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getCache()).getObjectList());
        }
    }

    @Override
    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            this.loadProfileBuilder = new LGLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }
}
