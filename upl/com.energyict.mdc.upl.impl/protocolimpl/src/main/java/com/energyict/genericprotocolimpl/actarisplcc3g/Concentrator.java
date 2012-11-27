package com.energyict.genericprotocolimpl.actarisplcc3g;

import com.energyict.cbo.BusinessException;
import com.energyict.cpo.*;
import com.energyict.dialer.core.*;
import com.energyict.dlms.*;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.PLCCMeterListBlocData;
import com.energyict.genericprotocolimpl.actarisplcc3g.cosemobjects.PLCCObjectFactory;
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.*;
import com.energyict.protocol.messaging.*;
import com.energyict.protocolimpl.edf.messages.objects.ActivityCalendar;

import java.io.*;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Note: class design is not multithreaded.  This would required too many
 * paramters in the method calls.
 *
 * @author fbo
 */

public class Concentrator implements GenericProtocol, ProtocolLink, Messaging {

    final int DEBUG = 1;

    /* property key security level */
    private static final String PK_SECURITY_LEVEL = "SecurityLevel";
    private static final String PK_TIMEOUT = "Timeout";

    /* property default security level */
    private static final int PD_SECURITY_LEVEL = 1;


    private Properties properties;

    private CommunicationScheduler scheduler;
    private Link link;
    private Logger logger;

    private TCPIPConnection connection;
    private PLCCObjectFactory objectFactory;
    private CosemObjectFactory cosemObjectFactory;

    private Device concentratorDevice;
    private Device currentSelectedDevice;

    DLMSMeterConfig dLMSMeterConfig = DLMSMeterConfig.getInstance("ActarisPLCC");
    private int profileInterval;
    private ConcentratorProfile concentratorProfile = null;
    private ConcentratorRegister concentratorRegister = null;
    StringBuffer errorMessage = null;


    public List getMessageCategories() {

        List theCategories = new ArrayList();
        // Action Parameters
        MessageCategorySpec cat = new MessageCategorySpec("Actions");
        MessageSpec msgSpec = null;

//        msgSpec = addBasicMsg("Connect", RtuMessageConstant.CONNECT_LOAD, !ADVANCED);
//        cat.addMessageSpec(msgSpec);
//        
//        msgSpec = addBasicMsg("Disconnect", RtuMessageConstant.DISCONNECT_LOAD, !ADVANCED);
//        cat.addMessageSpec(msgSpec);

        theCategories.add(cat);
        return theCategories;

    }

    public String writeMessage(Message msg) {
        return msg.write(this);
    }

    private MessageSpec addBasicMsg(String keyId, String tagName, boolean advanced) {
        MessageSpec msgSpec = new MessageSpec(keyId, advanced);
        MessageTagSpec tagSpec = new MessageTagSpec(tagName);
        msgSpec.add(tagSpec);
        return msgSpec;
    }

    public String writeTag(MessageTag msgTag) {
        StringBuffer buf = new StringBuffer();

        // a. Opening tag
        buf.append("<");
        buf.append(msgTag.getName());

        // b. Attributes
        for (Iterator it = msgTag.getAttributes().iterator(); it.hasNext(); ) {
            MessageAttribute att = (MessageAttribute) it.next();
            if (att.getValue() == null || att.getValue().length() == 0) {
                continue;
            }
            buf.append(" ").append(att.getSpec().getName());
            buf.append("=").append('"').append(att.getValue()).append('"');
        }
        if (msgTag.getSubElements().isEmpty()) {
            buf.append("/>");
            return buf.toString();
        }
        buf.append(">");
        // c. sub elements
        for (Iterator it = msgTag.getSubElements().iterator(); it.hasNext(); ) {
            MessageElement elt = (MessageElement) it.next();
            if (elt.isTag()) {
                buf.append(writeTag((MessageTag) elt));
            } else if (elt.isValue()) {
                String value = writeValue((MessageValue) elt);
                if (value == null || value.length() == 0) {
                    return "";
                }
                buf.append(value);
            }
        }

        // d. Closing tag
        buf.append("</");
        buf.append(msgTag.getName());
        buf.append(">");

        return buf.toString();
    }

    public String writeValue(MessageValue msgValue) {
        return msgValue.getValue();
    }

