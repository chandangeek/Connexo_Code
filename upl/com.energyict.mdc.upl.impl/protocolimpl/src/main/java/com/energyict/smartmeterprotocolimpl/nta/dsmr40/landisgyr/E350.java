package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.messages.legacy.NumberLookupExtractor;
import com.energyict.mdc.upl.messages.legacy.NumberLookupFinder;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarExtractor;
import com.energyict.mdc.upl.messages.legacy.TariffCalendarFinder;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.connections.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.protocol.HHUEnabler;
import com.energyict.protocol.MessageProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.AbstractSmartDSMR40NtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

import java.io.IOException;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2011
 * Time: 12:04:50
 */
public class E350 extends AbstractSmartDSMR40NtaProtocol implements HHUEnabler {

    protected LoadProfileBuilder loadProfileBuilder;
    protected MessageProtocol messageProtocol;

    public E350(TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, PropertySpecService propertySpecService, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
        super(propertySpecService, calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, numberLookupFinder, numberLookupExtractor);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new Dsmr40Messaging(new Dsmr40MessageExecutor(this, this.getCalendarFinder(), this.getCalendarExtractor(), this.getMessageFileFinder(), this.getMessageFileExtractor(), getNumberLookupExtractor(), getNumberLookupFinder()));
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-25 16:08:19 +0100 (Tue, 25 Nov 2014) $";
    }

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr E350 XEMEX DLMS (NTA DSMR4.0)";
    }

    @Override
    public String getMeterSerialNumber() throws IOException {
        try {
            return getMeterInfo().getEquipmentIdentifier();
        } catch (DataAccessResultException e) {
            getLogger().warning("Could not retrieve the equipment identifier of the meter. " + e.getMessage());
            getLogger().warning("Moving on without validating the equipment identifier.");
            return "";
        } catch (IOException e) {
            String message = "Could not retrieve the equipment identifier of the meter. " + e.getMessage();
            getLogger().warning(message);
            throw e;
        }
    }

    @Override
    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        HHUSignOn hhuSignOn = new IEC1107HHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                                  //HDLC:         9600 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "P07210", 0);      //IEC1107:      300 baud, 7E1
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        if (getCache() == null) {
            setCache(new DLMSCache());
        }
        if ((((DLMSCache) getCache()).getObjectList() == null) || ((Dsmr40Properties) getProperties()).isForcedToReadCache()) {
            getLogger().info(((Dsmr40Properties) getProperties()).isForcedToReadCache() ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
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
            ((LGLoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(((Dsmr40Properties) getProperties()).getCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return getProperties().getUPLPropertySpecs();
    }

}
