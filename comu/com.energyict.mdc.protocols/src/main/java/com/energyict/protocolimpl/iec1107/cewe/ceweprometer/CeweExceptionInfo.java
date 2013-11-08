package com.energyict.protocolimpl.iec1107.cewe.ceweprometer;

import java.util.HashMap;
import java.util.Map;

/**
 * Copyrights EnergyICT
 * Date: 6/05/11
 * Time: 8:58
 */
public class CeweExceptionInfo {

    private static final Map<String, String> EXCEPTION_INFO = new HashMap<String, String>();

    static {
        EXCEPTION_INFO.put("001", "Non existing main id: There is no message with the requested main id. Main id is the first four leading digits in the message id. i.e. 0152 in 015200");
        EXCEPTION_INFO.put("002", "Non existing sub id: There are no message with the requested sub id. Sub id is the last two digits in the message id. i.e. 00 in 015200");
        EXCEPTION_INFO.put("003", "Locations requested exceeds limit: A maximum of 16 locations may be read in a single read command e.g. R1<STX>101200(33) is not accepted and the meter returns error message 003");
        EXCEPTION_INFO.put("004", "Data format error: The format set in a data message has not a valid format, e.g. a character string is sent when the meter expects an integer.");
        EXCEPTION_INFO.put("005", "Data content error: The data set in a data message has not a valid content, e.g. a value is given outside valid limits.");
        EXCEPTION_INFO.put("006", "Message read only: Returned when a write command is sent to a read only message.");
        EXCEPTION_INFO.put("007", "Message write only: Returned when a read command is sent to a write only message.");
        EXCEPTION_INFO.put("008", "Reserved");
        EXCEPTION_INFO.put("009", "General error message.");
        EXCEPTION_INFO.put("010", "Access denied: Returned when current access level is not enough for the requested message.");
    }

    public static String getExceptionInfo(String id) {
        String info = EXCEPTION_INFO.get(id);
        String defaultMessage = "Error: '" + id + "' occurred ";
        return info == null ? defaultMessage : info;
    }

}
