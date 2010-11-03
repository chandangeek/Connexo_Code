package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.info.*;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.TableDECFQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.TableDECQueryResponseStructure;
import com.energyict.genericprotocolimpl.webrtuz3.MeterAmrLogging;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.math.BigDecimal;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 13-okt-2010
 * Time: 10:20:17
 */
public class ObisCodeMapper {

    private Logger logger;
    private List<CTRRegisterMapping> registerMapping = new ArrayList<CTRRegisterMapping>();
    private final GprsRequestFactory requestFactory;
    private MeterAmrLogging meterAmrLogging;
                              
    private TableDECFQueryResponseStructure tableDECF;
    private TableDECQueryResponseStructure tableDEC;
    private static final String OBIS_DEVICE_STATUS = "0.0.96.10.1.255";
    private static final String OBIS_SEAL_STATUS = "0.0.96.10.2.255";
    private static final String OBIS_DIAG = "0.0.96.10.3.255";
    private static final String OBIS_DIAG_REDUCED = "0.0.96.10.4.255";

    public ObisCodeMapper(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        initRegisterMapping();
    }

    private void initRegisterMapping() {

        registerMapping.add(new CTRRegisterMapping("7.0.13.26.0.0", "2.1.6"));      //Tot_Vb_pf   (end of previous billing period)
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.0.255", "2.0.0"));     //Tot_Vm
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.0.255", "2.1.0"));     //Tot_Vb
        registerMapping.add(new CTRRegisterMapping("7.0.128.1.0.255", "2.3.0"));    //Tot_Vme

        registerMapping.add(new CTRRegisterMapping("7.0.43.0.0.255", "1.0.0"));     //Qm
        registerMapping.add(new CTRRegisterMapping("7.0.43.1.0.255", "1.2.0"));     //Qb
        registerMapping.add(new CTRRegisterMapping("7.0.42.0.0.255", "4.0.0"));     //P
        registerMapping.add(new CTRRegisterMapping("7.0.41.0.0.255", "7.0.0"));     //T
        registerMapping.add(new CTRRegisterMapping("7.0.52.0.0.255", "A.0.0"));     //C, conversion factor
        registerMapping.add(new CTRRegisterMapping("7.0.53.0.0.255", "A.1.6"));     //Z, compressibility

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
        registerMapping.add(new CTRRegisterMapping(OBIS_DIAG, "12.1.0"));           //Diagn: status register 5
        registerMapping.add(new CTRRegisterMapping(OBIS_DIAG_REDUCED, "12.2.0"));   //DiagnR: status register 4

        registerMapping.add(new CTRRegisterMapping("0.0.96.12.5.255", "E.C.0"));    //gsm signal strength (deciBell)
        registerMapping.add(new CTRRegisterMapping("7.0.0.9.4.255", "8.1.2"));      //remaining shift in time
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.6.255", "F.5.0", 3));  //battery time remaining (hours)
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.0.255", "F.5.1", 3));  //battery hours used
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.3.255", "F.5.2", 3));  //battery voltage

    }

    public MeterAmrLogging getMeterAmrLogging() {
        if (meterAmrLogging == null) {
            meterAmrLogging = new MeterAmrLogging();
        }
        return meterAmrLogging;
    }

    /**
     * Read the register from the device with a given obisCode.
     *
     * @param obisCode
     * @return
     * @throws NoSuchRegisterException
     * @throws CTRException
     */
    public RegisterValue readRegister(ObisCode obisCode) throws NoSuchRegisterException, CTRException {
        return readRegister(obisCode, null);
    }

    /**
     * Read the register from the device with a given obisCode.
     * Use the list of objects received from sms if smsObjects != null
     *
     * @param obisCode
     * @param smsObjects
     * @return
     * @throws NoSuchRegisterException
     * @throws CTRException
     */
    public RegisterValue readRegister(ObisCode obisCode, List<AbstractCTRObject> smsObjects) throws NoSuchRegisterException, CTRException {
        ObisCode obis = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0x00);

        CTRRegisterMapping regMap = searchRegisterMapping(obis);
        if (regMap == null) {
            String message = "Unsupported Obis Code";
            getMeterAmrLogging().logRegisterFailure(message, obisCode);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        }

