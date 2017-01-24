package com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr;

import com.elster.jupiter.calendar.CalendarService;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdc.device.topology.TopologyService;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.HHUEnabler;
import com.energyict.mdc.protocol.api.MessageProtocol;
import com.energyict.mdc.protocol.api.device.LoadProfileFactory;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.protocols.mdc.services.impl.OrmClient;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.Dsmr40Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.AbstractSmartDSMR40NtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.landisgyr.profiles.LGLoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.messages.Dsmr40Messaging;

import javax.inject.Inject;
import java.io.IOException;
import java.time.Clock;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2011
 * Time: 12:04:50
 */
public class E350 extends AbstractSmartDSMR40NtaProtocol implements HHUEnabler {

    @Override
    public String getProtocolDescription() {
        return "Landis+Gyr E350 XEMEX DLMS (NTA DSMR4.0)";
    }

    private LoadProfileBuilder loadProfileBuilder;
    protected MessageProtocol messageProtocol;

    @Inject
    public E350(PropertySpecService propertySpecService, Clock clock, TopologyService topologyService, CalendarService calendarService, OrmClient ormClient, MdcReadingTypeUtilService readingTypeUtilService, LoadProfileFactory loadProfileFactory) {
        super(propertySpecService, clock, topologyService, calendarService, ormClient, readingTypeUtilService, loadProfileFactory);
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        if (messageProtocol == null) {
            messageProtocol = new Dsmr40Messaging(new Dsmr40MessageExecutor(this, this.getClock(), this.getTopologyService(), this.getCalendarService()));
        }
        return messageProtocol;
    }

    @Override
    public String getVersion() {
        return "$Date: 2014-11-25 16:08:19 +0100 (Tue, 25 Nov 2014) $";
    }

    /**
     * Get the equipment identifier (serves as a unique serial number) of the device
     */
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

    /**
     * Method to check whether the cache needs to be read out or not, if so the read will be forced.<br>
     * <br>
     * <p/>
     * The E350 module does not have the checkConfigParameter in his objectlist, thus to prevent reading the
     * objectlist each time we read the device, we will go for the following approach:<br>
     * 1/ check if the cache exists, if it does exist, go to step 2, if not go to step 3    <br>
     * 2/ is the custom property forcedToReadCache enabled? If yes then go to step 3, else exit    <br>
     * 3/ readout the objectlist    <br>
     *
     * @throws java.io.IOException
     */
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
            this.loadProfileBuilder = new LGLoadProfileBuilder(this, this.getReadingTypeUtilService());
            ((LGLoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(((Dsmr40Properties) getProperties()).getCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    /**
     * Get the AXDRDateTimeDeviationType for this DeviceType
     *
     * @return the requested type
     */
    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

}