package com.energyict.genericprotocolimpl.gatewayz3;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.energyict.cbo.BusinessException;
import com.energyict.cbo.NestedIOException;
import com.energyict.commserverj.FileFormatter;
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
import com.energyict.mdw.core.AmrJournalEntry;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.MeteringWarehouseFactory;
import com.energyict.mdw.core.Rtu;
import com.energyict.mdw.shadow.ComPortShadow;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;


/**
 * <p>
 * Implements the GateWay Z3 protocol. The Z3 will act as a Master/Gateway in an RF-Mesh network with a certain amount of
 * R2 slave devices.
 * After fetching the "routingTable" of the Z3, the protocol will handle each R2 one by one. The handling of the R2's will depend on
 * the nextCommunicationDate of his commSchedulers.
 * </p> 
 * 
 * @author gna
 * @since 21 October 2009
 */
public class GateWayZ3 extends DLMSProtocol implements ConcentratorProtocol{
	
	/** Helper to rapidly fetch data without a Z3/R2 */
	private boolean TESTING = true;
	
	/** Obis code we can use to request the RF network topology. */
	private static final ObisCode OBIS_CODE_NETWORK_TOPOLOGY = ObisCode.fromString("0.128.3.0.8.255");
	
	/** The current NetworkTopology from the Z3 */
	private NetworkTopology networkTopology;
	
	/* Properties */
	private String slaveRtuType;
	private String folderExtName;

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
		// Need to close the DLMS association
		// TODO Maybe just close the association and don't disconnect the MAC ...
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
		long connectTime = System.currentTimeMillis();
		Integer completionCode = null;
		String errorMessage = "";
		try {
			
			try {
				rtu = CommonUtils.findOrCreateDeviceByDeviceId(id, slaveRtuType, folderExtName);
			} catch (IllegalArgumentException e) {
				//TODO to test
				logger.log(Level.INFO, e.getMessage());
			}
			
			if(rtu != null){
				
				logger.log(Level.INFO, "Starting to handle meter \'" + rtu + "\'");
			    
			    // Loop over the Rtu's communicationSchedules
//				for(CommunicationScheduler commSchedule : rtu.getCommunicationSchedulers()){
				for(int i = 0; i < rtu.getCommunicationSchedulers().size(); i++){
					CommunicationScheduler commSchedule = (CommunicationScheduler)rtu.getCommunicationSchedulers().get(i);
					
					if(commSchedule.getNextCommunication().before(Calendar.getInstance(rtu.getDeviceTimeZone()).getTime())){
						CommunicationSchedulerFullShadow csfs = CommunicationSchedulerFullShadowBuilder.getCommunicationSchedulerFullShadow(commSchedule);
						csfs.setDialerFactory(communicationScheduler.getDialerFactory());
						TaskImpl ti = new TaskImpl(csfs, getCurrentComportSchadow());
						
						//TODO
						//TODO
						//TODO
						//TODO Not correct yet!!
//						this.link.getStreamConnection().close();
//						this.link.setStreamConnection(new SocketStreamConnection(getMeter().getPhoneNumber()));
//						this.link.getStreamConnection().open();
	//					((Dialer)this.link).connect(ti.getPhoneNumber(), ti.getPostDialCommand(), 90000);
							
						Handler fhSimple = new FileHandler("FileName", true);
			            getLogger().addHandler(fhSimple);
			            fhSimple.setFormatter(new FileFormatter());
			            fhSimple.setLevel(Level.ALL);
			            
						// TODO send the postDialCommand
			            	
			            completionCode = AmrJournalEntry.CC_OK;
			            errorMessage = "";
						// TODO check if all parameters are correct
			            try{
			            	ti.execute(this.link, false, false, logger);
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
			    					if(completionCode == AmrJournalEntry.CC_OK){
			    						amrjm.updateLastCommunication();
			    					} else {
			    						amrjm.journal(new AmrJournalEntry(completionCode, errorMessage));
			    						amrjm.updateRetrials();
			    					}
			    					
			    				}
			    			}
			    		}
						
						
					//TODO update gateway
					}
				}		
				logger.log(Level.INFO, "Meter \'" + rtu + "\' has finished.");
			} else {
				logger.log(Level.INFO, "No meter found with DeviceId: " + id);
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
	
	/**
	 * {@inheritDoc}
	 */
	public List<String> getSlaveDevices() throws IOException {
		
		String nt = getNetworkTopology();
			
		this.networkTopology = NetworkTopology.parse(new ManufacturerId(0), nt, null);
		
		List<String> allNodes = new ArrayList<String>();
		
		for(NetworkNode nn:this.networkTopology.getRoot().getAllNodes()){
			allNodes.add(getOriginalManufacturerIdNotation(nn));
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
		return "$Date";
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
		// TODO Auto-generated method stub
		
	}

	@Override
	protected RegisterValue readRegister(ObisCode obisCode) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List getMessageCategories() {
		// TODO Auto-generated method stub
		return null;
	}
}
