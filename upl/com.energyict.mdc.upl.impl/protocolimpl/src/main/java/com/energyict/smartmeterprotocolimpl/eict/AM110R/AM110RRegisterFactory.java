package com.energyict.smartmeterprotocolimpl.eict.AM110R;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.VisibleString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.ExtendedRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.BulkRegisterProtocol;
import com.energyict.protocol.Register;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;

public class AM110RRegisterFactory implements BulkRegisterProtocol {

    // Regular event logs
    public static final ObisCode STANDARD_EVENT_LOG = ObisCode.fromString("0.0.99.98.0.255");
    public static final ObisCode FRAUD_DETECTION_EVENT_LOG = ObisCode.fromString("0.0.99.98.1.255");
    public static final ObisCode DISCONNECT_CONTROL_EVENT_LOG = ObisCode.fromString("0.0.99.98.2.255");
    public static final ObisCode FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.3.255");
    public static final ObisCode POWER_QUALITY_EVENT_LOG = ObisCode.fromString("0.0.99.98.4.255");
    public static final ObisCode COMM_FAILURE_EVENT_LOG = ObisCode.fromString("0.0.99.98.5.255");
    public static final ObisCode PREPAYMENT_EVENT_LOG = ObisCode.fromString("0.0.99.98.6.255");
    public static final ObisCode NOTIFICATION_FLAGS_EVENT_LOG = ObisCode.fromString("0.0.99.98.8.255");
    public static final ObisCode TARIFF_UPDATES_EVENT_LOG = ObisCode.fromString("0.0.99.98.9.255");
    public static final ObisCode MIRROR_UPDATES_EVENT_LOG = ObisCode.fromString("0.0.99.98.10.255");
    public static final ObisCode MANUFACTURER_SPECIFIC_EVENT_LOG = ObisCode.fromString("0.0.99.98.31.255");

    // Custom event logs
    public static final ObisCode HAN_MANAGEMENT_EVENT_LOG = ObisCode.fromString("0.0.99.98.12.255");
    public static final ObisCode ELSTER_FIRMWARE_EVENT_LOG = ObisCode.fromString("0.0.99.98.20.255");
    public static final ObisCode DEBUG_EVENT_LOG = ObisCode.fromString("0.0.96.128.0.255");

    // Firmware update
    public static final ObisCode FIRMWARE_UPDATE = ObisCode.fromString("0.0.44.0.0.255");
    public static final ObisCode IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.0.15.0.2.255");
    public static final ObisCode ZIGBEE_NCP_FIRMWARE_UPDATE = ObisCode.fromString("0.1.44.0.0.255");
    public static final ObisCode ZIGBEE_NCP_IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.1.15.0.2.255");
    public static final ObisCode IHD_FIRMWARE_UPDATE = ObisCode.fromString("0.2.44.0.0.255");
    public static final ObisCode IHD_IMAGE_ACTIVATION_SCHEDULER = ObisCode.fromString("0.0.15.0.2.255");

    // Others
    public static final ObisCode GPRS_MANUAL_ROAMING_SETUP = ObisCode.fromString("0.128.0.0.8.255");
    public static ObisCode KEYS_LOCK_DOWN_SWITCH_OBIS = ObisCode.fromString("0.128.0.0.1.255");
    public static final ObisCode REBOOT_OBISCODE = ObisCode.fromString("0.0.10.0.0.255");

    private final AM110R meterProtocol;

    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<Register, ComposedRegister>();
    private Map<Register, DLMSAttribute> registerMap = new HashMap<Register, DLMSAttribute>();

