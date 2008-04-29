package com.energyict.genericprotocolimpl.lgadvantis;

import junit.framework.TestCase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

public class HttpTimeoutTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testHttpTimeout() {
        try {
            int msTimeout = 5000;

            HttpTimeoutHandler th = new HttpTimeoutHandler(msTimeout);
            URL aUrl = new URL("http", "10.0.0.88", 80, "/ReportAllValues", th);

            URLConnection conn = aUrl.openConnection();

            conn.setDoOutput(true);
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
            wr.write("");
            wr.flush();
            System.out.println(new Date().toString());
            InputStreamReader reader = new InputStreamReader(conn.getInputStream());
            BufferedReader bReader = new BufferedReader(reader);

            StringBuffer sb = new StringBuffer();
            String line;

            while ((line = bReader.readLine()) != null) {
                sb.append(line);
            }

            wr.close();
            bReader.close();
        } catch (MalformedURLException e) {
            assertTrue("Exception occurred: " + e.getMessage(), false);
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println(new Date().toString());
            assertTrue("Exception occurred: " + e.getMessage(), false);
            e.printStackTrace();
        }

    }

}
