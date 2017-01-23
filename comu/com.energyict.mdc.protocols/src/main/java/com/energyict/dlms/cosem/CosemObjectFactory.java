/*
 * CosemObjectFactory.java
 *
 * Created on 18 augustus 2004, 9:26
 */

package com.energyict.dlms.cosem;

import com.energyict.mdc.protocol.api.ProtocolException;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.cosem.attributes.MbusClientAttributes;
import com.energyict.obis.ObisCode;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author  Koen
 */
public class CosemObjectFactory implements DLMSCOSEMGlobals {

    private ProtocolLink protocolLink;
    private StoredValues storedValues=null; // cached
    private LoadProfile loadProfile=null; // cached
    private SAPAssignment sapAssignment = null; // cached
    private boolean useGetWithList;

    /** Creates a new instance of CosemObjectFactory */
    public CosemObjectFactory(ProtocolLink protocolLink) {
        this(protocolLink, false);
    }

    public CosemObjectFactory(ProtocolLink protocolLink, boolean useGetWithList) {
        this.protocolLink=protocolLink;
        this.useGetWithList = useGetWithList;
    }

    public ProtocolLink getProtocolLink() {
        return protocolLink;
    }

    public SAPAssignment getSAPAssignment() {
        if (sapAssignment == null) {
            sapAssignment = new SAPAssignment(protocolLink);
        }
        return sapAssignment;
    }

    public StoredValues getStoredValues() throws IOException {
        if (storedValues==null) {
            storedValues = protocolLink.getStoredValues();
            storedValues.retrieve();
        }
        return storedValues;
    }

    public LoadProfile getLoadProfile() throws ProtocolException {
        if (loadProfile==null) {
            loadProfile = new LoadProfile(this);
            loadProfile.retrieve();
        }
        return loadProfile;
    }

    public Clock getClock() throws ProtocolException {
        return new Clock(protocolLink,getObjectReference(CLOCK_OBJECT_LN,protocolLink.getMeterConfig().getClockSN()));
    }

    public Clock getClock(ObisCode obisCode) throws ProtocolException {
        return new Clock(protocolLink,getObjectReference(obisCode));
    }

    public GenericRead getGenericRead(int baseObject, int snAttr) {
        return new GenericRead(protocolLink,new ObjectReference(baseObject),snAttr);
    }

    public GenericRead getGenericRead(UniversalObject uo) throws ProtocolException {
        return getGenericRead(uo.getObisCode(),uo.getValueAttributeOffset(),uo.getClassID());
    }

    public GenericRead getGenericRead(DLMSAttribute attribute) throws ProtocolException {
        return getGenericRead(attribute.getObisCode(), attribute.getSnAttribute(), attribute.getClassId());
    }

    public GenericRead getGenericRead(ObisCode obisCode, int snAttr) throws ProtocolException {
        return getGenericRead(obisCode,snAttr,-1);
    }

    public GenericRead getGenericRead(ObisCode obisCode, int snAttr, int classId) throws ProtocolException {
        return new GenericRead(protocolLink,getObjectReference(obisCode,classId),snAttr);
    }

    public SMTPSetup getSMTPSetup(ObisCode obisCode) throws ProtocolException {
        return new SMTPSetup(protocolLink, getObjectReference(obisCode));
    }
    public ActivityCalendar getActivityCalendar(ObisCode obisCode) throws ProtocolException {
        return new ActivityCalendar(protocolLink, getObjectReference(obisCode));
    }
    public SpecialDaysTable getSpecialDaysTable(ObisCode obisCode) throws ProtocolException {
        return new SpecialDaysTable(protocolLink, getObjectReference(obisCode));
    }
    public ScriptTable getScriptTable(ObisCode obisCode) throws ProtocolException {
        return new ScriptTable(protocolLink, getObjectReference(obisCode));
    }

