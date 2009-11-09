package com.energyict.genericprotocolimpl.gatewayz3;

import java.io.IOException;
import java.net.SocketException;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.DuplicateException;
import com.energyict.concentrator.communication.driver.rf.cynet.ManufacturerId;
import com.energyict.concentrator.communication.driver.rf.cynet.Network;
import com.energyict.concentrator.communication.driver.rf.cynet.NetworkNode;
import com.energyict.concentrator.communication.driver.rf.cynet.NetworkTopology;
import com.energyict.dialer.coreimpl.IPDialerSelector;
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
import com.energyict.mdw.amr.GenericProtocol;
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationProtocol;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.ComPortShadow;
import com.energyict.mdw.shadow.RtuShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.InvalidPropertyException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.messaging.MessageCategorySpec;
import com.energyict.protocol.messaging.MessageSpec;


/**
 * <p>
 * Implements the GateWay Z3 protocol. The Z3 will act as a Master/Gateway in an RF-Mesh 
 * network with a certain amount of R2 slave devices.
 * After fetching the "routingTable" of the Z3, the protocol will handle each R2 one by one. 
 * The handling of the R2's will depend on the nextCommunicationDate of his {@link CommunicationProtocol}.
 * </p><p>
 * <u>NOTE:</u>
 * The communication to a slave should be started with a postDialCommand:
 * <b>&lt;ESC&gt;rfclient="rfclientid"&lt;/ESC&gt;</b>
 * Normally you should use an '{@link IPDialerSelector}' for this, but because we use the 
 * same link from the Z3, we should send it our selves
 * </p> 
 * 
 * @author gna
 * @since 21 October 2009
 */
public class GateWayZ3 extends DLMSProtocol implements ConcentratorProtocol{
	
	/** ObisCode we can use to request the RF network topology. */
	private static final ObisCode OBIS_CODE_NETWORK_TOPOLOGY = ObisCode.fromString("0.128.3.0.8.255");
	
    private static final String errorDuringCommunication = "- Meter with deviceId {0} has failed during communication schedule {1}.\r\n";
    private static final String errorDuringDBUpdate = "- Meter with deviceId {0} during database update for communication schedule {1}.\r\n";
	
	/** The current NetworkTopology from the Z3 */
	private NetworkTopology networkTopology;
	
	/** Contains a list of R2's who didn't end successfully */
	private List<String> failingSlaves;
	
	/* Properties */
	private String slaveRtuType;
	private String folderExtName;
	private String masterDeviceId = "";

	@Override
	protected void doExecute() throws BusinessException, SQLException, IOException {

		List<String> slaves;
		failingSlaves = new ArrayList<String>();
		slaves = getSlaveDevices();
		
		disconnect();
		log(Level.INFO, "Disconnected from the Z3, now all slaves will be processed.");
		
		// handle each slave device
		for(String slaveId : slaves){
			handleSlaveDevice(slaveId);
		}
		
		if(failingSlaves.size() > 0){
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append("The Z3 has finished but one or several slaves didn't end successfully: \r\n");
			for(String entry : failingSlaves){
				strBuilder.append(entry);
			}
			log(Level.INFO, strBuilder.toString());
		} else {
			log(Level.INFO, "The Z3 has completely finished.");
		}
		
	}
	
	/**
	 * {@inheritDoc}
	 * @param id - the DeviceId of the rtu
	 */
	public void handleSlaveDevice(String id) {
		Rtu rtu = null;
		try {
			
			if(id.equalsIgnoreCase("00000000")){ //this is the master
				
				rtu = getMeter();
				
			    // Loop over the Rtu's communicationSchedules
				for(int i = 0; i < rtu.getCommunicationSchedulers().size(); i++){
					CommunicationScheduler commSchedule = (CommunicationScheduler)rtu.getCommunicationSchedulers().get(i);
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
					log(Level.INFO, "No meter found with DeviceId: " + id);
				}
			}
			
		} catch (IOException e) {
			log(Level.INFO, e.getMessage());
		} catch (SQLException e) {
			log(Level.INFO, e.getMessage());
		} catch (BusinessException e) {
			if( e instanceof DuplicateException){
				log(Level.INFO, e.getMessage() + " The deviceId is probably not correctly filled in.");
			} else {
				log(Level.INFO, e.getMessage());
			}
		}
	}
	
	/**
	 * Execute the Masters Protocol. The protocol for the master is the {@link WebRTUZ3} protocol. 
	 * @param commSchedule
	 */
	private void doExecuteMaster(CommunicationScheduler commSchedule){
		doExecuteSlave(commSchedule, true);
	}
	
