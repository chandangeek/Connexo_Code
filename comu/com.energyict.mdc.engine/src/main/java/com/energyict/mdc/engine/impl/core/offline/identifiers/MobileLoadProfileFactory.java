package com.energyict.mdc.engine.impl.core.offline.identifiers;

import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.commands.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.offline.ComJobExecutionModel;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.meterdata.identifiers.LoadProfileIdentifier;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;
import com.energyict.mdc.upl.offline.OfflineDevice;
import com.energyict.mdc.upl.offline.OfflineLoadProfile;
import com.energyict.obis.ObisCode;

import java.util.List;

/**
 * @author sva
 * @since 28/10/2014 - 16:39
 */
public class MobileLoadProfileFactory {

    public static OfflineLoadProfile findOfflineLoadProfile(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, LoadProfileIdentifier loadProfileIdentifier) {
        try {
            switch (loadProfileIdentifier.forIntrospection().getTypeName()) {
                case "DatabaseId":
                    long dataBaseId = (Long) loadProfileIdentifier.forIntrospection().getValue("databaseValue");
                    return findOfflineLoadProfileByDataBaseId(comServerDAO, comJobExecutionModel, dataBaseId);
                case "DeviceIdentifierAndObisCode":
                    DeviceIdentifier deviceIdentifier = (DeviceIdentifier)loadProfileIdentifier.forIntrospection().getValue("device");
                    ObisCode obisCode = (ObisCode)loadProfileIdentifier.forIntrospection().getValue("obisCode");
                    return findOfflineLoadProfileByDeviceIdentifierAndObisCode(comServerDAO, comJobExecutionModel, deviceIdentifier, obisCode);
                case "FirstLoadProfileOnDevice":
                    DeviceIdentifier  firstOnDeviceIdentifier = (DeviceIdentifier)loadProfileIdentifier.forIntrospection().getValue("device");
                    return findFirstLoadProfileOnDevice(comServerDAO, comJobExecutionModel, firstOnDeviceIdentifier);
                default:
                    throw new UnsupportedOperationException("Unsupported identifier '" + loadProfileIdentifier + "' of type " + loadProfileIdentifier.forIntrospection().getTypeName() + ", command cannot be executed offline.");
            }
        } catch (ClassCastException | IndexOutOfBoundsException e) {
            throw CodingException.failedToParseIdentifierData(e, MessageSeeds.FAILED_TO_PARSE_IDENTIFIER_DATA, loadProfileIdentifier.forIntrospection().getTypeName());
        }
    }

    private static OfflineLoadProfile findOfflineLoadProfileByDataBaseId(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, long databaseId) {
        for (OfflineLoadProfile offlineLoadProfile : comJobExecutionModel.getOfflineDevice().getAllOfflineLoadProfiles()) {
            if (offlineLoadProfile.getLoadProfileId() == databaseId) {
                return offlineLoadProfile;
            }
        }
        return null;
    }

    private static OfflineLoadProfile findOfflineLoadProfileByDeviceIdentifierAndObisCode(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, DeviceIdentifier deviceIdentifier, ObisCode obisCode) {
        OfflineDevice offlineDevice = comServerDAO.getOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG));
        if (offlineDevice != null) {
            for (OfflineLoadProfile offlineLoadProfile : offlineDevice.getMasterOfflineLoadProfiles()) {
                if (offlineLoadProfile.getObisCode().equals(obisCode)) {
                    return offlineLoadProfile;
                }
            }
        }
        return null;
    }

    private static OfflineLoadProfile findFirstLoadProfileOnDevice(ComServerDAO comServerDAO, ComJobExecutionModel comJobExecutionModel, DeviceIdentifier deviceIdentifier) {
        OfflineDevice offlineDevice = comServerDAO.getOfflineDevice(deviceIdentifier, new DeviceOfflineFlags(DeviceOfflineFlags.ALL_LOAD_PROFILES_FLAG));
        if (offlineDevice != null) {
            List<OfflineLoadProfile> offlineLoadProfiles = offlineDevice.getMasterOfflineLoadProfiles();
            if (!offlineLoadProfiles.isEmpty()) {
                return offlineLoadProfiles.get(0);
            }
        }
        return null;
    }
}
