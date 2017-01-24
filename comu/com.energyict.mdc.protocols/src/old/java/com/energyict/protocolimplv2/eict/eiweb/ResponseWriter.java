package com.energyict.protocolimplv2.eict.eiweb;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Writes responses back to the eiweb device.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-17 (11:20)
 */
public class ResponseWriter {

    private static final int SUCCESS_VALUE_INDICATOR = 0;
    private static final int FAILURE_VALUE_INDICATOR = -1;

    private PrintWriter writer;

    public ResponseWriter(HttpServletResponse response) throws IOException {
        this(new PrintWriter(response.getOutputStream()));
    }

    private ResponseWriter(PrintWriter writer) {
        super();
        this.writer = writer;
    }

    public void success() {
        this.writeClockSyncAndResult(SUCCESS_VALUE_INDICATOR);
    }

    public void failure() {
        this.writeClockSyncAndResult(FAILURE_VALUE_INDICATOR);
    }

    public void add(String messageContent) {
        this.writer.println(messageContent);
    }

    private void writeClockSyncAndResult(int value) {
        this.writeClockSync();
        this.writeResult(value);
        this.writer.flush();
    }

    private void writeClockSync() {
        long utc = (System.currentTimeMillis() / 1000L) - EIWebConstants.SECONDS10YEARS;
        this.writer.println("<CLOCKUTC>" + utc + "</CLOCKUTC>");
    }

    private void writeResult(int value) {
        this.writer.println("<RESULT>" + value + "</RESULT>");
    }

}