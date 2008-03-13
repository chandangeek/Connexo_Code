package com.energyict.protocolimpl.iec1107.ppm.register;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.io.IOException;


import com.energyict.obis.ObisCode;
import com.energyict.cbo.BaseUnit;
import com.energyict.protocolimpl.iec1107.ppm.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppm.RegisterFactory;
import com.energyict.protocolimpl.iec1107.ppm.ObisCodeMapper;

/**
 * For more information regardig Register Allocations first read
 * RegisterInformationParser.
 *
 * @author fbo
 */

public class RegisterInformation {
    
    private BaseUnit wattHour = BaseUnit.get(BaseUnit.WATTHOUR);
    private BaseUnit varhHour = BaseUnit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
    private BaseUnit vAHour = BaseUnit.get(BaseUnit.VOLTAMPEREHOUR);
    
    public MetaRegister importWh = cr("Import", RegisterFactory.R_TOTAL_IMPORT_WH, wattHour);
    public MetaRegister exportWh = cr("Export", RegisterFactory.R_TOTAL_EXPORT_WH, wattHour);
    public MetaRegister importVarh = cr("Import", RegisterFactory.R_TOTAL_IMPORT_VARH, varhHour);
    public MetaRegister exportVarh = cr("Export", RegisterFactory.R_TOTAL_EXPORT_VARH, varhHour);
    public MetaRegister vAh = cr("VAh", RegisterFactory.R_TOTAL_VAH, vAHour);
    
    public MetaRegister[] energyDefinition = {importWh, exportWh, importVarh, exportVarh, vAh};
    
    private BaseUnit wattUnit = BaseUnit.get(BaseUnit.WATT);
    private BaseUnit varhUnit = BaseUnit.get(BaseUnit.VOLTAMPEREREACTIVE);
    private BaseUnit vAUnit = BaseUnit.get(BaseUnit.VOLTAMPERE);
    
    public MetaRegister importW = cr("Import", wattUnit);
    public MetaRegister exportW = cr("Export", wattUnit);
    public MetaRegister importVar = cr("Import", varhUnit);
    public MetaRegister exportVar = cr("Export", varhUnit);
    public MetaRegister vA = cr("VA", vAUnit);
    
    public MetaRegister[] demandDefinition = {importW, exportW, importVar, exportVar, vA};
    
    public MetaRegister tou1 = cr("Time Of Use 1", RegisterFactory.R_TIME_OF_USE_1);
    public MetaRegister tou2 = cr("Time Of Use 2", RegisterFactory.R_TIME_OF_USE_2);
    public MetaRegister tou3 = cr("Time Of Use 3", RegisterFactory.R_TIME_OF_USE_3);
    public MetaRegister tou4 = cr("Time Of Use 4", RegisterFactory.R_TIME_OF_USE_4);
    public MetaRegister tou5 = cr("Time Of Use 5", RegisterFactory.R_TIME_OF_USE_5);
    public MetaRegister tou6 = cr("Time Of Use 6", RegisterFactory.R_TIME_OF_USE_6);
    public MetaRegister tou7 = cr("Time Of Use 7", RegisterFactory.R_TIME_OF_USE_7);
    public MetaRegister tou8 = cr("Time Of Use 8", RegisterFactory.R_TIME_OF_USE_8);
    
    public MetaRegister touRegister[] = {tou1, tou2, tou3, tou4, tou5, tou6, tou7, tou8};
    public List touRegisterList = Arrays.asList( touRegister );
    
    public MetaRegister mdTou1 = cr("Maximum Demand 1", RegisterFactory.R_MAXIMUM_DEMAND_1);
    public MetaRegister mdTou2 = cr("Maximum Demand 2", RegisterFactory.R_MAXIMUM_DEMAND_2);
    public MetaRegister mdTou3 = cr("Maximum Demand 3", RegisterFactory.R_MAXIMUM_DEMAND_3);
    public MetaRegister mdTou4 = cr("Maximum Demand 4", RegisterFactory.R_MAXIMUM_DEMAND_4);
    
    public MetaRegister mdRegister[] = {mdTou1, mdTou2, mdTou3, mdTou4};
    public List mdRegisterList = Arrays.asList( mdRegister );
    
