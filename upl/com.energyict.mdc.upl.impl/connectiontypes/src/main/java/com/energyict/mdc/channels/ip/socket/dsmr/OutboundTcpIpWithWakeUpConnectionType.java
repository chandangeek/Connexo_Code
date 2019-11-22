package com.energyict.mdc.channels.ip.socket.dsmr;


import com.energyict.mdc.channels.ip.socket.OutboundTcpIpConnectionType;
import com.energyict.mdc.channels.nls.MessageSeeds;
import com.energyict.mdc.channels.nls.PropertyTranslationKeys;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.Services;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocolimpl.properties.UPLPropertySpecFactory;
import com.energyict.protocolimplv2.messages.nls.Thesaurus;
import com.vodafone.gdsp.ws.*;
import org.osgi.framework.FrameworkUtil;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.rmi.RemoteException;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Semaphore;
import java.util.logging.*;


@XmlRootElement
public class OutboundTcpIpWithWakeUpConnectionType extends OutboundTcpIpConnectionType {

    private static final int semaphorePermits;

    public static final String SIM_CARD_PROPERTY_ICCID = "iccid";
    public static final String SIM_CARD_PROPERTY_ACTIVE_IMSI = "activeIMSI";
    public static final String SIM_CARD_PROPERTY_PROVIDER = "provider";


    public static final String DEVICE_PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String DEVICE_PROPERTY_MRID = "mrID";
    public static final String CONNECTION_TASK_PROPERTY_ID = "connectionTask.id";
    public static final String CONNECTION_TASK_PROPERTY_NAME = "connectionTask.name";
    public static final String WAITING_FOR_WAKEUP_TEMPORARY_HOST_VALUE = "[waiting for wakeup...]";

    static {
        semaphorePermits =  getSystemProperty("com.energyict.mdc.vodafone.ws.wakeup.semaphore.maxpermits", 25);
    }

    private static final Semaphore semaphore = new Semaphore(semaphorePermits, true);

    private static String mrcRequestComplete = "000";
    private static String mrcInvalidSecurity = "005";
    private static String mrcFailedValidation = "100";
    private static String mrcProcessingError = "999";


    public static final String PROPERTY_ENDPOINT_ADDRESS = "endpointAddress";
    public static final String PROPERTY_SOAP_ACTION = "soapAction";
    public static final String PROPERTY_WS_CONNECT_TIME_OUT = "wsConnectTimeOut";
    public static final String PROPERTY_WS_REQUEST_TIME_OUT = "wsRequestTimeOut";
    public static final String PROPERTY_WAITING_TIME = "waitingTime";
    public static final String PROPERTY_POOL_RETRIES = "poolRetries";
    public static final String PROPERTY_SOURCE_ID = "sourceId";
    public static final String PROPERTY_TRIGGER_TYPE = "triggerType";
    public static final String PROPERTY_USER_ID = "wsUserId";
    public static final String PROPERTY_USER_PASS = "wsUserPass";


    // Need use string here so we don't need a dependency to other freaking module somewhere in the cloud ...
    public static final String OUR_PROPERTY_SET = "com.energyict.protocols.impl.channels.ip.socket.dsmr.OutboundTcpIpWithWakeupConnectionProperties";
    private String mrid = null;
    private Long connTaskId = null;

    private java.util.logging.Logger logger;

    public OutboundTcpIpWithWakeUpConnectionType(PropertySpecService propertySpecService) {
        super(propertySpecService);
    }

