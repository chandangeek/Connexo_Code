package com.energyict.genericprotocolimpl.rtuplusserver.g3;

import com.energyict.cbo.BusinessException;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dlms.cosem.SAPAssignmentItem;
import com.energyict.genericprotocolimpl.common.StoreObject;
import com.energyict.mdw.core.CommunicationProfile;
import com.energyict.mdw.core.CommunicationScheduler;
import com.energyict.mdw.core.Device;
import com.energyict.mdw.core.MeteringWarehouse;
import com.energyict.mdw.core.ModemPool;
import com.energyict.mdw.core.ModemPoolFactory;
import com.energyict.mdw.shadow.ComPortShadow;
import com.energyict.mdw.shadow.CommunicationSchedulerShadow;
import com.energyict.mdw.shadow.DeviceShadow;
import com.energyict.mdw.shadow.ModemPoolShadow;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 6/03/12
 * Time: 17:34
 */
public class RtuPlusServerTask {

    public static final String EMPTY_MODEM_POOL_NAME = "None";
    
    private final CommunicationScheduler scheduler;
    private final Logger logger;
    private final List<Device> devicesInField = new ArrayList<Device>();
    private final StoreObject storeObject = new StoreObject();

    public RtuPlusServerTask(CommunicationScheduler scheduler, Logger logger) {
        this.scheduler = scheduler;
        this.logger = logger;
    }

    public CommunicationScheduler getScheduler() {
        return scheduler;
    }

    public CommunicationProfile getCommunicationProfile() {
        return getScheduler().getCommunicationProfile();
    }

    public Logger getLogger() {
        return logger;
    }

    public Device getGateway() {
        return scheduler.getRtu();
    }

    public TimeZone getGatewayTimeZone() {
        return scheduler.getRtu().getDeviceTimeZone();
    }

    public String getGatewaySerialNumber() {
        final String serialNumber = getGateway().getSerialNumber();
        return serialNumber != null ? serialNumber.trim() : "";
    }

    /**
     * Update the concentrator link in EIServer for all found devices, remove the link for ghost devices.
     *
     * @param sapAssignmentItems
     */
    public void updateEIServerTopology(List<SAPAssignmentItem> sapAssignmentItems) {
        for (SAPAssignmentItem item : sapAssignmentItems) {
            if (item.getSap() != 1) {
                Device plcDevice = findByCallHomeId(item);
                if (plcDevice != null) {
                    devicesInField.add(plcDevice);
                    updateEiServerGatewaySettings(plcDevice, item);
                }
            }
        }
        final List<Device> eiServerGhostDevices = getEiServerGhostDevices();
        for (Device ghostDevice : eiServerGhostDevices) {
            cleanupEiServerGhostDevice(ghostDevice);
        }
    }

    /**
     * Remove the concentrator link in EIServer if this device is not found on the concentrator in the field.
     */
    private List<Device> getEiServerGhostDevices() {
        final List<Device> ghostDevices = new ArrayList<Device>();
        final List<Device> downstreamRtus = getGateway().getDownstreamRtus();
        for (Device downstreamRtu : downstreamRtus) {
            if (!devicesInField.contains(downstreamRtu)) {
                ghostDevices.add(downstreamRtu);
            }
        }
        return ghostDevices;
    }

    /**
     * Update the gateway on all the devices found in EIServer to the current concentrator if required
     *
     * @param plcDevice
     * @param sap
     */
    private void updateEiServerGatewaySettings(Device plcDevice, SAPAssignmentItem sap) {
        if (sap != null) {
            if (needsUpdate(plcDevice, sap)) {
                setEiServerGatewaySettings(plcDevice, sap);
            }
        } else {
            cleanupEiServerGhostDevice(plcDevice);
        }
    }

    private boolean needsUpdate(Device plcDevice, SAPAssignmentItem sap) {
        final Device currentGateway = plcDevice.getGateway();
        final String nodeAddress = plcDevice.getNodeAddress() == null ? "" : plcDevice.getNodeAddress().trim();
        final String sapAddress = String.valueOf(sap.getSap());
        if (currentGateway == null) {
            return true;
        } else if (currentGateway.getId() != getGateway().getId()) {
            return true;
        } else if (!nodeAddress.equalsIgnoreCase(sapAddress)) {
            return true;
        }
        return false;
    }

    /**
     * Update the gateway link and the node address field for th given plcDevice to the values provided in the sap object
     *
     * @param plcDevice The plcDevice to update
     * @param sap       The sap values to use during the update
     */
    private void setEiServerGatewaySettings(Device plcDevice, SAPAssignmentItem sap) {
        try {
            final String plcName = plcDevice.getName();
            final String newGwName = getGateway().getName();
            final String oldGwName = plcDevice.getGateway() == null ? "none" : plcDevice.getGateway().getName();
            final String newNodeAddress = String.valueOf(sap.getSap());
            final String phoneNumber = getGateway().getPhoneNumber();
            final ModemPool modemPool = getScheduler().getModemPool();
            final DialerFactory dialer = getScheduler().getDialerFactory();

            logger.warning("Changing link [" + plcName + "] from [" + oldGwName + "] to [" + newGwName + "]");

            final DeviceShadow shadow = plcDevice.getShadow();
            shadow.setPhoneNumber(phoneNumber);
            shadow.setNodeAddress(newNodeAddress);
            shadow.setGatewayId(getGateway().getId());
            final List<CommunicationSchedulerShadow> schedulerShadowList = shadow.getCommunicationSchedulerShadows();
            for (CommunicationSchedulerShadow schedulerShadow : schedulerShadowList) {
                schedulerShadow.setModemPoolId(modemPool.getId());
                schedulerShadow.setDialer(dialer);
            }
            plcDevice.update(shadow);

        } catch (SQLException e) {
            getLogger().severe("Unable to update gateway link in EIServer for [" + plcDevice.getName() + "] to [" + getGateway().getName() + "]! " + e.getMessage());
        } catch (BusinessException e) {
            getLogger().severe("Unable to update gateway link in EIServer for [" + plcDevice.getName() + "] to [" + getGateway().getName() + "]! " + e.getMessage());
        }
    }

