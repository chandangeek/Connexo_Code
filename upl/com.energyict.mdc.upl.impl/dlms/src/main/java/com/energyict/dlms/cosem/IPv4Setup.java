/**
 * 
 */
package com.energyict.dlms.cosem;

import java.io.IOException;

import com.energyict.dlms.ProtocolLink;
import com.energyict.dlms.axrdencoding.AXDRDecoder;
import com.energyict.dlms.axrdencoding.Array;
import com.energyict.dlms.axrdencoding.OctetString;
import com.energyict.dlms.axrdencoding.Unsigned32;

/**
 * @author gna
 *
 */
public class IPv4Setup extends AbstractCosemObject{
	
	private OctetString dl_Reference = null;
	private Unsigned32 ipAddress = null;
	private Array multicastIPAddress = null;		
	private Array ipOptions = null;					
	private Unsigned32 subnetMask = null;			
	private Unsigned32 gatewayIPAddress = null;		
	private boolean useDHCPFlag = true;				//TODO
	private Unsigned32 primaryDNSAddress = null;	//TODO
	private Unsigned32 secondaryDNSAddress = null;	//TODO
	

	/** Creates a new instance of IPv4Setup */
	public IPv4Setup(ProtocolLink protocolLink, ObjectReference objectReference) {
		super(protocolLink, objectReference);
	}

	@Override
	protected int getClassId() {
		return AbstractCosemObject.CLASSID_IPV4SETUP;
	}
	
	public OctetString readDLReference() throws IOException {
		if(this.dl_Reference == null){
			this.dl_Reference = new OctetString(getLNResponseData(2));
		}
		return this.dl_Reference;
	}
	
	public void writeDLReference(OctetString dlReference) throws IOException{
		write(2, dlReference.getBEREncodedByteArray());
		this.dl_Reference = dlReference;
	}
	
	public Unsigned32 readIPAddress() throws NumberFormatException, IOException{
		if(this.ipAddress == null){
			this.ipAddress = new Unsigned32(getLNResponseData(3), 0);
		}
		return this.ipAddress;
	}
	
	public String getIPAddress() throws NumberFormatException, IOException{
	   	StringBuilder builder = new StringBuilder();
    	for(int i = 1; i < readIPAddress().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readIPAddress().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}
	
	public void setIPAddress(String newIp) throws IOException{
		int pointer = 0;
		byte[] ipByte = new byte[5];
		ipByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < ipByte.length; i++){
			ipByte[i] = (byte)Integer.parseInt(newIp.substring(pointer, (newIp.indexOf(".", pointer) == -1)?newIp.length():newIp.indexOf(".", pointer)));
			pointer = newIp.indexOf(".", pointer) + 1;
		}
		writeIPAddress(new Unsigned32(ipByte, 0));
	}
	
	public void writeIPAddress(Unsigned32 newIp) throws IOException{
		write(3, newIp.getBEREncodedByteArray());
		this.ipAddress = newIp;
	}

	public static void main(String args[]){
		try {
			String str = "10.0.0.214";
			IPv4Setup ipv4 = new IPv4Setup(null, null);
			ipv4.setIPAddress(str);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Array readMulticastIPAddress() throws IOException{
		if(this.multicastIPAddress == null){
			this.multicastIPAddress = (Array)AXDRDecoder.decode(getLNResponseData(4));
			
		}
		return this.multicastIPAddress;
	}
	
	public void writeMulticastIPAddress(Array multicastIPAddress) throws IOException{
		write(4, multicastIPAddress.getBEREncodedByteArray());
		this.multicastIPAddress = multicastIPAddress;
	}
	
	public Array readIPOptions() throws IOException {
		if(this.ipOptions == null){
			this.ipOptions = (Array)AXDRDecoder.decode(getLNResponseData(5));
		}
		return this.ipOptions;
	}
	
	public void writeIPOptions(Array ipOptions) throws IOException {
		write(5, ipOptions.getBEREncodedByteArray());
		this.ipOptions = ipOptions;
	}
	
	public Unsigned32 readSubnetMask() throws IOException {
		if(this.subnetMask == null){
			this.subnetMask = new Unsigned32(getLNResponseData(6), 0);
		}
		return this.subnetMask;
	}
	
	public String getSubnetMask() throws IOException{
	   	StringBuilder builder = new StringBuilder();
    	for(int i = 1; i < readSubnetMask().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readSubnetMask().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}
	
	public void writeSubnetMask(Unsigned32 subnetMask) throws IOException{
		write(6, subnetMask.getBEREncodedByteArray());
		this.subnetMask = subnetMask;
	}
	
	public void setSubnetMask(String subnetMask) throws IOException {
		int pointer = 0;
		byte[] subnetByte = new byte[5];
		subnetByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < subnetByte.length; i++){
			subnetByte[i] = (byte)Integer.parseInt(subnetMask.substring(pointer, (subnetMask.indexOf(".", pointer) == -1)?subnetMask.length():subnetMask.indexOf(".", pointer)));
			pointer = subnetMask.indexOf(".", pointer) + 1;
		}
		writeSubnetMask(new Unsigned32(subnetByte, 0));
	}
	
	public Unsigned32 readGatewayIPAddress() throws IOException {
		if(this.gatewayIPAddress == null){
			this.gatewayIPAddress = new Unsigned32(getLNResponseData(7), 0);
		}
		return this.gatewayIPAddress;
	}
	
	public String getGatewayIPAddress() throws IOException{
	   	StringBuilder builder = new StringBuilder();
    	for(int i = 1; i < readGatewayIPAddress().getBEREncodedByteArray().length; i++){
    		if(i != 1){
    			builder.append(".");
    		}
    		builder.append(Integer.toString(readGatewayIPAddress().getBEREncodedByteArray()[i]&0xff));
    	}
    	return builder.toString();
	}
	
	public void writeGatewayIPAddress(Unsigned32 gatewayIPAddress) throws IOException{
		write(7, gatewayIPAddress.getBEREncodedByteArray());
		this.gatewayIPAddress = gatewayIPAddress;
	}
	
	public void setGatewayIPAddress(String gatewayIPAddress) throws IOException {
		int pointer = 0;
		byte[] gatewayByte = new byte[5];
		gatewayByte[0] = TYPEDESC_DOUBLE_LONG_UNSIGNED;
		for(int i = 1; i < gatewayByte.length; i++){
			gatewayByte[i] = (byte)Integer.parseInt(gatewayIPAddress.substring(pointer, (gatewayIPAddress.indexOf(".", pointer) == -1)?gatewayIPAddress.length():gatewayIPAddress.indexOf(".", pointer)));
			pointer = gatewayIPAddress.indexOf(".", pointer) + 1;
		}
		writeGatewayIPAddress(new Unsigned32(gatewayByte, 0));
	}
	
//	public boolean readDHCPFlag() throws IOException {
//		if(this.useDHCPFlag =){
//			
//		}
//	}
}