    @Override
    public String getVersion() {
        return "2019-09-09";
    }

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(endpointAddressPropertySpec());
        propertySpecs.add(soapActionPropertySpec());
        propertySpecs.add(requestTimeOutPropertySpec());
        propertySpecs.add(sourceIdPropertySpec());
        propertySpecs.add(triggerTypePropertySpec());
        propertySpecs.add(userIdPropertySpec());
        propertySpecs.add(passwordPropertySpec());
        propertySpecs.add(waitingTimePropertySpec());
        propertySpecs.add(numberOfPoolRetriesPropertySpec());
        propertySpecs.add(connectTimeOutPropertySpec());
        return propertySpecs;
    }


    @Override
    public ComChannel connect() throws ConnectionException {
        resetHostProperty();
        doWakeUp();

        waitBeforeMonitoring();

        Optional<String> newHost = monitorHostProperty();

        if (newHost.isPresent()){
            int port = this.portNumberPropertyValue();
            int timeout = this.connectionTimeOutPropertyValue();

            log("Creating a new TcpIpConnection to " + newHost.get() + ":" + port +" with a timeout of "+timeout+" ms");
            ComChannel comChannel = this.newTcpIpConnection(newHost.get(), port, timeout);
            log("ComChannel created, wakeup successful!" );
            return comChannel;
        }

        throw new ConnectionException(Thesaurus.ID.toString(),
                MessageSeeds.WakeupCallFailed,
                "New host address was not filled-in by the IP notification service within expected waiting time.");
    }

    private void waitBeforeMonitoring() {
        Duration waitingTime = getPropertyWaitingTime();
        log("Sleeping "+waitingTime.getSeconds()+" seconds before pooling host property");
        try {
            Thread.sleep(waitingTime.toMillis());
        } catch (InterruptedException e) {
            logError("Sleep interrupted, exiting! ("+e.getLocalizedMessage()+")");
        }
    }

    private Optional<String> monitorHostProperty() {

        Duration connectTimeout = getPropertyConnectTimeout();
        int poolRetries = getPropertyNumberOfPoolRetries().intValue();

        log("Monitoring the host property, waiting for the IP notification web-service to fill it: "
                +poolRetries+" tries x "+connectTimeout.toMillis()+" milliseconds ...");

        while (poolRetries > 0){
            Optional<String> host = getUpdatedHostProperty();
            if (host.isPresent()){
                if (WAITING_FOR_WAKEUP_TEMPORARY_HOST_VALUE.equals(host.get())){
                    try {
                        Thread.sleep(connectTimeout.toMillis());
                        poolRetries--;
                        log("\t host still not updated, waiting another "+poolRetries+" retries");
                    } catch (InterruptedException e) {
                        logError("Waiting for host property to be updated interrupted, exiting! ("+e.getLocalizedMessage()+")");
                        return Optional.empty();
                    }
                } else {
                    log("Detected new host value: "+host.get()+" resuming with TCP/IP connection");
                    return host;
                }
            }
        }

        log("No updated host value detected after waiting timeout, exiting ");
        return Optional.empty();
    }

    private Optional<String> getUpdatedHostProperty() {
        Optional<Map<String, Object>> currentProperties = Services.devicePropertiesDelegate()
                .getConnectionMethodProperties(
                        OUR_PROPERTY_SET,
                        getDevicePropertyMRID(),
                        getConnectionTaskPropertyId());
        if (currentProperties.isPresent()){
            return Optional.of(  currentProperties.get().get(HOST_PROPERTY_NAME).toString()  );
        }
        return Optional.empty();
    }


    private void resetHostProperty() {
        log("Resetting host property ...");

        try {
            Services.devicePropertiesDelegate()
                    .setConnectionMethodProperty(
                            HOST_PROPERTY_NAME,
                            WAITING_FOR_WAKEUP_TEMPORARY_HOST_VALUE,
                            OUR_PROPERTY_SET,
                            getDevicePropertyMRID(),
                            getConnectionTaskPropertyId());
        } catch (Exception ex) {
            logError(ex);
        }
    }


    /**
     * The following properties are transported to allow identificatin of device and connectiontask in MDC
     * They are transported by raw value because of fancy class dependencies between bundles.
     *
     * Everything retreived here is set in
     *      com.energyict.mdc.device.data.impl.tasks.ScheduledConnectionTaskImpl#getDeviceIdentificationProperties()
     */

    private Long getConnectionTaskPropertyId(){
        if (this.connTaskId == null){
            // this is pooled while monitoring, do a small cache to improve performance
            this.connTaskId = (Long)getProperty(CONNECTION_TASK_PROPERTY_ID);
        }
        return this.connTaskId;
    }

    private String getDevicePropertyMRID() {
        if (this.mrid == null){
            // this is pooled while monitoring, do a small cache to improve performance
            this.mrid =getProperty(DEVICE_PROPERTY_MRID).toString();
        }
        return this.mrid;
    }

    private String getConnectionTaskPropertyName(){
        return (String)getProperty(CONNECTION_TASK_PROPERTY_NAME);
    }

    private String getDevicePropertySerialNumber() {
        return getProperty(DEVICE_PROPERTY_SERIAL_NUMBER).toString();
    }


    /**
     * Triggers the wakeup
     *
     */
    public void doWakeUp() throws ConnectionException {
        try {
            // get a lock on the semaphore
            log("Request semaphore lock at " + Calendar.getInstance().getTime());
            semaphore.acquire();
            try {
                log("Got semaphore lock at " + Calendar.getInstance().getTime());

                createWakeupCall();

            } catch (ConnectionException ex){
                throw ex;
            } catch (Exception ex) {
                logError(ex);
                throw new ConnectionException(Thesaurus.ID.toString(),
                        MessageSeeds.WakeupCallFailed,
                        ex.getMessage());
            } finally {
                semaphore.release();
                log("Released semaphore lock at " + Calendar.getInstance().getTime());
            }
        } catch (InterruptedException e) {
            logError(e);
            throw new ConnectionException(Thesaurus.ID.toString(),
                    MessageSeeds.WakeupCallFailed,
                    "Interrupted while waiting for a lock on the SmsWakeup thread");
        }
    }



    /**
     * Create the actual call.
     * Find all necessary parameters on the device, including his attributes and send the trigger.
     * Afterwards, check the response to see if the SMS will be sent to the device so we can start polling the IP-Address
     *
     * @throws IOException when the wsdl is not found, or when certain attributes are not correctly filled in
     */
    private void createWakeupCall() throws ConnectionException {
        log("Creating WebService wake-up call");
        WUTrigger wuTrigger = null;
        try {
            wuTrigger = getWUTrigger();
        } catch (ServiceException e) {
            logError(e);
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.WakeupCallFailed, e.getMessage());
        }

        log("\t- building parameters & header");
        SubmitWUTrigger parameters = getParameters();
        GdspHeader gdspHeader = getGdspHeader();

        log("\t- submitting WU trigger");
        SubmitWUTriggerResponse swuTriggerResponse;
        try {
            swuTriggerResponse = wuTrigger.submitWUTrigger(parameters, gdspHeader);
        } catch (RemoteException e) {
            logError(e);
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.WakeupCallFailed, e.getMessage());
        }

        log("\t- response received, analyzing it");

        analyseResponse(swuTriggerResponse);
    }


    public WUTrigger getWUTrigger() throws ServiceException, ConnectionException {
        QName qName = new QName("http://ws.gdsp.vodafone.com/", "WUTriggerService");
        String wsdlSchema = getResource("wsdl/WUTriggerServiceConcrete_Incl_Schema.wsdl");

        log("\t- service: "+qName.getNamespaceURI());
        WUTriggerService wuService = new WUTriggerServiceLocator(wsdlSchema,qName);
        WUTrigger wuTriggerPort = wuService.getWUTriggerPort();

        WUTriggerPortBindingStub stub = (WUTriggerPortBindingStub) wuTriggerPort;
        WUTriggerServiceLocator serviceLocator = (WUTriggerServiceLocator) stub._getService();

        String serviceEndpoint = getPropertyEndpointAddress() + getPropertySoapAction();
        log("\t- endpoint: "+serviceEndpoint);

        serviceLocator.setWUTriggerPortEndpointAddress(serviceEndpoint);

        int timeoutMilliseconds = (int) getPropertyRequestTimeout().toMillis();
        stub.setTimeout(timeoutMilliseconds);
        log("\t- request timeout: "+timeoutMilliseconds+" milliseconds");

        //need also this for cached endpoint
        ((WUTriggerPortBindingStub) wuTriggerPort)._setProperty("javax.xml.rpc.service.endpoint.address", serviceEndpoint );

        return wuTriggerPort;
    }

    private String getResource(String resourcePath) throws ConnectionException {
        try {
            URL resourceUrl = OutboundTcpIpWithWakeUpConnectionType.class.getClassLoader().getResource(resourcePath);
            return resourceUrl.toString();
        } catch (Exception e){
            logError("Cannot load system resource ["+resourcePath+"]");
            logError(e);
            throw new ConnectionException(Thesaurus.ID.toString(), MessageSeeds.WakeupCallFailed, e.getMessage());
        }
    }


    private SubmitWUTrigger getParameters() throws ConnectionException {
        SubmitWUTrigger parameters = new SubmitWUTrigger();

        String provider = getDeviceCasGprsProvider();
        parameters.setOperatorName(provider);
        log("\t\t- provider: "+provider);

        /**
         * Vodafone = IMSI
         * Others = ICCID
         */
        String deviceId;
        if ("Vodafone".equals(provider)){
            String imsi = getDeviceCasIMSI();
            log("\t\t- operator is Vodafone, using IMSI: "+imsi+" as deviceId");
            deviceId = imsi;
        } else {
            String iccid = getDeviceCasICCID();
            log("\t\t- other operator using ICCID: "+iccid+" as deviceId");
            deviceId = iccid;
        }
        parameters.setDeviceId(deviceId);

        /**
         * Optional, do not fill this
         */
        //String msisdn =  getDeviceCasICCID();
        //parameters.setMSISDNNumber("");

        String sourceId =  getPropertySourceId();
        parameters.setSourceId(sourceId);
        log("\t\t- sourceId: "+sourceId);

        String triggerType =  getPropertyTriggerType();
        parameters.setTriggerType(triggerType);
        log("\t\t- triggerType: "+triggerType);

        return parameters;
    }

    private GdspHeader getGdspHeader() {
        GdspHeader gdspHeader = new GdspHeader();
        GdspCredentials value = new GdspCredentials();

        String userId = getPropertyUserId();
        value.setUserId(userId);
        log("\t\t- userId: "+userId);

        String userPass = getPropertyUserPass();
        value.setPassword(userPass);
        log("\t\t- userPass: "+userPass.replaceAll("(?s).", "*"));

        gdspHeader.setGdspCredentials(value);
        return gdspHeader;
    }


    /**
     * Analyze the response code.
     * Currently we only check if it is OK, otherwise exception is thrown
     *
     * @param swuTriggerResponse
     */
    private void analyseResponse(SubmitWUTriggerResponse swuTriggerResponse) throws ConnectionException {
        String majorReturnCode = swuTriggerResponse.get_return().getReturnCode().getMajorReturnCode();
        String minorReturnCode = swuTriggerResponse.get_return().getReturnCode().getMinorReturnCode();

        log(" - response major=["+majorReturnCode+"] minor=["+minorReturnCode+"]");

        if (majorReturnCode.equalsIgnoreCase(mrcRequestComplete)) {
            log("Wakeup trigger was sent successfully");
            // great success
        } else {
            logError("Wakeup trigger was not accepted!");
            throw new ConnectionException(Thesaurus.ID.toString(),
                    MessageSeeds.WakeupCallFailed,
                    "majorReturnCode="+majorReturnCode+", minorReturnCode="+minorReturnCode);
        }
    }





    private PropertySpec endpointAddressPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_ENDPOINT_ADDRESS, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_ENDPOINT_ADDRESS, getPropertySpecService()::stringSpec)
                .setDefaultValue("http://...")
                .finish();
    }

    private String getPropertyEndpointAddress(){
        return (String)getProperty(PROPERTY_ENDPOINT_ADDRESS);
    }


    private PropertySpec soapActionPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_SOAP_ACTION, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_SOAP_ACTION, getPropertySpecService()::stringSpec)
                .setDefaultValue("/.../submitWUTrigger")
                .finish();
    }

    private String getPropertySoapAction(){
        return (String)getProperty(PROPERTY_SOAP_ACTION);
    }


    private PropertySpec requestTimeOutPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_WS_REQUEST_TIME_OUT, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_REQUEST_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(Duration.ofMillis(4500))
                .finish();
    }

    private Duration getPropertyRequestTimeout(){
        return (Duration)getProperty(PROPERTY_WS_REQUEST_TIME_OUT);
    }

    private PropertySpec numberOfPoolRetriesPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_POOL_RETRIES, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_POOL_RETRIES, getPropertySpecService()::bigDecimalSpec)
                .setDefaultValue(BigDecimal.valueOf(20))
                .finish();
    }

    private BigDecimal getPropertyNumberOfPoolRetries(){
        return (BigDecimal) getProperty(PROPERTY_POOL_RETRIES);
    }

    private PropertySpec waitingTimePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_WAITING_TIME, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_WAITING_TIME, getPropertySpecService()::durationSpec)
                .setDefaultValue(Duration.ofSeconds(25))
                .finish();
    }

    private Duration getPropertyWaitingTime(){
        return (Duration)getProperty(PROPERTY_WAITING_TIME);
    }

    private PropertySpec connectTimeOutPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_WS_CONNECT_TIME_OUT, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_CONNECT_TIMEOUT, getPropertySpecService()::durationSpec)
                .setDefaultValue(Duration.ofSeconds(5))
                .finish();
    }



    private Duration getPropertyConnectTimeout(){
        return (Duration)getProperty(PROPERTY_WS_CONNECT_TIME_OUT);
    }

    private PropertySpec sourceIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_SOURCE_ID, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_GPRS_SOURCE_ID, getPropertySpecService()::stringSpec)
                .setDefaultValue("")
                .finish();
    }

    private String getPropertySourceId(){
        return (String)getProperty(PROPERTY_SOURCE_ID);
    }


    private PropertySpec triggerTypePropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_TRIGGER_TYPE, true, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_GPRS_TRIGGER_TYPE, getPropertySpecService()::stringSpec)
                .setDefaultValue("SMS")
                .finish();
    }

    private String getPropertyTriggerType(){
        return (String)getProperty(PROPERTY_TRIGGER_TYPE);
    }


    private PropertySpec userIdPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_USER_ID, false, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_GPRS_USER_ID, getPropertySpecService()::stringSpec).finish();
    }

    private String getPropertyUserId(){
        Object prop = getProperty(PROPERTY_USER_ID);
        if (prop!=null){
            return (String) prop;
        }
        return "";
    }


    private PropertySpec passwordPropertySpec() {
        return UPLPropertySpecFactory.specBuilder(PROPERTY_USER_PASS, false, PropertyTranslationKeys.OUTBOUND_IP_WAKEUP_GPRS_USER_PASS, getPropertySpecService()::stringSpec).finish();
    }


    private String getPropertyUserPass(){
        Object prop = getProperty(PROPERTY_USER_PASS);
        if (prop!=null){
            return (String) prop;
        }
        return "";
    }

    private String getPropertyWithValidator(String propertyName) throws ConnectionException {
        try {
            String value = (String) getProperty(propertyName, null);
            return value;
        } catch (Exception ex){
            String errorMessage = "Cannot get value of custom property ["+propertyName+"]";
            logError(errorMessage);
            logError(ex);
            throw new ConnectionException(Thesaurus.ID.toString(),
                    MessageSeeds.WakeupCallFailed,
                    errorMessage);
        }
    }

    private String getDeviceCasIMSI() throws ConnectionException {
        return getPropertyWithValidator(SIM_CARD_PROPERTY_ACTIVE_IMSI);
    }

    private String getDeviceCasICCID() throws ConnectionException {
        return getPropertyWithValidator(SIM_CARD_PROPERTY_ICCID);
    }

    private String getDeviceCasGprsProvider() throws ConnectionException {
        return getPropertyWithValidator(SIM_CARD_PROPERTY_PROVIDER);
    }

    private static int getSystemProperty(String propertyName, int defaultValue) {
        int value = defaultValue;

        try {
            String property = FrameworkUtil.getBundle(OutboundTcpIpWithWakeUpConnectionType.class).getBundleContext().getProperty(propertyName);

            if (property!=null){
                try {
                    value = Integer.parseInt(property);
                }catch (Exception ex){
                    System.err.println("[Wakeup]: System configuration property ["+propertyName+"] could not be parsed, using default value of "+defaultValue);
                }
            } else {
                System.err.println("[Wakeup]: System configuration property ["+propertyName+"] is not defined, using default value of "+defaultValue);
            }
        } catch (Exception ex){
            System.err.println("[Wakeup]: Cannot get system configuration property ["+propertyName+"] using default value of "+defaultValue+": "+ex.getLocalizedMessage());
        }
        return value;
    }


    /**
     * Session identifier, used in logging to distinguish between tasks
     *
     * @return
     */
    private String getSession() {
        return "{"+getDevicePropertyMRID()+"}.{"+getConnectionTaskPropertyName()+"} ";
    }

    /**
     * Log messages to to give the poor delivery & support people some hints in case of trouble
     * TODO: find a magic way to connect to journal, create an service, etc
     */
    private Logger getLogger() {
        if (this.logger==null) {
            this.logger = Logger.getLogger(this.getClass().getName());
        }
        return this.logger;
    }

    private void logError(String errorMessage) {
        getLogger().severe("[Wakeup]" + getSession() + errorMessage);
    }

    private void logError(Exception ex) {
        logError( ex.getLocalizedMessage()+ "\n"+ex.toString());
    }

    private void log(String message) {
        getLogger().info("[Wakeup]" + getSession() + message);
    }


}