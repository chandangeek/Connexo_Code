package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540;

import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocolimpl.dlms.idis.AM540ObjectList;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.E350;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.events.AM540EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.messages.AM540Messaging;
import com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.registers.AM540RegisterFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.logging.Level;

/**
 * Protocol for the Elster AM540 module (connected to an AS3000 e-meter), following the DSMR 5.0 spec.
 * This is a hybrid supporting both the DSMR4.0 functionality and the G3 PLC objects.
 * <p>
 * Copyrights EnergyICT
 * Author: khe
 */
public class AM540 extends E350 {

    private static final String TIMEOUT = "timeout";

    public AM540(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, propertySpecService, numberLookupFinder, numberLookupExtractor);
        setHasBreaker(false);
    }

    @Override
    public void connect() throws IOException {
        getDlmsSession().init();
        try {
            getDlmsSession().getDLMSConnection().connectMAC();
        } catch (DLMSConnectionException e) {
            throw new NestedIOException(e, "Exception occurred while connection DLMSStream");
        }
        connectWithRetries();
        checkCacheObjects();
        initAfterConnect();
    }

    @Override
    public Serializable getCache() {
        if (dlmsCache == null || !(dlmsCache instanceof AM540Cache)) {
            this.dlmsCache = new AM540Cache();
        }
        ((AM540Cache) this.dlmsCache).setFrameCounter(getDlmsSession().getAso().getSecurityContext().getFrameCounter() + 1);     //Save this for the next session
        return dlmsCache;
    }

    @Override
    public void setCache(Serializable cache) {
        if ((cache != null) && (cache instanceof AM540Cache)) {
            this.dlmsCache = (AM540Cache) cache;
            long initialFrameCounter = ((AM540Cache) this.dlmsCache).getFrameCounter();
            this.getProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter == -1 ? 1 : initialFrameCounter);    //Get this from the last session
        }
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        int readCacheProperty = getProperties().getForcedToReadCache();
        if (getCache() == null) {
            setCache(new AM540Cache());
        }
        if ((((DLMSCache) getCache()).getObjectList() == null) || (readCacheProperty == 1)) {
            if (readCacheProperty == 1) {
                getLogger().info("ForcedToReadCache property is true, reading cache!");
                requestConfiguration();
                ((DLMSCache) getCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getLogger().info("Cache does not exist, using hardcoded copy of object list");
                UniversalObject[] objectList = new AM540ObjectList().getObjectList();
                ((DLMSCache) getCache()).saveObjectList(objectList);
            }
        } else {
            getLogger().info("Cache exist, will not be read!");
        }
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getCache()).getObjectList());
    }

    /**
     * Add extra retries to the association request.
     * If the request was rejected because by the meter the previous association was still open, this retry mechanism will solve the problem.
     */
    private void connectWithRetries() throws IOException {
        int tries = 0;
        while (true) {
            IOException exception;
            try {
                getDlmsSession().getDLMSConnection().setRetries(0);   //AARQ retries are handled here
                getDlmsSession().createAssociation(getProperties().getAARQTimeout());
                return;
            } catch (IOException e) {
                if (e instanceof DataAccessResultException) {
                    throw e;        //Throw real errors, e.g. unsupported security mechanism, wrong password...
                }
                exception = e;
            } finally {
                getDlmsSession().getDLMSConnection().setRetries(getProperties().getRetries());
            }

            //Release and retry the AARQ in case of ACSE exception
            if (++tries > getProperties().getAARQRetries()) {
                getLogger().severe("Unable to establish association after [" + tries + "/" + (getProperties().getAARQRetries() + 1) + "] tries.");
                throw new NestedIOException(exception);
            } else {
                if ((exception.getMessage() != null) && exception.getMessage().toLowerCase().contains(TIMEOUT)) {
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Unable to establish association after [" + tries + "/" + (getProperties().getAARQRetries() + 1) + "] tries, due to timeout. Retrying.");
                    }
                } else {
                    if (getLogger().isLoggable(Level.INFO)) {
                        getLogger().info("Unable to establish association after [" + tries + "/" + (getProperties().getAARQRetries() + 1) + "] tries. Sending RLRQ and retry ...");
                    }
                    try {
                        getDlmsSession().getAso().releaseAssociation();
                    } catch (IOException | DLMSConnectionException e) {
                        // Absorb exception: in 99% of the cases we expect an exception here ...
                    }
                }
                getDlmsSession().getAso().setAssociationState(ApplicationServiceObject.ASSOCIATION_DISCONNECTED);
            }
        }
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        //Do nothing. No MBUS slave devices are supported.
    }


    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }

    @Override
    public Dsmr50Properties getProperties() {
        if (this.properties == null) {
            this.properties = new Dsmr50Properties(this.getPropertySpecService());
        }
        return (Dsmr50Properties) this.properties;
    }

    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (registerFactory == null) {
            registerFactory = new AM540RegisterFactory(this);
        }
        return registerFactory;
    }

    @Override
    public EventProfile getEventProfile() {
        if (this.eventProfile == null) {
            this.eventProfile = new AM540EventProfile(this);
        }
        return eventProfile;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new AM540Messaging(this, this.getCalendarFinder(), this.getCalendarExtractor(), this.getMessageFileFinder(), this.getMessageFileExtractor(), getNumberLookupFinder(), getNumberLookupExtractor());
        }
        return messageProtocol;
    }

    @Override
    public String getProtocolDescription() {
        return "Elster AM540 DLMS (NTA DSMR5.0)";
    }

    @Override
    public String getVersion() {
        return "$Date: 2015-01-14 09:42:21 +0100 (Wed, 14 Jan 2015) $";
    }
}