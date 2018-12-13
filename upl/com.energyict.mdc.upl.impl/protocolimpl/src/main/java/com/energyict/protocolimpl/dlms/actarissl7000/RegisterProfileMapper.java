/*
 * RegisterProfileMapper.java
 *
 * Created on 13 oktober 2004, 17:24
 */

package com.energyict.protocolimpl.dlms.actarissl7000;

import com.energyict.dlms.DataContainer;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.cosem.*;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.dlms.DLMSLNSL7000;

import java.io.IOException;
import java.util.*;
/**
 *
 * @author  Koen
 */
public class RegisterProfileMapper {

    final ObisCode ALLDEMANDS_PROFILE=ObisCode.fromString("0.0.98.133.5.255");
    final ObisCode ALLMAXIMUMDEMANDS_PROFILE=ObisCode.fromString("0.0.98.133.6.255");
    final ObisCode ALLCUMULATIVEMAXDEMANDS_PROFILE=ObisCode.fromString("0.0.98.133.90.255");
    final ObisCode ALLTOTALENERGIES_PROFILE=ObisCode.fromString("255.255.98.133.2.255");
    final ObisCode ALLENERGYRATES_PROFILE=ObisCode.fromString("255.255.98.133.1.255");

    CosemObjectFactory cof;
    ObisCodeRelation maximumDemandRelations = null; // cached

    DataContainer dcMaximumDemand=null; // cached
    //DataContainer dcCumulativeMaximumDemand=null; // cached
    DataContainer dcDemand=null; // cached
    DataContainer dcTotalEnergies=null;  // cached
    DataContainer dcEnergyRates=null;  // cached

    DLMSLNSL7000 meterProtocol;

    /** Creates a new instance of RegisterProfileMapper */
    public RegisterProfileMapper(CosemObjectFactory cof, DLMSLNSL7000 meterProtocol) {
        this.cof=cof;
        this.meterProtocol = meterProtocol;
    }

    public ObisCode getProfileObisCode(ObisCode obisCode) throws IOException {
        ObisCode profileObisCode = obisCode;
        if (obisCode.getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
            profileObisCode = getMaximumDemandRelations().getProfileObisCode(obisCode);
        }
        else if (obisCode.getD() == ObisCode.CODE_D_RISING_DEMAND) {
            profileObisCode = ALLDEMANDS_PROFILE;
        }
        else if (obisCode.getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) {
            profileObisCode = ALLCUMULATIVEMAXDEMANDS_PROFILE;
        }
        return null;
    }

    public ObisCode getMDProfileObisCode(ObisCode obisCode) throws IOException {
        ObisCode profileObisCode = obisCode;
        if (obisCode.getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
            int fieldF = obisCode.getF();
            // KV 02092005 bugfix to read historical maxdemand values
            profileObisCode=new ObisCode(obisCode.getA(),obisCode.getB(),obisCode.getC(),obisCode.getD(),obisCode.getE(),255);
            profileObisCode = getMaximumDemandRelations().getProfileObisCode(profileObisCode);
            profileObisCode=new ObisCode(profileObisCode.getA(),profileObisCode.getB(),profileObisCode.getC(),profileObisCode.getD(),profileObisCode.getE(),fieldF,fieldF<=0?true:false);
        }
        return profileObisCode;
    }

