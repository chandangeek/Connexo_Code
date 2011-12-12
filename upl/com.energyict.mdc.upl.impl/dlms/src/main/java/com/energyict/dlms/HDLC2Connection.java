package com.energyict.dlms;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.*;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.protocol.ProtocolUtils;

import java.io.*;

final public class HDLC2Connection extends Connection implements DLMSConnection {

    private static final int[] crc_tab = {
        0x0000, 0x1189, 0x2312, 0x329b, 0x4624, 0x57ad, 0x6536, 0x74bf,
        0x8c48, 0x9dc1, 0xaf5a, 0xbed3, 0xca6c, 0xdbe5, 0xe97e, 0xf8f7,
        0x1081, 0x0108, 0x3393, 0x221a, 0x56a5, 0x472c, 0x75b7, 0x643e,
        0x9cc9, 0x8d40, 0xbfdb, 0xae52, 0xdaed, 0xcb64, 0xf9ff, 0xe876,
        0x2102, 0x308b, 0x0210, 0x1399, 0x6726, 0x76af, 0x4434, 0x55bd,
        0xad4a, 0xbcc3, 0x8e58, 0x9fd1, 0xeb6e, 0xfae7, 0xc87c, 0xd9f5,
        0x3183, 0x200a, 0x1291, 0x0318, 0x77a7, 0x662e, 0x54b5, 0x453c,
        0xbdcb, 0xac42, 0x9ed9, 0x8f50, 0xfbef, 0xea66, 0xd8fd, 0xc974,
        0x4204, 0x538d, 0x6116, 0x709f, 0x0420, 0x15a9, 0x2732, 0x36bb,
        0xce4c, 0xdfc5, 0xed5e, 0xfcd7, 0x8868, 0x99e1, 0xab7a, 0xbaf3,
        0x5285, 0x430c, 0x7197, 0x601e, 0x14a1, 0x0528, 0x37b3, 0x263a,
        0xdecd, 0xcf44, 0xfddf, 0xec56, 0x98e9, 0x8960, 0xbbfb, 0xaa72,
        0x6306, 0x728f, 0x4014, 0x519d, 0x2522, 0x34ab, 0x0630, 0x17b9,
        0xef4e, 0xfec7, 0xcc5c, 0xddd5, 0xa96a, 0xb8e3, 0x8a78, 0x9bf1,
        0x7387, 0x620e, 0x5095, 0x411c, 0x35a3, 0x242a, 0x16b1, 0x0738,
        0xffcf, 0xee46, 0xdcdd, 0xcd54, 0xb9eb, 0xa862, 0x9af9, 0x8b70,
        0x8408, 0x9581, 0xa71a, 0xb693, 0xc22c, 0xd3a5, 0xe13e, 0xf0b7,
        0x0840, 0x19c9, 0x2b52, 0x3adb, 0x4e64, 0x5fed, 0x6d76, 0x7cff,
        0x9489, 0x8500, 0xb79b, 0xa612, 0xd2ad, 0xc324, 0xf1bf, 0xe036,
        0x18c1, 0x0948, 0x3bd3, 0x2a5a, 0x5ee5, 0x4f6c, 0x7df7, 0x6c7e,
        0xa50a, 0xb483, 0x8618, 0x9791, 0xe32e, 0xf2a7, 0xc03c, 0xd1b5,
        0x2942, 0x38cb, 0x0a50, 0x1bd9, 0x6f66, 0x7eef, 0x4c74, 0x5dfd,
        0xb58b, 0xa402, 0x9699, 0x8710, 0xf3af, 0xe226, 0xd0bd, 0xc134,
        0x39c3, 0x284a, 0x1ad1, 0x0b58, 0x7fe7, 0x6e6e, 0x5cf5, 0x4d7c,
        0xc60c, 0xd785, 0xe51e, 0xf497, 0x8028, 0x91a1, 0xa33a, 0xb2b3,
        0x4a44, 0x5bcd, 0x6956, 0x78df, 0x0c60, 0x1de9, 0x2f72, 0x3efb,
        0xd68d, 0xc704, 0xf59f, 0xe416, 0x90a9, 0x8120, 0xb3bb, 0xa232,
        0x5ac5, 0x4b4c, 0x79d7, 0x685e, 0x1ce1, 0x0d68, 0x3ff3, 0x2e7a,
        0xe70e, 0xf687, 0xc41c, 0xd595, 0xa12a, 0xb0a3, 0x8238, 0x93b1,
        0x6b46, 0x7acf, 0x4854, 0x59dd, 0x2d62, 0x3ceb, 0x0e70, 0x1ff9,
        0xf78f, 0xe606, 0xd49d, 0xc514, 0xb1ab, 0xa022, 0x92b9, 0x8330,
        0x7bc7, 0x6a4e, 0x58d5, 0x495c, 0x3de3, 0x2c6a, 0x1ef1, 0x0f78
    };

