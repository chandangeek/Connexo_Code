package com.energyict.protocolimpl.landisgyr.maxsys2510;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.ProtocolUtils;

class LinkLayer extends Connection {
	
	private static final int PASSWORD_ERROR_BIT = 0x80;

    int cmdCount = 0;
    int timeoutMilli = 10000;
    int retries;
    int forceDelay;

    MaxSys maxSys;
    CommandFactory cmdFactory;

    public LinkLayer(InputStream inputStream, OutputStream outputStream, long l, int i, int retries, int forceDelay,
            MaxSys maxSys) throws ConnectionException {
        super(inputStream, outputStream, l, i);
        this.retries = retries;
        this.forceDelay = forceDelay;
        this.maxSys = maxSys;
        this.cmdFactory = maxSys.commandFactory;
    }

    /** Send a StandardCommand */
    ByteArray send(StandardCommand command) throws IOException {
        int nrTry = 0;

        while (nrTry < retries) {

            try {

                cmdCount = cmdCount + 1;
                command.setCrn(cmdCount);
                command.setPassword(maxSys.getPassword());

                sendRawData(command.getBytes());
                ByteArray ba = receive(16);
                if ((PASSWORD_ERROR_BIT & ba.getBytes()[6]) == PASSWORD_ERROR_BIT)
                	throw new IOException("password error: wrong password");

                ByteArray rslt = new ByteArray();
                boolean done = false;
                int blockNr = 0xff;

                while (!done) {
                    sendRawData(cmdFactory.createAck(blockNr).getBytes());
                    Command c = cmdFactory.parse(receive(267));
                    if (c.isBlockCommand()) {
                        BlockCommand bc = (BlockCommand) c;
                        blockNr = bc.getDbn();
                        rslt.add(bc.getData());
                        done = bc.isEot();
                    } else {
                        done = true;
                    }
                }

                sendRawData(cmdFactory.createAck(0).getBytes());

                try {
                    Thread.sleep(forceDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    throw new IOException(e.getMessage());
                }

                return rslt.trim();
                
            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry++;
                
            } catch (IOException ioe) {
                throw ioe;
            }

        }

        throw new IOException("Failed to read: communication error");

    }
    
    ByteArray send( XCommand command, int customRetries ) throws IOException {
        int nrTry = 0;
        while (nrTry < customRetries) {

            try {

                cmdCount = cmdCount + 1;
                command.setCrn(cmdCount);
                command.setPassword(maxSys.getPassword());

                sendRawData(command.getBytes());
                ByteArray ba = receive(16);
                if ((PASSWORD_ERROR_BIT & ba.getBytes()[6]) == PASSWORD_ERROR_BIT)
                	throw new IOException("password error: check password level");
                return ba;

            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry++;
            } catch (IOException ioe) {
                throw ioe;
            }

        }

        throw new IOException("Failed to read: communication error");
        
    }
    
    ByteArray send( XCommand command ) throws IOException {
        int nrTry = 0;
        while (nrTry < retries) {

            try {

                cmdCount = cmdCount + 1;
                command.setCrn(cmdCount);
                command.setPassword(maxSys.getPassword());

                sendRawData(command.getBytes());
                ByteArray ba = receive(16);
                if ((PASSWORD_ERROR_BIT & ba.getBytes()[6]) == PASSWORD_ERROR_BIT)
                	throw new IOException("password error: check password level");
                return ba;

            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry++;
            } catch (IOException ioe) {
                throw ioe;
            }

        }

        throw new IOException("Failed to read: communication error");
        
    }

    ByteArray receive(int length) throws IOException {

        ByteArray buffer = new ByteArray();
        copyEchoBuffer();
        long endTime = System.currentTimeMillis() + timeoutMilli;

        int aChar;
        int count = 0;
        while (count < length) {
            aChar = readIn();
            if (aChar != -1) {
                buffer.add((byte) aChar);
                count++;
            }
            if (isTimeOut(endTime)) {
                String msg = "Connection timed out. " + buffer.toHexaString(true);
                throw new TimeOutException(msg);
            }
        }

        return buffer;

    }

    private boolean isTimeOut(long endTime) {
        return System.currentTimeMillis() >= endTime;
    }

}

class TimeOutException extends IOException {

    private static final long serialVersionUID = 1L;

    TimeOutException(String msg) {
        super(msg);
    }

}
