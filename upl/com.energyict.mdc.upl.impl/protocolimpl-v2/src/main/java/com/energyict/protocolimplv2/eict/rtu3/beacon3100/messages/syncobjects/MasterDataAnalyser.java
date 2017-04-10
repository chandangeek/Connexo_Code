package com.energyict.protocolimplv2.eict.rtu3.beacon3100.messages.syncobjects;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.cosem.ClientTypeManager;
import com.energyict.dlms.cosem.ConcentratorSetup;
import com.energyict.dlms.cosem.ConcentratorSetup.MeterInfo;
import com.energyict.dlms.cosem.DeviceTypeManager;
import com.energyict.dlms.cosem.SAPAssignment;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.dlms.cosem.ScheduleManager;

/**
 * Helper class to analyse everything which has to be added / updated / removed while synchronizing Beacon master data.
 * The analysis will use the HES master-data and existing Beacon master-data while deciding what to do with each item.
 * The items used by other Beacon mirrors and not present in EIServer will be preserved.
 */
public final class MasterDataAnalyser {
	
	/** Prefix of a logical device name of a mirror device. */
	private static final String SAP_ITEM_MIRROR_PREFIX = "ELS-MIR";
	
	/**
	 * Defines a sync action.
	 * 
	 * @author alex
	 *
	 * @param <T>
	 */
	public interface SyncAction<T> {
		
		/**
		 * Executes the action.
		 * 
		 * @throws IOException
		 */
		void execute() throws IOException;
	}
	
	/**
	 * Supplier for an ActionSet.
	 * 
	 * @author alex
	 *
	 * @param <T>
	 */
	public interface ActionSetSupplier<T> {
		
		/**
		 * Creates an {@link ActionSet}.
		 * 
		 * @param 	objectsToAdd			The objects that need adding.
		 * @param 	objectsToUpdate			The objects that need removing.
		 * @param 	objectsToRemove			The objects that need updating.
		 * 
		 * @return	The corresponding {@link ActionSet}.
		 */
		ActionSet<T> createActionSet(final Set<T> objectsToAdd, final Set<T> objectsToUpdate, final Set<T> objectsToRemove);
	}
	
	/**
	 * Action that deletes a schedule.
	 * 
	 * @author alex
	 */
	private final class DeleteScheduleAction implements SyncAction<Beacon3100Schedule> {

		/** The schedule to delete. */
		private final Beacon3100Schedule schedule;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		schedule		The schedule.
		 */
		private DeleteScheduleAction(final Beacon3100Schedule schedule) {
			this.schedule = schedule;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			scheduleManager.removeSchedule(this.schedule.getId());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Delete schedule [ID ").append(this.schedule.getId()).append(", name ").append(this.schedule.getName()).append("]").toString();
		}
	}
	
	/**
	 * Action that creates a schedule.
	 * 
	 * @author alex
	 */
	private final class CreateScheduleAction implements SyncAction<Beacon3100Schedule> {

		/** The schedule to delete. */
		private final Beacon3100Schedule schedule;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		schedule		The schedule.
		 */
		private CreateScheduleAction(final Beacon3100Schedule schedule) {
			this.schedule = schedule;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			scheduleManager.addSchedule(this.schedule.toStructure());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Create schedule [ID ").append(this.schedule.getId()).append(", name ").append(this.schedule.getName()).append("]").toString();
		}
	}
	
	/**
	 * Delete a mirror on the concentrator.
	 * 
	 * @author alex
	 */
	private final class DeleteMirrorAction implements SyncAction<Void> {
		
		/** MAC of the meter to remove. */
		private final byte[] mac;
		
		/**
		 * Create a new instance.
		 * 	
		 * @param	mac		MAC of the mirror to delete.
		 */
		private DeleteMirrorAction(final byte[] mac) {
			this.mac = mac;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			concentratorSetup.removeLogicalDevice(this.mac);
		}
	}
	
