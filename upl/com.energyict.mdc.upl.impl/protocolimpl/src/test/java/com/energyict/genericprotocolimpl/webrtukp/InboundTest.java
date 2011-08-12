package com.energyict.genericprotocolimpl.webrtukp;

import java.io.IOException;
import java.net.*;

import org.junit.Ignore;
import org.junit.Test;

public class InboundTest {

    private static final String LOCALHOST = "naessens-govanni";

    @Ignore
    @Test
    public void inboundTest() throws IOException, InterruptedException {

        String dataToSend = "test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1test1";

        DatagramSocket ds = new DatagramSocket();
        ds.connect(InetAddress.getByName("localhost"), 4056);
        System.out.println("Length of sending data : " + dataToSend.length());
        ds.send(new DatagramPacket(dataToSend.getBytes(), dataToSend.length()));

        Thread.sleep(5000);

//        Socket socket = new Socket(LOCALHOST, 4058);
//        socket.getOutputStream().write("<DEPLOY>ipPort=5678, meterType=AS230,serialId=12345,dbaseId=5,ipAddress=192.168.0.1</DEPLOY>".getBytes());
//        socket.getOutputStream().write(("<REQUEST>serialId=SomeSerialNumber100</REQUEST>").getBytes());

//        for(int i = 0; i < 100; i++){
//            try {
//                Socket socket = new Socket(LOCALHOST, 4058);
//                socket.getOutputStream().write(("<REQUEST>serialId=SomeSerialNumber"+i+"</REQUEST>").getBytes());
//            } catch (IOException e) {
//                System.out.println("Could not acquire connection " + i);
//                Thread.sleep(1000);
//            }
//        }
//
//
//        for(int i = 0; i < 100 ;i++){
//            try {
//                Socket socket = new Socket(LOCALHOST, 4057);
//                socket.getOutputStream().write(("<REQUEST>serialId=SomeSerialNumber"+i+"</REQUEST>").getBytes());
//            } catch (IOException e) {
//                System.out.println("Could not acquire connection " + i);
//                Thread.sleep(1000);
//            }
//        }
//        Socket socket2 = new Socket(LOCALHOST, 4059);
//        socket2.getOutputStream().write("<REQUEST>serialId=SomeSerialNumber1</REQUEST>".getBytes());
//
//        Socket socket3 = new Socket(LOCALHOST, 4059);
//        socket3.getOutputStream().write("<REQUEST>serialId=SomeSerialNumber2</REQUEST>".getBytes());
//
//        Socket socket4 = new Socket(LOCALHOST, 4059);
//        socket4.getOutputStream().write("<REQUEST>serialId=SomeSerialNumber3</REQUEST>".getBytes());

    }
}
