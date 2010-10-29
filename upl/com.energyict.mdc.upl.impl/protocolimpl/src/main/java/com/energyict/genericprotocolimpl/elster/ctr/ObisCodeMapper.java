package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.Quantity;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.common.Diagnostics;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.Qualifier;
import com.energyict.genericprotocolimpl.elster.ctr.structure.TableDECFQueryResponseStructure;
import com.energyict.genericprotocolimpl.elster.ctr.structure.TableDECQueryResponseStructure;
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

    public TableDECFQueryResponseStructure getTableDECF() throws CTRException {
        if (tableDECF == null) {
            tableDECF = getRequestFactory().queryTableDECF();
        }
        return tableDECF;
    }

    public TableDECQueryResponseStructure getTableDEC() throws CTRException {
        if (tableDEC == null) {
            tableDEC = getRequestFactory().queryTableDEC();
        }
        return tableDEC;
    }

    private TableDECFQueryResponseStructure tableDECF;
    private TableDECQueryResponseStructure tableDEC;

    public ObisCodeMapper(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        initRegisterMapping();
    }

    private void initRegisterMapping() {

        registerMapping.add(new CTRRegisterMapping("7.0.13.26.0.0", "2.1.6"));      //Tot_Vb_pf   (end of previous billing period)
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.0.255", "2.0.0"));     //Tot_Vm
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.0.255", "2.1.0"));     //Tot_Vb
        registerMapping.add(new CTRRegisterMapping("7.0.128.1.0.255", "2.3.0"));     //Tot_Vme

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
        registerMapping.add(new CTRRegisterMapping("7.0.128.7.0.255", "2.3.0"));    //Tot_Vme
        registerMapping.add(new CTRRegisterMapping("7.0.128.8.0.255", "10.1.0"));   //number of elements

        registerMapping.add(new CTRRegisterMapping("7.0.13.2.1.255", "2.5.0"));     //Tot_Vcor_f1
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.2.255", "2.5.1"));     //Tot_Vcor_f2
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.3.255", "2.5.2"));     //Tot_Vcor_f3
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.1.0", "2.5.3"));       //Tot_Vpre_f1
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.2.0", "2.5.4"));       //Tot_Vpre_f2
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.3.0", "2.5.5"));       //Tot_Vpre_f3

        registerMapping.add(new CTRRegisterMapping("0.0.96.10.1.255", "12.0.0"));   //device status  : status register 1
        registerMapping.add(new CTRRegisterMapping("0.0.96.10.2.255", "D.9.0"));    //seal status    : status register 2
        registerMapping.add(new CTRRegisterMapping("0.0.96.10.3.255", "12.1.0"));   //Diagn          : status register 5
        registerMapping.add(new CTRRegisterMapping("0.0.96.10.4.255", "12.2.0"));   //DiagnR         : status register 4

        registerMapping.add(new CTRRegisterMapping("0.0.96.12.5.255", "E.C.0"));    //gsm signal strength (deciBell)
        registerMapping.add(new CTRRegisterMapping("7.0.0.9.4.255", "8.1.2"));      //remaining shift in time
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.6.255", "F.5.0"));     //battery time remaining (hours)
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.0.255", "F.5.1"));     //battery hours used
        registerMapping.add(new CTRRegisterMapping("0.0.96.6.3.255", "F.5.2"));     //battery voltage

    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public RegisterValue readRegister(ObisCode obisCode, List<AbstractCTRObject> list) throws NoSuchRegisterException, CTRException {
        ObisCode obis = ProtocolTools.setObisCodeField(obisCode, 1, (byte) 0x00);
        
        CTRObjectID idObject = null;

        for (CTRRegisterMapping ctrRegisterMapping : registerMapping) {
            if (obis.equals(ctrRegisterMapping.getObisCode())) {
                idObject = new CTRObjectID(ctrRegisterMapping.getId());
                break;
            }
        }

        if (idObject == null) {
            throw new NoSuchRegisterException("Unsupported Obis Code");
        }

        //If there's no pushed registers (SMS case), do a query for register data.
        //First check if the required register is in the tableDECF response
        AbstractCTRObject object = null;
        object = getObject(idObject, list);

        if (object == null) {
            getLogger().log(Level.WARNING, "No suitable object available");
            throw new NoSuchRegisterException("No suitable object available");
        }

        RegisterValue regValue;
        Quantity quantity;

        if (object.getQlf() == null) {
            object.setQlf(new Qualifier(0));
        }

        if (object.getQlf().isInvalid()) {
            getLogger().log(Level.WARNING, "Invalid Data: Qualifier was 0xFF at register reading for ID: " + idObject.toString() + " (Obiscode: " + obisCode.toString() + ")");
            throw new NoSuchRegisterException("Invalid Data: Qualifier was 0xFF at register reading for ID: " + idObject.toString() + " (Obiscode: " + obisCode.toString() + ")");
        } else if (object.getQlf().isInvalidMeasurement()) {
            getLogger().log(Level.WARNING, "Invalid Measurement at register reading for ID: " + idObject.toString() + " (Obiscode: " + obisCode.toString() + ")");
            throw new NoSuchRegisterException("Invalid Measurement at register reading for ID: " + idObject.toString() + " (Obiscode: " + obisCode.toString() + ")");
        } else if (object.getQlf().isSubjectToMaintenance()) {
            getLogger().log(Level.WARNING, "Meter is subject to maintenance  at register reading for ID: " + idObject.toString() + " (Obiscode: " + obisCode.toString() + ")");
            throw new NoSuchRegisterException("Meter is subject to maintenance  at register reading for ID: " + idObject.toString() + " (Obiscode: " + obisCode.toString() + ")");
        } else {
            if (object.getValue().length == 1) {
                if (idObject.getX() == 0x12) { //In case of the diagnostics objects, map the justified bit to a description
                    CTRAbstractValue value = object.getValue()[0];
                    quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                    String description = Diagnostics.getDescriptionFromCode(value.getIntValue());
                    regValue = new RegisterValue(obisCode, quantity, cal.getTime(), cal.getTime(), cal.getTime(), cal.getTime(), 0, description);
                } else {
                    CTRAbstractValue value = object.getValue()[0];
                    quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
                    regValue = new RegisterValue(obisCode, quantity);
                }

            } else {          //When there's multiple value fields: only case is Qcb_max_g
                Calendar cal = Calendar.getInstance();
                CTRAbstractValue value1 = object.getValue()[0];
                CTRAbstractValue value2 = object.getValue()[1];
                CTRAbstractValue value3 = object.getValue()[2];
                int hours = value2.getIntValue();
                int minutes = value3.getIntValue();
                quantity = new Quantity((BigDecimal) value1.getValue(), value1.getUnit());
                cal.set(Calendar.HOUR_OF_DAY, hours);
                cal.set(Calendar.MINUTE, minutes);
                Date date = cal.getTime();
                regValue = new RegisterValue(obisCode, quantity, date);
            }
        }
/*
        getLogger().log(Level.INFO, "Succesfully read register with ID: " + idObject.toString() + " and Obiscode: " + obisCode.toString());
*/

        return regValue;
    }

    private AbstractCTRObject getObjectFromSMSList(CTRObjectID idObject, List<AbstractCTRObject> list) {
        for (AbstractCTRObject ctrObject : list) {
            if (idObject.toString().equals(ctrObject.getId().toString())) {    //find the object in the sms response, that fits the obiscode
                return ctrObject;
            }
        }
        return null;
    }

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

    private AbstractCTRObject getObjectFromRegisterRequest(CTRObjectID idObject) throws CTRException, NoSuchRegisterException {
        List<AbstractCTRObject> list;
        AbstractCTRObject object;
        AttributeType attributeType = new AttributeType();
        attributeType.setHasIdentifier(true);
        attributeType.setHasValueFields(true);
        attributeType.setHasQualifier(true);
        list = getRequestFactory().queryRegisters(attributeType, idObject);
        if (list == null || list.size() == 0) {
            throw new NoSuchRegisterException("Query for register with id: " + idObject.toString() + " failed. Meter response was empty");
        }
        object = list.get(0);
        return object;
    }

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

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

}
