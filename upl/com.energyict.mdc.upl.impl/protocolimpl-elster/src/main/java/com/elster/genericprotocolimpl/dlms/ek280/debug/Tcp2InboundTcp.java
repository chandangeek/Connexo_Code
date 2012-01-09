package com.elster.genericprotocolimpl.dlms.ek280.debug;

import com.energyict.dialer.core.LinkException;
import com.energyict.protocolimpl.base.DebuggingObserver;

import java.io.*;
import java.net.Socket;

/**
 * Copyrights
 * Date: 8/06/11
 * Time: 9:44
 */
public class Tcp2InboundTcp {

    private static final boolean SHOW_COMMUNICATION = false;
    private static final boolean ASCII_MODE = false;
    private static final boolean MODE_7E1 = false;
    private static final String OBSERVER_FILE_NAME = null;

    private static final String COMMSERVER_HOST = "10.0.2.62";
    private static final int COMMSERVER_PORT = 12001;

    private static final String METER_HOST = "213.31.32.171";
    private static final int METER_PORT = 40000;

    private static final int BUFFER_SIZE = 1024 * 4;


    public static void main(String[] args) throws IOException, LinkException {
        if ((args != null) && (args.length > 0)) {
            new Tcp2InboundTcp().setupInboundConnection(args[0]);
        } else {
            new Tcp2InboundTcp().setupInboundConnection(null);
        }
    }

    private void setupInboundConnection(String serialNumber) throws LinkException, IOException {
        DebuggingObserver debuggingObserver = new DebuggingObserver(OBSERVER_FILE_NAME, SHOW_COMMUNICATION, ASCII_MODE, MODE_7E1);

        Socket meterSocket = getMeterSocket();
        Socket comServerSocket = getComServerSocket();

        IOLinker linker1 = new IOLinker(comServerSocket.getInputStream(), meterSocket.getOutputStream(), BUFFER_SIZE);
        IOLinker linker2 = new IOLinker(meterSocket.getInputStream(), comServerSocket.getOutputStream(), BUFFER_SIZE);

        System.out.println("Starting linkers ... ");
        linker1.start();
        linker2.start();

        if (serialNumber != null) {
            String discoveryValue = "<REQUEST>serialId=" + serialNumber + ",type=EK280_C0,ipAddress=192.168.27.11</REQUEST>";
            //String discoveryValue = "<REQUEST>serialId=" + serialNumber + "</REQUEST>";
            comServerSocket.getOutputStream().write(discoveryValue.getBytes());
            comServerSocket.getOutputStream().flush();
        }

        do {
            delay(500);
        } while (linker1.isRunning() && linker2.isRunning());

        System.out.println("Triggering close on linkers.");

        linker1.close();
        linker2.close();

        System.out.println("Waiting until linkers are closed ...");
        do {
            delay(500);
        } while (linker1.isRunning() || linker2.isRunning());

        System.out.println("Disconnecting meterSocket ...");
        meterSocket.close();

        System.out.println("Done.");

    }

    private void delay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Absorb
        }
    }

    private Socket getComServerSocket() throws IOException {
        System.out.println("Setting up TCP connection to [" + COMMSERVER_HOST + ":" + COMMSERVER_PORT + "]");
        Socket comServerSocket = new Socket(COMMSERVER_HOST, COMMSERVER_PORT);
        System.out.println("Socket connected: " + comServerSocket.getRemoteSocketAddress().toString());
        return comServerSocket;
    }

    private Socket getMeterSocket() throws IOException {
        System.out.println("Setting up TCP connection to [" + METER_HOST + ":" + METER_PORT + "]");
        Socket meterSocket = new Socket(METER_HOST, METER_PORT);
        System.out.println("Socket connected: " + meterSocket.getRemoteSocketAddress().toString());
        return meterSocket;
    }

    public class IOLinker extends Thread {

        private static final int POLLING_DELAY = 1;
        private static final int NODATA_POLLING_THRESHOLD = 10;

        private final InputStream input;
        private final OutputStream output;
        private final int bufferSize;

        private boolean closeRequested = false;
        private boolean running = false;

        @Override
        public void start() {
            running = true;
            super.start();
        }

        @Override
        public void run() {
            System.out.println(getName() + " => Started");
            doRun();
            System.out.println(getName() + " => Exited");
            running = false;
        }

        public boolean isCloseRequested() {
            return closeRequested;
        }

        public void close() {
            this.closeRequested = true;
            while (isRunning()) {
                delay(10);
            }
        }

        private void delay(long millis) {
            try {
                Thread.sleep(millis);
            } catch (InterruptedException e) {

            }
        }

        public boolean isRunning() {
            return running;
        }

        public IOLinker(InputStream input, OutputStream output, int bufferSize) {
            this.input = input;
            this.output = output;
            this.bufferSize = bufferSize;
        }

        public void doRun() {
            byte[] buffer = new byte[bufferSize];
            try {
                int noDataCount = 0;
                while (!isCloseRequested()) {
                    int count = input.read(buffer);
                    if (count > 0) {
                        output.write(buffer, 0, count);
                        noDataCount = 0;
                    } else if (count == -1) {
                        throw new IOException("Received -1. Probably the stream was closed remotely.");
                    } else {
                        if (noDataCount < NODATA_POLLING_THRESHOLD) {
                            noDataCount++;
                        } else {
                            Thread.sleep(POLLING_DELAY);
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println(getName() + " => " + e.getMessage());
            }

            closeCloseable(input);
            closeCloseable(output);
        }

        public boolean closeCloseable(Closeable closeable) {
            if (closeable != null) {
                try {
                    closeable.close();
                    return true;
                } catch (IOException e) {
                    return false;
                }
            } else {
                return false;
            }
        }

    }


}
