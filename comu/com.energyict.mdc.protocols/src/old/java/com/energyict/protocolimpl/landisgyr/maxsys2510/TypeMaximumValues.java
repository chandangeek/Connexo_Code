package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

class TypeMaximumValues {

	private String manufacturer;
	private String versionNumber;
	private String revisionNumber;

	private Set tablesImplemented;

	private int maxMtrInputs;
	private int maxSenseInputs;
	private int maxTotalizations;
	private int maxStorageChnnls;
	private int maxXforms;
	private int maxRates;
	private int maxSummations;
	private int maxConcValues;
	private int maxRatePeaks;
	private int maxLoadCntrls;
	private int maxDataBlks;
	private int maxOutputs;
	private int maxDailyEvents;
	private int maxDailyScheds;
	private int maxSeasons;
	private int maxDateEvents;
	private int maxSelfReads;
	private int maxDisplayItems;
	private int maxDialInCnditns;
	private int maxHistryMsgCnditns;
	private int maxSmdErrors;
	private int maxStorK;
	private int maxHistEvents;
	private int maxEncdrs;
	private int maxMidnightEncdrReads;
	private int maxRateMins;
	private int maxSubIntvls;

	private Set auxTablesImplemented;

	private int industryNo;
	private int maxCartSize;
	private int cartMemSize;

	private Set controlTables;

	private int maxExternalDisplayItems;
	private int maxHighSpeedEvents;
	private int maxHiLoDemands;
	private int maxDaysHiLoDemands;
	private int maxLogicOrs;
	private int maxLogicElements;
	private int maxTimers;
	private int maxFlipflops;
	private int maxEventScheds;
	private int maxAnalogOutputs;
	private int maxAnalogFilters;
	private int maxKompensations;
	private int maxKompTotalizers;
	private int maxKompFilters;
	private int maxKompConstants;
	private int lastMLine;
	private int maxPeerNumericOutputs;
	private int maxPeerNumericInputs;
	private int maxPeerStatusOutputs;
	private int maxPeerSenseInputs;
	private int maxConstants;
	private int maxNandAllocations;
	private int maxBussFilters;
	private int maxBussFilterLen;
	private int maxModemStrings;
	private int maxPeerAbsoluteOutputs;
	private int maxPeerAbsoluteInputs;