    public void execute(CommunicationScheduler scheduler, Link link, Logger logger)
            throws BusinessException, SQLException, IOException {

        this.setScheduler(scheduler);
        this.link = link;
        this.logger = logger;


        init(link.getInputStream(), link.getOutputStream());
        initFactories();
        concentratorDevice = scheduler.getRtu();
        //setProfileInterval(concentratorDevice.getIntervalInSeconds());

        try {
            connect();
            logger.info("connected to " + concentratorToString(concentratorDevice));

            HandleConcentrator handleConcentrator = new HandleConcentrator(this, concentratorDevice, scheduler, logger);
            handleConcentrator.verifyAndSetTime();


            if (handleConcentrator.isFailed()) {
                if (errorMessage == null) {
                    errorMessage = new StringBuffer();
                }
                errorMessage.append(", error reading/storing concentrator " + concentratorDevice.getSerialNumber());
            }

            handleConcentrator.handleConcentratorTransaction();

            List meterList = getAllMeters();

            if (meterList.size() == 0) {
                logger.warning("Concentrator, concentrator has empty meterlist...");
            } else {
                Iterator it = meterList.iterator();
                while (it.hasNext()) {
                    handleMeter((PLCCMeterListBlocData) it.next());
                }
            }
            //handleConcentrator.handleConcentratorTransaction();

            if (errorMessage != null) {
                throw new IOException("Concentrator failed" + errorMessage);
            }
        } finally {
            disConnect();
        }

    }


    private void initFactories() {
        cosemObjectFactory = new CosemObjectFactory((ProtocolLink) this);
        objectFactory = new PLCCObjectFactory(this, cosemObjectFactory);
    }

    private void init(InputStream is, OutputStream os) throws IOException {
        // KV_TO_DO make customizable
        connection = new TCPIPConnection(is, os, getTimeout(), 0, 0, 17, 16);
    }

    static public void main(String[] args) {
        Concentrator concentrator = new Concentrator();
        try {

            concentrator.logger = Logger.getLogger("test");

            concentrator.addProperties(new Properties());
            //concentrator.getP
            concentrator.setConcentratorDevice(new MeteringWarehouseFactory().getBatch().getDeviceFactory().find(4084));
            concentrator.setScheduler((CommunicationScheduler) concentrator.getConcentratorDevice().getCommunicationSchedulers().get(0));
            //concentrator.setProfileInterval(concentrator.getConcentratorDevice().getIntervalInSeconds());
            Dialer dialer = DialerFactory.get("IPDIALER").newDialer();
            dialer.connect(concentrator.getConcentratorDevice().getPhoneNumber(), 5000);

            InputStream is = dialer.getInputStream();
            OutputStream os = dialer.getOutputStream();
            concentrator.init(is, os);

            concentrator.initFactories();
            concentrator.connect();

            System.out.println("Connected to concentrator...");

            System.out.println("Request concentrator time...");
            System.out.println(concentrator.getPLCCObjectFactory().getPLCCCurrentDateTime().getDateTime().getTime());

            //System.out.println("Concentrator initial discover...");
            //concentrator.getPLCCObjectFactory().getPLCCPLCEquipmentList().discover();            
            //concentrator.getPLCCObjectFactory().getPLCCPLCEquipmentList().initialDiscover();
            //if (true) return;  

            //System.out.println("set time on concentrator...");
            //concentrator.getPLCCObjectFactory().getPLCCCurrentDateTime().setDateTime();


            //concentrator.handleConcentrator();


            //System.out.println("concentrator get server address... returns object undefined");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCFTPServerId().getSMTPSetup().readServerAddress());

            System.out.println("Request Meterlist...");
            List meterList = concentrator.getAllMeters();
            if (meterList.size() == 0) {
                System.out.println("no meters discovered...");
                return;
            }
            Iterator it = meterList.iterator();
            while (it.hasNext()) {
                PLCCMeterListBlocData o = (PLCCMeterListBlocData) it.next();
                System.out.println("meter " + o);
                //if (o.getSerialNumber().compareTo("030790012517")==0) {

                concentrator.selectMeter(o);
                System.out.println("Request meter time...");
                System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().getDate());


                System.out.println("Request meter activity calendar...");
                ActivityCalendar a = concentrator.getPLCCObjectFactory().getPLCCMeterActivityCalendar().readActivityCalendar();
                System.out.println(a);
//            System.out.println("Write meter activity calendar...");
//            concentrator.getPLCCObjectFactory().getPLCCMeterActivityCalendar().writeActivityCalendar(a);


                //concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().setDate(new Date());

//                if ((o.getSerialNumber().compareTo("040790120115")==0)){ 
//                    System.out.println("select meter "+o); 
//                    concentrator.selectMeter(o);
//                    //break; // to select the actaris meter
//                    //System.out.println("Execute Moving Peak script ID...");
//                    //concentrator.getPLCCObjectFactory().getPLCCMeterMovingPeak().execute(1);
//                    //System.out.println(concentrator.getConcentratorRegister().readRegister(ObisCode.fromString("1.1.0.4.2.255")));
//                //System.out.println("Request meter Demand management...");
//                //com.energyict.protocolimpl.edf.messages.objects.DemandManagement demandManagement = concentrator.getPLCCObjectFactory().getPLCCMeterDemandManagement().readDemandManagement();
//                //demandManagement.setMaxloadThreshold(6000);
//                //demandManagement.setSubscribedThreshold(6000);
//                //System.out.println(demandManagement);
//                //concentrator.getPLCCObjectFactory().getPLCCMeterDemandManagement().writeDemandManagement(demandManagement);
//
//
//
//
//
//
//                    try {
//                        DeviceFactory rtuFactory = MeteringWarehouse.getCurrent().getDeviceFactory();
//                        List found = rtuFactory.findBySerialNumber(o.getSerialNumber());
//                        if (found.size()==1) {
//                            concentrator.setCurrentSelectedDevice((Device)found.get(0));
//                            System.out.println("Request LoadProfile");
//                            System.out.println(concentrator.getConcentratorProfile().getProfileData(new Date(new Date().getTime()-(3600000*10)),true,false));
//                        }
//                    }
//                    catch(IOException e) {
//                        e.printStackTrace();
//                    }
                //    }
                //PLCCMeterLoadProfileEnergy o = concentrator.getPLCCObjectFactory().getPLCCMeterLoadProfileEnergy();
                //o.writeCapturePeriod(900);

            }


            //System.out.println("Read contactor state in meter... connect true & false seems to work fine");
            //System.out.println(concentrator.getPLCCObjectFactory().getContactorState().isConnected()); 
            //concentrator.getPLCCObjectFactory().getContactorState().setConnected(false);
            //System.out.println(concentrator.getPLCCObjectFactory().getContactorState().isConnected());

            //System.out.println("Request meter time...");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().getDate());
            //concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().setDate(new Date());
            // KV_TO_DO when perform sync time?
            //concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().syncTime();
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().readMeterClock());

