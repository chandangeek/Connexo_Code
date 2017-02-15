/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BitString;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Structure;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.cosem.AssociationLN;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.SecuritySetup;
import com.energyict.dlms.cosem.attributes.AssociationLNAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.protocolimpl.generic.EncryptionStatus;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.attributes.DSMR4_MbusClientAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class DSMR40RegisterFactory extends Dsmr23RegisterFactory {

    public static final ObisCode MbusEncryptionStatus_New = ObisCode.fromString("0.x.24.1.0.13");   // ObisCode from MbusSetup object with AttributeNr. as F-field
    public static final ObisCode MbusKeyStatusObisCode = ObisCode.fromString("0.x.24.1.0.14"); // ObisCode from MbusSetup object with AttributNr. as F-field

    public static final ObisCode SecurityPolicyObisCode = ObisCode.fromString("0.0.43.0.0.2");
    public static final ObisCode HighLevelSecurityObisCode = ObisCode.fromString("0.0.40.0.0.6");
    public static final ObisCode GPRSNetworkInformation = ObisCode.fromString("0.1.94.31.4.255");
    public static final ObisCode ConfigurationObject = ObisCode.fromString("0.1.94.31.3.255");

    public static final ObisCode AdministrativeStatusObisCode = ObisCode.fromString("0.1.94.31.0.255");
    private static final int BULK_RESQUEST_LIMIT = 5;  //The number of attributes in a bulk request should be smaller than 16. Note that 2 or 3 attributes are read out for every register!

    public DSMR40RegisterFactory(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        registers = filterOutAllInvalidRegisters(registers);
        List<Register> toRead;
        List<RegisterValue> result = new ArrayList<RegisterValue>();
        int count = 0;
        while (((count + 1) * BULK_RESQUEST_LIMIT) <= registers.size()) {    //Read out in steps of 5 registers
            toRead = registers.subList(count * BULK_RESQUEST_LIMIT, (count + 1) * BULK_RESQUEST_LIMIT);
            result.addAll(super.readRegisters(toRead));
            count++;
        }
        result.addAll(super.readRegisters(registers.subList(count * BULK_RESQUEST_LIMIT, registers.size()))); //Read out the remaining registers
        return result;
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the Register is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
     * and the ObisCode is listed in the ObjectList(see {@link com.energyict.dlms.DLMSMeterConfig#getInstance(String)}, then we define a ComposedRegister and add
     * it to the {@link #composedRegisterMap}. Otherwise if it is not a DLMS <CODE>Register</CODE> or <CODE>ExtendedRegister</CODE>, but the ObisCode exists in the
     * ObjectList, then we just add it to the {@link #registerMap}. The handling of the <CODE>registerMap</CODE> should be done by the {@link #readRegisters(java.util.List)}
     * method for each <CODE>ObisCode</CODE> in specific.
     *
     * @param registers           the Registers to convert
     * @param supportsBulkRequest indicates whether a DLMS Bulk reques(getWithList) is desired
     * @return a ComposedCosemObject or null if the list was empty
     */
    @Override
    protected ComposedCosemObject constructComposedObjectFromRegisterList(final List<Register> registers, final boolean supportsBulkRequest) {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                try {
                    ObisCode rObisCode = getCorrectedRegisterObisCode(register);
                    if (rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus_New) || rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus)) {     // if they still use the old obiscode, then read the new object
                        ObisCode mbusClientObisCode = this.protocol.getPhysicalAddressCorrectedObisCode(this.protocol.getDlmsSession().getMeterConfig().getMbusClient(0).getObisCode(), register.getSerialNumber());
                        this.registerMap.put(register, new DLMSAttribute(mbusClientObisCode, DSMR4_MbusClientAttributes.ENCRYPTION_STATUS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equalsIgnoreBChannel(MbusKeyStatusObisCode)) {
                        ObisCode mbusClientObisCode = this.protocol.getPhysicalAddressCorrectedObisCode(this.protocol.getDlmsSession().getMeterConfig().getMbusClient(0).getObisCode(), register.getSerialNumber());
                        this.registerMap.put(register, new DLMSAttribute(mbusClientObisCode, DSMR4_MbusClientAttributes.KEY_STATUS.getAttributeNumber(), DLMSClassId.MBUS_CLIENT.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(SecurityPolicyObisCode)) {
                        this.registerMap.put(register, new DLMSAttribute(SecuritySetup.getDefaultObisCode(), 2, DLMSClassId.SECURITY_SETUP.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(HighLevelSecurityObisCode)) {
                        this.registerMap.put(register, new DLMSAttribute(AssociationLN.getDefaultObisCode(), AssociationLNAttributes.AUTHENTICATION_MECHANISM_NAME.getAttributeNumber(), DLMSClassId.ASSOCIATION_LN.getClassId()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(AdministrativeStatusObisCode)) {
                        this.registerMap.put(register, new DLMSAttribute(AdministrativeStatusObisCode, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(GPRSNetworkInformation)) {
                        this.registerMap.put(register, new DLMSAttribute(GPRSNetworkInformation, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                        dlmsAttributes.add(this.registerMap.get(register));
                    } else if (rObisCode.equals(ConfigurationObject)) {
                        this.registerMap.put(register, new DLMSAttribute(ConfigurationObject, DataAttributes.VALUE.getAttributeNumber(), DLMSClassId.DATA));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                } catch (IOException e) {
                    this.protocol.getLogger().warning("Could not process register: " + register);
                }
            }
            ComposedCosemObject sRegisterList = super.constructComposedObjectFromRegisterList(registers, supportsBulkRequest);
            if (sRegisterList != null) {
                dlmsAttributes.addAll(Arrays.asList(sRegisterList.getDlmsAttributesList()));
            }
            return new ComposedCosemObject(protocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    @Override
    protected RegisterValue convertCustomAbstractObjectsToRegisterValues(final Register register, AbstractDataType abstractDataType) throws IOException {
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus_New) || rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus)) {     // if they still use the old obiscode, then read the new object
            long encryptionValue = abstractDataType.longValue();
            Quantity quantity = new Quantity(BigDecimal.valueOf(encryptionValue), Unit.getUndefined());
            String text = EncryptionStatus.forValue((int) encryptionValue).getLabelKey();
            return new RegisterValue(register, quantity, null, null, null, new Date(), 0, text);
        } else if (rObisCode.equalsIgnoreBChannel(MbusKeyStatusObisCode)) {
            int state = ((TypeEnum) abstractDataType).getValue();
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(state), Unit.getUndefined()), null, null, null, new Date(), 0, MbusKeyStatus.getDescriptionForValue(state));
        } else if (rObisCode.equals(SecurityPolicyObisCode)) {
            long securityPolicy = abstractDataType.longValue();
            Quantity quantity = new Quantity(BigDecimal.valueOf(securityPolicy), Unit.getUndefined());
            return new RegisterValue(register, quantity);
        } else if (rObisCode.equals(HighLevelSecurityObisCode)) {
            int level = -1;
            if (abstractDataType instanceof Structure) {
                level = ((Structure) abstractDataType).getDataType(((Structure) abstractDataType).nrOfDataTypes() - 1).intValue();
            } else if (abstractDataType instanceof OctetString) {
                level = ((OctetString) abstractDataType).getContentBytes()[((OctetString) abstractDataType).getContentBytes().length - 1];
            }
            Quantity quantity = new Quantity(BigDecimal.valueOf(level), Unit.getUndefined());
            return new RegisterValue(register, quantity);
        } else if (rObisCode.equals(AdministrativeStatusObisCode)) {
            int adminStatus = abstractDataType.intValue();
            return new RegisterValue(register, new Quantity(BigDecimal.valueOf(adminStatus), Unit.getUndefined()), null, null, null, new Date(), 0, AdministrativeStatus.getDescriptionForValue(adminStatus));
        } else if (rObisCode.equals(ConfigurationObject)) {
            Structure config = abstractDataType.getStructure();
            BitString flags = config.getDataType(1).getBitString();
            return new RegisterValue(register, new Quantity(flags.intValue(), Unit.get("")), null, null, new Date(), new Date(), 0, "Active flags: " + flags.asBitSet().toString());
        } else if (rObisCode.equals(GPRSNetworkInformation)) {
            Structure networkInformation = abstractDataType.getStructure();
            if (networkInformation != null) {
                int signal_strength = networkInformation.getDataType(0).intValue();
                int base_stations = networkInformation.getDataType(1).intValue();
                String network_id = networkInformation.getDataType(2).getOctetString().stringValue();
                return new RegisterValue(register, "Signal strength: " + signal_strength + ", number of base stations: " + base_stations + ", network ID: " + network_id);
            }
        } else if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE) || rObisCode.equals(MODULE_FIRMWARE_SIGNATURE)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(abstractDataType.getContentByteArray()));
        }

        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }

    private enum MbusKeyStatus {

        No_Keys(0, "No encryption key"),
        Encryption_Key_Set(1, "Encryption key is set"),
        Encryption_Key_Transferred(2, "Encryption key is transferred to the MBus device"),
        Encryption_Key_Set_And_Transferred(3, "Encryption key is set and transferred to the MBus device"),
        Encryption_Key_In_Use(4, "Encryption key is in use");

        private final int value;
        private final String description;

        MbusKeyStatus(final int value, final String description) {
            this.value = value;
            this.description = description;
        }

        public static String getDescriptionForValue(int value) {
            for (MbusKeyStatus mbusKeyStatus : values()) {
                if (mbusKeyStatus.getValue() == value) {
                    return mbusKeyStatus.getDescription();
                }
            }
            return "Unknown State";
        }

        private int getValue() {
            return this.value;
        }

        private String getDescription() {
            return this.description;
        }
    }

    private enum AdministrativeStatus {

        UNDEFINED(0, "Undefined"),
        OPT_OUT(1, "Administrative off (Opt Out)"),
        DEFAULT(2, "Administrative on (Default)"),
        OPT_IN(3, "Meter is administrative on and reading of profile data is allowed (Opt In)");

        private final int value;
        private final String description;

        AdministrativeStatus(final int value, final String description) {
            this.value = value;
            this.description = description;
        }

        public static String getDescriptionForValue(int value) {
            for (AdministrativeStatus administrativeStatus : values()) {
                if (administrativeStatus.getValue() == value) {
                    return administrativeStatus.getDescription();
                }
            }
            return "Unknown";
        }

        private int getValue() {
            return this.value;
        }

        private String getDescription() {
            return this.description;
        }
    }
}