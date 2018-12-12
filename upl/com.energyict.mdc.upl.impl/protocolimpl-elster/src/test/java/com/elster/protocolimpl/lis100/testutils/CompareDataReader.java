package com.elster.protocolimpl.lis100.testutils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Class to get comparable test data as interval map
 * <p/>
 * User: heuckeg
 * Date: 21.02.11
 * Time: 16:23
 */
public class CompareDataReader {

    public CompareDataReader() {

    }

    public static String getAsDataString(String file) {
        //
        StringBuilder result = new StringBuilder();

        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yy HH:mm");
        TimeZone timeZone = TimeZone.getTimeZone("GMT+1");
        sdf.setTimeZone(timeZone);
        //sdf.set2DigitYearStart();

        InputStream stream = CompareDataReader.class.getResourceAsStream(file);

        BufferedReader data = new BufferedReader(new InputStreamReader(stream));

        int i;
        String line;
        String[] lineData;
        Date date;
        double val;
        long l;
        BigDecimal bdval;
        try {
            do {
                line = data.readLine();
                if (line == null) {
                    break;
                }

                lineData = line.split(";");
                if ((lineData.length > 1) && (lineData[1].trim().length() > 0)) {
                    date = sdf.parse(lineData[0]);

                    /* replace "," by "." */
                    String s1 = lineData[1].trim();
                    i = s1.indexOf(",");
                    if (i > 0) {
                        s1 = s1.replace(",", ".");
                    }
                    val = Double.parseDouble(s1);
                    l = Math.round(val * 10000);
                    bdval = BigDecimal.valueOf(l, 4);

                    if (lineData.length > 2) {
                        int state = Integer.parseInt(lineData[2]);
                        result.append(date).append(",").append(bdval).append(",").append(state).append("\r");
                    } else {
                        result.append(date).append(",").append(bdval).append(",-\r");
                    }
                }
            }
            while (true);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        return result.toString();
    }
}