    private static final int MAX_BUFFER_SIZE=512;
    private static final int HDLC_RX_OK=0x00;
    private static final int HDLC_BADCRC=0x01;
    private static final int  HDLC_TIMEOUT=0x02;
    private static final int  HDLC_ABORT=0x04;
    private static final int HDLC_BADFRAME=0x08;
    private static final String[] reasons={"BADCRC","TIMEOUT","ABORT","BADFRAME"};
    private static final int WAIT_FOR_START_FLAG=0x00;
    private static final int WAIT_FOR_FRAME_FORMAT=0x01;
    private static final int WAIT_FOR_DATA=0x02;
    private static final int WAIT_FOR_END_FLAG=0x03;
    private static final int SNRM= 0x83;
    private static final int DISC=0x43;
    private static final int UA=0x63;
    private static final int DM=0x0F;
    private static final int FRMR=(byte)0x87;
    private static final int UI=0x03;
    private static final int I_MASK=0x01;
    private static final int I=0x00;
    private static final int RR_MASK=0x0F;
    private static final int RR=0x01;
    private static final int RNR_MASK=0x0F;
    private static final int RNR=0x05;
    private static final int HDLC_FLAG=0x7E;
    private static final int HDLC_FRAME_TYPE3 = 0xA000;
    private static final int HDLC_FRAME_S_BIT=0x0800;
    private static final int HDLC_FRAME_CONTROL_PF_BIT=0x10;
    private static final int CLIENT_NO_SECURITY=0;
    private static final int CLIENT_LOWLEVEL_SECURITY=1;
    private static final int CRC_SIZE=2;
    private static final int CLIENT_ADDRESSING_DEFAULT = -1;
    private static final int CLIENT_ADDRESSING_1BYTE = 1;
    private static final int CLIENT_ADDRESSING_2BYTE = 2;
    private static final int CLIENT_ADDRESSING_4BYTE = 4;
    private static final byte[] addressing1b = new byte[]{0,1,2,3,4,5,6,7,7};
    private static final byte[] addressing2b = new byte[]{0,1,2,4,5,6,7,8,8};
    private static final byte[] addressing4b = new byte[]{0,1,2,6,7,8,9,10,10};
    private static final int frameFormatMSB = 0;
    private static final int frameFormatLSB = 1;
    private static final int frameDestination = 2;
    private static final int frameSource = 3;
    private static final int frameControl = 4;
    private static final int frameHeaderCRCMSB = 5;
    private static final int frameHeaderCRCLSB = 6;
    private static final int frameInformationField = 7;
    private static final int headerSize = 8;
    private static final int ISIZE = 128;
    private static final byte[] macDISCFrame={DISC |  HDLC_FRAME_CONTROL_PF_BIT,0x00,0x00};
    private static final int OFFSET_TO_FORMAT_ID=0;
    private static final int OFFSET_TO_GROUP_ID=1;
    private static final int OFFSET_TO_GROUP_LENGTH=2;
    private static final int OFFSET_TO_DATA=3;

    final int RX_FRAME_SIZE=5;
    final int TX_FRAME_SIZE=6;
    final int RX_WINDOW_SIZE=7;
    final int TX_WINDOW_SIZE=8;

    final private int iProtocolTimeout;
    final private long lForceDelay;
    final private int iMaxRetries;
    final private int iClientMacAddress;
    final private int iServerUpperMacAddress;
    final private int iServerLowerMacAddress;
    final private int hhuSignonBaudRateCode;
    final private int informationFieldSize;

    private int bAddressingMode = 1;
    private byte[] protocolParameters = null;
    private int sMaxRXIFSize;
    private int sMaxTXIFSize;
    private byte[] txFrame = new byte[MAX_BUFFER_SIZE+1];
    private byte[] rxFrame = new byte[MAX_BUFFER_SIZE+1];
    private int NR;
    private int NS;
    private boolean boolHDLCConnected;
    private byte[] macSNRMFrame;
    private HHUSignOn hhuSignOn=null;
    private String meterId="";
    private InvokeIdAndPriority invokeIdAndPriority;

    public HDLC2Connection(
    			InputStream inputStream,
    			OutputStream outputStream,
    			int iTimeout,
    			long lForceDelay,
    			int iMaxRetries,
    			int iClientMacAddress,
    			int iServerLowerMacAddress,
    			int iServerUpperMacAddress,
    			int addressingMode,
    			int informationFieldSize,
    			int hhuSignonBaudRateCode) throws DLMSConnectionException, ConnectionException {

        super(inputStream, outputStream);
        this.iMaxRetries = iMaxRetries;
        this.iProtocolTimeout=iTimeout;
        this.NR=0;
        this.NS=0;
        this.boolHDLCConnected=false;
        this.lForceDelay = lForceDelay;
        this.iClientMacAddress=iClientMacAddress;
        this.iServerUpperMacAddress=iServerUpperMacAddress;
        this.iServerLowerMacAddress=iServerLowerMacAddress;
        getAddressingMode(addressingMode);
        setProtocolParams();
        this.informationFieldSize= (informationFieldSize == - 1) ? ISIZE : informationFieldSize;
        this.hhuSignonBaudRateCode = hhuSignonBaudRateCode;
        updateSNRMFrames();
        this.invokeIdAndPriority = new InvokeIdAndPriority();
    }