	/**
	 * Action that updates a schedule.
	 * 
	 * @author alex
	 */
	private final class UpdateScheduleAction implements SyncAction<Beacon3100Schedule> {

		/** The schedule to delete. */
		private final Beacon3100Schedule schedule;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		schedule		The schedule.
		 */
		private UpdateScheduleAction(final Beacon3100Schedule schedule) {
			this.schedule = schedule;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			scheduleManager.updateSchedule(this.schedule.toStructure());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Update schedule [ID ").append(this.schedule.getId()).append(", name ").append(this.schedule.getName()).append("]").toString();
		}
	}
	
	/**
	 * Action that deletes a client type.
	 * 
	 * @author alex
	 */
	private final class DeleteClientTypeAction implements SyncAction<Beacon3100ClientType> {

		/** The schedule to delete. */
		private final Beacon3100ClientType clientType;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		clientType		The client type.
		 */
		private DeleteClientTypeAction(final Beacon3100ClientType clientType) {
			this.clientType = clientType;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			clientTypeManager.removeClientType(this.clientType.getId());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Delete client type [ID ").append(this.clientType.getId()).append(", client ID ").append(this.clientType.getClientMacAddress()).append("]").toString();
		}
	}

	/**
	 * Action that adds a client type.
	 * 
	 * @author alex
	 */
	private final class AddClientTypeAction implements SyncAction<Beacon3100ClientType> {

		/** The schedule to delete. */
		private final Beacon3100ClientType clientType;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		clientType		The client type.
		 */
		private AddClientTypeAction(final Beacon3100ClientType clientType) {
			this.clientType = clientType;
			this.clientType.setIsFirmware140orAbove(fwVersionAbove1dot4);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			clientTypeManager.addClientType(this.clientType.toStructure());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Create client type [ID ").append(this.clientType.getId()).append(", client ID ").append(this.clientType.getClientMacAddress()).append("]").toString();
		}
	}
	
	/**
	 * Action that updates a client type.
	 * 
	 * @author alex
	 */
	private final class UpdateClientTypeAction implements SyncAction<Beacon3100ClientType> {

		/** The schedule to delete. */
		private final Beacon3100ClientType clientType;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		clientType		The client type.
		 */
		private UpdateClientTypeAction(final Beacon3100ClientType clientType) {
			this.clientType = clientType;
			this.clientType.setIsFirmware140orAbove(fwVersionAbove1dot4);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			clientTypeManager.updateClientType(this.clientType.toStructure());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Update client type [ID ").append(this.clientType.getId()).append(", client ID ").append(this.clientType.getClientMacAddress()).append("]").toString();
		}
	}
	
	/**
	 * Action that updates a device type.
	 * 
	 * @author alex
	 */
	private final class UpdateDeviceTypeAction implements SyncAction<Beacon3100DeviceType> {

		/** The schedule to delete. */
		private final Beacon3100DeviceType deviceType;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		deviceType		The device type.
		 */
		private UpdateDeviceTypeAction(final Beacon3100DeviceType deviceType) {
			this.deviceType = deviceType;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			final Structure structure = fwVersionAbove1dot10 ? this.deviceType.toStructure(false) : this.deviceType.toStructure(true);
			
			deviceTypeManager.updateDeviceType(structure);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Update device type [ID ").append(this.deviceType.getId()).append(", name ").append(this.deviceType.getName()).append("]").toString();
		}
	}
	
	/**
	 * Action that deletes a device type.
	 * 
	 * @author alex
	 */
	private final class DeleteDeviceTypeAction implements SyncAction<Beacon3100DeviceType> {

		/** The schedule to delete. */
		private final Beacon3100DeviceType deviceType;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		deviceType		The device type.
		 */
		private DeleteDeviceTypeAction(final Beacon3100DeviceType deviceType) {
			this.deviceType = deviceType;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			deviceTypeManager.removeDeviceType(this.deviceType.getId());
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Delete device type [ID ").append(this.deviceType.getId()).append(", name ").append(this.deviceType.getName()).append("]").toString();
		}
	}
	
