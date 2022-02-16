package com.energyict.protocolimplv2.dlms.ei7.frames;

import com.energyict.cbo.BaseUnit;
import com.energyict.dlms.ScalerUnit;
import com.energyict.dlms.axrdencoding.AxdrType;
import com.energyict.dlms.axrdencoding.BooleanObject;
import com.energyict.dlms.axrdencoding.TypeEnum;
import com.energyict.dlms.axrdencoding.Unsigned16;
import com.energyict.dlms.axrdencoding.Unsigned32;
import com.energyict.dlms.axrdencoding.Unsigned8;
import com.energyict.obis.ObisCode;
import com.energyict.protocol.exception.DataParseException;

import java.io.IOException;
import java.util.Date;
import java.util.function.BiConsumer;
import java.util.function.Function;




public class Frame30 extends Frame {

	private Unsigned32 unixTime;                // UNIX time
	private TimeoutGPRS gprsTimeout;            // GPRS/NB-IoT time out structure
	private BooleanObject disconnectControlB;   // Disconnect control
	private TypeEnum disconnectControlE;        // Disconnect control
	private Unsigned32 managementFcOnline;      // Management Frame Counter - On-line
	private Unsigned16 methodEventHandler;      // Metrological Event Counter
	private Unsigned16 eventCounter;            // Event counter
	private Unsigned16 currentDiagnostic;       // Current Diagnostic
	private Unsigned8 batteriesInUse;           // Batteries in use
	private Unsigned8 batteriesPresent;         // Batteries present
	private Unsigned8 percentageOf1Battery;     // Percentage of primary battery residual capacity
	private Unsigned8 percentageOf2Battery;     // Percentage of secondary battery residual capacity
	private String volumeUnitsAndScalar;        // Volume units and scalar
	private String tempUnitsAndScalar;          // Temperature units and scalar
	private DailyReadings readings[];           // Readings
	private DailyReadings intervalData[];       // Interval data

	public static class ObisConst
	{
		public static final ObisCode GPRS_TIMEOUT                  = ObisCode.fromString("0.0.94.39.52.255");
		public static final ObisCode NBIoT_TIMEOUT                 = ObisCode.fromString("0.1.94.39.52.255");
		public static final ObisCode DISCONNECT_CONTROL_BOOL       = ObisCode.fromString("0.0.96.3.10.255");
		public static final ObisCode DISCONNECT_CONTROL_ENUM       = ObisCode.fromString("0.1.96.3.10.255");
		public static final ObisCode MANAGEMENT_FRAMECOUNTER_ON_LINE    = ObisCode.fromString("0.0.43.1.1.255");
		public static final ObisCode METROLOGICAL_EVENT_COUNTER    = ObisCode.fromString("0.0.96.15.1.255");
		public static final ObisCode EVENT_COUNTER                 = ObisCode.fromString("0.0.96.15.2.255");
		public static final ObisCode CURRENT_DIAGNOSTIC            = ObisCode.fromString("7.0.96.5.1.255");
		public static final ObisCode BATTERIES_IN_USE              = ObisCode.fromString("0.0.94.39.14.255");
		public static final ObisCode BATTERIES_PRESENT             = ObisCode.fromString("0.1.94.39.14.255");
		public static final ObisCode PERCENTAGE_OF_PRIMARY_BATTERY_RESIDUAL_CAPACITY    = ObisCode.fromString("0.0.96.6.6.255");
		public static final ObisCode PERCENTAGE_OF_SECONDARY_BATTERY_RESIDUAL_CAPACITY    = ObisCode.fromString("0.1.96.6.6.255");
		public static final ObisCode VOLUME_UNITS_AND_SCALAR       = ObisCode.fromString("7.0.13.2.0.255");
		public static final ObisCode TEMPERATURE_UNITS_AND_SCALAR  = ObisCode.fromString("7.0.41.3.0.255");
		public static final ObisCode READINGS                      = ObisCode.fromString("7.0.99.99.3.255");
		public static final ObisCode INTERVAL_DATA                 = ObisCode.fromString("7.0.99.99.2.255");
	}

