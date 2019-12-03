package com.energyict.mdc.engine.impl.core.offline.identifiers;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LogBookIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLogBook;
import com.energyict.obis.ObisCode;

/**
 * @author sva
 * @since 28/10/2014 - 16:39
 */
public class MobileLogBookFactory {

    public static OfflineLogBook findOfflineLogBook(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, LogBookIdentifier logBookIdentifier) {
        try {
            switch (logBookIdentifier.forIntrospection().getTypeName()) {
                case "DatabaseId":
                    long dataBaseId = (Long) logBookIdentifier.forIntrospection().getValue("databaseValue");
                    return findOfflineLogBookByDataBaseId(comServerDAO, comJobExecutionModel, dataBaseId);
                case "DeviceIdentifierAndObisCode":
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier)logBookIdentifier.forIntrospection().getValue("device");
                    ObisCode obisCode = (ObisCode)logBookIdentifier.forIntrospection().getValue("obisCode");
                    return findOfflineLogBookByDeviceIdentifierAndObisCode(comServerDAO, comJobExecutionModel, deviceIdentifier, obisCode);
                default:
                    throw new UnsupportedOperationException("Unsupported identifier '" + logBookIdentifier + "' of type " + logBookIdentifier.forIntrospection().getTypeName() + ", command cannot be executed offline.");
            }
        } catch (ClassCastException | IndexOutOfBoundsException e) {
            throw CodingException.failedToParseIdentifierData(e, MessageSeeds.FAILED_TO_PARSE_IDENTIFIER_DATA, logBookIdentifier.forIntrospection().getTypeName());
        }
    }

    private static OfflineLogBook findOfflineLogBookByDataBaseId(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, long databaseId) {
        for (OfflineLogBook offlineLogBook : comJobExecutionModel.getOfflineDevice().getAllOfflineLogBooks()) {
            if (offlineLogBook.getLogBookId() == databaseId) {
                return offlineLogBook;
            }
        }
        return null;
    }

    private static OfflineLogBook findOfflineLogBookByDeviceIdentifierAndObisCode(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, DeviceIdentifier deviceIdentifier, ObisCode obisCode) {
        OfflineDevice offlineDevice = comServerDAO.getOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.LOG_BOOKS_FLAG));
        if (offlineDevice != null && offlineDevice.getAllOfflineLogBooks() != null) {
            for (OfflineLogBook offlineLogBook : offlineDevice.getAllOfflineLogBooks()) {
                if (offlineLogBook.getOfflineLogBookSpec().getDeviceObisCode().equals(obisCode) &&
                        offlineLogBook.getDeviceId() == offlineDevice.getId()) { // Do not take into account logBooks of slave devices
                    return offlineLogBook;
                }
            }
        }
        return null;
    }
}
