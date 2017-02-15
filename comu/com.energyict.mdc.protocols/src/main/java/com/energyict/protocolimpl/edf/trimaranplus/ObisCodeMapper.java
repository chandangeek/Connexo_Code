/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ObisCodeMapper.java
 *
 * Created on 5 maart 2007, 16:45
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaranplus;

import com.energyict.mdc.common.ObisCode;
import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.protocolimpl.edf.trimarandlms.common.Register;
import com.energyict.protocolimpl.edf.trimarandlms.common.RegisterNameFactory;
import com.energyict.protocolimpl.edf.trimaranplus.core.DepassementQuadratique;
import com.energyict.protocolimpl.edf.trimaranplus.core.DureeDepassement;
import com.energyict.protocolimpl.edf.trimaranplus.core.Energie;
import com.energyict.protocolimpl.edf.trimaranplus.core.ParametresP;
import com.energyict.protocolimpl.edf.trimaranplus.core.Pmax;
import com.energyict.protocolimpl.edf.trimaranplus.core.TempsFonctionnement;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ObisCodeMapper {

    TrimaranPlus trimaranPlus;

    /** Creates a new instance of ObisCodeMapper */
    public ObisCodeMapper(TrimaranPlus trimaranPlus) {
        this.trimaranPlus=trimaranPlus;
    }

    public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        return new RegisterInfo(RegisterNameFactory.findObisCode(obisCode));
    }

    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

        Register register = trimaranPlus.getRegisterFactory().findRegister(obisCode);

        // cumul register, no from time and to time only for previous month
        if (register.getVariableName().isENERGIE()) {
            Energie energie = trimaranPlus.getTrimaranObjectFactory().readEnergieIndex().getEnergie(register.getVariableName().getCode());
            if (register.getObisCode().getF() == 255) {
                return new RegisterValue(obisCode,energie.getIndexEnergie()[register.getIndex()]);
            }
            else {
                return new RegisterValue(obisCode,energie.getIndexEnergie()[register.getIndex()],null,energie.getDateFinPeriode().getCalendar().getTime());
            }
        }

        // minuten dat tarief actief is
        if (register.getVariableName().isTEMPS_FONCTIONNEMENT()) {
            TempsFonctionnement tempsFonctionnement = trimaranPlus.getTrimaranObjectFactory().readTempsFonctionnementValues().getTempsFonctionnement(register.getVariableName().getCode());
            if (register.getObisCode().getF() == 255) {
                return new RegisterValue(obisCode,tempsFonctionnement.getValueTempsFonctionnement()[register.getIndex()],null,tempsFonctionnement.getDateDebutPeriode().getCalendar().getTime());
            }
            else {
                return new RegisterValue(obisCode,tempsFonctionnement.getValueTempsFonctionnement()[register.getIndex()],null,tempsFonctionnement.getDateDebutPeriode().getCalendar().getTime(),tempsFonctionnement.getDateFinPeriode().getCalendar().getTime());
            }
        }

        // depassement quadratique et energie de depassement
        if (register.getVariableName().isDEPASSEMENT_QUADRATIUQUE()) {
            if (register.getVariableName().getCode()<1000) {
                DepassementQuadratique depassementQuadratique = trimaranPlus.getTrimaranObjectFactory().readDepassementQuadratiqueValues().getDepassementQuadratique(register.getVariableName().getCode());
                if (register.getObisCode().getF() == 255) {
                    return new RegisterValue(obisCode,depassementQuadratique.getValueDepassementQuadratique()[register.getIndex()],null,depassementQuadratique.getDateDebutPeriode().getCalendar().getTime());
                }
                else {
                    return new RegisterValue(obisCode,depassementQuadratique.getValueDepassementQuadratique()[register.getIndex()],null,depassementQuadratique.getDateDebutPeriode().getCalendar().getTime(),depassementQuadratique.getDateFinPeriode().getCalendar().getTime());
                }
            }
            else if (register.getVariableName().getCode()>=1000) {
                DepassementQuadratique depassementQuadratique = trimaranPlus.getTrimaranObjectFactory().readDepassementQuadratiqueValues().getDepassementQuadratique(register.getVariableName().getCode()-1000);
                if (register.getObisCode().getF() == 255) {
                    return new RegisterValue(obisCode,depassementQuadratique.getEnergiesDepassement(),null,depassementQuadratique.getDateDebutPeriode().getCalendar().getTime());
                }
                else {
                    return new RegisterValue(obisCode,depassementQuadratique.getEnergiesDepassement(),null,depassementQuadratique.getDateDebutPeriode().getCalendar().getTime(),depassementQuadratique.getDateFinPeriode().getCalendar().getTime());
                }
            }
        }

        // minuten dat tarief actief is
        if (register.getVariableName().isDUREE_DEPASSEMENT()) {
            DureeDepassement dureeDepassement = trimaranPlus.getTrimaranObjectFactory().readDureeDepassementValues().getDureeDepassement(register.getVariableName().getCode());
            if (register.getObisCode().getF() == 255) {
                return new RegisterValue(obisCode,dureeDepassement.getValueDureeDepassement()[register.getIndex()],null,dureeDepassement.getDateDebutPeriode().getCalendar().getTime());
            }
            else {
                return new RegisterValue(obisCode,dureeDepassement.getValueDureeDepassement()[register.getIndex()],null,dureeDepassement.getDateDebutPeriode().getCalendar().getTime(),dureeDepassement.getDateFinPeriode().getCalendar().getTime());
            }
        }

        // minuten dat tarief actief is
        if (register.getVariableName().isPMAX()) {
            Pmax pmax = trimaranPlus.getTrimaranObjectFactory().readPmaxValues().getPmax(register.getVariableName().getCode());
            if (register.getObisCode().getF() == 255) {
                return new RegisterValue(obisCode,pmax.getValuePmax()[register.getIndex()],null,pmax.getDateDebutPeriode().getCalendar().getTime());
            }
            else {
                return new RegisterValue(obisCode,pmax.getValuePmax()[register.getIndex()],null,pmax.getDateDebutPeriode().getCalendar().getTime(),pmax.getDateFinPeriode().getCalendar().getTime());
            }
        }

        if (register.getVariableName().isABSTRACT()) {
            if (register.getVariableName().getCode() == 48) {
                ParametresP param = trimaranPlus.getTrimaranObjectFactory().readParametresP();
                if (register.getObisCode().getF() == 255) {
                    return new RegisterValue(obisCode,param.getPS()[register.getIndex()],null,param.getDateDebutPeriode().getCalendar().getTime());
                }
                else {
                    return new RegisterValue(obisCode,param.getPS()[register.getIndex()],null,param.getDateDebutPeriode().getCalendar().getTime(),param.getDateFinPeriode().getCalendar().getTime());
                }
            }
            else if (register.getVariableName().getCode() == 168) {
                ParametresP param = trimaranPlus.getTrimaranObjectFactory().readParametresPmoins1();
                if (register.getObisCode().getF() == 255) {
                    return new RegisterValue(obisCode,param.getPS()[register.getIndex()],null,param.getDateDebutPeriode().getCalendar().getTime());
                }
                else {
                    return new RegisterValue(obisCode,param.getPS()[register.getIndex()],null,param.getDateDebutPeriode().getCalendar().getTime(),param.getDateFinPeriode().getCalendar().getTime());
                }

            }
        }


        throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");

    } // private Object getRegisterValue(ObisCode obisCode, boolean read)

}
