package com.energyict.genericprotocolimpl.common.wakeup;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.Utils;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.sun.xml.ws.client.ClientTransportException;
import com.vodafone.gdsp.ws.*;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * <pre>
 * The SMS Wakeup will send a wakeup trigger to TIBCO and receive an OK or NOT OK as a response to the trigger.
 * Depending on the response, a polling mechanism is started to see if the meter gets an IP address, or ...
 * exception is thrown when the trigger failed or the polling timed out.
 * Changes:
 * GNA |04062009| Use the attribute GPRSProvider instead of Provider. Added a vfSoapAction, just in case this should change as well...
 * </pre>
 *
 * @author gna
 */
public class SmsWakeup {

    private int logLevel = -2;

    private String updatedIpAddress = "";
    private long pollTimeout;
    private int pollFreq;
    private boolean requestSuccess = false;
    private String endpointAddress;
    private String soapAction;

    private Rtu meter;
    private CommunicationScheduler scheduler;
    private Logger logger;

    private static String mrcRequestComplete = "000";
    private static String mrcInvalidSecurity = "005";
    private static String mrcFailedValidation = "100";
    private static String mrcProcessingError = "999";

    private static String NOTIFY_NOT_CONNECTED = "NotConnected";
    private static String NOTIFY_COMMON_ERROR = "ComError";
    private static String NOTIFY_DUPLICATE_IMSI = "Duplicate";

    public SmsWakeup(CommunicationScheduler scheduler) {
        this(scheduler, null);
    }

    /**
     * Constructor
     *
     * @param scheduler
     * @param logger
     */
    public SmsWakeup(CommunicationScheduler scheduler, Logger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
		if(this.scheduler != null){
            this.meter = this.scheduler.getRtu();
            updateProperties();
        }
    }

    /**
     * Triggers the wakeup
     *
     * @throws SQLException      if we couldn't clear the IP-address
     * @throws BusinessException if a business error occurred
     * @throws IOException       if parameters aren't correctly configured, when WSDL isn't found or when interrupted while sleeping
     */
    public void doWakeUp() throws SQLException, BusinessException, IOException {
        clearMetersIpAddress();
        log(5, "Cleared IP");
        // make the request to Tibco
		createWakeupCall();
        // check for an updated IP-address
        waitForIpUpdate();
    }

    /**
     * Set some polling properties
     */
	private void updateProperties(){
        this.pollTimeout = Integer.parseInt(this.meter.getProperties().getProperty("PollTimeOut", "900000"));
        this.pollFreq = Integer.parseInt(this.meter.getProperties().getProperty("PollFrequency", "15000"));
        String host = mw().getSystemProperty("vfEndpointAddress");
		if(host == null){
            endpointAddress = "http://localhost:4423/SharedResources/COMM_DEVICE/WUTriggerService.serviceagent/WUTriggerPort";
        } else {
            endpointAddress = host;
        }
        String action = mw().getSystemProperty("vfSoapAction");
        if (action == null) {
            soapAction = "/SharedResources/COMM_DEVICE/WUTriggerService.serviceagent/WUTriggerPort/submitWUTrigger";
        } else {
            soapAction = action;
        }
    }

    /**
     * Clear the IP address field of the meter so we can poll on a filled in address
     *
     * @throws SQLException      if a database error occurred
     * @throws BusinessException if a business error occurred
     */
    private void clearMetersIpAddress() throws SQLException, BusinessException {

        try {
            this.meter.updateIpAddress("");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException("Could not clear the IP address.");
        } catch (BusinessException e) {
            e.printStackTrace();
            throw new BusinessException("Failed to clear the IP address.", e.getMessage());
        }

    }

