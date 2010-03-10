package com.energyict.protocolimpl.dlms.as220;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.UnsupportedException;
import com.energyict.protocolimpl.utils.ProtocolTools;

/**
 * @author jme
 */
public class AS220StoredValues implements StoredValues {

	public static final ObisCode		MONTHLY_OBISCODE	= ObisCode.fromString("0.0.98.1.0.255");
	public static final ObisCode		DAILY_OBISCODE		= ObisCode.fromString("1.0.99.2.0.255");

	private final ObisCode				obisCode;
	private final CosemObjectFactory	cosemObjectFactory;

	private ProfileGeneric				profileGeneric		= null;
	private List<ObisCode>				capturedCodes		= null;
	private Array						dataArray			= null;

	public AS220StoredValues(ObisCode obisCode, CosemObjectFactory cosemObjectFactory) throws IOException {
		this.obisCode = obisCode;
		this.cosemObjectFactory = cosemObjectFactory;
	}

	public ObisCode getObisCode() {
		return obisCode;
	}

	public CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}

	public List<ObisCode> getCapturedCodes() {
		if (capturedCodes == null) {
			this.capturedCodes = new ArrayList<ObisCode>();
			try {
				for (CapturedObject co : getProfileGeneric().getCaptureObjects()) {
					if (co.getClassId() != DLMSClassId.CLOCK.getClassId()) {
						capturedCodes.add(co.getLogicalName().getObisCode());
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return capturedCodes;
	}

	public Array getDataArray() {
		if (dataArray == null) {
			try {
				dataArray = new Array(getProfileGeneric().getBufferData(), 0, 0);
			} catch (IOException e) {
				dataArray = null;
			}
		}
		return dataArray;
	}

	public int getBillingPointCounter() throws IOException {
		return getDataArray().nrOfDataTypes();
	}

	public Date getBillingPointTimeDate(int billingPoint) throws IOException {
		if (!isValidBillingPoint(billingPoint)) {
			throw new UnsupportedException("Billing point [" + billingPoint + "] doesn't exist.");
		}
		return getDataArray().getDataType(billingPoint).getStructure().getDataType(0).getOctetString().getDateTime(TimeZone.getDefault()).getValue().getTime();
	}

	public HistoricalValue getHistoricalValue(ObisCode obisCode) throws IOException {
		ObisCode baseObisCode = ProtocolTools.setObisCodeField(obisCode, 5, (byte) 255);

		int billingPoint = obisCode.getF();
		if (billingPoint > 0) {
			billingPoint--;
		} else if (billingPoint <= 0) {
			billingPoint = (getBillingPointCounter() - 1) + billingPoint;
		}

		if (!isValidBillingPoint(billingPoint)) {
			throw new UnsupportedException("Billing point " + billingPoint + " doesn't exist for obiscode " + baseObisCode + ".");
		}

		BigDecimal value = getValue(baseObisCode, billingPoint);
		Date billingPointTimeDate = getBillingPointTimeDate(billingPoint);
		Register reg = getCosemObjectFactory().getRegister(baseObisCode);
		Unit unit = reg.getQuantityValue().getUnit();
		ScalerUnit scalerUnit = reg.getScalerUnit();

		HistoricalRegister historicalRegister = new HistoricalRegister();
		historicalRegister.setBillingDate(billingPointTimeDate);
		historicalRegister.setCaptureTime(new Date());
		historicalRegister.setQuantityValue(value, unit);
		historicalRegister.setScalerUnit(scalerUnit);

		return new HistoricalValue(historicalRegister, billingPointTimeDate, getProfileGeneric().getResetCounter());
	}

	private BigDecimal getValue(ObisCode baseObisCode, int billingPoint) throws IOException {
		ObisCode capturedCode = ProtocolTools.setObisCodeField(baseObisCode, 0, (byte) 0);
		capturedCode = ProtocolTools.setObisCodeField(capturedCode, 0, (byte) 0);

		int index = getCapturedCodes().indexOf(capturedCode);
		if (index == -1) {
			throw new UnsupportedException(baseObisCode + " has no historical values.");
		}

		if (isDaily()) {
			index--;
		}

		Unsigned32 value = getDataArray().getDataType(billingPoint).getStructure().getDataType(1).getArray().getDataType(index).getUnsigned32();
		return new BigDecimal(value.getValue());
	}

	private boolean isDaily() {
		return getObisCode().equals(DAILY_OBISCODE);
	}

	public ProfileGeneric getProfileGeneric() {
		if (profileGeneric == null) {
			try {
				profileGeneric = getCosemObjectFactory().getProfileGeneric(getObisCode());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return profileGeneric;
	}

	public void retrieve() throws IOException {
		// Not implemented
	}

	private boolean isValidBillingPoint(int billingPoint) {
		try {
			return (billingPoint >= 0) && (billingPoint < getBillingPointCounter());
		} catch (IOException e) {
			return false;
		}
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();

		String crlf = "\r\n";

		int billingPointCount = 0;

		try {
			billingPointCount = getBillingPointCounter();
		} catch (IOException e1) {
			billingPointCount = 0;
		}

		sb.append("AS220StoredValues").append(crlf);
		sb.append(" > getBillingPointCounter = ").append(billingPointCount).append(crlf);

		sb.append(" > obisCodes = ").append(crlf);
		for (ObisCode oc : getCapturedCodes()) {
			sb.append("     # ").append(oc).append(" - ");
			sb.append(oc.getDescription()).append(crlf);
		}

		sb.append(" > billingPointDates = ").append(crlf);
		for (int i = 0; i < billingPointCount; i++) {
			sb.append("     # ").append(i).append(" = ");
			try {
				sb.append(getBillingPointTimeDate(i)).append(crlf);
			} catch (IOException e) {
				sb.append(e.getMessage()).append(crlf);
			}
		}
		sb.append(crlf);

		return sb.toString();
	}

}
