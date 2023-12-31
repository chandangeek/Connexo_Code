package com.energyict.smartmeterprotocolimpl.kaifa;

import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileExtractor;
import com.energyict.mdc.upl.messages.legacy.DeviceMessageFileFinder;
import com.energyict.mdc.upl.properties.PropertySpecService;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.LoadProfileConfiguration;
import com.energyict.protocol.LoadProfileReader;
import com.energyict.protocol.ProfileData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.events.AM110REventProfiles;

import java.io.IOException;
import java.util.List;


public class Kaifa extends AM110R {

    private KaifaProperties properties;

    /**
     * The used {@link KaifaLoadProfileBuilder} to read and manage the load profiles
     */
    private KaifaLoadProfileBuilder loadProfileBuilder;

    public Kaifa(PropertySpecService propertySpecService, DeviceMessageFileFinder deviceMessageFileFinder, DeviceMessageFileExtractor deviceMessageFileExtractor) {
        super(propertySpecService, deviceMessageFileFinder, deviceMessageFileExtractor);
    }

    @Override
    public List<ProfileData> getLoadProfileData(List<LoadProfileReader> loadProfiles) throws IOException {
        return getLoadProfileBuilder().getLoadProfileData(loadProfiles);
    }


    private KaifaLoadProfileBuilder getLoadProfileBuilder() {
        if (loadProfileBuilder == null) {
            loadProfileBuilder = new KaifaLoadProfileBuilder(this);
        }
        return loadProfileBuilder;
    }

    @Override
    public String getProtocolDescription() {
        return "Kaifa C&I DLMS (SMETS)";
    }

    @Override
    public String getVersion() {
        return "$Date: Mon Nov 7 16:38:22 2016 +0100 $";
    }


    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        HHUSignOn hhuSignOn = new KaifaHHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
        hhuSignOn.setMode(HHUSignOn.MODE_BINARY_HDLC);                                  //HDLC:         9600 baud, 8N1
        hhuSignOn.setProtocol(HHUSignOn.PROTOCOL_HDLC);
        hhuSignOn.enableDataReadout(datareadout);
        getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn, "", 0);            //IEC1107:      300 baud, 7E1
    }

    @Override
    public String getFirmwareVersion() throws IOException {
        try {
            byte[] firmwareVersion = getMeterInfo().getFirmwareVersion().getBytes();
            return ProtocolTools.getHexStringFromBytes(firmwareVersion);
        } catch (IOException e) {
            String message = "Could not fetch the firmwareVersion. " + e.getMessage();
            getLogger().finest(message);
            return "Unknown version";
        }
    }

    @Override
    public KaifaProperties getProperties() {
        if (this.properties == null) {
            this.properties = new KaifaProperties(this.getPropertySpecService());
        }
        return this.properties;
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        getDlmsSession().getMeterConfig().setInstantiatedObjectList(KaifaObjectList.OBJECT_LIST);
    }

    public String getMeterSerialNumber() {
        return getProperties().getSerialNumber();
    }

    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new KaifaRegisterFactory(this);
        }
        return registerFactory;
    }

    public AM110REventProfiles getEventProfiles() {
        if (eventProfiles == null) {
            this.eventProfiles = new KaifaEventProfiles(this);
        }
        return eventProfiles;
    }

    @Override
    public List<LoadProfileConfiguration> fetchLoadProfileConfiguration(List<LoadProfileReader> loadProfilesToRead) throws IOException {
        return getLoadProfileBuilder().fetchLoadProfileConfiguration(loadProfilesToRead);
    }


}