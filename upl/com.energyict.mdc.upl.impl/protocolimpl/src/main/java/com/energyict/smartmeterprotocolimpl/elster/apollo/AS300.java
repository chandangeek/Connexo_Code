package com.energyict.smartmeterprotocolimpl.elster.apollo;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.axrdencoding.util.AXDRDateTime;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.dlms.common.AbstractSmartDlmsProtocol;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.smartmeterprotocolimpl.common.SimpleMeter;
import com.energyict.smartmeterprotocolimpl.elster.apollo.eventhandling.ApolloEventProfiles;

import java.io.IOException;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 29-jun-2011
 * Time: 11:32:30
 */
public class AS300 extends AbstractSmartDlmsProtocol implements SimpleMeter {

    private ApolloProperties properties;
    private ApolloObjectFactory objectFactory;
    private RegisterReader registerReader;
    private LoadProfileBuilder loadProfileBuilder;

    @Override
    protected DlmsProtocolProperties getProperties() {
        if (properties == null) {
            properties = new ApolloProperties();
        }
        return properties;
    }

    public ApolloObjectFactory getObjectFactory() {
        if (objectFactory == null) {
            objectFactory = new ApolloObjectFactory(getDlmsSession());
        }
        return objectFactory;
    }

    private RegisterReader getRegisterReader() {
        if (registerReader == null) {
            registerReader = new RegisterReader(this);
        }
        return registerReader;
    }

    private LoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new LoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        //Nothing, no topology available.
    }

    public String getFirmwareVersion() throws IOException {
        return getObjectFactory().getFirmwareVersion().getString();
    }

    @Override
    public Date getTime() throws IOException {
        return getObjectFactory().getClock().getDateTime();
    }

    @Override
    public void setTime(Date newMeterTime) throws IOException {
        getObjectFactory().getClock().setAXDRDateTimeAttr(new AXDRDateTime(newMeterTime));
    }

    public String getMeterSerialNumber() throws IOException {
        return getObjectFactory().getSerialNumber().getString();
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return null; //TODO
    }

    /**
     * Overridden because the object list is not used.
     */
    @Override
    public void connect() throws IOException {
        getDlmsSession().connect();
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        return getRegisterReader().read(registers);
    }

    public List<MeterEvent> getMeterEvents(Date lastLogbookDate) throws IOException {
        ApolloEventProfiles logs = new ApolloEventProfiles(this);
        Calendar cal = Calendar.getInstance(getTimeZone());
        cal.setTime(lastLogbookDate == null ? new Date(0) : lastLogbookDate);
        return logs.getEventLog(cal);
    }

    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }

    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }

    public String getVersion() {
        return "$Date$";
    }

    public String getSerialNumber() {
        return getProperties().getSerialNumber();
    }

    public int getPhysicalAddress() {
        return 0;//TODO?
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(ApolloObjectList.OBJECT_LIST);
    }

}