            //System.out.println("Set meter time...");
            //concentrator.getPLCCObjectFactory().getPLCCMeterCurrentDateTime().setDate(new Date());

            //System.out.println("Request meter status... (bit 10 & 11 conflicts with the brealker state)");
            //System.out.println("0x"+Integer.toHexString(concentrator.getPLCCObjectFactory().getPLCCMeterStatus().statusValue())); 

            //System.out.println("Request meter errorCode register...");
            //System.out.println("0x"+Long.toHexString(concentrator.getPLCCObjectFactory().getPLCCMeterErrorCodeRegister().errorCodeValue()));

//            System.out.println("Request meter activity calendar...");
//            ActivityCalendar a = concentrator.getPLCCObjectFactory().getPLCCMeterActivityCalendar().readActivityCalendar();
//            System.out.println(a);
//            System.out.println("Write meter activity calendar...");
//            concentrator.getPLCCObjectFactory().getPLCCMeterActivityCalendar().writeActivityCalendar(a);


            //System.out.println("Execute Moving Peak script ID...");
            //concentrator.getPLCCObjectFactory().getPLCCMeterMovingPeak().execute(3);

            //System.out.println("Request meter Demand management...");
            //com.energyict.protocolimpl.edf.messages.objects.DemandManagement demandManagement = concentrator.getPLCCObjectFactory().getPLCCMeterDemandManagement().readDemandManagement();
            //System.out.println(demandManagement);
            //concentrator.getPLCCObjectFactory().getPLCCMeterDemandManagement().writeDemandManagement(demandManagement);

            //System.out.println("Request getPLCCMeterIdentification");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterIdentification().toMeterIdentification());

//            System.out.println("Request getPLCCMeterTICConfiguration");
//            PLCCMeterTICConfiguration o = concentrator.getPLCCObjectFactory().getPLCCMeterTICConfiguration();
//            System.out.println("Request getPLCCMeterTICConfiguration 2");
//            System.out.println(o);
//            o.writeMode(o.getMode());

//            System.out.println("Request getPLCCMeterCurrentRatio");
//            PLCCMeterCurrentRatio o = concentrator.getPLCCObjectFactory().getPLCCMeterCurrentRatio();
//            System.out.println("Request getPLCCMeterCurrentRatio 2");
//            System.out.println(o);
            //o.writeEnergyMultiplier(o.getEnergyMultiplier());


            //System.out.println("Request getPLCCMeterEnergyRegister");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterEnergyRegister(0));
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterEnergyRegister(1));
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterEnergyRegister(2));
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterEnergyRegister(3));

            //System.out.println("Request getPLCCMeterDailyEnergyValueProfile");
