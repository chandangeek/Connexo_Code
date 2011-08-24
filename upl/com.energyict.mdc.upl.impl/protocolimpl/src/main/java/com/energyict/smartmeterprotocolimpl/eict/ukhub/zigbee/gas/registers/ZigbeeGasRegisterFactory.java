package com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.registers;

import com.energyict.cbo.*;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.Register;
import com.energyict.smartmeterprotocolimpl.eict.ukhub.zigbee.gas.ZigbeeGas;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

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
            }
        }
        return registerValues;
    }

    private RegisterValue readSingleRegister(Register register) throws IOException {
        ObisCode obisCode = register.getObisCode();
        if (getObisCodeMapper().isAbstractRegister(obisCode)) {
            DLMSAttribute attribute = getObisCodeMapper().getAbstractRegisterDLMSAttribute(obisCode);
            if (attribute.getDLMSClassId().isRegister()) {
                com.energyict.dlms.cosem.Register registerObject = getCosemObjectFactory().getRegister(obisCode);
                Quantity quantity = registerObject.getQuantityValue();
                return new RegisterValue(register, quantity);
            } else if (attribute.getDLMSClassId().isDemandRegister()) {
                DemandRegister demandRegister = getCosemObjectFactory().getDemandRegister(obisCode);
                Quantity quantity = demandRegister.getQuantityValue();
                Date captureTime = demandRegister.getCaptureTime();
                Date billingDate = demandRegister.getBillingDate();
                return new RegisterValue(register, quantity, captureTime, billingDate);
            } else if (attribute.getDLMSClassId().isExtendedRegister()) {
                ExtendedRegister extendedRegister = getCosemObjectFactory().getExtendedRegister(obisCode);
                Quantity quantity = extendedRegister.getQuantityValue();
                Date captureTime = extendedRegister.getCaptureTime();
                Date billingDate = extendedRegister.getBillingDate();
                return new RegisterValue(register, quantity, captureTime, billingDate);
            } else if(attribute.getDLMSClassId().isData()){
                Data data = getCosemObjectFactory().getData(obisCode);
                Long value = data.getValue();
                return new RegisterValue(register, new Quantity(new BigDecimal(value), Unit.get(BaseUnit.HOUR)), null, null, null, null, -1, String.valueOf(value));
            }
        } else if (getObisCodeMapper().isAbstractTextRegister(obisCode)) {
            DLMSAttribute attribute = getObisCodeMapper().getAbstractTextDLMSAttribute(obisCode);
            Data data = getCosemObjectFactory().getData(obisCode);
//            GenericRead genericRead = getCosemObjectFactory().getGenericRead(attribute);
            return new RegisterValue(register, new String(data.getAttrbAbstractDataType(DataAttributes.VALUE.getAttributeNumber()).toByteArray()));
        } else if (getObisCodeMapper().isAbstractValueRegister(obisCode)) {
            DLMSAttribute attribute = getObisCodeMapper().getAbstractValueDLMSAttribute(obisCode);
            GenericRead genericRead = getCosemObjectFactory().getGenericRead(attribute);
            Quantity quantity = new Quantity(genericRead.getValue(), Unit.getUndefined());
            return new RegisterValue(register, quantity);
        }
        throw new IOException("Register with obisCode [" + obisCode + "] not found in mapping.");
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
