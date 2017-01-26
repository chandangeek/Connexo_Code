/**
 *
 */
package com.energyict.protocolimpl.edf.trimaran2p;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.upl.UnsupportedException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpec;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.ProfileData;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.support.SerialNumberSupport;
import com.energyict.protocolimpl.base.AbstractProtocol;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.edf.trimaran2p.core.TrimaranObjectFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.APSEPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.dlmscore.dlmspdu.DLMSPDUFactory;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.APSEParameters;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.Connection62056;
import com.energyict.protocolimpl.edf.trimarandlms.protocol.ProtocolLink;
import com.energyict.protocolimpl.errorhandling.ProtocolIOExceptionHandler;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.energyict.mdc.upl.MeterProtocol.Property.PASSWORD;

/**
 * @author gna
 *
 */
public class Trimaran2P extends AbstractProtocol implements ProtocolLink, SerialNumberSupport {

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

	public Trimaran2P(PropertySpecService propertySpecService) {
		super(propertySpecService);
	}

	@Override
	protected void doConnect() throws IOException {
		getAPSEFactory().getAuthenticationReqAPSE();
		getDLMSPDUFactory().getInitiateRequest();
		if(getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].getResources().contains("TEC")){
			setMeterVersion("TEC");
		} else if(getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].getResources().contains("TEP")) {
			setMeterVersion("TEP");
		}
		getLogger().info(getDLMSPDUFactory().getStatusResponse().toString());
	}

	@Override
	protected void doDisconnect() throws IOException {
	}

	@Override
	public ProfileData getProfileData(Date lastReading, Date to, boolean includeEvents) throws IOException{
		return getTrimaran2PProfile().getProfileData(lastReading, to);
	}

	@Override
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

    @Override
    public List<PropertySpec> getUPLPropertySpecs() {
        List<PropertySpec> propertySpecs = new ArrayList<>(super.getUPLPropertySpecs());
        propertySpecs.add(this.integerSpec("T1Timeout", false));
        propertySpecs.add(this.integerSpec("STSAP", false));
        propertySpecs.add(this.integerSpec("DTSAP", false));
        propertySpecs.add(this.integerSpec("ClientType", false));
        propertySpecs.add(this.stringSpec("CallingPhysicalAddress", false));
        propertySpecs.add(this.integerSpec("ProposedAppCtxName", false));
        propertySpecs.add(this.integerSpec("DelayAfterConnect", false));
        return propertySpecs;
    }

    @Override
    public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
        super.setUPLProperties(properties);
		setT1Timeout(Integer.parseInt(properties.getTypedProperty("T1Timeout", "5000").trim()));
		setSourceTransportAddress(Integer.parseInt(properties.getTypedProperty("STSAP", "0").trim()));
		setDestinationTransportAddress(Integer.parseInt(properties.getTypedProperty("DTSAP", "2").trim()));

		setAPSEParameters(new APSEParameters());
		getAPSEParameters().setClientType(Integer.parseInt(properties.getTypedProperty("ClientType", "40967").trim()));
		getAPSEParameters().setCallingPhysicalAddress(properties.getTypedProperty("CallingPhysicalAddress","30"));
		getAPSEParameters().setProposedAppCtxName(Integer.parseInt(properties.getTypedProperty("ProposedAppCtxName","0").trim()));

		setInfoTypePassword(properties.getTypedProperty(PASSWORD.getName(), "0000000000000000"));

        if(Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "0")) == 1) {
			delayAfterConnect = 6000;
		} else {
			delayAfterConnect = Integer.parseInt(properties.getTypedProperty("DelayAfterConnect", "0").trim());
		}

        try {
			getAPSEParameters().setKey(ProtocolUtils.convert2ascii(getInfoTypePassword().getBytes()));
		} catch (IOException e) {
			e.printStackTrace();
			throw new InvalidPropertyException(e.toString());
		}
	}

    @Override
	public int getNumberOfChannels() throws IOException{
		if(getTrimaranObjectFactory().readParameters().isCcReact()) {
			return 6;
		} else {
			return 2;
		}
	}

    @Override
	public int getProfileInterval() throws IOException{
		return getTrimaranObjectFactory().readParameters().getTCourbeCharge() * 60;
	}

    @Override
	public String getFirmwareVersion() throws IOException {
        return getDLMSPDUFactory().getStatusResponse().getStatusIdentifies()[0].toString();
	}

    @Override
    public String getSerialNumber() {
        try {
            return getDLMSPDUFactory().getStatusResponse().getSerialNumber();
        } catch (IOException e) {
            throw ProtocolIOExceptionHandler.handle(e, getInfoTypeRetries() + 1);
        }
    }

    @Override
    public String getProtocolVersion() {
		return "$Date: Wed Dec 28 16:35:58 2016 +0100 $";
	}

    @Override
	public Date getTime() throws IOException {
		long roundTrip = System.currentTimeMillis() - getRoundTripStart();
		long meterTime = getTrimaranObjectFactory().readParameters().getDernierHoroDate().getCalendar().getTimeInMillis();
		return new Date(meterTime + roundTrip);
	}

    @Override
	public void setTime() throws IOException {
		throw new UnsupportedException("Setting time in Trimaran meter is not supported.");
	}

    @Override
	protected String getRegistersInfo(int extendedLogging) throws IOException{
		StringBuilder builder = new StringBuilder();
		builder.append(getTrimaranObjectFactory().readParameters());
		builder.append(getTrimaranObjectFactory().readParametersMoins1());
		builder.append(getTrimaranObjectFactory().readAccessPartiel());
		builder.append(getTrimaranObjectFactory().readEnergieIndex());
		builder.append(getTrimaranObjectFactory().readTempsFonctionnement());	// not sure with TEP
		if(isTECMeter()){
			builder.append(getTrimaranObjectFactory().readParametersPlus1());		// not with TEP
			builder.append(getTrimaranObjectFactory().readArreteJournalier());		// not with TEP
			builder.append(getTrimaranObjectFactory().readArreteProgrammables());	// not with TEP
			builder.append(getTrimaranObjectFactory().readProgrammablesIndex());	// not with TEP
		}
		else if(isTEPMeter()){
			builder.append(getTrimaranObjectFactory().readPMaxMois());				// not with TEC
		}
		return builder.toString();
	}

    @Override
	public RegisterValue readRegister(ObisCode obisCode) throws IOException{
		ObisCodeMapper ocm = new ObisCodeMapper(this);
		return ocm.getRegisterValue(obisCode);
	}

    @Override
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

    @Override
	public APSEPDUFactory getAPSEFactory() {
		return aPSEFactory;
	}

    @Override
	public Connection62056 getConnection62056() {
		return connection62056;
	}

    @Override
	public DLMSPDUFactory getDLMSPDUFactory() {
		return dLMSPDUFactory;
	}

	/**
	 * @return the trimaran2PProfile
	 */
    private Trimaran2PProfile getTrimaran2PProfile() {
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
    private void setAPSEFactory(APSEPDUFactory factory) {
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

    private void setConnection62056(Connection62056 connection62056) {
		this.connection62056 = connection62056;
	}

    private void setSourceTransportAddress(int sourceTransportAddress) {
		this.sourceTransportAddress = sourceTransportAddress;
	}

    private void setDestinationTransportAddress(int destinationTransportAddress) {
		this.destinationTransportAddress = destinationTransportAddress;
	}

	/**
	 * @param delayAfterConnect the delayAfterConnect to set
	 */
	protected void setDelayAfterConnect(int delayAfterConnect) {
		this.delayAfterConnect = delayAfterConnect;
	}

	private void setT1Timeout(int timeout) {
		t1Timeout = timeout;
	}

	private long getRoundTripStart() {
		return roundTripStart;
	}

	public void setRoundTripStart(long roundTripStart) {
		this.roundTripStart = roundTripStart;
	}

	private String getMeterVersion() {
		return meterVersion;
	}

	public void setMeterVersion(String meterVersion) {
		this.meterVersion = meterVersion;
	}

	public boolean isTECMeter(){
        return "TEC".equalsIgnoreCase(getMeterVersion());
	}

	public boolean isTEPMeter(){
        return "TEP".equalsIgnoreCase(getMeterVersion());
	}

}