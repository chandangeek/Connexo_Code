/**
 * 
 */
package com.energyict.protocolimpl.iec1107.instromet.dl220.registers;

import com.energyict.cbo.Quantity;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.IntervalData;
import com.energyict.protocol.IntervalValue;
import com.energyict.protocol.NoSuchRegisterException;
import com.energyict.protocol.RegisterValue;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.instromet.dl220.Archives;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220;
import com.energyict.protocolimpl.iec1107.instromet.dl220.DL220Utils;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.ClockObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.objects.DLObject;
import com.energyict.protocolimpl.iec1107.instromet.dl220.profile.DL220Profile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Basic functionality for Register reading support
 * 
 * @author gna
 * @since 22-mrt-2010
 * 
 */
public class DL220ObisCodeMapper {

	/** The used {@link DL220} */
	private final DL220 dl220;
	/** The billingProfile */
	private DL220Profile profile;

	/**
	 * Constructor with the {@link ProtocolLink}
	 * 
	 * @param dl220
	 *            - the used {@link DL220}
	 */
	public DL220ObisCodeMapper(DL220 dl220) {
		this.dl220 = dl220;
	}

	/**
	 * Read the {@link RegisterValue} from the device
	 * 
	 * @param obisCode
	 *            - the obisCode to read
	 * 
	 * @return the {@link RegisterValue}
	 * @throws IOException
	 */
	public RegisterValue getRegisterValue(ObisCode obisCode) throws IOException {
		ObisCode original = obisCode;
		/* Check if it is a billing value */
		int billingPoint = -1;

		// obis F code
		if ((obisCode.getF() >= 0) && (obisCode.getF() <= 99)) {
			billingPoint = obisCode.getF();
			obisCode = new ObisCode(obisCode, this.dl220.getMeterIndex()+1, billingPoint);
		} else if ((obisCode.getF() <= 0) && (obisCode.getF() >= -99)) {
			billingPoint = obisCode.getF() * -1;
			obisCode = new ObisCode(obisCode, this.dl220.getMeterIndex()+1, billingPoint);
		} else if (obisCode.getF() == 255) {
			billingPoint = -1;
		} else {
			throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the protocol");
		}
		
		ObisCode adjustedOC = convertObisCodeToValidWithMeterIndex(obisCode);
		RegisterValue rv;

		if(billingPoint == -1){
			
			if(DL220Registers.containsMaxDemandRegister(adjustedOC)){
				try {
					DL220Registers dlRegister = DL220Registers.forObisCode(adjustedOC);
					DLObject object = DLObject.constructObject(dl220, dlRegister.getAddress());
					String rawValue = object.readRawValue(dlRegister.getInstanceID());
					String valueAndUnit = DL220Utils.getTextBetweenBracketsFromIndex(rawValue, 0);
					Date eventTime = ClockObject.parseCalendar(DL220Utils.getTextBetweenBracketsFromIndex(rawValue, 1), dl220.getTimeZone()).getTime();
					BigDecimal bd = new BigDecimal(valueAndUnit.split(DLObject.ASTERISK)[DLObject.valueIndex]);
					rv = new RegisterValue(original, new Quantity(bd, DL220Utils.getUnitFromString(valueAndUnit.split(DLObject.ASTERISK)[DLObject.unitIndex])), eventTime);
				} catch (IOException e) {
					throw new NoSuchRegisterException("Failed while reading register with ObisCode " + obisCode);
				}
			} else if (DL220Registers.contains(adjustedOC)) {
				try {
					DL220Registers dlRegister = DL220Registers.forObisCode(adjustedOC);
					DLObject object = DLObject.constructObject(dl220, dlRegister.getAddress());
					String valueWithUnit = object.getValue(dlRegister.getInstanceID());
					BigDecimal bd = new BigDecimal(valueWithUnit.split(DLObject.ASTERISK)[DLObject.valueIndex]);
					rv = new RegisterValue(original, new Quantity(bd, DL220Utils.getUnitFromString(valueWithUnit
							.split(DLObject.ASTERISK)[DLObject.unitIndex])), new Date());
				} catch (IOException e) {
					throw new NoSuchRegisterException("Failed while reading register with ObisCode " + obisCode);
				}
			} else {
				dl220.getLogger().info("ObisCode " + obisCode.toString() + " is not supported by the PROTOCOL.");
				throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the protocol.");
			}
			
		}else {
			/*
			 * The BillingPoints are daily values
			 */

			if ((adjustedOC.getA() == 8 || adjustedOC.getA() == 9)
					&& (adjustedOC.getB() == 1 || adjustedOC.getB() == 2) && adjustedOC.getC() == 1
					&& adjustedOC.getD() == 1 && adjustedOC.getE() == 0){
				Calendar cal = Calendar.getInstance(this.dl220.getTimeZone());
				cal.add(Calendar.DAY_OF_MONTH, -(billingPoint + 1));	// normally the last billingpoint comes first
				Date fromDate = cal.getTime();
				cal.add(Calendar.DAY_OF_MONTH, 1);	// add one day
				Date toDate = cal.getTime();
				IntervalData id = getBillingInterval(fromDate, toDate, billingPoint, original);
				IntervalValue iv = (IntervalValue)id.getIntervalValues().get(0);			// we take the first value, we assume it is the one you want
				rv = new RegisterValue(original, new Quantity(iv.getNumber(), getProfileObject().getValueUnit()), id.getEndTime());
			} else {
				dl220.getLogger().info("ObisCode " + obisCode.toString() + " is not supported by the PROTOCOL.");
				throw new NoSuchRegisterException("ObisCode " + obisCode.toString() + " is not supported by the protocol.");
			}
		}
		return rv;
	}
	