//            PLCCMeterDailyEnergyValueProfile o = concentrator.getPLCCObjectFactory().getPLCCMeterDailyEnergyValueProfile(new Date(new Date().getTime()-(3600000*48)));
//            System.out.println(o.getDailyBillingEntries().get(0));
//            System.out.println(o.getDailyBillingEntries().get(1));

            //System.out.println("Request register");
//            System.out.println(concentrator.getConcentratorRegister().readRegister(ObisCode.fromString("0.0.96.5.0.255")));
//            System.out.println(concentrator.getConcentratorRegister().readRegister(ObisCode.fromString("1.0.1.8.0.255")));
//            System.out.println(concentrator.getConcentratorRegister().readRegister(ObisCode.fromString("1.0.1.8.1.VZ")));

//            System.out.println("Request LoadProfile");
//            PLCCMeterLoadProfileEnergy o = concentrator.getPLCCObjectFactory().getPLCCMeterLoadProfileEnergy();
//            System.out.println("Request LoadProfile capture period");
//            int capturePeriod = (int)o.getCapturePeriod();
//            System.out.println(capturePeriod);
//            System.out.println("Request LoadProfile set capture period");
//            o.writeCapturePeriod(capturePeriod);
//            System.out.println("Request LoadProfile");
//            System.out.println(concentrator.getConcentratorProfile().getProfileData(new Date(new Date().getTime()-(3600000*5)),true,true));

            //System.out.println("Request getPLCCMeterThresholdForSag object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterThresholdForSag());

            //System.out.println("Request getPLCCMeterThresholdForSwell object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterThresholdForSwell());

            //System.out.println("Request getPLCCMeterTimeIntegralForInstantaneousDemand object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterTimeIntegralForInstantaneousDemand());

            //System.out.println("Request getPLCCMeterTimeIntegralForSagMeasurement Object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterTimeIntegralForSagMeasurement());

            //System.out.println("Request getPLCCMeterTimeIntegralForSwellMeasurement Object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterTimeIntegralForSwellMeasurement());

            //System.out.println("Request getPLCCMeterTimeThresholdForLongPowerFailure Object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterTimeThresholdForLongPowerFailure());

            //System.out.println("Request getPLCCMeterNumberOfLongPowerFailures Object undefined");
