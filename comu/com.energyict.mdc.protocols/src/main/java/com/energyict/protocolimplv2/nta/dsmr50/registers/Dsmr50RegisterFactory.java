package com.energyict.protocolimplv2.nta.dsmr50.registers;

import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.device.data.ResultType;
import com.energyict.mdc.protocol.api.device.offline.OfflineRegister;
import com.energyict.mdc.upl.meterdata.CollectedDataFactory;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.dlms.AbstractDlmsProtocol;
import com.energyict.protocolimplv2.dlms.DlmsStoredValues;
import com.energyict.protocolimplv2.nta.IOExceptionHandler;
import com.energyict.protocolimplv2.nta.dsmr40.registers.Dsmr40RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
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
    private final Clock clock;

    private AM540PLCRegisterMapper plcRegisterMapper;

    public Dsmr50RegisterFactory(AbstractDlmsProtocol protocol, IssueService issueService, MdcReadingTypeUtilService readingTypeUtilService, boolean supportsBulkRequests, CollectedDataFactory collectedDataFactory, Clock clock) {
        super(protocol, issueService, readingTypeUtilService, supportsBulkRequests, collectedDataFactory);
        this.clock = clock;
    }

    public List<CollectedRegister> readRegisters(List<OfflineRegister> allRegisters) {
        List<CollectedRegister> collectedRegisters = new ArrayList<>();
        List<OfflineRegister> normalRegisters = new ArrayList<>();

        //First read out the G3 PLC registers, using the G3 PLC register mapper
        for (OfflineRegister register : allRegisters) {
            ObisCode obisCode = register.getObisCode();
            if (getPLCRegisterMapper().getG3Mapping(obisCode) != null) {
                try {
                    RegisterValue registerValue = getPLCRegisterMapper().readRegister(obisCode);
                    CollectedRegister deviceRegister = getCollectedDataFactory().createMaximumDemandCollectedRegister(getRegisterIdentifier(register),
                            register.getReadingType());
                    deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
                    deviceRegister.setCollectedTimeStamps(registerValue.getReadTime().toInstant(), registerValue.getFromTime().toInstant(), registerValue.getToTime().toInstant(), registerValue.getEventTime().toInstant());
                    collectedRegisters.add(deviceRegister);
                } catch (IOException e) {
                    handleIOException(collectedRegisters, register, e);
                }
                // Else try to read out specific DSMR5.0 registers
            } else if (obisCode.equals(ClockObisCode)) {
                Date time = this.protocol.getTime();
                CollectedRegister deviceRegister = getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
                deviceRegister.setCollectedData(new Quantity(BigDecimal.valueOf(time.getTime()), Unit.get(BaseUnit.SECOND, -3)), time.toString());
                final Instant currentInstant = this.clock.instant();
                deviceRegister.setCollectedTimeStamps(currentInstant, null, currentInstant);
                collectedRegisters.add(deviceRegister);
            } else if (obisCode.equals(EndOfBillingPeriod1SchedulerObisCode)) {
                try {
                    SingleActionSchedule singleActionSchedule = this.protocol.getDlmsSession().getCosemObjectFactory().getSingleActionSchedule(EndOfBillingPeriod1SchedulerObisCode);
                    Array executionTime = singleActionSchedule.getExecutionTime();

                    CollectedRegister deviceRegister = getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
                    deviceRegister.setCollectedData(parseExecutionTimeArrayToHumanReadableText(executionTime));
                    final Instant currentInstant = this.clock.instant();
                    deviceRegister.setCollectedTimeStamps(currentInstant, null, currentInstant);
                    collectedRegisters.add(deviceRegister);
                } catch (IOException e) {
                    handleIOException(collectedRegisters, register, e);
                }
            } else if (obisCode.equals(BillingProfileObisCode)) {
                try {
                    DlmsStoredValues dlmsStoredValues = new DlmsStoredValues(protocol.getDlmsSession(), BillingProfileObisCode);
                    Date billingPointTimeDate = dlmsStoredValues.getBillingPointTimeDate(0);
                    CollectedRegister deviceRegister = getCollectedDataFactory().createDefaultCollectedRegister(getRegisterIdentifier(register), register.getReadingType());
                    deviceRegister.setCollectedData(new Quantity(BigDecimal.valueOf(billingPointTimeDate.getTime()), Unit.get(BaseUnit.SECOND, -3)), billingPointTimeDate.toString());
                    final Instant currentInstant = this.clock.instant();
                    deviceRegister.setCollectedTimeStamps(currentInstant, null, currentInstant);
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

    private void handleIOException(List<CollectedRegister> collectedRegisters, OfflineRegister register, IOException e) {
        if (IOExceptionHandler.isUnexpectedResponse(e, protocol.getDlmsSession())) {
            if (IOExceptionHandler.isNotSupportedDataAccessResultException(e)) {
                collectedRegisters.add(createFailureCollectedRegister(register, ResultType.NotSupported));
            } else {
                collectedRegisters.add(createFailureCollectedRegister(register, ResultType.InCompatible, e.getMessage()));
            }
        }
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

    private AM540PLCRegisterMapper getPLCRegisterMapper() {
        if (plcRegisterMapper == null) {
            plcRegisterMapper = new AM540PLCRegisterMapper(protocol.getDlmsSession());
        }
        return plcRegisterMapper;
    }


}