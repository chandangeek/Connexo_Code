package com.energyict.protocolimplv2.elster.ctr.MTU155;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.mdc.meterdata.CollectedRegister;
import com.energyict.mdc.meterdata.ResultType;
import com.energyict.mdc.protocol.inbound.DeviceIdentifier;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.MdcManager;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.DeviceStatus;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.Diagnostics;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.EquipmentClassInfo;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.SealStatusBit;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.VolumeCalculationMethod;
import com.energyict.protocolimplv2.elster.ctr.MTU155.info.ZCalculationMethod;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.AbstractCTRObject;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRAbstractValue;
import com.energyict.protocolimplv2.elster.ctr.MTU155.object.field.CTRObjectID;
import com.energyict.protocolimplv2.identifiers.RegisterDataIdentifierByObisCodeAndDevice;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2010
 * Time: 10:20:17
 */
public abstract class ObisCodeMapper {

    public static final String OBIS_DEVICE_STATUS = "0.0.96.10.1.255";
    public static final String OBIS_SEAL_STATUS = "0.0.96.10.2.255";
    public static final String OBIS_DIAG = "0.0.96.10.3.255";
    public static final String OBIS_DIAG_REDUCED = "0.0.96.10.4.255";
    public static final String OBIS_EQUIPMENT_CLASS = "7.0.0.2.3.255";
    public static final String OBIS_MTU_PHONE_NR = "0.0.96.12.6.255";
    public static final String OBIS_SMSC_NUMBER = "0.0.96.50.0.255";
    public static final String OBIS_MTU_IP_ADDRESS = "0.0.96.51.0.255";
    public static final String OBIS_INSTALL_DATE = "0.0.96.52.0.255";
    public static final String OBIS_DST_IN_USE = "0.0.96.53.0.255";
    public static final String OBIS_Z_CALC_METHOD = "7.0.53.12.0.255";
    public static final String OBIS_VOLUME_CALC_METHOD = "7.0.53.12.1.255";
    public static final String OBIS_HEAD_END_IP = "0.0.2.1.0.255";
    public static final String OBIS_DATE_OF_CLOSURE_BILLING_PERIOD = "7.0.0.8.23.255";
    public static final String OBIS_WAKE_UP_PARAMTERS = "0.1.2.2.0.255";
    public static final String OBIS_FREQUENCY_STRAGY_CLIENT_CONNECTION = "0.1.2.3.0.255";
    public static final String OBIS_IDENTIFIER_TARIFF_SCHEME = "0.0.13.0.0.255";
    public static final String OBIS_VOLUNTARY_PARAMETERS_PROFILE_0 = "0.1.25.9.0.255";
    public static final String OBIS_GASDAY_START_TIME = "7.0.0.9.3.255";
    protected Logger logger;
    protected DeviceIdentifier deviceIdentifier;
    protected RequestFactory requestFactory;
    protected boolean isEK155Protocol;
    protected List<CTRRegisterMapping> registerMapping = new ArrayList<>();

