/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.dynamic.PropertySpecService;
import com.energyict.mdc.protocol.api.InvalidPropertyException;
import com.energyict.mdc.protocol.api.MissingPropertyException;
import com.energyict.mdc.protocol.api.UnsupportedException;
import com.energyict.mdc.protocol.api.device.data.ProfileData;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.HalfDuplexController;
import com.energyict.mdc.protocol.api.legacy.MeterProtocol;
import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.trimaran2p.core.TrimaranObjectFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.APSEParameters;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.Connection62056;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author gna
 *
 */
public class Trimaran2P extends AbstractProtocol implements ProtocolLink{

	@Override
	public String getProtocolDescription() {
		return "EDF Trimaran 2P";
	}

	private APSEPDUFactory aPSEFactory;
	private APSEParameters aPSEParameters;
	private DLMSPDUFactory dLMSPDUFactory;
	private Connection62056 connection62056;
	private Trimaran2PProfile trimaran2PProfile;
	private TrimaranObjectFactory trimaranObjectFactory;
	private RegisterFactory registerFactory = null;

    private int sourceTransportAddress;
    private int destinationTransportAddress;
    private int delayAfterConnect;
    private int t1Timeout;

    private long roundTripStart;

    private String meterVersion;

	@Inject
	public Trimaran2P(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	protected void doConnect() throws IOException {
		getAPSEFactory().getAuthenticationReqAPSE();
		getDLMSPDUFactory().getInitiateRequest();
		if(getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].getResources().indexOf("TEC") != -1){
			setMeterVersion("TEC");
		} else if(getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].getResources().indexOf("TEP") != -1) {
			setMeterVersion("TEP");
		}
		getLogger().info(getDLMSPDUFactory().getStatusResponse().toString());
	}

	protected void doDisConnect() throws IOException {

	}

	public ProfileData getProfileData(Date lastReading, Date to, boolean includeEvents) throws IOException{
		return getTrimaran2PProfile().getProfileData(lastReading, to);
	}

	protected void validateSerialNumber() throws IOException{
		if((getInfoTypeSerialNumber() == null) || ("".compareTo(getInfoTypeSerialNumber()) == 0)) {
			return;
		}

		String serialNumber = getDLMSPDUFactory().getStatusResponse().getSerialNumber();
		if(serialNumber.compareTo(getInfoTypeSerialNumber()) == 0) {
			return;
		}

		throw new IOException("SerialNumber mismatch! Meter serialNumber = "+serialNumber+", configured serialNumber = "+getInfoTypeSerialNumber());
	}

	protected List doGetOptionalKeys() {
        List list = new ArrayList(7);
        list.add("T1Timeout");
        list.add("STSAP");
        list.add("DTSAP");
        list.add("ClientType");
        list.add("CallingPhysicalAddress");
        list.add("ProposedAppCtxName");
        list.add("DelayAfterConnect");
		return list;
	}

	public ProtocolConnection doInit(InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) throws IOException {

		setAPSEFactory(new APSEPDUFactory(this, getAPSEParameters()));
		setDLMSPDUFactory(new DLMSPDUFactory(this));
		setTrimaranObjectFactory(new TrimaranObjectFactory(this));
		setConnection62056(new Connection62056(inputStream, outputStream, timeoutProperty, protocolRetriesProperty, forcedDelay, echoCancelling, halfDuplexController, getInfoTypeSerialNumber(),getInfoTypeSecurityLevel(),getInfoTypeHalfDuplex(),getT1Timeout(), getSourceTransportAddress(), getDestinationTransportAddress(), getDelayAfterConnect()));
		getConnection62056().initProtocolLayers();
		trimaran2PProfile = new Trimaran2PProfile(this);
		return getTrimaran2PConnection();
	}

	private Connection62056 getTrimaran2PConnection() {
		return getConnection62056();
	}

	public void setTrimaran2PConnection(Connection62056 connection62056){
		setConnection62056(connection62056);
	}

	private int getSourceTransportAddress() {
		return sourceTransportAddress;
	}

	private int getT1Timeout() {
		return t1Timeout;
	}

	private int getDestinationTransportAddress() {
		return destinationTransportAddress;
	}

	private int getDelayAfterConnect() {
		return delayAfterConnect;
	}

	public void doValidateProperties(Properties properties) throws MissingPropertyException, InvalidPropertyException {
		setT1Timeout(Integer.parseInt(properties.getProperty("T1Timeout","5000").trim()));
		setSourceTransportAddress(Integer.parseInt(properties.getProperty("STSAP","0").trim()));
		setDestinationTransportAddress(Integer.parseInt(properties.getProperty("DTSAP","2").trim()));

		setAPSEParameters(new APSEParameters());
		getAPSEParameters().setClientType(Integer.parseInt(properties.getProperty("ClientType","40967").trim()));
		getAPSEParameters().setCallingPhysicalAddress(properties.getProperty("CallingPhysicalAddress","30"));
		getAPSEParameters().setProposedAppCtxName(Integer.parseInt(properties.getProperty("ProposedAppCtxName","0").trim()));

		setInfoTypePassword(properties.getProperty(MeterProtocol.PASSWORD,"0000000000000000"));

        if(Integer.parseInt(properties.getProperty("DelayAfterConnect", "0")) == 1) {
			delayAfterConnect = 6000;
		} else {
			delayAfterConnect = Integer.parseInt(properties.getProperty("DelayAfterConnect", "0").trim());
		}

        try {
			getAPSEParameters().setKey(ProtocolUtils.convert2ascii(getInfoTypePassword().getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new InvalidPropertyException(e.toString());
		}
	}

	public int getNumberOfChannels() throws IOException{
		if(getTrimaranObjectFactory().readParameters().isCcReact()) {
			return 6;
		} else {
			return 2;
		}
	}

	public int getProfileInterval() throws IOException{
		return getTrimaranObjectFactory().readParameters().getTCourbeCharge() * 60;
	}

	public String getFirmwareVersion() throws IOException, UnsupportedException {
		String firm = getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].toString();
//		setMeterVersion(getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].getResources());	// do the version check in the init routine
		return firm;
	}

    public String getProtocolVersion() {
		return "$Date: 2013-10-31 11:22:19 +0100 (Thu, 31 Oct 2013) $";
//		return "$Revision$";
	}

	public Date getTime() throws IOException {
		long roundTrip = System.currentTimeMillis() - getRoundTripStart();
		long meterTime = getTrimaranObjectFactory().readParameters().getDernierHoroDate().getCalendar().getTimeInMillis();
		return new Date(meterTime + roundTrip);
	}

	public void setTime() throws IOException {
		throw new UnsupportedException("Setting time in Trimaran meter is not supported.");
	}

	protected String getRegistersInfo(int extendedLogging) throws IOException{
		StringBuffer strBuff = new StringBuffer();

		strBuff.append(getTrimaranObjectFactory().readParameters());
		strBuff.append(getTrimaranObjectFactory().readParametersMoins1());
		strBuff.append(getTrimaranObjectFactory().readAccessPartiel());
		strBuff.append(getTrimaranObjectFactory().readEnergieIndex());
		strBuff.append(getTrimaranObjectFactory().readTempsFonctionnement());	// not sure with TEP
		if(isTECMeter()){
			strBuff.append(getTrimaranObjectFactory().readParametersPlus1());		// not with TEP
			strBuff.append(getTrimaranObjectFactory().readArreteJournalier());		// not with TEP
			strBuff.append(getTrimaranObjectFactory().readArreteProgrammables());	// not with TEP
			strBuff.append(getTrimaranObjectFactory().readProgrammablesIndex());	// not with TEP
		}
		else if(isTEPMeter()){
			strBuff.append(getTrimaranObjectFactory().readPMaxMois());				// not with TEC
//			strBuff.append(getTrimaranObjectFactory().readDureesPnonGarantie());	// not with TEC
		}

		return strBuff.toString();
	}

    /*******************************************************************************************
    R e g i s t e r P r o t o c o l  i n t e r f a c e
     * @throws IOException
    *******************************************************************************************/
	public RegisterValue readRegister(ObisCode obisCode) throws IOException{
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

    public RegisterInfo translateRegister(ObisCode obisCode) throws IOException {
        return ObisCodeMapper.getRegisterInfo(obisCode);
    }

	public RegisterFactory getRegisterFactory() throws IOException{
		if(registerFactory == null) {
			setRegisterFactory(new RegisterFactory(this));
		}
		return registerFactory;
	}

	public void setRegisterFactory(RegisterFactory registerFactory){
		this.registerFactory = registerFactory;
	}

	public APSEPDUFactory getAPSEFactory() {
		return aPSEFactory;
	}

	public Connection62056 getConnection62056() {
		return connection62056;
	}

	public DLMSPDUFactory getDLMSPDUFactory() {
		return dLMSPDUFactory;
	}

	/**
	 * @return the trimaran2PProfile
	 */
	protected Trimaran2PProfile getTrimaran2PProfile() {
		return trimaran2PProfile;
	}

	/**
	 * @param trimaran2PProfile the trimaran2PProfile to set
	 */
	protected void setTrimaran2PProfile(Trimaran2PProfile trimaran2PProfile) {
		this.trimaran2PProfile = trimaran2PProfile;
	}

	/**
	 * @param factory the aPSEFactory to set
	 */
	public void setAPSEFactory(APSEPDUFactory factory) {
		aPSEFactory = factory;
	}

	/**
	 * @return the aPSEParameters
	 */
	public APSEParameters getAPSEParameters() {
		return aPSEParameters;
	}

	/**
	 * @param parameters the aPSEParameters to set
	 */
	private void setAPSEParameters(APSEParameters parameters) {
		aPSEParameters = parameters;
	}

	/**
	 * @param factory the dLMSPDUFactory to set
	 */
	public void setDLMSPDUFactory(DLMSPDUFactory factory) {
		dLMSPDUFactory = factory;
	}

	/**
	 * @return the trimaranObjectFactory
	 */
	public TrimaranObjectFactory getTrimaranObjectFactory() {
		return trimaranObjectFactory;
	}

	/**
	 * @param trimaranObjectFactory the trimaranObjectFactory to set
	 */
	protected void setTrimaranObjectFactory(
			TrimaranObjectFactory trimaranObjectFactory) {
		this.trimaranObjectFactory = trimaranObjectFactory;
	}

	/**
	 * @param connection62056 the connection62056 to set
	 */
	protected void setConnection62056(Connection62056 connection62056) {
		this.connection62056 = connection62056;
	}

	/**
	 * @param sourceTransportAddress the sourceTransportAddress to set
	 */
	protected void setSourceTransportAddress(int sourceTransportAddress) {
		this.sourceTransportAddress = sourceTransportAddress;
	}

	/**
	 * @param destinationTransportAddress the destinationTransportAddress to set
	 */
	protected void setDestinationTransportAddress(int destinationTransportAddress) {
		this.destinationTransportAddress = destinationTransportAddress;
	}

	/**
	 * @param delayAfterConnect the delayAfterConnect to set
	 */
	protected void setDelayAfterConnect(int delayAfterConnect) {
		this.delayAfterConnect = delayAfterConnect;
	}

	/**
	 * @param timeout the t1Timeout to set
	 */
	protected void setT1Timeout(int timeout) {
		t1Timeout = timeout;
	}

	/**
	 * @return the roundTripStart
	 */
	protected long getRoundTripStart() {
		return roundTripStart;
	}

	/**
	 * @param roundTripStart the roundTripStart to set
	 */
	public void setRoundTripStart(long roundTripStart) {
		this.roundTripStart = roundTripStart;
	}

	protected String getMeterVersion() {
		return meterVersion;
	}

	public void setMeterVersion(String meterVersion) {
		this.meterVersion = meterVersion;
	}

	public boolean isTECMeter(){
		if(getMeterVersion().equalsIgnoreCase("TEC")) {
			return true;
		} else {
			return false;
		}
	}

	public boolean isTEPMeter(){
		if(getMeterVersion().equalsIgnoreCase("TEP")) {
			return true;
		} else {
			return false;
		}
	}

}
