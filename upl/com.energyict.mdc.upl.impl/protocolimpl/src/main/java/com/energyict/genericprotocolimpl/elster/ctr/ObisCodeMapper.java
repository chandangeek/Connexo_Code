package com.energyict.genericprotocolimpl.elster.ctr;

import com.energyict.cbo.Quantity;
import com.energyict.genericprotocolimpl.elster.ctr.common.AttributeType;
import com.energyict.genericprotocolimpl.elster.ctr.common.Diagnostics;
import com.energyict.genericprotocolimpl.elster.ctr.exception.CTRException;
import com.energyict.genericprotocolimpl.elster.ctr.object.AbstractCTRObject;
import com.energyict.genericprotocolimpl.elster.ctr.object.CTRObjectID;
import com.energyict.genericprotocolimpl.elster.ctr.object.field.CTRAbstractValue;
import com.energyict.genericprotocolimpl.elster.ctr.structure.TableDECFQueryResponseStructure;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;

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

    private TableDECFQueryResponseStructure tableDECF;

    public ObisCodeMapper(GprsRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        initRegisterMapping();
    }

    private void initRegisterMapping() {

        //Daily readings = register values
        registerMapping.add(new CTRRegisterMapping("7.0.13.29.0.255", "1.3.3"));    //Vb_g
        registerMapping.add(new CTRRegisterMapping("7.0.41.0.0.255", "7.0.2"));     //T_h
        registerMapping.add(new CTRRegisterMapping("7.0.13.30.0.255", "1.1.3"));    //Vm_g
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.0.255", "2.0.3"));     //Tot_Vm_g
        registerMapping.add(new CTRRegisterMapping("7.0.13.2.0.255", "2.1.3"));     //Tot_Vb_g
        registerMapping.add(new CTRRegisterMapping("7.0.43.25.0.255", "1.A.3"));    //Qcb_max_g

        registerMapping.add(new CTRRegisterMapping("7.0.128.0.0.255", "12.6.3"));   //DiagnRS_g     = Manufacturer specific code!!  TODO: add in release notes
        registerMapping.add(new CTRRegisterMapping("7.0.128.1.0.255", "12.2.0"));   //DiagnR        = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.2.0.255", "12.1.0"));   //Diagn         = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.3.0.255", "2.3.3"));    //Tot_Vme_g     = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.4.0.255", "2.3.7"));    //Tot_Vme_f1    = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.5.0.255", "2.3.8"));    //Tot_Vme_f2    = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.6.0.255", "2.3.9"));    //Tot_Vme_f3    = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.7.0.255", "18.6.3"));   //Tot_Vme_f1_g  = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.8.0.255", "18.7.3"));   //Tot_Vme_f2_g  = Manufacturer specific code!!
        registerMapping.add(new CTRRegisterMapping("7.0.128.9.0.255", "18.8.3"));   //Tot_Vme_f3_g  = Manufacturer specific code!!

        registerMapping.add(new CTRRegisterMapping("7.0.13.0.1.255", "2.5.0"));     //Tot_Vcor_f1
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.2.255", "2.5.1"));     //Tot_Vcor_f2
        registerMapping.add(new CTRRegisterMapping("7.0.13.0.3.255", "2.5.2"));     //Tot_Vcor_f3

    }

    public GprsRequestFactory getRequestFactory() {
        return requestFactory;
    }

    public RegisterValue readRegister(ObisCode obisCode, List<AbstractCTRObject> list) throws NoSuchRegisterException, CTRException {
        AttributeType attributeType = new AttributeType();
        attributeType.setHasIdentifier(true);
        attributeType.setHasValueFields(true);
        attributeType.setHasQualifier(true);
        String id = null;
        CTRObjectID idObject = null;
        AbstractCTRObject object = null;
                                    
        for (CTRRegisterMapping ctrRegisterMapping : registerMapping) {
            if (obisCode.equals(ctrRegisterMapping.getObisCode())) {
                id = ctrRegisterMapping.getId();
                idObject = new CTRObjectID(id);
                break;
            }
        }

        if (id == null) {
            throw new NoSuchRegisterException("Unsupported Obis Code");
        }

        //If there's no pushed registers (SMS case), do a query for register data.
        //First check if the required register is in the tableDECF response
        if (list == null) {
            for (AbstractCTRObject ctrObject : getTableDECF().getObjects()) {
                if (ctrObject.getId().toString().equals(id)) {
                    object = ctrObject;
                    break;
                }
            }
            if (object == null) {               //If the object was not sent in the tableDECF response, query specifically for it
                list = getRequestFactory().queryRegisters(attributeType, id);
                if (list == null || list.size() == 0) {
                    throw new NoSuchRegisterException("Query for register with id: " + id.toString() + " failed. Meter response was empty");
                }
                object = list.get(0);
            }


        }

        //There's a given sms response containing data for several registers, find the right one
        else {
            for (AbstractCTRObject ctrObject : list) {
                if (id.equals(ctrObject.getId().toString())) {    //find the object in the sms response, that fits the obiscode
                    object = ctrObject;
                }
            }
        }

        if (object == null) {
            getLogger().log(Level.WARNING, "No suitable object available");
            throw new NoSuchRegisterException("No suitable object available");
        }
        
        RegisterValue regValue;
        Quantity quantity;

        if (object.getQlf().isInvalid()) {
            getLogger().log(Level.WARNING, "Invalid Data: Qualifier was 0xFF at register reading for ID: " + id.toString() + " (Obiscode: " + obisCode.toString() + ")");
            throw new NoSuchRegisterException("Invalid Data: Qualifier was 0xFF at register reading for ID: " + id.toString() + " (Obiscode: " + obisCode.toString() + ")");
        } else if (object.getQlf().isInvalidMeasurement()) {
            getLogger().log(Level.WARNING, "Invalid Measurement at register reading for ID: " + id.toString() + " (Obiscode: " + obisCode.toString() + ")");
            throw new NoSuchRegisterException("Invalid Measurement at register reading for ID: " + id.toString() + " (Obiscode: " + obisCode.toString() + ")");
        } else if (object.getQlf().isSubjectToMaintenance()) {
            getLogger().log(Level.WARNING, "Meter is subject to maintenance  at register reading for ID: " + id.toString() + " (Obiscode: " + obisCode.toString() + ")");
            throw new NoSuchRegisterException("Meter is subject to maintenance  at register reading for ID: " + id.toString() + " (Obiscode: " + obisCode.toString() + ")");
        } else {
            if (object.getValue().length == 1) {
                if (idObject.getX() == 0x12) { //In case of the diagnostics objects, map the justified bit to a description
                    CTRAbstractValue value = object.getValue()[0];
                    quantity = new Quantity((BigDecimal) value.getValue(), value.getUnit());
                    Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                    String description = Diagnostics.getDescriptionFromCode(value.getIntValue());
                    regValue = new RegisterValue(obisCode, quantity, cal.getTime(), cal.getTime(), cal.getTime(),cal.getTime(), 0, description);
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
        getLogger().log(Level.INFO, "Succesfully read register with ID: " + id.toString() + " and Obiscode: " + obisCode.toString());

        return regValue;
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(getClass().getName());
        }
        return logger;
    }

}
