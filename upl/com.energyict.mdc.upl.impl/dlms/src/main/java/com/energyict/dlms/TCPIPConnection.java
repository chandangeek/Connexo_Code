package com.energyict.dlms;

import com.energyict.cpo.Environment;
import com.energyict.dialer.connection.*;
import com.energyict.dlms.aso.ApplicationServiceObject;
import com.energyict.protocol.ProtocolUtils;

import java.io.*;

/**
 * @version  1.0
 * @author   Koenraad Vanderschaeve
 * <P>
 * <B>Description :</B><BR>
 * Class that implements the TCPIP transport layer wrapper protocol.
 */

public class TCPIPConnection extends Connection implements DLMSConnection {
	private static final byte DEBUG=0;
	private static final long TIMEOUT=300000;
	private static final int WRAPPER_VERSION=0x0001;

	// TCPIP specific
	// Sequence numbering
	private boolean boolTCPIPConnected;

	private int maxRetries;
	private int clientAddress;
	private int serverAddress;
	int timeout;
	private long forceDelay;

	private int iskraWrapper = 0;

	private InvokeIdAndPriority invokeIdAndPriority;

	public TCPIPConnection(InputStream inputStream,
			OutputStream outputStream,
			int timeout,
			int forceDelay,
			int maxRetries,
			int clientAddress,
			int serverAddress) throws IOException {
		super(inputStream, outputStream);
		this.maxRetries = maxRetries;
		this.timeout=timeout;
		this.clientAddress=clientAddress;
		this.serverAddress=serverAddress;
		this.forceDelay = forceDelay;
		this.boolTCPIPConnected=false;
		this.invokeIdAndPriority = new InvokeIdAndPriority();

	} // public TCPIPConnection(...)

	public int getType() {
		return DLMSConnection.DLMS_CONNECTION_TCPIP;
	}

	public long getForceDelay() {
		return this.forceDelay;
	}

	public void setSNRMType(int type) {
		// absorb...
	}

	/**
	 * Method that requests a MAC connection for the TCPIP layer. this request negotiates some parameters
	 * for the buffersizes and windowsizes.
	 */
	public void connectMAC() throws IOException {
		if (this.boolTCPIPConnected==false) {
			if (this.hhuSignOn != null) {
				this.hhuSignOn.signOn("",this.meterId);
			}
			this.boolTCPIPConnected=true;
		} // if (boolTCPIPConnected==false)
	} // public void connectMAC() throws IOException

	/**
	 * Method that requests a MAC disconnect for the TCPIP layer.
	 */
	public void disconnectMAC() throws IOException {
		if (this.boolTCPIPConnected==true) {
			this.boolTCPIPConnected=false;
		} // if (boolTCPIPConnected==true)

	} // public void disconnectMAC() throws IOException

	private final int STATE_HEADER_VERSION=0;
	private final int STATE_HEADER_SOURCE=1;
	private final int STATE_HEADER_DESTINATION=2;
	private final int STATE_HEADER_LENGTH=3;
	private final int STATE_DATA=4;

