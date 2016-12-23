package com.energyict.protocolimplv2.nta.dsmr50.elster.am540.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ParseUtils;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.G3NetworkManagement;
import com.energyict.dlms.cosem.ImageTransfer;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.dlms.exceptionhandler.DLMSIOExceptionHandler;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdw.offline.OfflineRegister;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DLMSStoredValues;
import com.energyict.protocolimplv2.dlms.idis.am540.registers.AM540PLCRegisterMapper;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;
import com.energyict.protocolimplv2.nta.dsmr50.elster.am540.Dsmr50Properties;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 18/12/2014 - 14:29
 */
public class Dsmr50RegisterFactory extends Dsmr40RegisterFactory {

    public static final ObisCode ClockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode EndOfBillingPeriod1SchedulerObisCode = ObisCode.fromString("0.0.15.0.0.255");
    public static final ObisCode BillingProfileObisCode = ObisCode.fromString("0.0.98.1.0.255");
    private static final ObisCode MULTICAST_FIRMWARE_UPGRADE_OBISCODE = ObisCode.fromString("0.0.44.0.128.255");
    private static final ObisCode MULTICAST_METER_PROGRESS = ProtocolTools.setObisCodeField(MULTICAST_FIRMWARE_UPGRADE_OBISCODE, 1, (byte) (-1 * ImageTransfer.ATTRIBUTE_UPGRADE_PROGRESS));

    private AM540PLCRegisterMapper plcRegisterMapper;

    public Dsmr50RegisterFactory(AbstractDlmsProtocol protocol) {
        super(protocol);
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        List<OfflineRegister> normalRegisters = new ArrayList<>();

        //First read out the G3 PLC registers, using the G3 PLC register mapper
        for (OfflineRegister register : allRegisters) {
            ObisCode obisCode = register.getObisCode();

            if (obisCode.equals(G3NetworkManagement.getDefaultObisCode())) {
                final CollectedRegister incompatibleRegister = createIncompatibleRegister(register, "Register with obiscode " + obisCode + " cannot be read out, use the path request message for this.");
                collectedRegisters.add(incompatibleRegister);
            } else if (register.getObisCode().equals(MULTICAST_METER_PROGRESS)) {
                final CollectedRegister incompatibleRegister = createIncompatibleRegister(register, "Register with obiscode " + register.getObisCode() + " cannot be read out, use the 'read DC multicast progress' message on the Beacon protocol for this.");
                collectedRegisters.add(incompatibleRegister);
            } else if (getPLCRegisterMapper().getG3Mapping(obisCode) != null) {
                try {
                    RegisterValue registerValue = getPLCRegisterMapper().readRegister(obisCode);
                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getEventTime());
                    collectedRegisters.add(deviceRegister);
                } catch (IOException e) {
                    handleIOException(collectedRegisters, register, e);
                }

                // Else try to read out specific DSMR5.0 registers
            } else if (obisCode.equals(ClockObisCode)) {
                Date time = getProtocol().getTime();
                CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
                deviceRegister.setCollectedData(new Quantity(BigDecimal.valueOf(time.getTime()), Unit.get(BaseUnit.SECOND, -3)), time.toString());
                deviceRegister.setCollectedTimeStamps(new Date(), null, new Date());
                collectedRegisters.add(deviceRegister);
            } else if (obisCode.equals(EndOfBillingPeriod1SchedulerObisCode)) {
                try {
                    SingleActionSchedule singleActionSchedule = getProtocol().getDlmsSession().getCosemObjectFactory().getSingleActionSchedule(EndOfBillingPeriod1SchedulerObisCode);
                    Array executionTime = singleActionSchedule.getExecutionTime();

                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(parseExecutionTimeArrayToHumanReadableText(executionTime));
                    deviceRegister.setCollectedTimeStamps(new Date(), null, new Date());
                    collectedRegisters.add(deviceRegister);
                } catch (IOException e) {
                    handleIOException(collectedRegisters, register, e);
                }
            } else if (obisCode.equals(BillingProfileObisCode)) {
                try {
                    DLMSStoredValues dlmsStoredValues = new DLMSStoredValues(protocol.getDlmsSession(), BillingProfileObisCode);
                    Date billingPointTimeDate = dlmsStoredValues.getBillingPointTimeDate(0);
                    CollectedRegister deviceRegister = MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register));
                    deviceRegister.setCollectedData(new Quantity(BigDecimal.valueOf(billingPointTimeDate.getTime()), Unit.get(BaseUnit.SECOND, -3)), billingPointTimeDate.toString());
                    deviceRegister.setCollectedTimeStamps(new Date(), null, new Date());
                    collectedRegisters.add(deviceRegister);
                } catch (IOException e) {
                    handleIOException(collectedRegisters, register, e);
                }

