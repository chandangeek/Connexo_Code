/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.elster.protocolimpl.dlms.objects.a1;

import com.elster.dlms.types.basic.ObisCode;
import com.elster.protocolimpl.dlms.objects.ObjectPool;
import com.elster.protocolimpl.dlms.objects.a1.utils.BPValueHist;
import com.elster.protocolimpl.dlms.objects.a1.utils.DataReader;
import com.elster.protocolimpl.dlms.objects.a1.utils.RegisterReader;
import com.elster.protocolimpl.dlms.objects.a1.utils.RegisterReaderDT;
import com.elster.protocolimpl.dlms.util.A1Defs;

/**
 *
 * @author heuckeg
 */
@SuppressWarnings("unused")
public class A1ObjectPool extends ObjectPool
{
  public static final ObisCode Clock = A1Defs.CLOCK_OBJECT;
  public static final ObisCode SyncReg = A1Defs.SYNC_REG;
  public static final ObisCode MPI = A1Defs.METERING_POINT_ID;
  //
  public static final ObisCode BatteryVoltage = A1Defs.BATTERY_VOLTAGE;
  public static final ObisCode BatteryInitialEnergy = A1Defs.BATTERY_INITAL_ENERGY;
  public static final ObisCode BatteryInstallTime = A1Defs.BATTERY_INSTALL_TIME;
  public static final ObisCode BatteryRemainingTime = A1Defs.BATTERY_REMAINING_TIME;
  //
  public static final ObisCode BatteryVoltageModem = A1Defs.BATTERY_VOLTAGE_MODEM;
  public static final ObisCode BatteryInitialEnergyModem = A1Defs.BATTERY_INITAL_ENERGY_MODEM;
  public static final ObisCode BatteryInstallTimeModem = A1Defs.BATTERY_INSTALL_TIME_MODEM;
  public static final ObisCode BatteryRemainingTimeModem = A1Defs.BATTERY_REMAINING_TIME_MODEM;
  //
  public static final ObisCode TotalOpTime = A1Defs.TOTAL_OPERATION_TIME;
  public static final ObisCode SnapshotReasonCode = A1Defs.SNAPSHOT_REASON_CODE;
  public static final ObisCode SnapshotPeriodCounter = A1Defs.SNAPSHOT_PERIOD_COUNTER;
  //
  public static final ObisCode UnitsEvtCnt = A1Defs.UNITS_EVENT_COUNTER;
  public static final ObisCode UnitsEvtStat = A1Defs.UNITS_EVENT_STATUS;
  public static final ObisCode UnitsDeviceMode = A1Defs.UNITS_DEVICE_MODE;
  public static final ObisCode UnitsDeviceDiagnostic = A1Defs.UNITS_DEVICE_DIAGNOSTIC;
  public static final ObisCode MetroAlarmStat = A1Defs.METRO_ALARM_STATUS;
  public static final ObisCode GsmRssi = A1Defs.GSM_RSSI;
  public static final ObisCode FirmwareMet = A1Defs.SOFTWARE_VERSION;
  public static final ObisCode BOD = A1Defs.BEGIN_OF_DAY;
  //
  public static final ObisCode TotVa = A1Defs.VM_TOTAL_CURR;
  public static final ObisCode TotVb = A1Defs.VB_TOTAL_CURR;
  public static final ObisCode Va = A1Defs.VAA;
  public static final ObisCode Vb = A1Defs.VB_TOTAL_QUANTITY;
  public static final ObisCode Vb_F1 = A1Defs.VB_F1;
  public static final ObisCode Vb_F2 = A1Defs.VB_F2;
  public static final ObisCode Vb_F3 = A1Defs.VB_F3;
  public static final ObisCode CurrT = A1Defs.TEMPERATURE_CURR;
  public static final ObisCode CurrP = A1Defs.PRESSURE_ABSOLUTE_CURR;
  //
  //
  public static final ObisCode BillingProfile = A1Defs.BILLING_PROFILE;
  public static final ObisCode HourlyProfile = A1Defs.LOAD_PROFILE_60;
  public static final ObisCode DailyProfile = A1Defs.LOAD_PROFILE_DLY;
  public static final ObisCode UnitsEventLog = A1Defs.EVENT_LOG;
  public static final ObisCode MetrologyEventLog = A1Defs.METROLOGY_EVENT_LOG;
  //
  // special codes
  public static final ObisCode DstEnabled = new ObisCode("0.0.96.53.0.255");
  public static final ObisCode UnitsEventLogCount = new ObisCode("7.0.128.8.0.255");
  public static final ObisCode AutoConnect = new ObisCode("0.0.2.1.0.255");
  public static final ObisCode CurrentTariff = new ObisCode("0.0.13.0.0.255");
  public static final ObisCode WakeUpParams = new ObisCode("0.1.2.2.0.255");
  public static final ObisCode ModemTimeout = new ObisCode("0.1.2.1.0.255");
  public static final ObisCode MetrologyEventLogCount = new ObisCode("7.0.128.8.1.255");
  public static final ObisCode OnDemandSnapshotTime = new ObisCode("0.1.15.0.0.255");

