package com.energyict.genericprotocolimpl.webrtukp.wakeup;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.Rtu;
import com.vodafone.gdsp.ws.GdspCredentials;
import com.vodafone.gdsp.ws.GdspHeader;
import com.vodafone.gdsp.ws.SubmitWUTrigger;
import com.vodafone.gdsp.ws.SubmitWUTriggerResponse;
import com.vodafone.gdsp.ws.WUTrigger;
import com.vodafone.gdsp.ws.WUTriggerService;

public class SmsWakeup {
	
	private String updatedIpAddress = "";
	private long pollTimeout;	// TODO give it a default value
	private int pollFreq;
	private boolean requestSuccess = false;
	
	private Rtu meter;
	private CommunicationScheduler scheduler;
	private Logger logger;
	
	private static String mrcRequestComplete = "000";
	private static String mrcInvalidSecurity = "005";
	private static String mrcFailedValidation = "100";
	private static String mrcProcessingError = "999";
//	private static String[] majorErrorString = new String[]{
//		"Invalid security context passed",
//		"Request has failed validation",
//		"Internal processing error. Please contact support help desk.",
//		"UNKNOWN major return code: "
//	};
//	
//	public SmsWakeup(Rtu meter) {
//		this.meter = meter;
//	}
	
	public SmsWakeup(CommunicationScheduler scheduler) {
		this(scheduler, null);
	}
	
	public SmsWakeup(CommunicationScheduler scheduler, Logger logger) {
		this.scheduler = scheduler;
		this.meter = this.scheduler.getRtu();
		this.logger = logger;
		updateProperties();
	}

	public void doWakeUp() throws SQLException, BusinessException, IOException{
		// TODO
		// perhaps clear the IP-address of the RTU
		clearMetersIpAddress();
		// make the request to Tibco
		createWakeupCall();
		// check for an updated IP-address
		waitForIpUpdate();
	}

	private void updateProperties(){
		//TODO update all meters properties and attributes!
		this.pollTimeout = Integer.parseInt(this.meter.getProperties().getProperty("PollTimeOut", "900000"));
		this.pollFreq = Integer.parseInt(this.meter.getProperties().getProperty("PollFrequency", "15000"));
	}
	
	private void clearMetersIpAddress() throws SQLException, BusinessException{
		
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
	
	private void createWakeupCall() throws IOException{
		WUTrigger wuTrigger = getWUTrigger();
		SubmitWUTrigger parameters = new SubmitWUTrigger();
		GdspHeader gdspHeader = new GdspHeader();
		GdspCredentials value = new GdspCredentials();
		value.setUserId("");
		value.setPassword("");
		gdspHeader.setGdspCredentials(value);
		parameters.setDeviceId(Long.toString(((BigDecimal)getAttributeValue("IMSI")).longValue()));
		parameters.setMSISDNNumber((String) getAttributeValue("MSISDN"));
		parameters.setOperatorName((String) getAttributeValue("Provider"));
		parameters.setSourceId(MeteringWarehouse.getCurrent().getSystemProperty("SourceId"));
		parameters.setTriggerType("SMS");
		SubmitWUTriggerResponse swuTriggerResponse = wuTrigger.submitWUTrigger(parameters, gdspHeader);
		analyseRespsonse(swuTriggerResponse);
		
	}
	
	/**
	 * Analyze the response code.
	 * Currently we only check if it is OK, otherwise exception is thrown
	 * @param swuTriggerResponse
	 * @throws IOException
	 */
	private void analyseRespsonse(SubmitWUTriggerResponse swuTriggerResponse) throws IOException {
		String majorReturnCode = swuTriggerResponse.getReturn().getReturnCode().getMajorReturnCode();
		String minorReturnCode = swuTriggerResponse.getReturn().getReturnCode().getMinorReturnCode();
		if(majorReturnCode.equalsIgnoreCase(mrcRequestComplete)){
			this.requestSuccess = false;
			this.logger.info("Successfully send the wakeup trigger.");
		} else {
			this.logger.info("Wakeup trigger failed, majorReturnCode: " + majorReturnCode + ", minorReturnCode: " + minorReturnCode);
			throw new IOException("TriggerResponse is not correct: " + majorReturnCode);
		}
	}

	private void waitForIpUpdate() throws BusinessException, IOException{
		// TODO
		// Poll the database for a filled in IPaddress of this rtu
		long protocolTimeout = System.currentTimeMillis() + this.pollTimeout;
		while(updatedIpAddress == ""){
			if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
				throw new BusinessException("Could not update the meters IP-address");
			}
			sleep(this.pollFreq);	
			updatedIpAddress = getRefreshedMeter().getIpAddress();
		}
		this.logger.info("IP-Address " + this.updatedIpAddress + " found for meter with serialnumber" + this.meter.getSerialNumber());
	}
	
	private void sleep(long sleepTime) throws IOException{
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new IOException("Interrupted while sleeping." +  e.getMessage());
		}
	}

	private Rtu getRefreshedMeter() {
		return MeteringWarehouse.getCurrent().getRtuFactory().find(this.scheduler.getRtuId());
	}

	public String getIpAddress() {
		if(this.updatedIpAddress == null){
			return "";
		} else {
			return this.updatedIpAddress;
		}
	}
	
	private Object getAttributeValue(String attribute){
		return this.meter.getDefaultRelation().get(attribute);
	}
	
	public static void main(String args[]){
		
//		SmsWakeup swu = new SmsWakeup();
//		
//		WUTrigger wuTrigger = swu.getWUTrigger();
//		SubmitWUTrigger parameters = new SubmitWUTrigger();
//		GdspHeader gdspHeader = new GdspHeader();
//		GdspCredentials value = new GdspCredentials();
//		value.setUserId("User");
//		value.setPassword("Passw");
//		gdspHeader.setGdspCredentials(value);
//		parameters.setDeviceId("The_DeviceId");
//		parameters.setMSISDNNumber("The_MSISDNNumber");
//		parameters.setOperatorName("The_OperatorName");
//		parameters.setSourceId("The_SourceId");
//		parameters.setTriggerType("The_TriggerType");
//		SubmitWUTriggerResponse swuTriggerResponse = wuTrigger.submitWUTrigger(parameters, gdspHeader);
		
//		Utilities.createEnvironment();
//		MeteringWarehouse.createBatchContext(false);
//		
//		MeteringWarehouse mw = MeteringWarehouse.getCurrent();
//		
//		Rtu rtu = mw.getRtuFactory().find(18052);
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
	
//	public DeviceInfo getDeviceInfo(){
//		DeviceInfoService dis = new DeviceInfoService();
//		return dis.getDeviceInfoPort();
//	}
	
	public WUTrigger getWUTrigger() throws IOException{
		try {
			WUTriggerService wuts = new WUTriggerService();
			return wuts.getWUTriggerPort();
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException("Just to Test");
		}
	}
	
	public boolean isRequestSuccess(){
		return this.requestSuccess;
	}

}
