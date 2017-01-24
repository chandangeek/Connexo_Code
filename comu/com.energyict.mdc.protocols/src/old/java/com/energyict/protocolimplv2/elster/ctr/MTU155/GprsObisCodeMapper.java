package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;

import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.exception.CTRConnectionException;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 28/06/13 - 8:25
 */
public class GprsObisCodeMapper extends ObisCodeMapper {

    public GprsObisCodeMapper(MTU155 protocol, MdcReadingTypeUtilService readingTypeUtilService, IssueService issueService, CollectedDataFactory collectedDataFactory) {
        super(readingTypeUtilService, issueService, collectedDataFactory);
        this.deviceIdentifier = protocol.getDeviceIdentifier();
        this.requestFactory = protocol.getRequestFactory();
        this.isEK155Protocol = false;
        initRegisterMapping();
    }

    /**
     * Read the registers from the device.
     *
     * @param rtuRegisters: the list of {@link OfflineRegister}s to read
     * @return a list containing all {@link CollectedRegister}s
     */
    public List<CollectedRegister> readRegisters(List<OfflineRegister> rtuRegisters) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>(rtuRegisters.size());

        List<CTRObjectID> objectsToRequest = new ArrayList<>();
        List<CTRRegisterMapping> registerMappings = new ArrayList<>();

        for (OfflineRegister register : rtuRegisters) {
            ObisCode obisCode = register.getObisCode();

            CTRRegisterMapping regMap = searchRegisterMapping(obisCode);
            if (regMap == null) {
                collectedRegisters.add(createNotSupportedCollectedRegister(obisCode, register.getUnit()));
                String message = "Register with obisCode [" + obisCode + "] is not supported by the meter.";
                getLogger().log(Level.WARNING, message);
            } else if (regMap.getObjectId() == null) {
                try {
                    collectedRegisters.add(createCollectedRegister(readSpecialRegister(regMap)));
                } catch (NoSuchRegisterException e) {
                    String message = "Register with obisCode [" + obisCode + "] is not supported by the meter.";
                    collectedRegisters.add(createNotSupportedCollectedRegister(obisCode, register.getUnit()));
                    getLogger().log(Level.WARNING, message);
                }
            } else {
                registerMappings.add(regMap);
                objectsToRequest.add(regMap.getObjectId());
            }
        }

        if (!objectsToRequest.isEmpty()) {
            CTRObjectID[] objectIDs = new CTRObjectID[objectsToRequest.size()];
            List<AbstractCTRObject> ctrObjects = null;
            try {
                ctrObjects = getRequestFactory().getObjects(objectsToRequest.toArray(objectIDs));
            } catch (CTRConnectionException e) {
            }

            for (CTRRegisterMapping mapping : registerMappings) {
                CTRObjectID objectID = mapping.getObjectId();
                ObisCode obisCode = mapping.getObisCode();
                AbstractCTRObject object = getObjectFromList(ctrObjects, objectID);

                if (object == null) {
                    String message = "Received no suitable data at register reading for ID: " + objectID + " (Obiscode: " + obisCode.toString() + ")";
                    collectedRegisters.add(createIncompatibleCollectedRegister(obisCode, message));
                    getLogger().log(Level.WARNING, message);
                    continue;
                }

                if (object.getQlf() == null) {
                    object.setQlf(new Qualifier(0));
                }

                if (object.getQlf().isInvalid()) {
                    String message = "Invalid Data: Qualifier was 0xFF at register reading for ID: " + objectID + " (Obiscode: " + obisCode.toString() + ")";
                    collectedRegisters.add(createIncompatibleCollectedRegister(obisCode, message));
                    getLogger().log(Level.WARNING, message);
                } else if (object.getQlf().isInvalidMeasurement()) {
                    String message = "Invalid Measurement at register reading for ID: " + objectID + " (Obiscode: " + obisCode.toString() + ")";
                    collectedRegisters.add(createIncompatibleCollectedRegister(obisCode, message));
                    getLogger().log(Level.WARNING, message);
                } else if (object.getQlf().isSubjectToMaintenance()) {
                    String message = "Meter is subject to maintenance at register reading for ID: " + objectID + " (Obiscode: " + obisCode.toString() + ")";
                    collectedRegisters.add(createIncompatibleCollectedRegister(obisCode, message));
                    getLogger().log(Level.WARNING, message);
                } else if (object.getQlf().isReservedVal()) {
                    String message = "Qualifier is 'Reserved' at register reading for ID: " + objectID + " (Obiscode: " + obisCode.toString() + ")";
                    collectedRegisters.add(createIncompatibleCollectedRegister(obisCode, message));
                    getLogger().log(Level.WARNING, message);
                } else {
                    collectedRegisters.add(createCollectedRegister(ProtocolTools.setRegisterValueObisCode(getRegisterValue(obisCode, mapping, object), obisCode)));
                }
            }
        }

        return collectedRegisters;
    }

    private RegisterValue readSpecialRegister(CTRRegisterMapping registerMapping) throws NoSuchRegisterException {
        RegisterValue registerValue = null;
        ObisCode obis = ProtocolTools.setObisCodeField(registerMapping.getObisCode(), 1, (byte) 0x00);

//        if (isObis(obis, OBIS_MTU_IP_ADDRESS)) {
//            registerValue = new RegisterValue(obis, getRequestFactory().getIPAddress());
//        }

        if (isObis(obis, OBIS_INSTALL_DATE)) {
            getLogger().warning("Installation date cannot be read as a regular register.");
            throw new NoSuchRegisterException("Installation date cannot be read as a regular register.");
        }

        if (registerValue == null) {
            throw new NoSuchRegisterException("Register with obisCode [" + obis + "] is not supported.");
        } else {
            return ProtocolTools.setRegisterValueObisCode(registerValue, registerMapping.getObisCode());
        }
    }
}
