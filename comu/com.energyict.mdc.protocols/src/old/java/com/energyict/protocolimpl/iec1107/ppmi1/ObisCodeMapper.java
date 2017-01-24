package com.energyict.protocolimpl.iec1107.ppmi1;

import com.energyict.mdc.protocol.api.NoSuchRegisterException;
import com.energyict.mdc.protocol.api.device.data.RegisterInfo;
import com.energyict.mdc.protocol.api.device.data.RegisterValue;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Quantity;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.ppmi1.register.HistoricalData;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MainRegister;
import com.energyict.protocolimpl.iec1107.ppmi1.register.MaximumDemand;
import com.energyict.protocolimpl.iec1107.ppmi1.register.RegisterInformation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * ObisCodes
 *
 * Value group A - Electricity related objects
 * Value group B - Channel number (1-255)
 *
 * Value group C - Abstract of physical data items related to information
 * source
 * == 1  Active Power+
 * == 2  Active Power-
 * == 3  Reactive Power+
 * == 4  Reactive Power-
 * == 9  Apparent Power
 *
 * Value group D, Defines types
 * == 2  cumulative maximum
 * == 6  maximum 1 MD
 * == 8  time integral 1 TOTAL and RATE
 *
 * Value group E
 * Defines the Rate register.
 * == 0, no Rate register (TOU) so just the TOTAL
 * == 1-8, Rate register 1-8
 *
 * Value group F
 * - defines storage of Data
 * - value 0-255, (255 if not used)
 * - specifies the allocation of billing periods (historical values)
 * == 255 Not used
 * == 0 becomes 1
 * == 1 becomes 2
 * == ...
 *
 *
 * Power in AC circuits:
 *
 * - Apparent power
 * The product of voltage and current.  Expressed in Volt-Amps (VA).  Apparent
 * power is the easist to measure and is the vector sum of real power and
 * reactive power.
 * - Real power
 * The time avergage of the instantaneous product of voltage and current.
 * Expressed in Watts (W).  Real power can only be consumed in the resistive
 * part of the load, where the current is in phase with the voltage.
 * - Reactive power
 * The time avergage of the instantaneous product of the voltage and current,
 * with current phasshifted by 90°.  Expressed in Volt-Amps reactive (VAr).
 * Reactive power, while still drawing a current from the supply, is not
 * actually consuming any power at all - one way of describing reactive power
 * is to use the term  &quot;wattless watts&quot; since it can draw 1A of
 * current at 1V but not produce 1W of heat.
 *
 * </pre>
 *
 * 16/03/2005 added optimisation: if obiscode x.x.x.x.x.VZ is used, then use
 * register 541 (last billing) instead of 540.
 *
 * @author fbo
 */

public class ObisCodeMapper {

	private RegisterFactory rFactory;
	private RegisterInformation ri = new RegisterInformation();

	/**
	 * Dangerous stuff (using instance variables with a static method:
	 * getRegisterInfo). Make sure only 2 methods are public!
	 */

	//private Date billingDate = null; // KV 22072005 billingDate is never created
	private MetaRegister sourceRegister = null;
	private MetaRegister derivedRegister = null;
	//private int registerIndex;// KV22072005 unused code
	private int billingPoint;

	/** Manufacturer specific codes */
	public static final int CODE_E_REGISTER_1=128;
	public static final int CODE_E_REGISTER_2=129;
	public static final int CODE_E_REGISTER_3=130;
	public static final int CODE_E_REGISTER_4=131;

	private static final long	MS_PER_SECOND	= 1000;

	private ObisCodeMapper( ){
	}

	public ObisCodeMapper(RegisterInformation ri) throws IOException {
		this.ri = ri;
	}

	public ObisCodeMapper(RegisterFactory registerFactory) throws IOException {
		this.rFactory = registerFactory;
		this.ri = registerFactory.getRegisterInformation();
	}

	private ObisCodeMapper(ObisCode obisCode) throws NoSuchRegisterException {
		this.getMapRegister(obisCode);
		this.getBillingPoint(obisCode);
	}