    public int getType() {
        return DLMSConnection.DLMS_CONNECTION_HDLC;
    }

    private void updateSNRMFrames() {
    	byte[] macSNRM_part1 =
    		new byte[]{
    			(byte) ( SNRM | HDLC_FRAME_CONTROL_PF_BIT) ,
                0x00,
                0x00, // Header CRC
                (byte)0x81,
                (byte)0x80,
                0x0E};
    	byte[] macSNRM_part2 = new byte[] {0x07,0x01,0x01,0x08,0x01, 0x01,0x00,0x00};
    	byte[] infoFieldBytes = new byte[8];
    	int index = 0;
    	infoFieldBytes[index++] = 0x05;
    	infoFieldBytes[index++] = 2;
    	infoFieldBytes[index++] = highByte(informationFieldSize);
    	infoFieldBytes[index++] = lowByte(informationFieldSize);
    	infoFieldBytes[index++] = 0x06;
    	infoFieldBytes[index++] = 2;
    	infoFieldBytes[index++] = highByte(informationFieldSize);
    	infoFieldBytes[index++] = lowByte(informationFieldSize);
    	macSNRMFrame = new byte[macSNRM_part1.length + macSNRM_part2.length + infoFieldBytes.length];
    	System.arraycopy(macSNRM_part1, 0, macSNRMFrame, 0, macSNRM_part1.length);
    	System.arraycopy(infoFieldBytes, 0, macSNRMFrame, macSNRM_part1.length, infoFieldBytes.length);
    	System.arraycopy(macSNRM_part2, 0, macSNRMFrame, macSNRM_part1.length+infoFieldBytes.length, macSNRM_part2.length);
	}

	private void getAddressingMode(int addressingMode) throws DLMSConnectionException {
        if (addressingMode == CLIENT_ADDRESSING_DEFAULT) {
            if ((iServerLowerMacAddress  == 0) && (iServerUpperMacAddress  <= 0x7F)) {
				bAddressingMode = CLIENT_ADDRESSING_1BYTE;
			} else if (iServerLowerMacAddress  != 0) {
				bAddressingMode = CLIENT_ADDRESSING_4BYTE;
			} else {
				throw new DLMSConnectionException("HDLC2Connection, getAddressingMode, unknown addressing scheme, invalid server addresses");
			}
        } else {
        	this.bAddressingMode=(byte)addressingMode;
        }
        if (iClientMacAddress > 0x7F) {
			throw new DLMSConnectionException("HDLC2Connection, getAddressingMode, unknown addressing scheme, invalid client addres");
		}
        if (iClientMacAddress == 0) {
			throw new DLMSConnectionException("HDLC2Connection, getAddressingMode, unknown addressing scheme, client addres = 0");
		}
        if (iServerUpperMacAddress == 0) {
			throw new DLMSConnectionException("HDLC2Connection, getAddressingMode, unknown addressing scheme, server upper addres = 0");
		}
    }


    private void setProtocolParams() {
       if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
	   protocolParameters = addressing1b;
       } else if (bAddressingMode == CLIENT_ADDRESSING_2BYTE) {
    	   protocolParameters = addressing2b;
       } else if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {
    	   protocolParameters = addressing4b;
       }
    }

    private byte[] buildFrame(byte[] macFrame) {
    	int sSize = macFrame.length+protocolParameters[headerSize]-3;
    	int sFrameFormat = sSize | HDLC_FRAME_TYPE3;
    	byte[] frame=new byte[sSize];
    	frame[protocolParameters[frameFormatMSB]] = highByte(sFrameFormat);
    	frame[protocolParameters[frameFormatLSB]] = lowByte(sFrameFormat);
    	frame = buildAddressingScheme(frame);
    	for(int i=0;i<macFrame.length;i++) {
			frame[protocolParameters[frameSource]+1+i] = macFrame[i];
		}
    	return frame;
    }

    public void connectMAC() throws IOException,DLMSConnectionException {
    	if (!boolHDLCConnected) {
           try {
              if (hhuSignOn != null) {
            	  if (hhuSignonBaudRateCode == -1) {
					hhuSignOn.signOn("",meterId);
				} else {
					hhuSignOn.signOn("",meterId,hhuSignonBaudRateCode);
				}
              }
           }
           catch(ConnectionException e) {
               throw new DLMSConnectionException("connectMAC, HHU signOn failed, ConnectionException, "+e.getMessage());
           }
           byte[] macFrame = buildFrame(macSNRMFrame);
           doConnectMAC(macFrame);
    	}
    }

	public void setSNRMType(int type) {
	}

    public String getReason(int reason) {
       StringBuffer strbuff = new StringBuffer();
       for (int i=0;i<reasons.length;i++) {
           if ((reason & (0x1 << i)) != 0) {
               strbuff.append(reasons[i]+" ");
           }
       }
       return strbuff.toString();
    }

    private void doConnectMAC(byte[] macFrame) throws IOException,DLMSConnectionException {
       int bResult=0;

       sMaxRXIFSize=0x00F8;
       sMaxTXIFSize=0x00F8;
       calcCRC(macFrame);
       for (int i=0; i<=iMaxRetries; i++) {
           sendFrame(macFrame);
           bResult = waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame);
           if (bResult == HDLC_RX_OK) {
              HDLCFrame hdlcFrame=decodeFrame(rxFrame);
              if (hdlcFrame.bControl != UA) {
                 throw new DLMSConnectionException("ERROR connecting MAC, should receive an UA frame.");
              }
              getHDLCParameters(hdlcFrame.informationBuffer);
              boolHDLCConnected=true;
              return;
           }
       }
       throw new DLMSConnectionException("ERROR connecting MAC, reason "+ getReason(bResult),(short)(bResult&0xFF));
    }

