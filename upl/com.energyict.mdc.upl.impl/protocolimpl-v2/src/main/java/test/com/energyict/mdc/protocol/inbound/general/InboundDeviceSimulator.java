package test.com.energyict.mdc.protocol.inbound.general;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

/**
 * This class can be used to simulate an inbound device and can be used to test the inbound discovery path of the comserver.<br/>
 * This simulator will simulate an inbound device which contacts comserver. The comserver should have discovery 'DlmsSerialNumberDiscover' running.
 *  During discovery, the serial number of the device will be transmitted; so the discovery process can return a proper DeviceIdentifier, based on serial number.<br/>
 *  After discovery process, comserver will:
 *  <ul>
 *      <li>start readout of the device, if pending schedules are found</li>
 *      <li>add the serial number to the 'unknown devices log', in case no matching device could be fouond</li>
 *  </ul>
 *
 * @author sva
 * @since 28/05/2014 - 13:37
 */
public class InboundDeviceSimulator {

    /**
     * Start the simulator & push a frame towards comserver
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        String hostNameOfComserver = args[0];
        int portNumberOfComserver = Integer.parseInt(args[1]);
        String serialNumberOfDevice = args[2];  // Will be left padded with 0's untill length is 16

        InboundDeviceSimulator.simulateInboundDevice(hostNameOfComserver, portNumberOfComserver, serialNumberOfDevice);

    }

    public static void simulateInboundDevice(String hostNameOfComserver, int portNumberOfComserver, String serialNumberOfDevice) throws IOException, InterruptedException {
        String receivedData = "";
        String sendData = "";

        Socket clientSocket = new Socket(hostNameOfComserver, portNumberOfComserver);
        clientSocket.setSoTimeout(60000);

        OutputStream outToServer = clientSocket.getOutputStream();
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        int count = 0;

        while (!inFromServer.ready()) {
            Thread.sleep(100);
        }
        while (true) {
            while (inFromServer.ready()) {
                receivedData += inFromServer.read();
            }

            System.out.println("RECEIVED:" + receivedData);
            receivedData = "";

            if (count == 0) {
                sendData = "00010001001000376135a109060760857405080101a203020100a305a103020100a40a04085858580000000224be10040e0800065f1f040000101404400007";
            } else if (count == 1) {
                sendData = "0001000100100016c40142000910" + toHexString(leftPadWithZeros(serialNumberOfDevice, 16).getBytes());
            } else if (count == 2) {
                sendData = "00010001001000026300";
                outToServer.write(hexStringToByteArray(sendData));
            } else {
               Thread.sleep(60000000);
            }
            count++;

            if (sendData.equals("Q") || sendData.equals("q")) {
                clientSocket.close();
                break;
            } else {
                System.out.println("SENDING OUT: " + sendData);
                outToServer.write(hexStringToByteArray(sendData));
                while (!inFromServer.ready()) {
                    Thread.sleep(100);
                }
            }
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private static String leftPadWithZeros(String text, int size) {
        while(text.length() < size) {
            text = "0".concat(text);
        }
        return text;
    }

    public static String toHexString(byte[] ba) {
        StringBuilder str = new StringBuilder();
        for(int i = 0; i < ba.length; i++)
            str.append(String.format("%02x", ba[i]));
        return str.toString();
    }
}