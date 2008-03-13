/*
 * ObisCodeMapper.java
 *
 * Created on 11 juni 2004, 13:55
 */

package com.energyict.protocolimpl.iec1107.abba1140;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterInfo;
import com.energyict.protocol.RegisterValue;
import java.util.logging.Level;
import java.util.logging.Logger;

/** @author Koen */

public class ObisCodeMapper {
    
    private ABBA1140RegisterFactory rFactory;
    
    /** Creates a new instance of ObisCodeMapping */
    ObisCodeMapper(ABBA1140RegisterFactory abba1140RegisterFactory) {
        this.rFactory=abba1140RegisterFactory;
    }
    
    static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
        ObisCodeMapper ocm = new ObisCodeMapper(null);
        return (RegisterInfo)ocm.doGetRegister(obisCode,false);
    }
    
    public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
        try {
            return (RegisterValue)doGetRegister(obisCode,true);
        } catch( IOException ioe ){
            Logger logger = rFactory.getProtocolLink().getLogger();
            String msg = "Problems retrieving " + obisCode.toString();
            logger.log( Level.INFO, msg );
            throw ioe;
        }
    }
    
    private Object doGetRegister(ObisCode obisCode, boolean read) throws IOException {
        
        RegisterValue registerValue=null;
        String registerName=null;
        int bp=-1;
        StringBuffer obisTranslation=new StringBuffer();
        Unit unit = null;
        
        // obis F code
        if ((obisCode.getF() >=0) && (obisCode.getF() <= 99))
            bp = obisCode.getF();
        else if ((obisCode.getF() <=0) && (obisCode.getF() >= -99))
            bp = obisCode.getF()*-1;
        else if (obisCode.getF() == 255)
            bp = -1;
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        
        // *********************************************************************************
        // General purpose ObisRegisters & abstract general service
        
        /** Billing Point Timestamp */
        if (obisCode.toString().startsWith("1.1.0.1.2.")) { 
            if ((bp >= 0) && (bp < 14)) {
                if (read) {
                    HistoricalRegister hv = (HistoricalRegister)rFactory.getRegister("HistoricalRegister",bp);

                    Quantity quantity = new Quantity( new BigDecimal( hv.getBillingTrigger() ),Unit.get(255) );
                    Date eventTime = hv.getBillingDate();
                    Date fromTime = null;
                    Date toTime = hv.getBillingDate();
                    
                    registerValue = new RegisterValue(obisCode,quantity,eventTime,fromTime,toTime);
                    
                    return registerValue;
                } else { 
                    return new RegisterInfo("billing point "+bp+" timestamp");
                }
            } else {
                String msg = "ObisCode "+obisCode.toString()+" is not supported!";
                throw new NoSuchRegisterException(msg);
            }
        }
        /** Current System Status */
        else if (obisCode.toString().startsWith("0.0.96.50.0.255") ) { 
            if (read) {
                SystemStatus ss = (SystemStatus)rFactory.getRegister("SystemStatus");
                registerValue = new RegisterValue(obisCode,new Quantity(BigDecimal.valueOf(ss.getValue()),Unit.get(255)));
                return registerValue;
            } else {
                return new RegisterInfo("Current System Status (32 bit word)");
            }
        } else if ((obisCode.toString().indexOf("1.1.0.4.2.255") != -1) || (obisCode.toString().indexOf("1.0.0.4.2.255") != -1)) { 
            if (read) {
                BigDecimal bd = (BigDecimal)rFactory.getRegister("CTPrimary");
                registerValue = new RegisterValue(obisCode,new Quantity(bd,Unit.get(255)));
                return registerValue;
            } else return new RegisterInfo("CT numerator");
        } else if ((obisCode.toString().indexOf("1.1.0.4.5.255") != -1) || (obisCode.toString().indexOf("1.0.0.4.5.255") != -1)) { // CT denominator
            if (read) {
                BigDecimal bd = (BigDecimal)rFactory.getRegister("CTSecundary");
                registerValue = new RegisterValue(obisCode,new Quantity(bd,Unit.get(255)));
                return registerValue;
            } else return new RegisterInfo("CT denominator");
        } else if (obisCode.toString().indexOf("1.0.0.0.1.255") != -1) { // SchemeID
            if (read) {
                String schemeId = (String)rFactory.getRegister("SchemeID");
                registerValue = new RegisterValue(obisCode,schemeId);
                return registerValue;
            } else return new RegisterInfo("SchemeID");
        }
        
        // *********************************************************************************
        // Electricity related ObisRegisters
        // verify a & b
        if ((obisCode.getA() != 1) || (obisCode.getB() < 0) || (obisCode.getB() > 3))
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        
        // obis C code
        if (obisCode.getC() == 1) {
            registerName = "CummMainImport";
        } else if (obisCode.getC() == 2) {
            registerName = "CummMainExport";
        } else if (obisCode.getC() == 5) {
            registerName = "CummMainQ1";
        } else if (obisCode.getC() == 6) {
            registerName = "CummMainQ2";
        } else if (obisCode.getC() == 7) {
            registerName = "CummMainQ3";
        } else if (obisCode.getC() == 8) {
            registerName = "CummMainQ4";
        } else if (obisCode.getC() == 9) {
            registerName = "CummMainVAImport";
        } else if (obisCode.getC() == 10) {
            registerName = "CummMainVAExport";
        } else if (obisCode.getC() == 128) {
            if (read) {
                CustDefRegConfig cdrc = (CustDefRegConfig)rFactory.getRegister("CustDefRegConfig");
                unit = EnergyTypeCode.getUnitFromRegSource(cdrc.getRegSource(0),true);
            }
            registerName = "CummMainCustDef1";
        } else if (obisCode.getC() == 129) {
            if (read) {
                CustDefRegConfig cdrc = (CustDefRegConfig)rFactory.getRegister("CustDefRegConfig");
                unit = EnergyTypeCode.getUnitFromRegSource(cdrc.getRegSource(1),true);
            }
            registerName = "CummMainCustDef2";
        }
        
        else throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        
        if( rFactory != null && obisCode.getF() >= 0 && obisCode.getF() <= 14 ) {
            HistoricalRegister hv = (HistoricalRegister)rFactory.getRegister("HistoricalRegister",bp);
            if( hv.getBillingDate() ==  null )
                throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        }
        
        // obis D code
        if (obisCode.getD() == 2) {// cumulative maximum 1  CMD
            // search for right unit (registersource)...
            if (read) {
                for (int i=0;i<ABBA1140RegisterFactory.MAX_CMD_REGS;i++) {
                    CumulativeMaximumDemand cmd = (CumulativeMaximumDemand)rFactory.getRegister("CumulativeMaximumDemand"+i,bp);
                    // energytype code match
                    if (cmd.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        if (unit != null) // in case of customer defined registers unit is defined earlier
                            cmd.setQuantity(new Quantity(cmd.getQuantity().getAmount(),unit.getFlowUnit()));
                        
                        return cmd.toRegisterValue(obisCode);
                    }
                }
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(),false));
                obisTranslation.append(", cumulative maximum demand");
            }
            
        } else if (obisCode.getD() == 6 && obisCode.getB() > 0 ) {// maximum 1 MD
            
           /*
            *   1a 1b 1c
            *   2a 2b 2c
            *   ...
            *   Search in the 1a, 2a,... values to match the energytype code. Then search for the highest value of the 3.
            *   We suppost that the energytype code is the same for all 3 MD registers which is not always so. When the
            *   configuration changes, it is possible that the energytype code changes in the registers.
            *   Generate an NoSuchRegisterException wanneer het energytype verschillend is in het te lezen register!
            *   Gebruik de Obis B code om te bepalen welk van de 3 registers je wil lezen.
            *
            */
            
            if (read) {
                for (int i=0;i<ABBA1140RegisterFactory.MAX_MD_REGS;i+=3) {
                    List mds = new ArrayList();
                    for (int j=0;j<3;j++)
                        mds.add((MaximumDemand)rFactory.getRegister("MaximumDemand"+(i+j),bp));
                    // sort in accending datetime
                    MaximumDemand.sortOnDateTime(mds);
                    // energytype code match with the maximumdemand register with the most
                    // recent datetime stamp.
                    MaximumDemand md = (MaximumDemand)mds.get(2);
                    if (md.getRegSource() == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        // Sort in accending quantity. If not all 3 energytype codes are
                        // the same, an IOException is thrown.
                        MaximumDemand.sortOnQuantity(mds);
                        md = (MaximumDemand)mds.get(3 - obisCode.getB()); // B=1 => get(2), B=2 => get(1), B=3 => get(0)
                        if (unit != null) // in case of customer defined registers unit is defined earlier
                            md.setQuantity(new Quantity(md.getQuantity().getAmount(),unit.getFlowUnit()));
                        return md.toRegisterValue(obisCode);
                    }
                }
            } else {
                obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(),false));
                obisTranslation.append(", maximum demand");
            }
            
        } else if (obisCode.getD() == 8) {// time integral 1 TOTAL & RATE
            obisTranslation.append(EnergyTypeCode.getCompountInfoFromObisC(obisCode.getC(),true));
            
            if (read) {
                // fbl: bugfix 9/10/2006 extra check on e field.
                if( obisCode.getE() > 8 ){
                    String msg = "ObisCode "+obisCode.toString()+" is not supported!";
                    throw new NoSuchRegisterException(msg);
                }
                if (obisCode.getE() > 0) {
                    
                    TariffSources ts;
                    if (bp == -1) {
                        ts=(TariffSources)rFactory.getRegister("TariffSources");
                    } else {
                        ts=((HistoricalRegister)rFactory.getRegister( "HistoricalRegister", bp )).getTariffSources();
                    }
                    
                    registerName = "TimeOfUse"+(obisCode.getE()-1);
                    MainRegister mr = (MainRegister)rFactory.getRegister(registerName,bp);
                    if (ts.getRegSource()[obisCode.getE()-1] == EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC())) {
                        if (EnergyTypeCode.isCustomerDefined(ts.getRegSource()[obisCode.getE()-1])) {
                            unit = EnergyTypeCode.getUnitFromRegSource(EnergyTypeCode.getRegSourceFromObisCCode(obisCode.getC()),true);
                        } else {
                            unit = EnergyTypeCode.getUnitFromObisCCode(obisCode.getC(),true);
                        }
                        mr.setQuantity(new Quantity(mr.getQuantity().getAmount(),unit));
                        return mr.toRegisterValue(obisCode);
                    } else {
                        String msg = "ObisCode "+obisCode.toString()+" is not supported!";
                        throw new NoSuchRegisterException(msg);
                    }
                    
                } else {
                    MainRegister mr = (MainRegister)rFactory.getRegister(registerName,bp);
                    if (unit != null) {
                        mr.setQuantity(new Quantity(mr.getQuantity().getAmount(),unit));
                    }
                    return mr.toRegisterValue(obisCode);
                }
            } else {
                if (obisCode.getE() > 0) {
                    obisTranslation.append(", tariff register "+obisCode.getE());
                }
            }
        } else {
            String msg = "ObisCode "+obisCode.toString()+" is not supported!";
            throw new NoSuchRegisterException(msg);
        }
        
        if (bp == -1)
            obisTranslation.append(", current value");
        else
            obisTranslation.append(", billing point "+(bp+1));
        
        if (read)
            throw new NoSuchRegisterException("ObisCode "+obisCode.toString()+" is not supported!");
        else
            return new RegisterInfo(obisTranslation.toString());
        
    }
    
}
