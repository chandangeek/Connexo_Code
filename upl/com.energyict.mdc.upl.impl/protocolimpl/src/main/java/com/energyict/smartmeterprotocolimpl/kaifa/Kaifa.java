package com.energyict.smartmeterprotocolimpl.kaifa;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.dialer.coreimpl.IPDialer;
import com.energyict.dialer.coreimpl.NullDialer;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.CipheringType;
import com.energyict.protocol.*;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110R;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.AM110RProperties;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.AM110RSecurityProvider;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.common.MultipleClientRelatedObisCodes;
import com.energyict.smartmeterprotocolimpl.eict.AM110R.events.AM110REventProfiles;

import java.io.IOException;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;


public class Kaifa extends AM110R {

    private KaifaProperties properties;
    private HHUSignOn hhuSignOn;

    /**
     * The used {@link KaifaLoadProfileBuilder} to read and manage the load profiles
     */
    private KaifaLoadProfileBuilder loadProfileBuilder;

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
    public String getVersion() {
        return "$Date: 2016-03-25 14:54:31 +0200 (Fri, 25 Mar 2016)$";
    }


    public void enableHHUSignOn(SerialCommunicationChannel commChannel, boolean datareadout) throws ConnectionException {
        try {
            getDlmsSession().init();
        } catch (IOException e) {
            getLogger().warning("Failed while initializing the DLMS connection.");
        }
        hhuSignOn = (HHUSignOn) new KaifaHHUConnection(commChannel, getProperties().getTimeout(), getProperties().getRetries(), 300, 0);
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
            this.properties = new KaifaProperties();
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


    /**
     * Initializes the security provider and if necessary reads out the frame counter
     */
    protected void initializeSecurityProvider(Link link, Logger logger) throws IOException {
        init(link.getInputStream(), link.getOutputStream(), TimeZone.getDefault(), logger);
        enableHHUSignOn(link.getSerialCommunicationChannel(), false);

        if (getProperties().getDataTransportSecurityLevel() != 0 || getProperties().getAuthenticationSecurityLevel() == 5) {
            int backupClientId = getProperties().getClientMacAddress();
            String backupSecurityLevel = getProperties().getSecurityLevel();
            String password = getProperties().getPassword();
            CipheringType backUpCipheringType = getProperties().getCipheringType();

            getProperties().getProtocolProperties().setProperty(AM110RProperties.CLIENT_MAC_ADDRESS, "16");
            getProperties().getProtocolProperties().setProperty(AM110RProperties.SECURITY_LEVEL, "0:0");
            getProperties().getProtocolProperties().setProperty(AM110RProperties.CIPHERING_TYPE, "0");

            getProperties().setSecurityProvider(new AM110RSecurityProvider(getProperties().getProtocolProperties()));
            HHUSignOn hhuSignOn = getDlmsSession().getDLMSConnection().getHhuSignOn();
            dlmsSession = null;
            getDlmsSession().init();
            getDlmsSession().getDLMSConnection().setHHUSignOn(hhuSignOn,"", 0);

            getDlmsSession().connect();
            long initialFrameCounter = getDlmsSession().getCosemObjectFactory().getData(MultipleClientRelatedObisCodes.frameCounterForClient(backupClientId)).getValue();
            getDlmsSession().disconnect();

            getProperties().getProtocolProperties().setProperty(AM110RProperties.CLIENT_MAC_ADDRESS, Integer.toString(backupClientId));
            getProperties().getProtocolProperties().setProperty(AM110RProperties.SECURITY_LEVEL, backupSecurityLevel);
            getProperties().getProtocolProperties().setProperty(SmartMeterProtocol.PASSWORD, password);
            getProperties().getProtocolProperties().setProperty(AM110RProperties.CIPHERING_TYPE, backUpCipheringType.getTypeString());

            if (link instanceof IPDialer || link instanceof NullDialer) {
                String ipAddress = link.getStreamConnection().getSocket().getInetAddress().getHostAddress();
                link.getStreamConnection().serverClose();
                link.setStreamConnection(new SocketStreamConnection(ipAddress + ":4059"));
                link.getStreamConnection().serverOpen();
            }

            getProperties().setSecurityProvider(new AM110RSecurityProvider(getProperties().getProtocolProperties()));
            getProperties().getSecurityProvider().setInitialFrameCounter(initialFrameCounter + 1);

            reInitDlmsSession(link);
        } else {
            this.dlmsSession = null;
        }
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