	/**
	 * Get the {@link IntervalData} record that contains the billingValue
	 * 
	 * @param from 
	 * 			- the FromDate to read the profile
	 * 
	 * @param to
	 * 			- the Date to end the billingPeriod
	 * 
	 * @param billingPoint
	 * 			- the requested billingpoint
	 * 
	 * @param obisCode
	 * 			- the handling obisCode (just for logging)
	 * 
	 * @return the {@link IntervalData} with the requested billingValue
	 * 
	 * @throws IOException if the billingValue doesn't exist yet or something went wrong during the read of the profile
	 */
	private IntervalData getBillingInterval(Date from, Date to, int billingPoint, ObisCode obisCode) throws IOException{
		IntervalData id = intervalListAlreadyContainsBillingPoint(from, to);
		
		if(id != null){
			return id;
		} else {
			List<IntervalData> intervalList = getProfileObject().getIntervalData(from, new Date());
			
			if(intervalList.size() != 0){
				if(billingPoint > intervalList.size()){
					dl220.getLogger().info("Register with ObisCode " + obisCode + " is not yet available in the meter.(Billingpoint does not exist yet)");
					throw new NoSuchRegisterException("Register with ObisCode " + obisCode + " is not yet available in the meter.(Billingpoint does not exist yet)");
				}
				return intervalList.get(intervalList.size() - 1 - billingPoint);	// we take the last value, which is normally the last updated value
			} else {
				dl220.getLogger().info("Register with ObisCode " + obisCode + " is not available in the device(possibly billing point doesn't exist)");
				throw new NoSuchRegisterException("Register with ObisCode " + obisCode + " is not available in the device(possibly billing point doesn't exist)");
			}
		}
	}
	
	/**
	 * Check if the intervalList already contains the billingPoint between the requested date
	 * 
	 * @param from
	 * 			- the start of the billingPeriod
	 * 
	 * @param to
	 * 			- the end of the billingPeriod
	 * 
	 * @return null if the billingPeriod doesn't exist yet, or the {@link IntervalData} containing the billingPeriod
	 */
	private IntervalData intervalListAlreadyContainsBillingPoint(Date from, Date to){
		for(IntervalData id : getProfileObject().getIntervalList()){
			if(id.getEndTime().compareTo(from) > 0 && id.getEndTime().compareTo(to) < 0){
				return id;
			}
		}
		return null;
	}

	/**
	 * Change the B-channel of the {@link ObisCode} to the meterIndex.<br>
	 * Also change the A field to a 'cold-water-obiscode', meaning 8
	 * 
	 * @param oc
	 *            - the given ObisCode
	 * 
	 * @return the <i>obiscode</i> with an adjusted A-/B-channel (1 or 2)
	 * @throws NoSuchRegisterException
	 *             if the obiscode is not a Hot- or ColdWater ObisCode
	 */
	private ObisCode convertObisCodeToValidWithMeterIndex(ObisCode oc) throws NoSuchRegisterException {
		if (oc.getA() == 8 || oc.getA() == 9) {
			ObisCode obisCode = new ObisCode(oc.getA(), dl220.getMeterIndex() + 1, oc.getC(), oc.getD(), oc.getE(), oc
					.getF());
			return obisCode;
		} else {
			dl220.getLogger().info("ObisCode " + oc.toString() + " is not supported by the PROTOCOL.");
			throw new NoSuchRegisterException("ObisCode " + oc + " is not supported by the protocol.");
		}
	}

	/**
	 * @return the {@link DL220Profile}
	 */
	protected DL220Profile getProfileObject() {
		if (this.profile == null) {
			this.profile = new DL220Profile(this.dl220, this.dl220.getMeterIndex(),
					(this.dl220.getMeterIndex() == 0) ? Archives.DAILY1 : Archives.DAILY2,
							dl220.getProfileRequestBlockSize());
		}
		return this.profile;
	}

}