    /**
     * Create the actual call.
     * Find all necessary parameters on the device, including his attributes and send the trigger.
     * Afterwards, check the response to see if the SMS will be sent to the device so we can start polling the IP-Address
     *
     * @throws IOException when the wsdl is not found, or when certain attributes are not correctly filled in
     */
	private void createWakeupCall() throws IOException{
        log(5, "In createWakeupCall");
        WUTrigger wuTrigger = getWUTrigger();
        log(5, "Got wuTriggerPort");
        SubmitWUTrigger parameters = new SubmitWUTrigger();
        GdspHeader gdspHeader = new GdspHeader();
        GdspCredentials value = new GdspCredentials();
        value.setUserId("");
        value.setPassword("");
        gdspHeader.setGdspCredentials(value);
        BigDecimal bd = (BigDecimal) getAttributeValue("IMSI");
        if (bd != null) {
            parameters.setDeviceId(Long.toString((bd).longValue()));
        } else {
            throw new IOException("The IMSI number is a required attribute to successfully execute the wakeup trigger.");
        }
        String MSISDN = (String) getAttributeValue("MSISDN");
        if (MSISDN != null) {
            parameters.setMSISDNNumber(MSISDN);
        }
        String provider = (String) getAttributeValue("GPRSProvider");
        if (provider != null) {
            parameters.setOperatorName(provider);
        } else {
            throw new IOException("The Provider is a required attribute to successfully execute the wakeup trigger.");
        }
        String srcId = mw().getSystemProperty("SourceId");
        if (srcId != null) {
            parameters.setSourceId(srcId);
        } else {
            throw new IOException("The SourceId is a required System property to successfully execute the wakeup trigger.");
        }

        /** If in time CSD is added to this functionality, then make sure this one is fetched from the attributes and is correctly filled in*/
        parameters.setTriggerType("SMS");

        log(5, "Ready for takeoff");
        SubmitWUTriggerResponse swuTriggerResponse;
        try {
            swuTriggerResponse = wuTrigger.submitWUTrigger(parameters, gdspHeader);
            log(5, "Took off ...");
            analyseRespsonse(swuTriggerResponse);
        } catch (ClientTransportException e) {
            e.printStackTrace();
            throw new ConnectionException("Connection refused, please check if the endpointAddress is correct." + e.getMessage());
        }

    }

    /**
     * Analyze the response code.
     * Currently we only check if it is OK, otherwise exception is thrown
     *
     * @param swuTriggerResponse
     * @throws IOException
     */
    private void analyseRespsonse(SubmitWUTriggerResponse swuTriggerResponse) throws ConnectionException {
        String majorReturnCode = swuTriggerResponse.getReturn().getReturnCode().getMajorReturnCode();
        String minorReturnCode = swuTriggerResponse.getReturn().getReturnCode().getMinorReturnCode();
        if (majorReturnCode.equalsIgnoreCase(mrcRequestComplete)) {
            this.requestSuccess = true;
            this.logger.info("Successfully sent the wakeup trigger.");
        } else {
            this.requestSuccess = false;
            this.logger.info("Wakeup trigger failed, majorReturnCode: " + majorReturnCode + ", minorReturnCode: " + minorReturnCode);
            throw new ConnectionException("TriggerResponse is not as excpected, MajorReturnCode: " + majorReturnCode + ", MinorReturnCode: " + minorReturnCode);
        }
    }

    /**
     * Poll the meters IP-address field for an update
     *
     * @throws BusinessException if there isn't an updated IP-address
     * @throws IOException       during the sleep
     */
    private void waitForIpUpdate() throws BusinessException, IOException {
        long protocolTimeout = System.currentTimeMillis() + this.pollTimeout;
        while (updatedIpAddress == "") {
            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new BusinessException("Could not update the meters IP-address");
            }
            sleep(this.pollFreq);
            updatedIpAddress = getRefreshedMeter().getIpAddress();
        }
        String error = "";
        if (NOTIFY_COMMON_ERROR.equalsIgnoreCase(updatedIpAddress)) {
            error = "Device with serialNumber " + this.meter.getSerialNumber() + " has an unknown connect state.";
            this.logger.info(error);
            throw new BusinessException(error);
        } else if (NOTIFY_NOT_CONNECTED.equalsIgnoreCase(updatedIpAddress)) {
            error = "Device with serialNumber " + this.meter.getSerialNumber() + " is not connected.";
            this.logger.info(error);
            throw new BusinessException(error);
        } else if (NOTIFY_DUPLICATE_IMSI.equalsIgnoreCase(updatedIpAddress)) {
            error = "There are multiple RTU's defined with the same IMSI number of device " + this.meter.getSerialNumber();
            this.logger.info(error);
            throw new BusinessException(error);
        }