	/**
	 * Action that deletes a device type.
	 * 
	 * @author alex
	 */
	private final class AddDeviceTypeAction implements SyncAction<Beacon3100DeviceType> {

		/** The schedule to delete. */
		private final Beacon3100DeviceType deviceType;
		
		/**
		 * Create a new instance.
		 * 
		 * @param 		deviceType		The device type.
		 */
		private AddDeviceTypeAction(final Beacon3100DeviceType deviceType) {
			this.deviceType = deviceType;
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final void execute() throws IOException {
			final Structure structure = fwVersionAbove1dot10 ? this.deviceType.toStructure(false) : this.deviceType.toStructure(true);
			
			deviceTypeManager.addDeviceType(structure);
		}
		
		/**
		 * {@inheritDoc}
		 */
		@Override
		public final String toString() {
			return new StringBuilder("Create device type [ID ").append(this.deviceType.getId()).append(", name ").append(this.deviceType.getName()).append("]").toString();
		}
	}
	
	/**
	 * Groups the actions for a particular object type.
	 * 
	 * @author 	alex
	 *
	 * @param 	<T>		The type of object.
	 */
	private static final class ActionSet<T> {
		
		/** Delete actions. */
		private final Set<SyncAction<T>> deleteActions;
		
		/** Add actions. */
		private final Set<SyncAction<T>> addActions;
		
		/** Update actions. */
		private final Set<SyncAction<T>> updateActions;
		
		/**
		 * Create a new instance.
		 * 
		 * @param addActions
		 * @param updateActions
		 * @param deleteActions
		 */
		private ActionSet(final Set<SyncAction<T>> addActions,
						  final Set<SyncAction<T>> updateActions,
						  final Set<SyncAction<T>> deleteActions) {
			this.addActions = addActions;
			this.updateActions = updateActions;
			this.deleteActions = deleteActions;
		}

		/** 
		 * Returns the delete actions.
		 * 
		 * @return		The delete actions.
		 */
		public final Set<SyncAction<T>> getDeleteActions() {
			return Collections.unmodifiableSet(this.deleteActions);
		}

		/**
		 * Returns the add actions.
		 * 
		 * @return		The add actions.
		 */
		public final Set<SyncAction<T>> getAddActions() {
			return Collections.unmodifiableSet(this.addActions);
		}

		/**
		 * Returns the update actions.
		 * 
		 * @return	The update actions.
		 */
		public final Set<SyncAction<T>> getUpdateActions() {
			return Collections.unmodifiableSet(this.updateActions);
		}
	}
	
	/** The {@link ScheduleManager}. */
	private final ScheduleManager scheduleManager;
	
	/** The {@link DeviceTypeManager}. */
	private final DeviceTypeManager deviceTypeManager;
	
	/** The {@link ClientTypeManager}. */
	private final ClientTypeManager clientTypeManager;
	
	/** The {@link ConcentratorSetup}. */
	private final ConcentratorSetup concentratorSetup;
	
	/** The {@link SAPAssignment}. */
	private final SAPAssignment sapAssignment;
	
	/** The HES master data. */
	private final AllMasterData masterData;
	
	/** Indicates the fw version is > 1.4. */
	private final boolean fwVersionAbove1dot4;
	
	/** Indicates the fw version is > 1.10. */
	private final boolean fwVersionAbove1dot10;
	
