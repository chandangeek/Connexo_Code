/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.iec1107.ppmi1.register;

import com.energyict.mdc.common.BaseUnit;
import com.energyict.mdc.common.ObisCode;

import com.energyict.protocolimpl.iec1107.ppmi1.MetaRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.ObisCodeMapper;
import com.energyict.protocolimpl.iec1107.ppmi1.RegisterFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * For more information regardig Register Allocations first read
 * RegisterInformationParser.
 *
 * @author fbo
 */

public class RegisterInformation {

	private static final String	TAB					= "\t";
	private static final String	LINE_FEED			= "\n";
	private static final String	ENERGY_OBIS_PREFIX	= "1.1.";
	private static final String	VOLT_AMP			= "VA";
	private static final String	VOLT_AMP_HOUR		= "VAh";
	private static final String	EXPORT_STRING		= "Export";
	private static final String	IMPORT_STRING		= "Import";

	private BaseUnit wattHour = BaseUnit.get(BaseUnit.WATTHOUR);
	private BaseUnit varhHour = BaseUnit.get(BaseUnit.VOLTAMPEREREACTIVEHOUR);
	private BaseUnit vAHour = BaseUnit.get(BaseUnit.VOLTAMPEREHOUR);

	private MetaRegister importWh = cr(IMPORT_STRING, RegisterFactory.R_TOTAL_IMPORT_WH, this.wattHour);
	private MetaRegister exportWh = cr(EXPORT_STRING, RegisterFactory.R_TOTAL_EXPORT_WH, this.wattHour);
	private MetaRegister importVarh = cr(IMPORT_STRING, RegisterFactory.R_TOTAL_IMPORT_VARH, this.varhHour);
	private MetaRegister exportVarh = cr(EXPORT_STRING, RegisterFactory.R_TOTAL_EXPORT_VARH, this.varhHour);
	private MetaRegister vAh = cr(VOLT_AMP_HOUR, RegisterFactory.R_TOTAL_VAH, this.vAHour);

	private MetaRegister[] energyDefinition = { this.importWh, this.exportWh, this.importVarh, this.exportVarh, this.vAh };

	private BaseUnit wattUnit = BaseUnit.get(BaseUnit.WATT);
	private BaseUnit varhUnit = BaseUnit.get(BaseUnit.VOLTAMPEREREACTIVE);
	private BaseUnit vAUnit = BaseUnit.get(BaseUnit.VOLTAMPERE);

	private MetaRegister importW = cr(IMPORT_STRING, this.wattUnit);
	private MetaRegister exportW = cr(EXPORT_STRING, this.wattUnit);
	private MetaRegister importVar = cr(IMPORT_STRING, this.varhUnit);
	private MetaRegister exportVar = cr(EXPORT_STRING, this.varhUnit);
	private MetaRegister vA = cr(VOLT_AMP, this.vAUnit);

	private MetaRegister[] demandDefinition = { this.importW, this.exportW, this.importVar, this.exportVar, this.vA };

	private MetaRegister tou1 = cr("Time Of Use 1", RegisterFactory.R_TIME_OF_USE_1);
	private MetaRegister tou2 = cr("Time Of Use 2", RegisterFactory.R_TIME_OF_USE_2);
	private MetaRegister tou3 = cr("Time Of Use 3", RegisterFactory.R_TIME_OF_USE_3);
	private MetaRegister tou4 = cr("Time Of Use 4", RegisterFactory.R_TIME_OF_USE_4);
	private MetaRegister tou5 = cr("Time Of Use 5", RegisterFactory.R_TIME_OF_USE_5);
	private MetaRegister tou6 = cr("Time Of Use 6", RegisterFactory.R_TIME_OF_USE_6);
	private MetaRegister tou7 = cr("Time Of Use 7", RegisterFactory.R_TIME_OF_USE_7);
	private MetaRegister tou8 = cr("Time Of Use 8", RegisterFactory.R_TIME_OF_USE_8);

