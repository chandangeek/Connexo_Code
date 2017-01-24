package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers;

import com.energyict.mdc.common.ApplicationException;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.BulkRegisterProtocol;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.util.DateTime;
import com.energyict.dlms.cosem.ActivePassive;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.Data;
import com.energyict.dlms.cosem.DemandRegister;
import com.energyict.dlms.cosem.ExtendedRegister;
import com.energyict.obis.ObisCode;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ObisCodeProvider;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 25/07/11
 * Time: 9:26
 */
public class ZigbeeGasRegisterFactory implements BulkRegisterProtocol {

    private ZigbeeGas zigbeeGas;
    private ObisCodeMapper obisCodeMapper = null;

    public ZigbeeGasRegisterFactory(ZigbeeGas zigbeeGas) {
        this.zigbeeGas = zigbeeGas;
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return new RegisterInfo(register.getObisCode().getDescription());
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        for (Register register : registers) {
            try {
                registerValues.add(readSingleRegister(register));
            } catch (IOException e) {
                if(e.getMessage().contains("ConnectionException: receiveResponse() interframe timeout error")){
                    throw e;
                } else {
                    getZigbeeGas().getLogger().severe("Unable to read register [" + register.getObisCode() + "]: " + e.getMessage());
                }
            } catch (ApplicationException e) {
                getZigbeeGas().getLogger().severe("Unable to read register [" + register.getObisCode() + "]: " + e.getMessage());
            }
        }
        return registerValues;
    }

    private RegisterValue readSingleRegister(Register register) throws IOException {
        ObisCode obisCode = register.getObisCode();

        final UniversalObject universalObject = zigbeeGas.getDlmsSession().getMeterConfig().findObject(obisCode);
        if (universalObject.getClassID() == DLMSClassId.REGISTER.getClassId()) {
            com.energyict.dlms.cosem.Register registerObject = getCosemObjectFactory().getRegister(obisCode);
            Quantity quantity = registerObject.getQuantityValue();
            return new RegisterValue(register, quantity);
        } else if (universalObject.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
            ExtendedRegister extendedRegister = getCosemObjectFactory().getExtendedRegister(obisCode);
            Quantity quantity = extendedRegister.getQuantityValue();
            Date captureTime = extendedRegister.getCaptureTime();
            Date billingDate = extendedRegister.getBillingDate();
            return new RegisterValue(register, quantity, captureTime, billingDate);
        } else if (universalObject.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
            DemandRegister demandRegister = getCosemObjectFactory().getDemandRegister(obisCode);
            Quantity quantity = demandRegister.getQuantityValue();
            Date captureTime = demandRegister.getCaptureTime();
            Date billingDate = demandRegister.getBillingDate();
            return new RegisterValue(register, quantity, captureTime, billingDate);
        } else if (universalObject.getClassID() == DLMSClassId.DATA.getClassId()) {
            Data data = getCosemObjectFactory().getData(obisCode);
            if (data.getValueAttr().isNumerical()) {
                return new RegisterValue(register, new Quantity(data.getValue(), Unit.getUndefined()));
            } else if (data.getValueAttr().isOctetString()) {
                return new RegisterValue(register, data.getString());
            } else if (data.getValueAttr().isBooleanObject()) {
                boolean state = ((BooleanObject) data.getValueAttr()).getState();
                return new RegisterValue(register, "" + state);
            } else {
                throw new IOException("DLMS object with obiscode " + register.getObisCode() + " cannot be read out as a register.");
            }
        } else if (universalObject.getClassID() == DLMSClassId.ACTIVE_PASSIVE.getClassId()) {
            ActivePassive activePassive = getCosemObjectFactory().getActivePassive(obisCode);
            AbstractDataType dataType = activePassive.getValue();
            if (dataType.isNumerical()) {
                Unsigned32 value = dataType.getUnsigned32();
                ScalerUnit scalerUnit = activePassive.getScalerUnit();
                Quantity quantity = new Quantity(value.getValue(), scalerUnit.getEisUnit());
                return new RegisterValue(register, quantity);
            } else if (dataType.isOctetString()) {
                if (obisCode.equals(ObisCodeProvider.cotManagement)) {
                    DateTime dateTime = ((OctetString) dataType).getDateTime(getZigbeeGas().getTimeZone());
                    if (dateTime != null) {
                        return new RegisterValue(register, new Quantity(dateTime.getValue().getTimeInMillis(), Unit.getUndefined()), dateTime.getValue().getTime());
                    }
                }
                return new RegisterValue(register, ((OctetString) dataType).stringValue());
            } else {
                throw new IOException("DLMS object with obiscode " + register.getObisCode() + " cannot be read out as a register.");
            }
        }
        throw new IOException("DLMS object with obiscode " + register.getObisCode() + " has DLMS class " + universalObject.getClassID() + " - object cannot be readout as a register.");
    }

    private CosemObjectFactory getCosemObjectFactory() {
        return getZigbeeGas().getDlmsSession().getCosemObjectFactory();
    }

    public ZigbeeGas getZigbeeGas() {
        return zigbeeGas;
    }

    public ObisCodeMapper getObisCodeMapper() {
        if (obisCodeMapper == null) {
            this.obisCodeMapper = new ObisCodeMapper();
        }
        return obisCodeMapper;
    }

}