  public static final ObisCode BOY = A1Defs.BEGIN_OF_YEAR;
  public static final ObisCode BILLING_PERIOD = A1Defs.BILLING_PERIOD;
  public static final ObisCode BILLING_PERIOD_START = A1Defs.BILLING_PERIOD_START;

  //
  // Qb max hist
  public static final ObisCode DlyQbMaxHist = new ObisCode("7.0.43.47.0.255");

  // V2 only
  public static final ObisCode TotVaOld = new ObisCode("7.0.12.0.0.255");
  public static final ObisCode UmiAlarmStatus = new ObisCode("7.0.0.64.58.255");
  //

  public static final ObisCode UnitsDlyDiagnostic = new ObisCode("7.1.96.5.1.255");

   // V4
  public static final ObisCode SIM_SubscriberID = new ObisCode("0.128.96.194.2.255");
  public static final ObisCode A1_RESET_ALARMS_1 = new ObisCode(7, 0, 0, 64, 56, 255);
  public static final ObisCode A1_RESET_ALARMS_2 = new ObisCode(7, 0, 0, 64, 51, 255);
  public static final ObisCode A1_CHANGE_DEVICE_COND_SCRIPT = new ObisCode(0, 0, 10, 0, 0, 255);
  public static final ObisCode A1_GSM_CONFIG = new ObisCode("0.128.96.194.1.255");