    public MetaRegister cmdTou1 = cr("Cumulative Maximum Demand 1",
    RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND1);
    public MetaRegister cmdTou2 = cr("Cumulative Maximum Demand 2",
    RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND2);
    public MetaRegister cmdTou3 = cr("Cumulative Maximum Demand 3",
    RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND3);
    public MetaRegister cmdTou4 = cr("Cumulative Maximum Demand 4",
    RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND4);
    
    public MetaRegister cmdRegister[] = {cmdTou1, cmdTou2, cmdTou3, cmdTou4};
    public List cmdRegisterList = Arrays.asList( cmdRegister );
    
    private ScalingFactor scalingFactor = null;
    
    public void setScalingFactor(ScalingFactor scalingFactor) {
        this.scalingFactor = scalingFactor;
        
        for (int i = 0; i < energyDefinition.length; i++)
            energyDefinition[i].setScalingFactor(scalingFactor);
        
        for (int i = 0; i < demandDefinition.length; i++)
            demandDefinition[i].setScalingFactor(scalingFactor);
    }
    
    /** index is 1-based */
    public MetaRegister findTOURegisterFor(MetaRegister mr, int index) {
        if (mr.equals(touRegister[index].getSourceRegister()))
            return mdRegister[index-1];
        else
            return null;
    }
    
    public MetaRegister[] findTOURegisterFor(MetaRegister mr) {
        if (mr == null)
            return null;
        
        HashSet result = new HashSet();
        
        for (int i = 0; i < touRegister.length; i++)
            if (mr.equals(touRegister[i].getSourceRegister()))
                result.add(touRegister[i]);
        
        return (MetaRegister[]) result.toArray(new MetaRegister[0]);
        
    }
    
    public MetaRegister findMDRegisterFor(MetaRegister mr, int index) {
        if (mr.equals(mdRegister[index].getSourceRegister()))
            return mdRegister[index];
        else
            return null;
    }
    
    /**
     * Find the FIRST Maximum Demand register that maps to the 
     * mr.sourceRegister.  This method only works when the protocol is running
     * since it needs the meter configuration.
     *
     * @param mr source register for wich to find derived register
     * @return first matching derived register
     */
    public MetaRegister findMDRegisterFor(MetaRegister mr) {
        
        if (mr == null)
            return null;
        
        for (int i = 0; i < mdRegister.length; i++)
            if (mr.equals(mdRegister[i].getSourceRegister()))
                return mdRegister[i];
        
        return null;
    }

    /**
     * Find All Maximum Demand register that map to the mr.sourceRegister.
     * This method only works when the protocol is running since it needs the 
     * meter configuration.
     *
     * @param mr source register for wich to find derived registers
     * @return all matching derived register
     */    
    public List findAllMDRegistersFor( MetaRegister mr ){
        ArrayList result = new ArrayList();
        for (int i = 0; i < mdRegister.length; i++)
            if (mr.equals(mdRegister[i].getSourceRegister()))       
                result.add( mdRegister[i] );
        return result;
    }
    
    /**
     * Find the FIRST Cummulative Maximum Demand register that maps to the 
     * mr.sourceRegister.  This method only works when the protocol is running
     * since it needs the meter configuration.
     *
     * @param mr source register for wich to find derived register
     * @return first matching derived register
     */
    public MetaRegister findCMDRegisterFor(MetaRegister mr) {
        
        if (mr == null)
            return null;
        
        for (int i = 0; i < cmdRegister.length; i++)
            if (mr.equals(cmdRegister[i].getSourceRegister()))
                return cmdRegister[i];
        
        return null;
    }
    
    /**
     * Find All Cummulative Maximum Demand register that map to the 
     * mr.sourceRegister.  This method only works when the protocol is running
     * since it needs the meter configuration.
     *
     * @param mr source register for wich to find derived registers
     * @return all matching derived register
     */    
    public List findAllCMDRegistersFor( MetaRegister mr ){
        ArrayList result = new ArrayList();
        for (int i = 0; i < cmdRegister.length; i++)
            if (mr.equals(cmdRegister[i].getSourceRegister()))       
                result.add( cmdRegister[i] );
        return result;
    }
    
    public boolean isTouRegister( MetaRegister mr ){
        return touRegisterList.contains( mr );
    }
    
    public boolean isMDRegister( MetaRegister mr ){
        return mdRegisterList.contains( mr );
    }
    
    public boolean isCMDRegister( MetaRegister mr ){
        return cmdRegisterList.contains( mr );
    }
    
