package com.energyict.protocols.mdc.inbound.general;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Calendar;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 8:44 AM
 */
public class RequestDiscoverRealtimeTest {

    private static final String LOCALHOST = "govanni-Latitude-E6410";
    private static final String serialNumber = "EISERVERSG-2519";
    private static final String registerPush = "<REGISTER>serialId=" + serialNumber + ",1.0.1.8.0.255=" + String.valueOf(getRegisterValue()) + " kWh " + getCurrentTime() + ", 1.0.2.8.0.255=" + String.valueOf(getRegisterValue() + 10000) + " kWh " + getCurrentTime() + "</REGISTER>";
    private static final String eventPush = "<EVENT>serialId=" + serialNumber + ",event0=123 8 130906082400, event1=20 6995 130906082505</EVENT>";
    private static final String deployPush = "<DEPLOY>serialId=" + serialNumber + ",type=AS230</DEPLOY>";
    private static final String initiateRequest = "<REQUEST>serialId=" + serialNumber + "</REQUEST>";
    private final int portNumber = 4875;

    private static long getRegisterValue() {
        return System.currentTimeMillis() / 60000;
    }

    private static String getCurrentTime() {
        Calendar cal = Calendar.getInstance();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getDoubleDigitNumber(cal.get(Calendar.YEAR) - 2000));
        stringBuilder.append(getDoubleDigitNumber(cal.get(Calendar.MONTH)));
        stringBuilder.append(getDoubleDigitNumber(cal.get(Calendar.DATE)));
        stringBuilder.append(getDoubleDigitNumber(cal.get(Calendar.HOUR)));
        stringBuilder.append(getDoubleDigitNumber(cal.get(Calendar.MINUTE)));
        stringBuilder.append(getDoubleDigitNumber(cal.get(Calendar.SECOND)));
        return stringBuilder.toString();
    }

    private static String getDoubleDigitNumber(int i) {
        if (i < 10) {
            return "0" + i;
        } else {
            return String.valueOf(i);
        }
    }

    @Test
    public void registerTest() throws InterruptedException, IOException {
        int nThreads = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(nThreads);
        for (int i = 0; i < nThreads; i++) {
            executorService.submit(new SocketRunner());
        }

//        socket.getOutputStream().write(("<REQUEST>serialId=SomeSerialNumber100</REQUEST>").getBytes());

//        for (int i = 0; i < 100; i++) {
//            try {
//                Socket socket = new Socket(LOCALHOST, 4058);
//                socket.getOutputStream().write(("<REQUEST>serialId=SomeSerialNumber" + i + "</REQUEST>").getBytes());
//            } catch (IOException e) {
//                System.out.println("Could not acquire connection " + i);
//                Thread.sleep(1000);
//            }
//        }
    }

    private class SocketRunner implements Runnable {

        @Override
        public void run() {
            try {
                Socket socket = new Socket(LOCALHOST, portNumber);
                socket.getOutputStream().write(registerPush.getBytes());

                InputStream socketInputStream = socket.getInputStream();
                while (socketInputStream.available() > 0) {
                    System.out.println(socketInputStream.read());
                }
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
    }

}