	static TypeMaximumValues parse( Assembly assembly ){
		TypeMaximumValues tmv = new TypeMaximumValues();

		tmv.manufacturer = assembly.stringValue(4);
		tmv.versionNumber = assembly.stringValue(4);
		tmv.revisionNumber = assembly.stringValue(2);

		tmv.tablesImplemented = new TreeSet();
		ByteArray ba = assembly.getBytes(32);
		for( int i = 0; i < 255; i ++ ) {
			Integer table = Integer.valueOf(i);
			if( ba.getBit(i) ) {
				tmv.tablesImplemented.add(table);
			}
		}

		tmv.maxMtrInputs = assembly.intValue();
		tmv.maxSenseInputs = assembly.intValue();
		tmv.maxTotalizations = assembly.intValue();
		tmv.maxStorageChnnls = assembly.intValue();
		tmv.maxXforms = assembly.intValue();
		tmv.maxRates = assembly.intValue();
		tmv.maxSummations = assembly.intValue();
		tmv.maxConcValues = assembly.intValue();
		tmv.maxRatePeaks = assembly.intValue();
		tmv.maxLoadCntrls = assembly.intValue();
		tmv.maxDataBlks = assembly.intValue();
		tmv.maxOutputs = assembly.intValue();
		tmv.maxDailyEvents = assembly.intValue();
		tmv.maxDailyScheds = assembly.intValue();
		tmv.maxSeasons = assembly.intValue();
		tmv.maxDateEvents = assembly.intValue();
		tmv.maxSelfReads = assembly.intValue();
		tmv.maxDisplayItems = assembly.intValue();
		tmv.maxDialInCnditns = assembly.intValue();
		tmv.maxHistryMsgCnditns = assembly.intValue();
		tmv.maxSmdErrors = assembly.intValue();
		tmv.maxStorK = assembly.intValue();
		tmv.maxHistEvents = assembly.intValue();
		tmv.maxEncdrs = assembly.intValue();
		tmv.maxMidnightEncdrReads = assembly.intValue();
		tmv.maxRateMins = assembly.intValue();
		tmv.maxSubIntvls = assembly.intValue();

		tmv.auxTablesImplemented = new TreeSet();
		ba = assembly.getBytes(32);
		for( int i = 0; i < 255; i ++ ) {
			Integer table = Integer.valueOf(i);
			if( ba.getBit(i) ) {
				tmv.auxTablesImplemented.add(table);
			}
		}

		tmv.industryNo = assembly.intValue();
		tmv.maxCartSize = assembly.intValue();
		tmv.cartMemSize = assembly.intValue();

		tmv.controlTables = new TreeSet();
		ba = assembly.getBytes(32);
		for( int i = 0; i < 255; i ++ ) {
			Integer table = Integer.valueOf(i);
			if( ba.getBit(i) ) {
				tmv.controlTables.add(table);
			}
		}

		tmv.maxExternalDisplayItems = assembly.intValue();
		tmv.maxHighSpeedEvents = assembly.intValue();
		tmv.maxHiLoDemands = assembly.intValue();
		tmv.maxDaysHiLoDemands = assembly.intValue();
		tmv.maxLogicOrs = assembly.intValue();
		tmv.maxLogicElements = assembly.intValue();
		tmv.maxTimers = assembly.intValue();
		tmv.maxFlipflops = assembly.intValue();
		tmv.maxEventScheds = assembly.intValue();
		tmv.maxAnalogOutputs = assembly.intValue();
		tmv.maxAnalogFilters = assembly.intValue();
		tmv.maxKompensations = assembly.intValue();
		tmv.maxKompTotalizers = assembly.intValue();
		tmv.maxKompFilters = assembly.intValue();
		tmv.maxKompConstants = assembly.intValue();
		tmv.lastMLine = assembly.intValue();
		tmv.maxPeerNumericOutputs = assembly.intValue();
		tmv.maxPeerNumericInputs = assembly.intValue();
		tmv.maxPeerStatusOutputs = assembly.intValue();
		tmv.maxPeerSenseInputs = assembly.intValue();
		tmv.maxConstants = assembly.intValue();
		tmv.maxNandAllocations = assembly.intValue();
		tmv.maxBussFilters = assembly.intValue();
		tmv.maxBussFilterLen = assembly.intValue();
		tmv.maxModemStrings = assembly.intValue();
		tmv.maxPeerAbsoluteOutputs = assembly.intValue();
		tmv.maxPeerAbsoluteInputs = assembly.intValue();

		return tmv;
	}

	/** Max mtr inputs allowed this SMD */
	int getMaxMtrInputs() {
		return maxMtrInputs;
	}

	/** Max no of external event inputs */
	int getMaxSenseInputs() {
		return maxSenseInputs;
	}

	/** Max totaliztion channels allowed */
	int getMaxTotalizations() {
		return maxTotalizations;
	}

	/** Max no of chnnls for intvl data */
	int getMaxStorageChnnls() {
		return maxStorageChnnls;
	}

	/** Max no of transform channels */
	int getMaxXforms() {
		return maxXforms;
	}

	/** Max no of rates calculated */
	int getMaxRates() {
		return maxRates;
	}

	/** Max no of summation regs/data blk */
	int getMaxSummations() {
		return maxSummations;
	}

	/** Max concurrent values saved */
	int getMaxConcValues() {
		return maxConcValues;
	}

	/** Max peaks saved, each data blk */
	int getMaxRatePeaks() {
		return maxRatePeaks;
	}

	/** Max no of load control functions */
	int getMaxLoadCntrls() {
		return maxLoadCntrls;
	}

