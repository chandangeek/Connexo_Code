package com.energyict.protocolimpl.landisgyr.us.maxsys2510;

import com.energyict.dialer.connection.Connection;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

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
//    	maxSys.getLogger().info("new Y command " + command.getTbn());
    	//sleep(maxSys.getCommandDelay());
        int nrTry = 0;

        while (nrTry < retries) {

            try {

                cmdCount = cmdCount + 1;
                command.setCrn(cmdCount);
                command.setPassword(maxSys.getPassword());

                sleep(50);
//                maxSys.getLogger().info("Y sent: " + ProtocolUtils.outputHexString(command.getBytes()));
                sendRawData(command.getBytes());

                sleep(50);
                ByteArray ba = receiveResponse(16);

//                maxSys.getLogger().info("Y received: " + ProtocolUtils.outputHexString(ba.getBytes()));

                if ((PASSWORD_ERROR_BIT & ba.getBytes()[6]) == PASSWORD_ERROR_BIT)
                	throw new IOException("1) Communication error or wrong password: " + ba.toHexaString(true));

                ByteArray rslt = new ByteArray();
                boolean done = false;
                int blockNr = 0xff;

                while (!done) {

                	Date before = new Date();
                	sleep(80);
                	byte[] ack = cmdFactory.createAck(blockNr).getBytes();
//                	maxSys.getLogger().info("X ack sent: " + blockNr + ", " + ProtocolUtils.outputHexString(ack)
//                			+ ", " + (new Date().getTime() - before.getTime()));
                    sendRawData(ack);

                    //maxSys.getLogger().info("");
//                    maxSys.getLogger().info("receive block data " + blockNr);
                    //sleep(50);
                    Command c = cmdFactory.parse(receiveBlockData(267));
                    if (c.isBlockCommand()) {
                        BlockCommand bc = (BlockCommand) c;
                        blockNr = bc.getDbn();
                        rslt.add(bc.getData());
                        done = bc.isEot();
                    } else {
                        done = true;
                    }
                }

                byte[] lastAck = cmdFactory.createAck(0).getBytes();
//            	maxSys.getLogger().info("X ack sent: 0 " + ProtocolUtils.outputHexString(lastAck));
                sendRawData(lastAck);

                //sleep(4000);
                //sleep(forceDelay);

                return rslt.trim();

            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry++;

            } catch (IOException ioe) {
                throw ioe;
            }
            finally {
            	sleep(forceDelay);
            }

        }

        throw new IOException("1) Failed to read: communication error");

    }


    protected void sleep(int millisec) throws IOException{
    	try {
            Thread.sleep(millisec);
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        }
    }

    ByteArray send( XCommand command, int customRetries ) throws IOException {
    	//sleep(maxSys.getCommandDelay());
        int nrTry = 0;
        while (nrTry < customRetries) {

            try {

                cmdCount = cmdCount + 1;
                command.setCrn(cmdCount);
                command.setPassword(maxSys.getPassword());

                sendRawData(command.getBytes());
                ByteArray ba = receiveResponse(16);
                if ((PASSWORD_ERROR_BIT & ba.getBytes()[6]) == PASSWORD_ERROR_BIT)
                	throw new IOException("2) Communication error or wrong password");
                return ba;

            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry++;
            } catch (IOException ioe) {
                throw ioe;
            }

        }

        throw new IOException("2) Failed to read: communication error");

    }

    ByteArray send( XCommand command ) throws IOException {
    	//sleep(maxSys.getCommandDelay());
        int nrTry = 0;
        while (nrTry < retries) {

            try {

                cmdCount = cmdCount + 1;
                command.setCrn(cmdCount);
                command.setPassword(maxSys.getPassword());

                sendRawData(command.getBytes());
                ByteArray ba = receiveResponse(16);
                if ((PASSWORD_ERROR_BIT & ba.getBytes()[6]) == PASSWORD_ERROR_BIT)
                	throw new IOException("3) communication error or wrong password");
                return ba;

            } catch (TimeOutException toe) {
                toe.printStackTrace();
                nrTry++;
            } catch (IOException ioe) {
                throw ioe;
            }

        }

        throw new IOException("3) Failed to read: communication error");

    }

    static final int WAIT_FOR_STX = 1;
    static final int STX_FOUND = 2;
    static final int BLOCK_INDICATOR_FOUND = 3;
    static final int ACK_FOUND = 4;


    protected ByteArray receiveBlockData(int length) throws IOException {

    	byte stx = 0x02;
    	byte blockIndicator = 0x0c;
    	long endTime = System.currentTimeMillis() + timeoutMilli;
    	ByteArray buffer = new ByteArray();
        copyEchoBuffer();
        int aChar;
        int state = WAIT_FOR_STX;
        int count = 0;
        while (true) {
        	if ((aChar = readIn()) != -1) {
        		if (state == WAIT_FOR_STX) {
        			if (aChar == (int)stx) {
        				state = STX_FOUND;
//        				maxSys.getLogger().info("STX_FOUND");
        			}
//        			else
//        				maxSys.getLogger().info("unexpected block data answer: " + ProtocolUtils.hex2String(aChar));
        		}
        		else if (state == STX_FOUND) {
        			if (aChar == (int)blockIndicator) {
        				state = BLOCK_INDICATOR_FOUND;
        				count = 3;
        				buffer.add(stx);
        				buffer.add(blockIndicator);
//        				maxSys.getLogger().info("BLOCK_INDICATOR_FOUND");
        			}
        			else
        				state = WAIT_FOR_STX;
        		}
        		else if (state == BLOCK_INDICATOR_FOUND) {
        			if (count <= length) {
            			//maxSys.getLogger().info("count: " + count + ", " + ProtocolUtils.hex2String(aChar));
        				buffer.add((byte) aChar);
        				if (buffer.size() == 267) {
        					Date before = new Date();
//        					this.maxSys.getLogger().info("return data= " + buffer.toHexaString(true)
//        							+ ", " + (new Date().getTime() - before.getTime()));
            				return buffer;
        				}
        				count++;
        			}
        		}
        	}
        	if (isTimeOut(endTime)) {
                String msg = "Connection timed out  . " + buffer.toHexaString(true);
                throw new TimeOutException(msg);
            }
        }
    }

    protected ByteArray receiveResponse(int length) throws IOException {

    	byte stx = 0x02;
    	byte ack = 0x06;
    	byte nack = 0x15;
    	long endTime = System.currentTimeMillis() + timeoutMilli;
    	ByteArray buffer = new ByteArray();
        copyEchoBuffer();
        int aChar;
        int state = WAIT_FOR_STX;
        int count = 0;
        while (true) {
        	if ((aChar = readIn()) != -1) {
        		if (state == WAIT_FOR_STX) {
        			if (aChar == (int)stx) {
        				state = STX_FOUND;
//        				maxSys.getLogger().info("STX_FOUND");
        			}
//        			else
//        				maxSys.getLogger().info("unexpected Y response answer: " + ProtocolUtils.hex2String(aChar));
        		}
        		else if (state == STX_FOUND) {
        			if (aChar == (int)ack) {
        				state = ACK_FOUND;
        				count = 3;
        				buffer.add(stx);
        				buffer.add(ack);
//        				maxSys.getLogger().info("ACK_FOUND");
        			}
        			else if (aChar == (int) nack) {
//        				maxSys.getLogger().info("NACK_FOUND: " + buffer.toHexaString(true));
        				String msg = "Nack received  . " + buffer.toHexaString(true);
                        throw new TimeOutException(msg);
        			}
        			else
        				state = WAIT_FOR_STX;
        		}
        		else if (state == ACK_FOUND) {
        			if (count <= length) {
            			//maxSys.getLogger().info("count: " + count + ", " + ProtocolUtils.hex2String(aChar));
        				buffer.add((byte) aChar);
        				if (buffer.size() == 16) {
        					Date before = new Date();
//        					this.maxSys.getLogger().info("return data= " + buffer.toHexaString(true)
//        							+ ", " + (new Date().getTime() - before.getTime()));
            				return buffer;
        				}
        				count++;
        			}
        		}
        	}
        	if (isTimeOut(endTime)) {
                String msg = "Connection timed out  . " + buffer.toHexaString(true);
                throw new TimeOutException(msg);
            }
        }
    }

    /*ByteArray receive(int length) throws IOException {

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

    }*/

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
