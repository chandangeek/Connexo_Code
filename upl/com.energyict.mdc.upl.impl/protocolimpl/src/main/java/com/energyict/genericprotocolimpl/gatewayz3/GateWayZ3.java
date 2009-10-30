package com.energyict.genericprotocolimpl.gatewayz3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.commserverj.TaskImpl;
import com.energyict.commserverj.shadow.CommunicationSchedulerFullShadow;
import com.energyict.commserverj.shadow.CommunicationSchedulerFullShadowBuilder;
import com.energyict.concentrator.communication.driver.rf.cynet.ManufacturerId;
import com.energyict.concentrator.communication.driver.rf.cynet.NetworkNode;
import com.energyict.concentrator.communication.driver.rf.cynet.NetworkTopology;
import com.energyict.dlms.DLMSConnection;
import com.energyict.dlms.DataStructure;
import com.energyict.dlms.InvokeIdAndPriority;
import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.aso.ConformanceBlock;
import com.energyict.dlms.aso.SecurityProvider;
import com.energyict.dlms.aso.XdlmsAse;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.dlms.cosem.DataAccessResultException.DataAccessResultCode;
import com.energyict.genericprotocolimpl.common.AMRJournalManager;
import com.energyict.genericprotocolimpl.common.CommonUtils;
import com.energyict.genericprotocolimpl.common.ConcentratorProtocol;
import com.energyict.genericprotocolimpl.common.DLMSProtocol;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageCategoryConstants;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageConstant;
import com.energyict.genericprotocolimpl.common.messages.RtuMessageKeyIdConstants;
import com.energyict.genericprotocolimpl.webrtuz3.WebRTUZ3;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.ComPortShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;


/**
 * <pre>
 * Implements the GateWay Z3 protocol. The Z3 will act as a Master/Gateway in an RF-Mesh 
 * network with a certain amount of R2 slave devices.
 * After fetching the "routingTable" of the Z3, the protocol will handle each R2 one by one. 
 * The handling of the R2's will depend on the nextCommunicationDate of his commSchedulers.
 * 
 * <u>NOTE:</u>
 * The communication to a slave should be started with a postDialCommand:
 * <b>&lt;ESC&gt;rfclient="rfclientid"&lt;/ESC&gt;</b>
 * Normally you should use an 'IP-dialer with selector' for this, but because we use the 
 * same link from the Z3, we should send it our selves
 * </pre> 
 * 
 * @author gna
 * @since 21 October 2009
 */
public class GateWayZ3 extends DLMSProtocol implements ConcentratorProtocol{
	
	/** Helper to rapidly fetch data without a Z3/R2 */
	private boolean TESTING = false;
	
	/** Obis code we can use to request the RF network topology. */
	private static final ObisCode OBIS_CODE_NETWORK_TOPOLOGY = ObisCode.fromString("0.128.3.0.8.255");
	
	/** The current NetworkTopology from the Z3 */
	private NetworkTopology networkTopology;
	
	/* Properties */
	private String slaveRtuType;
	private String folderExtName;
	private String masterDeviceId = "";

