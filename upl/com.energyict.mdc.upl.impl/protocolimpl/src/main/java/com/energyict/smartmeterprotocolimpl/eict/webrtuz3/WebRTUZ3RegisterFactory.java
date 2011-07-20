package com.energyict.smartmeterprotocolimpl.eict.webrtuz3;

import com.energyict.cbo.*;
import com.energyict.dlms.*;
import com.energyict.dlms.axrdencoding.AbstractDataType;
import com.energyict.dlms.cosem.*;
import com.energyict.dlms.cosem.attributes.RegisterAttributes;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.*;
import com.energyict.protocol.Register;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.WebRTUZ3;
import com.energyict.smartmeterprotocolimpl.eict.webrtuz3.composedobjects.ComposedRegister;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Copyrights EnergyICT
 * Date: 28-feb-2011
 * Time: 9:50:47
 */
public class WebRTUZ3RegisterFactory implements BulkRegisterProtocol {

    public static final ObisCode CORE_FW_VERSION = ObisCode.fromString("0.0.0.2.0.255");
    public static final ObisCode ERROR_REGISTER = ObisCode.fromString("0.0.97.97.0.255");
    public static final ObisCode ALARM_REGISTER = ObisCode.fromString("0.0.97.98.0.255");
    public static final ObisCode ACTIVE_TARIFF_REGISTER = ObisCode.fromString("0.0.96.14.0.255");
    public static final ObisCode ACTIVITY_CALENDAR = ObisCode.fromString("0.0.13.0.0.255");

    private final WebRTUZ3 meterProtocol;

    private Map<Register, ComposedRegister> composedRegisterMap = new HashMap<Register, ComposedRegister>();
    private Map<Register, DLMSAttribute> registerMap = new HashMap<Register, DLMSAttribute>();

    /**
     * Default constructor
     *
     * @param meterProtocol the WebRTUZ3 protocol for this RegisterFactory
     */
    public WebRTUZ3RegisterFactory(WebRTUZ3 meterProtocol) {
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
        List<RegisterValue> registerValues = new ArrayList<RegisterValue>();
        ComposedCosemObject registerComposedCosemObject = constructComposedObjectFromRegisterList(registers, this.meterProtocol.supportsBulkRequests());

        for (Register register : registers) {
            RegisterValue rv = null;
            try {
                if (this.composedRegisterMap.containsKey(register)) {
                    ScalerUnit su = new ScalerUnit(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterUnitAttribute()));
                    if (su.getUnitCode() != 0) {
                        rv = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        su.getUnit()));
                    } else { // TODO don't do this, we should throw an exception because we will report incorrect data. (keeping it here for testing with dump meter)
                        rv = new RegisterValue(register,
                                new Quantity(registerComposedCosemObject.getAttribute(this.composedRegisterMap.get(register).getRegisterValueAttribute()).toBigDecimal(),
                                        Unit.getUndefined()));
                    }

                } else if (this.registerMap.containsKey(register)) {
                    rv = convertCustomAbstractObjectsToRegisterValues(register, registerComposedCosemObject.getAttribute(this.registerMap.get(register)));
                }
            } catch (IOException e) {
                this.meterProtocol.getLogger().log(Level.WARNING, "Failed to fetch register with ObisCode " + register.getObisCode() + "[" + register.getSerialNumber() + "]");
            }
            if (rv != null) {
                registerValues.add(rv);
            }
        }
        return registerValues;
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
    protected ComposedCosemObject constructComposedObjectFromRegisterList(List<Register> registers, boolean supportsBulkRequest) {

        if (registers != null) {
            List<DLMSAttribute> dlmsAttributes = new ArrayList<DLMSAttribute>();
            for (Register register : registers) {
                ObisCode rObisCode = getCorrectedRegisterObisCode(register);

                UniversalObject uo = DLMSUtils.findCosemObjectInObjectList(this.meterProtocol.getDlmsSession().getMeterConfig().getInstantiatedObjectList(), rObisCode);
                if (uo != null) {
                    if (uo.getClassID() == DLMSClassId.REGISTER.getClassId() || uo.getClassID() == DLMSClassId.EXTENDED_REGISTER.getClassId()) {
                        ComposedRegister composedRegister = new ComposedRegister(new DLMSAttribute(rObisCode, RegisterAttributes.Register_Value.getAttributeNumber(), uo.getClassID()),
                                new DLMSAttribute(rObisCode, RegisterAttributes.Register_Unit.getAttributeNumber(), uo.getClassID()));
                        dlmsAttributes.add(composedRegister.getRegisterValueAttribute());
                        dlmsAttributes.add(composedRegister.getRegisterUnitAttribute());
                        this.composedRegisterMap.put(register, composedRegister);
                    } else {
                        // We get the default 'Value' attribute (2)
                        this.registerMap.put(register, new DLMSAttribute(rObisCode, DLMSCOSEMGlobals.ATTR_DATA_VALUE, uo.getClassID()));
                        dlmsAttributes.add(this.registerMap.get(register));
                    }
                } else {
                    this.meterProtocol.getLogger().log(Level.INFO, "Register with ObisCode " + rObisCode + " is not supported.");
                }
            }
            return new ComposedCosemObject(this.meterProtocol.getDlmsSession(), supportsBulkRequest, dlmsAttributes);
        }
        return null;
    }

    public ObisCode getCorrectedRegisterObisCode(Register register) {
        return this.meterProtocol.getPhysicalAddressCorrectedObisCode(register.getObisCode(), register.getSerialNumber());
    }

    private RegisterValue convertCustomAbstractObjectsToRegisterValues(Register register, AbstractDataType abstractDataType) throws UnsupportedException {

        //TODO need to check if register needs to be build with value AND text or just one of the two.
        //TODO loop over the ObisCodeMapper list of the webrtu.common.ObisCodeMapper, we should support all of them ...
        ObisCode rObisCode = getCorrectedRegisterObisCode(register);
        if (isSupportedByProtocol(rObisCode)) {
            return new RegisterValue(register, new Quantity(abstractDataType.longValue(), Unit.getUndefined()), null, null, null, new Date(), 0, String.valueOf(abstractDataType.longValue()));
        } else {
            throw new UnsupportedException("Register with obisCode " + rObisCode + " is not supported.");
        }
    }

    private static boolean isSupportedByProtocol(ObisCode obisCode) {
        return ERROR_REGISTER.equals(obisCode) || ALARM_REGISTER.equals(obisCode) || ACTIVE_TARIFF_REGISTER.equals(obisCode)
                || CORE_FW_VERSION.equals(obisCode) || ACTIVITY_CALENDAR.equals(obisCode);
    }
}