	/** Max rating pd & totalization blks */
	int getMaxDataBlks() {
		return maxDataBlks;
	}

	/** Maximum gen. purpose relays */
	int getMaxOutputs() {
		return maxOutputs;
	}

	/** Maximum no. of events allowed in each TOU_DAILY_SCHED. */
	int getMaxDailyEvents() {
		return maxDailyEvents;
	}

	/** Max no. of daily scheds allowed */
	int getMaxDailyScheds() {
		return maxDailyScheds;
	}

	/** Max no. of seasons allowed in SMD */
	int getMaxSeasons() {
		return maxSeasons;
	}

	/** Max no. of dated events in table */
	int getMaxDateEvents() {
		return maxDateEvents;
	}

	/** The maximum no. of data blocks saved after a reset */
	int getMaxSelfReads() {
		return maxSelfReads;
	}

	/** The max no of display items which can be put into a display table */
	int getMaxDisplayItems() {
		return maxDisplayItems;
	}

	/** The max no of conditions which can cause an event dial in. */
	int getMaxDialInCnditns() {
		return maxDialInCnditns;
	}

	/** The max no of conditions which can cause a history message to be written. */
	int getMaxHistryMsgCnditns() {
		return maxHistryMsgCnditns;
	}

	/** The max no of error condition flags allotted for this device */
	int getMaxSmdErrors() {
		return maxSmdErrors;
	}

	/** The max no of k bytes available for interval data storage. */
	int getMaxStorK() {
		return maxStorK;
	}

	/** Maximum no. of history messages which can be stored */
	int getMaxHistEvents() {
		return maxHistEvents;
	}

	/** Maximum no. of encoders which can be attached to the SMD */
	int getMaxEncdrs() {
		return maxEncdrs;
	}

	/** Maximum no. of days of midnight encoder readings stored */
	int getMaxMidnightEncdrReads() {
		return maxMidnightEncdrReads;
	}

	/** Maximum No. of rate of consumption minimum peaks. */
	int getMaxRateMins() {
		return maxRateMins;
	}

	/** Maximum no. of SUB_INTVLS which can be rolled for 1 RATE_INTVL */
	int getMaxSubIntvls() {
		return maxSubIntvls;
	}

	/** 0 = Electric Utility Industry */
	int getIndustryNo() {
		return industryNo;
	}

	/***/
	int getMaxCartSize() {
		return maxCartSize;
	}

	/***/
	int getCartMemSize() {
		return cartMemSize;
	}

	/***/
	int getMaxExternalDisplayItems() {
		return maxExternalDisplayItems;
	}

	/***/
	int getMaxHighSpeedEvents() {
		return maxHighSpeedEvents;
	}

	/***/
	int getMaxHiLoDemands() {
		return maxHiLoDemands;
	}

	/***/
	int getMaxDaysHiLoDemands() {
		return maxDaysHiLoDemands;
	}

	/***/
	int getMaxLogicOrs() {
		return maxLogicOrs;
	}

	/***/
	int getMaxLogicElements() {
		return maxLogicElements;
	}

	/***/
	int getMaxTimers() {
		return maxTimers;
	}

	/***/
	int getMaxFlipflops() {
		return maxFlipflops;
	}

	/***/
	int getMaxEventScheds() {
		return maxEventScheds;
	}

	/** max number of analog output board channels */
	int getMaxAnalogOutputs() {
		return maxAnalogOutputs;
	}

	/** max analog filter size */
	int getMaxAnalogFilters() {
		return maxAnalogFilters;
	}

	/** max number of loss comp stages */
	int getMaxKompensations() {
		return maxKompensations;
	}

	/** max number of totalizers per stage */
	int getMaxKompTotalizers() {
		return maxKompTotalizers;
	}

	/** max number of totalizer filter stages */
	int getMaxKompFilters() {
		return maxKompFilters;
	}

	/** max number of constants sets per stage */
	int getMaxKompConstants() {
		return maxKompConstants;
	}