	@Override
	protected void doExecute() throws BusinessException, SQLException, IOException {

		List<String> slaves;
		if(TESTING){
			slaves = new ArrayList<String>();
			slaves.add("00000000");
			slaves.add("28000552");
			slaves.add("28000549");
			slaves.add("28000548");
			slaves.add("28000550");
			slaves.add("28000551");
			slaves.add("2800054d");
			slaves.add("2800055b");
			slaves.add("2800054b");
		} else {
			slaves = getSlaveDevices();
		}
		disconnect();
		logger.log(Level.INFO, "Disconnected from the Z3, now all slaves will be processed.");
		// handle each slave device
		for(String slaveId : slaves){
			handleSlaveDevice(slaveId);
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 * @param id - the DeviceId of the rtu
	 */
	public void handleSlaveDevice(String id) {
		Rtu rtu = null;
		try {
			
			try {
				if(id.equalsIgnoreCase("00000000")){ //this is the master
					
					rtu = getMeter();
					id = this.masterDeviceId;
					
				    // Loop over the Rtu's communicationSchedules
					for(int i = 0; i < rtu.getCommunicationSchedulers().size(); i++){
						CommunicationScheduler commSchedule = (CommunicationScheduler)rtu.getCommunicationSchedulers().get(i);
//						doExecuteSlave(commSchedule);
						doExecuteMaster(commSchedule);
					}
					
				} else {
					rtu = findOrCreateDeviceByDeviceId(id);
					if(rtu != null){
						
					    // Loop over the Rtu's communicationSchedules
						for(int i = 0; i < rtu.getCommunicationSchedulers().size(); i++){
							CommunicationScheduler commSchedule = (CommunicationScheduler)rtu.getCommunicationSchedulers().get(i);
							doExecuteSlave(commSchedule, false);
						}
						
					} else {
						logger.log(Level.INFO, "No meter found with DeviceId: " + id);
					}
				}
				
			} catch (InvalidPropertyException e) {
				//TODO to test
				logger.log(Level.INFO, e.getMessage());
			}
			
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (NestedIOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void doExecuteMaster(CommunicationScheduler commSchedule){
		doExecuteSlave(commSchedule, true);
	}
	
	private void doExecuteSlave(CommunicationScheduler commSchedule, boolean useFixedZ3Protocol){
		try {
			long connectTime = System.currentTimeMillis();
			Integer completionCode = null;
			String errorMessage = "";
			Rtu rtu = commSchedule.getRtu();
			if((commSchedule.getNextCommunication()!=null)&&(commSchedule.getNextCommunication().before(Calendar.getInstance(rtu.getDeviceTimeZone()).getTime()))
					&&!commSchedule.equals(communicationScheduler)){
				
				logger.log(Level.INFO, "Starting to handle meter \'" + rtu + "\'");
				
				CommunicationSchedulerFullShadow csfs = CommunicationSchedulerFullShadowBuilder.getCommunicationSchedulerFullShadow(commSchedule);
				csfs.setDialerFactory(communicationScheduler.getDialerFactory());
				
				link.getStreamConnection().write(rtu.getPostDialCommand()+"\r\n",500);
			    
			    completionCode = AmrJournalEntry.CC_OK;
			    errorMessage = "";
			    
			    try{
			    	commSchedule.startCommunication();
			    	logger.log(Level.INFO, "modem dialing "+ getMeter().getPhoneNumber() + "("+ rtu.getPostDialCommand() +")");
			    	
			    	if(useFixedZ3Protocol){ // then it's the master and you MUST use the WebRTUZ3 protocol
						WebRTUZ3 wZ3 = new WebRTUZ3();
						wZ3.addProperties(rtu.getProperties());
						wZ3.execute(commSchedule, link, logger);
			    	} else {	// it's a slave so you can execute his taskImpl
			    		TaskImpl ti = new TaskImpl(csfs, getCurrentComportSchadow());
			    		ti.execute(this.link, false, false, logger);
			    	}
			    	
			    } catch (IOException e){
			    	completionCode = AmrJournalEntry.CC_IOERROR;
			    	errorMessage = e.getMessage();
			    } catch (SQLException e){
			    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
			    	errorMessage = e.getMessage();
			    } catch (BusinessException e){
			    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
			    	errorMessage = e.getMessage();
				} finally {
					if(rtu != null){	// only if we have an Rtu we should set an AmrJournal
						if(completionCode != null){
							
							AMRJournalManager amrjm = new AMRJournalManager(rtu, commSchedule);
							amrjm.journal(new AmrJournalEntry(completionCode));
							amrjm.journal(new AmrJournalEntry(AmrJournalEntry.CONNECTTIME, Math.abs(System.currentTimeMillis() - connectTime)/1000));
							
							if(completionCode == AmrJournalEntry.CC_OK){
								amrjm.updateLastCommunication();
							} else {
								amrjm.journal(new AmrJournalEntry(AmrJournalEntry.DETAIL, errorMessage));
								amrjm.updateRetrials();
							}
						}
					}
				}
				
				Rtu master = getMasterForMeter(rtu.getDeviceId());
				if(master != null){
					rtu.updateGateway(master);
				}
				
				logger.log(Level.INFO, "Meter \'" + rtu + "\' has finished.");
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Get the {@link TaskExecutor} for the given commSchedule
//	 * @param commSchedule
//	 * @return a {@link MeterProtocolTaskExecutor} or a {@link GenericProtocolExecutor}
//	 * @throws BusinessException if a business error occurred
//	 */
//	private TaskExecutor getTaskExecutor(CommunicationScheduler commSchedule) throws BusinessException{
//        Object object = newInstance(commSchedule.getRtu().getProtocol().getShadow());
//        if (object instanceof MeterProtocol) {
//			return new MeterProtocolTaskExecutor(false);
//		} else if (object instanceof GenericProtocol) {
//			return new GenericProtocolExecutor();
//		}
//        throw new BusinessException("Invalid communication class");
//	}
//	
//	/**
//	 * Create a new instance of the protocol
//	 * @param cps the CommunicationProtocolShadow
//	 * @return a new instance of the protocol
//	 * @throws BusinessException if a business error occurred during the creation of a new instance
//	 */
//    private Object newInstance(CommunicationProtocolShadow cps) throws BusinessException {
//        try {
//            Class implementor = Class.forName(cps.getJavaClassName());
//            return implementor.newInstance();
//        } catch (ClassNotFoundException ex) {
//            throw new BusinessException(ex);
//        } catch (InstantiationException ex) {
//            throw new BusinessException(ex);
//        } catch (IllegalAccessException ex) {
//            throw new BusinessException(ex);
//        }
//    }
	
	/**
	 * Find the master of the meter with the given deviceId in the networkTopology
	 * @param deviceId of the slave meter
	 * @return the master meter(Rtu)
	 * @throws IOException if multiple meters were found in the database
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	private Rtu getMasterForMeter(String deviceId) throws IOException, SQLException, BusinessException{
		
		for(NetworkNode nn:this.networkTopology.getRoot().getAllNodes()){
			if(getOriginalManufacturerIdNotation(nn).equalsIgnoreCase(deviceId)){
				Set<NetworkNode> networkMaster = nn.getParentNetwork().getNetworkMasters();
				if(networkMaster.size() != 1){
					throw new IllegalArgumentException("Number is masterNodes is different from 1 (" + networkMaster.size() + ")");
				} else {
					return findOrCreateDeviceByDeviceId(getOriginalManufacturerIdNotation(networkMaster.iterator().next()));
				}
			}
		}
		
		return null;
	}
	
	/**
	 * Custom find or createDevice method.
	 * After finding the device we check if the deviceId and postDialCommand is filled in and update if necessary.
	 * 
	 * @param deviceId - the rfclient id of the R2
	 * @return an rtu
	 * @throws IOException if multiple meters were found in the database
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	private Rtu findOrCreateDeviceByDeviceId(String deviceId) throws IOException, SQLException, BusinessException{
		try{
			Rtu device = CommonUtils.findOrCreateDeviceByDeviceId(deviceId, slaveRtuType, folderExtName);
			
			if(device != null){
				//TODO toTest
				updateR2WithPostDialCommandRfClient(device);
			}
			
			return device;
		} catch (InvalidPropertyException e) {
			logger.log(Level.INFO, e.getMessage());
		} 
		return null;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getSlaveDevices() throws IOException {
		
		List<String> allNodes = new ArrayList<String>();
		String nt = getNetworkTopology();
		
		if(nt != null){
			this.networkTopology = NetworkTopology.parse(new ManufacturerId(0), nt, null);
			
			for(NetworkNode nn:this.networkTopology.getRoot().getAllNodes()){
				allNodes.add(getOriginalManufacturerIdNotation(nn));
			}
			
		} else {
			logger.log(Level.INFO, "No slaves were found on the device.");
		}
		
 		return allNodes;
	}
	
	/**
	 * Fetch the networkToplogy from the device
	 * 
	 * @return a String containing node definitions. Each line is manufacturerID and routing address, separated by a comma.
	 * 
	 * @throws IOException when data parsing fails
	 */
	private String getNetworkTopology() throws IOException{
		logger.log(Level.INFO, "Requesting network topology from Z3.");
		
		try {
			final Data cosemTopology = this.getCosemObjectFactory().getData(OBIS_CODE_NETWORK_TOPOLOGY);
			
			if (cosemTopology != null) {
				final StringBuilder stringBuilder = new StringBuilder();
				final DataStructure root = (cosemTopology).getDataContainer().getRoot();
				
				for (int i = 0; i < root.element.length; i++) {
					final DataStructure topologyEntry = (DataStructure)root.element[i];
					
					final String manufacturerId = Long.toHexString((((Integer)topologyEntry.element[0]).intValue() & 0xFFFFFFFFl));
					final String routingAddress = Long.toHexString((((Integer)topologyEntry.element[1]).intValue() & 0xFFFFFFFFl));
					
					stringBuilder.append(manufacturerId).append(',').append(routingAddress).append("\n");
				}
				
				logger.log(Level.INFO, "Got routing table \n[" + stringBuilder.toString() + "] from the device.");
				
				return stringBuilder.toString();
			} else {
				logger.log(Level.WARNING, "Query for OBIS code [" + OBIS_CODE_NETWORK_TOPOLOGY + "] did not yield any result, assuming no RF available.");
			}
			
			// The EpIO did not return anything when requested for the particular register, so we do neither.
			return null;
		} catch (final DataAccessResultException e) {
			if (e.getCode() == DataAccessResultCode.OBJECT_UNDEFINED) {
				// No such register.
				logger.log(Level.INFO, "The EpIO says there is no register with Obis code [" + OBIS_CODE_NETWORK_TOPOLOGY + "], assuming it is not acting as an RF master.");
			}
			
			return null;
		}
	}
	
	/**
	 * Create a default string from the NetworkNode manufacturer notation
	 * @param nn - the NetworkNode
	 * @return the original manufacturerId
	 */
	private String getOriginalManufacturerIdNotation(NetworkNode nn){
		StringBuilder strBuilder = new StringBuilder();
		String tmp = Integer.toHexString(nn.getManufacturerID().getOctet1());
		strBuilder.append((tmp.length()==1)?("0"+tmp):tmp);
		tmp = Integer.toHexString(nn.getManufacturerID().getOctet2());
		strBuilder.append((tmp.length()==1)?("0"+tmp):tmp);
		tmp = Integer.toHexString(nn.getManufacturerID().getOctet3());
		strBuilder.append((tmp.length()==1)?("0"+tmp):tmp);
		tmp = Integer.toHexString(nn.getManufacturerID().getOctet4());
		strBuilder.append((tmp.length()==1)?("0"+tmp):tmp);
		return strBuilder.toString();
	}
	
	/**
	 * Set the postDial command for the Given R2.
	 * The postDial command is constructed from the Rtu's deviceID and should be of the form
	 * <b>&lt;ESC&gt;rfclient="deviceid"&lt;/ESC&gt;</b>
	 * @param rtu - the Rtu to update
	 * @throws SQLException if a database error occurred
	 * @throws BusinessException if a business error occurred
	 * @throws InvalidPropertyException if the DeviceId is empty
	 */
	protected void updateR2WithPostDialCommandRfClient(Rtu rtu) throws SQLException, BusinessException, InvalidPropertyException{
		if((rtu.getPostDialCommand() == null) || (rtu.getPostDialCommand().equalsIgnoreCase(""))
				|| (rtu.getPostDialCommand().equalsIgnoreCase("<ESC>rfclient=\"\"<\\ESC>"))){
			
			RtuShadow shadow = rtu.getShadow();
			String deviceId = rtu.getDeviceId();
			if((deviceId != null) && !(deviceId.equalsIgnoreCase(""))){
				shadow.setPostDialCommand("<ESC>rfclient=\"" + deviceId +"\"<\\ESC>");
				rtu.update(shadow);
			} else {
				throw new InvalidPropertyException("DeviceId of rtu " + rtu + " is empty, can't update postDialCommand.");
			}
			
		}
	}
	
	/** Short notation for MeteringWarehouse.getCurrent() */
	private MeteringWarehouse mw(){
		MeteringWarehouse result = MeteringWarehouse.getCurrent();
		return (result == null) ? new MeteringWarehouseFactory().getBatch() : result;
	}
	
	/**
	 * Get the ComPort the ComServer is using for the Z3 communication
	 * @return current schedulers {@link ComPortShadow}
	 */
	private ComPortShadow getCurrentComportSchadow(){
		return communicationScheduler.getComPort().getShadow();
	}
	
	/**
	 * Setter for the DLMSConnection, mainly for testing purposes
	 * @param dlmsConnection - the given DLMSConnection
	 */
	protected void setDLMSConnection(DLMSConnection dlmsConnection){
		this.dlmsConnection = dlmsConnection;
	}
	
	/**
	 * Setter for the Logger, mainly for testing purposes
	 * @param logger - the given Logger
	 */
	protected void setLogger(Logger logger){
		this.logger = logger;
	}
	
	/**
	 * Setter for the CosemObjectFactory, mainly for testing purposes
	 * @param cosemObjectFactory - the given CosemObjectFactory
	 */
	protected void setCosemObjectFactory(CosemObjectFactory cosemObjectFactory){
		this.cosemObjectFactory = cosemObjectFactory;
	}
	
	@Override
	protected List<String> doGetOptionalKeys() {
		List<String> result = new ArrayList<String>();
		result.add("RtuType");			// To define a new SlaveRtu by it's RtuType
		result.add("FolderExtName");	// To place a new device in a folder
		return result;
	}

	@Override
	protected List<String> doGetRequiredKeys() {
		return null;
	}

	@Override
	protected void doValidateProperties() {
		this.slaveRtuType = properties.getProperty("RtuType");
		this.folderExtName = properties.getProperty("FolderExtName");
		this.masterDeviceId = getMeter().getDeviceId();
		if(this.masterDeviceId == null){
			this.masterDeviceId = "";
		}
	}

	@Override
	protected SecurityProvider getSecurityProvider() {
		// return null -> currently use the default one
		return null;
	}

	public long getTimeDifference() {
		// not relevant for the gateway
		return 0;
	}

	public String getVersion() {
		return "$Date$";
	}

	public int getReference() {
		return ProtocolLink.LN_REFERENCE;
	}

	public int getRoundTripCorrection() {
		// not relevant for the gateway
		return 0;
	}

	public StoredValues getStoredValues() {
		// not relevant for the gateway
		return null;
	}

	public TimeZone getTimeZone() {
		// not relevant for the gateway
		return null;
	}

	public boolean isRequestTimeZone() {
		return false;
	}

	@Override
	protected ConformanceBlock configureConformanceBlock() {
		// return null -> leave default
		return null;
	}

	@Override
	protected InvokeIdAndPriority configureInvokeIdAndPriority() {
		// return null -> leave default
		return null;
	}

	@Override
	protected XdlmsAse configureXdlmsAse() {
		// return null -> leave default
		return null;
	}

	@Override
	protected void doConnect() {
		// nothing else to do
	}

	@Override
	protected void doDisconnect() {
		// nothing else to do
	}

	@Override
	protected void doInit() {
	}

	@Override
	protected RegisterValue readRegister(ObisCode obisCode) throws IOException {
		return null;
	}

	@Override
	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		MessageCategorySpec catFirmware = getFirmwareCategory();
		
		categories.add(catFirmware);

		return categories;
	}
	
	/**
	 * This messageCategory let's you upgrade two types of firmware.
	 * One is the normal meter firmware, the other is the RF-firmware
	 * Both are imported with a userfile
	 * @return the messages for the FirmwareUpgrade
	 */
	@Override
	public MessageCategorySpec getFirmwareCategory() {
		MessageCategorySpec catFirmware = new MessageCategorySpec(
				RtuMessageCategoryConstants.FIRMWARE);
		MessageSpec msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.FIRMWARE,
				RtuMessageConstant.FIRMWARE_UPGRADE, false);
		catFirmware.addMessageSpec(msgSpec);
		msgSpec = addFirmwareMsg(RtuMessageKeyIdConstants.RFFIRMWARE,
				RtuMessageConstant.RF_FIRMWARE_UPGRADE, false);
		catFirmware.addMessageSpec(msgSpec);
		return catFirmware;
	}
}