    /**
     * Remove the concentrator link and remove the sap address from the node address
     *
     * @param plcDevice The plc ghost device to clean up
     */
    private void cleanupEiServerGhostDevice(Device plcDevice) {
        try {
            logger.severe("Removing gateway link and sap address from ghost device in EIServer [" + plcDevice.getName() + "]. Device not found in the field on gateway [" + getGateway().getName() + "].");
            final DeviceShadow shadow = plcDevice.getShadow();
            shadow.setNodeAddress("");
            shadow.setGatewayId(0);
            shadow.setPhoneNumber("");
            final List<CommunicationSchedulerShadow> schedulerShadowList = shadow.getCommunicationSchedulerShadows();
            for (CommunicationSchedulerShadow schedulerShadow : schedulerShadowList) {
                schedulerShadow.setDialer(DialerFactory.get("NULLDIALER"));
                schedulerShadow.setNextCommunication(null);
                schedulerShadow.setModemPoolId(getEmptyModemPoolId());
            }
            plcDevice.update(shadow);
        } catch (SQLException e) {
            getLogger().severe("Unable to remove gateway link in EIServer for ghost device [" + plcDevice.getName() + "]!" + e.getMessage());
        } catch (BusinessException e) {
            getLogger().severe("Unable to remove gateway link in EIServer for ghost device [" + plcDevice.getName() + "]!" + e.getMessage());
        }
    }

    private int getEmptyModemPoolId() {
        try {
            final ModemPoolFactory factory = mw().getModemPoolFactory();
            final List<ModemPool> pools = factory.findByName(EMPTY_MODEM_POOL_NAME);
            if (pools.isEmpty()) {
                final ModemPoolShadow shadow = new ModemPoolShadow();
                shadow.setName(EMPTY_MODEM_POOL_NAME);
                shadow.setActive(false);
                shadow.setInbound(false);
                shadow.setComPortShadows(new ArrayList<ComPortShadow>());
                shadow.setDescription("Empty non active modem pool");
                factory.create(shadow).getId();
            } else if (pools.size() == 1) {
                return pools.get(0).getId();
            }
        } catch (SQLException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (BusinessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return 0;
    }

    /**
     * Search in EIServer for a matching device using the logical name and the callHomeId found in the SAP assignment object
     *
     * @param sap The SAP assignment item used to get the logical device name
     * @return The rtu if found, or null if there was no exact match
     */
    private Device findByCallHomeId(SAPAssignmentItem sap) {
        final String logicalDeviceName = sap.getLogicalDeviceName().trim().toUpperCase();
        List<Device> result = mw().getDeviceFactory().findByDialHomeId(logicalDeviceName);
        if (result != null) {
            if (result.isEmpty()) {
                logger.severe("No matching device found for SAP [" + sap.getSap() + ", " + sap.getLogicalDeviceName() + "] in EIServer!");
            } else if (result.size() > 1) {
                logger.severe("Multiple devices found for SAP [" + sap.getSap() + ", " + sap.getLogicalDeviceName() + "] in EIServer! Skipping these devices.");
            } else {
                return result.get(0);
            }
        }
        return null;
    }

    private MeteringWarehouse mw() {
        if (MeteringWarehouse.getCurrent() == null) {
            MeteringWarehouse.createBatchContext(true);
        }
        return MeteringWarehouse.getCurrent();
    }

    public Date getLastLogBookDate() {
        return getGateway().getLastLogbook() == null ? new Date(0) : getGateway().getLastLogbook();
    }

    public StoreObject getStoreObject() {
        return storeObject;
    }

    public final void scheduleSlaveDevices() {
        for (Device deviceInField : devicesInField) {
            final List<CommunicationScheduler> communicationSchedulers = deviceInField.getCommunicationSchedulers();
            for (CommunicationScheduler schedule : communicationSchedulers) {
                try {
                    final CommunicationSchedulerShadow shadow = schedule.getShadow();
                    shadow.setNextCommunication(new Date());
                    schedule.update(shadow);
                } catch (SQLException e) {
                    getLogger().severe("Unable to trigger schedule [" + schedule.displayString() + "]! " + e.getMessage());
                } catch (BusinessException e) {
                    getLogger().severe("Unable to trigger schedule [" + schedule.displayString() + "]! " + e.getMessage());
                }
            }
        }
    }

}


