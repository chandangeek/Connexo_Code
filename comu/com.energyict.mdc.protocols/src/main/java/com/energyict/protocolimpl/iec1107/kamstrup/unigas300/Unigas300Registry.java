/*
 * Unigas300Register.java
 *
 * Created on 16 juni 2003, 16:35
 */

package com.energyict.protocolimpl.iec1107.kamstrup.unigas300;

import com.energyict.cbo.BaseUnit;
import com.energyict.cbo.Unit;
import com.energyict.obis.ObisCode;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;

import java.io.IOException;

/**
 *
 * @author  Koen
 */
public class Unigas300Registry extends AbstractVDEWRegistry {

	private static final RegisterMappingFactory rmf = new RegisterMappingFactory();

    /** Creates a new instance of Unigas300Register */
    public Unigas300Registry(ProtocolLink protocolLink) {
        super(null,protocolLink);
    }

    protected void initRegisters() {

    	add(RegisterMappingFactory.VM1, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.VC1_ERR, VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.VC1, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.VB1, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.VB1_ERR,	VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.VM2, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.VM3, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);

    	add(RegisterMappingFactory.STATUS1,	VDEWRegisterDataParse.VDEW_STRING, null);
    	add(RegisterMappingFactory.STATUS2,	VDEWRegisterDataParse.VDEW_STRING, null);
    	add(RegisterMappingFactory.STATUS3,	VDEWRegisterDataParse.VDEW_STRING, null);
    	add(RegisterMappingFactory.STATUS4,	VDEWRegisterDataParse.VDEW_STRING, null);

    	add(RegisterMappingFactory.CF, 		VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.C, 		VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.Z, 		VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.Z_ZB, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.P,		VDEWRegisterDataParse.VDEW_QUANTITY, null);
    	add(RegisterMappingFactory.T, 		VDEWRegisterDataParse.VDEW_QUANTITY, 8, Unit.get(BaseUnit.DEGREE_CELSIUS, -2));

        add(RegisterMappingFactory.PMAX_YESTERDAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMAX_YESTERDAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.PMIN_YESTERDAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMIN_YESTERDAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QC_YESTERDAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QB_YESTERDAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);

        add(RegisterMappingFactory.PMAX_LASTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMAX_LASTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.PMIN_LASTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMIN_LASTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QC_LASTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QB_LASTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);

        add(RegisterMappingFactory.PMAX_LASTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMAX_LASTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.PMIN_LASTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMIN_LASTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QC_LASTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QB_LASTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);

        add(RegisterMappingFactory.PMAX_TODAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMAX_TODAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.PMIN_TODAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMIN_TODAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QC_TODAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QB_TODAY, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);

        add(RegisterMappingFactory.PMAX_CURRENTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMAX_CURRENTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.PMIN_CURRENTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMIN_CURRENTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QC_CURRENTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QB_CURRENTMONTH, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);

        add(RegisterMappingFactory.PMAX_CURRENTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMAX_CURRENTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.PMIN_CURRENTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.TMIN_CURRENTYEAR, 	VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QC_CURRENTYEAR, 		VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);
        add(RegisterMappingFactory.QB_CURRENTYEAR, 		VDEWRegisterDataParse.KAMSTRUP300_DATE_VALUE_PAIR, null);

        add(RegisterMappingFactory.APPLIANCE_TYPE, 	VDEWRegisterDataParse.VDEW_STRING, null);
        add(RegisterMappingFactory.DEVICE_SERIAL, 	VDEWRegisterDataParse.VDEW_STRING, null);
        add(RegisterMappingFactory.DEVICE_ADDRESS, 	VDEWRegisterDataParse.VDEW_STRING, null);
        add(RegisterMappingFactory.DEVICE_EANCODE, 	VDEWRegisterDataParse.VDEW_STRING, null);

        add(RegisterMappingFactory.FW_VERSION_D, 	VDEWRegisterDataParse.VDEW_STRING, null);
        add(RegisterMappingFactory.FW_VERSION_M, 	VDEWRegisterDataParse.VDEW_STRING, null);
        add(RegisterMappingFactory.FW_CRC_D, 		VDEWRegisterDataParse.VDEW_STRING, null);
        add(RegisterMappingFactory.FW_CRC_M, 		VDEWRegisterDataParse.VDEW_STRING, null);

        add(RegisterMappingFactory.GSM_UPTIME, 			VDEWRegisterDataParse.VDEW_QUANTITY, null);
        add(RegisterMappingFactory.GSM_CONNECTIONTIME, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);
        add(RegisterMappingFactory.GSM_SIGNAL, 			VDEWRegisterDataParse.VDEW_QUANTITY, null);

        add(RegisterMappingFactory.BATTERY_V_UNILOG,	VDEWRegisterDataParse.VDEW_QUANTITY, null);
        add(RegisterMappingFactory.BATTERY_USED_UNILOG,	VDEWRegisterDataParse.VDEW_QUANTITY, null); // Only since FW 1.2.4
        add(RegisterMappingFactory.BATTERY_C_NEW, 		VDEWRegisterDataParse.VDEW_QUANTITY, null);
        add(RegisterMappingFactory.BATTERY_C_USED, 		VDEWRegisterDataParse.VDEW_QUANTITY, null);
        add(RegisterMappingFactory.BATTERY_V_UNIGAS,	VDEWRegisterDataParse.VDEW_QUANTITY, null);
        add(RegisterMappingFactory.OPERATING_HOURS, 	VDEWRegisterDataParse.VDEW_QUANTITY, null);

        add(RegisterMappingFactory.SCHEDULER_START, 	VDEWRegisterDataParse.VDEW_DATESTRING, null);

        registers.put("Time", new VDEWRegister("0.9.1",VDEWRegisterDataParse.VDEW_TIMESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Date", new VDEWRegister("0.9.2",VDEWRegisterDataParse.VDEW_DATESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("TimeDate", new VDEWRegister("0.9.1 0.9.2",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));

        registers.put("DeviceSerialNumber", new VDEWRegister("C.1.0",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

        registers.put(RegisterMappingFactory.UNIGAS_SOFTWARE_REVISION_NUMBER, new VDEWRegister("C.90.2",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put(RegisterMappingFactory.CI_SOFTWARE_REVISION_NUMBER, new VDEWRegister("C.90.3",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

    }

    private void add(String description, int parserType, Unit unit) {
    	add(description, parserType, -1, unit);
    }

    private void add(String description, int parserType, int length, Unit unit) {
    	RegisterMapping rm;
		ObisCode oc;
    	try {
			oc = rmf.findObisCode(description);
			rm = rmf.findRegisterMapping(oc);
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println(" ###### ERROR ###### " + e.getMessage());
			return;
		}
    	String register = rm.getRegisterCode();
    	registers.put(description, new VDEWRegister(register,parserType,0,length,unit,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
	}

    /**
     *
     * @param registerCode
     * @return
     * @throws IOException
     */
    public Object getRegisterFromDevice(String registerCode) throws IOException {
        Object registerContent;
        try {
            registerContent = getRegister(registerCode);
        } catch (IOException e) {
            if (e.getMessage().contains("does not exist in datareadout")) {
                registerContent = getRegister(registerCode, false);
            } else {
                throw e;
            }
        }
        return registerContent;
    }


}