        this.logger.info("IP-Address " + this.updatedIpAddress + " found for meter with serialnumber" + this.meter.getSerialNumber());
    }

    /**
     * Hold the thread for the given sleeptime
     *
     * @param sleepTime
     * @throws IOException when we get interrupted while sleeping
     */
    private void sleep(long sleepTime) throws IOException {

        ProtocolTools.delay(sleepTime);
        /*
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IOException("Interrupted while sleeping." +  e.getMessage());
		}*/
    }

    /**
     * Get the updated meter from the database
     *
     * @return
     */
    private Rtu getRefreshedMeter() {
        return MeteringWarehouse.getCurrent().getRtuFactory().find(this.scheduler.getRtuId());
    }

    /**
     * @return the local ipaddress String
     */
    public String getIpAddress() {
        if (this.updatedIpAddress == null) {
            return "";
        } else {
            return this.updatedIpAddress;
        }
    }

    /**
     * Return the attribute with the given name, if an attribute doesn't exist then NULL is returned
     *
     * @param attribute
     * @return
     */
    private Object getAttributeValue(String attribute) {
        return this.meter.getDefaultRelation().get(attribute);
    }

    public WUTrigger getWUTrigger() throws IOException {
        WUTriggerService wuService = new WUTriggerService(Utils.class.getResource("/wsdl/WUTriggerServiceConcrete_Incl_Schema.wsdl"),
                new QName("http://ws.gdsp.vodafone.com/", "WUTriggerService"));
        WUTrigger proxy = wuService.getWUTriggerPort();
        ((BindingProvider) proxy).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
        ((BindingProvider) proxy).getRequestContext().put(BindingProvider.SOAPACTION_URI_PROPERTY, soapAction);

        return proxy;

    }

    public boolean isRequestSuccess() {
        return this.requestSuccess;
    }

    private void log(int level, String msg) {
        if (this.logLevel == -2) {
            this.logLevel = Integer.parseInt(this.meter.getProperties().getProperty("TestLogging", "-1"));
        }
        if (level <= this.logLevel) {
            this.logger.info(msg);
        }
    }

    private MeteringWarehouse mw() {
        return MeteringWarehouse.getCurrent();
    }

    public static void main(String args[]) {

        SmsWakeup swu = new SmsWakeup(null);

        try {
            WUTrigger wuTrigger = swu.getWUTrigger();
            SubmitWUTrigger parameters = new SubmitWUTrigger();
            GdspHeader gdspHeader = new GdspHeader();
            GdspCredentials value = new GdspCredentials();
            value.setUserId("User");
            value.setPassword("Passw");
            gdspHeader.setGdspCredentials(value);
            parameters.setDeviceId("The_DeviceId");
            parameters.setMSISDNNumber("The_MSISDNNumber");
            parameters.setOperatorName("The_OperatorName");
            parameters.setSourceId("The_SourceId");
            parameters.setTriggerType("The_TriggerType");
            SubmitWUTriggerResponse swuTriggerResponse = wuTrigger.submitWUTrigger(parameters, gdspHeader);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

//		Utilities.createEnvironment();
//		MeteringWarehouse.createBatchContext(false);
//		MeteringWarehouse mw = MeteringWarehouse.getCurrent();
//		Rtu rtu = mw.getRtuFactory().find(18052);
//		String str = (String)rtu.getDefaultRelation().get("Gov");
//		System.out.println(str);


////		MdwAttributeType mat = rtu.getDefaultRelationType().getAttributeType("IMSI");
////			rtu.getDefaultRelation().get("IMSI");
//		
//		SmsWakeup smsWakeup = new SmsWakeup((CommunicationScheduler) rtu.getCommunicationSchedulers().get(9));
//		try {
//			smsWakeup.waitForIpUpdate();
//		} catch (BusinessException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		};
	}
}