	private WPDU receiveData() throws IOException {
		long protocolTimeout,interFrameTimeout;
		int kar;
		int length=0;
		int state=this.STATE_HEADER_VERSION;
		int count=0;
		WPDU wpdu=null;
		ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();

		interFrameTimeout = System.currentTimeMillis() + this.timeout;
		protocolTimeout = System.currentTimeMillis() + this.TIMEOUT;

		resultArrayOutputStream.reset();
		copyEchoBuffer();

		while(true) {
			if ((kar = readIn()) != -1) {
				if (DEBUG>=2) {
					ProtocolUtils.outputHex(kar);
				}

				switch (state) {

				case STATE_HEADER_VERSION: {
					if (count==0) {
						wpdu = new WPDU();
						wpdu.setVersion(kar);
						count++;
					}
					else {
						wpdu.setVersion(wpdu.getVersion()*256+kar);
						count=0;
						if (wpdu.getVersion() == this.WRAPPER_VERSION) {
							state = this.STATE_HEADER_SOURCE;
						}
					}
				} break; // case STATE_HEADER_VERSION

				case STATE_HEADER_SOURCE: {
					if (count==0) {
						wpdu.setSource(kar);
						count++;
					}
					else {
						wpdu.setSource(wpdu.getSource()*256+kar);
						count=0;

						int address = -1;
						switch(this.iskraWrapper){
						case 0: {address = this.clientAddress;break;}
						case 1: {address = this.serverAddress;break;}
						default: {address = this.clientAddress;break;}
						}

						if (wpdu.getSource() != address) {
							state = this.STATE_HEADER_VERSION;
						} else {
							state = this.STATE_HEADER_DESTINATION;
						}
					}
				} break; // case STATE_HEADER_DESTINATION

				case STATE_HEADER_DESTINATION: {
					if (count==0) {
						wpdu.setDestination(kar);
						count++;
					}
					else {
						wpdu.setDestination(wpdu.getDestination()*256+kar);
						count=0;

						int address = -1;
						switch(this.iskraWrapper){
						case 0: {address = this.serverAddress;break;}
						case 1: {address = this.clientAddress;break;}
						default: {address = this.serverAddress;break;}
						}

						if (wpdu.getDestination() != address) {
							state = this.STATE_HEADER_VERSION;
						} else {
							state = this.STATE_HEADER_LENGTH;
						}
					}
				} break; // case STATE_HEADER_DESTINATION

				case STATE_HEADER_LENGTH: {
					if (DEBUG>=2) {
						System.out.println("KV_DEBUG> receive "+count+ "bytes of data");
					}
					if (count==0) {
						wpdu.setLength(kar);
						count++;
					}
					else {
						wpdu.setLength(wpdu.getLength()*256+kar);
						count=wpdu.getLength();

						if (DEBUG>=2) {
							System.out.println("KV_DEBUG> count="+count);
						}

						// Add padding of 3 bytes to fake the HDLC LLC. Very tricky to reuse all code written in the early days when only HDLC existed...
						resultArrayOutputStream.write(0);
						resultArrayOutputStream.write(0);
						resultArrayOutputStream.write(0);


						state = this.STATE_DATA;
					}
				} break; // case STATE_HEADER_LENGTH

				case STATE_DATA: {

					interFrameTimeout = System.currentTimeMillis() + this.timeout;

					resultArrayOutputStream.write(kar);
					if (--count<=0) {
						wpdu.setData(resultArrayOutputStream.toByteArray());
						if (DEBUG>=1) {
							System.out.println("KV_DEBUG> RX-->"+wpdu);
						}
						return wpdu;
					}

				} break; // STATE_DATA STATE_IDLE

				} // switch (bCurrentState)

			} // if ((iNewKar = readIn()) != -1)

			if (((System.currentTimeMillis() - protocolTimeout)) > 0) {
				throw new ConnectionException("receiveResponse() response timeout error",this.TIMEOUT_ERROR);
			}

			if (((System.currentTimeMillis() - interFrameTimeout)) > 0) {
				throw new ConnectionException("receiveResponse() interframe timeout error",this.TIMEOUT_ERROR);
			}

		} // while(true)
	} // private byte waitForTCPIPFrameStateMachine()


	public void setIskraWrapper(int type) {

		/**
		 * GN|260208|: Creation of this method to switch the client an server addresses
		 * Should have been correct but Actaris probably switched this and Iskra came last ...
		 */
		this.iskraWrapper = type;
	}

