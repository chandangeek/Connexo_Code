package com.energyict.protocolimpl.dlms.as220;

import com.energyict.mdc.upl.UnsupportedException;

import com.energyict.cbo.Unit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.cosem.CapturedObject;
import com.energyict.dlms.cosem.CosemObjectFactory;
import com.energyict.dlms.cosem.DLMSClassId;
import com.energyict.dlms.cosem.DataAccessResultException;
import com.energyict.dlms.cosem.HistoricalRegister;
import com.energyict.dlms.cosem.HistoricalValue;
import com.energyict.dlms.cosem.ProfileGeneric;
import com.energyict.dlms.cosem.Register;
import com.energyict.dlms.cosem.StoredValues;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.base.RetryHandler;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * @author jme
 */
public class AS220StoredValues implements StoredValues {

    public static final ObisCode DAILY_OBISCODE = ObisCode.fromString("1.0.99.2.0.255");

    private final ObisCode obisCode;
    private final CosemObjectFactory cosemObjectFactory;

    private List<UnitInfo> unitInfos = new ArrayList<>();
    private ProfileGeneric profileGeneric = null;
    private List<ObisCode> capturedCodes = null;
    private Array dataArray = null;

	public AS220StoredValues(ObisCode obisCode, CosemObjectFactory cosemObjectFactory) {
		this.obisCode = obisCode;
		this.cosemObjectFactory = cosemObjectFactory;
	}

	private ObisCode getObisCode() {
		return obisCode;
	}

	private CosemObjectFactory getCosemObjectFactory() {
		return cosemObjectFactory;
	}

    private List<ObisCode> getCapturedCodes() throws IOException {
        if (capturedCodes == null) {
            this.capturedCodes = new ArrayList<>();
            for (CapturedObject co : getProfileGeneric().getCaptureObjects()) {
                if (co.getClassId() != DLMSClassId.CLOCK.getClassId()) {
                    capturedCodes.add(co.getLogicalName().getObisCode());
                }
            }
        }
        return capturedCodes;
    }

    private Array getDataArray() throws IOException {
		if (dataArray == null) {
				dataArray = new Array(getProfileGeneric().getBufferData(), 0, 0);
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
        return getDataArray().getDataType(billingPoint).getStructure().getDataType(0).getOctetString().getDateTime(getTimeZone()).getValue().getTime();
	}

    private TimeZone getTimeZone() {
	    return getCosemObjectFactory().getProtocolLink().getTimeZone();
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

        RetryHandler retryHandler = new RetryHandler();


        Date billingPointTimeDate;
        HistoricalRegister historicalRegister;
        do {
            try {
                BigDecimal value = getValue(baseObisCode, billingPoint);
                billingPointTimeDate = getBillingPointTimeDate(billingPoint);
                Unit unit = getUnit(baseObisCode);
                ScalerUnit scalerUnit = getScalerUnit(baseObisCode);

                historicalRegister = new HistoricalRegister();
                historicalRegister.setBillingDate(billingPointTimeDate);
                historicalRegister.setCaptureTime(new Date());
                historicalRegister.setQuantityValue(value, unit);
                historicalRegister.setScalerUnit(scalerUnit);
                return new HistoricalValue(historicalRegister, billingPointTimeDate, getProfileGeneric().getResetCounter());
            } catch (DataAccessResultException e) {
                retryHandler.logFailure(e);
                getCosemObjectFactory().getProtocolLink().getLogger().warning("Problem while reading historical value " + obisCode + ", will retry.");
            }
        } while (retryHandler.canRetry());

        throw new IOException("Could not construct a proper historicalValue for ObisCode " + obisCode);
	}

	private ScalerUnit getScalerUnit(ObisCode baseObisCode) throws IOException {
		return getUnitInfo(baseObisCode).scalerUnit;
	}

	private Unit getUnit(ObisCode baseObisCode) throws IOException {
		return getUnitInfo(baseObisCode).unit;
	}

	private List<UnitInfo> getUnitInfos() {
		return unitInfos;
	}

	private UnitInfo getUnitInfo(ObisCode baseObisCode) throws IOException {
		for (UnitInfo info : getUnitInfos()) {
			if (info.obis.equals(baseObisCode)) {
				return info;
			}
		}

		Register reg = getCosemObjectFactory().getRegister(baseObisCode);
		Unit unit = reg.getQuantityValue().getUnit();
		ScalerUnit scalerUnit = reg.getScalerUnit();
		UnitInfo unitInfo = new UnitInfo(baseObisCode, unit, scalerUnit);
		getUnitInfos().add(unitInfo);
		return unitInfo;
	}

	private BigDecimal getValue(ObisCode baseObisCode, int billingPoint) throws IOException {
		int index = getCapturedCodes().indexOf(baseObisCode);
		if (index == -1) {
			throw new UnsupportedException(baseObisCode + " has no historical values.");
		}

		Unsigned32 value = getDataArray().getDataType(billingPoint).getStructure().getDataType(1).getArray().getDataType(index).getUnsigned32();
		return new BigDecimal(value.getValue());
	}

	public ProfileGeneric getProfileGeneric() {
		if (profileGeneric == null) {
			try {
				profileGeneric = getCosemObjectFactory().getProfileGeneric(getObisCode());
			} catch (IOException e) {
                //Absorb exception
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

	/**
	 * This inner class is just a data container to cache the {@link Unit} and
	 * {@link ScalerUnit} of a register.
     *
	 * @author jme
	 */
	private class UnitInfo {

		private final ObisCode obis;
		private final Unit unit;
		private final ScalerUnit scalerUnit;

		private UnitInfo(ObisCode obisCode, Unit unit, ScalerUnit scalerUnit) {
			this.obis = obisCode;
			this.unit = unit;
			this.scalerUnit = scalerUnit;
		}

	}

	@Override
	public String toString() {
		String crlf = "\r\n";
		StringBuilder builder = new StringBuilder();

		int billingPointCount;

		try {
			billingPointCount = getBillingPointCounter();
		} catch (IOException e1) {
			billingPointCount = 0;
		}

		builder.append("AS220StoredValues").append(crlf);
		builder.append(" > getBillingPointCounter = ").append(billingPointCount).append(crlf);

		builder.append(" > obisCodes = ").append(crlf);
        try {
            for (ObisCode oc : getCapturedCodes()) {
                builder.append("     # ").append(oc).append(" - ");
                builder.append(oc.toString()).append(crlf);
            }
        } catch (IOException e) {
            builder.append(e.getMessage()).append(crlf);
        }

        builder.append(" > billingPointDates = ").append(crlf);
		for (int i = 0; i < billingPointCount; i++) {
			builder.append("     # ").append(i).append(" = ");
			try {
				builder.append(getBillingPointTimeDate(i)).append(crlf);
			} catch (IOException e) {
				builder.append(e.getMessage()).append(crlf);
			}
		}
		builder.append(crlf);

		return builder.toString();
	}

}