        AbstractCTRObject object = getObject(regMap.getObjectId(), smsObjects);
        if (object == null) {
            String message = "No suitable object available in received data";
            getMeterAmrLogging().logRegisterFailure(message, obisCode);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        }

        if (object.getQlf() == null) {
            object.setQlf(new Qualifier(0));
        }

        if (object.getQlf().isInvalid()) {
            String message = "Invalid Data: Qualifier was 0xFF at register reading for ID: " + regMap.getId() + " (Obiscode: " + obisCode.toString() + ")";
            getMeterAmrLogging().logRegisterFailure(message, obisCode);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        } else if (object.getQlf().isInvalidMeasurement()) {
            String message = "Invalid Measurement at register reading for ID: " + regMap.getId() + " (Obiscode: " + obisCode.toString() + ")";
            getMeterAmrLogging().logRegisterFailure(message, obisCode);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        } else if (object.getQlf().isSubjectToMaintenance()) {
            String message = "Meter is subject to maintenance at register reading for ID: " + regMap.getId() + " (Obiscode: " + obisCode.toString() + ")";
            getMeterAmrLogging().logRegisterFailure(message, obisCode);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        } else if (object.getQlf().isReservedVal()) {
            String message = "Qualifier is 'Reserved' at register reading for ID: " + regMap.getId() + " (Obiscode: " + obisCode.toString() + ")";
            getMeterAmrLogging().logRegisterFailure(message, obisCode);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        }