    /**
     * Maps the obiscodes to a CTR Object's ID
     */
    protected void initRegisterMapping() {

        registerMapping.add(new CTRRegisterMapping("7.0.13.26.0.0", "2.1.6"));      //Tot_Vb_pf   (end of previous billing period)
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.0.255", "2.0.0"));     //Tot_Vm
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.1.255", "1.1.3"));     //Vm_g
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.0.255", "2.1.0"));     //Tot_Vb
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.1.255", "1.3.0"));     //Vb_g
        registerMapping.add(new CTRRegisterMapping("7.0.128.1.0.255", "2.3.0"));    //Tot_Vme
        registerMapping.add(new CTRRegisterMapping("7.0.128.1.1.255", "1.F.0"));    //Vme_g

        registerMapping.add(new CTRRegisterMapping("7.0.43.0.0.255", "1.0.0"));     //Qm
        registerMapping.add(new CTRRegisterMapping("7.0.43.1.0.255", "1.2.0"));     //Qb
        registerMapping.add(new CTRRegisterMapping("7.0.42.0.0.255", "4.0.0"));     //P
        registerMapping.add(new CTRRegisterMapping("7.0.41.0.0.255", "7.0.0"));     //T
        registerMapping.add(new CTRRegisterMapping("7.0.52.0.0.255", "A.0.0"));     //C, conversion factor
        registerMapping.add(new CTRRegisterMapping("7.0.53.0.1.255", "A.1.6"));     //Z, compressibility, attributed
        registerMapping.add(new CTRRegisterMapping("7.0.53.0.0.255", "A.2.0"));     //Z, compressibility, instantaneous

        registerMapping.add(new CTRRegisterMapping("7.0.43.34.0.255", "1.A.0"));    //Qbc_max

        registerMapping.add(new CTRRegisterMapping("7.0.128.2.1.255", "2.3.7"));    //Tot_Vme_f1
        registerMapping.add(new CTRRegisterMapping("7.0.128.2.2.255", "2.3.8"));    //Tot_Vme_f2
        registerMapping.add(new CTRRegisterMapping("7.0.128.2.3.255", "2.3.9"));    //Tot_Vme_f3

        registerMapping.add(new CTRRegisterMapping("7.0.128.4.0.255", "C.0.0"));    //PDR
        registerMapping.add(new CTRRegisterMapping("7.0.128.5.0.0", "2.3.6"));      //Tot_Vme_pf     (end of previous billing period)
        registerMapping.add(new CTRRegisterMapping("7.0.128.6.1.0", "2.3.A"));      //Tot_Vme_pf_f1  (alarm conditions are not documented in the blue book)
        registerMapping.add(new CTRRegisterMapping("7.0.128.6.2.0", "2.3.B"));      //Tot_Vme_pf_f2
        registerMapping.add(new CTRRegisterMapping("7.0.128.6.3.0", "2.3.C"));      //Tot_Vme_pf_f3
        registerMapping.add(new CTRRegisterMapping("7.0.128.8.0.255", "10.1.0"));   //number of elements

        registerMapping.add(new CTRRegisterMapping("7.0.13.2.1.255", "2.5.0"));     //Tot_Vcor_f1
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.2.255", "2.5.1"));     //Tot_Vcor_f2
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.3.255", "2.5.2"));     //Tot_Vcor_f3
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.1.0", "2.5.3"));       //Tot_Vpre_f1
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.2.0", "2.5.4"));       //Tot_Vpre_f2
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.3.0", "2.5.5"));       //Tot_Vpre_f3

        registerMapping.add(new CTRRegisterMapping(OBIS_DEVICE_STATUS, "12.0.0"));  //device status: status register 1
        registerMapping.add(new CTRRegisterMapping(OBIS_SEAL_STATUS, "D.9.0"));     //seal status: status register 2
        registerMapping.add(new CTRRegisterMapping(OBIS_DIAG, "12.1.0"));           //Diagn: status register 3
        registerMapping.add(new CTRRegisterMapping(OBIS_DIAG_REDUCED, "12.2.0"));   //DiagnR: status register 4

        registerMapping.add(new CTRRegisterMapping("0.0.96.12.5.255", "E.C.0"));    //gsm signal strength (deciBell)
        registerMapping.add(new CTRRegisterMapping("7.0.0.9.4.255", "8.1.2"));      //remaining shift in time
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.6.255", "F.5.0", 3));  //battery time remaining (hours)
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.0.255", "F.5.1", 3));  //battery hours used
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.3.255", "F.5.2", 3));  //battery voltage

        registerMapping.add(new CTRRegisterMapping("7.0.0.2.0.255", "9.0.3"));      //Equipment configuration code
        registerMapping.add(new CTRRegisterMapping("7.0.0.2.1.255", "9.0.4"));      //Firmware version
        registerMapping.add(new CTRRegisterMapping("7.0.0.2.2.255", "9.0.7"));      //Protocol version supported
        registerMapping.add(new CTRRegisterMapping(OBIS_EQUIPMENT_CLASS, "9.0.5")); //Equipment class

        registerMapping.add(new CTRRegisterMapping(OBIS_MTU_PHONE_NR, "E.2.1", 1)); // Device phone number
        registerMapping.add(new CTRRegisterMapping(OBIS_SMSC_NUMBER, "E.3.1", 1));  // SMS center number
        registerMapping.add(new CTRRegisterMapping(OBIS_HEAD_END_IP, "E.3.2", 1));  // IP destination address of head-end
        registerMapping.add(new CTRRegisterMapping(OBIS_MTU_IP_ADDRESS));           // Device IP address

        registerMapping.add(new CTRRegisterMapping("7.0.54.0.0.255", "B.1.0"));    // HCV
        registerMapping.add(new CTRRegisterMapping("7.0.0.12.19.255", "B.2.0"));   // LCV
        registerMapping.add(new CTRRegisterMapping("7.0.0.12.11.255", "B.2.6"));   // LCV attributed

        registerMapping.add(new CTRRegisterMapping("7.0.0.12.8.255", "4.9.1")); // P ref
        registerMapping.add(new CTRRegisterMapping("7.0.0.12.9.255", "7.B.1")); // T ref

        registerMapping.add(new CTRRegisterMapping(OBIS_Z_CALC_METHOD, "A.B.2"));      // Z calculation method
        registerMapping.add(new CTRRegisterMapping(OBIS_VOLUME_CALC_METHOD, "A.B.4")); // Volume calculation method

        registerMapping.add(new CTRRegisterMapping("7.0.45.0.0.255", "A.3.0")); // Density Gas
        registerMapping.add(new CTRRegisterMapping("7.0.49.0.0.255", "A.4.0")); // Density Air
        registerMapping.add(new CTRRegisterMapping("7.0.46.0.0.255", "A.5.0")); // Density Relative

        registerMapping.add(new CTRRegisterMapping("7.0.0.12.60.255", "A.6.0")); // N2%
        registerMapping.add(new CTRRegisterMapping("7.0.0.12.66.255", "A.7.0")); // CO2%
        registerMapping.add(new CTRRegisterMapping("7.0.0.12.61.255", "A.8.0")); // H2%
        registerMapping.add(new CTRRegisterMapping("7.0.0.12.65.255", "A.9.0")); // CO%

        registerMapping.add(new CTRRegisterMapping(OBIS_INSTALL_DATE));             // Installation date
        registerMapping.add(new CTRRegisterMapping(OBIS_DST_IN_USE, "8.2.0", 0));   // DST status

    }