	/**
	 * Execute the slave devices
	 * @param commSchedule the CommunicationSchedule to execute
	 * @param useFixedZ3Protocol indicate whether to use the fixed {@link WebRTUZ3} protocol or not
	 */
	private void doExecuteSlave(CommunicationScheduler commSchedule, boolean useFixedZ3Protocol){
		Rtu rtu = commSchedule.getRtu();
		try {
			if((commSchedule.getNextCommunication()!=null)&&(commSchedule.getNextCommunication().before(Calendar.getInstance(rtu.getDeviceTimeZone()).getTime()))
					&&!commSchedule.equals(getCommunicationScheduler())){
				
				log(Level.INFO, "Starting to handle meter \'" + rtu + "\'");
				
				getLink().getStreamConnection().write(rtu.getPostDialCommand()+"\r\n",500);
				
				executeProtocol(commSchedule, rtu, useFixedZ3Protocol);
			    
				Rtu master = getMasterForMeter(rtu.getDeviceId());
				if(master != null){
					if(rtu.getGateway() == null){
						rtu.updateGateway(master);
					} else if(!master.getDeviceId().equalsIgnoreCase(rtu.getGateway().getDeviceId())){
						rtu.updateGateway(master);
					}
				}
				
				log(Level.INFO, "Meter \'" + rtu + "\' has finished.");
			}
		} catch (IOException e) {
			if(e instanceof SocketException){
				failingSlaves.add("- Meter with deviceId " + rtu.getDeviceId() + " has failed, a SocketException occurred.\r\n");
			} else {
				log(Level.FINEST, e.getMessage());
				failingSlaves.add(logErrorDuringDBUpdate(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
			}
				
		} catch (SQLException e) {
			log(Level.FINEST, e.getMessage());
			failingSlaves.add(logErrorDuringDBUpdate(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
		} catch (BusinessException e) {
			log(Level.FINEST, e.getMessage());
			failingSlaves.add(logErrorDuringDBUpdate(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
		}
	}
	
	/**
	 * Handles the protocol. The rtu properties are added before the generic execute method is called
	 * @param commSchedule - the {@link CommunicationScheduler} to execute
	 * @param rtu - the rtu we are dealing with
	 * @param useFixedZ3Protocol - indicates to use the WebRTUZ3 protocol(for the master)
	 * @throws SQLException if a database error occurred when we update the journal
	 * @throws BusinessException if a business error occurred when we update the journal
	 */
	private void executeProtocol(CommunicationScheduler commSchedule, Rtu rtu, boolean useFixedZ3Protocol) throws SQLException, BusinessException{
	    Integer completionCode = AmrJournalEntry.CC_OK;
	    String errorMessage = "";
	    long connectTime = System.currentTimeMillis();
	    try{
	    	commSchedule.startCommunication();
	    	log(Level.INFO, "modem dialing "+ getMeter().getPhoneNumber() + "("+ rtu.getPostDialCommand() +")");
	    	
	    	Properties props = rtu.getProperties();
	    	if(useFixedZ3Protocol){ // then it's the master and you MUST use the WebRTUZ3 protocol
				WebRTUZ3 wZ3 = new WebRTUZ3();
				/* We remove the WakeUp property so it only wakes up in the Gateway protocol and not in the WebRTUZ3 protocol */
				props.remove("WakeUp");	
				wZ3.addProperties(props);
				wZ3.execute(commSchedule, getLink(), getLogger());
	    	} else {	// it's a slave so you can execute his taskImpl
	    		
	            Class implementor = Class.forName(rtu.getRtuType().getProtocol().getJavaClassName());
	            Object obj = implementor.newInstance();
	            if(obj instanceof GenericProtocol){
	            	((GenericProtocol) obj).addProperties(props);
	            	((GenericProtocol) obj).execute(commSchedule, getLink(), getLogger());
	            }
	    	}
	    	
	    } catch (IOException e){
	    	completionCode = AmrJournalEntry.CC_IOERROR;
	    	errorMessage = e.getMessage();
	    	log(Level.INFO, errorMessage);
	    	failingSlaves.add(logErrorDuringCommunication(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
	    } catch (SQLException e){
	    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
	    	errorMessage = e.getMessage();
	    	log(Level.INFO, errorMessage);
	    	failingSlaves.add(logErrorDuringCommunication(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
	    } catch (BusinessException e){
	    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
	    	errorMessage = e.getMessage();
	    	log(Level.INFO, errorMessage);
	    	failingSlaves.add(logErrorDuringCommunication(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
		} catch (ClassNotFoundException e) {
	    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
	    	errorMessage = e.getMessage();
	    	log(Level.INFO, errorMessage);
	    	failingSlaves.add(logErrorDuringCommunication(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
		} catch (InstantiationException e) {
	    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
	    	errorMessage = e.getMessage();
	    	log(Level.INFO, errorMessage);
	    	failingSlaves.add(logErrorDuringCommunication(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
		} catch (IllegalAccessException e) {
	    	completionCode = AmrJournalEntry.CC_UNEXPECTED_ERROR;
	    	errorMessage = e.getMessage();
	    	log(Level.INFO, errorMessage);
	    	failingSlaves.add(logErrorDuringCommunication(rtu.getDeviceId(), commSchedule.getCommunicationProfile().getFullName()));
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
	}
	
	/**
	 * @param rtu the deviceId of the rtu
	 * @param commProfileName the name of the current communicationSchedule
	 * @return an error string
	 */
	private String logErrorDuringCommunication(String rtu, String commProfileName){
		return new MessageFormat(errorDuringCommunication).format(new Object[]{rtu, commProfileName});
	}
	
	/**
	 * @param rtu the deviceId of the rtu
	 * @param commprofileName the name of the current communicationSchedule
	 * @return an errorString
	 */
	private String logErrorDuringDBUpdate(String rtu, String commprofileName){
		return new MessageFormat(errorDuringDBUpdate).format(new Object[]{rtu, commprofileName});
	}
	
	/**
	 * Find the master of the meter with the given deviceId in the networkTopology
	 * @param deviceId of the slave meter
	 * @return the master meter(Rtu)
	 * @throws IOException if multiple meters were found in the database
	 * @throws SQLException if database exception occurred
	 * @throws BusinessException if business exception occurred
	 */
	private Rtu getMasterForMeter(String deviceId) throws IOException, SQLException, BusinessException{
		
		String masterDeviceId = getDeviceIdFromMaster(deviceId);
		if(masterDeviceId != null){
			if(masterDeviceId.equalsIgnoreCase("00000000")){ //this means the central master
				return getMeter();
			} else {
				return findOrCreateDeviceByDeviceId(masterDeviceId);
			}
		} else {
			return null;
		}
	}
	
	/**
	 * Find the deviceId of the master of a given slaveDeviceId
	 * @param deviceId - the slave his deviceId 
	 * @return the deviceId of the master or null if no networkNode was found with the given deviceId
	 */
	protected String getDeviceIdFromMaster(String deviceId){
		NetworkNode nn = getNetworkNodeForDeviceId(deviceId);
		if(nn != null){
			NetworkNode subMaster = getMasterFromNetwork(nn.getParentNetwork());
				String master = getOriginalManufacturerIdNotation(subMaster);
				if (master.equalsIgnoreCase(deviceId)){
					NetworkNode superNetSubMaster = getMasterFromNetwork(subMaster.getParentNetwork().getSupernet());
					return getOriginalManufacturerIdNotation(superNetSubMaster);
					
				} else {
					return master;
				}
			
		} else {
			return null;
		}
	}
	
	/**
	 * Get the master of the given network. If multiple masters were found then something is wrong in the mesh network.
	 * @param network - the network to search for the master
	 * @return the 'Master'(or 'SubMaster') networkNode
	 * @throws IllegalArgumentException if multiple masters/submasters were found in the network
	 */
	private NetworkNode getMasterFromNetwork(Network network){
		Set<NetworkNode> networkMaster = network.getNetworkMasters();
		if(networkMaster.size() != 1){
			throw new IllegalArgumentException("Number of masterNodes is different from 1 (" + networkMaster.size() + ")");
		} else {
			return networkMaster.iterator().next();
		}
	}
	
	
	/**
	 * Search the NetworkNode for the given deviceId
	 * @param deviceId the deviceId(rfclient) of a device
	 * @return the networknode or null
	 */
	private NetworkNode getNetworkNodeForDeviceId(String deviceId){
		for(NetworkNode nn:this.networkTopology.getRoot().getAllNodes()){
			if(getOriginalManufacturerIdNotation(nn).equalsIgnoreCase(deviceId)){
				return nn;
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
				updateR2WithPostDialCommandRfClient(device);
			}
			
			return device;
		} catch (InvalidPropertyException e) {
			log(Level.INFO, e.getMessage());
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
			log(Level.INFO, "No slaves were found on the device.");
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
		log(Level.INFO, "Requesting network topology from Z3.");
		
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
				
				log(Level.INFO, "Got routing table \n[" + stringBuilder.toString() + "] from the device.");
				
				return stringBuilder.toString();
			} else {
				log(Level.WARNING, "Query for OBIS code [" + OBIS_CODE_NETWORK_TOPOLOGY + "] did not yield any result, assuming no RF available.");
			}
			
			// The EpIO did not return anything when requested for the particular register, so we do neither.
			return null;
		} catch (final DataAccessResultException e) {
			if (e.getCode() == DataAccessResultCode.OBJECT_UNDEFINED) {
				// No such register.
				log(Level.INFO, "The EpIO says there is no register with Obis code [" + OBIS_CODE_NETWORK_TOPOLOGY + "], assuming it is not acting as an RF master.");
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
				|| (rtu.getPostDialCommand().equalsIgnoreCase("<ESC>rfclient=\"\"</ESC>"))){
			
			RtuShadow shadow = rtu.getShadow();
			String deviceId = rtu.getDeviceId();
			if((deviceId != null) && !(deviceId.equalsIgnoreCase(""))){
				shadow.setPostDialCommand("<ESC>rfclient=\"" + deviceId +"\"</ESC>");
				rtu.update(shadow);
			} else {
				throw new InvalidPropertyException("DeviceId of rtu " + rtu + " is empty, can't update postDialCommand.");
			}
			
		}
	}
	
	@Override
	public List getMessageCategories() {
		List<MessageCategorySpec> categories = new ArrayList();
		MessageCategorySpec catXMLConfig = getXmlConfigCategory();
		MessageCategorySpec catFirmware = getFirmwareCategory();
		MessageCategorySpec catP1Messages = getP1Category();
		MessageCategorySpec catDisconnect = getConnectControlCategory();
		MessageCategorySpec catLoadLimit = getLoadLimitCategory();
		MessageCategorySpec catActivityCal = getActivityCalendarCategory();
		MessageCategorySpec catTime = getTimeCategory();
		MessageCategorySpec catMakeEntries = getDataBaseEntriesCategory();
		MessageCategorySpec catTestMessage = getTestCategory();
		MessageCategorySpec catGlobalDisc = getGlobalResetCategory();
		MessageCategorySpec catAuthEncrypt = getAuthEncryptCategory();
		MessageCategorySpec catConnectivity = getConnectivityCategory();
		
		categories.add(catXMLConfig);
		categories.add(catFirmware);
		categories.add(catP1Messages);
		categories.add(catDisconnect);
		categories.add(catLoadLimit);
		categories.add(catActivityCal);
		categories.add(catTime);
		categories.add(catMakeEntries);
		categories.add(catTestMessage);
		categories.add(catGlobalDisc);
		categories.add(catConnectivity);
		
		categories.add(catAuthEncrypt);

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
	
	/**
	 * @return the messages for the ConnectivityCategory
	 */
	private MessageCategorySpec getConnectivityCategory() {
		MessageCategorySpec catGPRSModemSetup = new MessageCategorySpec(
				RtuMessageCategoryConstants.CHANGECONNECTIVITY);
		MessageSpec msgSpec = addChangeGPRSSetup(
				RtuMessageKeyIdConstants.GPRSMODEMSETUP,
				RtuMessageConstant.GPRS_MODEM_SETUP, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addPhoneListMsg(RtuMessageKeyIdConstants.SETWHITELIST,
				RtuMessageConstant.WAKEUP_ADD_WHITELIST, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.ACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_ACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		msgSpec = addNoValueMsg(RtuMessageKeyIdConstants.DEACTIVATESMSWAKEUP,
				RtuMessageConstant.WAKEUP_DEACTIVATE, false);
		catGPRSModemSetup.addMessageSpec(msgSpec);
		return catGPRSModemSetup;
	}
	
	/**
	 * Get the ComPort the ComServer is using for the Z3 communication
	 * @return current schedulers {@link ComPortShadow}
	 */
	private ComPortShadow getCurrentComportSchadow(){
		return getCommunicationScheduler().getComPort().getShadow();
	}
	
	/**
	 * Setter for the CosemObjectFactory, mainly for testing purposes
	 * @param cosemObjectFactory - the given CosemObjectFactory
	 */
	protected void setterForCosemObjectFactory(CosemObjectFactory cosemObjectFactory){
		setCosemObjectFactory(cosemObjectFactory);
	}
	
	/**
	 * Setter for the DLMSConnection, mainly for testing purposes
	 * @param dlmsConnection - the given DLMSConnection
	 */
	protected void setterForDLMSConnection(DLMSConnection dlmsConnection){
		setDLMSConnection(dlmsConnection);
	}
	
	/**
	 * Setter for the Logger, mainly for testing purposes
	 * @param logger - the given Logger
	 */
	protected void setterForLogger(Logger logger){
		setLogger(logger);
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
		this.slaveRtuType = getProperties().getProperty("RtuType");
		this.folderExtName = getProperties().getProperty("FolderExtName");
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
}