	/** Last M Buss line number */
	int getLastMLine() {
		return lastMLine;
	}

	/** Max number of peer to peer numeric outputs */
	int getMaxPeerNumericOutputs() {
		return maxPeerNumericOutputs;
	}

	/** Max number of peer to peer numeric inputs */
	int getMaxPeerNumericInputs() {
		return maxPeerNumericInputs;
	}

	/** Max number of peer to peer status outputs */
	int getMaxPeerStatusOutputs() {
		return maxPeerStatusOutputs;
	}

	/** Max number of peer to peer sense inputs */
	int getMaxPeerSenseInputs() {
		return maxPeerSenseInputs;
	}

	/** Max number table 55 constants */
	int getMaxConstants() {
		return maxConstants;
	}

	/** Max number allocatable NAND flash files */
	int getMaxNandAllocations() {
		return maxNandAllocations;
	}

	/** Max number type 2 buss filters */
	int getMaxBussFilters() {
		return maxBussFilters;
	}

	/** Max number of stages in type 2 buss filters */
	int getMaxBussFilterLen() {
		return maxBussFilterLen;
	}

	/** Max number of stages in type 2 buss filters */
	int getMaxModemStrings() {
		return maxModemStrings;
	}

	/** Max number of peer to peer absolute outputs */
	int getMaxPeerAbsoluteOutputs() {
		return maxPeerAbsoluteOutputs;
	}

	/** Max number of peer to peer absolute inputs */
	int getMaxPeerAbsoluteInputs() {
		return maxPeerAbsoluteInputs;
	}