    protected AbstractCTRObject getObjectFromList(List<AbstractCTRObject> ctrObjects, CTRObjectID objectID) {
        for (AbstractCTRObject each : ctrObjects) {
            if (each.getId().toString().equals(objectID.toString())) {
                return each;
            }
        }
        return null;
    }

    /**
     * Create a registerValue from the given value
     *
     * @param oc:     a given obsicode
     * @param regMap: the map linking obiscodes to CTR Object ID's
     * @param object: the CTR Object
     * @return the register value
     */
    protected RegisterValue getRegisterValue(ObisCode oc, CTRRegisterMapping regMap, AbstractCTRObject object) {
        CTRAbstractValue value = object.getValue()[regMap.getValueIndex()];

        if (isObis(oc, OBIS_DEVICE_STATUS)) {

            String description = DeviceStatus.fromStatusCode(value.getIntValue()).getDescription();
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), Unit.getUndefined());
            return new RegisterValue(oc, quantity, null, null, null, new Date(), 0, description);
        } else if (isObis(oc, OBIS_DIAG)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = Diagnostics.getDescriptionFromCode(value.getIntValue());
            return new RegisterValue(oc, quantity, null, null, null, cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_DIAG_REDUCED)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = Diagnostics.getDescriptionFromCode(value.getIntValue());
            return new RegisterValue(oc, quantity, null, null, null, cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_SEAL_STATUS)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = SealStatusBit.getIntegerSealsDescription(value.getIntValue());
            return new RegisterValue(oc, quantity, null, null, null, cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_EQUIPMENT_CLASS)) {
            return new RegisterValue(oc, EquipmentClassInfo.getEquipmentClass(value.getValue().toString()));
        } else if (isObis(oc, OBIS_MTU_PHONE_NR) || isObis(oc, OBIS_SMSC_NUMBER)) {
            byte[] phoneNumberArray = ProtocolTools.getSubArray(value.getBytes(), 0, 14);
            return new RegisterValue(oc, ProtocolTools.getAsciiFromBytes(phoneNumberArray, ' ').trim());
        } else if (isObis(oc, OBIS_Z_CALC_METHOD)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = ZCalculationMethod.fromMethodNr(value.getIntValue()).getDescription();
            return new RegisterValue(oc, quantity, null, null, null, cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_VOLUME_CALC_METHOD)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = VolumeCalculationMethod.fromMethodNr(value.getIntValue()).getDescription();
            return new RegisterValue(oc, quantity, null, null, null, cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_HEAD_END_IP)) {
            if (object.getValue(0).getIntValue() == 4) {    // Type = GPRS
                String ipAddress;
                byte[] ipAddressBytes;
                byte[] portBytes;

                if (!isEK155Protocol && firmwareBelow200()) {
                    ipAddressBytes = ProtocolTools.getSubArray(object.getBytes(), 4, 8);
                    portBytes = ProtocolTools.getSubArray(object.getBytes(), 8, 10);
                } else {
                    ipAddressBytes = ProtocolTools.getSubArray(object.getBytes(), 5, 9);
                    portBytes = ProtocolTools.getSubArray(object.getBytes(), 9, 11);
                }
                ipAddress = ProtocolTools.getIntFromByte(ipAddressBytes[0]) + "." +
                        ProtocolTools.getIntFromByte(ipAddressBytes[1]) + "." +
                        ProtocolTools.getIntFromByte(ipAddressBytes[2]) + "." +
                        ProtocolTools.getIntFromByte(ipAddressBytes[3]) + ":" +
                        ProtocolTools.getIntFromBytes(portBytes);
                return new RegisterValue(oc, ipAddress);
            } else {
                getLogger().info("Device is not in GPRS mode - unable to determine the head-end IP address.");
                return new RegisterValue(oc, "Unknown");
            }
        } else if (isObis(oc, OBIS_DATE_OF_CLOSURE_BILLING_PERIOD)) {
            int day = object.getValue(0).getIntValue();
            int period = object.getValue(1).getIntValue();
            int month = object.getValue(2).getIntValue();
            String info = "Day = " + day + " | Month = " + month + " | period = " + period;
            return new RegisterValue(oc, info);
        } else if (isObis(oc, OBIS_WAKE_UP_PARAMTERS)) {
            CTRAbstractValue[] values = object.getValue();
            String status = "On = " + values[0].getIntValue();
            status += " | Off = " + values[1].getIntValue();
            status += " | Monthly = 0x" + ProtocolTools.getHexStringFromBytes(values[2].getBytes(), "");
            byte[] weekdayBytes = values[3].getBytes();
            status += " | Daily = {Mode = 0x" + ProtocolTools.getHexStringFromInt(ProtocolTools.getIntFromByte(weekdayBytes[0]), 1, "");
            status += " | Working = " + ProtocolTools.getIntFromByte(weekdayBytes[1]) + ":" + ProtocolTools.getIntFromByte(weekdayBytes[2]);
            status += " | Holiday = " + ProtocolTools.getIntFromByte(weekdayBytes[3]) + ":" + ProtocolTools.getIntFromByte(weekdayBytes[4]) + "}";

            return new RegisterValue(oc, status);
        } else if (isObis(oc, OBIS_FREQUENCY_STRAGY_CLIENT_CONNECTION)) {
            CTRAbstractValue[] values = object.getValue();
            String status = "Delay = " + values[1].getIntValue();
            status += " | Method = 0x" + ProtocolTools.getHexStringFromInt(values[2].getIntValue(), 1, "");
            status += " | Retry = 0x" + ProtocolTools.getHexStringFromInt(values[3].getIntValue(), 1, "");

            return new RegisterValue(oc, status);
        } else if (isObis(oc, OBIS_IDENTIFIER_TARIFF_SCHEME)) {
            CTRAbstractValue[] values = object.getValue();
            String status = "Curr. TariffPlanId = 0x" + ProtocolTools.getHexStringFromInt(values[0].getIntValue(), 2, "");
            status += " | Prev. TariffPlanId = 0x" + ProtocolTools.getHexStringFromInt(values[1].getIntValue(), 2, "");
            status += " | Future. TariffPlanId = 0x" + ProtocolTools.getHexStringFromInt(values[2].getIntValue(), 2, "");
            if (values[0].getIntValue() != 0x0 && values[0].getIntValue() != 0xFFFF) {
                status += " | Future Activation = " + ProtocolTools.getIntFromByte(values[3].getBytes()[0]);
                status += "-" + ProtocolTools.getIntFromByte(values[3].getBytes()[1]);
                status += "-" + ProtocolTools.getIntFromByte(values[3].getBytes()[2]);
            } else {
                status += " | Future Activation = 2099-01-01";
            }

            return new RegisterValue(oc, status);
        } else if (isObis(oc, OBIS_VOLUNTARY_PARAMETERS_PROFILE_0)) {
            CTRAbstractValue[] values = object.getValue();
            String status = new String();
            for (int i = 0; i < 6; i++) {
                status += "#" + (i + 1) + " = {0x" + ProtocolTools.getHexStringFromInt(values[i].getBytes()[0], 1, "");
                status += " | 0x" + ProtocolTools.getHexStringFromBytes(ProtocolTools.getSubArray(values[i].getBytes(), 1, 3), "");
                status += " | 0x" + ProtocolTools.getHexStringFromInt(values[i].getBytes()[3], 1, "");
                status += (i < 5) ? " } | " : "}";
            }

            return new RegisterValue(oc, status);
        } else if (isObis(oc, OBIS_GASDAY_START_TIME)) {
            CTRAbstractValue[] values = object.getValue();
            String status = "Hour = " + values[0].getIntValue();
            status += " | UTC = " + values[1].getIntValue();
            return new RegisterValue(oc, status);
        } else {
            return readGenericRegisterValue(oc, object, value);
        }
    }

    /**
     * Read a register value with no specific structure or value. Only checks if the value is numeric or not.
     * If not, the object is stored as text in te registerValue;
     *
     * @param obisCode: a given obsicode
     * @param object:   the CTR Object
     * @param value:    the raw register value
     * @return the register value object
     */
    protected RegisterValue readGenericRegisterValue(ObisCode obisCode, AbstractCTRObject object, CTRAbstractValue value) {
        Object objectValue = value.getValue();
        if (objectValue instanceof BigDecimal) {
            Unit unit = value.getUnit();
            BigDecimal amount = (BigDecimal) objectValue;
            amount = amount.movePointRight(object.getQlf().getKmoltFactor());
            Quantity quantity = new Quantity(amount, unit);
            return new RegisterValue(obisCode, quantity);
        } else {
            return new RegisterValue(obisCode, objectValue.toString());
        }
    }

    /**
     * Try to get the matching registerMapping for a given obisCode
     *
     * @param obis: the given obiscode
     * @return a matching Object ID
     */
    public CTRRegisterMapping searchRegisterMapping(ObisCode obis) {
        for (CTRRegisterMapping ctrRegisterMapping : registerMapping) {
            if (obis.equals(ctrRegisterMapping.getObisCode())) {
                return ctrRegisterMapping;
            }
        }
        return null;
    }

    /**
     * Try to get the matching registerMapping for a given CTRObject
     *
     * @param objectId: the given CTRObjectID
     * @return a matching Object ID
     */
    public CTRRegisterMapping searchRegisterMapping(CTRObjectID objectId) {
        for (CTRRegisterMapping ctrRegisterMapping : registerMapping) {
            if (ctrRegisterMapping.getObjectId() != null && objectId.equals(ctrRegisterMapping.getObjectId())) {
                return ctrRegisterMapping;
            }
        }
        return null;
    }

    protected CollectedRegister createDeviceRegister(ObisCode obisCode) {
        return MdcManager.getCollectedDataFactory().createDefaultCollectedRegister(new RegisterDataIdentifierByObisCodeAndDevice(obisCode, getDeviceIdentifier()));
    }

    protected CollectedRegister createCollectedRegister(RegisterValue registerValue) {
        CollectedRegister deviceRegister = createDeviceRegister(registerValue.getObisCode());
        deviceRegister.setCollectedData(registerValue.getQuantity(), registerValue.getText());
        deviceRegister.setCollectedTimeStamps(registerValue.getReadTime(), registerValue.getFromTime(), registerValue.getToTime());
        return deviceRegister;
    }

    protected CollectedRegister createNotSupportedCollectedRegister(ObisCode obisCode) {
        CollectedRegister failedRegister = createDeviceRegister(obisCode);
        failedRegister.setFailureInformation(ResultType.NotSupported, MdcManager.getIssueFactory().createWarning(obisCode, "registerXnotsupported", obisCode));
        return failedRegister;
    }

    protected CollectedRegister createIncompatibleCollectedRegister(ObisCode obisCode, String message) {
        CollectedRegister failedRegister = createDeviceRegister(obisCode);
        failedRegister.setFailureInformation(ResultType.InCompatible, MdcManager.getIssueFactory().createWarning(obisCode, "registerXincompatible", obisCode, message));
        return failedRegister;
    }

    /**
     * Lazy getter for the logger
     *
     * @return: the logger
     */
    public Logger getLogger() {
        if (logger == null) {
            if (requestFactory == null) {
                logger = Logger.getLogger(getClass().getName());
            } else {
                logger = getRequestFactory().getLogger();
            }
        }
        return logger;
    }

    /**
     * Getter for the request factory
     *
     * @return the request factory
     */
    protected RequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * @param obisCodeToCheck:  the obiscode that needs to be checked
     * @param constantObisCode: a second obiscode to compare the first obiscode to
     * @return boolean
     */
    protected boolean isObis(ObisCode obisCodeToCheck, String constantObisCode) {
        return ObisCode.fromString(constantObisCode).equals(obisCodeToCheck);
    }

    /**
     * Test is the MTU155 firmware version is below 0200.
     *
     * @return true if the version is below 0200
     * false if the version is equal or higher than 0200
     */
    protected Boolean firmwareBelow200() {
        AbstractCTRObject vf = getRequestFactory().getIdentificationStructure().getVf();
        try {
            return (Integer.parseInt(vf.getValue(0).getStringValue().substring(3, 6)) < 200);
        } catch (NumberFormatException e) {
            return true;
        }
    }

    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }

    public void setDeviceIdentifier(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    public enum BillingPeriodClosureReason {
        DOES_NOT_EXIST,
        SWITCH_OF_VENDOR,
        CHANGE_OF_CONTRACT,
        CHANGE_OF_CONSUMER,
        SWITCH_OF_DISTRIBUTOR,
        END_OF_BILLING_PERIOD,
        START_OF_NEW_TARIFF_SCHEME,
        GENERIC,
        UNKNOWN;

        public static BillingPeriodClosureReason valueFromOrdinal(int ordinal) {
            for (BillingPeriodClosureReason indicator : values()) {
                if (indicator.ordinal() == ordinal) {
                    return indicator;
                }
            }
            return UNKNOWN;
        }

        public String getReason() {
            switch (this) {
                case DOES_NOT_EXIST:
                    return "Does not exist";
                case SWITCH_OF_VENDOR:
                    return "For switching of vendor";
                case CHANGE_OF_CONTRACT:
                    return "For change of contract";
                case CHANGE_OF_CONSUMER:
                    return "For change of end consumer (transfer)";
                case SWITCH_OF_DISTRIBUTOR:
                    return "For switching of distributor";
                case END_OF_BILLING_PERIOD:
                    return "For the end of the billing period";
                case START_OF_NEW_TARIFF_SCHEME:
                    return "For the start of a new tariff scheme";
                case GENERIC:
                    return "Generic";
                default:
                    return "Unknown";
            }
        }
    }
}