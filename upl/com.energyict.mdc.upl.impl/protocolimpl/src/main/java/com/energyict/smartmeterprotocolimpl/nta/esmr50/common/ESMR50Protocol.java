package com.energyict.smartmeterprotocolimpl.nta.esmr50.common;


import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NotFoundException;
import com.energyict.cpo.Transaction;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.Link;
import com.energyict.dialer.coreimpl.IPDialer;
import com.energyict.dialer.coreimpl.OpticalDialer;
import com.energyict.dialer.coreimpl.SocketStreamConnection;
import com.energyict.dlms.DlmsSession;
import com.energyict.dlms.axrdencoding.util.AXDRDateTimeDeviationType;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.MessageProtocol;
import com.energyict.protocol.MeterEvent;
import com.energyict.protocol.ProtocolException;
import com.energyict.protocol.SlaveMeterProtocolEventSupport;
import com.energyict.protocolimpl.base.RTUCache;
import com.energyict.protocolimpl.dlms.DLMSCache;
import com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23Properties;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.profiles.LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.events.ESMR50EventProfile;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.loadprofiles.ESMR50LoadProfileBuilder;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50MessageExecutor;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.messages.ESMR50Messaging;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.ESMR50Cache;
import com.energyict.smartmeterprotocolimpl.nta.esmr50.common.registers.ESMR50RegisterFactory;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class ESMR50Protocol extends com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.AbstractSmartDSMR40NtaProtocol
                                    implements SlaveMeterProtocolEventSupport{

    public static final ObisCode EMETER_LP1_OBISCODE = ObisCode.fromString("1.0.99.1.0.255");
    public static final ObisCode EMETER_LP2_OBISCODE_SAME_AS_MBUS_LP2 = ObisCode.fromString("1.0.99.2.0.255");
    public static final ObisCode EMETER_LP3_OBISCODE_SAME_AS_MBUS_LP3 = ObisCode.fromString("1.0.98.1.0.255");

    public static final ObisCode MBUS_LP1_OBISCODE = ObisCode.fromString("0.x.24.3.0.255");
    public static final ObisCode MBUS_LP2_OBISCODE_SAME_AS_EMETER_LP2 = ObisCode.fromString("0.x.99.2.0.255");
    public static final ObisCode MBUS_LP3_OBISCODE_SAME_AS_EMETER_LP3 = ObisCode.fromString("0.x.98.1.0.255");

    private static final ObisCode FRAME_COUNTER_OBISCODE = ObisCode.fromString("0.0.43.1.0.255");
    private static final long FRAME_COUNTER_NEXT_INCREMENT = 1;
    private Link link;
    private long cachedFrameCounter;
    private String connectionAddress;

    public String getVersion() {
        return "$Date: 2017-04-25 16:00:00$";
    }

    @Override
    public void connect() throws IOException {
        initFrameCounter();
        getDlmsSession().connect();
        checkCacheObjects();
    }

    @Override
    protected void initAfterConnect() throws ConnectionException {
        super.initAfterConnect(); // this will call searchForSlaveDevices(); in AbstractSmartNtaProtocol
    }


    @Override
    public AXDRDateTimeDeviationType getDateTimeDeviationType() {
        return AXDRDateTimeDeviationType.Negative;
    }

    @Override
    public MessageProtocol getMessageProtocol() {
        return new ESMR50Messaging(new ESMR50MessageExecutor(this));
    }

    @Override
    public Object getCache() {
        if (dlmsCache != null && dlmsCache instanceof ESMR50Cache) {
            long lastFrameCounter = getDlmsSession().getAso().getSecurityContext().getFrameCounter() + FRAME_COUNTER_NEXT_INCREMENT;
            ((ESMR50Cache) this.dlmsCache).setFrameCounter(lastFrameCounter);     //Save this for the next session
            return this.dlmsCache;
        } else {
            return new ESMR50Cache();
        }
    }

    @Override
    public void setCache(Object cache) {
        if ((cache != null) && (cache instanceof ESMR50Cache)) {
            this.dlmsCache = (ESMR50Cache) cache;
            cachedFrameCounter = ((ESMR50Cache) this.dlmsCache).getFrameCounter();
            this.getProperties().getSecurityProvider().setInitialFrameCounter(cachedFrameCounter == -1 ? 1 : cachedFrameCounter+FRAME_COUNTER_NEXT_INCREMENT);    //Get this from the last session
        }
    }

    @Override
    public Object fetchCache(int rtuid) throws SQLException, BusinessException {
        if (rtuid != 0) {
            RTUCache rtuCache = new RTUCache(rtuid);
            try {
                return rtuCache.getCacheObject();
            } catch (NotFoundException e) {
                return new ESMR50Cache();
            } catch (IOException e) {
                return new ESMR50Cache();
            }
        } else {
            throw new com.energyict.cbo.BusinessException("invalid RtuId!");
        }
    }

    @Override
    public void updateCache(final int rtuid, final Object cacheObject) throws SQLException, BusinessException {
        if (rtuid != 0) {
            Transaction tr = new Transaction() {
                public Object doExecute() throws BusinessException, SQLException {
                    ESMR50Cache dc = (ESMR50Cache) cacheObject;
                    new RTUCache(rtuid).setBlob(dc);
                    return null;
                }
            };
            MeteringWarehouse.getCurrent().execute(tr);
        } else {
            throw new com.energyict.cbo.BusinessException("invalid RtuId!");
        }
    }

    @Override
    protected void checkCacheObjects() throws IOException {
        if (getCache() == null) {
            setCache(new DLMSCache());
        }

        try {
            if ((((DLMSCache) getCache()).getObjectList() == null) || ((ESMR50Properties) getProperties()).getForcedToReadCache()) {
                getLogger().info(((ESMR50Properties) getProperties()).getForcedToReadCache() ? "ForcedToReadCache property is true, reading cache!" : "Cache does not exist, configuration is forced to be read.");
                requestConfiguration();
                ((DLMSCache) getCache()).saveObjectList(getDlmsSession().getMeterConfig().getInstantiatedObjectList());
            } else {
                getDlmsSession().getMeterConfig().setInstantiatedObjectList(((DLMSCache) getCache()).getObjectList());
                getLogger().info("Cache exist, will not be read.");
            }
        } catch (Exception ex){
            getLogger().severe("Exception while reading cache :" + ex.getMessage() + ex.getCause());

        }
    }

    /**
     * Implementation of SlaveMeterProtocolEventSupport
     *
     * @param lastLogbookDate
     * @return
     * @throws IOException
     */
    public List<MeterEvent> getSlaveMeterEvents(String slaveSerialNumber, final Date lastLogbookDate) throws IOException {
        ESMR50EventProfile esmr50EventProfile = (ESMR50EventProfile) getEventProfile();

        return esmr50EventProfile.getSlaveEvents(slaveSerialNumber, lastLogbookDate);
    }

    /**
     * Getter for the {@link com.energyict.protocolimpl.dlms.common.DlmsProtocolProperties}
     *
     * @return the requested Properties
     */
    @Override
    public DlmsProtocolProperties getProperties() {
        if (this.properties == null) {
            this.properties = new ESMR50Properties();
        }
        return this.properties;
    }


    @Override
    public BulkRegisterProtocol getRegisterFactory() {
        if (this.registerFactory == null) {
            this.registerFactory = new ESMR50RegisterFactory(this);
        }
        return this.registerFactory;
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

    @Override
    public LoadProfileBuilder getLoadProfileBuilder() {
        if (this.loadProfileBuilder == null) {
            loadProfileBuilder = new ESMR50LoadProfileBuilder(this);
            ((ESMR50LoadProfileBuilder) loadProfileBuilder).setCumulativeCaptureTimeChannel(((ESMR50Properties) getProperties()).getCumulativeCaptureTimeChannel());
        }
        return loadProfileBuilder;
    }

    /**
     * Return a B-Field corrected ObisCode.
     *
     * @param obisCode     the ObisCode to correct
     * @param serialNumber the serialNumber of the device for which this ObisCode must be corrected
     * @return the corrected ObisCode
     */
    @Override
    public ObisCode getPhysicalAddressCorrectedObisCode(ObisCode obisCode, String serialNumber) {
        int address;
        StringBuilder sb = new StringBuilder();
        sb.append(" #translate ").append(obisCode.toString()).append(" [").append(serialNumber).append(" ] --> ");

        if (obisCode.equalsIgnoreBChannel(EMETER_LP1_OBISCODE)
                || (obisCode.equalsIgnoreBChannel(EMETER_LP2_OBISCODE_SAME_AS_MBUS_LP2) && obisCode.getA() == 1)
                || (obisCode.equalsIgnoreBChannel(EMETER_LP3_OBISCODE_SAME_AS_MBUS_LP3) && obisCode.getA() == 1)) {
            address = 0; // this is an e-meter
            sb.append(" (this looks like an e-meter) ");
        } else { // this is an MBus
            sb.append(" (this looks like an M-Bus, getting channel) ");
            address = getPhysicalAddressFromSerialNumber(serialNumber);
        }

        sb.append(" @={").append(address).append("} ");
        if ((address == 0 && obisCode.getB() != -1 && obisCode.getB() != 128)) { // then don't correct the obisCode
            //sb.append(obisCode.toString());
            //getLogger().finest(sb.toString());
            return obisCode;
        }

        if (address != -1) {
            ObisCode returnObis = ProtocolTools.setObisCodeField(obisCode, ObisCodeBFieldIndex, (byte) address);
            //sb.append(returnObis.toString());
            //getLogger().finest(sb.toString());
            return returnObis;
        }
        sb.append("NULL");
        getLogger().finest(sb.toString());
        return null;
    }

    /**
     * Implementation according to https://jira.eict.vpdc/browse/COMMUNICATION-1718
     */
    @Override
    public boolean executeWakeUp(int communicationSchedulerId, Link link, Logger logger) throws BusinessException, IOException {
        logger.info("ESMR 5.0 Protocol " + getVersion());
        this.link = link;
        try {
            if (link.getClass().equals(OpticalDialer.class)){
                logger.info("No wakeup for optical connections.");
               return true;
            }

                if (communicationSchedulerId == 0){
                logger.info("Mocked launch, no wake-up, return true");
                return  true;
            }
            CommunicationScheduler cs = ProtocolTools.mw().getCommunicationSchedulerFactory().find(communicationSchedulerId);
            String dialerClass = cs.getDialerFactory().getDialerClassName();

            if (!dialerClass.equals(com.energyict.dialer.coreimpl.NullDialer.class.getName())){
                logger.finest(" - dialer configured, will let it handle the connection (" + cs.getDialerFactory().getName() + ")");
                return true;
            } else {
                logger.info("Connecting using IP stored in EIWeb (from a previous wake-up)");
            }

            String ipAddress = ProtocolTools.checkIPAddressForPortNumber(cs.getRtu().getIpAddress(), String.valueOf(getProperties().getIpPortNumber()));
            this.connectionAddress = ipAddress;
            logger.info("Connecting EIWeb IP address: " + ipAddress);
            link.setStreamConnection(new SocketStreamConnection(ipAddress));
            link.getStreamConnection().open();
            logger.info("Connected to " + ipAddress);


            return true;
        } catch (Exception ex){
            logger.log(Level.SEVERE, ex.getMessage(), ex);
        }

        return false;
    }

    private void initFrameCounter() throws IOException {
        if (getProperties().getDataTransportSecurityLevel() != 0 || getProperties().getAuthenticationSecurityLevel() == 5) {

            if (cachedFrameCounter!=-1 ){
                getLogger().info("Cached frameCounter is "+cachedFrameCounter+", will test it ...");
                try {
                    getDlmsSession().getProperties().getSecurityProvider().setInitialFrameCounter(cachedFrameCounter+1);
                    getDlmsSession().getAso().getSecurityContext().setFrameCounter(cachedFrameCounter+1);
                    getDlmsSession().connect();
                    getLogger().info("Cached frameCounter is valid!");
                    return;
                } catch (Exception ex) {
                    logException(Level.WARNING, "Cached frameCounter is not valid! Exception: " + ex.getMessage(), ex);
                }
            } else {
                getLogger().info("No cache for the frame counter!");
            }

            try{
                getDlmsSession().disconnect();
            } catch (Exception ex){
                logException(Level.WARNING, "Could not disconnect the dlms session!", ex);
            }

            reconnect();
            sleep();

            long initialFrameCounter = 0;

            try {
                getLogger().info("Requesting the frame counter from the meter ...");

                Properties properties = (Properties) getProperties().getProtocolProperties().clone();
                properties.setProperty(Dsmr23Properties.CLIENT_MAC_ADDRESS, "16");
                properties.setProperty(Dsmr23Properties.SECURITY_LEVEL, "0:0");
                ESMR50Properties publicClientProperties = new ESMR50Properties(properties);


                DlmsSession publicDlmsSession = new DlmsSession(getInputStream(), getOutputStream(), getLogger(), publicClientProperties, getTimeZone());
                publicDlmsSession.getAso().getAssociationControlServiceElement().clearCalledApplicationProcessTitle();
                publicDlmsSession.getAso().getAssociationControlServiceElement().clearCallingApplicationProcessTitle();
                //publicDlmsSession.getAso().getAssociationControlServiceElement().setCallingApplicationProcessTitle(ProtocolTools.getBytesFromHexString("04504c053000000000000000"));
                publicDlmsSession.init();

                publicDlmsSession.connect();

                Data frameCounterObjectData = publicDlmsSession.getCosemObjectFactory().getData(FRAME_COUNTER_OBISCODE);
                initialFrameCounter = frameCounterObjectData.getValueAttr().longValue();
                getLogger().info(" .. read-out frame counter: "+initialFrameCounter);

                ((ESMR50Cache)getCache()).setFrameCounter(initialFrameCounter+FRAME_COUNTER_NEXT_INCREMENT);

                publicDlmsSession.disconnect();

                reconnect();
               // getDlmsSession().assumeConnected(publicClientProperties.getMaxRecPDUSize(), publicClientProperties.getConformanceBlock());


            } catch (Exception ex) {
                logException(Level.SEVERE, "Exception while reading frame counter with public client: "+ex.getMessage(), ex);
                throw new IOException("Unable to read framecounter or connect");
            }

            sleep();

            resetFrameCounter(initialFrameCounter + FRAME_COUNTER_NEXT_INCREMENT);
        }
    }

    private void sleep() {
        try {
            getLogger().finest("... sleeping 5 seconds to allow the meter to settle ...");
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    private void reconnect(){
        try {
            if (getProperties().isNtaSimulationTool()) {
                if (link instanceof IPDialer) {
                    String ipAddress = link.getStreamConnection().getSocket().getInetAddress().getHostAddress();
                    String fullIpAddress = ProtocolTools.checkIPAddressForPortNumber(ipAddress, String.valueOf(getProperties().getIpPortNumber()));
                    getLogger().info("Reconnecting to " + fullIpAddress);
                    link.getStreamConnection().serverClose();
                    link.setStreamConnection(new SocketStreamConnection(fullIpAddress));
                    link.getStreamConnection().serverOpen();
                    init(link.getInputStream(), link.getOutputStream(), getTimeZone(), getLogger());
                } else if (link instanceof OpticalDialer) {
                    // no connection
                } else {
                    getLogger().info("Re-connecting EIWeb IP address: " + connectionAddress);
                    link.getStreamConnection().serverClose();
                    link.setStreamConnection(new SocketStreamConnection(connectionAddress));
                    link.getStreamConnection().open();
                    init(link.getInputStream(), link.getOutputStream(), getTimeZone(), getLogger());
                }
            }
        }catch (Exception ex){
            logException(Level.SEVERE, "Exception while reconecting: " + ex.getMessage(), ex);
        }

    }

    private void logException(Level level, String message, Exception ex){
        /*
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        getLogger().log(level, message+"\n"+sw.toString());
        */
        getLogger().log(level, message, ex);

    }

    public void resetFrameCounter(long newFrameCounter) {
        getProperties().getSecurityProvider().setInitialFrameCounter(newFrameCounter);
        getDlmsSession().getAso().getSecurityContext().setFrameCounter(newFrameCounter);
        getDlmsSession().getAso().getSecurityContext().setFrameCounterInitialized(true);
        if (getDlmsSession().getDLMSConnection()!=null) {
            getDlmsSession().getDLMSConnection().getApplicationServiceObject().getSecurityContext().setFrameCounter(newFrameCounter);
        }
        ((ESMR50Cache)getCache()).setFrameCounter(newFrameCounter);
    }
}