	public String toString( ) {
		StringBuffer rslt = new StringBuffer();

		rslt.append( "TypeMaximumValues [\n" );

		rslt.append( "    tablesImplemented:" );
		Iterator i = tablesImplemented.iterator();
		while( i.hasNext()) {
			rslt.append(i.next().toString()).append(" ");
		}
		rslt.append( "\n" );

		rslt.append( "    auxTablesImplemented:" );
		i = auxTablesImplemented.iterator();
		while( i.hasNext()) {
			rslt.append(i.next().toString()).append(" ");
		}
		rslt.append( "\n" );

		rslt.append( "    controlTables:" );
		i = controlTables.iterator();
		while( i.hasNext()) {
			rslt.append(i.next().toString()).append(" ");
		}
		rslt.append( "\n" );

		rslt.append( "    maxMtrInputs=" + this.maxMtrInputs + "\n" );
		rslt.append( "    maxSenseInputs=" + this.maxSenseInputs + "\n" );
		rslt.append( "    maxTotalizations=" + this.maxTotalizations + "\n" );
		rslt.append( "    maxStorageChnnls=" + this.maxStorageChnnls + "\n" );
		rslt.append( "    maxXforms=" + this.maxXforms + "\n" );
		rslt.append( "    maxRates=" + this.maxRates + "\n" );
		rslt.append( "    maxSummations=" + this.maxSummations + "\n" );
		rslt.append( "    maxConcValues=" + this.maxConcValues + "\n" );
		rslt.append( "    maxRatePeaks=" + this.maxRatePeaks + "\n" );
		rslt.append( "    maxLoadCntrls=" + this.maxLoadCntrls + "\n" );
		rslt.append( "    maxDataBlks=" + this.maxDataBlks + "\n" );
		rslt.append( "    maxOutputs=" + this.maxOutputs + "\n" );
		rslt.append( "    maxDailyEvents=" + this.maxDailyEvents + "\n" );
		rslt.append( "    maxDailyScheds=" + this.maxDailyScheds + "\n" );
		rslt.append( "    maxSeasons=" + this.maxSeasons + "\n" );
		rslt.append( "    maxDateEvents=" + this.maxDateEvents + "\n" );
		rslt.append( "    maxSelfReads=" + this.maxSelfReads + "\n" );
		rslt.append( "    maxDisplayItems=" + this.maxDisplayItems + "\n" );
		rslt.append( "    maxDialInCnditns=" + this.maxDialInCnditns + "\n" );
		rslt.append( "    maxHistryMsgCnditns=" + this.maxHistryMsgCnditns + "\n" );
		rslt.append( "    maxSmdErrors=" + this.maxSmdErrors + "\n" );
		rslt.append( "    maxStorK=" + this.maxStorK + "\n" );
		rslt.append( "    maxHistEvents=" + this.maxHistEvents + "\n" );
		rslt.append( "    maxEncdrs=" + this.maxEncdrs + "\n" );
		rslt.append( "    maxMidnightEncdrReads=" + this.maxMidnightEncdrReads + "\n" );
		rslt.append( "    maxRateMins=" + this.maxRateMins + "\n" );
		rslt.append( "    maxSubIntvls=" + this.maxSubIntvls + "\n" );
		rslt.append( "    industryNo=" + this.industryNo + "\n" );
		rslt.append( "    maxCartSize=" + this.maxCartSize + "\n" );
		rslt.append( "    cartMemSize=" + this.cartMemSize + "\n" );
		rslt.append( "    maxExternalDisplayItems=" + this.maxExternalDisplayItems + "\n" );
		rslt.append( "    maxHighSpeedEvents=" + this.maxHighSpeedEvents + "\n" );
		rslt.append( "    maxHiLoDemands=" + this.maxHiLoDemands + "\n" );
		rslt.append( "    maxDaysHiLoDemands=" + this.maxDaysHiLoDemands + "\n" );
		rslt.append( "    maxLogicOrs=" + this.maxLogicOrs + "\n" );
		rslt.append( "    maxLogicElements=" + this.maxLogicElements + "\n" );
		rslt.append( "    maxTimers=" + this.maxTimers + "\n" );
		rslt.append( "    maxFlipflops=" + this.maxFlipflops + "\n" );
		rslt.append( "    maxEventScheds=" + this.maxEventScheds + "\n" );
		rslt.append( "    maxAnalogOutputs=" + this.maxAnalogOutputs + "\n" );
		rslt.append( "    maxAnalogFilters=" + this.maxAnalogFilters + "\n" );
		rslt.append( "    maxKompensations=" + this.maxKompensations + "\n" );
		rslt.append( "    maxKompTotalizers=" + this.maxKompTotalizers + "\n" );
		rslt.append( "    maxKompFilters=" + this.maxKompFilters + "\n" );
		rslt.append( "    maxKompConstants=" + this.maxKompConstants + "\n" );
		rslt.append( "    lastMLine=" + this.lastMLine + "\n" );
		rslt.append( "    maxPeerNumericOutputs=" + this.maxPeerNumericOutputs + "\n" );
		rslt.append( "    maxPeerNumericInputs=" + this.maxPeerNumericInputs + "\n" );
		rslt.append( "    maxPeerStatusOutputs=" + this.maxPeerStatusOutputs + "\n" );
		rslt.append( "    maxPeerSenseInputs=" + this.maxPeerSenseInputs + "\n" );
		rslt.append( "    maxConstants=" + this.maxConstants + "\n" );
		rslt.append( "    maxNandAllocations=" + this.maxNandAllocations + "\n" );
		rslt.append( "    maxBussFilters=" + this.maxBussFilters + "\n" );
		rslt.append( "    maxBussFilterLen=" + this.maxBussFilterLen + "\n" );
		rslt.append( "    maxModemStrings=" + this.maxModemStrings + "\n" );
		rslt.append( "    maxPeerAbsoluteOutputs=" + this.maxPeerAbsoluteOutputs + "\n" );
		rslt.append( "    maxPeerAbsoluteInputs=" + this.maxPeerAbsoluteInputs + "\n" );
		rslt.append( "]" );

		return rslt.toString();

	}

	String getManufacturer() {
		return manufacturer;
	}

	String getRevisionNumber() {
		return revisionNumber;
	}

	String getVersionNumber() {
		return versionNumber;
	}

}
