package com.energyict.smartmeterprotocolimpl.nta.dsmr50.elster.am540.registers;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.util.AXDRDate;
import com.energyict.dlms.axrdencoding.util.AXDRTime;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.SingleActionSchedule;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.dlms.common.DLMSStoredValues;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.DSMR40RegisterFactory;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 *
 * @author khe
 * @since 5/06/2014 - 15:16
 */
public class AM540RegisterFactory extends DSMR40RegisterFactory {

    public static final ObisCode ClockObisCode = ObisCode.fromString("0.0.1.0.0.255");
    public static final ObisCode EndOfBillingPeriod1SchedulerObisCode = ObisCode.fromString("0.0.15.0.0.255");
    public static final ObisCode BillingProfileObisCode = ObisCode.fromString("0.0.98.1.0.255");

    private AM540PLCRegisterMapper plcRegisterMapper;

    public AM540RegisterFactory(AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return new RegisterInfo(register.getObisCode().toString());
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> result = new ArrayList<RegisterValue>();
        List<Register> normalRegisters = new ArrayList<Register>();

        //First read out the G3 PLC registers, using the G3 PLC register mapper
        for (Register register : registers) {
            ObisCode obisCode = register.getObisCode();
            if (getPLCRegisterMapper().getG3Mapping(obisCode) != null) {
                try {
                    RegisterValue registerValue = getPLCRegisterMapper().readRegister(obisCode);
                    //Now include the serial number
                    registerValue = new RegisterValue(register, registerValue.getQuantity(), registerValue.getEventTime(), registerValue.getFromTime(), registerValue.getToTime(), registerValue.getReadTime(), registerValue.getRtuRegisterId(), registerValue.getText());
                    result.add(registerValue);
                } catch (NoSuchRegisterException e) {
                    protocol.getLogger().warning("Register with obiscode " + obisCode + " is not supported: " + e.getMessage());
                } catch (DataAccessResultException e) {
                    protocol.getLogger().warning("Error while reading out register with obiscode " + obisCode + ": " + e.getMessage());
                }

                // Else try to read out specific DSMR5.0 registers
            } else if (obisCode.equals(ClockObisCode)) {
                Date time = getProtocol().getTime();
                RegisterValue registerValue = new RegisterValue(register, new Quantity(BigDecimal.valueOf(time.getTime()), Unit.get(BaseUnit.SECOND, -3)), null, null, new Date(), new Date(), -1, time.toString());
                result.add(registerValue);
            } else if (obisCode.equals(EndOfBillingPeriod1SchedulerObisCode)) {
                SingleActionSchedule singleActionSchedule = getProtocol().getDlmsSession().getCosemObjectFactory().getSingleActionSchedule(EndOfBillingPeriod1SchedulerObisCode);
                Array executionTime = singleActionSchedule.getExecutionTime();
                RegisterValue registerValue = new RegisterValue(register, parseExecutionTimeArrayToHumanReadableText(executionTime));                result.add(registerValue);
            } else if (obisCode.equals(BillingProfileObisCode)) {
                DLMSStoredValues dlmsStoredValues = new DLMSStoredValues(protocol.getDlmsSession().getCosemObjectFactory(), BillingProfileObisCode);
                Date billingPointTimeDate = dlmsStoredValues.getBillingPointTimeDate(0);
                RegisterValue registerValue = new RegisterValue(register, new Quantity(BigDecimal.valueOf(billingPointTimeDate.getTime()), Unit.get(BaseUnit.SECOND, -3)), null, null, new Date(), new Date(), -1, billingPointTimeDate.toString());
                result.add(registerValue);

                // Else read out as regular Dsmr4.0 register
            } else {
                normalRegisters.add(register);
            }
        }

        //Now read out the "normal" DSMR registers, using the DSMR4.0 register factory
        result.addAll(super.readRegisters(normalRegisters));

        //Finally, return all the register values
        return result;
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