	public static RegisterInfo getRegisterInfo(ObisCode obisCode) throws IOException {
		ObisCodeMapper ocm = new ObisCodeMapper(obisCode);
		String desc = ocm.toString( obisCode );
		return new RegisterInfo(desc);
	}

	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {

		this.getBillingPoint(obisCode);

		if( isBillingDate(obisCode ) ) {
			HistoricalData hd = null;
			hd = this.rFactory.getHistoricalData().get(this.billingPoint);
			Date date = hd.getDate();
			Unit secondsUnit = Unit.get( BaseUnit.SECOND );
			Long sl = new Long( date.getTime() / MS_PER_SECOND );
			Quantity seconds = new Quantity( sl, secondsUnit );
			return new RegisterValue( obisCode, seconds,  date );
		}

		this.getMapRegister(obisCode);
		String key = getRegisterFactoryKey();

		if (key == null) {
			throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
					+ " is not supported!");
		}

		Object o = null;
		Date toDate = null;
		Date eventDate = null;

		if (this.rFactory != null) {

			if( obisCode.getD() == 2 ) {
				return getCMD(obisCode);
			}
			if( obisCode.getD() == 6 ) {
				return getMD(obisCode);
			}

			if (this.billingPoint == -1) {
				o = this.rFactory.getRegister(key);
				toDate = new Date();
			} else {
				HistoricalData hd = null;
				hd = this.rFactory.getHistoricalData().get(this.billingPoint);


				if( hd == null ) {
					return null;
				}

				o = hd.get(key);
				toDate = hd.getDate();
				eventDate = hd.getDate();

			}

		}

		if (o instanceof MainRegister) {
			return ((MainRegister) o).toRegisterValue(obisCode, eventDate, toDate);
		}
		if (o instanceof MaximumDemand) {
			return ((MaximumDemand) o).toRegisterValue(obisCode, toDate);
		}