	private MetaRegister touRegister[] = { this.tou1, this.tou2, this.tou3, this.tou4, this.tou5, this.tou6, this.tou7, this.tou8 };
	private List touRegisterList = Arrays.asList(this.touRegister);

	private MetaRegister mdTou1 = cr("Maximum Demand 1", RegisterFactory.R_MAXIMUM_DEMAND_1);
	private MetaRegister mdTou2 = cr("Maximum Demand 2", RegisterFactory.R_MAXIMUM_DEMAND_2);
	private MetaRegister mdTou3 = cr("Maximum Demand 3", RegisterFactory.R_MAXIMUM_DEMAND_3);
	private MetaRegister mdTou4 = cr("Maximum Demand 4", RegisterFactory.R_MAXIMUM_DEMAND_4);

	private MetaRegister mdRegister[] = { this.mdTou1, this.mdTou2, this.mdTou3, this.mdTou4 };
	private List mdRegisterList = Arrays.asList(this.mdRegister);

	private MetaRegister cmdTou1 = cr("Cumulative Maximum Demand 1", RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND1);
	private MetaRegister cmdTou2 = cr("Cumulative Maximum Demand 2", RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND2);
	private MetaRegister cmdTou3 = cr("Cumulative Maximum Demand 3", RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND3);
	private MetaRegister cmdTou4 = cr("Cumulative Maximum Demand 4", RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND4);

	private MetaRegister cmdRegister[] = { this.cmdTou1, this.cmdTou2, this.cmdTou3, this.cmdTou4 };
	private List cmdRegisterList = Arrays.asList(this.cmdRegister);

	private ScalingFactor scalingFactor = null;

	public void setScalingFactor(ScalingFactor scalingFactor) {
		this.scalingFactor = scalingFactor;

		for (int i = 0; i < this.energyDefinition.length; i++) {
			this.energyDefinition[i].setScalingFactor(scalingFactor);
		}

		for (int i = 0; i < this.demandDefinition.length; i++) {
			this.demandDefinition[i].setScalingFactor(scalingFactor);
		}
	}

	/** index is 1-based */
	public MetaRegister findTOURegisterFor(MetaRegister mr, int index) {
		if (mr.equals(this.touRegister[index].getSourceRegister())) {
			return this.mdRegister[index-1];
		} else {
			return null;
		}
	}

	public MetaRegister[] findTOURegisterFor(MetaRegister mr) {
		if (mr == null) {
			return null;
		}

		HashSet result = new HashSet();

		for (int i = 0; i < this.touRegister.length; i++) {
			if (mr.equals(this.touRegister[i].getSourceRegister())) {
				result.add(this.touRegister[i]);
			}
		}

		return (MetaRegister[]) result.toArray(new MetaRegister[0]);

	}

	public MetaRegister findMDRegisterFor(MetaRegister mr, int index) {
		if (mr.equals(this.mdRegister[index].getSourceRegister())) {
			return this.mdRegister[index];
		} else {
			return null;
		}
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

		if (mr == null) {
			return null;
		}

		for (int i = 0; i < this.mdRegister.length; i++) {
			if (mr.equals(this.mdRegister[i].getSourceRegister())) {
				return this.mdRegister[i];
			}
		}

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
		for (int i = 0; i < this.mdRegister.length; i++) {
			if (mr.equals(this.mdRegister[i].getSourceRegister())) {
				result.add( this.mdRegister[i] );
			}
		}
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

		if (mr == null) {
			return null;
		}

		for (int i = 0; i < this.cmdRegister.length; i++) {
			if (mr.equals(this.cmdRegister[i].getSourceRegister())) {
				return this.cmdRegister[i];
			}
		}

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
		for (int i = 0; i < this.cmdRegister.length; i++) {
			if (mr.equals(this.cmdRegister[i].getSourceRegister())) {
				result.add( this.cmdRegister[i] );
			}
		}
		return result;
	}

	public boolean isTouRegister( MetaRegister mr ){
		return this.touRegisterList.contains( mr );
	}