                // Else read out as regular Dsmr4.0 register
            } else {
                normalRegisters.add(register);
            }
        }

        //Now read out the "normal" DSMR registers, using the DSMR4.0 register factory
        collectedRegisters.addAll(super.readRegisters(normalRegisters));

        //Finally, return all the register values
        return collectedRegisters;
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(OfflineRegister register, AbstractDataType abstractDataType) throws UnsupportedException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);

        //Non ASCII octetstring, so return a hexstring that represents the byte array
        if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE) || rObisCode.equals(MODULE_FIRMWARE_SIGNATURE)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, ParseUtils.decimalByteToString(abstractDataType.getContentByteArray()).toUpperCase());
        }

        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }

    private String parseExecutionTimeArrayToHumanReadableText(Array executionTime) throws IOException {
        Array emptyArray = new Array(
                new OctetString(ProtocolTools.getBytesFromHexString("FFFFFFFFFF", "")),
                new OctetString(ProtocolTools.getBytesFromHexString("FFFFFFFFFF", ""))
        );
        if (executionTime.getArray().equals(emptyArray)) {
            return "End of billing period: undefined";
        } else {
            try {
                String executionTimeText = AXDRDate.toReadableDescription((OctetString) ((Structure) executionTime.getDataType(0)).getDataType(1));
                AXDRTime time = new AXDRTime((OctetString) ((Structure) executionTime.getDataType(0)).getDataType(0));
                executionTimeText = executionTimeText.concat(" ");
                executionTimeText = executionTimeText.concat(time.getTime());
                return executionTimeText;
            } catch (IndexOutOfBoundsException | ClassCastException e) {
                throw new IOException("Failed to parse the execution time array");
            }
        }
    }

    private void handleIOException(List<CollectedRegister> collectedRegisters, OfflineRegister register, IOException e) {
        if (DLMSIOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSessionProperties().getRetries() + 1)) {
            if (DLMSIOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                collectedRegisters.add(createUnsupportedRegister(register));
            } else {
                collectedRegisters.add(createIncompatibleRegister(register, e.getMessage()));
            }
        }
    }

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(protocol.getDlmsSession());
        }
        return plcRegisterMapper;
    }



    protected RegisterValue getRegisterValueForComposedRegister(OfflineRegister offlineRegister, Date captureTime, AbstractDataType attributeValue, Unit unit) {
        Dsmr50Properties dsmr50Properties = (Dsmr50Properties) protocol.getDlmsSessionProperties();
        if (captureTime!=null && dsmr50Properties.useBeaconMirrorDeviceDialect()) {
            // for composed registers:
            // - readTime is the value stored in attribute#5=captureTime = the metrological date
            // - eventTime is the communication time -> not used in metrology
            return new RegisterValue(offlineRegister, new Quantity(attributeValue.toBigDecimal(), unit),
                    new Date(), // eventTime = read-out time
                    null,       // fromTime
                    null,       // toTime
                    captureTime); // readTime
        }

        return super.getRegisterValueForComposedRegister(offlineRegister, captureTime, attributeValue, unit);
    }

}