	public static Frame30 deserialize(byte[] data) throws IOException, ClassNotFoundException {
		Frame30 new_frame = new Frame30();

		try {
			// Register part
			new_frame.unixTime = new Unsigned32(getByteArray(data, 1, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			new_frame.gprsTimeout = new TimeoutGPRS();
			new_frame.gprsTimeout.sessionMaxDuration    = new Unsigned32(getByteArray(data, 5, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			new_frame.gprsTimeout.inactivityTimeout     = new Unsigned32(getByteArray(data, 9, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			new_frame.gprsTimeout.networkAttachTimeout  = new Unsigned32(getByteArray(data, 13, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			new_frame.disconnectControlB                = new BooleanObject(getByteArray(data, 17, BooleanObject.SIZE, AxdrType.BOOLEAN), 0);
			new_frame.disconnectControlE                = new TypeEnum(getByteArray(data, 18, TypeEnum.SIZE, AxdrType.ENUM), 0);
			new_frame.managementFcOnline                = new Unsigned32(getByteArray(data, 19, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			new_frame.methodEventHandler                = new Unsigned16(getByteArray(data, 23, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
			new_frame.eventCounter                      = new Unsigned16(getByteArray(data, 25, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
			new_frame.currentDiagnostic                 = new Unsigned16(getByteArray(data, 27, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
			new_frame.batteriesInUse                    = new Unsigned8(getByteArray(data, 29, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			new_frame.batteriesPresent                  = new Unsigned8(getByteArray(data, 30, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			new_frame.percentageOf1Battery              = new Unsigned8(getByteArray(data, 31, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			new_frame.percentageOf2Battery              = new Unsigned8(getByteArray(data, 32, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);

			final byte[] scalarAndUnit = getByteArray(data, 33, 3, AxdrType.NULL);
			new_frame.volumeUnitsAndScalar              = "Scalar = " + scalarAndUnit[1] + ", Unit = " + BaseUnit.get(scalarAndUnit[2]).toString();

			final byte[] tempAndUnit   = getByteArray(data, 35, 3, AxdrType.NULL);
			new_frame.tempUnitsAndScalar                = "Scalar = " + tempAndUnit[1] + ", Unit = " + BaseUnit.get(tempAndUnit[2]).toString();

			// LP part
			Unsigned8 readingsEntriesNumber = new Unsigned8(getByteArray(data, 37, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			int start_pos = 38;
			new_frame.readings = new DailyReadings[readingsEntriesNumber.getValue()];
			for (int i = 0; i < readingsEntriesNumber.getValue(); ++i) {
				new_frame.readings[i] = new DailyReadings();
				start_pos = readLP(data, start_pos, new_frame.readings[i]);
			}

			Unsigned8 intrvlDataEntriesNmbr = new Unsigned8(getByteArray(data, start_pos, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			++start_pos;
			new_frame.intervalData = new DailyReadings[intrvlDataEntriesNmbr.getValue()];
			for (int i = 0; i < intrvlDataEntriesNmbr.getValue(); ++i) {
				new_frame.intervalData[i] = new DailyReadings();
				start_pos = readLP(data, start_pos, new_frame.intervalData[i]);
			}
		} catch (IOException e) {
			throw DataParseException.ioException(e);
		}
		return new_frame;
	}

	public final void save(FiveConsumer<ObisCode, Long, ScalerUnit, Date, String> saveRegisterFunc, BiConsumer<ObisCode, DailyReadings[]> saveLoadProfileFunc, Function<Unsigned32, Date> getDateTime, boolean isGPRS)
	{
		Date dateTime = getDateTime.apply(unixTime);

		ObisCode timeoutObisCode = isGPRS ?ObisConst.GPRS_TIMEOUT: ObisConst.NBIoT_TIMEOUT;
		saveRegisterFunc.accept(timeoutObisCode.setB(2), gprsTimeout.sessionMaxDuration.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(timeoutObisCode.setB(3), gprsTimeout.inactivityTimeout.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(timeoutObisCode.setB(4), gprsTimeout.sessionMaxDuration.longValue(), null, dateTime, null);

		saveRegisterFunc.accept(ObisConst.DISCONNECT_CONTROL_BOOL, disconnectControlB.getBooleanObject().longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.DISCONNECT_CONTROL_ENUM, disconnectControlE.getTypeEnum().longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.MANAGEMENT_FRAMECOUNTER_ON_LINE, managementFcOnline.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.METROLOGICAL_EVENT_COUNTER, methodEventHandler.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.EVENT_COUNTER, eventCounter.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.CURRENT_DIAGNOSTIC, currentDiagnostic.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.BATTERIES_IN_USE, batteriesInUse.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.BATTERIES_PRESENT, batteriesPresent.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.PERCENTAGE_OF_PRIMARY_BATTERY_RESIDUAL_CAPACITY, percentageOf1Battery.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.PERCENTAGE_OF_SECONDARY_BATTERY_RESIDUAL_CAPACITY, percentageOf2Battery.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.VOLUME_UNITS_AND_SCALAR, new Long(0),null, dateTime, volumeUnitsAndScalar);
		saveRegisterFunc.accept(ObisConst.TEMPERATURE_UNITS_AND_SCALAR, new Long(0), null, dateTime, tempUnitsAndScalar);

		saveLoadProfileFunc.accept(ObisConst.READINGS, readings);
		saveLoadProfileFunc.accept(ObisConst.INTERVAL_DATA, intervalData);
	}

	private static int readLP(byte[] data, int m_offset, DailyReadings aReading) throws IOException {
		aReading.unixTime                       = new Unsigned32(getByteArray(data, m_offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED),0);
		aReading.dailyDiagnostic                = new Unsigned16(getByteArray(data, m_offset += 4, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED),0);
		aReading.currentIndexOfConvertedVolume  = new Unsigned32(getByteArray(data, m_offset += 2, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED),0);
		aReading.currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(data, m_offset += 4, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED),0);
		aReading.currentActiveTariff            = new Unsigned8(getByteArray(data, m_offset += 4, Unsigned8.SIZE, AxdrType.UNSIGNED),0);
		return ++m_offset;
	}
}
