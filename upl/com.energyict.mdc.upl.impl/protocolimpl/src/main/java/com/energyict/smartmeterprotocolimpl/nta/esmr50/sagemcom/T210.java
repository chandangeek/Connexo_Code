package com.energyict.smartmeterprotocolimpl.nta.esmr50.sagemcom;


import com.energyict.cbo.Quantity;
import com.energyict.mdc.upl.MeterProtocol;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.protocol.ProfileData;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;
@Deprecated
public class T210 implements MeterProtocol {

//    public T210(PropertySpecService propertySpecService, TariffCalendarFinder calendarFinder, TariffCalendarExtractor calendarExtractor, DeviceMessageFileFinder messageFileFinder, DeviceMessageFileExtractor messageFileExtractor, NumberLookupFinder numberLookupFinder, NumberLookupExtractor numberLookupExtractor) {
//        super(propertySpecService, calendarFinder, calendarExtractor, messageFileFinder, messageFileExtractor, numberLookupFinder, numberLookupExtractor);
//    }
//
//    @Override
//    public BulkRegisterProtocol getRegisterFactory() {
//        if (this.registerFactory == null) {
//            this.registerFactory = new T210RegisterFactory(this);
//        }
//        return this.registerFactory;
//    }
//
//    /**
//     * Getter for the <b>ESMR 5.0</b> EventProfile
//     *
//     * @return the lazy loaded EventProfile
//     */
//    public EventProfile getEventProfile() {
//        if (this.eventProfile == null) {
//            this.eventProfile = new SagemcomEsmr50EventProfile(this);
//        }
//        return eventProfile;
//    }
//
//    @Override
//    public MessageProtocol getMessageProtocol() {
//        return new ESMR50Messaging( new ESMR50MessageExecutor(this, this.getCalendarFinder(), this.getCalendarExtractor(), this.getMessageFileFinder(), this.getMessageFileExtractor(), this.getNumberLookupExtractor(), this.getNumberLookupFinder()));
//    }
//
//    @Override
//    public String getVersion() {
//        return "Sergiu first integration version";
//    }
//
//    @Override
//    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
//        try {
//            getDlmsSession().init();
//        } catch (IOException e) {
//            getLogger().warning("Failed while initializing the DLMS connection.");
//        }
//        DLMSConnection connection = getDlmsSession().getDLMSConnection();
//        getLogger().info("Not necessary to do HHU SignOn initialization, just create the DLMS Connection "+connection.toString());
//        return;
//    }

    @Override
    public String getProtocolDescription() {
        return "Sagemcom T210 DLMS protocol"; //todo Add proper description for T210
    }

    @Override
    public void init(InputStream inputStream, OutputStream outputStream, TimeZone timeZone, Logger logger) throws IOException {

    }

    @Override
    public void connect() throws IOException {

    }

    @Override
    public void disconnect() throws IOException {

    }

    @Override
    public String getProtocolVersion() {
        return "Sergiu first integration version";
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date lastReading, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public ProfileData getProfileData(Date from, Date to, boolean includeEvents) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(int channelId) throws IOException {
        return null;
    }

    @Override
    public Quantity getMeterReading(String name) throws IOException {
        return null;
    }

    @Override
    public int getNumberOfChannels() throws IOException {
        return 0;
    }

    @Override
    public int getProfileInterval() throws IOException {
        return 0;
    }

    @Override
    public Date getTime() throws IOException {
        return null;
    }

    @Override
    public String getRegister(String name) throws IOException {
        return null;
    }

    @Override
    public void setRegister(String name, String value) throws IOException {

    }

    @Override
    public void setTime() throws IOException {

    }

    @Override
    public void initializeDevice() throws IOException {

    }

    @Override
    public void release() throws IOException {

    }

    @Override
    public boolean hasSupportForSeparateEventsReading() {
        return false;
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        return null;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {

    }
}