	public boolean isMDRegister( MetaRegister mr ){
		return this.mdRegisterList.contains( mr );
	}

	public boolean isCMDRegister( MetaRegister mr ){
		return this.cmdRegisterList.contains( mr );
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

		if (RegisterFactory.R_TOTAL_IMPORT_WH.equals(registerFactoryKey)) {
			return this.importWh;
		}
		if (RegisterFactory.R_TOTAL_EXPORT_WH.equals(registerFactoryKey)) {
			return this.exportWh;
		}
		if (RegisterFactory.R_TOTAL_IMPORT_VARH.equals(registerFactoryKey)) {
			return this.importVarh;
		}
		if (RegisterFactory.R_TOTAL_EXPORT_VARH.equals(registerFactoryKey)) {
			return this.exportVarh;
		}
		if (RegisterFactory.R_TOTAL_VAH.equals(registerFactoryKey)) {
			return this.vAh;
		}

		if (RegisterFactory.R_TIME_OF_USE_1.equals(registerFactoryKey)) {
			return this.tou1;
		}
		if (RegisterFactory.R_TIME_OF_USE_2.equals(registerFactoryKey)) {
			return this.tou2;
		}
		if (RegisterFactory.R_TIME_OF_USE_3.equals(registerFactoryKey)) {
			return this.tou3;
		}
		if (RegisterFactory.R_TIME_OF_USE_4.equals(registerFactoryKey)) {
			return this.tou4;
		}
		if (RegisterFactory.R_TIME_OF_USE_5.equals(registerFactoryKey)) {
			return this.tou5;
		}
		if (RegisterFactory.R_TIME_OF_USE_6.equals(registerFactoryKey)) {
			return this.tou6;
		}
		if (RegisterFactory.R_TIME_OF_USE_7.equals(registerFactoryKey)) {
			return this.tou7;
		}
		if (RegisterFactory.R_TIME_OF_USE_8.equals(registerFactoryKey)) {
			return this.tou8;
		}

		if (RegisterFactory.R_MAXIMUM_DEMAND_1.equals(registerFactoryKey)) {
			return this.mdTou1;
		}
		if (RegisterFactory.R_MAXIMUM_DEMAND_2.equals(registerFactoryKey)) {
			return this.mdTou2;
		}
		if (RegisterFactory.R_MAXIMUM_DEMAND_3.equals(registerFactoryKey)) {
			return this.mdTou3;
		}
		if (RegisterFactory.R_MAXIMUM_DEMAND_4.equals(registerFactoryKey)) {
			return this.mdTou4;
		}

		if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND1
				.equals(registerFactoryKey)) {
			return this.cmdTou1;
		}
		if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND2
				.equals(registerFactoryKey)) {
			return this.cmdTou2;
		}
		if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND3
				.equals(registerFactoryKey)) {
			return this.cmdTou3;
		}
		if (RegisterFactory.R_CUMULATIVE_MAXIMUM_DEMAND4
				.equals(registerFactoryKey)) {
			return this.cmdTou4;
		}

		return null;
	}


	public Collection getAvailableObisCodes( ){
		ArrayList result = new ArrayList();

		result.add( ObisCode.fromString("1.1.1.8.0.255") );
		result.add( ObisCode.fromString("1.1.2.8.0.255") );
		result.add( ObisCode.fromString("1.1.3.8.0.255") );
		result.add( ObisCode.fromString("1.1.4.8.0.255") );
		result.add( ObisCode.fromString("1.1.9.8.0.255") );

		for( int ei = 0; ei < this.energyDefinition.length; ei ++ ) {
			for( int i = 0; i < 8; i++) {
				if( this.energyDefinition[ei].equals( this.touRegister[i].getSourceRegister() ) ) {
					String code = ENERGY_OBIS_PREFIX + (ei+1) + ".8." + (i+1) + ".255";
					result.add( ObisCode.fromString( code ) );
				}
			}
		}

		int dom [ ] = { 1, 2, 3, 4, 9 };
		boolean found = false;

		for( int di = 0; di < this.demandDefinition.length; di ++ ) {
			found = false;
			for( int i = 0; i < 4; i++) {
				if( this.demandDefinition[di].equals( this.mdRegister[i].getSourceRegister() ) ) {
					String code = null;
					if( !found ) {
						code = ENERGY_OBIS_PREFIX + dom[di] + ".6.0.255";
						result.add( ObisCode.fromString( code ) );
					}
					code = ENERGY_OBIS_PREFIX + dom[di] + ".6." + ( 128 + i ) + ".255";
					result.add( ObisCode.fromString( code ) );
					found = true;
				}
			}
		}

		for( int di = 0; di < this.demandDefinition.length; di ++ ) {
			found = false;
			for( int i = 0; i < 4; i++) {
				if( this.demandDefinition[di].equals( this.cmdRegister[i].getSourceRegister() ) ) {
					String code = null;
					if( !found ) {
						code = ENERGY_OBIS_PREFIX + dom[di] + ".2.0.255";
						result.add( ObisCode.fromString( code ) );
					}
					code = ENERGY_OBIS_PREFIX + dom[di] + ".2." + ( 128 + i ) + ".255";
					result.add( ObisCode.fromString( code ) );
					found = true;
				}
			}
		}
		logObisCodes(result);
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
			"<void method=\"add\">" + LINE_FEED +
			"<object class=\"com.energyict.commserverguicommon.core.RegisterPair\">" + LINE_FEED +
			"<string>" + obisCode + "</string>" + LINE_FEED +
			"<string>Reactive power+ all phases rate 3 Cumulative maximum using measurement period 1</string>" + LINE_FEED +
			"<string>Power, Reactive import, Cumulative Maximum Demand, current value</string>" + LINE_FEED +
			"<boolean>true</boolean>" + LINE_FEED +
			"</object>" + LINE_FEED +
			"</void>" + LINE_FEED;
		System.out.println( xml );
	}


	public String getExtendedLogging( ) throws IOException {
		StringBuffer r = new StringBuffer();

		//r.append( this );

		Iterator o = getAvailableObisCodes().iterator();
		while( o.hasNext() ){
			ObisCode code = (ObisCode)o.next();
			r.append( code.toString() + " = " + ObisCodeMapper.getRegisterInfo( code ) + LINE_FEED );
		}

		r.append( "Register mappings are identical for historical values F= 255, VZ, VZ-1, VZ-2 and VZ-3" );

		return r.toString();

	}


	public String toString() {
		StringBuffer sb = new StringBuffer();

		sb.append("importWh   ").append(TAB).append("[").append(this.importWh).append("]").append(LINE_FEED);
		sb.append("exportWh   ").append(TAB).append("[").append(this.exportWh).append("]").append(LINE_FEED);
		sb.append("importVarh ").append(TAB).append("[").append(this.importVarh).append("]").append(LINE_FEED);
		sb.append("exportVarh ").append(TAB).append("[").append(this.exportVarh).append("]").append(LINE_FEED);
		sb.append("vAh        ").append(TAB).append("[").append(this.vAh).append("]").append(LINE_FEED).append(LINE_FEED);

		sb.append("importW ").append(this.importW).append(LINE_FEED);
		sb.append("exportW ").append(this.exportW).append(LINE_FEED);
		sb.append("importVar ").append(this.importVar).append(LINE_FEED);
		sb.append("exportVar ").append(this.exportVar).append(LINE_FEED);
		sb.append("vA ").append(this.vA).append(LINE_FEED).append(LINE_FEED);

		sb.append(this.tou1.toString()).append(LINE_FEED);
		sb.append(this.tou2.toString()).append(LINE_FEED);
		sb.append(this.tou3.toString()).append(LINE_FEED);
		sb.append(this.tou4.toString()).append(LINE_FEED);
		sb.append(this.tou5.toString()).append(LINE_FEED);
		sb.append(this.tou6.toString()).append(LINE_FEED);
		sb.append(this.tou7.toString()).append(LINE_FEED);
		sb.append(this.tou8.toString()).append(LINE_FEED).append(LINE_FEED);

		sb.append(this.mdTou1.toString()).append(LINE_FEED);
		sb.append(this.mdTou2.toString()).append(LINE_FEED);
		sb.append(this.mdTou3.toString()).append(LINE_FEED);
		sb.append(this.mdTou4.toString()).append(LINE_FEED).append(LINE_FEED);

		sb.append(this.cmdTou1.toString()).append(LINE_FEED);
		sb.append(this.cmdTou2.toString()).append(LINE_FEED);
		sb.append(this.cmdTou3.toString()).append(LINE_FEED);
		sb.append(this.cmdTou4.toString()).append(LINE_FEED).append(LINE_FEED);

		sb.append(this.scalingFactor);

		return sb.toString();
	}

	public BaseUnit getWattHour() {
		return this.wattHour;
	}

	public BaseUnit getVarhHour() {
		return this.varhHour;
	}

	public BaseUnit getVAHour() {
		return this.vAHour;
	}

	public MetaRegister getImportWh() {
		return this.importWh;
	}

	public MetaRegister getExportWh() {
		return this.exportWh;
	}

	public MetaRegister getImportVarh() {
		return this.importVarh;
	}

	public MetaRegister getExportVarh() {
		return this.exportVarh;
	}

	public MetaRegister getVAh() {
		return this.vAh;
	}

	public MetaRegister[] getEnergyDefinition() {
		return this.energyDefinition;
	}

	public BaseUnit getWattUnit() {
		return this.wattUnit;
	}

	public BaseUnit getVarhUnit() {
		return this.varhUnit;
	}

	public BaseUnit getVAUnit() {
		return this.vAUnit;
	}

	public MetaRegister getImportW() {
		return this.importW;
	}

	public MetaRegister getExportW() {
		return this.exportW;
	}

	public MetaRegister getImportVar() {
		return this.importVar;
	}

	public MetaRegister getExportVar() {
		return this.exportVar;
	}

	public MetaRegister getVA() {
		return this.vA;
	}

	public MetaRegister[] getDemandDefinition() {
		return this.demandDefinition;
	}

	public MetaRegister getTou1() {
		return this.tou1;
	}

	public MetaRegister getTou2() {
		return this.tou2;
	}

	public MetaRegister getTou3() {
		return this.tou3;
	}

	public MetaRegister getTou4() {
		return this.tou4;
	}

	public MetaRegister getTou5() {
		return this.tou5;
	}

	public MetaRegister getTou6() {
		return this.tou6;
	}

	public MetaRegister getTou7() {
		return this.tou7;
	}

	public MetaRegister getTou8() {
		return this.tou8;
	}

	public MetaRegister[] getTouRegister() {
		return this.touRegister;
	}

	public List getTouRegisterList() {
		return this.touRegisterList;
	}

	public MetaRegister getMdTou1() {
		return this.mdTou1;
	}

	public MetaRegister getMdTou2() {
		return this.mdTou2;
	}

	public MetaRegister getMdTou3() {
		return this.mdTou3;
	}

	public MetaRegister getMdTou4() {
		return this.mdTou4;
	}

	public MetaRegister[] getMdRegister() {
		return this.mdRegister;
	}

	public List getMdRegisterList() {
		return this.mdRegisterList;
	}

	public MetaRegister getCmdTou1() {
		return this.cmdTou1;
	}

	public MetaRegister getCmdTou2() {
		return this.cmdTou2;
	}

	public MetaRegister getCmdTou3() {
		return this.cmdTou3;
	}

	public MetaRegister getCmdTou4() {
		return this.cmdTou4;
	}

	public MetaRegister[] getCmdRegister() {
		return this.cmdRegister;
	}

	public List getCmdRegisterList() {
		return this.cmdRegisterList;
	}

	public ScalingFactor getScalingFactor() {
		return this.scalingFactor;
	}

}