	/**
	 * Create a new instance.
	 * 
	 * @param 	masterData					The master data in the HES.
	 * @param 	scheduleManager				The {@link ScheduleManager}.
	 * @param 	deviceTypeManager			The {@link DeviceTypeManager}.
	 * @param 	clientTypeManager			The {@link ClientTypeManager}.
	 * @param 	concentratorSetup			The {@link ConcentratorSetup}.
	 * @param	sapAssigment				The {@link SAPAssignment}.
	 * @param	fwVersionHigherThan1dot4	Indicates whether or not the firmware version is higher than 1.4.
	 * @param	fwVersionHigherThan1dot10	Indicates whether or not the firmware version is higher than 1.10.
	 */
	public MasterDataAnalyser(final AllMasterData masterData,
							  final ScheduleManager scheduleManager,
							  final DeviceTypeManager deviceTypeManager,
							  final ClientTypeManager clientTypeManager,
							  final ConcentratorSetup concentratorSetup,
							  final SAPAssignment sapAssigment,
							  final boolean fwVersionHigherThan1dot4,
							  final boolean fwVersionHigherThan1dot10) {
		this.masterData = masterData;
		this.scheduleManager = scheduleManager;
		this.concentratorSetup = concentratorSetup;
		this.deviceTypeManager = deviceTypeManager;
		this.clientTypeManager = clientTypeManager;
		this.sapAssignment = sapAssigment;
		this.fwVersionAbove1dot4 = fwVersionHigherThan1dot4;
		this.fwVersionAbove1dot10 = fwVersionHigherThan1dot10;
	}
	
	/**
	 * Generates the {@link ActionSet} for device types.
	 * 
	 * @param 		mirrors		The mirrors on the concentrator.
	 * 
	 * @return		The {@link ActionSet}.
	 */
	private final <T> ActionSet<T> generateActions(final Set<T> objectsInHES,
												   final Set<T> objectsInBeaconMasterData,
												   final Set<T> objectsUsedByBeaconMirrors,
												   final ActionSetSupplier<T> actionSetSupplier) {
		// Stuff that's in the HES, not on the DC.
		final Set<T> objectsToAdd = new HashSet<>();
		objectsToAdd.addAll(objectsInHES);
		objectsToAdd.removeAll(objectsInBeaconMasterData);
		
		// Stuff that's in the HES, and the DC.
		final Set<T> objectsToUpdate = new HashSet<>();
		objectsToUpdate.addAll(objectsInHES);
		objectsToUpdate.retainAll(objectsInBeaconMasterData);
		
		// Stuff that's on the DC, not the HES, except for stuff we can't delete yet.
		final Set<T> objectsToKeep = new HashSet<>();
		objectsToKeep.addAll(objectsInHES);
		objectsToKeep.addAll(objectsUsedByBeaconMirrors);
		
		final Set<T> objectsToDelete = new HashSet<>();
		objectsToDelete.addAll(objectsInBeaconMasterData);
		objectsToDelete.removeAll(objectsToKeep);
		
		return actionSetSupplier.createActionSet(objectsToAdd, objectsToUpdate, objectsToDelete);
	}
	
	/**
	 * Generates the {@link List} of actions needed to clean up dangling mirrors. They could be leftovers from the clear nodelist for example, and are no longer present in the meter info.
	 * 
	 * @return	A list of actions.
	 */
	private final List<SyncAction<?>> generateMirrorCleanupActions(final List<MeterInfo> meterInfo) throws IOException {
		final List<SyncAction<?>> mirrorDeleteActions = new ArrayList<>();
		
		for (final SAPAssignmentItem sapAssignmentItem : this.sapAssignment.getSapAssignmentList()) {
			if (sapAssignmentItem.getLogicalDeviceName().startsWith(SAP_ITEM_MIRROR_PREFIX) &&
				sapAssignmentItem.getLogicalDeviceNameBytes().length == 16) {
				final byte[] macAddress = Arrays.copyOfRange(sapAssignmentItem.getLogicalDeviceNameBytes(), 8, 16);
				
				boolean mirrorKnown = false;
				
				for (final MeterInfo mirror : meterInfo) {
					if (Arrays.equals(mirror.getId(), macAddress)) {
						mirrorKnown = true;
						
						break;
					}
				}
				
				if (!mirrorKnown) {
					// Mirror that's not in the meter-info list, will need to delete it.
					mirrorDeleteActions.add(new DeleteMirrorAction(macAddress));
				}
			}
		}
		
		return mirrorDeleteActions;
	}
	