    public CosemObject getRegister(ObisCode obisCode) throws IOException {
        if (obisCode.getD() == ObisCode.CODE_D_MAXIMUM_DEMAND) {
            int channelIndex = obisCode.getB()-1; // save B field
            obisCode = new ObisCode(obisCode,1); // reset B field
            ObisCode profileObisCode = getMaximumDemandRelations().getProfileObisCode(obisCode);
            if (dcMaximumDemand==null) {
				dcMaximumDemand = cof.getProfileGeneric(profileObisCode).getBuffer();
			}
            if (channelIndex < dcMaximumDemand.getRoot().getNrOfElements()) {
                int value = dcMaximumDemand.getRoot().getStructure(channelIndex).getInteger(0);
                ScalerUnit scalerUnit = new ScalerUnit(dcMaximumDemand.getRoot().getStructure(channelIndex).getStructure(1).getInteger(0),dcMaximumDemand.getRoot().getStructure(channelIndex).getStructure(1).getInteger(1));
                Date date = dcMaximumDemand.getRoot().getStructure(channelIndex).getOctetString(2).toDate(meterProtocol.getTimeZone());
                ExtendedRegister extendedRegister = new ExtendedRegister(cof.getProtocolLink(),new ObjectReference(obisCode.getLN(),DLMSClassId.PROFILE_GENERIC.getClassId()));
                extendedRegister.setValue(new Long(value));
                extendedRegister.setCaptureTime(date);
                extendedRegister.setScalerUnit(scalerUnit);
                return extendedRegister;
            }
        }
        else if ((obisCode.getD() == ObisCode.CODE_D_RISING_DEMAND) || (obisCode.getD() == ObisCode.CODE_D_LAST_AVERAGE)) {
            if (dcDemand==null) {
				dcDemand = cof.getProfileGeneric(ALLDEMANDS_PROFILE).getBuffer();
			}
            for (int i=0;i<dcDemand.getRoot().getStructure(0).getNrOfElements();i++) {
                if (dcDemand.getRoot().getStructure(0).getStructure(i).getOctetString(0).toObisCode().equals(obisCode)) {
                    int value = dcDemand.getRoot().getStructure(0).getStructure(i).getInteger(1);
                    ScalerUnit scalerUnit = new ScalerUnit(dcDemand.getRoot().getStructure(0).getStructure(i).getStructure(2).getInteger(0),dcDemand.getRoot().getStructure(0).getStructure(i).getStructure(2).getInteger(1));
                    Register register = new Register(cof.getProtocolLink(),new ObjectReference(obisCode.getLN(),DLMSClassId.PROFILE_GENERIC.getClassId()));
                    register.setValue(new Long(value));
                    register.setScalerUnit(scalerUnit);
                    return register;
                }
            }
        }
        else if (obisCode.getD() == ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND) {
            // Requesting the buffer of obis CODE_D_CUMULATIVE_MAXUMUM_DEMAND seems not to work! It results in NULL data...
            /*
            DataContainer dc = cof.getProfileGeneric(ALLCUMULATIVEMAXDEMANDS_PROFILE).getBuffer();
            for (int i=0;i<dc.getRoot().getStructure(0).getNrOfElements();i++) {
                if (dc.getRoot().getStructure(0).getStructure(i).getOctetString(0).toObisCode().equals(obisCode)) {
                    int value = dc.getRoot().getStructure(0).getStructure(i).getInteger(0);
                    ScalerUnit scalerUnit = new ScalerUnit(dc.getRoot().getStructure(0).getStructure(i).getStructure(1).getInteger(0),dc.getRoot().getStructure(0).getStructure(i).getStructure(1).getInteger(1));
                    Register register = cof.getRegister(obisCode);
                    register.setValue(new Integer(value));
                    register.setScalerUnit(scalerUnit);
                    return register;
                }
            }
             */
        }
        else if ((obisCode.getD() == ObisCode.CODE_D_TIME_INTEGRAL) && (obisCode.getE()==0)) {
            if (dcEnergyRates==null) {
				dcEnergyRates = cof.getProfileGeneric(ALLTOTALENERGIES_PROFILE).getBuffer();
			}
            for (int i=0;i<dcEnergyRates.getRoot().getStructure(0).getNrOfElements();i++) {
                if (dcEnergyRates.getRoot().getStructure(0).getStructure(i).getOctetString(0).toObisCode().equals(obisCode)) {
                    int value = dcEnergyRates.getRoot().getStructure(0).getStructure(i).getInteger(1);
                    ScalerUnit scalerUnit = new ScalerUnit(dcEnergyRates.getRoot().getStructure(0).getStructure(i).getStructure(2).getInteger(0),dcEnergyRates.getRoot().getStructure(0).getStructure(i).getStructure(2).getInteger(1));
                    Register register = new Register(cof.getProtocolLink(),new ObjectReference(obisCode.getLN(),DLMSClassId.PROFILE_GENERIC.getClassId()));
                    register.setValue(new Long(value));
                    register.setScalerUnit(scalerUnit);
                    return register;
                }
            }
        }
        else if ((obisCode.getD() == ObisCode.CODE_D_TIME_INTEGRAL) && (obisCode.getE()!=0)) {
            if (dcTotalEnergies==null) {
				dcTotalEnergies = cof.getProfileGeneric(ALLENERGYRATES_PROFILE).getBuffer();
			}
            for (int i=0;i<dcTotalEnergies.getRoot().getStructure(0).getNrOfElements();i++) {
                if (dcTotalEnergies.getRoot().getStructure(0).getStructure(i).getOctetString(0).toObisCode().equals(obisCode)) {
                    int value = dcTotalEnergies.getRoot().getStructure(0).getStructure(i).getInteger(1);
                    ScalerUnit scalerUnit = new ScalerUnit(dcTotalEnergies.getRoot().getStructure(0).getStructure(i).getStructure(2).getInteger(0),dcTotalEnergies.getRoot().getStructure(0).getStructure(i).getStructure(2).getInteger(1));
                    Register register = new Register(cof.getProtocolLink(),new ObjectReference(obisCode.getLN(),DLMSClassId.PROFILE_GENERIC.getClassId()));
                    register.setValue(new Long(value));
                    register.setScalerUnit(scalerUnit);
                    return register;
                }
            }
        }
        return null;
    } // public CosemObject getRegister(ObisCode obisCode)

    private ObisCodeRelation getMaximumDemandRelations() throws IOException {
        if (maximumDemandRelations == null) {
            maximumDemandRelations = new ObisCodeRelation();
            List mdprofileCodes = cof.getProfileGeneric(ALLMAXIMUMDEMANDS_PROFILE).getCaptureObjects();
            Iterator it = mdprofileCodes.iterator();
            while(it.hasNext()) {
                CapturedObject caomdp = (CapturedObject)it.next();
                List mdCodes = cof.getProfileGeneric(caomdp.getLogicalName().getObisCode()).getCaptureObjects();
                CapturedObject caomd = (CapturedObject)mdCodes.get(0);
                maximumDemandRelations.addObisCodePair(caomd.getLogicalName().getObisCode(), caomdp.getLogicalName().getObisCode());
            }
        }

        return maximumDemandRelations;

    } // private ObisCodeRelation getMaximumDemandRelations() throws IOException

    private void test() {
        ObisCode oc = ObisCode.fromString("1.1.1.6.0.4");
        System.out.println(oc);
        int fieldF = oc.getF();
        System.out.println(fieldF);
        ObisCode oc2 = new ObisCode(oc.getA(),oc.getB(),oc.getC(),oc.getD(),oc.getE(),255);
        System.out.println(oc2);
        oc2 = new ObisCode(oc.getA(),oc.getB(),oc.getC(),oc.getD(),oc.getE(),fieldF,fieldF<=0?true:false);
        System.out.println(oc2);
    }

    static public void main(String[] args) {
        RegisterProfileMapper rpm = new RegisterProfileMapper(null, null);
        rpm.test();


    }

} // public class RegisterProfileMapper
