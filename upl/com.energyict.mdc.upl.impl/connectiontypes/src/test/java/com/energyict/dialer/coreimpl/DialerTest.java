/*
 * DialerTest.java
 *
 * Created on 14 april 2004, 15:34
 */

package com.energyict.dialer.coreimpl;

import com.energyict.dialer.core.Dialer;
import com.energyict.dialer.core.DialerFactory;
import com.energyict.dialer.core.LinkException;
import com.energyict.dialer.core.SerialCommunicationChannel;

import java.io.IOException;

import org.junit.Ignore;

/**
 * @author Koen
 */
@Ignore
public class DialerTest {

    /**
     * Establish a connection to 0479972472 via a local com port
     */
    public void start() {
        try {
            int count = 0;
            System.out.println("start DialerTest");
            Dialer dialer = new ATDialer();
            dialer.init("COM1", "AT+CBST=71,0,1", "ATZ", "0479");
            dialer.connect("972472", 60000);

            try {
                System.out.println("connected, send I");
                dialer.getOutputStream().write("I".getBytes());

                while (true) {
                    int av = dialer.getInputStream().available();
                    if (av != 0) {

                        byte[] data = new byte[av];

                        int received = dialer.getInputStream().read(data, 0, av);
                        System.out.println("av=" + av + ", received=" + received);
                        for (byte kar : data) {
                            System.out.print((char) kar);
                        }
                        count = 0;
                    } else {
                        Thread.sleep(100);
                        if (count++ >= 100) {
                            System.out.println();
                            System.out.println("KV_DEBUG> connection timeout DialerTest");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialer.disConnect();
            System.out.println("end DialerTest");
        } catch (LinkException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Establish a connection to 0479972472 via a virtual com port
     */
    public void start5() {
        try {
            int count = 0;
            System.out.println("start DialerTest");
            Dialer dialer = new ATDialer();
            //dialer.init("10.0.0.42:9000:COM1","AT+CBST=71,0,1","ATZ","0479");
            dialer.init("koenportable:9000:COM1", "AT+CBST=71,0,1", "ATZ", "0479");
            dialer.connect("972472", 60000);

            try {
                System.out.println("connected, send I");
                dialer.getOutputStream().write("I".getBytes());

                while (true) {
                    if (dialer.getInputStream().available() != 0) {

                        int kar = dialer.getInputStream().read();
                        System.out.print((char) kar);
                        count = 0;
                    } else {
                        Thread.sleep(100);
                        if (count++ >= 100) {
                            System.out.println();
                            System.out.println("KV_DEBUG> connection timeout DialerTest");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialer.disConnect();
            System.out.println("end DialerTest");
        } catch (LinkException | IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * Establish a connection to 0479972472 via a virtual com port
     */
    public void start4() {
        try {
            int count = 0;
            System.out.println("start DialerTest");
            Dialer dialer = new EMDialDialer();
            dialer.init("10.0.0.42:9000:COM1", "AT+CBST=71,0,1", "ATZ", "0479");
            dialer.connect("972472", "$23", 60000);

            try {
                System.out.println("connected, send I");
                dialer.getOutputStream().write("I".getBytes());

                while (true) {
                    if (dialer.getInputStream().available() != 0) {

                        int kar = dialer.getInputStream().read();
                        System.out.print((char) kar);
                        count = 0;
                    } else {
                        Thread.sleep(100);
                        if (count++ >= 100) {
                            System.out.println();
                            System.out.println("KV_DEBUG> connection timeout DialerTest");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialer.disConnect();
            System.out.println("end DialerTest");
        } catch (LinkException | IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Use an IPDialerSelector to establish a TCP connection to an address with a postDial Command
     */
    public void start3() {
        try {
            int count = 0;
            System.out.println("start DialerTest");
            Dialer dialer = new IPDialerSelector();
            dialer.init("TCP1");
            dialer.connect("10.0.0.42:23", "$12", 60000);

            try {
                System.out.println("Send ATZ");
                dialer.getOutputStream().write("ATZ\r\n".getBytes());
                Thread.sleep(500);
                dialer.getOutputStream().write("ATZ\r\n".getBytes());
                Thread.sleep(500);
                dialer.getOutputStream().write("ATZ\r\n".getBytes());
                while (true) {
                    if (dialer.getInputStream().available() != 0) {

                        int kar = dialer.getInputStream().read();
                        System.out.print((char) kar);
                        count = 0;
                    } else {
                        Thread.sleep(100);
                        if (count++ >= 100) {
                            System.out.println();
                            System.out.println("KV_DEBUG> connection timeout DialerTest");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialer.disConnect();
            //dialer2.disConnect();


            System.out.println("end DialerTest");
        } catch (LinkException | IOException e) {
            e.printStackTrace();
        }
    }

    /*
    *  Stacking dialers:
    *  Connecting ATDialer via DirectDialer, via IPDialer
    *
    */
    public void start2() {
        try {
            int count = 0;
            System.out.println("start DialerTest");
            Dialer dialer = new IPDialer();
            dialer.init("TCP1");
            Dialer dialer2 = new DirectDialer();
            dialer2.init(null);
            Dialer dialer3 = new ATDialer();
            dialer3.init(null, "AT+CBST=71,0,1", "ATZ", null);
            dialer2.connectDialer(dialer3, "0479972472", null, 600000);
            dialer.connectDialer(dialer2, null, null, 60000);
            dialer.connect("10.0.0.42:9000:COM1", "$12", 60000);   // single virtualcom
            //dialer.connect("10.0.0.42:23","$12",60000);   // multiple virtualcom
            try {
                System.out.println("connected, send I");
                dialer.getOutputStream().write("I".getBytes());

                while (true) {
                    if (dialer.getInputStream().available() != 0) {

                        int kar = dialer.getInputStream().read();
                        System.out.print((char) kar);
                        count = 0;
                    } else {
                        Thread.sleep(100);
                        if (count++ >= 100) {
                            System.out.println();
                            System.out.println("KV_DEBUG> connection timeout DialerTest");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialer.disConnect();
            System.out.println("end DialerTest");
        } catch (LinkException | IOException e) {
            e.printStackTrace();
        }
    }


    private void sendreceive() {
        final Dialer dialer = DialerFactory.getDirectDialer().newDialer();
        try {
            dialer.init("COM1");

            try {
                dialer.getSerialCommunicationChannel().setParams(115200,
                        SerialCommunicationChannel.DATABITS_8,
                        SerialCommunicationChannel.PARITY_NONE,
                        SerialCommunicationChannel.STOPBITS_1);
                dialer.connect();
                dialer.getOutputStream().write("\n\r".getBytes());
                int count = 0;
                while (true) {
                    int av = dialer.getInputStream().available();
                    if (av != 0) {

                        byte[] data = new byte[av];

                        int received = dialer.getInputStream().read(data, 0, av);
                        System.out.println("av=" + av + ", received=" + received);
                        for (byte kar : data) {
                            System.out.print((char) kar);
                        }


                        count = 0;
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        if (count++ >= 10) {
                            System.out.println();
                            System.out.println("KV_DEBUG> connection timeout DialerTest");
                            break;
                        }
                    }
                }

                dialer.disConnect();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (LinkException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /*
    * direct connection
    */
    private void start1() {
        sendreceive();
        sendreceive();
        sendreceive();
        sendreceive();
    }

    public static void main(String[] args) {
        DialerTest dt = new DialerTest();
        dt.start1();
    }

}