    /* short cut methods */
    
    /* Create Register */
    private MetaRegister cr(String name, String registerFactoryKey) {
        return new MetaRegister(name, registerFactoryKey);
    }
    
    /* Create Register */
    private MetaRegister cr(String name, BaseUnit baseUnit) {
        return new MetaRegister(name, baseUnit);
    }
    
    /* Create Register */
    private MetaRegister cr(String name, String registerFactoryKey,
    BaseUnit baseUnit) {
        return new MetaRegister(name, registerFactoryKey, baseUnit);
    }
    
    public MetaRegister get(String registerFactoryKey) {
        
        if (RegisterFactory.R_TOTAL_IMPORT_WH.equals(registerFactoryKey))
            return importWh;
        if (RegisterFactory.R_TOTAL_EXPORT_WH.equals(registerFactoryKey))
            return exportWh;
        if (RegisterFactory.R_TOTAL_IMPORT_VARH.equals(registerFactoryKey))
            return importVarh;
        if (RegisterFactory.R_TOTAL_EXPORT_VARH.equals(registerFactoryKey))
            return exportVarh;
        if (RegisterFactory.R_TOTAL_VAH.equals(registerFactoryKey))
            return vAh;
        
        if (RegisterFactory.R_TIME_OF_USE_1.equals(registerFactoryKey))
            return tou1;
        if (RegisterFactory.R_TIME_OF_USE_2.equals(registerFactoryKey))
            return tou2;
        if (RegisterFactory.R_TIME_OF_USE_3.equals(registerFactoryKey))
            return tou3;
        if (RegisterFactory.R_TIME_OF_USE_4.equals(registerFactoryKey))
            return tou4;
        if (RegisterFactory.R_TIME_OF_USE_5.equals(registerFactoryKey))
            return tou5;
        if (RegisterFactory.R_TIME_OF_USE_6.equals(registerFactoryKey))
            return tou6;
        if (RegisterFactory.R_TIME_OF_USE_7.equals(registerFactoryKey))
            return tou7;
        if (RegisterFactory.R_TIME_OF_USE_8.equals(registerFactoryKey))
            return tou8;
        
        if (RegisterFactory.R_MAXIMUM_DEMAND_1.equals(registerFactoryKey))
            return mdTou1;
        if (RegisterFactory.R_MAXIMUM_DEMAND_2.equals(registerFactoryKey))
            return mdTou2;
        if (RegisterFactory.R_MAXIMUM_DEMAND_3.equals(registerFactoryKey))
            return mdTou3;
        if (RegisterFactory.R_MAXIMUM_DEMAND_4.equals(registerFactoryKey))
            return mdTou4;
        
        if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND1.equals(registerFactoryKey))
            return cmdTou1;
        if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND2.equals(registerFactoryKey))
            return cmdTou2;
        if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND3.equals(registerFactoryKey))
            return cmdTou3;
        if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND4.equals(registerFactoryKey))
            return cmdTou4;
        
        return null;
    }
    
    public Collection getAvailableObisCodes( ){
        
        ArrayList result = new ArrayList();
        
        result.add( ObisCode.fromString("1.1.1.8.0.255") );
        result.add( ObisCode.fromString("1.1.2.8.0.255") );
        result.add( ObisCode.fromString("1.1.3.8.0.255") );
        result.add( ObisCode.fromString("1.1.4.8.0.255") );
        result.add( ObisCode.fromString("1.1.9.8.0.255") );
        
        for( int ei = 0; ei < energyDefinition.length; ei ++ ) {
            for( int i = 0; i < 8; i++) {
                if( energyDefinition[ei].equals( touRegister[i].getSourceRegister() ) ) {
                    String code = "1.1." + (ei+1) + ".8.0" + (i+1) + ".255";
                    result.add( ObisCode.fromString( code ) );
                }
            }
        }
        
        int dom [ ] = { 1, 2, 3, 4, 9 };
        boolean found = false;
        
        for( int di = 0; di < demandDefinition.length; di ++ ) {
            found = false;
            for( int i = 0; i < 4; i++) {
                if( demandDefinition[di].equals( mdRegister[i].getSourceRegister() ) ) {
                    String code = null;
                    if( !found ) {
                        code = "1.1." + dom[di] + ".6.0.255";
                        result.add( ObisCode.fromString( code ) );
                    }
                    code = "1.1." + dom[di] + ".6." + ( 128 + i ) + ".255";
                    result.add( ObisCode.fromString( code ) );
                    found = true;
                }
            }
        }
        
        for( int di = 0; di < demandDefinition.length; di ++ ) {
            found = false;
            for( int i = 0; i < 4; i++) {
                if( demandDefinition[di].equals( cmdRegister[i].getSourceRegister() ) ) {
                    String code = null;
                    if( !found ) {
                        code = "1.1." + dom[di] + ".2.0.255";
                        result.add( ObisCode.fromString( code ) );
                    }
                    code = "1.1." + dom[di] + ".2." + ( 128 + i ) + ".255";
                    result.add( ObisCode.fromString( code ) );
                    found = true;
                }
            }
        }
        //logObisCodes(result);
        return result;
    }
    
    /** Some logging for configuring the protocoltester during debugging */
    public void logObisCodes( Collection obisCodes ) {
       
        System.out.println( " log obis codes " );
          
        Iterator iter = obisCodes.iterator();
        while( iter.hasNext() ){
            ObisCode oc = (ObisCode)iter.next();
           
            int a = oc.getA();
            int b = oc.getB();
            int c = oc.getC();
            int d = oc.getD();
            int e = oc.getE();
            
            ObisCode oc0 = new ObisCode( a, b, c, d, e, 0, true );
            ObisCode oc1 = new ObisCode( a, b, c, d, e, -1, true );
            ObisCode oc2 = new ObisCode( a, b, c, d, e, -2, true );
            ObisCode oc3 = new ObisCode( a, b, c, d, e, -3, true );
            
            //new ObisCode( )
            logObisCode( oc );
            logObisCode( oc0 );
            logObisCode( oc1 );
            logObisCode( oc2 );
            logObisCode( oc3 );
            
        }
        
    }
    
    public void logObisCode( ObisCode obisCode ){
        String xml = 
            "<void method=\"add\">\n" +
            "<object class=\"com.energyict.commserverguicommon.core.RegisterPair\">\n" +  
            "<string>" + obisCode + "</string>\n" + 
            "<string>Reactive power+ all phases rate 3 Cumulative maximum using measurement period 1</string>\n" +
     	    "<string>Power, Reactive import, Cumulative Maximum Demand, current value</string>\n" +
     	    "<boolean>true</boolean>\n" +
            "</object>\n" +
            "</void>\n";
        System.out.println( xml );
    }

    public String getExtendedLogging( ) throws IOException {
        StringBuffer r = new StringBuffer();
        
        //r.append( this );
        
        Iterator o = getAvailableObisCodes().iterator();
        while( o.hasNext() ){
            ObisCode code = (ObisCode)o.next();
            r.append( code.toString() + " = " + ObisCodeMapper.getRegisterInfo( code ) + "\n" );
        }
        
        r.append( "Register mappings are identical for historical values F= 255, VZ, VZ-1, VZ-2 and VZ-3" );
        
        return r.toString();
        
    }
    
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        sb.append("importWh   \t[" + importWh + "]\n");
        sb.append("exportWh   \t[" + exportWh + "]\n");
        sb.append("importVarh \t[" + importVarh + "]\n");
        sb.append("exportVarh \t[" + exportVarh + "]\n");
        sb.append("vAh        \t[" + vAh + "]\n\n");
        
        sb.append("importW " + importW + "\n");
        sb.append("exportW " + exportW + "\n");
        sb.append("importVar " + importVar + "\n");
        sb.append("exportVar " + exportVar + "\n");
        sb.append("vA " + vA + "\n\n");
        
        sb.append(tou1.toString() + "\n");
        sb.append(tou2.toString() + "\n");
        sb.append(tou3.toString() + "\n");
        sb.append(tou4.toString() + "\n");
        sb.append(tou5.toString() + "\n");
        sb.append(tou6.toString() + "\n");
        sb.append(tou7.toString() + "\n");
        sb.append(tou8.toString() + "\n\n");
        
        sb.append(mdTou1.toString() + "\n");
        sb.append(mdTou2.toString() + "\n");
        sb.append(mdTou3.toString() + "\n");
        sb.append(mdTou4.toString() + "\n\n");
        
        sb.append(scalingFactor);
        
        return sb.toString();
        
    }
    
}