		return new RegisterValue(obisCode);

	}

	/* Return the RegisterFactoryKey that identifies the register.  If it
	 * concerns a simple (cumulative|primary) register, just return
	 * that RegisterFactoryKey, if it is a derived register, return that key.
	 */
	private String getRegisterFactoryKey() {
		if (this.derivedRegister == null) {
			return this.sourceRegister.getRegisterFactoryKey();
		} else {

			if (this.derivedRegister.getSourceRegister() != null) {

				if (this.ri.isCMDRegister(this.derivedRegister) && this.derivedRegister.getSourceRegister().equals(this.sourceRegister)) {
					return this.derivedRegister.getRegisterFactoryKey();
				}

				if (this.ri.isMDRegister(this.derivedRegister) && this.derivedRegister.getSourceRegister().equals(this.sourceRegister)) {
					return this.derivedRegister.getRegisterFactoryKey();
				}

				if (this.ri.isTouRegister(this.derivedRegister) && this.derivedRegister.getSourceRegister().equals(this.sourceRegister)) {
					return this.derivedRegister.getRegisterFactoryKey();
				}
			}

		}

		return null;
	}

	/* This is a big chunk of mapping/translation */

	/* In the case of energy: map the obiscode to the MetaRegister */
	private Map energyMap = new HashMap() {
		{
			put(Integer.valueOf(ObisCode.CODE_C_ACTIVE_IMPORT), ObisCodeMapper.this.ri.getImportWh());
			put(Integer.valueOf(ObisCode.CODE_C_ACTIVE_EXPORT), ObisCodeMapper.this.ri.getExportWh());
			put(Integer.valueOf(ObisCode.CODE_C_REACTIVE_IMPORT), ObisCodeMapper.this.ri.getImportVarh());
			put(Integer.valueOf(ObisCode.CODE_C_REACTIVE_EXPORT), ObisCodeMapper.this.ri.getExportVarh());
			put(Integer.valueOf(ObisCode.CODE_C_APPARENT), ObisCodeMapper.this.ri.getVAh());
		}
	};

	private MetaRegister getEnergy(ObisCode o) {
		return (MetaRegister) this.energyMap.get(Integer.valueOf(o.getC()));
	}

	/* In the case of power: map the obiscode to the MetaRegister */
	private Map powerMap = new HashMap() {
		{
			put(Integer.valueOf(ObisCode.CODE_C_ACTIVE_IMPORT), ObisCodeMapper.this.ri.getImportW());
			put(Integer.valueOf(ObisCode.CODE_C_ACTIVE_EXPORT), ObisCodeMapper.this.ri.getExportW());
			put(Integer.valueOf(ObisCode.CODE_C_REACTIVE_IMPORT), ObisCodeMapper.this.ri.getImportVar());
			put(Integer.valueOf(ObisCode.CODE_C_REACTIVE_EXPORT), ObisCodeMapper.this.ri.getExportVar());
			put(Integer.valueOf(ObisCode.CODE_C_APPARENT), ObisCodeMapper.this.ri.getVA());
		}
	};

	private MetaRegister getPower(ObisCode o) {
		return (MetaRegister) this.powerMap.get(Integer.valueOf(o.getC()));
	}

	/* sourceRegister and derivedRegister are mapped */
	private void getMapRegister(ObisCode obisCode)
	throws NoSuchRegisterException {

		if( isBillingDate( obisCode ) ) {
			return ;
		}

		// KV22072005 unused code
		//int e = registerIndex = obisCode.getE();
		int e = obisCode.getE();

		// Step 3 :: What does code D say ?
		switch (obisCode.getD()) {

		case ObisCode.CODE_D_CUMULATIVE_MAXUMUM_DEMAND : // C_MX_DMD (o=2)
			this.sourceRegister = getPower(obisCode);
			if( e == 0 ) {
				this.derivedRegister = this.ri.findCMDRegisterFor(this.sourceRegister);
			} else if ( e == CODE_E_REGISTER_1 ) {
				this.derivedRegister = this.ri.getCmdRegister()[ 0 ];
			} else if ( e == CODE_E_REGISTER_2 ) {
				this.derivedRegister = this.ri.getCmdRegister()[ 1 ];
			} else if ( e == CODE_E_REGISTER_3 ) {
				this.derivedRegister = this.ri.getCmdRegister()[ 2 ];
			} else if ( e == CODE_E_REGISTER_4 ) {
				this.derivedRegister = this.ri.getCmdRegister()[ 3 ];
			}

			//derivedRegister = (e == 0 | e > ri.cmdRegister.length ) ? null : ri.cmdRegister[e - 1];
			break;

		case ObisCode.CODE_D_MAXIMUM_DEMAND : // MX_DMD (o=6)
			this.sourceRegister = getPower(obisCode);
			if( e == 0 ) {
				this.derivedRegister = this.ri.findMDRegisterFor(this.sourceRegister);
			} else if ( e == CODE_E_REGISTER_1 ) {
				this.derivedRegister = this.ri.getMdRegister()[ 0 ];
			} else if ( e == CODE_E_REGISTER_2 ) {
				this.derivedRegister = this.ri.getMdRegister()[ 1 ];
			} else if ( e == CODE_E_REGISTER_3 ) {
				this.derivedRegister = this.ri.getMdRegister()[ 2 ];
			} else if ( e == CODE_E_REGISTER_4 ) {
				this.derivedRegister = this.ri.getMdRegister()[ 3 ];
			}

			//derivedRegister = (e == 0 | e > ri.mdRegister.length ) ? null : ri.mdRegister[e - 1];
			break;

		case ObisCode.CODE_D_TIME_INTEGRAL : // TOU (o=8)
			this.sourceRegister = getEnergy(obisCode);
			this.derivedRegister = ((e == 0) | (e > this.ri.getTouRegister().length)) ? null : this.ri.getTouRegister()[e - 1];
			break;


		default :
			throw new NoSuchRegisterException("ObisCode "
					+ obisCode.toString() + " is not supported!");
		}

	}

	void getBillingPoint(ObisCode obisCode) throws NoSuchRegisterException {

		if (obisCode.getF() == 255) {
			this.billingPoint = -1;
		} else if ((obisCode.getF() >= 0) && (obisCode.getF() <= 3)) {
			this.billingPoint = obisCode.getF();
		} else if ((obisCode.getF() < 0) && (obisCode.getF() >= -3)) {
			this.billingPoint = obisCode.getF() * -1;
		} else {
			throw new NoSuchRegisterException("ObisCode " + obisCode.toString()
					+ " is not supported!");
		}
	}

	private boolean isBillingDate( ObisCode obisCode ){
		return ( obisCode.getC() == 0 ) && ( obisCode.getE() == 2 );
	}

	/*
	 * For making a nice descriptive toString(), the MetaRegisters are mapped
	 * onto a special string, that makes sense in an Obiscode-context.
	 */
	public String toString( ObisCode obisCode ) {
		String result = "";

		// KV 22072005 billingDate is never created
		//        if ( billingDate != null )
		//            result += "billing date ";

		if (this.sourceRegister != null) {
			result += this.metaRegToString.get(this.sourceRegister).toString();
		}

		if (this.derivedRegister != null) {
			result += ", " + this.metaRegToString.get(this.derivedRegister).toString();
		} else {
			if( (obisCode.getE() == 0) && (obisCode.getD() == 2) ) {
				result += ", " + this.metaRegToString.get(this.ri.getCmdTou1()).toString();
			}
			if( (obisCode.getE() == 0) && (obisCode.getD() == 6) ) {
				result += ", " + this.metaRegToString.get(this.ri.getMdTou1()).toString();
			}
		}

		if (this.billingPoint != -1) {
			result += ", billingpoint " + (this.billingPoint);
		} else {
			result += ", current value";
		}

		return result;
	}

	/* String representation of the registers */
	private Map metaRegToString = new HashMap() {
		{
			put(ObisCodeMapper.this.ri.getImportWh(), "Energy, Active import");
			put(ObisCodeMapper.this.ri.getExportWh(), "Energy, Active export");
			put(ObisCodeMapper.this.ri.getImportVarh(), "Energy, Reactive import");
			put(ObisCodeMapper.this.ri.getExportVarh(), "Energy, Reactive export");
			put(ObisCodeMapper.this.ri.getVAh(), "Energy, Apparent import");

			put(ObisCodeMapper.this.ri.getImportW(), "Power, Active import");
			put(ObisCodeMapper.this.ri.getExportW(), "Power, Active export");
			put(ObisCodeMapper.this.ri.getImportVar(), "Power, Reactive import");
			put(ObisCodeMapper.this.ri.getExportVar(), "Power, Reactive export");
			put(ObisCodeMapper.this.ri.getVA(), "Power, Apparent import");

			put(ObisCodeMapper.this.ri.getTou1(), "Tarif reg. 1");
			put(ObisCodeMapper.this.ri.getTou2(), "Tarif reg. 2");
			put(ObisCodeMapper.this.ri.getTou3(), "Tarif reg. 3");
			put(ObisCodeMapper.this.ri.getTou4(), "Tarif reg. 4");
			put(ObisCodeMapper.this.ri.getTou5(), "Tarif reg. 5");
			put(ObisCodeMapper.this.ri.getTou6(), "Tarif reg. 6");
			put(ObisCodeMapper.this.ri.getTou7(), "Tarif reg. 7");
			put(ObisCodeMapper.this.ri.getTou8(), "Tarif reg. 8");

			put(ObisCodeMapper.this.ri.getMdTou1(), "Maximum Demand");
			put(ObisCodeMapper.this.ri.getMdTou2(), "Maximum Demand");
			put(ObisCodeMapper.this.ri.getMdTou3(), "Maximum Demand");
			put(ObisCodeMapper.this.ri.getMdTou4(), "Maximum Demand");

			put(ObisCodeMapper.this.ri.getCmdTou1(), "Cumulative Maximum Demand");
			put(ObisCodeMapper.this.ri.getCmdTou2(), "Cumulative Maximum Demand");
			put(ObisCodeMapper.this.ri.getCmdTou3(), "Cumulative Maximum Demand");
			put(ObisCodeMapper.this.ri.getCmdTou4(), "Cumulative Maximum Demand");

		}
	};

	/** Getting Maximum Demands.
	 *
	 */
	private RegisterValue getMD( ObisCode obisCode ) throws IOException {

		if( obisCode.getE() == 0 ) {

			List l = this.ri.findAllMDRegistersFor( this.sourceRegister );

			if( this.billingPoint == -1 ) {

				Date d = new Date();
				ArrayList r = new ArrayList();

				if( l.contains( this.ri.getMdTou1() )  ) {
					r.add( this.rFactory.getMaximumDemand1().toRegisterValue( obisCode, d ) );
				}
				if( l.contains( this.ri.getMdTou2() ) ) {
					r.add( this.rFactory.getMaximumDemand2().toRegisterValue( obisCode, d ) );
				}
				if( l.contains( this.ri.getMdTou3() ) ) {
					r.add( this.rFactory.getMaximumDemand3().toRegisterValue( obisCode, d ) );
				}
				if( l.contains( this.ri.getMdTou4() ) ) {
					r.add( this.rFactory.getMaximumDemand4().toRegisterValue( obisCode, d ) );
				}

				return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );

			} else if((this.billingPoint >= 0) && (this.billingPoint < 4)) {

				HistoricalData hd = this.rFactory.getHistoricalData().get(this.billingPoint);

				if( hd == null ) {
					return null;
				}

				Date d = hd.getDate();
				ArrayList r = new ArrayList();

				if( l.contains( this.ri.getMdTou1() )  ) {
					r.add( hd.getMaxDemand1().toRegisterValue( obisCode, d ) );
				}
				if( l.contains( this.ri.getMdTou2() ) ) {
					r.add( hd.getMaxDemand2().toRegisterValue( obisCode, d ) );
				}
				if( l.contains( this.ri.getMdTou3() ) ) {
					r.add( hd.getMaxDemand3().toRegisterValue( obisCode, d ) );
				}
				if( l.contains( this.ri.getMdTou4() ) ) {
					r.add( hd.getMaxDemand4().toRegisterValue( obisCode, d ) );
				}

				return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );


			}

		} else {

			if( this.billingPoint == -1 ) {

				Date d = new Date();
				//ArrayList r = new ArrayList();// KV 22072005 unused code

				if( obisCode.getE() == CODE_E_REGISTER_1 ) {
					return this.rFactory.getMaximumDemand1().toRegisterValue( obisCode, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_2 ) {
					return this.rFactory.getMaximumDemand2().toRegisterValue( obisCode, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_3 ) {
					return this.rFactory.getMaximumDemand3().toRegisterValue( obisCode, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_4 ) {
					return this.rFactory.getMaximumDemand4().toRegisterValue( obisCode, d );
				}

			} else if((this.billingPoint >= 0) && (this.billingPoint < 4)) {

				HistoricalData hd = this.rFactory.getHistoricalData().get(this.billingPoint);

				if( hd == null ) {
					return null;
				}

				Date d = hd.getDate();
				//ArrayList r = new ArrayList();// KV 22072005 unused code

				if( obisCode.getE() == CODE_E_REGISTER_1 ) {
					return hd.getMaxDemand1().toRegisterValue( obisCode, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_2 ) {
					return hd.getMaxDemand2().toRegisterValue( obisCode, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_3 ) {
					return hd.getMaxDemand3().toRegisterValue( obisCode, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_4 ) {
					return hd.getMaxDemand4().toRegisterValue( obisCode, d );
				}

			}

		}

		throw new
		NoSuchRegisterException( "ObisCode " + obisCode.toString()
				+ " is not supported!");

	}


	private RegisterValue getCMD( ObisCode obisCode ) throws IOException {

		if( obisCode.getE() == 0 ) {

			List l = this.ri.findAllCMDRegistersFor( this.sourceRegister );

			if( this.billingPoint == -1 ) {

				Date d = new Date();
				ArrayList r = new ArrayList();

				if( l.contains( this.ri.getCmdTou1() )  ) {
					r.add( this.rFactory.getCumulativeMaximumDemand1().toRegisterValue( obisCode, d, d ) );
				}
				if( l.contains( this.ri.getCmdTou2() ) ) {
					r.add( this.rFactory.getCumulativeMaximumDemand2().toRegisterValue( obisCode, d, d ) );
				}
				if( l.contains( this.ri.getCmdTou3() ) ) {
					r.add( this.rFactory.getCumulativeMaximumDemand3().toRegisterValue( obisCode, d, d ) );
				}
				if( l.contains( this.ri.getCmdTou4() ) ) {
					r.add( this.rFactory.getCumulativeMaximumDemand4().toRegisterValue( obisCode, d, d ) );
				}

				return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );

			} else if((this.billingPoint >= 0) && (this.billingPoint < 4)) {

				HistoricalData hd = this.rFactory.getHistoricalData().get(this.billingPoint);

				if( hd == null ) {
					return null;
				}

				Date d = hd.getDate();
				ArrayList r = new ArrayList();

				if( l.contains( this.ri.getCmdTou1() )  ) {
					r.add( hd.getCumulativeMaxDemand1().toRegisterValue( obisCode, d, d ) );
				}
				if( l.contains( this.ri.getCmdTou2() ) ) {
					r.add( hd.getCumulativeMaxDemand2().toRegisterValue( obisCode, d, d ) );
				}
				if( l.contains( this.ri.getCmdTou3() ) ) {
					r.add( hd.getCumulativeMaxDemand3().toRegisterValue( obisCode, d, d ) );
				}
				if( l.contains( this.ri.getCmdTou4() ) ) {
					r.add( hd.getCumulativeMaxDemand4().toRegisterValue( obisCode, d, d ) );
				}

				return getMax( (RegisterValue[])r.toArray( new RegisterValue[0] ) );

			}

		} else {

			if( this.billingPoint == -1 ) {

				Date d = new Date();
				//ArrayList r = new ArrayList();// KV 22072005 unused code

				if( obisCode.getE() == CODE_E_REGISTER_1 ) {
					return this.rFactory.getCumulativeMaximumDemand1().toRegisterValue( obisCode, d, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_2 ) {
					return this.rFactory.getCumulativeMaximumDemand2().toRegisterValue( obisCode, d, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_3 ) {
					return this.rFactory.getCumulativeMaximumDemand3().toRegisterValue( obisCode, d, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_4 ) {
					return this.rFactory.getCumulativeMaximumDemand4().toRegisterValue( obisCode, d, d );
				}

			} else if((this.billingPoint >= 0) && (this.billingPoint < 4)) {

				HistoricalData hd = this.rFactory.getHistoricalData().get(this.billingPoint);

				if( hd == null ) {
					return null;
				}

				Date d = hd.getDate();
				//ArrayList r = new ArrayList();// KV 22072005 unused code

				if( obisCode.getE() == CODE_E_REGISTER_1 ) {
					return hd.getCumulativeMaxDemand1().toRegisterValue( obisCode, d, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_2 ) {
					return hd.getCumulativeMaxDemand2().toRegisterValue( obisCode, d, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_3 ) {
					return hd.getCumulativeMaxDemand3().toRegisterValue( obisCode, d, d );
				}
				if( obisCode.getE() == CODE_E_REGISTER_4 ) {
					return hd.getCumulativeMaxDemand4().toRegisterValue( obisCode, d, d );
				}

			}

		}

		throw new
		NoSuchRegisterException( "ObisCode " + obisCode.toString()
				+ " is not supported!");

	}

	/* Return the biggest RegisterValue out of an array */
	private RegisterValue getMax( RegisterValue[] values ){
		RegisterValue max = values[0];
		for( int i = 1; i < values.length; i ++ ) {
			if( values[i].getQuantity().compareTo( max.getQuantity() ) > 0 ) {
				max = values[i];
			}
		}
		return max;
	}

}