  //
  private static final ObjectAccessor[] oas =
  {
    new ObjectAccessor(0x000000, 0xFFFFFF, "SetClock", new Clock()),
    new ObjectAccessor(0x000000, 0xFFFFFF, "FirmwareVersion", new DataReader(FirmwareMet)),
    //
    // data (class 1)
    new ObjectAccessor(0x011539, 0xFFFFFF, "DeviceMode", new DataReader(UnitsDeviceMode)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "StartOfGasDay", new StartOfDay()),
//    new ObjectAccessor(0x011539, 0xFFFFFF, "SnapshotReasonCode", new DataReader(SnapshotReasonCode)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "UnitsDeviceDiagnostic", new DataReader(UnitsDeviceDiagnostic)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SynchronizationRegister", new SynchronizationRegister()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "UnitsEventCounter", new DataReader(UnitsEvtCnt)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BillingSnapshotPeriodCounter", new DataReader(
    SnapshotPeriodCounter)),
    new ObjectAccessor(0x000000, 0x011539, "UmiAlarmStatus", new DataReader(UmiAlarmStatus)),
    //
    // Register (class 3)
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryVoltage", new RegisterReader(BatteryVoltage)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "UnitsEventStatus", new RegisterReader(UnitsEvtStat)),
    new ObjectAccessor(0x000000, 0xFFFFFF, "GsmRssi", new RegisterReader(GsmRssi)),
    new ObjectAccessor(0x000000, 0xFFFFFF, "CurrentPressure", new RegisterReader(CurrP)),
    new ObjectAccessor(0x000000, 0xFFFFFF, "CurrentTemperature", new RegisterReader(CurrT)),
    new ObjectAccessor(0x000000, 0xFFFFFF, "BatteryEstimRemTime", new RegisterReader(BatteryRemainingTime)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryVoltageModem", new RegisterReader(BatteryVoltageModem)),
    //
    new ObjectAccessor(0x000000, 0x011539, "TotalVaOld", new RegisterReader(TotVaOld)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "TotalVa", new RegisterReader(TotVa)),
    new ObjectAccessor(0x000000, 0xFFFFFF, "TotalVb", new RegisterReader(TotVb)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "Vb_F1", new RegisterReader(Vb_F1)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "Vb_F2", new RegisterReader(Vb_F2)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "Vb_F3", new RegisterReader(Vb_F3)),
    //
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryInitialEnergyModem", new RegisterReader(
    BatteryInitialEnergyModem)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryInstallationTimeModem", new RegisterReaderDT(
    BatteryInstallTimeModem)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryInitialEnergy", new RegisterReader(BatteryInitialEnergy)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryInstallationTime", new RegisterReaderDT(BatteryInstallTime)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BatteryEstimRemTimeModem", new RegisterReader(
    BatteryRemainingTimeModem)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "TotalOperatingTime", new RegisterReader(TotalOpTime)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "MetrologyAlarmStatus", new RegisterReader(MetroAlarmStat)),
    //
    // specials
    new ObjectAccessor(0x000000, 0xFFFFFF, "DstEnabled", new ClockConfiguration()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "UnitsEventLogEntries", new NumberOfLogEntries(UnitsEventLog,
                                                                                          UnitsEventLogCount)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "CurrentTariff", new CurrentTariff()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "WakeupParams", new WakeupParams()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "MetrologyEventLogEntries", new NumberOfLogEntries(
    MetrologyEventLog, MetrologyEventLogCount)),
    //
    //
    new ObjectAccessor(0x000000, 0x011539, "SetMeteringPointId", new MeteringPointId()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SetMeteringPointId", new MeteringPointIdV2()),
    //
    new ObjectAccessor(0x000000, 0x011539, "SetGprsConfiguration", new GprsConfigurationV1()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SetGprsConfiguration", new GprsConfigurationV2()),
    //
    new ObjectAccessor(0x000000, 0x011539, "SetSessionTimeout", new SessionTimeoutV1()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SetSessionTimeout", new SessionTimeoutV2()),
    //
    new ObjectAccessor(0x000000, 0x011539, "SetWanConfiguration", new WanConfiguratonV1()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SetWanConfiguration", new WanConfiguratonV2()),
    //
    new ObjectAccessor(0x000000, 0x011539, "SetCyclicMode", new ConfigureCyclicModeV1()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SetCyclicMode", new ConfigureCyclicModeV2()),
    //
    new ObjectAccessor(0x000000, 0x011539, "SetPreferredDateMode", new ConfigurePreferredDateModeV1()),
    //    new ObjectAccessor(0x011539, 0xFFFFFF, "SetPreferredDateMode", new ConfigureCyclicModeV2()),

    new ObjectAccessor(0x011539, 0xFFFFFF, "DisablePassiveTariff", new TariffPassiveDisable()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "UploadPassiveTariff", new TariffPassiveUpload()),
    new ObjectAccessor(0x011539, 0xFFFFFF, "SetSpecialDaysTable", new SpecialDaysTable()),

    new ObjectAccessor(0x011539, 0xFFFFFF, "DlyQbMaxPrev1", new QbMaxHist(101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "DlyQbMaxPrev2", new QbMaxHist(102)),

    new ObjectAccessor(0x011539, 0xFFFFFF, "OnDemandSnapshotTime", new OnDemandSnapshotTime()),

    new ObjectAccessor(0x011539, 0xFFFFFF, "BPSnapshotCounter_1", new BPValueHist(A1ObjectPool.SnapshotPeriodCounter, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPReasonCode_1", new BPValueHist(A1ObjectPool.SnapshotReasonCode, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPDeviceDiag_1", new BPValueHist(A1ObjectPool.UnitsDeviceDiagnostic, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPCalendarName_1", new BPValueHist(A1ObjectPool.CurrentTariff, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_1", new BPValueHist(A1ObjectPool.Vb, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F1_1", new BPValueHist(A1ObjectPool.Vb_F1, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F2_1", new BPValueHist(A1ObjectPool.Vb_F2, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F3_1", new BPValueHist(A1ObjectPool.Vb_F3, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVa_1", new BPValueHist(A1ObjectPool.Va, 101)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPSnapshotCounter_2", new BPValueHist(A1ObjectPool.SnapshotPeriodCounter, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPReasonCode_2", new BPValueHist(A1ObjectPool.SnapshotReasonCode, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPDeviceDiag_2", new BPValueHist(A1ObjectPool.UnitsDeviceDiagnostic, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPCalendarName_2", new BPValueHist(A1ObjectPool.CurrentTariff, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_2", new BPValueHist(A1ObjectPool.Vb, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F1_2", new BPValueHist(A1ObjectPool.Vb_F1, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F2_2", new BPValueHist(A1ObjectPool.Vb_F2, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F3_2", new BPValueHist(A1ObjectPool.Vb_F3, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVa_2", new BPValueHist(A1ObjectPool.Va, 102)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPSnapshotCounter_3", new BPValueHist(A1ObjectPool.SnapshotPeriodCounter, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPReasonCode_3", new BPValueHist(A1ObjectPool.SnapshotReasonCode, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPDeviceDiag_3", new BPValueHist(A1ObjectPool.UnitsDeviceDiagnostic, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPCalendarName_3", new BPValueHist(A1ObjectPool.CurrentTariff, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_3", new BPValueHist(A1ObjectPool.Vb, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F1_3", new BPValueHist(A1ObjectPool.Vb_F1, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F2_3", new BPValueHist(A1ObjectPool.Vb_F2, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVb_F3_3", new BPValueHist(A1ObjectPool.Vb_F3, 103)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BPVa_3", new BPValueHist(A1ObjectPool.Va, 103)),

    new ObjectAccessor(0x011539, 0xFFFFFF, "BillingPeriodStart", new BillingPeriodStart(BILLING_PERIOD_START)),
    new ObjectAccessor(0x011539, 0xFFFFFF, "BillingPeriod", new BillingPeriod(BILLING_PERIOD)),
    // new to V4
    new ObjectAccessor(0x011549, 0xFFFFFF, "SimSubscriberId", new DataReader(SIM_SubscriberID, 9137, 2)),
    // allow Reset of alarms also for version 3
    new ObjectAccessor(0x011539, 0xFFFFFF, "ResetAlarms", new AlarmResetter(A1_RESET_ALARMS_1, A1_RESET_ALARMS_2)),
    new ObjectAccessor(0x011549, 0xFFFFFF, "UnitsStatus", new UNITSStatusChanger()),
    // allow Reset of log also for version 3
    new ObjectAccessor(0x011539, 0xFFFFFF, "ResetUNITSLog", new ResetLog(A1Defs.EVENT_LOG)),

    new ObjectAccessor(0x011549, 0xFFFFFF, "RSSIMultipleSampling", new RSSIMultipleSampling(A1_GSM_CONFIG)),
    new ObjectAccessor(0x011549, 0xFFFFFF, "FlagGasDayConfig", new FlagGasDayConfig()),

    new ObjectAccessor(0x011539, 0xFFFFFF, "UnitsDlyDiag", new DataReader(UnitsDlyDiagnostic)),

  };

  public A1ObjectPool()
  {
    super(oas);
  }

}
