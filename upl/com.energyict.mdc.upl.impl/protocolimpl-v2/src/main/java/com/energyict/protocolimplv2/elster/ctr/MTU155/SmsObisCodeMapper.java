package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.mdc.upl.issue.IssueFactory;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.mdc.upl.meterdata.CollectedRegister;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.Qualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * @author sva
 * @since 27/06/13 - 15:49
 */
public class SmsObisCodeMapper extends ObisCodeMapper {

    public SmsObisCodeMapper(DeviceIdentifier deviceIdentifier, CollectedDataFactory collectedDataFactory, IssueFactory issueFactory) {
        super(collectedDataFactory, issueFactory);
        super.setDeviceIdentifier(deviceIdentifier);
        super.initRegisterMapping();
    }

    /**
     * Read the registers from the device, using the list of objects received from an sms
     *
     * @param smsObjects: the list of objects received in the SMS
     */
    public List<CollectedRegister> readRegisters(List<AbstractCTRObject> smsObjects) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>(smsObjects.size());

        for (AbstractCTRObject smsObject : smsObjects) {
            CTRRegisterMapping mapping = searchRegisterMapping(smsObject.getId());
            if (mapping == null) {
                // Unknown CTRObjectID - no need to collect data
                continue;
            }

            if (smsObject.getQlf() == null) {
                smsObject.setQlf(new Qualifier(0));
            }

            if (smsObject.getQlf().isInvalid()) {
                String message = "Invalid Data: Qualifier was 0xFF at register reading for ID: " + mapping.getId() + " (Obiscode: " + mapping.getObisCode() + ")";
                collectedRegisters.add(createIncompatibleCollectedRegister(mapping.getObisCode(), message));
                getLogger().log(Level.WARNING, message);
            } else if (smsObject.getQlf().isInvalidMeasurement()) {
                String message = "Invalid Measurement at register reading for ID: " + mapping.getId() + " (Obiscode: " + mapping.getObisCode() + ")";
                collectedRegisters.add(createIncompatibleCollectedRegister(mapping.getObisCode(), message));
                getLogger().log(Level.WARNING, message);
            } else if (smsObject.getQlf().isSubjectToMaintenance()) {
                String message = "Meter is subject to maintenance at register reading for ID: " + mapping.getId() + " (Obiscode: " + mapping.getObisCode() + ")";
                collectedRegisters.add(createIncompatibleCollectedRegister(mapping.getObisCode(), message));
                getLogger().log(Level.WARNING, message);
            } else if (smsObject.getQlf().isReservedVal()) {
                String message = "Qualifier is 'Reserved' at register reading for ID: " + mapping.getId() + " (Obiscode: " + mapping.getObisCode() + ")";
                collectedRegisters.add(createIncompatibleCollectedRegister(mapping.getObisCode(), message));
                getLogger().log(Level.WARNING, message);
            } else {
                collectedRegisters.add(createCollectedRegister(ProtocolTools.setRegisterValueObisCode(getRegisterValue(mapping.getObisCode(), mapping, smsObject), mapping.getObisCode())));
            }
        }

        return collectedRegisters;
    }

    @Override
    protected Boolean firmwareBelow200() {
        return false;
    }
}
