/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.smartmeterprotocolimpl.eict.ukhub;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.common.Quantity;
import com.energyict.mdc.common.Unit;
import com.energyict.mdc.protocol.api.device.data.Register;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;
import com.energyict.mdc.protocol.api.legacy.BulkRegisterProtocol;

import com.energyict.dlms.DLMSAttribute;
import com.energyict.dlms.DLMSCOSEMGlobals;
import com.energyict.dlms.DLMSUtils;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.UniversalObject;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.cosem.ComposedCosemObject;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.attributes.DemandRegisterAttributes;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.smartmeterprotocolimpl.common.composedobjects.ComposedRegister;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class UkHubRegisterFactory implements BulkRegisterProtocol {

//    public static final ObisCode CORE_FW_VERSION = ObisCode.fromString("0.0.0.2.0.255");
    public static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
//    public static final ObisCode ACTIVE_TARIFF_REGISTER = ObisCode.fromString("0.0.96.14.0.255");
//    public static final ObisCode ACTIVITY_CALENDAR = ObisCode.fromString("0.0.13.0.0.255");

    public static final ObisCode DeviceId1 = ObisCode.fromString("0.0.96.1.0.255");     // HUB SerialNumber
    public static final ObisCode DeviceId2 = ObisCode.fromString("0.0.96.1.1.255");     // UtilitySpecified EquipmentID
    public static final ObisCode DeviceId3 = ObisCode.fromString("0.0.96.1.2.255");     //E-Function location details, e.g. 48 chars (maybe need removing)
    public static final ObisCode DeviceId4 = ObisCode.fromString("0.0.96.1.3.255");     //E-location information - 48 chars
    public static final ObisCode DeviceId5 = ObisCode.fromString("0.0.96.1.4.255");     //E-configuration information - 16 chars
    public static final ObisCode DeviceId6 = ObisCode.fromString("0.0.96.1.5.255");     //Manufacturer Name
    public static final ObisCode DeviceId7 = ObisCode.fromString("0.0.96.1.6.255");     //Manufacture ID (ZigBee MSP ID [SSWG code for Clusters])
    public static final ObisCode DeviceId8 = ObisCode.fromString("0.0.96.1.7.255");     //PAYG ID
    public static final ObisCode DeviceId10 = ObisCode.fromString("0.0.96.1.9.255");     //Serial Number of Module
    public static final ObisCode MeteringPointId = ObisCode.fromString("0.0.96.1.10.255");     //MPAN or the MPRN
    public static final ObisCode DeviceId50 = ObisCode.fromString("0.0.96.1.50.255");     //hours in operation
    public static final ObisCode DeviceId51 = ObisCode.fromString("0.0.96.1.51.255");     //hours in fault
    public static final ObisCode DeviceId52 = ObisCode.fromString("0.0.96.1.52.255");     //remaining battery life

    public static final ObisCode OperationalFirmwareMonolitic = ObisCode.fromString("0.0.0.2.0.255");
    public static final ObisCode OperationalFirmwareMID = ObisCode.fromString("0.1.0.2.0.255");
    public static final ObisCode OperationalFirmwareNonMIDApp = ObisCode.fromString("0.2.0.2.0.255");
    public static final ObisCode OperationalFirmwareBootloader = ObisCode.fromString("0.3.0.2.0.255");
    public static final ObisCode OperationalFirmwareZCLVersion = ObisCode.fromString("0.4.0.2.0.255");
    public static final ObisCode OperationalFirmwareStackVersion = ObisCode.fromString("0.5.0.2.0.255");
    public static final ObisCode OperationalFirmwareZigbeeChip = ObisCode.fromString("0.6.0.2.0.255");
    public static final ObisCode OperationalFirmwareHAN = ObisCode.fromString("0.7.0.2.0.255");
    public static final ObisCode OperationalFirmwareWAN = ObisCode.fromString("0.8.0.2.0.255");

    private final UkHub meterProtocol;

    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<>();
    private Map<Register, DLMSAttribute> registerMap = new HashMap<>();

    /**
     * Default constructor
     *
     * @param meterProtocol the UkHub protocol for this RegisterFactory
     */
    public UkHubRegisterFactory(UkHub meterProtocol) {
        this.meterProtocol = meterProtocol;
    }

    /**
     * This method is used to request a RegisterInfo object that gives info
     * about the meter's supporting the specific ObisCode. If the ObisCode is
     * not supported, NoSuchRegister is thrown.
     *
     * @param register the Register to request RegisterInfo for
     * @return RegisterInfo about the ObisCode
     * @throws java.io.IOException Thrown in case of an exception
     */
    public RegisterInfo translateRegister(Register register) throws IOException {
        return null;  //TODO implement proper functionality.
    }

    /**
     * Request an array of RegisterValue objects for an given List of ObisCodes. If the ObisCode is not
     * supported, there should not be a register value in the list.
     *
     * @param registers The Registers for which to request a RegisterValues
     * @return List<RegisterValue> for an List of ObisCodes
     * @throws java.io.IOException Thrown in case of an exception
     */
    public List<RegisterValue> readRegisters(List<Register> registers) throws IOException {
        List<RegisterValue> registerValues = new ArrayList<>();
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
                    if (su.getUnitCode() == 0) {
                        throw new IOException("Register with ObisCode: " + register.getObisCode() + " does not provide a proper Unit.");
                    }
                    rv = new RegisterValue(register, new Quantity(value, su.getEisUnit()));
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
     * If the {@link com.energyict.mdc.protocol.api.device.data.Register} is a DLMS {@link com.energyict.dlms.cosem.Register} or {@link com.energyict.dlms.cosem.ExtendedRegister},
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
            List<DLMSAttribute> dlmsAttributes = new ArrayList<>();
            for (Register register : registers) {
                ObisCode rObisCode = register.getObisCode();

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    // ALL registers and ExtendedRegisters will be supported
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, RegisterAttributes.VALUE.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, RegisterAttributes.SCALER_UNIT.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    }

                    // All Demand registers will be supported
                    else if (uo.getClassID() == DLMSClassId.DEMAND_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, DemandRegisterAttributes.CURRENT_AVG_VALUE.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, DemandRegisterAttributes.UNIT.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
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
        } else if (abstractDataType.isBooleanObject()) {
            boolean state = ((BooleanObject) abstractDataType).getState();
            return new RegisterValue(register, "" + state);
        }
        throw new IOException("DLMS object with obiscode " + register.getObisCode() + " cannot be read out as a register.");
    }

}
