package com.energyict.protocolimpl.dlms.edp;

import com.energyict.dlms.DLMSCache;
import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.messaging.Message;
import com.energyict.protocol.messaging.MessageTag;
import com.energyict.protocol.messaging.MessageValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.dlms.AbstractDLMSProtocol;
import com.energyict.protocolimpl.dlms.edp.logbooks.LogbookReader;
import com.energyict.protocolimpl.dlms.edp.registers.EDPStoredValues;
import com.energyict.protocolimpl.dlms.edp.registers.RegisterReader;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 5/02/14
 * Time: 11:34
 * Author: khe
 */
public class CX20009 extends AbstractDLMSProtocol implements MessageProtocol, CacheMechanism, SerialNumberSupport {

    public static final ObisCode SERIAL_NUMBER = ObisCode.fromString("0.0.96.1.0.255");
    public static final ObisCode CORE_FIRMWARE_VERSION = ObisCode.fromString("1.0.0.2.0.255");
    public static final ObisCode APPLICATION_FIRMWARE_VERSION = ObisCode.fromString("1.1.0.2.0.255");
    public static final ObisCode PROFILE_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");

    private RegisterReader registerReader = null;
    private EDPProperties edpProperties;
    private LogbookReader logbookReader;
    private LoadProfileReader loadProfileReader;
    private EDPStoredValues storedValues;

    @Override
    public Date getTime() throws IOException {
        if (getEdpProperties().isFirmwareClient()) {
            getLogger().info("Using firmware client, cannot read out the device time. Using system time instead.");
            return new Date();
        }
        return getCosemObjectFactory().getClock().getDateTime();
    }

    @Override
    public void setTime() throws IOException {
        getCosemObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(new Date(), getTimeZone()));
    }

    @Override
    public String getProtocolVersion() {
        return "$Date: 2015-11-26 15:25:58 +0200 (Thu, 26 Nov 2015)$";
    }

    @Override
    public String getSerialNumber()  {
        try {
            AbstractDataType valueAttr = getCosemObjectFactory().getData(SERIAL_NUMBER).getValueAttr();
            if (valueAttr.getOctetString() == null) {
                throw new ProtocolException("Could not verify serial number, expected an OctetString but received an " + valueAttr.getClass().getSimpleName());
            }
            return valueAttr.getOctetString().stringValue();
        } catch (IOException e) {
            throw DLMSIOExceptionHandler.handle(e, retries + 1);
        }
    }

    @Override
    public void setProperties(Properties properties) throws InvalidPropertyException, MissingPropertyException {
        getEdpProperties().addProperties(properties);
        super.setProperties(properties);
    }

    @Override
    protected void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
        clientMacAddress = getEdpProperties().getClientMacAddress();
        serverUpperMacAddress = getEdpProperties().getServerUpperMacAddress();
        serverLowerMacAddress = getEdpProperties().getServerLowerMacAddress();
        if (connectionMode == 1) {     //Swap the fields for GPRS: TCPIPConnection uses the serverLowerMacAddress as destination-port
            serverLowerMacAddress = serverUpperMacAddress;
        }
    }

    @Override
    protected List doGetOptionalKeys() {
        return getEdpProperties().getOptionalKeys();
    }

    public EDPProperties getEdpProperties() {
        if (edpProperties == null) {
            edpProperties = new EDPProperties(new Properties());
        }
        return edpProperties;
    }

    public String getFirmwareVersion() throws IOException {
        String coreFWVersion = getCosemObjectFactory().getData(CORE_FIRMWARE_VERSION).getValueAttr().getOctetString().stringValue();
        String applicationFWVersion = getCosemObjectFactory().getData(APPLICATION_FIRMWARE_VERSION).getValueAttr().getOctetString().stringValue();
        return "Active core: [" + coreFWVersion + "]" + ", active application: [" + applicationFWVersion + "]";
    }

    @Override
    public void connect() throws IOException {
        try {
            if (this.aso.getAssociationStatus() == ApplicationServiceObject.ASSOCIATION_DISCONNECTED) {
                getDLMSConnection().connectMAC();
                this.aso.createAssociation();
                checkCacheObjects();
            }
        } catch (DLMSConnectionException e) {
            disconnect();    //Previous connection is still open, close it and retry
            super.connect();
        }
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        if (to == null) {
            to = Calendar.getInstance(getTimeZone()).getTime();
        }
        ProfileData profileData = getLoadProfileReader().readProfileData(from, to);
        if (includeEvents) {
            List<MeterEvent> meterEvents = getLogbookReader().readAllEvents(from, to);
            profileData.setMeterEvents(meterEvents);
        }
        return profileData;
    }

    private LoadProfileReader getLoadProfileReader() {
        if (loadProfileReader == null) {
            loadProfileReader = new LoadProfileReader(this);
        }
        return loadProfileReader;
    }

    private LogbookReader getLogbookReader() {
        if (logbookReader == null) {
            logbookReader = new LogbookReader(this);
        }
        return logbookReader;
    }

    /**
     * Only disconnectMAC is needed - releaseAssociation is not supported by meter & should not be sent.
     */
    @Override
    public void disconnect() {
        try {
            getDLMSConnection().disconnectMAC();
        } catch (IOException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        } catch (DLMSConnectionException e) {
            //absorb -> trying to close communication
            getLogger().log(Level.FINEST, e.getMessage());
        }
    }

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return new RegisterInfo(obisCode.getDescription());
    }

    @Override
    public RegisterValue readRegister(ObisCode obisCode) throws IOException {
        return getRegisterReader().readRegister(obisCode);
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    public void applyMessages(List messageEntries) throws IOException {
        //Do nothing
    }

    public MessageResult queryMessage(MessageEntry messageEntry) throws IOException {
        return MessageResult.createFailed(messageEntry);
    }

    public List getMessageCategories() {
        return new ArrayList<>();
    }

    public String writeMessage(Message msg) {
        return "";
    }

    public String writeTag(MessageTag tag) {
        return "";
    }

    public String writeValue(MessageValue value) {
        return "";
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        if (dlmsCache == null) {
            dlmsCache = new DLMSCache();
        }
        if (dlmsCache.getObjectList() == null || getEdpProperties().isReadCache()) {
            requestObjectList();
            dlmsCache.saveObjectList(dlmsMeterConfig.getInstantiatedObjectList());  // save object list in cache
        } else {
            dlmsMeterConfig.setInstantiatedObjectList(dlmsCache.getObjectList());
        }
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return getCosemObjectFactory().getProfileGeneric(PROFILE_OBISCODE).getCapturePeriod();
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return getCosemObjectFactory().getProfileGeneric(PROFILE_OBISCODE).getNumberOfProfileChannels();
    }

    public int getReference() {
        return LN_REFERENCE;
    }

    public StoredValues getStoredValues() {
        if (storedValues == null) {
            storedValues = new EDPStoredValues(this);
        }
        return storedValues;
    }

    public String getFileName() {
        final Calendar calendar = Calendar.getInstance();
        return calendar.get(Calendar.YEAR) + "_" + (calendar.get(Calendar.MONTH) + 1) + "_" + calendar.get(Calendar.DAY_OF_MONTH) + "_" + this.deviceId + "_" + this.serialNumber + "_" + serverUpperMacAddress + "_" + this.getClass().getSimpleName() + ".cache";
    }
}