//            System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfLongPowerFailures());

            //System.out.println("Request getPLCCMeterNumberOfShortPowerFailures");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfShortPowerFailures());

            //System.out.println("Request getPLCCMeterNumberOfSag");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfSag());

            //System.out.println("Request getPLCCMeterNumberOfSwell");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterNumberOfSwell());

            //System.out.println("Request getPLCCMeterMaximumVoltage");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterMaximumVoltage());

            //System.out.println("Request getPLCCMeterMinimumVoltage");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterMinimumVoltage());

            //System.out.println("Request getPLCCMeterInstantaneousDemand");
            //System.out.println(concentrator.getPLCCObjectFactory().getPLCCMeterInstantaneousDemand());

            dialer.disConnect();
        } catch (LinkException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                concentrator.disConnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /* connect to a concetrator */
    private void connect() throws IOException {
        connection.connectMAC();
        new Association(getPassword(), connection, getSecurityLevel());
    }

    private void disConnect() throws IOException {

    }

    public void selectMeter(PLCCMeterListBlocData meter) throws IOException {
        getPLCCObjectFactory().getPLCCSelectedMeter(meter.getSerialNumber());
    }

    private void handleMeter(PLCCMeterListBlocData meterInfo) {
        HandleMeter handleMeter = new HandleMeter(this, concentratorDevice, meterInfo, scheduler, logger);
//        try {
        // select the meter
        //selectMeter(meterInfo);
        handleMeter.handleMeterTransaction();
//        }
//        catch (IOException e) {
//            /* log & continue to next meter */
//            logger.log( Level.SEVERE, e.getMessage(), e );
//            e.printStackTrace();
//            if (errorMessage == null)
//                errorMessage = new StringBuffer();
//            errorMessage.append(", IOException handleMeter meter "+meterInfo.getSerialNumber());
//        }
        if (handleMeter.isFailed()) {
            if (errorMessage == null) {
                errorMessage = new StringBuffer();
            }
            errorMessage.append(", error reading/storing meter " + meterInfo.getSerialNumber());
        }
    }

    /* Property code */

    /* (non-Javadoc)
    * @see com.energyict.cbo.ConfigurationSupport#getOptionalKeys()
    */
    public List getOptionalKeys() {
        List result = new ArrayList();
        result.add(PK_SECURITY_LEVEL);
        result.add(PK_TIMEOUT);
        return result;
    }

    /* (non-Javadoc)
     * @see com.energyict.cbo.ConfigurationSupport#getRequiredKeys()
     */
    public List getRequiredKeys() {
        List result = new ArrayList();
        return result;
    }

    @Override
    public List<PropertySpec> getRequiredProperties() {
        return PropertySpecFactory.toPropertySpecs(getRequiredKeys());
    }

    @Override
    public List<PropertySpec> getOptionalProperties() {
        return PropertySpecFactory.toPropertySpecs(getOptionalKeys());
    }

    @Override
    public void addProperties(TypedProperties properties) {
        addProperties(properties.toStringProperties());
    }

    /* (non-Javadoc)
    * @see com.energyict.mdw.core.Pluggable#addProperties(java.util.Properties)
    */
    public void addProperties(Properties properties) {
        this.properties = properties;

        StringBuffer sb = new StringBuffer();

        Iterator i = getRequiredKeys().iterator();
        while (i.hasNext()) {
            String key = (String) i.next();
            if (!properties.containsKey(key)) {
                if (sb.length() > 0) {
                    sb.append(", ");
                }
                sb.append(key);
            }
        }

        if (sb.length() > 0) {
            throw new RuntimeException("Missing properties: " + sb);
        }

    }


    private String getPassword() {
        return concentratorDevice.getPassword();
    }

    private int getTimeout() {
        return Integer.parseInt(properties.getProperty(PK_TIMEOUT, "240000"));
    }


    private int getSecurityLevel() {
        if (properties.getProperty(PK_SECURITY_LEVEL) != null) {
            String prop = properties.getProperty(PK_SECURITY_LEVEL);
            return Integer.parseInt(prop);
        } else {
            return PD_SECURITY_LEVEL;
        }
    }

    public Logger getLogger() {
        return logger;
    }

    public TimeZone getTimeZone() {
        return concentratorDevice.getTimeZone();
    }

    public String getVersion() {
        return "$Revision: 1.17 $";
    }

    PLCCObjectFactory getPLCCObjectFactory() {
        return objectFactory;
    }

    private CommunicationProfile getCommunicationProfile() {
        return getScheduler().getCommunicationProfile();
    }

    /* return all meters connected to a concentrator */
    public List getAllMeters() throws IOException {
        return objectFactory.getPLCCMeterList().getMeterList();
    }

    private String concentratorToString(Device concentrator) {
        return "concentrator " + toString(concentrator);
    }

    private String toString(Device rtu) {
        return "[" + concentratorDevice.getId() +
                " serial=" + concentratorDevice.getSerialNumber() +
                " external name=" + concentratorDevice.getExternalName() +
                "] ";
    }

    public DLMSConnection getDLMSConnection() {
        return connection;
    }

    public DLMSMeterConfig getMeterConfig() {
        return dLMSMeterConfig;
    }

    public int getReference() {
        return 0;
    }

    public int getRoundTripCorrection() {
        return 0;
    }

    public StoredValues getStoredValues() {
        return null;
    }

    public boolean isRequestTimeZone() {
        return false;
    }

    private void setConcentratorDevice(Device concentratorDevice) {
        this.concentratorDevice = concentratorDevice;
    }

    private Device getConcentratorDevice() {
        return concentratorDevice;
    }

    public CommunicationScheduler getScheduler() {
        return scheduler;
    }

    private void setScheduler(CommunicationScheduler scheduler) {
        this.scheduler = scheduler;
    }

//    public int getProfileInterval() {
//        return profileInterval;
//    }
//
//    private void setProfileInterval(int profileInterval) {
//        this.profileInterval = profileInterval;
//    }

    public ConcentratorProfile getConcentratorProfile() {
        if (concentratorProfile == null) {
            concentratorProfile = new ConcentratorProfile(this);
        }
        return concentratorProfile;
    }

    public ConcentratorRegister getConcentratorRegister() {
        if (concentratorRegister == null) {
            concentratorRegister = new ConcentratorRegister(this);
        }
        return concentratorRegister;
    }

    public String getSelectedMeterName() {
        return "Meter";
    }

    public Device getCurrentSelectedDevice() {
        return currentSelectedDevice;
    }

    public void setCurrentSelectedDevice(Device currentSelectedDevice) {
        this.currentSelectedDevice = currentSelectedDevice;
    }

    public long getTimeDifference() {
        // TODO Auto-generated method stub
        return 0;
    }
} 