/**
     * Method that requests a MAC disconnect for the HDLC layer.
     * @exception DLMSConnectionException
     */
    public void disconnectMAC() throws IOException, DLMSConnectionException {
       int bResult=0 ;

       if (boolHDLCConnected==true)
       {
           byte[] macFrame = buildFrame(macDISCFrame);
           calcCRC(macFrame);
           for (int i=0; i < iMaxRetries; i++) {
               sendFrame(macFrame);
               bResult = waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame);
               if (bResult == HDLC_RX_OK)
               {
                   HDLCFrame hdlcFrame=decodeFrame(rxFrame);
                   if (hdlcFrame.bControl != UA) {
                      throw new DLMSConnectionException("ERROR disConnecting MAC, should receive an UA frame.");
                   }
                   boolHDLCConnected=false;
                   return;
               }
           }
           throw new DLMSConnectionException("ERROR disConnecting MAC, reason "+getReason(bResult),(short)((short)bResult& 0xFF));
       }
    }

    /**
     * Initialize the receivers Max. InformationFieldSizes
     *
     * @param byteReceiveBuffer
     * @throws DLMSConnectionException
     */
    protected void getHDLCParameters(byte[] byteReceiveBuffer) throws DLMSConnectionException {
	int negosMaxRXIFSize = 128;
	int negosMaxTXIFSize = 128;

	if ((byteReceiveBuffer[OFFSET_TO_FORMAT_ID] != (byte) 0x81)
		|| (byteReceiveBuffer[OFFSET_TO_GROUP_ID] != (byte) 0x80)) {
	    throw new DLMSConnectionException("HDLC2Connection, getHDLCParameters, format (0x"
		    + ((int) byteReceiveBuffer[OFFSET_TO_FORMAT_ID] & 0xFF) + ") and/or group identifier (0x"
		    + ((int) byteReceiveBuffer[OFFSET_TO_GROUP_ID] & 0xFF) + ") wrong!");
	}
	for (int i = 0; i < (byteReceiveBuffer.length - OFFSET_TO_DATA);) {
	    int data = byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
          int valLen = 0;
          try {
		switch (data) {
                  case RX_FRAME_SIZE:
                      i++;
		    valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                      i++;
		    if (valLen == 0) { // use default
			negosMaxRXIFSize = ISIZE;
		    } else {
			negosMaxRXIFSize = (short) (ProtocolUtils.getLongFromBytes(byteReceiveBuffer, OFFSET_TO_DATA
				+ i, valLen).intValue());
                      i += valLen;
		    }
                  break;
                  case TX_FRAME_SIZE:
                      i++;
		    valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                      i++;
		    if (valLen == 0) { // use default
			negosMaxTXIFSize = ISIZE;
		    } else {
			negosMaxTXIFSize = (short) (ProtocolUtils.getLongFromBytes(byteReceiveBuffer, OFFSET_TO_DATA
				+ i, valLen).intValue());
                      i += valLen;
		    }
                  break;
                  case RX_WINDOW_SIZE:
                      i++;
		    valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                      i++;
                      i += valLen;
                  break;
                  case TX_WINDOW_SIZE:
                      i++;
		    valLen = (int) byteReceiveBuffer[OFFSET_TO_DATA + i] & 0xFF;
                      i++;
                      i += valLen;
                  break;
                  default:
                     i++;
                  break;
              }
	    } catch (IOException e) {
		throw new DLMSConnectionException("HDLC2Connection, getHDLCParameters, IOException, " + e.getMessage());
          }
       }
       // Use smallest parameters negotiated.
	if (negosMaxRXIFSize < sMaxRXIFSize) {
		sMaxRXIFSize = negosMaxRXIFSize;
	}
	if (negosMaxTXIFSize < sMaxTXIFSize) {
		sMaxTXIFSize = negosMaxTXIFSize;
	}
    }

    private HDLCFrame decodeFrame(byte[] byteReceiveBuffer) throws DLMSConnectionException
    {
       HDLCFrame result = new HDLCFrame(byteReceiveBuffer);
       //print(byteReceiveBuffer[protocolParameters[frameControl]],false);
       return result;
    }


    private byte waitForHDLCFrameStateMachine(int iTimeout,byte[] byteReceiveBuffer) throws DLMSConnectionException , IOException {
        long lMSTimeout = System.currentTimeMillis() + iTimeout;
        int sRXCount = 0;
        int sLength=0;
        int bCurrentState=WAIT_FOR_START_FLAG;

        copyEchoBuffer();

        try {
        	while(true) {
        		int inewKar = readIn();
        		if (inewKar != -1) {
        			if (sRXCount>=MAX_BUFFER_SIZE) {
						return HDLC_BADFRAME;
					}
        			switch (bCurrentState) {
        				case WAIT_FOR_START_FLAG:
    						switch((byte)inewKar) {
    							case HDLC_FLAG:
    								sRXCount=0;
    								bCurrentState = WAIT_FOR_FRAME_FORMAT;
    								break;
                	   		}
    					break;

        				case WAIT_FOR_FRAME_FORMAT:
        					if ((sRXCount==0) &&((byte)inewKar == HDLC_FLAG)) {
								break;
							}
        					byteReceiveBuffer[sRXCount++]=(byte)inewKar;
        					if (sRXCount >= 2) {
        						sLength = getLength(byteReceiveBuffer);
        						bCurrentState = WAIT_FOR_DATA;
        					}
        					break;

        				case WAIT_FOR_DATA:
        					byteReceiveBuffer[sRXCount++] = (byte) inewKar;
        					if (sRXCount > sLength) {
        						return checkCRC(byteReceiveBuffer, sRXCount);
       						}
       						break;
        			}
                }
                if (((long) (System.currentTimeMillis() - lMSTimeout)) > 0) {
					return HDLC_TIMEOUT;
				}
            }
        } catch(ArrayIndexOutOfBoundsException e) {
           throw new DLMSConnectionException(e.getMessage());
        }
    }

    private byte checkCRC(byte[] byteReceiveBuffer, int count) {
        switch(byteReceiveBuffer[count-1]) {
        	case HDLC_FLAG:
        		// Check CRC
        		int[] CRC = getCRC(byteReceiveBuffer);
        		int[] calcCRC = calcCRC(byteReceiveBuffer);
        		if ((CRC[0] == calcCRC[0]) && (CRC[1] == calcCRC[1])) {
        			return HDLC_RX_OK;
        		} else {
        			return HDLC_BADCRC;
        		}

        	default:
	          return HDLC_BADFRAME;
	   }
	}

	private void sendFrame(byte[] byteBuffer) throws IOException,DLMSConnectionException {
        delay(lForceDelay);
        int iLength = getLength(byteBuffer);
        //print(byteBuffer[protocolParameters[frameControl]],true);
        byte[] data = new byte[iLength + 2];
    	data[0] = HDLC_FLAG;
    	System.arraycopy(byteBuffer, 0, data, 1, iLength);
    	data[data.length-1] = HDLC_FLAG;
    	sendOut(data);
    }

    public byte[] sendRequest(byte[] byteRequestBuffer) throws IOException {
        ReceiveBuffer receiveBuffer=new ReceiveBuffer();
        try {
        	sendInformationField(byteRequestBuffer);
        	return receiveInformationField(receiveBuffer);
        } catch(DLMSConnectionException e) {
        	throw new NestedIOException(e);
        }
    }

    private void sendInformationField(byte[] byteBuffer) throws IOException, DLMSConnectionException {
    	if (!boolHDLCConnected) {
			throw new DLMSConnectionException("HDLC connection not established!");
		}

        int lLength = byteBuffer.length; // length of information byteBuffer
        int iIndex=0; // index in information byteBuffer

        while(lLength>0) {
        	int infoLength = (lLength > sMaxTXIFSize) ? sMaxTXIFSize : lLength;
        	lLength -= infoLength;
            int sFrameFormat= (infoLength+protocolParameters[headerSize]+CRC_SIZE) | HDLC_FRAME_TYPE3;
            if (lLength > 0) {
				sFrameFormat |= HDLC_FRAME_S_BIT;
			}
            for (int i=0; i < infoLength ; i++) {
				txFrame[protocolParameters[frameInformationField]+i] = byteBuffer[iIndex++];
			}
            txFrame[protocolParameters[frameFormatMSB]] = highByte(sFrameFormat);
            txFrame[protocolParameters[frameFormatLSB]] = lowByte(sFrameFormat);
            txFrame = buildAddressingScheme(txFrame);
            txFrame[protocolParameters[frameControl]] = (byte) (I | (NR<<5) | (NS<<1) | HDLC_FRAME_CONTROL_PF_BIT);
            calcCRC(txFrame);
            sendFrame(txFrame);
            if (lLength > 0) {
            	waitForReceiveReady();
            }
        }
    }

    private byte[] buildAddressingScheme(byte[] buffer) {
       if (bAddressingMode == CLIENT_ADDRESSING_4BYTE) {
          buffer[protocolParameters[frameDestination]+3] = (byte)(((iServerLowerMacAddress << 1) | 0x01)& 0xFF);
          buffer[protocolParameters[frameDestination]+2] = (byte)((iServerLowerMacAddress >> 6) & 0xFE);
          buffer[protocolParameters[frameDestination]+1] = (byte)((iServerUpperMacAddress << 1)& 0xFF);
          buffer[protocolParameters[frameDestination]] = (byte)((iServerUpperMacAddress >> 6)& 0xFE);
       } else if (bAddressingMode == CLIENT_ADDRESSING_2BYTE) {
          buffer[protocolParameters[frameDestination]+1] = (byte)(((iServerLowerMacAddress << 1) | 0x01)& 0xFF);
          buffer[protocolParameters[frameDestination]] = (byte)((iServerUpperMacAddress << 1)& 0xFF);
       } else if (bAddressingMode == CLIENT_ADDRESSING_1BYTE) {
          buffer[protocolParameters[frameDestination]] = (byte)(((iServerUpperMacAddress<<1) | 0x01)& 0xFF);
       }
       buffer[protocolParameters[frameSource]] = (byte)(((iClientMacAddress<<1)| 0x01)& 0xFF);
       return buffer;
    }

    private void waitForReceiveReady() throws DLMSConnectionException,IOException {
        for (int retryCount = 1 ; retryCount <= (iMaxRetries + 1) ; retryCount++) {
            byte bResult = waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame);
            if (bResult == HDLC_RX_OK) {
                HDLCFrame hdlcFrame=decodeFrame(rxFrame);
                if ((hdlcFrame.bControl == I) || (hdlcFrame.bControl == RR)) {
                	if ( updateNS(hdlcFrame)) {
                    	return;
                    } else {
                    	if (hdlcFrame.boolControlPFBit) {
							sendFrame(txFrame);
						}
                    }
                } else {
                	throw new DLMSConnectionException("ERROR receiving data, should receive an I or RR frame.");
                }
            } else {
            	// receive timeout
                sendFrame(txFrame);
            }
        }
        throw new DLMSConnectionException("receiveReceiveReady > Retry count exceeded when receiving data");
    }

    private static final byte STATE_WAIT_FOR_I_FRAME=0;
    private static final byte STATE_WAIT_FOR_RR_FRAME=1;

    private byte[] receiveInformationField(ReceiveBuffer receiveBuffer) throws DLMSConnectionException,IOException {
        if (!boolHDLCConnected) {
			throw new DLMSConnectionException("HDLC connection not established!");
		}
        for (int retryCount = 1 ; retryCount <= (iMaxRetries+1) ; retryCount++) {
        	byte bResult = waitForHDLCFrameStateMachine(iProtocolTimeout,rxFrame);
            if (bResult == HDLC_RX_OK) {
                HDLCFrame hdlcFrame = decodeFrame(rxFrame);
                updateNS(hdlcFrame);
                if (hdlcFrame.bControl == I) {
                    if (NR == hdlcFrame.NS) {
                    	if (hdlcFrame.informationBuffer != null) {
							receiveBuffer.addArray(hdlcFrame.informationBuffer);
						}
                    	NR = (byte) nextSequence(NR);
                    	if ((hdlcFrame.sFrameFormat & HDLC_FRAME_S_BIT) != 0) {
                    		if (hdlcFrame.boolControlPFBit) {
								sendReceiveReady();
							}
                    		retryCount = 0;
                    	} else {
                    		return receiveBuffer.getArray();
                    	}
                    } else {
                    	// send correct sequence number to
                    	if (hdlcFrame.boolControlPFBit) {
                    		if (NS == hdlcFrame.NR) {
								sendFrame(txFrame); // resend last I frame
							} else {
								sendReceiveReady();
							}
                    	}
                          }
                } else if (hdlcFrame.bControl == RR) {
                    // received RR frame , expected I frame
                	if (hdlcFrame.boolControlPFBit) {
                		if (NS == hdlcFrame.NR) {
							sendFrame(txFrame); // resend last I frame
						} else {
							sendReceiveReady();
						}
                	}
                } else {
                	// invalid frame type
                    throw new DLMSConnectionException("ERROR receiving data, should receive an I or RR frame.");
                }
            } else {
                sendFrame(txFrame);
            }
        }
        throw new DLMSConnectionException("receiveInformationField> Retry count exceeded when receiving data");
    }

    private boolean updateNS(HDLCFrame frame) {
    	if (nextSequence(NS) == frame.NR) {
    		NS = frame.NR;
    		return true;
    	} else {
    		return false;
    	}
    }

	private void sendReceiveReady() throws IOException , DLMSConnectionException {
		int sFrameFormat = protocolParameters[headerSize] | HDLC_FRAME_TYPE3;
		txFrame[protocolParameters[frameFormatMSB]] = highByte(sFrameFormat);
		txFrame[protocolParameters[frameFormatLSB]] = lowByte(sFrameFormat);
		txFrame = buildAddressingScheme(txFrame);
		txFrame[protocolParameters[frameControl]] = (byte)(RR | (NR<<5) | HDLC_FRAME_CONTROL_PF_BIT);
		calcCRC(txFrame);
		sendFrame(txFrame);
    }

    private int[] getCRC(byte[] byteBuffer) {
        int iCRCHeader;

        int iLength = getLength(byteBuffer);
        if (protocolParameters[headerSize] <= iLength) {
            iCRCHeader =
            	((byteBuffer[protocolParameters[frameHeaderCRCMSB]]& 0xFF) << 8) |
                 (byteBuffer[protocolParameters[frameHeaderCRCLSB]]& 0xFF);
        } else {
           iCRCHeader=-1;
        }
        int iCRCFrame =
        	((byteBuffer[iLength-2]& 0xFF) << 8) |
        	 (byteBuffer[iLength-1]& 0xFF);
        return new int[] { iCRCHeader , iCRCFrame };
     }

    private int writeCRC(byte[] buffer , int length) {
    	int crc = 0xFFFF;

        for (int i=0; i < (length - 2) ; i++) {
            int iCharVal = buffer[i] & 0xFF;
            crc = ((crc >> 8) ^ crc_tab[(crc ^ iCharVal) & 0xFF]) & 0xFFFF;
         }
         crc ^= 0xFFFF;
         crc = ((lowByte(crc) << 8) & 0xFF00) | (highByte(crc) & 0xFF);
         buffer[length - 2] = highByte(crc);
         buffer[length - 1] = lowByte(crc);
         return crc;
    }

    private byte highByte(int in) {
    	return (byte) ((in >> 8) & 0xFF);
    }

    private byte lowByte(int in) {
    	return (byte) (in & 0xFF);
    }

    private int[] calcCRC(byte[] byteBuffer) {
        int iCRCHeader;

        int iLength = getLength(byteBuffer);
        if (protocolParameters[headerSize] <= iLength) {
        	iCRCHeader= writeCRC(byteBuffer , protocolParameters[headerSize]);
        } else {
        	iCRCHeader=-1;
        }
        int iCRCFrame= writeCRC(byteBuffer,iLength);
        return new int[] { iCRCHeader , iCRCFrame };
    }

    private int nextSequence(int in) {
    	return (in + 1) % 8;
    }

    // KV 18092003
    public void setHHUSignOn(HHUSignOn hhuSignOn,String meterId) {
        this.hhuSignOn=hhuSignOn;
        this.meterId=meterId;
    }
    public HHUSignOn getHhuSignOn() {
        return hhuSignOn;
    }

    private int getLength(byte[] buffer) {
    	return
    		((buffer[protocolParameters[frameFormatMSB]] & 0x07) << 8) |
    		 (buffer[protocolParameters[frameFormatLSB]] & 0xFF);
    }

    private void print(byte cntrl , boolean out) {
 	   String frameDescription = "?";
 	   int NR = (cntrl & 0xFF) >> 5;
 	   int NS = (cntrl & 0x0F) >> 1;

 	   if ((cntrl & 0x01) == 0x00) {
		frameDescription = "I (" + NR + "/" + NS + ")";
	}
 	   if ((cntrl & 0x0F) == 0x01) {
		frameDescription = "RR (" + NR + ")";
	}
 	   if ((cntrl & 0x0F) == 0x05) {
		frameDescription = "RNR (" + NR + ")";
	}
 	   if ((cntrl & 0x8F) == 0x83) {
		frameDescription = "SNRM";
	}
 	   if ((cntrl & 0x4F) == 0x43) {
		frameDescription = "DISC";
	}
 	   if ((cntrl & 0x6F) == 0x63) {
		frameDescription = "UA";
	}
 	   if ((cntrl & 0x0F) == 0x0F) {
		frameDescription = "DM";
	}
 	   if ((cntrl & 0x8F) == 0x87) {
		frameDescription = "FRMR";
	}
 	   if ((cntrl & 0x8F) == 0x87) {
		frameDescription = "FRMR";
	}
 	   if (out) {
		System.out.println("==> " + frameDescription);
	} else {
		System.out.println("<== " + frameDescription);
	}
    }

    public void setIskraWrapper(int type) {
	}

    public static void main(String args[]){
    	byte[] response = DLMSUtils.hexStringToByteArray("A019030243730E2F8180120501D20601D207010108010140E7");
    	try {
    		HDLC2Connection con = new HDLC2Connection(null, null, -1, 150, 1, 16, 1, 17, 2,255,300);
			HDLCFrame fram = con.new HDLCFrame(response);
		} catch (DLMSConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConnectionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    /********************************************************************************************************
     * Invoke-Id-And-Priority byte setting
     ********************************************************************************************************/

    public void setInvokeIdAndPriority(InvokeIdAndPriority iiap){
    	this.invokeIdAndPriority = iiap;
    }

    public InvokeIdAndPriority getInvokeIdAndPriority(){
    	return this.invokeIdAndPriority;
    }

    /**
     * Getter for the Servers' max receive Information size
     *
     * @return the servers's max receive information size
     */
    protected int getServerMaxRXIFSize() {
	return sMaxRXIFSize;
    }

    /**
     * Getter for the Servers' max transmit Information size
     *
     * @return the servers' max transmit information size
     */
    protected int getServerMaxTXIFSize() {
	return sMaxTXIFSize;
    }

    /**
     * Set the servers' maximum information field sizes to a default value
     */
    protected void initServerMaxSizes() {
	sMaxRXIFSize = 0x00F8;
	sMaxTXIFSize = 0x00F8;
    }
    private class HDLCFrame {

    	private int sLength;
        private int bFrameType;
        private int bControl;
        private boolean boolControlPFBit;
        private int NR;
        private int NS;
        private int bDestination;
        private int bSource;
        private byte[] informationBuffer;
        private int sFrameFormat;

        private HDLCFrame(byte[] byteReceiveBuffer) throws DLMSConnectionException {
        	sLength = getLength(byteReceiveBuffer);
        	sFrameFormat =
        		((byteReceiveBuffer[protocolParameters[frameFormatMSB]]& 0xFF) <<8) |
        		 (byteReceiveBuffer[protocolParameters[frameFormatLSB]]& 0xFF);
        	if (sLength >= (protocolParameters[headerSize]+2)){
        		informationBuffer = new byte[sLength - (protocolParameters[headerSize]+2)];
        		for (int i=0;i<sLength-(protocolParameters[headerSize]+2);i++) {
					informationBuffer[i] = byteReceiveBuffer[protocolParameters[headerSize]+i];
				}
        	} else {
        	  informationBuffer = null;
        	}
        	bFrameType= (byteReceiveBuffer[protocolParameters[frameFormatMSB]]>>4)& 0x0F;
        	boolControlPFBit = (byteReceiveBuffer[protocolParameters[frameControl]] & HDLC_FRAME_CONTROL_PF_BIT)!= 0;
        	// 	Mask out the PF bit
        	byteReceiveBuffer[protocolParameters[frameControl]] &= (HDLC_FRAME_CONTROL_PF_BIT^0xFF);
        	if ((byteReceiveBuffer[protocolParameters[frameControl]]&I_MASK) == I) {
				bControl = I;
			} else if ((byteReceiveBuffer[protocolParameters[frameControl]]&RR_MASK) == RR) {
				bControl = RR;
			} else if ((byteReceiveBuffer[protocolParameters[frameControl]]&RNR_MASK) == RNR) {
				bControl = RNR;
			} else {
				bControl = byteReceiveBuffer[protocolParameters[frameControl]];
			}
        	NR=0;
        	NS=0;
        	if (bControl == I) {
        		NR= (byteReceiveBuffer[protocolParameters[frameControl]] & 0xE0) >> 5;
        		NS= (byteReceiveBuffer[protocolParameters[frameControl]] & 0x0E) >> 1;
        	} else if ((bControl == RR) || (bControl == RNR)) {
        		NR= (byteReceiveBuffer[protocolParameters[frameControl]] & 0xE0) >> 5;
        	}
        	bDestination=0;
        	bSource=0;
        	if ((byteReceiveBuffer[2 + bAddressingMode] & 0x01)==0) {
				throw new DLMSConnectionException("HDLCFrame> Frame destination address error");
			}
        	bDestination = (byteReceiveBuffer[protocolParameters[frameDestination]] & 0xFF) >> 1 ;
          	if ((byteReceiveBuffer[protocolParameters[frameSource]] & 0x01) == 0) {
				throw new DLMSConnectionException("HDLCFrame> Frame source address error");
			}
          	bSource= (byteReceiveBuffer[protocolParameters[frameSource]] & 0xFF) >> 1;
        }
    }
	public int getMaxRetries() {
		return iMaxRetries;
	}

    public ApplicationServiceObject getApplicationServiceObject() {
        return null;
    }
}