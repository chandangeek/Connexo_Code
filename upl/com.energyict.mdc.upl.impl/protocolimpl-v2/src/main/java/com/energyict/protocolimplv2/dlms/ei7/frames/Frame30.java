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
import com.energyict.protocolimplv2.dlms.ei7.EI7Const;

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
	private DailyReadings[] readings;           // Readings
	private DailyReadings[] intervalData;       // Interval data

	public static class ObisConst {
		public static final ObisCode TIME_OUT_SESSION_MAX_DURATION                     = ObisCode.fromString("0.2.94.39.52.255");
		public static final ObisCode TIME_OUT_INACTIVITY                               = ObisCode.fromString("0.3.94.39.52.255");
		public static final ObisCode TIME_OUT_NETWORK_ATTACK                           = ObisCode.fromString("0.4.94.39.52.255");
		public static final ObisCode DISCONNECT_CONTROL_BOOL                           = ObisCode.fromString("0.0.96.3.10.255");
		public static final ObisCode DISCONNECT_CONTROL_ENUM                           = ObisCode.fromString("0.1.96.3.10.255");
		public static final ObisCode MANAGEMENT_FRAME_COUNTER_ON_LINE                  = ObisCode.fromString("0.0.43.1.1.255");
		public static final ObisCode METROLOGICAL_EVENT_COUNTER                        = ObisCode.fromString("0.0.96.15.1.255");
		public static final ObisCode EVENT_COUNTER                                     = ObisCode.fromString("0.0.96.15.2.255");
		public static final ObisCode CURRENT_DIAGNOSTIC                                = ObisCode.fromString("7.0.96.5.1.255");
		public static final ObisCode BATTERIES_IN_USE                                  = ObisCode.fromString("0.0.94.39.14.255");
		public static final ObisCode BATTERIES_PRESENT                                 = ObisCode.fromString("0.1.94.39.14.255");
		public static final ObisCode PERCENTAGE_OF_PRIMARY_BATTERY_RESIDUAL_CAPACITY   = ObisCode.fromString("0.0.96.6.6.255");
		public static final ObisCode PERCENTAGE_OF_SECONDARY_BATTERY_RESIDUAL_CAPACITY = ObisCode.fromString("0.1.96.6.6.255");
		public static final ObisCode VOLUME_UNITS_AND_SCALAR                           = ObisCode.fromString("7.0.13.2.0.255");
		public static final ObisCode TEMPERATURE_UNITS_AND_SCALAR                      = ObisCode.fromString("7.0.41.3.0.255");
		public static final ObisCode READINGS                                          = ObisCode.fromString("7.0.99.99.3.255");
		public static final ObisCode INTERVAL_DATA                                     = ObisCode.fromString("7.0.99.99.2.255");
	}

	public static Frame30 deserialize(byte[] data) throws IOException, ClassNotFoundException {
		Frame30 newFrame = new Frame30();

		try {
			// Register part

			newFrame.unixTime                          = new Unsigned32(getByteArray(data, 1, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			newFrame.gprsTimeout                       = new TimeoutGPRS();
			newFrame.gprsTimeout.sessionMaxDuration    = new Unsigned32(getByteArray(data, 5, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			newFrame.gprsTimeout.inactivityTimeout     = new Unsigned32(getByteArray(data, 9, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			newFrame.gprsTimeout.networkAttachTimeout  = new Unsigned32(getByteArray(data, 13, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			newFrame.disconnectControlB                = new BooleanObject(getByteArray(data, 17, BooleanObject.SIZE, AxdrType.BOOLEAN), 0);
			newFrame.disconnectControlE                = new TypeEnum(getByteArray(data, 18, TypeEnum.SIZE, AxdrType.ENUM), 0);
			newFrame.managementFcOnline                = new Unsigned32(getByteArray(data, 19, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED), 0);
			newFrame.methodEventHandler                = new Unsigned16(getByteArray(data, 23, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
			newFrame.eventCounter                      = new Unsigned16(getByteArray(data, 25, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
			newFrame.currentDiagnostic                 = new Unsigned16(getByteArray(data, 27, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED), 0);
			newFrame.batteriesInUse                    = new Unsigned8(getByteArray(data, 29, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			newFrame.batteriesPresent                  = new Unsigned8(getByteArray(data, 30, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			newFrame.percentageOf1Battery              = new Unsigned8(getByteArray(data, 31, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			newFrame.percentageOf2Battery              = new Unsigned8(getByteArray(data, 32, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);

			final byte[] scalarAndUnit = getByteArray(data, 33, 3, AxdrType.NULL);
			newFrame.volumeUnitsAndScalar              = "Scalar = " + scalarAndUnit[1] + ", Unit = " + BaseUnit.get(scalarAndUnit[2]).toString();

			final byte[] tempAndUnit   = getByteArray(data, 35, 3, AxdrType.NULL);
			newFrame.tempUnitsAndScalar                = "Scalar = " + tempAndUnit[1] + ", Unit = " + BaseUnit.get(tempAndUnit[2]).toString();

			// LP part
			Unsigned8 readingsEntriesNumber = new Unsigned8(getByteArray(data, 37, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			int startPos = 38;
			newFrame.readings = new DailyReadings[readingsEntriesNumber.getValue()];
			for (int i = 0; i < readingsEntriesNumber.getValue(); ++i) {
				newFrame.readings[i] = new DailyReadings();
				startPos = readLP(data, startPos, newFrame.readings[i]);
			}

			Unsigned8 intervalDataEntriesNumber = new Unsigned8(getByteArray(data, startPos, Unsigned8.SIZE, AxdrType.UNSIGNED), 0);
			++startPos;
			newFrame.intervalData = new DailyReadings[intervalDataEntriesNumber.getValue()];
			for (int i = 0; i < intervalDataEntriesNumber.getValue(); ++i) {
				newFrame.intervalData[i] = new DailyReadings();
				startPos = readLP(data, startPos, newFrame.intervalData[i]);
			}
		} catch (IOException e) {
			throw DataParseException.ioException(e);
		}
		return newFrame;
	}

	public final void save(FiveConsumer<ObisCode, Long, ScalerUnit, Date, String> saveRegisterFunc, BiConsumer<ObisCode, DailyReadings[]> saveLoadProfileFunc, Function<Unsigned32, Date> getDateTime, boolean isGPRS) {
		Date dateTime = getDateTime.apply(unixTime);

		saveRegisterFunc.accept(ObisConst.TIME_OUT_SESSION_MAX_DURATION, gprsTimeout.sessionMaxDuration.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.TIME_OUT_INACTIVITY, gprsTimeout.inactivityTimeout.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.TIME_OUT_NETWORK_ATTACK, gprsTimeout.networkAttachTimeout.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.DISCONNECT_CONTROL_BOOL, disconnectControlB.getBooleanObject().longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.DISCONNECT_CONTROL_ENUM, disconnectControlE.getTypeEnum().longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.MANAGEMENT_FRAME_COUNTER_ON_LINE, managementFcOnline.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.METROLOGICAL_EVENT_COUNTER, methodEventHandler.longValue(), null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.EVENT_COUNTER, eventCounter.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.CURRENT_DIAGNOSTIC, currentDiagnostic.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.BATTERIES_IN_USE, batteriesInUse.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.BATTERIES_PRESENT, batteriesPresent.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.PERCENTAGE_OF_PRIMARY_BATTERY_RESIDUAL_CAPACITY, percentageOf1Battery.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.PERCENTAGE_OF_SECONDARY_BATTERY_RESIDUAL_CAPACITY, percentageOf2Battery.longValue(),null, dateTime, null);
		saveRegisterFunc.accept(ObisConst.VOLUME_UNITS_AND_SCALAR, new Long(0), null, dateTime, volumeUnitsAndScalar);
		saveRegisterFunc.accept(ObisConst.TEMPERATURE_UNITS_AND_SCALAR, new Long(0), null, dateTime, tempUnitsAndScalar);

		saveLoadProfileFunc.accept(ObisConst.READINGS, readings);
		saveLoadProfileFunc.accept(ObisConst.INTERVAL_DATA, intervalData);
	}

	private static int readLP(byte[] data, int m_offset, DailyReadings aReading) throws IOException {
		aReading.unixTime                       = new Unsigned32(getByteArray(data, m_offset, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED),0);
		aReading.dailyDiagnostic                = new Unsigned16(getByteArray(data, m_offset += 4, Unsigned16.SIZE, AxdrType.LONG_UNSIGNED),0);
		aReading.currentIndexOfConvertedVolume  = new Unsigned32(getByteArray(data, m_offset += 2, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED),0);
		aReading.currentIndexOfConvertedVolumeUnderAlarm = new Unsigned32(getByteArray(data, m_offset += 4, Unsigned32.SIZE, AxdrType.DOUBLE_LONG_UNSIGNED),0);
		aReading.currentActiveTariff            = new Unsigned8(0);
		m_offset += 4;
		return m_offset;
	}
}