	/**
	 * Method that sends an information data field and receives an information field.
	 * @param data with the information field.
	 * @return Response data with the information field.
	 */
	public byte[] sendRequest(byte[] data) throws IOException {

		int retry=0;

		// strip the HDLC LLC header. This is because of the code inherited from  the days only HDLC existed...
		byte[] byteRequestBuffer = new byte[data.length-3];
		System.arraycopy(data, 3, byteRequestBuffer, 0, byteRequestBuffer.length);
		//        try {
		//           Thread.sleep(400);
		//        }
		//        catch(Exception e) {
		//            e.printStackTrace();
		//        }

		while(true) {
			try {
				WPDU wpdu = new WPDU(this.clientAddress,this.serverAddress,byteRequestBuffer);
				if (DEBUG>=1) {
					System.out.println("KV_DEBUG> TX-->"+wpdu);
				}
				sendOut(wpdu.getFrameData());
				return receiveData().getData();
			}
			catch (ConnectionException e) {
				if (retry++ >= this.maxRetries) {
					throw new IOException("sendRequest, IOException, "+com.energyict.cbo.Utils.stack2string(e));
				}
			}
		}

	} // public byte[] sendRequest(byte[] byteBuffer) throws TCPIPConnectionException

	// KV 18092003
	HHUSignOn hhuSignOn=null;
	String meterId="";
	public void setHHUSignOn(HHUSignOn hhuSignOn,String meterId) {
		this.hhuSignOn=hhuSignOn;
		this.meterId=meterId;
	}
	public HHUSignOn getHhuSignOn() {
		return this.hhuSignOn;
	}

	class WPDU {

		private int version;
		private int source;
		private int destination;
		private int length;
		private byte[] data;

		public WPDU(int source, int destination, byte[] data) {
			this.version = TCPIPConnection.this.WRAPPER_VERSION;
			this.source=source;
			this.destination=destination;
			this.data=data;
			this.length = data.length;
		}


		public WPDU() {
			this.version=this.source=this.destination=this.length=-1;
		}

		public WPDU(byte[] byteBuffer) throws IOException {
			int offset = 0;
			setVersion(ProtocolUtils.getInt(byteBuffer,offset,2));
			offset+=2;
			setSource(ProtocolUtils.getInt(byteBuffer,offset,2));
			offset+=2;
			setDestination(ProtocolUtils.getInt(byteBuffer,offset,2));
			offset+=2;
			setLength(ProtocolUtils.getInt(byteBuffer,offset,2));
			offset+=2;
			setData(ProtocolUtils.getSubArray2(byteBuffer, offset, getLength() - 8));
		}

		@Override
		public String toString() {
			return ProtocolUtils.outputHexString(this.data);
		}

		public byte[] getFrameData() throws IOException {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream daos = new DataOutputStream(baos);
			daos.writeShort(getVersion());
			daos.writeShort(getSource());
			daos.writeShort(getDestination());
			daos.writeShort(getLength());
			daos.write(getData());
			//            baos.write(getVersion()%256);
			//            baos.write(getVersion()/256);
			//            baos.write(getSource()%256);
			//            baos.write(getSource()/256);
			//            baos.write(getDestination()%256);
			//            baos.write(getDestination()/256);
			//            baos.write(getLength()%256);
			//            baos.write(getLength()/256);
			//            baos.write(getData());
			return baos.toByteArray();
		}

		public int getVersion() {
			return this.version;
		}

		public void setVersion(int version) {
			this.version = version;
		}

		public int getSource() {
			return this.source;
		}

		public void setSource(int source) {
			this.source = source;
		}

		public int getDestination() {
			return this.destination;
		}

		public void setDestination(int destination) {
			this.destination = destination;
		}

		public int getLength() {
			return this.length;
		}

		public void setLength(int length) {
			this.length = length;
		}

		public byte[] getData() {
			return this.data;
		}

		public void setData(byte[] data) {
			this.data = data;
		}
	} // class WPDU

	/********************************************************************************************************
	 * Invoke-Id-And-Priority byte setting
	 ********************************************************************************************************/

	public void setInvokeIdAndPriority(InvokeIdAndPriority iiap){
		this.invokeIdAndPriority = iiap;
	}

	public InvokeIdAndPriority getInvokeIdAndPriority(){
		return this.invokeIdAndPriority;
	}

	public int getMaxRetries() {
		return maxRetries;
	}

    public ApplicationServiceObject getApplicationServiceObject() {
        return null;
    }

} // public class TCPIPConnection