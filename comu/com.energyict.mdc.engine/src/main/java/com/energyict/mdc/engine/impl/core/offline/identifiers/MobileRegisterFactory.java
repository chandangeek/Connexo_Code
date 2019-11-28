package com.energyict.mdc.engine.impl.core.offline.identifiers;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.RegisterIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineRegister;
import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 28/10/2014 - 16:39
 */
public class MobileRegisterFactory {

    public static OfflineRegister findOfflineRegister(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, RegisterIdentifier registerIdentifier) {
        try {
            switch (registerIdentifier.forIntrospection().getTypeName()) {
                case "DatabaseId":
                    long dataBaseId = (Long) registerIdentifier.forIntrospection().getValue("databaseValue");
                    return findOfflineRegisterByDataBaseId(comJobExecutionModel, dataBaseId);
                case "DeviceIdentifierAndObisCode":
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier)registerIdentifier.forIntrospection().getValue("device");
                    ObisCode obisCode = (ObisCode)registerIdentifier.forIntrospection().getValue("obisCode");
                    return findOfflineRegisterByDeviceIdentifierAndObisCode(comServerDAO, comJobExecutionModel, deviceIdentifier, obisCode);
                default:
                    throw new UnsupportedOperationException("Unsupported identifier '" + registerIdentifier + "' of type " + registerIdentifier.forIntrospection().getTypeName() + ", command cannot be executed offline.");
            }
        } catch (ClassCastException | IndexOutOfBoundsException e) {
            throw CodingException.failedToParseIdentifierData(e, MessageSeeds.FAILED_TO_PARSE_IDENTIFIER_DATA, registerIdentifier.forIntrospection().getTypeName());
        }
    }

    private static OfflineRegister findOfflineRegisterByDataBaseId(ComJobExecutionModel comJobExecutionModel, long dataBaseId) {
        for (OfflineRegister offlineRegister : comJobExecutionModel.getOfflineDevice().getAllOfflineRegisters()) {
            if (offlineRegister.getRegisterId() == dataBaseId) {
                return offlineRegister;
            }
        }
        return null;
    }

    private static OfflineRegister findOfflineRegisterByDeviceIdentifierAndObisCode(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, DeviceIdentifier deviceIdentifier, ObisCode obisCode) {
        OfflineDevice offlineDevice = comServerDAO.getOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.REGISTERS_FLAG));
        if (offlineDevice != null) {
            for (OfflineRegister offlineRegister : offlineDevice.getAllOfflineRegisters()) {
                if (offlineRegister.getObisCode().equals(obisCode) &&
                        offlineRegister.getDeviceId() == offlineDevice.getId()) { // Do not take into account logBooks of slave devices
                    return offlineRegister;
                }
            }
        }
        return null;
    }
}
