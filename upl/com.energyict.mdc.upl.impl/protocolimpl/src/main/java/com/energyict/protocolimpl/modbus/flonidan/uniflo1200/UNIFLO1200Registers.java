/**
 * UNIFLO_1200v28_Registers.java
 * 
 * Created on 8-dec-2008, 13:37:23 by jme
 * 
 */
package com.energyict.protocolimpl.modbus.flonidan.uniflo1200;

import java.io.IOException;

/**
 * @author jme
 *
 */
public class UNIFLO1200Registers {

	private static final int MIN_INDEX = 0;
	private static final int MAX_INDEX = 255;

    public static final int UNIFLO1200_FW_25 = 25;
    public static final int UNIFLO1200_FW_28 = 28;

    private int fwVersion = 0;
	
	public UNIFLO1200Registers(int uniflo1200_fw_version) throws IOException {
		switch (uniflo1200_fw_version) {
			case UNIFLO1200_FW_25: break;
			case UNIFLO1200_FW_28: break;
			default: throw new IOException("Unknown firmwareversion: " + uniflo1200_fw_version);
		}
		this.fwVersion  = uniflo1200_fw_version;
	}
	
	public int getDataType(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex);
		return (absoluteAddress & 0x00F00000) >> 20;
	}
	
	public String getParser(int addressIndex) throws IOException {
		int dataType = getDataType(addressIndex);
		switch (dataType) {
			case 0x00: return UNIFLO1200Parsers.PARSER_UINT8;
			case 0x01: return UNIFLO1200Parsers.PARSER_UINT16;
			case 0x02: return UNIFLO1200Parsers.PARSER_UINT32;
			case 0x03: return UNIFLO1200Parsers.PARSER_REAL32;
			case 0x04: return UNIFLO1200Parsers.PARSER_INTREAL;
			case 0x05: return UNIFLO1200Parsers.PARSER_TIME;
			case 0x06: return UNIFLO1200Parsers.PARSER_STR22;
			case 0x07: return UNIFLO1200Parsers.PARSER_OPTION;
			case 0x08: return UNIFLO1200Parsers.PARSER_LOC_PTR;
			case 0x09: return UNIFLO1200Parsers.PARSER_STR29;
			case 0x0A: return UNIFLO1200Parsers.PARSER_STR1;
			case 0x0B: return UNIFLO1200Parsers.PARSER_UINT160;
			case 0x0C: return UNIFLO1200Parsers.PARSER_UINT320;
			case 0x0D: return UNIFLO1200Parsers.PARSER_REAL320;
			case 0x0E: return UNIFLO1200Parsers.PARSER_STR8;
			case 0x0F: return UNIFLO1200Parsers.PARSER_DATABLOCK;
			default: throw new IOException("Unknown datatype: " + dataType + " for firmware version " + fwVersion);
		}
	}
	
	public int getDataLength(int addressIndex) throws IOException {
		int dataType = getDataType(addressIndex);
		switch (dataType) {
			case 0x00: return UNIFLO1200Parsers.LENGTH_UINT8;
			case 0x01: return UNIFLO1200Parsers.LENGTH_UINT16;
			case 0x02: return UNIFLO1200Parsers.LENGTH_UINT32;
			case 0x03: return UNIFLO1200Parsers.LENGTH_REAL32;
			case 0x04: return UNIFLO1200Parsers.LENGTH_INTREAL;
			case 0x05: return UNIFLO1200Parsers.LENGTH_TIME;
			case 0x06: return UNIFLO1200Parsers.LENGTH_STR22;
			case 0x07: return UNIFLO1200Parsers.LENGTH_OPTION;
			case 0x08: return UNIFLO1200Parsers.LENGTH_LOC_PTR;
			case 0x09: return UNIFLO1200Parsers.LENGTH_STR29;
			case 0x0A: return UNIFLO1200Parsers.LENGTH_STR1;
			case 0x0B: return UNIFLO1200Parsers.LENGTH_UINT160;
			case 0x0C: return UNIFLO1200Parsers.LENGTH_UINT320;
			case 0x0D: return UNIFLO1200Parsers.LENGTH_REAL320;
			case 0x0E: return UNIFLO1200Parsers.LENGTH_STR8;
			default: throw new IOException("Unknown datatype length for dataType: " + dataType + " for firmware version " + fwVersion);
		}
	}

	public int getWordAddr(int addressIndex) throws IOException {
		int absoluteAddress = getAbsAddr(addressIndex) & 0x0000FFFF;
		return absoluteAddress / 0x02;
	}
		
	private int getAbsAddr(int addressIndex) throws IOException {
		if ((addressIndex > MAX_INDEX) || (addressIndex < MIN_INDEX)) 
			throw new IOException("getWordAddr() addressIndex wrong value: " + addressIndex + ". Valid value: " + MIN_INDEX + " to " + MAX_INDEX);

		switch (this.fwVersion) {
			case UNIFLO1200_FW_25: return ABSOLUTE_ADRESSES_V25[addressIndex];
			case UNIFLO1200_FW_28: return ABSOLUTE_ADRESSES_V28[addressIndex];
			default: throw new IOException("Unknown firmwareversion: " + this.fwVersion);
		}
	}

	private static final int ABSOLUTE_ADRESSES_V25[] = {
		0x00000000
	};

	public static final int V28_ZA 					= 0;
	public static final int V28_SLAVE_ADDR 			= 1;
	public static final int V28_TIME 				= 2;
	public static final int V28_ZB2 				= 3;
	public static final int V28_TEMP_CORR_TABEL 	= 4;
	public static final int V28_BAT					= 5;
	public static final int V28_I_ENERGY_F_AVG		= 6;
	public static final int V28_I_TEMP_AVG			= 7;
	
	public static final int V28_PASSWORD_LVL		= 31;
	
	public static final int V28_VER_TYPE			= 114;
	

	private static final int ABSOLUTE_ADRESSES_V28[] = {
		0x573000B0,   // 0    Za                              R       1R   Za                                                                          Za
		0x03000001,   // 1    SlaveAdr                                1    Modbus slave address                                                        Modbus address
		0x01500002,   // 2    Time                            T       1    Uniflo time                                                                 Time
		0x5A3000B4,   // 3    Zb2                             R       1R   Zb                                                                          Zb
		0x0BF01B0C,   // 4    TempCorrTabel        112                     Temperatur correction tabel
		0x011000F6,   // 5    Bat                  Days      U        1    Battery remaining                                                           Bat. remaining
		0x23300200,   // 6    IEnergyFAvg          MJ        U0        R1  Average power.
		0x23300204,   // 7    ITempAvg             °C         0        R1  Average temp.
		0x43300208,   // 8    ITrykAvg             BarA      U0        R1  Average Press.
		0x2330020C,   // 9    IFlowUAvg            m3/h       0        R1  Avg. flow corr.
		0x23300210,   // 10   IFlowKAvg            Nm3/h     U0        R1  Avg. flow conv.
		0x23300214,   // 11   DEnergyFAvg          MJ        U0        R2  Average power.
		0x23300218,   // 12   DTempAvg             °C         0        R2  Average temp.
		0x4330021C,   // 13   DTrykAvg             BarA      U0        R2  Average Press.
		0x23300220,   // 14   DFlowUAvg            m3/h       0        R2  Avg. flow corr.
		0x23300224,   // 15   DFlowKAvg            Nm3/h     U0        R2  Avg. flow conv.
		0x071001F0,   // 16   ConfigChecksum                          1R   Configuration checksum                                                      Config. checksum
		0x07100152,   // 17   LogChecksum                              R0  Log line checksum
		0x018000C4,   // 18   PulsoutReg1                     R            Pulse output 1
		0x018000C5,   // 19   PulsoutReg2                     R            Pulse output 2
		0x017000C6,   // 20   PulsDiv1                        R            Division faktor for puls 1
		0x017000C7,   // 21   PulsDiv2                        R            Division faktor for puls 2
		0x0300000F,   // 22   Powerup                                      Numbers of  powerup
		0x07100010,   // 23   ADtryk                                   R3  Pressure AD-count
		0x07100012,   // 24   ADtemp                                   R3  Temperature AD-count
		0x071000DA,   // 25   ADSensortemp                             R3  Pressure sensor A-count
		0x017000F4,   // 26   PulsOutTid                      R            Pulse output period length
		0xBA300064,   // 27   BassisEnergi         MJ/nm3    UR       1    Heat value                                                                  Heat value
		0x0AF01CC8,   // 28   DisplaySetupTable    18         R            Display setup table
		0x003000F0,   // 29   Operator                                     Operator ID 4 char
		0x07F0CEE8,   // 30   EventLog                                     Eventlog (200 logninger)
		0x070000D6,   // 31   Password                                 R   Actual secure level
		0xC34000B8,   // 32   VolCtrl              m3                      Vol. control                                        0           99999999
		0x0B601BC4,   // 33   SystemData                                   SystemData                                                                  Temp type
		0x09100124,   // 34   PulsCheckPulser      pulses    UR            Pulse check every
		0x07F011A0,   // 35   AlarmLog                                     Alarmlog (100 logninger)
		0x47300080,   // 36   Pressure             bar A     U        1R0 1Pressure                                                                    Pressure
		0x27300084,   // 37   Temperature          °C                 1R0 1Temperature                                                                 Temperature
		0x57300088,   // 38   Korr                                    1R0 1Conversion factor                                                           Conv. factor
		0xDB3000F8,   // 39   Pulsvalue            m3/pulse  UR       1    Value of pulse                                      0           99999999    Pulse Value
		0x27300078,   // 40   FlowCorr             m3/h               1R0 1Flow corrected                                                              Flow corr.
		0x2730007C,   // 41   FlowConv             Nm3/h     U        1R0 1Flow conv.                                                                  Flow conv.
		0xC3400068,   // 42   VolCorr              m3                      Vol. corrected                                      0           99999999
		0xC3400070,   // 43   VolConv              Nm3       U             Vol. converted                                      0           99999999
		0x83200068,   // 44   VolCorrI             m3                 1 01 Vol. corrected                                                              Vol. corr.
		0xC330006C,   // 45   VolCorrF             m3                 1 0  Vol. corr. dec.                                                             Vol. corr. dec
		0x83200070,   // 46   VolConvI             Nm3       U        1 01 Vol. conv.                                                                  Vol. conv.
		0xC3300074,   // 47   VolConvF             Nm3       U        1 0  Vol. conv. dec.                                                             Vol. conv. dec
		0x09F01A18,   // 48   AlarmSetupTable      192        R            Alarm setup table
		0x09100126,   // 49   Flowstop             seconds   UR            Flowstop after                                      10          600
		0x493000FC,   // 50   TrykMin              BarA      UR       1    Pressure low limit                                  0.6         80          Press. low limit
		0x49300100,   // 51   TrykMax              BarA      UR       1    Pressure high limit                                 0.6         80          Press. high limit
		0x29300104,   // 52   TempMin              °C         R       1    Temperature low limit                               -40         70          Temp. low limit
		0x29300108,   // 53   TempMax              °C         R       1    Temperature high limit                              -40         70          Temp. high limit
		0x2930010C,   // 54   FlowCorrMax          m3/h       R       1    Flow high limit                                     0           99999999    Flow high limit
		0x29300110,   // 55   FlowConvMax          Nm3/h     UR       1    Conv. flow high limit                               0           99999999    Conv. flow high limit
		0x29300114,   // 56   EnergiMax            MJ/h      UR       1    Power high limit                                    0           99999999    Power high limit
		0xDB300118,   // 57   TrykVFejl            BarA      UR            Fallback press. used on error                       0.6         80
		0xAB30011C,   // 58   TempVFejl            °C         R            Fallback temp. used on error                        -40         70
		0x0B0000C1,   // 59   DispOffTime          Sec.      UR            Turn off display after                              4           240
		0xDB300128,   // 60   Pb                   BarA      UR       1 1  Base pressure                                       0.6         80          Base press.
		0xAB30012C,   // 61   Tb                   °C         R       1 1  Base temperature                                    -40         70          Base temp.
		0x9B3000DC,   // 62   MaxPress             BarA      UR            Pressure range                                      0.6         80
		0x9B3000E0,   // 63   MinPress             BarA      UR            Pressure range                                      0.6         80
		0x9B3000E4,   // 64   MaxTemp              °C         R            Temperature range                                   -40         70
		0x9B300130,   // 65   MinTemp              °C         R            Temperature range                                   -40         70
		0x07F00234,   // 66   AlarmTable           12                      Alarm table
		0x03F01BE4,   // 67   AlarmCntTable        96         0            Alarm cnt table
		0x2730008C,   // 68   EnergiFlow           MJ/h      U        1R0 1Power                                                                       Power
		0x010000F5,   // 69   Alarmset                                     Set alarm reg in uniflo
		0x07901E02,   // 70   TDeviceID                                R   Manufacture and type                                                        P. Sens. type
		0x07A01E1F,   // 71   TPStyle                                  R   Tryksensor Pressure style
		0x07D01E20,   // 72   TPressRange                             1R   Range [bar]                                                                 Range [bar]
		0x07C01E24,   // 73   TSerialno                               1R   Serial no.                                                                  P. sens no.
		0x07001E28,   // 74   TCalibDay                                R   Date of calibration                                2
		0x07001E29,   // 75   TCalibMonth                              R   Date of calibration                                2
		0x07001E2A,   // 76   TCalibYear                               R   Date of calibration                                2
		0x57D01E2B,   // 77   TPres1Bar                                R   Tryksensor Pressure signal at 1 bar at 20 deg. C
		0x57D01E2F,   // 78   TPresFS                                  R   Tryksensor Pressure signal FS at 20 deg. C
		0x57D01E33,   // 79   TTemp                                    R   Tryksensor temperature signal at 20 deg. C
		0x01101E04,   // 80   ResetAlarm                                   Reset alarm
		0x03101E06,   // 81   ClrAlarm                                     Clear alarm
		0x897000EA,   // 82   ADAmp                                        AD amplify
		0x03101E0A,   // 83   ClrEvent                                     Delete configuration log
		0x02001E0C,   // 84   LockTable                                    Lock correction table
		0x02001E0E,   // 85   UnLockTable                                  UnLock correction table
		0x02001E10,   // 86   OpdateCorrTable                              Update correction table
		0x02001E12,   // 87   OpdateGasData                                Update Gas data
		0x01101E14,   // 88   Snapshot                                     Make a snapshot in Uniflo
		0x017000C8,   // 89   PulsOut1En                      R            Puls out 1 enable
		0x017000C9,   // 90   PulsOut2En                      R            Puls out 2 enable
		0x0B601B98,   // 91   Menufile                        R            Menu file
		0x070000D7,   // 92   IntvAntal                                R   x
		0x030000CA,   // 93   GSetup                                       Graphic display gain setup
		0x030000CB,   // 94   CSetup                                       Graphic display contrast setup
		0xC3400090,   // 95   Energi               MJ        U             Energy                                              0           99999999
		0x83200090,   // 96   EnergiI              MJ        U        1 01 Energy                                                                      Energy
		0xC3300094,   // 97   EnergiF              MJ        U        1 0  Energy dec.                                                                 Energy dec.
		0xF10000D8,   // 98   OptionCH                                     Options kort changed
		0x0B7000E8,   // 99   TempSource                      R            Temperatur sensor sourse
		0x0B7000E9,   // 100  PresSource                      R            Pressure sensor source
		0x07F00234,   // 101  Alarm active 1-32                        R0  Alarm active 1-32
		0x07F00238,   // 102  Alarm active 33-64                       R0  Alarm active 33-64
		0x07F0023C,   // 103  Alarm active 64-96                       R0  Alarm active 64-96
		0x07F01BD8,   // 104  Alarm reg. 1-32                          R0  Alarm reg. 1-32
		0x07F01BDC,   // 105  Alarm reg. 33-64                         R0  Alarm reg. 33-64
		0x07F01BE0,   // 106  Alarm reg. 64-96                         R0  Alarm reg. 64-96
		0x01F000CC,   // 107  IOTabel              10                  R   I/O - tabel
		0xC3400098,   // 108  VolMeasured          m3                      Volume measured                                     0           99999999
		0x07B01E8A,   // 109  TChecksum                                R   Pressure sensor check sum
		0x0BF0FFE0,   // 110  IntvalLogreg         20         R            Interval log registre
		0x09F04000,   // 111  DagsLogreg           20         R            24 hour log registre
		0x09F06000,   // 112  Snapshotreg          20         R            Snapshot log registre
		0x09F02000,   // 113  AlarmAktLogreg       20         R            Alarm triggered log registre
		0x07601DC0,   // 114  VerTyp                                  1R   Uniflo type and version                                                     Type/version
		0x01601588,   // 115  Installation                    R       1    Installation no.                                                            Installation no.
		0x0160159E,   // 116  Maalernr                        R       1    Meter no.                                                                   Meter no.
		0x016015B4,   // 117  MaalerSize                      R       1    Meter size                                                                  Meter size
		0x016015CA,   // 118  Kunde                           R       1    Customer                                                                    Customer
		0x016015E0,   // 119  Installationsdato               R       1    Date of installation                                                        Installation date
		0x016015F6,   // 120  Counter                         R       1    Meter index                                                                 Meter index
		0x0160160C,   // 121  Sag                             R       1    Project no.                                                                 Project no.
		0x09601622,   // 122  Serienr                         R       1    Uniflo serial no.                                                           Serial no.
		0x01601638,   // 123  Flowmaaler                      R       1    Manufacture and type                                                        Flow type
		0x0160164E,   // 124  Tempmaaler                      R       1    Manufacture and type                                                        Temp type
		0x0B10FFF4,   // 125  IntvalLogDyb                    R            No. of logs
		0x09104014,   // 126  DagsLogDyb                      R            No. of logs
		0x09106014,   // 127  SnapshotDyb                     R            No. of logs
		0x09102014,   // 128  AlarmAktDyb                     R            No. of logs
		0x0B00FFF6,   // 129  IntvalLogBredde                 R            No. of log points
		0x09004016,   // 130  DagslogBredde                   R            No. of log points
		0x09006016,   // 131  SnapshotBredde                  R            No. of log points
		0x09002016,   // 132  AlarmAktBredde                  R            No. of log points
		0x273000A0,   // 133  FlowUnCorr           m3/h               1R0 1Flow measured                                                               Flow meas.
		0xDA301668,   // 134  Methan                          R       1    Methane                                                                     Methane
		0xDA30166C,   // 135  Nitrogen                        R       1    Nitrogen                                                                    Nitrogen
		0xDA301670,   // 136  CarbonDioxide                   R       1    CO2                                                                         CO2
		0xDA301674,   // 137  Ethan                           R       1    Ethane                                                                      Ethane
		0xDA301678,   // 138  Propan                          R       1    Propane                                                                     Propane
		0xDA30167C,   // 139  Water                           R       1    Water                                                                       Water
		0xDA301680,   // 140  HydrogenSylfide                 R       1    Hydrg. Sul.                                                                 Hydrg. Sul.
		0xDA301684,   // 111  Hydrogen                        R       1    Hydrogen                                                                    Hydrogen
		0xDA301688,   // 142  CarbonMonoxide                  R       1    Carb. mo.                                                                   Carb. mo.
		0xDA30168C,   // 143  Oxygen                          R       1    Oxygen                                                                      Oxygen
		0xDA301690,   // 144  iButan                          R       1    i-Butane                                                                    i-Butane
		0xDA301694,   // 145  nButan                          R       1    n-Butane                                                                    n-Butane
		0xDA301698,   // 146  iPentan                         R       1    i-Pentane                                                                   i-Pentane
		0xDA30169C,   // 147  nPentan                         R       1    n-Pentane                                                                   n-Pentane
		0xDA3016A0,   // 148  nHexan                          R       1    n-Hexane                                                                    n-Hexane
		0xDA3016A4,   // 149  nHeptan                         R       1    n-Heptane                                                                   n-Heptane
		0xDA3016A8,   // 150  nOctan                          R       1    n-Octane                                                                    n-Octane
		0xDA3016AC,   // 151  nNontan                         R       1    n-Nonane                                                                    n-Nonane
		0xDA3016B0,   // 152  nDecan                          R       1    n-Decane                                                                    n-Decane
		0xDA3016B4,   // 153  Helium                          R       1    Helium                                                                      Helium
		0xDA3016B8,   // 154  Argon                           R       1    Argon                                                                       Argon
		0x8A7016BC,   // 155  BeregningsMetode                R       1    Formular                                                                    Formular
		0xFA6016BE,   // 156  GasComp                         R       1    Gas composition                                                             Gas comp.
		0x8A5016D6,   // 157  RevTime                         R       1R   Time of rev.                                                                Comp. rev.
		0xDA3016DC,   // 158  Density                         R       1    Density (rel.)                                      0.1         2           Density rel.
		0x0AF016E0,   // 159  Gastabel             800        R            Gas correction tabel
		0x0AE01A00,   // 160  Password3                       R            Password level 3
		0x0AE01A08,   // 161  Password2                       R            Password level 2
		0x0AE01A10,   // 162  Password1                       R            Password level 1
		0x007000C3,   // 163  LogReOrg                        R            Display mode
		0x573000A4,   // 164  KorrF                                   1R0 1Corrections factor                                                          Corr. fact.
		0x07F00200,   // 165  IOVersion            14                  R   IO version
		0x07F0020E,   // 166  IOSerienr            28                  R   IO serienr
		0x07130300,   // 167  HFCard                                   R   HF/Puls subtype
		0x07F00200,   // 168  SNTabel              40                  R   I/O - tabel
		0xFB601B7C,   // 169  TempStregkode                           1    Temperture code                                                             Temperture code
		0x23300020,   // 170  ITempMin             °C         0        R1  Min. temp.
		0x23300024,   // 171  ITempMax             °C         0        R1  Max. temp.
		0x43300028,   // 172  ITrykMin             BarA      U0        R1  Min. Press.
		0x4330002C,   // 173  ITrykMax             BarA      U0        R1  Max. Press.
		0x23300030,   // 174  IFlowUMin            m3/h       0        R1  Min. flow corr.
		0x23300034,   // 175  IFlowUMax            m3/h       0        R1  Max. flow corr.
		0x23300038,   // 176  IFlowKMin            Nm3/h     U0        R1  Min. flow conv.
		0x2330003C,   // 177  IFlowKMax            Nm3/h     U0        R1  Max. flow conv.
		0x23300040,   // 178  DTempMin             °C         0        R2  Min. temp.
		0x23300044,   // 179  DTempMax             °C         0        R2  Max. temp.
		0x43300048,   // 180  DTrykMin             BarA      U0        R2  Min. Press.
		0x4330004C,   // 181  DTrykMax             BarA      U0        R2  Max. Press.
		0x23300050,   // 182  DFlowUMin            m3/h       0        R2  Min. flow corr.
		0x23300054,   // 183  DFlowUMax            m3/h       0        R2  Max. flow corr.
		0x23300058,   // 184  DFlowKMin            Nm3/h     U0        R2  Min. flow conv.
		0x2330005C,   // 185  DFlowKMax            Nm3/h     U0        R2  Max. flow conv.
		0x23300018,   // 186  IEnergyFMin          MJ        U0        R1  Min. power
		0x2330001C,   // 187  IEnergyFMax          MJ        U0        R1  Max. power
		0x23300060,   // 188  DEnergyFMin          MJ        U0        R2  Min. power
		0x23300014,   // 189  DEnergyFMax          MJ        U0        R2  Max. power
		0x83200098,   // 190  VolMeasuredI         m3                 1 01 Vol. measured                                                               Vol. meas.
		0xC330009C,   // 191  VolMeasuredF         m3                 1 0  Vol. measured dec.                                                          Vol. meas. dec
		0xC34000A8,   // 192  VolErr               m3                      Vol. meas. at error                                 0           99999999
		0x832000A8,   // 193  VolErrI              m3                 1 01 Vol. meas. at error                                                         Vol. err.
		0xC33000AC,   // 194  VolErrF              m3                 1 0  Vol. meas. at err. dec.                                                     Vol. err. dec
		0xBA301B08,   // 195  Heatvalue            MJ/mn3    UR       1    Superior heat value                                 19          48          S. heat value
		0xCA301B92,   // 196  C6Value                         R       1    C6+ value                                                                   C6+ value
		0x8A701B96,   // 197  C6Enable                        R       1    C6+                                                                         C6+ enable
		0x8A000008,   // 198  ADTrykOffset                                 AD pressure offset                                  -128        127
		0x8A000009,   // 199  ADTempTrykOffset                             AD pressure temperature offset                      -128        127
		0x8A7000D4,//200 256  CountVmAtError                  R            Count Vm at Error
		0x8A7000D5,//201 257  TZMode                          R            Corrector Type
		0xDA300118,//202 258  TZTryk               bar A     UR            Pressure [bar A]                                    0.6         80
		0x0370C000,//203 259  Menu/Alarm           31768                   Menu og alarm text for grafisk display
		0x03000000,//204 260  NoOfSlaves                              0    Number of slaves                                                            Number of slaves
		0x00101E08,//205 261  OpdataerEEProm                               Opdater EEProm
		0x0B601BAE,//206 262  Alarmfile                       R            Alarm text file
		0x00000000,//207 263  No output                                   1No output
		0x02001E16,//208 264  LockDisp                                     Lock graphic display
		0x02001E18,//209 265  UnLockDisp                                   UnLock graphic display
		0x832000B8,//210 266  VolCtrlI             m3                 1 01 Vol. control                                                                Vol. ctrl.
		0xC33000BC,//211 267  VolCtrlF             m3                 1 0  Vol. control dec.                                                           Vol. ctrl. dec
		0x07200240,   // 212  VolHourI             m3                 1R0  Current hour incr.                                                          Hour incr. 
		0x47300244,   // 213  VolHourF             m3                 1R0  Current incr. dec.                                                          Hour incr. dec
		0x07500248,   // 214  MaxTime                                 1R0  1.Max hour incr. time                                                       1. Max incr. time
		0x0720024C,   // 215  MaxVolI              m3                 1R0  1.Max hour incr.                                                            1. Max incr. 
		0x47300250,   // 216  MaxVolF              m3                 1R0  1.Max hour incr. dec.                                                       1. Max incr. dec
		0x07500254,   // 217  MaxTime2                                1R0  2.Max hour incr. time                                                       2. Max time
		0x07200258,   // 218  MaxVolI2             m3                 1R0  2.max hour incr.                                                            2. Max incr.
		0x4730025C,   // 219  MaxVolF2             m3                 1R0  2.max hour incr. dec.                                                       2. Max incr. dec
		0x07500260,   // 220  MaxTime3                                1R0  3.Max hour incr. time                                                       3. Max time
		0x07200264,   // 221  MaxVolI3             m3                 1R0  3.max hour incr.                                                            3. Max incr.
		0x47300268,   // 222  MaxVolF3             m3                 1R0  3.max hour incr. dec.                                                       3. Max incr. dec
		0x0F201CE6,   // 223  PresureCalOP                            1    Pressure calibration operator                                               Press. cal. op.
		0x0F201CEA,   // 224  TempCalOP                               1    Temperature calibration operator                                            Temp. cal. op.
		0x071000EC,   // 225  EventlogIdx                              R   Indexpointer for Event log
		0x071000EE,   // 226  AlarmlogIdx                              R   Indexpointer for Alarmlog log
		0x0B70000D,   // 227  IntervallogState                R            Log interval
		0x0A00000A,   // 228  MaalInterval         Sec.      UR       1    Interval of measurement                             0           254         Cycl. of meas.
		0x0900000B,   // 229  DagslogTime                     R            Time of Day                                         0           24
		0x0900000C,   // 230  DagslogMin                      R            Time of Day                                         0           60
		0x07100148,   // 231  IntervallogIdx                               Indexpointer for interval log
		0x0710014A,   // 232  DagslogIdx                               R   Indexpointer for dagslog
		0x0710014C,   // 233  SnapshotlogIdx                           R   Indexpointer for snapshot log
		0x0710014E,   // 234  AlarmtriglogIdx                          R   Indexpointer for alarmaktiveret log
		0x090000C0,   // 235  DispTime             Sec.      UR            Change to display 1 after
		0x090000C2,   // 236  MaxpulsError                    R            Max. pulse error
		0xF3101F00,   // 237  ChangePressSensor                            Pressure sensor is change
		0xF3101F02,   // 238  ChangeTempSensor                             Temperature sensor is change
		0x0B007016,   // 239  MonthBredde                     R            No. of log points
		0x0B107014,   // 240  MonthDyb                        R            No. of logs
		0x0BF07000,   // 241  MonthLogreg          20         R            Month log registre
		0x071001F2,   // 242  MonthlogIdx                              R   Indexpointer for Month log
		0x02001E1A,   // 243  Disptest                                     Display test
		0x00FFFFFF,   // 244  FirmwareUpdate                               Firmware update
		0xF3101E1C,   // 245  FUPDATESTART                                 Firmware update start
		0xF3101E1E,   // 246  FUPDATEEND                                   Firmware update end
		0xF3101E20,   // 247  FUPDATECANCEL                                Firmware update cancel
		0x07100150,   // 248  PRGChecksum                             1R   Program checksum                                                            Prg. chksum
		0xFBF00194,   // 249  TempCorr             30                      Temperature calibretion tabel
		0xFBF001B2,   // 250  PressCorr            60                      Pressure calibretion tabel
		0x00101E22,   // 251  ForceMesurement                              Force measurment
		0x0F501CDA,   // 252  PresureCalTime                          1    Pressure calibration time                                                   Press. cal. Time
		0x0F501CE0,   // 253  TempCalTime                             1    Temperature calibration time                                                Temp. cal. Time
		0xFA101CEE,   // 254  ConvTableChecksum                       1R   Conversion table checksum                                                   Conv. table chksum
		0xFA101CF0,   // 255  ConvTableDLLChecksum                    1R   DLL checksum                                                                DLL checksum
	};

	
}