	/**
	 * Returns the device types contained in the Beacon master data.
	 * 
	 * @return	The device types contained in the beacon master data.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	private final Set<Beacon3100DeviceType> getDeviceTypesInBeaconMasterdata() throws IOException {
		final Set<Beacon3100DeviceType> masterDataDeviceTypes = new HashSet<>();
		
		final Array deviceTypeArray = this.deviceTypeManager.readDeviceTypes();
		
		for (final AbstractDataType deviceTypeStructure : deviceTypeArray) {
			masterDataDeviceTypes.add(Beacon3100DeviceType.fromStructure(deviceTypeStructure.getStructure()));
		}
		
		return masterDataDeviceTypes;
	}
	
	/**
	 * Returns the schedules contained in the Beacon master data.
	 * 
	 * @return	The schedules contained in the beacon master data.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	private final Set<Beacon3100Schedule> getSchedulesInBeaconMasterdata() throws IOException {
		final Set<Beacon3100Schedule> masterDataSchedules = new HashSet<>();
		
		final Array scheduleArray = this.scheduleManager.readSchedules();
		
		for (final AbstractDataType deviceTypeStructure : scheduleArray) {
			masterDataSchedules.add(Beacon3100Schedule.fromStructure(deviceTypeStructure.getStructure()));
		}
		
		return masterDataSchedules;
	}
	
	/**
	 * Returns the {@link Set} of client types in the Beacon master data.
	 * 
	 * @return	The {@link Set} of client types in the Beacon master data.
	 * 
	 * @throws 	IOException		If an IO error occurs.
	 */
	private final Set<Beacon3100ClientType> getClientTypesInBeaconMasterdata() throws IOException {
		final Set<Beacon3100ClientType> masterDataClientTypes = new HashSet<>();
		
		final Array clientTypeArray = this.clientTypeManager.readClients();
		
		for (final AbstractDataType clientTypeStructure : clientTypeArray) {
			masterDataClientTypes.add(Beacon3100ClientType.fromStructure(clientTypeStructure.getStructure()));
		}
		
		return masterDataClientTypes;
	}
	
	/**
	 * Returns the {@link Set} of device types in use by Beacon mirrors. We need these because while the device type can technically be removed, the Beacon will refuse because the mirrors
	 * have not been updated yet.
	 *  
	 * @param 	mirrors								The mirrors on the Beacon.
	 * @param 	deviceTypesInBeaconMasterdata		The device types on the Beacon.
	 * 
	 * @return	The {@link Set} of device types that are actually in use.
	 */
	private final Set<Beacon3100DeviceType> getDeviceTypesUsedByBeaconMirrors(final List<ConcentratorSetup.MeterInfo> mirrors,
																			  final Set<Beacon3100DeviceType> deviceTypesInBeaconMasterdata) {
		final Set<Beacon3100DeviceType> deviceTypes = new HashSet<>();
		
		for (final ConcentratorSetup.MeterInfo mirror : mirrors) {
			for (final ConcentratorSetup.DeviceTypeAssignment deviceTypeAssignment : mirror.getDeviceTypeAssignments()) {
				for (final Beacon3100DeviceType deviceType : deviceTypesInBeaconMasterdata) {
					if (deviceType.getId() == deviceTypeAssignment.getDeviceTypeId()) {
						deviceTypes.add(deviceType);
						
						break;
					}
				}
			}
		}
		
		return deviceTypes;
	}
	