    public ScriptTable getGlobalMeterResetScriptTable() throws ProtocolException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_GLOBAL_METER_RESET);
    }
    public ScriptTable getTarifficationScriptTable() throws ProtocolException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_TARIFFICATION_SCRIPT_TABLE);
    }
    public ScriptTable getDisconnectControlScriptTable() throws ProtocolException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_DISCONNECT_CONTROL);
    }
    public ScriptTable getImageActivationScriptTable() throws ProtocolException {
    	return new ScriptTable(protocolLink, ScriptTable.LN_IMAGE_ACTIVATION);
    }

    public RegisterMonitor getRegisterMonitor(ObisCode obisCode) throws ProtocolException {
        return new RegisterMonitor(protocolLink, getObjectReference(obisCode));
    }

    public SingleActionSchedule getSingleActionSchedule(ObisCode obisCode) throws ProtocolException {
    	return new SingleActionSchedule(protocolLink, getObjectReference(obisCode));
    }

    public void writeObject(ObisCode obisCode, int classId, int attrId, byte[] data) throws IOException {
        GenericWrite gw = new GenericWrite(protocolLink,new ObjectReference(obisCode.getLN(),classId), attrId);
        gw.write(data);
    }

    public GenericWrite getGenericWrite(int baseObject,int attr) {
        return new GenericWrite(protocolLink,new ObjectReference(baseObject),attr);
    }
    public GenericWrite getGenericWrite(UniversalObject uo) throws ProtocolException {
        return getGenericWrite(uo.getObisCode(),uo.getValueAttributeOffset(),uo.getClassID());
    }
    public GenericWrite getGenericWrite(ObisCode obisCode,int attr) throws ProtocolException {
        return getGenericWrite(obisCode,attr,-1);
    }
    public GenericWrite getGenericWrite(ObisCode obisCode,int attr, int classId) throws ProtocolException {
        return new GenericWrite(protocolLink,getObjectReference(obisCode,classId),attr);
    }

    public GenericInvoke getGenericInvoke(int baseObject, int method){
    	return new GenericInvoke(protocolLink, new ObjectReference(baseObject), method);
    }
    public GenericInvoke getGenericInvoke(ObisCode obisCode, int classId, int method) throws ProtocolException {
    	return new GenericInvoke(protocolLink, getObjectReference(obisCode, classId), method);
    }

    public ProfileGeneric getProfileGeneric(ObisCode obisCode) throws ProtocolException {
        return new ProfileGeneric(protocolLink,getObjectReference(obisCode));
    }

    public ProfileGeneric getProfileGeneric(ObisCode obisCode, boolean dsmr4SelectiveAccessFormat) throws IOException {
        ProfileGeneric profileGeneric = new ProfileGeneric(protocolLink, getObjectReference(obisCode));
        profileGeneric.setDsmr4SelectiveAccessFormat(dsmr4SelectiveAccessFormat);
        return profileGeneric;
    }

    public ProfileGeneric getProfileGeneric(int shortNameReference) throws ProtocolException {
        return new ProfileGeneric(protocolLink,new ObjectReference(shortNameReference));
    }

    public Register getRegister(int baseObject) {
        return new Register(protocolLink,new ObjectReference(baseObject));
    }
    public Register getRegister(ObisCode obisCode) throws ProtocolException {
        return new Register(protocolLink,getObjectReference(obisCode));
    }

    public ExtendedRegister getExtendedRegister(int baseObject) {
        return new ExtendedRegister(protocolLink,new ObjectReference(baseObject));
    }

    public ExtendedRegister getExtendedRegister(ObisCode obisCode) throws ProtocolException {
        return new ExtendedRegister(protocolLink,getObjectReference(obisCode));
    }

    public Data getData(ObisCode obisCode) throws ProtocolException {
        return new Data(protocolLink,getObjectReference(obisCode));
    }

    public NTPServerAddress getNTPServerAddress() throws ProtocolException {
        return new NTPServerAddress(protocolLink, getObjectReference(NTPServerAddress.getDefaultObisCode()));
    }

    public EventPushNotificationConfig getEventPushNotificationConfig() throws ProtocolException {
        return new EventPushNotificationConfig(protocolLink, getObjectReference(EventPushNotificationConfig.getDefaultObisCode()));
    }

    public WebPortalPasswordConfig getWebPortalPasswordConfig() throws ProtocolException {
        return new WebPortalPasswordConfig(protocolLink, getObjectReference(WebPortalPasswordConfig.getDefaultObisCode()));
    }

    public PrivacyEnhancingDataAggregation getPrivacyEnhancingDataAggregation(ObisCode obisCode) throws ProtocolException {
        return new PrivacyEnhancingDataAggregation(protocolLink,getObjectReference(obisCode));
    }

    public Data getData(int baseObject) throws ProtocolException {
        return new Data(protocolLink,new ObjectReference(baseObject));
    }

    public DemandRegister getDemandRegister(ObisCode obisCode) throws ProtocolException {
        return new DemandRegister(protocolLink,getObjectReference(obisCode));
    }

    public AssociationLN getAssociationLN() {
        return new AssociationLN(protocolLink,new ObjectReference(ASSOC_LN_OBJECT_LN));
    }

    public AssociationLN getAssociationLN(final ObisCode obisCode) {
        return new AssociationLN(protocolLink, new ObjectReference(obisCode.getLN()));
    }

    public AssociationSN getAssociationSN() {
        return new AssociationSN(protocolLink,new ObjectReference(ASSOC_SN_OBJECT));
    }

    public IPv4Setup getIPv4Setup() throws ProtocolException {
    	return new IPv4Setup(protocolLink, getObjectReference(IPV4_SETUP, protocolLink.getMeterConfig().getIPv4SetupSN()));
    }

    public IPv6Setup getIPv6Setup(){
        return new IPv6Setup(protocolLink);
    }

    public MacAddressSetup getMacAddressSetup(ObisCode obisCode) throws ProtocolException {
        return new MacAddressSetup(protocolLink, getObjectReference(obisCode, DLMSClassId.MAC_ADDRESS_SETUP.getClassId()));
    }

    public IPv4Setup getIPv4Setup(ObisCode obisCode) throws ProtocolException {
    	return new IPv4Setup(protocolLink, getObjectReference(obisCode));
    }

    public P3ImageTransfer getP3ImageTransfer() throws ProtocolException {
    	return new P3ImageTransfer(protocolLink, getObjectReference(P3IMAGE_TRANSFER, protocolLink.getMeterConfig().getP3ImageTransferSN()));
    }

    public P3ImageTransfer getP3ImageTransfer(ObisCode obisCode) throws ProtocolException {
    	return new P3ImageTransfer(protocolLink, getObjectReference(obisCode));
    }

    public Disconnector getDisconnector() throws ProtocolException {
    	return new Disconnector(protocolLink, getObjectReference(DISCONNECTOR, protocolLink.getMeterConfig().getDisconnectorSN()));
    }

    public Disconnector getDisconnector(ObisCode obisCode) throws ProtocolException {
    	return new Disconnector(protocolLink, getObjectReference(obisCode));
    }

    /**
     * Getter for the {@link com.energyict.dlms.cosem.MBusClient} object according to BlueBook version 9 or below
     *
     * @param obisCode the obisCode of the Object
     * @return a newly created MbusClient object
     * @throws java.io.IOException if the {@link com.energyict.dlms.ProtocolLink#getReference()} != (ProtocolLink#LN_REFERENCE || ProtocolLink#SN_REFERENCE)
     * @deprecated use {@link #getMbusClient(ObisCode, int)} instead
     */
	public MBusClient getMbusClient(ObisCode obisCode) throws ProtocolException {
        return getMbusClient(obisCode, MbusClientAttributes.VERSION9);
	}

    /**
     * Getter for the {@link com.energyict.dlms.cosem.MBusClient} object (valid for any DLMS version)
     *
     * @param obisCode the obisCode of the Object
     * @param version  the version of the object (see {@link com.energyict.dlms.cosem.attributes.MbusClientAttributes#version} for more details)
     * @return a newly created MbusClient object
     * @throws java.io.IOException if the {@link com.energyict.dlms.ProtocolLink#getReference()} != (ProtocolLink#LN_REFERENCE || ProtocolLink#SN_REFERENCE)
     */
    public MBusClient getMbusClient(ObisCode obisCode, int version) throws ProtocolException {
        return new MBusClient(protocolLink, getObjectReference(obisCode), version);
    }

	public Limiter getLimiter() throws ProtocolException {
		return new Limiter(protocolLink, getObjectReference(LIMITER, protocolLink.getMeterConfig().getLimiterSN()));
	}

	public PPPSetup getPPPSetup() throws ProtocolException {
		return new PPPSetup(protocolLink, getObjectReference(PPPSETUP, protocolLink.getMeterConfig().getPPPSetupSN()));
	}

	public GPRSModemSetup getGPRSModemSetup() throws ProtocolException {
		return new GPRSModemSetup(protocolLink, getObjectReference(GPRSMODEMSETUP, protocolLink.getMeterConfig().getGPRSModemSetupSN()));
	}

	public GPRSModemSetup getGPRSModemSetup(final ObisCode obisCode) throws ProtocolException {
		return new GPRSModemSetup(protocolLink, getObjectReference(obisCode));
	}

	public SFSKPhyMacSetup getSFSKPhyMacSetup() throws ProtocolException {
		return getSFSKPhyMacSetup(SFSKPhyMacSetup.getDefaultObisCode());
	}

	public SFSKPhyMacSetup getSFSKPhyMacSetup(ObisCode obisCode) throws ProtocolException {
		return new SFSKPhyMacSetup(protocolLink, getObjectReference(obisCode));
	}

	/**
	 * Getter for the ShortName ImageTransfer Object
	 */
	public SFSKPhyMacSetup getSFSKPhyMacSetupSN() throws ProtocolException {
	    return new SFSKPhyMacSetup(protocolLink, new ObjectReference(protocolLink.getMeterConfig().getSFSKPhyMacSetupSN()));
	}

	public SFSKMacCounters getSFSKMacCounters() throws ProtocolException {
		return getSFSKMacCounters(SFSKMacCounters.getDefaultObisCode());
	}

	public SFSKMacCounters getSFSKMacCounters(ObisCode obisCode) throws ProtocolException {
		return new SFSKMacCounters(protocolLink, getObjectReference(obisCode));
	}

	public SFSKIec61334LLCSetup getSFSKIec61334LLCSetup() throws ProtocolException {
		return getSFSKIec61334LLCSetup(SFSKIec61334LLCSetup.getDefaultObisCode());
	}

	public SFSKIec61334LLCSetup getSFSKIec61334LLCSetup(ObisCode obisCode) throws ProtocolException {
		return new SFSKIec61334LLCSetup(protocolLink, getObjectReference(obisCode));
	}

    public SFSKReportingSystemList getSFSKReportingSystemList() throws ProtocolException {
        return getSFSKReportingSystemList(SFSKReportingSystemList.getDefaultObisCode());
    }

    public SFSKReportingSystemList getSFSKReportingSystemList(ObisCode obisCode) throws ProtocolException {
        return new SFSKReportingSystemList(protocolLink, getObjectReference(obisCode));
    }

	public SFSKActiveInitiator getSFSKActiveInitiator() throws ProtocolException {
		return getSFSKActiveInitiator(SFSKActiveInitiator.getDefaultObisCode());
	}

	public SFSKActiveInitiator getSFSKActiveInitiator(ObisCode obisCode) throws ProtocolException {
		return new SFSKActiveInitiator(protocolLink, getObjectReference(obisCode));
	}

	public SFSKSyncTimeouts getSFSKSyncTimeouts() throws ProtocolException {
		return getSFSKSyncTimeouts(SFSKSyncTimeouts.getDefaultObisCode());
	}

	public SFSKSyncTimeouts getSFSKSyncTimeouts(ObisCode obisCode) throws ProtocolException {
		return new SFSKSyncTimeouts(protocolLink, getObjectReference(obisCode));
	}

	public PLCOFDMType2PHYAndMACCounters getPLCOFDMType2PHYAndMACCounters(ObisCode obisCode) throws ProtocolException {
		return new PLCOFDMType2PHYAndMACCounters(protocolLink, getObjectReference(obisCode));
	}

	public PLCOFDMType2PHYAndMACCounters getPLCOFDMType2PHYAndMACCounters() throws ProtocolException {
		return new PLCOFDMType2PHYAndMACCounters(protocolLink, getObjectReference(PLCOFDMType2PHYAndMACCounters.getDefaultObisCode()));
	}

	public PLCOFDMType2MACSetup getPLCOFDMType2MACSetup(ObisCode obisCode) throws ProtocolException {
		return new PLCOFDMType2MACSetup(protocolLink, getObjectReference(obisCode));
	}

	public PLCOFDMType2MACSetup getPLCOFDMType2MACSetup() throws ProtocolException {
		return new PLCOFDMType2MACSetup(protocolLink, getObjectReference(PLCOFDMType2MACSetup.getDefaultObisCode()));
	}

	public SixLowPanAdaptationLayerSetup getSixLowPanAdaptationLayerSetup(ObisCode obisCode) throws ProtocolException {
		return new SixLowPanAdaptationLayerSetup(protocolLink, getObjectReference(obisCode));
	}

	public SixLowPanAdaptationLayerSetup getSixLowPanAdaptationLayerSetup() throws ProtocolException {
		return new SixLowPanAdaptationLayerSetup(protocolLink, getObjectReference(SixLowPanAdaptationLayerSetup.getDefaultObisCode()));
	}

	public MBusSlavePortSetup getMBusSlavePortSetup() throws ProtocolException {
		return getMBusSlavePortSetup(MBusSlavePortSetup.getDefaultObisCode());
	}

	public MBusSlavePortSetup getMBusSlavePortSetup(ObisCode obisCode) throws ProtocolException {
		return new MBusSlavePortSetup(protocolLink, getObjectReference(obisCode));
	}

	public TCPUDPSetup getTCPUDPSetup() throws ProtocolException {
		return  new TCPUDPSetup(protocolLink);
	}

	public AutoConnect getAutoConnect() throws ProtocolException {
		return new AutoConnect(protocolLink);
	}

    public SmsWakeupConfiguration getSMSWakeupConfiguration(){
        return new SmsWakeupConfiguration(protocolLink);
    }

	/**
	 * Getter for the LongName ImageTransfer Object
	 */
	public ImageTransfer getImageTransfer() throws ProtocolException {
		return new ImageTransfer(protocolLink);
	}

	/**
     * Getter for the file transfer object
     */
	public FileTransfer getFileTransfer() throws ProtocolException {
		return getFileTransfer(FileTransfer.getDefaultObisCode());
	}

	/**
     * Getter for the file transfer object
     */
	public FileTransfer getFileTransfer(ObisCode obisCode) throws ProtocolException {
		return new FileTransfer(protocolLink, getObjectReference(obisCode));
	}

	/**
	 * Getter for the ShortName ImageTransfer Object
	 */
	public ImageTransfer getImageTransferSN() throws ProtocolException {
	    return new ImageTransfer(protocolLink, new ObjectReference(protocolLink.getMeterConfig().getImageTransferSN()));
	}

	/**
	 * Getter for the ImageTransfer Object with a given obisCode.
	 * If it is the default you need, then it is advised to use {@link #getImageTransfer()}
	 * or {@link #getImageTransferSN()}
	 */
	public ImageTransfer getImageTransfer(ObisCode obisCode) throws ProtocolException {
		return new ImageTransfer(protocolLink, getObjectReference(obisCode));
	}

	public SecuritySetup getSecuritySetup() throws ProtocolException {
		return new SecuritySetup(protocolLink);
	}

	public SecuritySetup getSecuritySetup(ObisCode obisCode) throws ProtocolException {
		return new SecuritySetup(protocolLink, getObjectReference(obisCode));
	}

    public ZigbeeHanManagement getZigbeeHanManagement() throws ProtocolException {
        return new ZigbeeHanManagement(protocolLink);
    }

    public ZigBeeSETCControl getZigBeeSETCControl() throws ProtocolException {
        return new ZigBeeSETCControl(protocolLink);
    }

    public ZigBeeSASStartup getZigBeeSASStartup() throws ProtocolException {
        return new ZigBeeSASStartup(protocolLink);
    }

    public ZigBeeSASStartup getZigBeeSASStartup(ObisCode obisCode) throws ProtocolException {
        return new ZigBeeSASStartup(protocolLink, getObjectReference(obisCode));
    }

    public ZigBeeSASJoin getZigBeeSASJoin() throws ProtocolException {
        return new ZigBeeSASJoin(protocolLink);
    }

    public ZigBeeSASJoin getZigBeeSASJoin(ObisCode obisCode) throws ProtocolException {
        return new ZigBeeSASJoin(protocolLink, getObjectReference(obisCode));
    }

    public CosemObject getCosemObject(ObisCode obisCode) throws IOException {
        if (obisCode.getF() != 255) {
            return getStoredValues().getHistoricalValue(obisCode);
        }
        else {
            if (protocolLink.getMeterConfig().getClassId(obisCode) == Register.CLASSID) {
				return new Register(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == ExtendedRegister.CLASSID) {
				return new ExtendedRegister(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == DLMSClassId.DEMAND_REGISTER.getClassId()) {
				return new DemandRegister(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == Data.CLASSID) {
				return new Data(protocolLink,getObjectReference(obisCode));
			} else if (protocolLink.getMeterConfig().getClassId(obisCode) == DLMSClassId.PROFILE_GENERIC.getClassId()) {
				return new ProfileGeneric(protocolLink,getObjectReference(obisCode));
			} else {
				throw new ProtocolException("CosemObjectFactory, getCosemObject, invalid classId "+protocolLink.getMeterConfig().getClassId(obisCode)+" for obisCode "+obisCode.toString()) ;
			}
        }
    }

    /**
     * Create a cosemObject based on the given parameters. Currently Data, Register, ExtendedRegister and DemandRegister are implemented
     *
     * @param oc      the obisCode for the object
     * @param classId the classId for the object
     * @return the newly constructed object
     * @throws java.io.IOException if the classId is not supported for this method
     */
    public CosemObject getCosemObjectFromObisAndClassId(ObisCode oc, int classId) throws ProtocolException {
        switch (classId) {
            case 1:
                return new Data(protocolLink, getObjectReference(oc));
            case 3:
                return new Register(protocolLink, getObjectReference(oc));
            case 4:
                return new ExtendedRegister(protocolLink, getObjectReference(oc));
            case 5:
                return new DemandRegister(protocolLink, getObjectReference(oc));
            default:
                throw new ProtocolException("CosemObjectFactory, getCosemObject, invalid classId " + classId + " for obisCode " + oc);
        }
    }

    public ComposedCosemObject getComposedCosemObject(DLMSAttribute... dlmsAttributes) {
        return new ComposedCosemObject(protocolLink, isUseGetWithList(), dlmsAttributes);
    }

    public ComposedCosemObject getComposedCosemObject(List<DLMSAttribute> dlmsAttributes) {
        return new ComposedCosemObject(protocolLink, isUseGetWithList(), dlmsAttributes);
    }

    //*****************************************************************************************
    public ObjectReference getObjectReference(ObisCode obisCode) throws ProtocolException {
        return getObjectReference(obisCode,-1);
    }

    public ObjectReference getObjectReference(ObisCode obisCode, int classId) throws ProtocolException {
        if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE) {
			return new ObjectReference(obisCode.getLN(),classId);
		} else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE) {
			return new ObjectReference(protocolLink.getMeterConfig().getSN(obisCode));
		}
        throw new ProtocolException("CosemObjectFactory, getObjectReference, invalid reference type "+protocolLink.getReference());
    }

    public ObjectReference getObjectReference(byte[] ln,int sn) throws ProtocolException {
        return getObjectReference(ln,-1,sn);
    }

	public ObjectReference getObjectReference(byte[] ln, int classId, int sn) throws ProtocolException {
		if (protocolLink.getReference() == ProtocolLink.LN_REFERENCE) {
			return new ObjectReference(ln, classId);
		} else if (protocolLink.getReference() == ProtocolLink.SN_REFERENCE) {
			return new ObjectReference(sn);
		}
		throw new ProtocolException("CosemObjectFactory, getObjectReference, invalid reference type " + protocolLink.getReference());
	}

    public boolean isUseGetWithList() {
        return useGetWithList;
    }

    public ChangeOfTenancyOrSupplierManagement getChangeOfTenancyOrSupplierManagement() throws ProtocolException {
        return new ChangeOfTenancyOrSupplierManagement(protocolLink);
    }

    public ChangeOfTenancyOrSupplierManagement getChangeOfTenancyOrSupplierManagement(ObisCode obisCode) throws ProtocolException {
        return new ChangeOfTenancyOrSupplierManagement(protocolLink, getObjectReference(obisCode));
    }

    public ChangeOfTenantManagement getChangeOfTenantManagement() {
        return new ChangeOfTenantManagement(protocolLink);
    }

    public ChangeOfTenantManagement getChangeOfTenantManagement(ObisCode obisCode) throws ProtocolException {
        return new ChangeOfTenantManagement(protocolLink, getObjectReference(obisCode));
    }

    public ChangeOfSupplierManagement getChangeOfSupplierManagement() throws ProtocolException {
        return new ChangeOfSupplierManagement(protocolLink);
    }

    public ChangeOfSupplierManagement getChangeOfSupplierManagement(ObisCode obisCode) throws ProtocolException {
        return new ChangeOfSupplierManagement(protocolLink, getObjectReference(obisCode));
    }

    public SupplierName getSupplierName(ObisCode obisCode) throws ProtocolException {
        return new SupplierName(protocolLink, getObjectReference(obisCode));
    }

    public SupplierId getSupplierId(ObisCode obisCode) throws ProtocolException {
        return new SupplierId(protocolLink, getObjectReference(obisCode));
    }

    public ActivePassive getActivePassive(ObisCode obisCode) throws ProtocolException {
        return new ActivePassive(protocolLink, getObjectReference(obisCode));
    }

    public CL432Setup getCL432Setup() throws ProtocolException {
        return new CL432Setup(protocolLink, getObjectReference(CL432Setup.getDefaultObis()));
    }

    public PrimePlcPhyLayerCounters getPrimePlcPhyLayerCounters() throws ProtocolException {
        return new PrimePlcPhyLayerCounters(protocolLink, getObjectReference(PrimePlcPhyLayerCounters.getDefaultObisCode()));
    }

    public PrimePlcMacSetup getPrimePlcMacSetup() throws ProtocolException {
        return new PrimePlcMacSetup(protocolLink, getObjectReference(PrimePlcMacSetup.getDefaultObisCode()));
    }

    public PrimePlcMacFunctionalParameters getPrimePlcMacFunctionalParameters() throws ProtocolException {
        return new PrimePlcMacFunctionalParameters(protocolLink, getObjectReference(PrimePlcMacFunctionalParameters.getDefaultObisCode()));
    }

    public LifeCycleManagement getLifeCycleManagement(final ObisCode obisCode) throws ProtocolException {
        return new LifeCycleManagement(protocolLink, getObjectReference(obisCode));
    }

    public LifeCycleManagement getLifeCycleManagement() throws ProtocolException {
        return new LifeCycleManagement(protocolLink, getObjectReference(LifeCycleManagement.getDefaultObisCode()));
    }

    /**
     * Returns the firewall setup object.
     *
     * @return		The firewall setup object.
     *
     * @throws java.io.IOException        If an IO error occurs while returning a reference;
     */
    public final FirewallSetup getFirewallSetup() throws ProtocolException {
    	return new FirewallSetup(this.protocolLink, this.getObjectReference(FirewallSetup.getDefaultObisCode()));
    }

    public final UplinkPingConfiguration getUplinkPingConfiguration() throws ProtocolException {
    	return new UplinkPingConfiguration(this.protocolLink, this.getObjectReference(UplinkPingConfiguration.getDefaultObisCode()));
    }

    public final ModemWatchdogConfiguration getModemWatchdogConfiguration() throws ProtocolException {
    	return new ModemWatchdogConfiguration(this.protocolLink, this.getObjectReference(ModemWatchdogConfiguration.getDefaultObisCode()));
    }

    public final G3NetworkManagement getG3NetworkManagement() throws ProtocolException {
        return new G3NetworkManagement(this.protocolLink, this.getObjectReference(G3NetworkManagement.getDefaultObisCode()));
    }

    public final G3NetworkManagement getG3NetworkManagement(final ObisCode obisCode) throws ProtocolException {
        return new G3NetworkManagement(this.protocolLink, this.getObjectReference(obisCode));
    }

    public final GenericPlcIBSetup getGenericPlcIBSetup() throws ProtocolException {
        return new GenericPlcIBSetup(this.protocolLink, this.getObjectReference(GenericPlcIBSetup.getDefaultObisCode()));
    }

    public final GenericPlcIBSetup getGenericPlcIBSetup(final ObisCode obisCode) throws ProtocolException {
        return new GenericPlcIBSetup(this.protocolLink, this.getObjectReference(obisCode));
    }

    public final G3PlcSetPSK getG3PlcSetPSK() throws ProtocolException {
        return new G3PlcSetPSK(this.protocolLink, this.getObjectReference(G3PlcSetPSK.getDefaultObisCode()));
    }

    public final G3PlcSetPSK getG3PlcSetPSK(final ObisCode obisCode) throws ProtocolException {
        return new G3PlcSetPSK(this.protocolLink, this.getObjectReference(obisCode));
    }

    public final NetworkManagement getNetworkManagement() throws ProtocolException {
        return new NetworkManagement(this.protocolLink, this.getObjectReference(NetworkManagement.getDefaultObisCode()));
    }

    public final GatewaySetup getGatewaySetup() throws ProtocolException {
        return new GatewaySetup(this.protocolLink, this.getObjectReference(GatewaySetup.getDefaultObisCode()));
    }

    public final LoggerSettings getLoggerSettings() throws ProtocolException {
        return new LoggerSettings(this.protocolLink, this.getObjectReference(LoggerSettings.getDefaultObisCode()));
    }

    public final MasterboardSetup getMasterboardSetup() throws ProtocolException {
        return new MasterboardSetup(this.protocolLink, this.getObjectReference(MasterboardSetup.getDefaultObisCode()));
    }
}
