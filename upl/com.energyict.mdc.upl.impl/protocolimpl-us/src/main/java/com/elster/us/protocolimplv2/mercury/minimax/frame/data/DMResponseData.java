package com.elster.us.protocolimplv2.mercury.minimax.frame.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_FS;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.RESPONSE_OK;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getString;

/**
 * Represents the data returned from  the device in a response frame as a
 * result of sending an "DM" command to read multiple audit log records
 *
 * @author James Fox
 */
public class DMResponseData extends BasicResponseData {

    //Channel #1 - Avg Gas Temperature in Degrees F -- 026
    //Channel #2 - Avg Gas Pressure in PSIG -- 008
    //Channel #3 - Uncorrected Volume Total in ft^3 x100 (CCF) -- 002
    //Channel #4 - Corrected Volume Total in ft^3 x100 (CCF) -- 000
    //Channel #5 - Battery Voltage in Volts -- 048
    //Channel #6 - Gas Demand (Max Day) in ft^3 x100 (CCF) -- 053
    //Channel #7 - Gas Demand Date (Max Day Date) - ??
    //Channel #8 - Uncorrected Volume Reading  in ft^3 x100 (CCF) -- ??
    //Channel #9 - Corrected Volume Reading  in ft^3 x100 (CCF) -- ??
    //Channel #10 - Flow Rate  in ft^3 x100 per hour (CCF\HR) - ??

    private Logger logger;

    List<AuditLogRecord> records = new ArrayList<AuditLogRecord>();
    /**
     * Construct a new EventResponseDataEM
     *
     * @param bytes the data bytes received in the frame from the device
     * @throws IOException
     */
    public DMResponseData(byte[] bytes, Logger logger) throws IOException {
        super(getBytes(RESPONSE_OK));
        this.logger = logger;
        parseBytes(bytes, logger);
        setData(bytes);
    }

    private void parseBytes(byte[] bytes, Logger logger) {
        ByteArrayOutputStream recordBytes = new ByteArrayOutputStream();
        int count = 0;
        logger.info("Dealing with " + bytes.length + " bytes: " + bytes.toString());
        for (byte b : bytes) {
            if (b == CONTROL_FS) {
                handleRecordBytes(recordBytes.toByteArray());
                recordBytes.reset();
            } else {
                recordBytes.write(b);
            }
            count++;
        }
        handleRecordBytes(recordBytes.toByteArray());
        logger.info("count is " + count);
    }

    public List<AuditLogRecord> getRecords() {
        return records;
    }

    private void handleRecordBytes(byte[] recordBytes) {
        String str = getString(recordBytes);
        logger.info("Handling record bytes " + str);
        StringTokenizer tok = new StringTokenizer(str, ",");
        int count = 0;
        AuditLogRecord record = new AuditLogRecord(logger);
        records.add(record);
        while (tok.hasMoreTokens()) {
            String token = tok.nextToken();
            switch (count) {
                case 0:
                    // It is the date
                    record.setDate(token);
                    break;
                case 1:
                    // It is the time
                    record.setTime(token);
                    break;
                default:
                    record.addStuff(token);
            }
            count++;
        }
    }
}