	/**
	 * Returns the {@link Set} of client types in use by mirrors on the Beacon.
	 * 
	 * @param 	mirrors								The mirrors on the Beacon.
	 * @param 	deviceTypesInBeaconMasterdata		The device types in the master data on the Beacon.
	 * @param 	clientTypesInBeaconMasterdata		The client types in the master data on the Beacon.
	 * 
	 * @return	The {@link Set} of client types that are used by mirrors on the Beacon.
	 */
	private final Set<Beacon3100ClientType> getClientTypesUsedByBeaconMirrors(final List<ConcentratorSetup.MeterInfo> mirrors,
																			  final Set<Beacon3100DeviceType> deviceTypesInBeaconMasterdata,
			  																  final Set<Beacon3100ClientType> clientTypesInBeaconMasterdata) {
		final Set<Beacon3100ClientType> clientTypes = new HashSet<>();
		
		for (final ConcentratorSetup.MeterInfo mirror : mirrors) {
			for (final ConcentratorSetup.DeviceTypeAssignment deviceTypeAssignment : mirror.getDeviceTypeAssignments()) {
				Beacon3100DeviceType currentDeviceType = null;
				
				for (final Beacon3100DeviceType deviceType : deviceTypesInBeaconMasterdata) {
					if (deviceType.getId() == deviceTypeAssignment.getDeviceTypeId()) {
						currentDeviceType = deviceType;
						
						break;
					}
				}
				
				if (currentDeviceType != null) {
					for (final Beacon3100Schedulable schedulable : currentDeviceType.getSchedulables()) {
						for (final Beacon3100ClientType clientType : clientTypesInBeaconMasterdata) {
							if (clientType.getId() == schedulable.getClientTypeId()) {
								clientTypes.add(clientType);
								
								break;
							}
						}
					}
				}
			}
		}
		
		return clientTypes;
	}
	
	/**
	 * Returns the {@link Set} of {@link Beacon3100Schedule}.
	 * 
	 * @param 	mirrors								The mirrors.
	 * @param 	deviceTypesInBeaconMasterdata		The device types in the beacon master data.
	 * @param 	schedulesInBeaconMasterdata			The schedules in the beacon master data.
	 * 
	 * @return	The {@link Set} of {@link Beacon3100Schedule}s used by the mirrors.
	 */
	private final Set<Beacon3100Schedule> getSchedulesUsedByBeaconMirrors(final List<ConcentratorSetup.MeterInfo> mirrors,
			  															  final Set<Beacon3100DeviceType> deviceTypesInBeaconMasterdata,
			  															  final Set<Beacon3100Schedule> schedulesInBeaconMasterdata) {
		final Set<Beacon3100Schedule> schedules = new HashSet<>();
		
		for (final ConcentratorSetup.MeterInfo mirror : mirrors) {
			for (final ConcentratorSetup.DeviceTypeAssignment deviceTypeAssignment : mirror.getDeviceTypeAssignments()) {
				Beacon3100DeviceType currentDeviceType = null;
				
				for (final Beacon3100DeviceType deviceType : deviceTypesInBeaconMasterdata) {
					if (deviceType.getId() == deviceTypeAssignment.getDeviceTypeId()) {
						currentDeviceType = deviceType;
						
						break;
					}
				}
				
				if (currentDeviceType != null) {
					for (final Beacon3100Schedulable schedulable : currentDeviceType.getSchedulables()) {
						for (final Beacon3100Schedule schedule : schedulesInBeaconMasterdata) {
							if (schedule.getId() == schedulable.getScheduleId()) {
								schedules.add(schedule);
								
								break;
							}
						}
					}
				}
			}
		}
		
		return schedules;
	}
	
	/**
	 * Returns the {@link Set} of {@link Beacon3100DeviceType}s in the HES master data.
	 * 
	 * @param 		masterData		The HES master data.
	 * 
	 * @return		The {@link Set} of {@link Beacon3100DeviceType}s in the HES master data.
	 */
	private final Set<Beacon3100DeviceType> getDeviceTypesInHES(final AllMasterData masterData) {
		final Set<Beacon3100DeviceType> deviceTypes = new HashSet<>();
		
		deviceTypes.addAll(masterData.getDeviceTypes());
		
		return deviceTypes;
	}
	