    public AM110RRegisterFactory(AM110R meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    public RegisterInfo translateRegister(Register register) throws IOException {
        return new RegisterInfo("AM110R register " + register.getObisCode());
    }

    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(registers, this.meterProtocol.supportsBulkRequests());

        for (Register register : registers) {
            RegisterValue rv = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ComposedRegister composedRegister = this.composedRegisterMap.get(register);
                    DLMSAttribute registerValueAttribute = composedRegister.getRegisterValueAttribute();
                    BigDecimal value = registerComposedCosemObject.getAttribute(registerValueAttribute).toBigDecimal();
                    DLMSAttribute registerUnitAttribute = composedRegister.getRegisterUnitAttribute();
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(registerUnitAttribute));
                    Date eventTime = null;   // Optional capture time attribute
                    DLMSAttribute registerCaptureTime = this.composedRegisterMap.get(register).getRegisterCaptureTime();
                    if (registerCaptureTime != null) {
                        AbstractDataType attribute = registerComposedCosemObject.getAttribute(registerCaptureTime);
                        eventTime = attribute.getOctetString().getDateTime(meterProtocol.getDlmsSession().getTimeZone()).getValue().getTime();
                    }

                    if (su.getUnitCode() == 0) {
                        String message = "Register with ObisCode: " + register.getObisCode() + " does not provide a proper Unit.";
                        this.meterProtocol.getLogger().log(Level.INFO, message);

                        su = new ScalerUnit(Unit.getUndefined());
                        // throw new IOException(message);
                    }
                    rv = new RegisterValue(register, new Quantity(value, su.getEisUnit()), eventTime);
                } else if (this.registerMap.containsKey(register)) {
                    rv = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                }
            } catch (IOException e) {
                this.meterProtocol.getLogger().log(Level.SEVERE, "Failed to fetch register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "]: " + e.getMessage());
            }
            if (rv != null) {
                registerValues.add(rv);
            }
        }
        return registerValues;
    }

    /**
     * Construct a ComposedCosemObject from a list of <CODE>Registers</CODE>.
     * If the {@link com.energyict.protocol.Register} is a DLMS {@link com.energyict.dlms.cosem.Register}, {@link com.energyict.dlms.cosem.ExtendedRegister} or {@link com.energyict.dlms.cosem.DemandRegister},
     * and the ObisCode is listed in the ObjectList(see {@link com.energyict.dlms.DLMSMeterConfig#getInstance(String)}, then we define a ComposedRegister and add
     * it to the {@link #composedRegisterMap}. Otherwise if it is not a DLMS <CODE>Register</CODE> or <CODE>ExtendedRegister</CODE>, but the ObisCode exists in the
     * ObjectList, then we just add it to the {@link #registerMap}. The handling of the <CODE>registerMap</CODE> should be done by the {@link #readRegisters(java.util.List)}
     * method for each <CODE>ObisCode</CODE> in specific.
     *
     * @param registers           the Registers to convert
     * @param supportsBulkRequest indicates whether a DLMS Bulk reques(getWithList) is desired
     * @return a ComposedCosemObject or null if the list was empty
     */
    protected ComposedCosemObject constructComposedObjectFromRegisterList(List<Register> registers, boolean supportsBulkRequest) throws IOException {
        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = register.getObisCode();

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    // ALL registers and ExtendedRegisters will be supported
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        DLMSAttribute valueAttribute = new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute captureTimeAttribute = null;  //Optional attribute
                        if (uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                            captureTimeAttribute = new DLMSAttribute(rObisCode, ExtendedRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());
                        }
                        ComposedRegister composedRegister = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        if (composedRegister.getRegisterCaptureTime() != null) {
                            dlmsAttributes.add(composedRegister.getRegisterCaptureTime());
                        }
                        this.composedRegisterMap.put(register, composedRegister);
                    }

                    // All Demand registers will be supported
                    else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                        DLMSAttribute valueAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute unitAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID());
                        DLMSAttribute captureTimeAttribute = new DLMSAttribute(rObisCode, DemandRegisterAttributes.CAPTURE_TIME.getAttributeNumber(), uo.getClassID());
                        ComposedRegister composedRegister = new ComposedRegister(valueAttribute, unitAttribute, captureTimeAttribute);
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterCaptureTime());
                        this.composedRegisterMap.put(register, composedRegister);
                    }

                    // The other DLMS classes need to be custom implemented, they are added to a general registerMap
                    // Note: We get the default 'Value' attribute (2)
                    else {
                        this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                } else {
                    this.meterProtocol.getLogger().log(Level.WARNING, rObisCode.toString() + " not found in meter's instantiated object list!");
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    private RegisterValue convertCustomAbstractObjectsToRegisterValues(Register register, AbstractDataType abstractDataType) throws IOException {

        // If the abstractDataType is of simple type (e.g.: Unsigned16 / OctetString), we can parse the content.
        if (abstractDataType.isNumerical()) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()));
        } else if (abstractDataType.isOctetString()) {
            return new RegisterValue(register, ((OctetString) abstractDataType).stringValue());
        } else if (abstractDataType.isVisibleString()) {
            return new RegisterValue(register, ((VisibleString) abstractDataType).getStr());
        } else if (abstractDataType.isBooleanObject()) {
            Boolean state = ((BooleanObject) abstractDataType).getState();
            return new RegisterValue(register, state.toString());
        }
        throw new IOException("DLMS object with obiscode " + register.getObisCode() + " cannot be read out as a register.");
    }
}