        return ProtocolTools.setRegisterValueObisCode(getRegisterValue(obis, regMap, object), obisCode);

    }

    /**
     * Create a registerValue from the given value
     *
     * @param oc
     * @param regMap
     * @param object
     * @return
     */
    private RegisterValue getRegisterValue(ObisCode oc, CTRRegisterMapping regMap, AbstractCTRObject object) {
        CTRAbstractValue value = object.getValue()[regMap.getValueIndex()];

        if (isObis(oc, OBIS_DEVICE_STATUS)) {
            String description = DeviceStatus.fromStatusCode(value.getIntValue()).getDescription();
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), Unit.getUndefined());
            return new RegisterValue(oc, quantity, new Date(), new Date(), null, new Date(), 0, description);
        } else if (isObis(oc, OBIS_DIAG)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = Diagnostics.getDescriptionFromCode(value.getIntValue());
            return new RegisterValue(oc, quantity, cal.getTime(), cal.getTime(), cal.getTime(), cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_DIAG_REDUCED)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = Diagnostics.getDescriptionFromCode(value.getIntValue());
            return new RegisterValue(oc, quantity, cal.getTime(), cal.getTime(), cal.getTime(), cal.getTime(), 0, description);
        } else if (isObis(oc, OBIS_SEAL_STATUS)) {
            Quantity quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            String description = SealStatusBit.getBrokenSealsDescription(value.getIntValue());
            return new RegisterValue(oc, quantity, cal.getTime(), cal.getTime(), cal.getTime(), cal.getTime(), 0, description);
        } else {
            return readGenericRegisterValue(oc, object, value);
        }

    }

    /**
     * Read a register value with no specific structure or value. Only checks if the value is numeric or not.
     * If not, the object is stored as text in te registerValue;
     *
     * @param obisCode
     * @param object
     * @param value
     * @return
     */
    private RegisterValue readGenericRegisterValue(ObisCode obisCode, AbstractCTRObject object, CTRAbstractValue value) {
        Object objectValue = value.getValue();
        if (objectValue instanceof BigDecimal) {
            Unit unit = value.getUnit();
            Quantity quantity = new Quantity((BigDecimal) objectValue, unit.getDlmsCode(), object.getQlf().getKmoltFactor());
            return new RegisterValue(obisCode, quantity);
        } else {
            return new RegisterValue(obisCode, objectValue.toString());
        }
    }

    /**
     * Try to get the matching registerMapping for a given obisCode
     *
     * @param obis
     * @return
     */
    private CTRRegisterMapping searchRegisterMapping(ObisCode obis) {
        for (CTRRegisterMapping ctrRegisterMapping : registerMapping) {
            if (obis.equals(ctrRegisterMapping.getObisCode())) {
                return ctrRegisterMapping;
            }
        }
        return null;
    }

    /**
     * Try to get the requested object from different sources
     * (DEC, DECF, SMS object list or registerQuery).
     *
     * @param idObject
     * @param list
     * @return
     * @throws CTRException
     * @throws NoSuchRegisterException
     */
    private AbstractCTRObject getObject(CTRObjectID idObject, List<AbstractCTRObject> list) throws CTRException, NoSuchRegisterException {
        AbstractCTRObject object = null;
        if (list == null) {
            if (object == null) {
                object = getObjectFromDECFTable(idObject);
            }
            if (object == null) {
                object = getObjectFromDECTable(idObject);
            }
            if (object == null) {
                object = getObjectFromRegisterRequest(idObject); //If the object was not sent in the tableDECF response, query specifically for it
            }
        } else { //There's a given sms response containing data for several registers, find the right one
            object = getObjectFromSMSList(idObject, list);
        }

        return object;
    }

    /**
     * Read the requested object from the device using a registerQuery request
     *
     * @param idObject
     * @return
     * @throws CTRException
     * @throws NoSuchRegisterException
     */
    private AbstractCTRObject getObjectFromRegisterRequest(CTRObjectID idObject) throws CTRException, NoSuchRegisterException {
        List<AbstractCTRObject> list;
        AbstractCTRObject object;
        AttributeType attributeType = new AttributeType();
        attributeType.setHasIdentifier(true);
        attributeType.setHasValueFields(true);
        attributeType.setHasQualifier(true);
        list = getRequestFactory().queryRegisters(attributeType, idObject);
        if (list == null || list.size() == 0) {
            String message = "Query for register with id: " + idObject.toString() + " failed. Meter response was empty";
            getMeterAmrLogging().logInfo(message);
            getLogger().log(Level.WARNING, message);
            throw new NoSuchRegisterException(message);
        }
        object = list.get(0);
        return object;
    }

    /**
     * Check if the requested object is in the DECF table, and read it if it is
     *
     * @param objectId
     * @return
     * @throws CTRException
     */
    private AbstractCTRObject getObjectFromDECFTable(CTRObjectID objectId) throws CTRException {
        if (TableDECFQueryResponseStructure.containsObjectId(objectId)) {
            for (AbstractCTRObject ctrObject : getTableDECF().getObjects()) {
                if (ctrObject.getId().toString().equals(objectId.toString())) {
                    return ctrObject;
                }
            }
        }
        return null;
    }

    /**
     * Check if the requested object is in the DEC table, and read it if it is
     *
     * @param objectId
     * @return
     * @throws CTRException
     */
    private AbstractCTRObject getObjectFromDECTable(CTRObjectID objectId) throws CTRException {
        if (TableDECQueryResponseStructure.containsObjectId(objectId)) {
            for (AbstractCTRObject ctrObject : getTableDEC().getObjects()) {
                if (ctrObject.getId().toString().equals(objectId.toString())) {
                    return ctrObject;
                }
            }
        }
        return null;
    }

    /**
     * Get the object from a given list of objects, received using SMS
     *
     * @param idObject
     * @param list
     * @return
     */
    private AbstractCTRObject getObjectFromSMSList(CTRObjectID idObject, List<AbstractCTRObject> list) {
        for (AbstractCTRObject ctrObject : list) {
            if (idObject.toString().equals(ctrObject.getId().toString())) {    //find the object in the sms response, that fits the obiscode
                return ctrObject;
            }
        }
        return null;
    }

    /**
     * Lazy getter for the logger
     *
     * @return
     */
    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

    /**
     * Return the cached DECF table, or read it from the device
     *
     * @return
     * @throws CTRException
     */
    public TableDECFQueryResponseStructure getTableDECF() throws CTRException {
        if (tableDECF == null) {
            tableDECF = getRequestFactory().queryTableDECF();
        }
        return tableDECF;
    }

    /**
     * Return the cached DEC table, or read it from the device
     *
     * @return
     * @throws CTRException
     */
    public TableDECQueryResponseStructure getTableDEC() throws CTRException {
        if (tableDEC == null) {
            tableDEC = getRequestFactory().queryTableDEC();
        }
        return tableDEC;
    }

    /**
     * Getter for the register factory
     *
     * @return
     */
    private GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    /**
     * @param obisCodeToCheck
     * @param constantObisCode
     * @return
     */
    private boolean isObis(ObisCode obisCodeToCheck, String constantObisCode) {
        return ObisCode.fromString(constantObisCode).equals(obisCodeToCheck);
    }

}
