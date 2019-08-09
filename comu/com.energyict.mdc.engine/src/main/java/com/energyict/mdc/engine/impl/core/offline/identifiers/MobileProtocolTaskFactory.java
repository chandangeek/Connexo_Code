package com.energyict.mdc.engine.impl.core.offline.identifiers;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.masterdata.RegisterGroup;
import com.energyict.mdc.protocol.LegacyProtocolProperties;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.offline.OfflineDevice;

import java.util.List;

/**
 * @author sva
 * @since 28/10/2014 - 16:28
 */
public class MobileProtocolTaskFactory {

    public static OfflineDevice findOfflineDevice(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, DeviceIdentifier deviceIdentifier) {
        switch (deviceIdentifier.forIntrospection().getTypeName()) {
            case "SerialNumber":
                String snIdentifier = deviceIdentifier.forIntrospection().getValue("serialNumber").toString();
                if (snIdentifier.equals(comJobExecutionModel.getOfflineDevice().getSerialNumber())) {
                    return comJobExecutionModel.getOfflineDevice();
                } else {
                    return findOfflineSlaveDeviceBySerialNumber(comJobExecutionModel, snIdentifier);
                }
            case "DatabaseId":
                long dataBaseId = (Long) deviceIdentifier.forIntrospection().getValue("databaseValue");
                if (dataBaseId == comJobExecutionModel.getOfflineDevice().getId()) {
                    return comJobExecutionModel.getOfflineDevice();
                } else {
                    return findOfflineSlaveDeviceByDataBaseId(comJobExecutionModel, dataBaseId);
                }
            case "CallHomeId":
                String chIdentifier = deviceIdentifier.forIntrospection().getValue("callHomeId").toString();
                if (chIdentifier.equals(getCallHomeId(comJobExecutionModel.getOfflineDevice()))) {
                    return comJobExecutionModel.getOfflineDevice();
                } else {
                    return findOfflineSlaveDeviceByCallHomeId(comJobExecutionModel, chIdentifier);
                }
            default:
                throw new UnsupportedOperationException("Unsupported identifier '" + deviceIdentifier + "' of type " + deviceIdentifier.forIntrospection().getTypeName() + ", command cannot be executed offline.");
        }
    }

    private static String getCallHomeId(OfflineDevice offlineDevice) {
        return offlineDevice.getAllProperties().getProperty(LegacyProtocolProperties.CALL_HOME_ID_PROPERTY_NAME, "").toString();
    }

    private static OfflineDevice findOfflineSlaveDeviceBySerialNumber(ComJobExecutionModel comJobExecutionModel, String serialNumber) {
        for (OfflineDevice slave : comJobExecutionModel.getOfflineDevice().getAllSlaveDevices()) {
            if (slave.getSerialNumber().equals(serialNumber)) {
                return slave;
            }
        }
        return null;    //ComServer expects null in case of new, unknown slave device
    }

    private static OfflineDevice findOfflineSlaveDeviceByDataBaseId(ComJobExecutionModel comJobExecutionModel, long dataBaseId) {
        for (OfflineDevice slave : comJobExecutionModel.getOfflineDevice().getAllSlaveDevices()) {
            if (slave.getId() == dataBaseId) {
                return slave;
            }
        }
        return null;    //ComServer expects null in case of new, unknown slave device
    }

    private static OfflineDevice findOfflineSlaveDeviceByCallHomeId(ComJobExecutionModel comJobExecutionModel, String callHomeId) {
        for (OfflineDevice slave : comJobExecutionModel.getOfflineDevice().getAllSlaveDevices()) {
            if (getCallHomeId(slave).equals(callHomeId)) {
                return slave;
            }
        }
        return null;    //ComServer expects null in case of new, unknown slave device
    }

    public static List<RegisterGroup> getRegisterGroupsByManualMeterReadingsTaskId(Long id) {
        return null;
    }
}
