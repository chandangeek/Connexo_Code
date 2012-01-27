package com.energyict.smartmeterprotocolimpl.nta.dsmr40;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.axrdencoding.*;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.AssociationLNAttributes;
import com.energyict.dlms.cosem.attributes.DataAttributes;
import com.energyict.genericprotocolimpl.common.EncryptionStatus;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterValue;
import com.energyict.smartmeterprotocolimpl.nta.abstractsmartnta.AbstractSmartNtaProtocol;
import com.energyict.smartmeterprotocolimpl.nta.dsmr23.Dsmr23RegisterFactory;
import com.energyict.smartmeterprotocolimpl.nta.dsmr40.common.customdlms.cosem.attributes.DSMR4_MbusClientAttributes;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

/**
 * Copyrights EnergyICT
 * Date: 31-aug-2011
 * Time: 16:44:24
 */
public class DSMR40RegisterFactory extends Dsmr23RegisterFactory {

    public static final ObisCode MbusEncryptionStatus_New = ObisCode.fromString("0.x.24.1.0.13");   // ObisCode from MbusSetup object with AttributeNr. as F-field
    public static final ObisCode MbusKeyStatusObisCode = ObisCode.fromString("0.x.24.1.0.15"); // ObisCode from MbusSetup object with AttributNr. as F-field

    public static final ObisCode SecurityPolicyObisCode = ObisCode.fromString("0.0.43.0.0.2");
    public static final ObisCode HighLevelSecurityObisCode = ObisCode.fromString("0.0.40.0.0.6");

    public static final ObisCode AdministrativeStatusObisCode = ObisCode.fromString("0.1.94.31.0.255");

    public DSMR40RegisterFactory(final AbstractSmartNtaProtocol protocol) {
        super(protocol);
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the {@link com.energyict.protocol.Register} is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
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
        if (rObisCode.equals(MbusEncryptionStatus_New) || rObisCode.equalsIgnoreBChannel(MbusEncryptionStatus)) {     // if they still use the old obiscode, then read the new object
            long encryptionValue = abstractDataType.longValue();
            Quantity quantity = new Quantity(BigDecimal.valueOf(encryptionValue), Unit.getUndefined());
            String text = EncryptionStatus.forValue((int) encryptionValue).getLabelKey();
            return new RegisterValue(register, quantity, null, null, null, new Date(), 0, text);
        } else if (rObisCode.equals(MbusKeyStatusObisCode)) {
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
        } else if (rObisCode.equals(CORE_FIRMWARE_SIGNATURE) || rObisCode.equals(MODULE_FIRMWARE_SIGNATURE)) {
            return new RegisterValue(register, null, null, null, null, new Date(), 0, new String(abstractDataType.getContentByteArray()));
        }

        return super.convertCustomAbstractObjectsToRegisterValues(register, abstractDataType);
    }

    private enum MbusKeyStatus {

        No_Keys(0, "No Keys Available"),
        Keys_Received_From_CS(1, "Keys received from Central System"),
        Keys_Forward_To_Mbus(2, "Keys forwarded to Mbus device");

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
            return "UnKnown State";
        }

        private int getValue() {
            return this.value;
        }

        private String getDescription() {
            return this.description;
        }
    }

    private enum AdministrativeStatus {

        OPT_OUT(0, "Administrative off (Opt Out)"),
        DEFAULT(1, "Administrative on (Default)"),
        OPT_IN(2, "Meter is administrative on and reading of profile data is allowed (Opt In)");

        private final int value;
        private final String description;

        AdministrativeStatus(final int value, final String description) {
            this.value = value;
            this.description = description;
        }

        public static String getDescriptionForValue(int value) {
            for (AdministrativeStatus administrativeStatus : values()) {
                if(administrativeStatus.getValue() == value){
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