	/**
	 * Returns the {@link Set} of {@link Beacon3100Schedule}s in the HES master data.
	 * 
	 * @param 		masterData		The HES master data.
	 * 
	 * @return		The {@link Set} of {@link Beacon3100Schedule}s in the HES master data.
	 */
	private final Set<Beacon3100Schedule> getSchedulesInHES(final AllMasterData masterData) {
		final Set<Beacon3100Schedule> schedules = new HashSet<>();
		
		schedules.addAll(masterData.getSchedules());
		
		return schedules;
	}
	
	/**
	 * Returns the {@link Set} of {@link Beacon3100ClientType}s in the HES master data.
	 * 
	 * @param 		masterData		The HES master data.
	 * 
	 * @return		The {@link Set} of {@link Beacon3100ClientType}s in the HES master data.
	 */
	private final Set<Beacon3100ClientType> getClientTypesInHES(final AllMasterData masterData) {
		final Set<Beacon3100ClientType> clientTypes = new HashSet<>();
		
		clientTypes.addAll(masterData.getClientTypes());
		
		return clientTypes;
	}
 	
	/**
	 * Returns the sync plan.
	 * 
	 * @return
	 */
	public final List<SyncAction<?>> analyze() throws IOException {
		// Prepare what we need.
		final Set<Beacon3100Schedule> schedulesInHES = this.getSchedulesInHES(this.masterData);
		final Set<Beacon3100Schedule> schedulesInBeaconMasterdata = this.getSchedulesInBeaconMasterdata();
		
		final Set<Beacon3100ClientType> clientTypesInHES = this.getClientTypesInHES(this.masterData);
		final Set<Beacon3100ClientType> clientTypesInBeaconMasterdata = this.getClientTypesInBeaconMasterdata();
		
		final Set<Beacon3100DeviceType> deviceTypesInHES = this.getDeviceTypesInHES(this.masterData);
		final Set<Beacon3100DeviceType> deviceTypesInBeaconMasterdata = this.getDeviceTypesInBeaconMasterdata();
		
		final List<ConcentratorSetup.MeterInfo> mirrors = this.concentratorSetup.getMeterInfo();
		
		final Set<Beacon3100Schedule> schedulesUsedByBeaconMirrors = this.getSchedulesUsedByBeaconMirrors(mirrors, deviceTypesInBeaconMasterdata, schedulesInBeaconMasterdata);
		final Set<Beacon3100ClientType> clientTypesUsedByBeaconMirrors = this.getClientTypesUsedByBeaconMirrors(mirrors, deviceTypesInBeaconMasterdata, clientTypesInBeaconMasterdata);
		final Set<Beacon3100DeviceType> deviceTypesUsedByBeaconMirrors = this.getDeviceTypesUsedByBeaconMirrors(mirrors, deviceTypesInBeaconMasterdata);
		
		// Schedules.
		final ActionSet<Beacon3100Schedule> scheduleActionSet = this.generateActions(schedulesInHES, schedulesInBeaconMasterdata, schedulesUsedByBeaconMirrors, new ActionSetSupplier<Beacon3100Schedule>() {
			@Override
			public final ActionSet<Beacon3100Schedule> createActionSet(final Set<Beacon3100Schedule> objectsToAdd, final Set<Beacon3100Schedule> objectsToUpdate, final Set<Beacon3100Schedule> objectsToRemove) {
				final Set<SyncAction<Beacon3100Schedule>> addActions = new HashSet<>();
				final Set<SyncAction<Beacon3100Schedule>> removeActions = new HashSet<>();
				final Set<SyncAction<Beacon3100Schedule>> updateActions = new HashSet<>();
				
				for (final Beacon3100Schedule schedule : objectsToAdd) {
					addActions.add(new CreateScheduleAction(schedule));
				}
				
				for (final Beacon3100Schedule schedule : objectsToUpdate) {
					updateActions.add(new UpdateScheduleAction(schedule));
				}
				
				for (final Beacon3100Schedule schedule : objectsToRemove) {
					removeActions.add(new DeleteScheduleAction(schedule));
				}
				
 				return new ActionSet<Beacon3100Schedule>(addActions, updateActions, removeActions);
			}
		});
		
		// Client types.
		final ActionSet<Beacon3100ClientType> clientTypeActionSet = this.generateActions(clientTypesInHES, clientTypesInBeaconMasterdata, clientTypesUsedByBeaconMirrors, new ActionSetSupplier<Beacon3100ClientType>() {
			@Override
			public final ActionSet<Beacon3100ClientType> createActionSet(final Set<Beacon3100ClientType> objectsToAdd, final Set<Beacon3100ClientType> objectsToUpdate, final Set<Beacon3100ClientType> objectsToRemove) {
				final Set<SyncAction<Beacon3100ClientType>> addActions = new HashSet<>();
				final Set<SyncAction<Beacon3100ClientType>> removeActions = new HashSet<>();
				final Set<SyncAction<Beacon3100ClientType>> updateActions = new HashSet<>();
				
				for (final Beacon3100ClientType clientType : objectsToAdd) {
					addActions.add(new AddClientTypeAction(clientType));
				}
				
				for (final Beacon3100ClientType clientType : objectsToUpdate) {
					updateActions.add(new UpdateClientTypeAction(clientType));
				}
				
				for (final Beacon3100ClientType clientType : objectsToRemove) {
					removeActions.add(new DeleteClientTypeAction(clientType));
				}
				
 				return new ActionSet<Beacon3100ClientType>(addActions, updateActions, removeActions);
			}
		});
		
		// Device types.
		final ActionSet<Beacon3100DeviceType> deviceTypeActionSet = this.generateActions(deviceTypesInHES, deviceTypesInBeaconMasterdata, deviceTypesUsedByBeaconMirrors, new ActionSetSupplier<Beacon3100DeviceType>() {
			@Override
			public final ActionSet<Beacon3100DeviceType> createActionSet(final Set<Beacon3100DeviceType> objectsToAdd, final Set<Beacon3100DeviceType> objectsToUpdate, final Set<Beacon3100DeviceType> objectsToRemove) {
				final Set<SyncAction<Beacon3100DeviceType>> addActions = new HashSet<>();
				final Set<SyncAction<Beacon3100DeviceType>> removeActions = new HashSet<>();
				final Set<SyncAction<Beacon3100DeviceType>> updateActions = new HashSet<>();
				
				for (final Beacon3100DeviceType deviceType : objectsToAdd) {
					addActions.add(new AddDeviceTypeAction(deviceType));
				}
				
				for (final Beacon3100DeviceType deviceType : objectsToUpdate) {
					updateActions.add(new UpdateDeviceTypeAction(deviceType));
				}
				
				for (final Beacon3100DeviceType deviceType : objectsToRemove) {
					removeActions.add(new DeleteDeviceTypeAction(deviceType));
				}
				
 				return new ActionSet<Beacon3100DeviceType>(addActions, updateActions, removeActions);
			}
		});
		
		// Integrate.
		
		// Mind the order.
		final List<SyncAction<?>> actions = new ArrayList<>();
		
		actions.addAll(this.generateMirrorCleanupActions(mirrors));
		actions.addAll(scheduleActionSet.getAddActions());
		actions.addAll(clientTypeActionSet.getAddActions());
		actions.addAll(deviceTypeActionSet.getAddActions());
		
		actions.addAll(scheduleActionSet.getUpdateActions());
		actions.addAll(clientTypeActionSet.getUpdateActions());
		actions.addAll(deviceTypeActionSet.getUpdateActions());
		
		actions.addAll(deviceTypeActionSet.getDeleteActions());
		actions.addAll(clientTypeActionSet.getDeleteActions());
		actions.addAll(scheduleActionSet.getDeleteActions());
		
		return actions;
	}
}
